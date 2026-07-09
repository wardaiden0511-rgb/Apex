package com.example.apexclient.module;

import com.example.apexclient.ApexConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AutoDrainModule extends Module {
    private static final int SCAN_RADIUS = 3;

    /**
     * Small cooldown so it can prepare quickly but does not spam slot packets every tick.
     */
    private static final long PREPARE_COOLDOWN_MS = 250L;

    private long lastPrepareMs = 0L;
    private BlockPos lastPreparedWaterPos = null;
    private int lastPreparedSlot = -1;

    public AutoDrainModule() {
        super("Auto Drain");
    }

    @Override
    protected void onEnable() {
        lastPrepareMs = 0L;
        lastPreparedWaterPos = null;
        lastPreparedSlot = -1;
    }

    @Override
    protected void onDisable() {
        lastPrepareMs = 0L;
        lastPreparedWaterPos = null;
        lastPreparedSlot = -1;
    }

    @Override
    public void tick(MinecraftClient client) {
        if (!isEnabled()) {
            return;
        }

        if (client == null || client.player == null || client.world == null) {
            return;
        }

        if (!ApexConfig.autoDrainWeb && !ApexConfig.autoDrainBlock && !ApexConfig.autoDrainBucket) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastPrepareMs < PREPARE_COOLDOWN_MS) {
            return;
        }

        ClientPlayerEntity player = client.player;

        BlockPos waterPos = getCrosshairEnemyWater(client, player);
        if (waterPos == null) {
            waterPos = findEnemyWaterSource(client, player);
        }

        if (waterPos == null) {
            lastPreparedWaterPos = null;
            lastPreparedSlot = -1;
            return;
        }

        int slot = chooseDrainSlot(player);
        if (slot == -1) {
            return;
        }

        // If we already prepared this exact water + slot recently, do not keep resending packets.
        if (lastPreparedWaterPos != null && lastPreparedWaterPos.equals(waterPos) && lastPreparedSlot == slot) {
            lookAtBlock(player, waterPos);
            lastPrepareMs = now;
            return;
        }

        selectHotbarSlot(player, slot);
        lookAtBlock(player, waterPos);

        lastPreparedWaterPos = waterPos;
        lastPreparedSlot = slot;
        lastPrepareMs = now;
    }

    /**
     * Priority:
     * 1. Empty bucket if Bucket Drain is enabled.
     * 2. Cobweb if Web Drain is enabled.
     * 3. Any placeable block if Block Drain is enabled.
     */
    private int chooseDrainSlot(ClientPlayerEntity player) {
        if (ApexConfig.autoDrainBucket) {
            int bucketSlot = findEmptyBucketSlot(player);
            if (bucketSlot != -1) {
                return bucketSlot;
            }
        }

        if (ApexConfig.autoDrainWeb) {
            int webSlot = findCobwebSlot(player);
            if (webSlot != -1) {
                return webSlot;
            }
        }

        if (ApexConfig.autoDrainBlock) {
            int blockSlot = findHotbarPlaceableBlock(player);
            if (blockSlot != -1) {
                return blockSlot;
            }
        }

        return -1;
    }

    private BlockPos getCrosshairEnemyWater(MinecraftClient client, ClientPlayerEntity player) {
        HitResult target = client.crosshairTarget;
        if (!(target instanceof BlockHitResult blockHitResult)) {
            return null;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        if (!isWaterSource(client, pos)) {
            return null;
        }

        if (!isWaterNearEnemy(client, player, pos)) {
            return null;
        }

        return pos;
    }

    private boolean isWaterNearEnemy(MinecraftClient client, ClientPlayerEntity player, BlockPos waterPos) {
        double range = ApexConfig.clampAutoDrainRange(ApexConfig.autoDrainRange);
        double rangeSq = range * range;

        for (PlayerEntity otherPlayer : client.world.getPlayers()) {
            if (otherPlayer == null || otherPlayer == player) {
                continue;
            }

            if (!otherPlayer.isAlive() || otherPlayer.isSpectator()) {
                continue;
            }

            if (player.squaredDistanceTo(otherPlayer) > rangeSq) {
                continue;
            }

            double waterToEnemySq = waterPos.getSquaredDistance(
                    otherPlayer.getX(),
                    otherPlayer.getY(),
                    otherPlayer.getZ()
            );

            if (waterToEnemySq <= rangeSq) {
                return true;
            }
        }

        return false;
    }

    private BlockPos findEnemyWaterSource(MinecraftClient client, ClientPlayerEntity player) {
        double range = ApexConfig.clampAutoDrainRange(ApexConfig.autoDrainRange);
        double rangeSq = range * range;

        BlockPos bestWater = null;
        double bestDistanceSq = rangeSq;

        for (PlayerEntity otherPlayer : client.world.getPlayers()) {
            if (otherPlayer == null || otherPlayer == player) {
                continue;
            }

            if (!otherPlayer.isAlive() || otherPlayer.isSpectator()) {
                continue;
            }

            if (player.squaredDistanceTo(otherPlayer) > rangeSq) {
                continue;
            }

            BlockPos base = otherPlayer.getBlockPos();

            for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
                for (int dy = -1; dy <= 2; dy++) {
                    for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                        BlockPos pos = base.add(dx, dy, dz);

                        if (!isWaterSource(client, pos)) {
                            continue;
                        }

                        double distToEnemySq = pos.getSquaredDistance(
                                otherPlayer.getX(),
                                otherPlayer.getY(),
                                otherPlayer.getZ()
                        );
                        if (distToEnemySq > rangeSq) {
                            continue;
                        }

                        double distToYouSq = pos.getSquaredDistance(
                                player.getX(),
                                player.getY(),
                                player.getZ()
                        );
                        if (distToYouSq < bestDistanceSq) {
                            bestDistanceSq = distToYouSq;
                            bestWater = pos;
                        }
                    }
                }
            }
        }

        return bestWater;
    }

    private boolean isWaterSource(MinecraftClient client, BlockPos pos) {
        BlockState state = client.world.getBlockState(pos);
        return state.getFluidState().isOf(Fluids.WATER) && state.getFluidState().isStill();
    }

    private void selectHotbarSlot(ClientPlayerEntity player, int slot) {
        if (slot < 0 || slot > 8) {
            return;
        }

        if (player.getInventory().getSelectedSlot() == slot) {
            return;
        }

        // Swap via packet and client-side for reliable sync
        player.getInventory().setSelectedSlot(slot);
        player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }

    private void lookAtBlock(ClientPlayerEntity player, BlockPos pos) {
        Vec3d target = Vec3d.ofCenter(pos);

        double dx = target.x - player.getX();
        double dy = target.y - player.getEyeY();
        double dz = target.z - player.getZ();

        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));

        player.setYaw(yaw);
        player.setPitch(MathHelper.clamp(pitch, -90.0F, 90.0F));
        player.setHeadYaw(yaw);
    }

    private int findEmptyBucketSlot(ClientPlayerEntity player) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isEmpty() && stack.isOf(Items.BUCKET)) {
                return slot;
            }
        }

        return -1;
    }

    private int findCobwebSlot(ClientPlayerEntity player) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isEmpty() && stack.isOf(Items.COBWEB)) {
                return slot;
            }
        }

        return -1;
    }

    private int findHotbarPlaceableBlock(ClientPlayerEntity player) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (!(stack.getItem() instanceof BlockItem blockItem)) {
                continue;
            }

            Block block = blockItem.getBlock();

            // Cobweb has its own Web Drain mode.
            if (block == Blocks.COBWEB) {
                continue;
            }

            // Avoid weird/unhelpful blocks.
            if (block == Blocks.AIR || block == Blocks.WATER || block == Blocks.LAVA) {
                continue;
            }

            return slot;
        }

        return -1;
    }
}