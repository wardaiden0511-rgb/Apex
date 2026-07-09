package com.example.apexclient.module;

import com.example.apexclient.ApexConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class StunSlamModule extends Module {
    private int originalSlot = -1;
    private int maceSlot = -1;
    private int swapCooldownTicks = 0;
    private int attackCooldownTicks = 0;
    private int state = 0; // 0=idle, 1=swapped, 2=attack-ready, 3=restoring
    private LivingEntity targetEntity = null;

    // Shield break tracking
    private boolean targetWasBlocking = false;
    private int shieldBreakTickCounter = 0;
    private static final int SHIELD_BREAK_WINDOW_TICKS = 8; // Time after shield break to react

    public StunSlamModule() {
        super("Stun Slam");
    }

    @Override
    protected void onEnable() {
        resetState();
    }

    @Override
    protected void onDisable() {
        // If disabled mid-sequence, restore hotbar
        if (state > 0 && state < 3 && originalSlot >= 0) {
            restoreOriginalSlot();
        }
        resetState();
    }

    private void resetState() {
        originalSlot = -1;
        maceSlot = -1;
        swapCooldownTicks = 0;
        attackCooldownTicks = 0;
        state = 0;
        targetEntity = null;
        targetWasBlocking = false;
        shieldBreakTickCounter = 0;
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

        // State 3: Restore original slot
        if (state == 3) {
            restoreOriginalSlot();
            resetState();
            return;
        }

        // State 2: Attack with mace
        if (state == 2) {
            if (attackCooldownTicks > 0) {
                attackCooldownTicks--;
                return;
            }

            if (targetEntity != null && targetEntity.isAlive() && player.networkHandler != null) {
                // Attack the target with mace
                player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(targetEntity, player.isSneaking()));
                player.swingHand(Hand.MAIN_HAND);
            }

            state = 3; // Restore next tick
            return;
        }

        // State 1: Wait for swap to sync, then prepare to attack
        if (state == 1) {
            if (swapCooldownTicks > 0) {
                swapCooldownTicks--;
                return;
            }
            state = 2;
            attackCooldownTicks = (int) ApexConfig.stunSlamAttackSpeedTicks;
            return;
        }

        // State 0: Monitor for shield break opportunity
        // Check if player is holding an axe
        if (!isHoldingAxe(player)) {
            return;
        }

        // Get target under crosshair (prioritize what player is aiming at)
        LivingEntity target = getCrosshairTarget(client);
        if (target == null) {
            target = findNearestEnemyWithShield(client, player);
        }

        if (target == null || !target.isAlive() || target == player) {
            targetWasBlocking = false;
            shieldBreakTickCounter = 0;
            return;
        }

        // Check distance
        double horizontalDist = Math.sqrt(
                Math.pow(target.getX() - player.getX(), 2) +
                        Math.pow(target.getZ() - player.getZ(), 2)
        );
        if (horizontalDist > ApexConfig.stunSlamHorizontalDistance + 1.0) {
            targetWasBlocking = false;
            return;
        }

        boolean isBlocking = targetIsBlockingWithShield(target);

        // Detect shield break: target WAS blocking but is NOT blocking anymore
        if (targetWasBlocking && !isBlocking) {
            // Shield was just broken/disabled!
            shieldBreakTickCounter++;

            if (shieldBreakTickCounter >= 1) { // React immediately on first detection
                int foundMaceSlot = findMaceSlot(player);
                if (foundMaceSlot == -1) {
                    return; // No mace in hotbar
                }

                // Don't trigger if already holding mace
                if (player.getInventory().getSelectedSlot() == foundMaceSlot) {
                    return;
                }

                originalSlot = player.getInventory().getSelectedSlot();
                maceSlot = foundMaceSlot;
                targetEntity = target;

                // Swap to mace
                player.getInventory().setSelectedSlot(maceSlot);
                if (player.networkHandler != null) {
                    player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(maceSlot));
                }

                state = 1;
                swapCooldownTicks = (int) ApexConfig.stunSlamSwapDelayTicks;
                targetWasBlocking = false;
                shieldBreakTickCounter = 0;
                return;
            }
        } else {
            shieldBreakTickCounter = 0;
        }

        // Update tracking state
        targetWasBlocking = isBlocking;
    }

    private void restoreOriginalSlot() {
        if (originalSlot >= 0 && originalSlot <= 8) {
            ClientPlayerEntity player = net.minecraft.client.MinecraftClient.getInstance().player;
            if (player != null) {
                player.getInventory().setSelectedSlot(originalSlot);
                if (player.networkHandler != null) {
                    player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
                }
            }
        }
    }

    private boolean isHoldingAxe(ClientPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        return !stack.isEmpty() && stack.getItem() instanceof AxeItem;
    }

    private LivingEntity getCrosshairTarget(MinecraftClient client) {
        HitResult hitResult = client.crosshairTarget;
        if (!(hitResult instanceof EntityHitResult entityHitResult)) {
            return null;
        }
        Entity entity = entityHitResult.getEntity();
        if (!(entity instanceof LivingEntity livingEntity) || entity == client.player) {
            return null;
        }
        if (!livingEntity.isAlive()) {
            return null;
        }
        return livingEntity;
    }

    private LivingEntity findNearestEnemyWithShield(MinecraftClient client, ClientPlayerEntity player) {
        LivingEntity nearest = null;
        double nearestDistanceSq = Double.MAX_VALUE;
        double maxRangeSq = (ApexConfig.stunSlamHorizontalDistance + 1.0) *
                (ApexConfig.stunSlamHorizontalDistance + 1.0);

        for (var entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }
            if (entity == player || !entity.isAlive()) {
                continue;
            }

            double distanceSq = player.squaredDistanceTo(entity);
            if (distanceSq > maxRangeSq) {
                continue;
            }

            if (!targetIsBlockingWithShield(livingEntity)) {
                continue;
            }

            if (distanceSq < nearestDistanceSq) {
                nearest = livingEntity;
                nearestDistanceSq = distanceSq;
            }
        }

        return nearest;
    }

    private boolean targetIsBlockingWithShield(LivingEntity target) {
        if (!(target instanceof PlayerEntity targetPlayer)) {
            return false;
        }
        return targetPlayer.isBlocking() && targetPlayer.getActiveItem().isOf(Items.SHIELD);
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
