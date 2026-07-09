package com.example.apexclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class StunSlamModule extends Module {
    private boolean shieldBroken = false;
    private int swapCooldownTicks = 0;
    private LivingEntity targetEntity = null;
    private boolean swappedToMace = false;

    public StunSlamModule() {
        super("Stun Slam");
    }

    @Override
    protected void onEnable() {
        shieldBroken = false;
        swapCooldownTicks = 0;
        targetEntity = null;
        swappedToMace = false;
    }

    @Override
    protected void onDisable() {
        shieldBroken = false;
        swapCooldownTicks = 0;
        targetEntity = null;
        swappedToMace = false;
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

        // Handle swap cooldown - attack after swap is complete
        if (swapCooldownTicks > 0) {
            swapCooldownTicks--;
            if (swapCooldownTicks == 0 && shieldBroken && targetEntity != null && swappedToMace) {
                // Attack with mace now that swap is complete
                attackWithMace(client, player, targetEntity);
                shieldBroken = false;
                targetEntity = null;
                swappedToMace = false;
            }
            return;
        }

        // Check if player is holding an axe
        if (!isHoldingAxe(player)) {
            return;
        }

        // Find nearest enemy
        LivingEntity target = findNearestEnemy(client, player);
        if (target == null) {
            return;
        }

        // Check if target has shield
        if (!targetHasShield(target)) {
            return;
        }

        // Check if shield was just broken (axe shield break)
        if (wasShieldBroken(client, player, target)) {
            shieldBroken = true;
            targetEntity = target;
            swappedToMace = false;
            swapToMace(client, player);
            swapCooldownTicks = (int) com.example.apexclient.ApexConfig.stunSlamSwapDelayTicks;
        }
    }

    private boolean isHoldingAxe(ClientPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        return !stack.isEmpty() && stack.getItem() instanceof AxeItem;
    }

    private LivingEntity findNearestEnemy(MinecraftClient client, ClientPlayerEntity player) {
        LivingEntity nearest = null;
        double nearestDistanceSq = Double.MAX_VALUE;

        for (var entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            if (entity == player) {
                continue;
            }

            if (!entity.isAlive()) {
                continue;
            }

            // Check if within range
            double distanceSq = player.squaredDistanceTo(entity);
            if (distanceSq > 9.0) { // 3 blocks range
                continue;
            }

            if (distanceSq < nearestDistanceSq) {
                nearest = livingEntity;
                nearestDistanceSq = distanceSq;
            }
        }

        return nearest;
    }

    private boolean targetHasShield(LivingEntity target) {
        if (target instanceof PlayerEntity targetPlayer) {
            ItemStack offHand = targetPlayer.getOffHandStack();
            ItemStack mainHand = targetPlayer.getMainHandStack();
            return offHand.getItem() == Items.SHIELD || mainHand.getItem() == Items.SHIELD;
        }
        return false;
    }

    private boolean wasShieldBroken(MinecraftClient client, ClientPlayerEntity player, LivingEntity target) {
        // Check if attack cooldown is ready and looking at target
        return player.getAttackCooldownProgress(0.0F) >= 0.9F && isLookingAtTarget(player, target);
    }

    private boolean isLookingAtTarget(ClientPlayerEntity player, LivingEntity target) {
        // Simple check if player is looking at the target
        double dx = target.getX() - player.getX();
        double dz = target.getZ() - player.getZ();
        double angle = Math.toDegrees(Math.atan2(dz, dx));
        double playerYaw = player.getYaw() % 360;
        if (playerYaw < 0) playerYaw += 360;

        double angleDiff = Math.abs(angle - playerYaw);
        return angleDiff < 30 || angleDiff > 330;
    }

    private void swapToMace(MinecraftClient client, ClientPlayerEntity player) {
        int maceSlot = findMaceSlot(player);
        if (maceSlot == -1) {
            return;
        }

        // Swap to mace via packet and client-side
        player.getInventory().setSelectedSlot(maceSlot);
        if (player.networkHandler != null) {
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(maceSlot));
        }
        swappedToMace = true;
    }

    private void attackWithMace(MinecraftClient client, ClientPlayerEntity player, LivingEntity target) {
        // Attack with mace
        if (player.networkHandler != null) {
            player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(target, player.isSneaking()));
        }
        player.swingHand(Hand.MAIN_HAND);
    }

    private int findMaceSlot(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.MACE) {
                return i;
            }
        }
        return -1;
    }
}
