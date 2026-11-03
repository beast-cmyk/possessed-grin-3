package com.possessedgrin.power;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;

import java.util.HashSet;
import java.util.Set;

/**
 * The head turn: the possessed mob's head twists all the way around to face behind it, with a sickening crack,
 * holds there, then twists back. The animation itself plays on every nearby player's screen (see the client side).
 */
public final class HeadTurn {
    private HeadTurn() {
    }

    static boolean use(ServerPlayerEntity player, MobEntity host) {
        Set<ServerPlayerEntity> viewers = new HashSet<>(PlayerLookup.tracking(host));
        viewers.add(player);
        for (ServerPlayerEntity viewer : viewers) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeVarInt(host.getId());
            ServerPlayNetworking.send(viewer, PossessedPowers.HEAD_TURN, buf);
        }
        PossessedPowers.sound(host, SoundEvents.BLOCK_BONE_BLOCK_BREAK, 1.5F, 0.5F);
        PossessedPowers.sound(host, SoundEvents.ENTITY_SKELETON_HURT, 0.8F, 0.4F);
        return true;
    }
}
