package com.possessedgrin.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Telepathy. Your mind opens (your client also makes every creature nearby glow through walls for 10 seconds),
 * and you read whoever you're looking at, or the nearest player: their health, hunger, what they're holding,
 * where they stand and where they last died. A player you read hears a voice in their head that knows things
 * it shouldn't.
 */
public final class Telepathy {
    private static final double RANGE = 64.0;

    private Telepathy() {
    }

    static boolean use(ServerPlayerEntity player, MobEntity host) {
        ServerWorld world = (ServerWorld) host.world;
        world.playSound(null, host.getX(), host.getY(), host.getZ(), SoundEvents.ENTITY_ELDER_GUARDIAN_AMBIENT, SoundCategory.HOSTILE, 0.6F, 0.5F);

        LivingEntity target = lookedAt(player, host);
        if (target == null) {
            target = nearestPlayer(player, world);
        }
        if (target == null) {
            say(player, Text.literal("You reach out with your mind... no one is near.").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            return true;
        }

        if (target instanceof ServerPlayerEntity victim) {
            readPlayer(player, victim);
            whisperTo(victim, host);
        } else {
            readCreature(player, target);
        }
        return true;
    }

    private static void readPlayer(ServerPlayerEntity reader, ServerPlayerEntity victim) {
        BlockPos pos = victim.getBlockPos();
        say(reader, Text.literal("You slip into " + victim.getName().getString() + "'s mind:").formatted(Formatting.DARK_RED, Formatting.BOLD));
        say(reader, line("Hearts", hearts(victim) + "   Hunger: " + victim.getHungerManager().getFoodLevel() + "/20"));
        say(reader, line("Holding", itemName(victim.getMainHandStack())));
        say(reader, line("Standing at", pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " in the " + dimension(victim.world)));
        Optional<GlobalPos> death = victim.getLastDeathPos();
        say(reader, line("Last died at", death
                .map(d -> d.getPos().getX() + ", " + d.getPos().getY() + ", " + d.getPos().getZ() + " in the " + dimension(d.getDimension().getValue().getPath()))
                .orElse("they have never died... yet")));
    }

    private static void readCreature(ServerPlayerEntity reader, LivingEntity creature) {
        say(reader, Text.literal("You slip into the " + creature.getName().getString() + "'s mind:").formatted(Formatting.DARK_RED, Formatting.BOLD));
        say(reader, line("Hearts", hearts(creature)));
        say(reader, line("Holding", itemName(creature.getMainHandStack())));
        if (creature instanceof MobEntity mob && mob.getTarget() != null) {
            say(reader, line("It is hunting", mob.getTarget().getName().getString()));
        } else {
            say(reader, line("Its thoughts", "blank, animal, afraid of you"));
        }
    }

    /** What the victim hears: one line picked at random, built from things only they should know. */
    private static void whisperTo(ServerPlayerEntity victim, MobEntity host) {
        String name = victim.getName().getString();
        BlockPos pos = victim.getBlockPos();
        List<String> whispers = new ArrayList<>();
        whispers.add("I can see you, " + name + ".");
        whispers.add(hearts(victim) + " hearts... how fragile you are.");
        whispers.add("Standing at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "? Hiding won't help.");
        whispers.add("Don't turn around.");
        whispers.add("Something is wearing a " + host.getType().getName().getString().toLowerCase() + " tonight. Guess what.");
        if (!victim.getMainHandStack().isEmpty()) {
            whispers.add("That " + itemName(victim.getMainHandStack()).toLowerCase() + " won't save you.");
        }
        if (victim.getLastDeathPos().isPresent()) {
            whispers.add("I remember where you died. Do you?");
        }
        String whisper = whispers.get(victim.world.getRandom().nextInt(whispers.size()));
        victim.sendMessage(Text.literal("A voice inside your head: \"" + whisper + "\"").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC), false);
        victim.playSound(SoundEvents.ENTITY_VEX_AMBIENT, SoundCategory.HOSTILE, 1.0F, 0.4F);
    }

    @Nullable
    private static LivingEntity lookedAt(ServerPlayerEntity player, MobEntity host) {
        Vec3d eye = player.getCameraPosVec(1.0F);
        Vec3d look = player.getRotationVec(1.0F);
        Box searchArea = player.getBoundingBox().stretch(look.multiply(RANGE)).expand(1.0);
        EntityHitResult hit = ProjectileUtil.raycast(player, eye, eye.add(look.multiply(RANGE)), searchArea,
                e -> e instanceof LivingEntity && e != host && e.isAlive() && !e.isSpectator(), RANGE * RANGE);
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }

    @Nullable
    private static ServerPlayerEntity nearestPlayer(ServerPlayerEntity player, ServerWorld world) {
        ServerPlayerEntity nearest = null;
        for (ServerPlayerEntity other : world.getPlayers()) {
            if (other != player && !other.isSpectator() && other.squaredDistanceTo(player) < RANGE * RANGE
                    && (nearest == null || other.squaredDistanceTo(player) < nearest.squaredDistanceTo(player))) {
                nearest = other;
            }
        }
        return nearest;
    }

    private static void say(PlayerEntity player, Text text) {
        player.sendMessage(text, false);
    }

    private static Text line(String label, String value) {
        return Text.literal("  " + label + ": ").formatted(Formatting.GRAY)
                .append(Text.literal(value).formatted(Formatting.WHITE));
    }

    private static String hearts(LivingEntity entity) {
        return Math.round(entity.getHealth() / 2.0F) + "/" + Math.round(entity.getMaxHealth() / 2.0F);
    }

    private static String itemName(ItemStack stack) {
        return stack.isEmpty() ? "nothing" : stack.getName().getString();
    }

    private static String dimension(World world) {
        return dimension(world.getRegistryKey().getValue().getPath());
    }

    private static String dimension(String path) {
        return switch (path) {
            case "overworld" -> "Overworld";
            case "the_nether" -> "Nether";
            case "the_end" -> "End";
            default -> path;
        };
    }
}
