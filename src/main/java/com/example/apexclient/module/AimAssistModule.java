package com.example.apexclient.module;

import com.example.apexclient.ApexConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class AimAssistModule extends Module {
    private static final double MAX_RANGE = 4.0;

    public AimAssistModule() {
        super("Aim Assist");
    }

    @Override
    protected void onEnable() {
        // No setup needed.
    }

    @Override
    protected void onDisable() {
        // No cleanup needed.
    }

    @Override
    public void tick(MinecraftClient client) {
        // Extra safety: even if something accidentally calls tick directly,
        // Aim Assist does nothing while disabled.
        if (!isEnabled()) {
            return;
        }

        if (client == null || client.world == null || client.player == null) {
            return;
        }

        ClientPlayerEntity player = client.player;
        PlayerEntity target = findNearestTarget(client, player);

        if (target == null) {
            return;
        }

        rotateToward(player, target);
    }

    private PlayerEntity findNearestTarget(MinecraftClient client, ClientPlayerEntity player) {
        double effectiveRange = getEffectiveRange();
        double effectiveRangeSq = effectiveRange * effectiveRange;

        PlayerEntity nearest = null;
        double nearestDistanceSq = effectiveRangeSq;

        for (PlayerEntity otherPlayer : client.world.getPlayers()) {
            if (otherPlayer == null || otherPlayer == player) {
                continue;
            }

            if (!otherPlayer.isAlive() || otherPlayer.isSpectator()) {
                continue;
            }

            // If through-walls is OFF, only target players the client player can see.
            // If through-walls is ON, skip this visibility check.
            if (!ApexConfig.aimAssistThroughWalls && !player.canSee(otherPlayer)) {
                continue;
            }

            double distanceSq = player.squaredDistanceTo(otherPlayer);
            if (distanceSq <= nearestDistanceSq) {
                nearest = otherPlayer;
                nearestDistanceSq = distanceSq;
            }
        }

        return nearest;
    }

    private void rotateToward(ClientPlayerEntity player, PlayerEntity target) {
        double dx = target.getX() - player.getX();
        double dy = target.getEyeY() - player.getEyeY();
        double dz = target.getZ() - player.getZ();

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, horizontalDistance));

        float currentYaw = player.getYaw();
        float currentPitch = player.getPitch();

        float strength = getAimStrength();

        float newYaw = MathHelper.lerpAngleDegrees(strength, currentYaw, targetYaw);
        float newPitch = currentPitch;

        // Vertical Assist ON = aim up/down too.
        // Vertical Assist OFF = horizontal-only aim assist.
        if (ApexConfig.aimAssistVertical) {
            newPitch = MathHelper.lerp(strength, currentPitch, targetPitch);
            newPitch = MathHelper.clamp(newPitch, -90.0F, 90.0F);
        }

        player.setYaw(newYaw);
        player.setHeadYaw(newYaw);

        if (ApexConfig.aimAssistVertical) {
            player.setPitch(newPitch);
        }
    }

    private double getEffectiveRange() {
        return MathHelper.clamp(
                ApexConfig.aimAssistRange,
                ApexConfig.AIM_ASSIST_MIN_RANGE,
                MAX_RANGE
        );
    }

    private float getAimStrength() {
        double clampedSpeed = MathHelper.clamp(
                ApexConfig.aimAssistSpeed,
                ApexConfig.AIM_ASSIST_MIN_SPEED,
                ApexConfig.AIM_ASSIST_MAX_SPEED
        );

        return (float) (clampedSpeed / 100.0);
    }
}