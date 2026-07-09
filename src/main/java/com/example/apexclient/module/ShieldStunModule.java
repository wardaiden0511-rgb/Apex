package com.example.apexclient.module;

import com.example.apexclient.ApexConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class ShieldStunModule extends Module {
    private PlayerEntity queuedTarget = null;
    private long queuedAttackTimeMs = 0L;
    private boolean wasAttackPressed = false;

    public ShieldStunModule() {
        super("Shield Stun");
    }

    @Override
    protected void onEnable() {
        queuedTarget = null;
        queuedAttackTimeMs = 0L;
        wasAttackPressed = false;
    }

    @Override
    protected void onDisable() {
        queuedTarget = null;
        queuedAttackTimeMs = 0L;
        wasAttackPressed = false;
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
        boolean attackPressed = client.options.attackKey.isPressed();

        // Detect the first frame of the attack click.
        if (attackPressed && !wasAttackPressed) {
            tryQueueShieldStun(client, player);
        }

        wasAttackPressed = attackPressed;

        runQueuedAttack(client, player);
    }

    private void tryQueueShieldStun(MinecraftClient client, ClientPlayerEntity player) {
        if (!isHoldingAxe(player)) {
            return;
        }

        PlayerEntity target = getCrosshairPlayerTarget(client);
        if (target == null) {
            return;
        }

        // Important:
        // Only queue the follow-up hit if the target is actively blocking with a shield
        // at the moment you axe-hit them.
        if (!isBlockingWithShield(target)) {
            return;
        }

        double delayMs = ApexConfig.clampShieldStunDelayMs(ApexConfig.shieldStunDelayMs);

        queuedTarget = target;
        queuedAttackTimeMs = System.currentTimeMillis() + Math.round(delayMs);
    }

    private void runQueuedAttack(MinecraftClient client, ClientPlayerEntity player) {
        if (queuedTarget == null) {
            return;
        }

        if (System.currentTimeMillis() < queuedAttackTimeMs) {
            return;
        }

        PlayerEntity target = queuedTarget;
        queuedTarget = null;
        queuedAttackTimeMs = 0L;

        if (!target.isAlive() || target.isSpectator()) {
            return;
        }

        // Safety distance check. 36.0 = 6 blocks squared.
        if (player.squaredDistanceTo(target) > 36.0) {
            return;
        }

        if (!isHoldingAxe(player)) {
            return;
        }

        client.interactionManager.attackEntity(player, target);
        player.swingHand(Hand.MAIN_HAND);
    }

    private boolean isHoldingAxe(ClientPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        return !stack.isEmpty() && stack.getItem() instanceof AxeItem;
    }

    private boolean isBlockingWithShield(PlayerEntity target) {
        if (!target.isBlocking()) {
            return false;
        }

        ItemStack activeStack = target.getActiveItem();
        return !activeStack.isEmpty() && activeStack.isOf(Items.SHIELD);
    }

    private PlayerEntity getCrosshairPlayerTarget(MinecraftClient client) {
        HitResult hitResult = client.crosshairTarget;

        if (!(hitResult instanceof EntityHitResult entityHitResult)) {
            return null;
        }

        Entity entity = entityHitResult.getEntity();

        if (!(entity instanceof PlayerEntity target)) {
            return null;
        }

        if (target == client.player) {
            return null;
        }

        if (!target.isAlive() || target.isSpectator()) {
            return null;
        }

        return target;
    }
}