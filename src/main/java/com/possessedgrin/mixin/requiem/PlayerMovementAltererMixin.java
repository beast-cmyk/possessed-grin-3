package com.possessedgrin.mixin.requiem;

import com.possessedgrin.PossessedGrin;
import ladysnake.requiem.api.v1.entity.MovementConfig;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Levitation and the spider-walk: every possessed mob can fly (double-tap jump, like creative mode) and crawl up
 * walls. Mobs that already fly or climb keep their own way of doing it.
 */
@Pseudo
@Mixin(targets = "ladysnake.requiem.core.movement.PlayerMovementAlterer", remap = false)
public abstract class PlayerMovementAltererMixin {
    @Shadow
    @Final
    protected PlayerEntity player;

    @Inject(method = "getActualFlightMode", at = @At("RETURN"), cancellable = true, require = 0)
    private static void possessedgrin$levitate(MovementConfig config, Entity entity,
                                              CallbackInfoReturnable<MovementConfig.MovementMode> cir) {
        if (cir.getReturnValue() == MovementConfig.MovementMode.DISABLED
                && (PossessedGrin.isPossessed(entity) || PossessionComponent.getHost(entity) != null)) {
            cir.setReturnValue(MovementConfig.MovementMode.ENABLED);
        }
    }

    @Inject(method = "canClimbWalls", at = @At("RETURN"), cancellable = true, require = 0)
    private void possessedgrin$spiderWalk(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && PossessionComponent.getHost(this.player) != null) {
            cir.setReturnValue(true);
        }
    }
}
