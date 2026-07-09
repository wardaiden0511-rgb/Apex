package com.example.apexclient.module;

import com.example.apexclient.ApexConfig;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class PressureWebModule extends Module {
    private int webCooldownTicks = 0;
    private int swapCooldownTicks = 0;
    private int originalSlot = -1;
    private int webSlot = -1;
    private BlockPos pendingWebPos = null;
    private int placeStage = 0; // 0=idle, 1=swapped, 2=placing, 3=restoring

    public PressureWebModule() {
        super("Pressure Web");
    }

    @Override
    protected void onEnable() {
        resetState();
    }

    @Override
    protected void onDisable() {
        resetState();
    }

    private void resetState() {
        webCooldownTicks = 0;
        swapCooldownTicks = 0;
        originalSlot = -1;
        webSlot = -1;
        pendingWebPos = null;
        placeStage = 0;
    }

    @Override
    public void tick(MinecraftClient client) {
        if (!isEnabled()) {
            return;
        }

        if (!ApexConfig.pressureWebEnabled) {
            return;
        }

        if (client == null || client.player == null || client.world == null || client.interactionManager == null) {
            return;
        }

        ClientPlayerEntity player = client.player;

        // Stage 3: Restore original slot after placement
        if (placeStage == 3) {
            if (originalSlot >= 0 && originalSlot <= 8) {
                player.getInventory().setSelectedSlot(originalSlot);
                if (player.networkHandler != null) {
                    player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
                }
            }
            resetState();
            return;
        }

        // Stage 2: Place the web (one tick after swap to ensure server knows our slot)
        if (placeStage == 2) {
            if (swapCooldownTicks > 0) {
                swapCooldownTicks--;
                return;
            }

            if (pendingWebPos != null && client.interactionManager != null) {
                BlockPos webPos = pendingWebPos.up();

                // Only place if the space above the pressure plate is air or replaceable
                if (client.world.getBlockState(webPos).isReplaceable()) {
                    // Build a proper BlockHitResult for the block face above the pressure plate
                    Vec3d hitVec = Vec3d.ofCenter(webPos).add(0, -0.5, 0);
                    BlockHitResult placeHit = new BlockHitResult(
                            hitVec,
                            Direction.UP,
                            pendingWebPos,
                            false
                    );

                    client.interactionManager.interactBlock(player, Hand.MAIN_HAND, placeHit);
                    player.swingHand(Hand.MAIN_HAND);
                }
            }

            placeStage = 3; // Move to restore stage next tick
            return;
        }

        // Stage 1: We just swapped to cobweb, wait a tick for sync
        if (placeStage == 1) {
            if (swapCooldownTicks > 0) {
                swapCooldownTicks--;
                return;
            }
            placeStage = 2;
            swapCooldownTicks = 1; // One more tick before placing
            return;
        }

        // Stage 0: Detect pressure plate under crosshair and initiate swap
        // Handle placement cooldown between attempts
        if (webCooldownTicks > 0) {
            webCooldownTicks--;
            return;
        }

        // Check what's under the crosshair
        HitResult hitResult = client.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            return;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        if (!isPressurePlate(client, pos)) {
            return;
        }

        // Check if space above is placeable
        BlockPos above = pos.up();
        if (!client.world.getBlockState(above).isReplaceable()) {
            return;
        }

        // Find cobweb in hotbar
        int foundWebSlot = findCobwebSlot(player);
        if (foundWebSlot == -1) {
            return;
        }

        // Don't trigger if we're already holding cobweb
        if (player.getInventory().getSelectedSlot() == foundWebSlot) {
            return;
        }

        // Initiate: save original slot, swap to cobweb
        originalSlot = player.getInventory().getSelectedSlot();
        webSlot = foundWebSlot;
        pendingWebPos = pos;

        // Swap to cobweb
        player.getInventory().setSelectedSlot(webSlot);
        if (player.networkHandler != null) {
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(webSlot));
        }

        placeStage = 1;
        swapCooldownTicks = (int) ApexConfig.pressureWebSwapSpeedTicks;
        webCooldownTicks = (int) ApexConfig.pressureWebDelayTicks + 10; // Cooldown before next trigger
    }

    private boolean isPressurePlate(MinecraftClient client, BlockPos pos) {
        var block = client.world.getBlockState(pos).getBlock();
        return block == Blocks.STONE_PRESSURE_PLATE
                || block == Blocks.OAK_PRESSURE_PLATE
                || block == Blocks.SPRUCE_PRESSURE_PLATE
                || block == Blocks.BIRCH_PRESSURE_PLATE
                || block == Blocks.JUNGLE_PRESSURE_PLATE
                || block == Blocks.ACACIA_PRESSURE_PLATE
                || block == Blocks.DARK_OAK_PRESSURE_PLATE
                || block == Blocks.MANGROVE_PRESSURE_PLATE
                || block == Blocks.CHERRY_PRESSURE_PLATE
                || block == Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE
                || block == Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE
                || block == Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE;
    }

    private int findCobwebSlot(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.COBWEB) {
                return i;
            }
        }
        return -1;
    }
}
