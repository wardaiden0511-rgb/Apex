package com.example.apexclient;

public class ApexConfig {
    public static boolean hudOverlay = true;
    public static boolean infoDisplay = false;

    public static double aimAssistSpeed = 35.0;
    public static double aimAssistRange = 4.0;

    /**
     * If true, Aim Assist can target players even when they are behind blocks/walls.
     * If false, Aim Assist only targets players the client player can see.
     */
    public static boolean aimAssistThroughWalls = false;

    /**
     * If true, Aim Assist controls yaw + pitch.
     * If false, Aim Assist only controls horizontal yaw and does not move pitch vertically.
     */
    public static boolean aimAssistVertical = true;

    /**
     * Delay before Shield Stun performs the follow-up attack.
     * Slider range should be 5 ms to 200 ms.
     */
    public static double shieldStunDelayMs = 80.0;

    /**
     * Auto Drain modes.
     * Multiple modes can be enabled at the same time.
     */
    public static boolean autoDrainWeb = true;
    public static boolean autoDrainBlock = true;
    public static boolean autoDrainBucket = true;

    /**
     * How close water must be to an enemy/player before Auto Drain targets it.
     * Slider range should be 2.0 to 6.0 blocks.
     */
    public static double autoDrainRange = 4.0;

    /**
     * Triggerbot random extra delay after the held weapon is fully cooled down.
     * Example: sword/axe cooldown finishes, then Triggerbot waits 0–this value before attacking.
     */
    public static double triggerBotRandomDelayMs = 60.0;

    /**
     * If true, Triggerbot only attacks player entities.
     * If false, it can also attack other living entities.
     */
    public static boolean triggerBotPlayersOnly = true;

    /**
     * If true, Triggerbot only attacks while holding a sword or axe.
     */
    public static boolean triggerBotWeaponOnly = true;

    /**
     * If true, Triggerbot only attacks targets the player can see.
     */
    public static boolean triggerBotRequireVisible = true;

    /**
     * If true, Triggerbot only attacks the exact entity under your crosshair.
     * Keep this true for the safest/cleanest behavior.
     */
    public static boolean triggerBotRequireCrosshair = true;

    /**
     * Delay in ticks before Pressure Web places cobweb after pressure plate is placed.
     * Slider range should be 1 to 5 ticks.
     */
    public static double pressureWebDelayTicks = 2.0;

    /**
     * If true, Pressure Web is enabled.
     */
    public static boolean pressureWebEnabled = true;

    /**
     * Speed of Pressure Web - how fast it swaps to cobweb slot.
     * Slider range should be 1 to 10 ticks.
     */
    public static double pressureWebSwapSpeedTicks = 5.0;

    /**
     * Delay in ticks before Stun Slam swaps to mace after shield break.
     * Slider range should be 1 to 10 ticks.
     */
    public static double stunSlamSwapDelayTicks = 5.0;

    /**
     * Speed of Stun Slam - how fast it attacks with mace after swapping.
     * Slider range should be 1 to 3 ticks.
     */
    public static double stunSlamAttackSpeedTicks = 1.0;

    /**
     * Minimum horizontal distance from enemy for Stun Slam to activate.
     * Slider range should be 1.0 to 3.0 blocks.
     */
    public static double stunSlamHorizontalDistance = 2.0;

    /**
     * Minimum fall distance for Stun Slam to activate.
     * Slider range should be 1.0 to 3.0 blocks.
     */
    public static double stunSlamFallDistance = 1.5;

    public static final double AIM_ASSIST_MIN_SPEED = 1.0;
    public static final double AIM_ASSIST_MAX_SPEED = 100.0;

    public static final double AIM_ASSIST_MIN_RANGE = 1.0;
    public static final double AIM_ASSIST_MAX_RANGE = 4.0;

    public static final double SHIELD_STUN_MIN_DELAY_MS = 5.0;
    public static final double SHIELD_STUN_MAX_DELAY_MS = 200.0;

