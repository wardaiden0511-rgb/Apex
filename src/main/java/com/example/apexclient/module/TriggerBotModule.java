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

    private LivingEntity lastDetectedTarget = null;

    public TriggerBotModule() {
        super("Triggerbot");
    }

    @Override
    protected void onEnable() {
        lastDetectedTarget = null;
    }

    @Override
    protected void onDisable() {
        lastDetectedTarget = null;
    }

    @Override
    public void tick(MinecraftClient client) {
        if (!isEnabled()) {
            return;
        }

        if (client == null || client.player == null || client.world == null || client.interactionManager == null) {
            lastDetectedTarget = null;
            return;
        }

        ClientPlayerEntity player = client.player;

        if (ApexConfig.triggerBotWeaponOnly && !isHoldingWeapon(player)) {
            lastDetectedTarget = null;
            return;
        }

        // Respect vanilla cooldown first.
        // Sword/axe cooldown differences are handled by Minecraft here.
        if (!isAttackCooledDown(player)) {
            return;
        }

        LivingEntity target = getTarget(client, player);
        if (target == null || !isValidTarget(player, target)) {
            lastDetectedTarget = null;
            return;
        }

        // Only attack if target changed (prevents spamming same target)
        if (lastDetectedTarget == target) {
            return;
        }

        lastDetectedTarget = target;

        // Send attack packet directly instead of using interactionManager
        sendAttackPacket(player, target);
        player.swingHand(Hand.MAIN_HAND);
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