package com.possessedgrin.power;

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/**
 * The room goes cold around the possessed: frost drifts off the body, and players nearby see their breath while
 * frost creeps across their screen. It chills them (a little slower) but never freezes them to death.
 */
public final class ColdPresence {
    private static final double RADIUS = 8.0;
    private static final int CHILL_PER_PULSE = 25;

    private ColdPresence() {
    }

    public static void tick(ServerWorld world) {
        if (world.getTime() % 10 != 0) {
            return;
        }
        for (ServerPlayerEntity possessor : world.getPlayers()) {
            MobEntity host = PossessionComponent.getHost(possessor);
            if (host == null || host.isInvisible()) {
                continue;
            }
            world.spawnParticles(ParticleTypes.SNOWFLAKE, host.getX(), host.getBodyY(0.6), host.getZ(),
                    3, host.getWidth() * 0.6, host.getHeight() * 0.4, host.getWidth() * 0.6, 0.01);

            for (ServerPlayerEntity other : world.getPlayers()) {
                if (other == possessor || other.isSpectator() || other.isCreative()
                        || other.squaredDistanceTo(host) > RADIUS * RADIUS) {
                    continue;
                }
                // Stop one tick short of the point where freezing starts to hurt
                int maxChill = other.getMinFreezeDamageTicks() - 1;
                other.setFrozenTicks(Math.min(maxChill, other.getFrozenTicks() + CHILL_PER_PULSE));
                Vec3d breath = other.getEyePos().add(other.getRotationVec(1.0F).multiply(0.4)).add(0.0, -0.15, 0.0);
                world.spawnParticles(ParticleTypes.CLOUD, breath.x, breath.y, breath.z, 1, 0.02, 0.02, 0.02, 0.005);
            }
        }
    }
}