    public static final double AUTO_DRAIN_MIN_RANGE = 2.0;
    public static final double AUTO_DRAIN_MAX_RANGE = 6.0;

    /**
     * Random delay range for Triggerbot after weapon cooldown is ready.
     * Keep this above 0 if you do not want instant attacks after detection.
     */
    public static final double TRIGGER_BOT_MIN_RANDOM_DELAY_MS = 20.0;
    public static final double TRIGGER_BOT_MAX_RANDOM_DELAY_MS = 150.0;

    public static final double PRESSURE_WEB_MIN_DELAY_TICKS = 1.0;
    public static final double PRESSURE_WEB_MAX_DELAY_TICKS = 5.0;

    public static final double PRESSURE_WEB_MIN_SWAP_SPEED_TICKS = 1.0;
    public static final double PRESSURE_WEB_MAX_SWAP_SPEED_TICKS = 10.0;

    public static final double STUN_SLAM_MIN_SWAP_DELAY_TICKS = 1.0;
    public static final double STUN_SLAM_MAX_SWAP_DELAY_TICKS = 10.0;

    public static final double STUN_SLAM_MIN_ATTACK_SPEED_TICKS = 1.0;
    public static final double STUN_SLAM_MAX_ATTACK_SPEED_TICKS = 3.0;

    public static final double STUN_SLAM_MIN_HORIZONTAL_DISTANCE = 1.0;
    public static final double STUN_SLAM_MAX_HORIZONTAL_DISTANCE = 3.0;

    public static final double STUN_SLAM_MIN_FALL_DISTANCE = 1.0;
    public static final double STUN_SLAM_MAX_FALL_DISTANCE = 3.0;

    private ApexConfig() {
    }

    public static double clampAimAssistSpeed(double value) {
        return clamp(value, AIM_ASSIST_MIN_SPEED, AIM_ASSIST_MAX_SPEED);
    }

    public static double clampAimAssistRange(double value) {
        return clamp(value, AIM_ASSIST_MIN_RANGE, AIM_ASSIST_MAX_RANGE);
    }

    public static double clampShieldStunDelayMs(double value) {
        return clamp(value, SHIELD_STUN_MIN_DELAY_MS, SHIELD_STUN_MAX_DELAY_MS);
    }

    public static double clampAutoDrainRange(double value) {
        return clamp(value, AUTO_DRAIN_MIN_RANGE, AUTO_DRAIN_MAX_RANGE);
    }

    public static double clampTriggerBotRandomDelayMs(double value) {
        return clamp(value, TRIGGER_BOT_MIN_RANDOM_DELAY_MS, TRIGGER_BOT_MAX_RANDOM_DELAY_MS);
    }

    public static double clampPressureWebDelayTicks(double value) {
        return clamp(value, PRESSURE_WEB_MIN_DELAY_TICKS, PRESSURE_WEB_MAX_DELAY_TICKS);
    }

    public static double clampPressureWebSwapSpeedTicks(double value) {
        return clamp(value, PRESSURE_WEB_MIN_SWAP_SPEED_TICKS, PRESSURE_WEB_MAX_SWAP_SPEED_TICKS);
    }

    public static double clampStunSlamSwapDelayTicks(double value) {
        return clamp(value, STUN_SLAM_MIN_SWAP_DELAY_TICKS, STUN_SLAM_MAX_SWAP_DELAY_TICKS);
    }

    public static double clampStunSlamAttackSpeedTicks(double value) {
        return clamp(value, STUN_SLAM_MIN_ATTACK_SPEED_TICKS, STUN_SLAM_MAX_ATTACK_SPEED_TICKS);
    }

    public static double clampStunSlamHorizontalDistance(double value) {
        return clamp(value, STUN_SLAM_MIN_HORIZONTAL_DISTANCE, STUN_SLAM_MAX_HORIZONTAL_DISTANCE);
    }

    public static double clampStunSlamFallDistance(double value) {
        return clamp(value, STUN_SLAM_MIN_FALL_DISTANCE, STUN_SLAM_MAX_FALL_DISTANCE);
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}