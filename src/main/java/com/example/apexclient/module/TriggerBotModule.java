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
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class TriggerBotModule extends Module {
    /**
     * Hard max reach for Triggerbot.
     * It will not attack targets farther than this.
     */
    private static final double MAX_REACH = 2.8;
    private static final double MAX_REACH_SQ = MAX_REACH * MAX_REACH;

    /**
     * 1.0F means fully cooled down.
     * This makes sword/axe timing follow Minecraft's real item cooldown.
     */
    private static final float ATTACK_COOLDOWN_THRESHOLD = 1.0F;

    private long nextAttackTimeMs = 0L;

    public TriggerBotModule() {
        super("Triggerbot");
    }

    @Override
    protected void onEnable() {
        nextAttackTimeMs = 0L;
    }

    @Override
    protected void onDisable() {
        nextAttackTimeMs = 0L;
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

        if (ApexConfig.triggerBotWeaponOnly && !isHoldingWeapon(player)) {
            return;
        }

        // Respect vanilla cooldown first.
        if (!isAttackCooledDown(player)) {
            return;
        }

        // Check random delay - add human-like variation between attacks
        long now = System.currentTimeMillis();
        if (now < nextAttackTimeMs) {
            return;
        }

        LivingEntity target = getTarget(client, player);
        if (target == null || !isValidTarget(player, target)) {
            return;
        }

        // Schedule next attack with random delay for humanization
        scheduleNextAttack();

        // Send attack packet directly instead of using interactionManager
        sendAttackPacket(player, target);
        player.swingHand(Hand.MAIN_HAND);
    }

    private void scheduleNextAttack() {
        double randomDelayMs = ApexConfig.triggerBotRandomDelayMs;
        if (randomDelayMs > 0) {
            // Add 0-100% of configured random delay
            long jitter = (long) (Math.random() * randomDelayMs);
            nextAttackTimeMs = System.currentTimeMillis() + jitter;
        }
    }

    private boolean isAttackCooledDown(ClientPlayerEntity player) {
        return player.getAttackCooldownProgress(0.0F) >= ATTACK_COOLDOWN_THRESHOLD;
    }

    private LivingEntity getTarget(MinecraftClient client, ClientPlayerEntity player) {
        LivingEntity target = getCrosshairTarget(client);

        if (target != null) {
            return target;
        }

        if (!ApexConfig.triggerBotRequireCrosshair) {
            return findNearestValidTarget(client, player);
        }

        return null;
    }

    private LivingEntity getCrosshairTarget(MinecraftClient client) {
        HitResult hitResult = client.crosshairTarget;

        if (!(hitResult instanceof EntityHitResult entityHitResult)) {
            return null;
        }

        Entity entity = entityHitResult.getEntity();

        if (!(entity instanceof LivingEntity livingEntity)) {
            return null;
        }

        return livingEntity;
    }

    private LivingEntity findNearestValidTarget(MinecraftClient client, ClientPlayerEntity player) {
        LivingEntity nearest = null;
        double nearestDistanceSq = MAX_REACH_SQ;

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            if (!isValidTarget(player, livingEntity)) {
                continue;
            }

            double distanceSq = player.squaredDistanceTo(livingEntity);
            if (distanceSq <= nearestDistanceSq) {
                nearest = livingEntity;
                nearestDistanceSq = distanceSq;
            }
        }

        return nearest;
    }

    private boolean isValidTarget(ClientPlayerEntity player, LivingEntity target) {
        if (target == null || target == player) {
            return false;
        }

        if (!target.isAlive()) {
            return false;
        }

        // Hard reach limit.
        if (player.squaredDistanceTo(target) > MAX_REACH_SQ) {
            return false;
        }

        if (target instanceof PlayerEntity targetPlayer) {
            if (targetPlayer.isSpectator()) {
                return false;
            }
        } else if (ApexConfig.triggerBotPlayersOnly) {
            return false;
        }

        if (ApexConfig.triggerBotRequireVisible && !player.canSee(target)) {
            return false;
        }

        return true;
    }

    private boolean isHoldingWeapon(ClientPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();

        if (stack.isEmpty()) {
            return false;
        }

        if (stack.getItem() instanceof AxeItem) {
            return true;
        }

        return stack.isOf(Items.WOODEN_SWORD)
                || stack.isOf(Items.STONE_SWORD)
                || stack.isOf(Items.IRON_SWORD)
                || stack.isOf(Items.GOLDEN_SWORD)
                || stack.isOf(Items.DIAMOND_SWORD)
                || stack.isOf(Items.NETHERITE_SWORD);
    }

    private void sendAttackPacket(ClientPlayerEntity player, LivingEntity target) {
        if (player.networkHandler == null) {
            return;
        }

        // Send attack packet directly instead of using interactionManager
        // This is less detectable than the high-level API
        player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(target, player.isSneaking()));
    }
}
