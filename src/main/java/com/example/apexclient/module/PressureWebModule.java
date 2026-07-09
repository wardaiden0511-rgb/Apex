package com.example.apexclient.module;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PressureWebModule extends Module {
    private BlockPos lastPressurePlatePos = null;
    private boolean shouldPlaceWeb = false;
    private int webCooldownTicks = 0;
    private int swapCooldownTicks = 0;
    private BlockPos pendingWebPos = null;

    public PressureWebModule() {
        super("Pressure Web");
    }

    @Override
    protected void onEnable() {
        lastPressurePlatePos = null;
        shouldPlaceWeb = false;
        webCooldownTicks = 0;
        swapCooldownTicks = 0;
        pendingWebPos = null;
    }

    @Override
    protected void onDisable() {
        lastPressurePlatePos = null;
        shouldPlaceWeb = false;
        webCooldownTicks = 0;
        swapCooldownTicks = 0;
        pendingWebPos = null;
    }

    @Override
    public void tick(MinecraftClient client) {
        if (!isEnabled()) {
            return;
        }

        if (client == null || client.player == null || client.world == null || client.interactionManager == null) {
            return;
        }

        ClientPlayerEntity player = client.player;

        // Handle swap cooldown - place web after swap is complete
        if (swapCooldownTicks > 0) {
            swapCooldownTicks--;
            if (swapCooldownTicks == 0 && pendingWebPos != null) {
                // Place web now that swap is complete
                BlockPos webPos = pendingWebPos.up();
                if (client.interactionManager != null) {
                    client.interactionManager.interactBlock(player, Hand.MAIN_HAND, new BlockHitResult(
                            player.getEyePos(),
                            Direction.UP,
                            webPos,
                            false
                    ));
                }
                pendingWebPos = null;
            }
            return;
        }

        // Handle web placement cooldown
        if (webCooldownTicks > 0) {
            webCooldownTicks--;
            if (webCooldownTicks == 0 && shouldPlaceWeb && lastPressurePlatePos != null) {
                placeWeb(client, player, lastPressurePlatePos);
                shouldPlaceWeb = false;
            }
            return;
        }

        // Check if player placed a pressure plate
        HitResult hitResult = client.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            return;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        if (isPressurePlate(client, pos)) {
            // Check if this is a new pressure plate placement
            if (lastPressurePlatePos == null || !lastPressurePlatePos.equals(pos)) {
                lastPressurePlatePos = pos;
                shouldPlaceWeb = true;
                webCooldownTicks = 2; // Wait 2 ticks before placing web
            }
        }
    }

    private boolean isPressurePlate(MinecraftClient client, BlockPos pos) {
        return client.world.getBlockState(pos).getBlock() == Blocks.STONE_PRESSURE_PLATE
                || client.world.getBlockState(pos).getBlock() == Blocks.OAK_PRESSURE_PLATE
                || client.world.getBlockState(pos).getBlock() == Blocks.SPRUCE_PRESSURE_PLATE
                || client.world.getBlockState(pos).getBlock() == Blocks.BIRCH_PRESSURE_PLATE
                || client.world.getBlockState(pos).getBlock() == Blocks.JUNGLE_PRESSURE_PLATE
                || client.world.getBlockState(pos).getBlock() == Blocks.ACACIA_PRESSURE_PLATE
                || client.world.getBlockState(pos).getBlock() == Blocks.DARK_OAK_PRESSURE_PLATE
                || client.world.getBlockState(pos).getBlock() == Blocks.MANGROVE_PRESSURE_PLATE
                || client.world.getBlockState(pos).getBlock() == Blocks.CHERRY_PRESSURE_PLATE
                || client.world.getBlockState(pos).getBlock() == Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE
                || client.world.getBlockState(pos).getBlock() == Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE
                || client.world.getBlockState(pos).getBlock() == Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE;
    }

    private void placeWeb(MinecraftClient client, ClientPlayerEntity player, BlockPos pos) {
        // Find cobweb in hotbar
        int webSlot = findCobwebSlot(player);
        if (webSlot == -1) {
            return;
        }

        // Swap to cobweb via packet and client-side
        player.getInventory().setSelectedSlot(webSlot);
        if (player.networkHandler != null) {
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(webSlot));
        }

        // Set pending web position and start swap cooldown
        pendingWebPos = pos;
        swapCooldownTicks = (int) com.example.apexclient.ApexConfig.pressureWebSwapSpeedTicks;
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
