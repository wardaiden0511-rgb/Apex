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
    // Shield break detection state
    private boolean wasTargetBlocking = false;
    private PlayerEntity trackedTarget = null;
    private int ticksSinceAttack = 0;
    private boolean pendingShieldBreakCheck = false;

    // Mace swap + attack state
    private boolean shieldBroken = false;
    private int swapCooldownTicks = 0;
    private LivingEntity targetEntity = null;
    private boolean swappedToMace = false;
    private int maceSlotCache = -1;

    // Anti-spam: prevent re-triggering on same target too quickly
    private long lastTriggerTime = 0;
    private static final long TRIGGER_COOLDOWN_MS = 500;

    public StunSlamModule() {
        super("Stun Slam");
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
        wasTargetBlocking = false;
        trackedTarget = null;
        ticksSinceAttack = 0;
        pendingShieldBreakCheck = false;
        shieldBroken = false;
        swapCooldownTicks = 0;
        targetEntity = null;
        swappedToMace = false;
        maceSlotCache = -1;
        lastTriggerTime = 0;
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

        // Handle post-swap attack sequence
        if (swapCooldownTicks > 0) {
            swapCooldownTicks--;
            if (swapCooldownTicks == 0 && shieldBroken && targetEntity != null && swappedToMace) {
                // Attack with mace now that swap is complete
                attackWithMace(client, player, targetEntity);
                resetState();
            }
            return;
        }

        // Anti-spam cooldown
        long now = System.currentTimeMillis();
        if (now - lastTriggerTime < TRIGGER_COOLDOWN_MS) {
            return;
        }

        // Must be holding an axe
        if (!isHoldingAxe(player)) {
            // Reset tracking if we swapped off axe
            wasTargetBlocking = false;
            trackedTarget = null;
            pendingShieldBreakCheck = false;
            return;
        }

        // Find nearest enemy player
        PlayerEntity target = findNearestEnemyPlayer(client, player);
        if (target == null) {
            wasTargetBlocking = false;
            trackedTarget = null;
            pendingShieldBreakCheck = false;
            return;
        }

        // Track shield blocking state changes to detect shield breaks
        boolean isCurrentlyBlocking = isBlockingWithShield(target);

        if (trackedTarget == target) {
            // We're tracking this target - check for shield break
            if (pendingShieldBreakCheck) {
                ticksSinceAttack++;

                // If they WERE blocking and now they're NOT, shield was broken
                if (wasTargetBlocking && !isCurrentlyBlocking) {
                    // SHIELD BREAK DETECTED!
                    onShieldBroken(client, player, target);
                    return;
                }

                // Give up after a few ticks if no break detected
                if (ticksSinceAttack > 8) {
                    pendingShieldBreakCheck = false;
                    wasTargetBlocking = isCurrentlyBlocking;
                }
            } else {
                // Start tracking if they raise their shield
                wasTargetBlocking = isCurrentlyBlocking;
            }
        } else {
            // New target - start fresh tracking
            trackedTarget = target;
            wasTargetBlocking = isCurrentlyBlocking;
            pendingShieldBreakCheck = false;
        }

        // If target is blocking with shield and we're ready to attack,
        // mark that we're about to hit them - this sets up the shield break detection
        if (isCurrentlyBlocking && isReadyToAttack(player) && isLookingAtTarget(player, target)) {
            pendingShieldBreakCheck = true;
            ticksSinceAttack = 0;
            wasTargetBlocking = true;
        }
    }

    /**
     * Called when we detect a shield has been broken.
     * Instantly swaps to mace and attacks.
     */
    private void onShieldBroken(MinecraftClient client, ClientPlayerEntity player, PlayerEntity target) {
        shieldBroken = true;
        targetEntity = target;
        swappedToMace = false;
        lastTriggerTime = System.currentTimeMillis();

        // Find mace in hotbar
        int maceSlot = findMaceSlot(player);
        if (maceSlot == -1) {
            // No mace found, just attack with axe instead
            attackWithAxe(player, target);
            resetState();
            return;
        }

        // INSTANT SWAP TO MACE
        swapToMace(player, maceSlot);
        swappedToMace = true;

        // INSTANT ATTACK with mace (0 tick delay - immediate)
        // The swap packet + attack packet are sent in the same tick
        attackWithMace(client, player, target);

        // Reset all state
        resetState();
    }

    private boolean isHoldingAxe(ClientPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        return !stack.isEmpty() && stack.getItem() instanceof AxeItem;
    }

    private PlayerEntity findNearestEnemyPlayer(MinecraftClient client, ClientPlayerEntity player) {
        PlayerEntity nearest = null;
        double nearestDistanceSq = Double.MAX_VALUE;
        double maxRangeSq = 9.0; // 3 blocks range

        for (var entity : client.world.getEntities()) {
            if (!(entity instanceof PlayerEntity targetPlayer)) {
                continue;
            }

            if (entity == player) {
                continue;
            }

            if (!entity.isAlive()) {
                continue;
            }

            if (targetPlayer.isSpectator()) {
                continue;
            }

            double distanceSq = player.squaredDistanceTo(entity);
            if (distanceSq > maxRangeSq) {
                continue;
            }

            if (distanceSq < nearestDistanceSq) {
                nearest = targetPlayer;
                nearestDistanceSq = distanceSq;
            }
        }

        return nearest;
    }

    private boolean isBlockingWithShield(PlayerEntity target) {
        if (!target.isBlocking()) {
            return false;
        }

        ItemStack activeStack = target.getActiveItem();
        return !activeStack.isEmpty() && activeStack.isOf(Items.SHIELD);
    }

    private boolean isReadyToAttack(ClientPlayerEntity player) {
        return player.getAttackCooldownProgress(0.0F) >= 0.95F;
    }

    private boolean isLookingAtTarget(ClientPlayerEntity player, LivingEntity target) {
        double dx = target.getX() - player.getX();
        double dz = target.getZ() - player.getZ();
        double angle = Math.toDegrees(Math.atan2(dz, dx));
        double playerYaw = player.getYaw() % 360;
        if (playerYaw < 0) playerYaw += 360;

        double angleDiff = Math.abs(angle - playerYaw);
        return angleDiff < 45 || angleDiff > 315;
    }

    private void swapToMace(ClientPlayerEntity player, int maceSlot) {
        // Swap to mace via packet and client-side
        player.getInventory().setSelectedSlot(maceSlot);
        if (player.networkHandler != null) {
            player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(maceSlot));
        }
    }

    private void attackWithMace(MinecraftClient client, ClientPlayerEntity player, LivingEntity target) {
        // Attack with mace - send packet and swing
        if (player.networkHandler != null) {
            player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(target, player.isSneaking()));
        }
        player.swingHand(Hand.MAIN_HAND);
    }

    private void attackWithAxe(ClientPlayerEntity player, LivingEntity target) {
        // Fallback: attack with axe if no mace available
        if (player.networkHandler != null) {
            player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(target, player.isSneaking()));
        }
        player.swingHand(Hand.MAIN_HAND);
    }

    private int findMaceSlot(ClientPlayerEntity player) {
        // Check cached slot first
        if (maceSlotCache >= 0 && maceSlotCache < 9) {
            ItemStack cachedStack = player.getInventory().getStack(maceSlotCache);
            if (cachedStack.getItem() == Items.MACE) {
                return maceSlotCache;
            }
        }

        // Search all hotbar slots
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.MACE) {
                maceSlotCache = i;
                return i;
            }
        }

        maceSlotCache = -1;
        return -1;
    }
}
