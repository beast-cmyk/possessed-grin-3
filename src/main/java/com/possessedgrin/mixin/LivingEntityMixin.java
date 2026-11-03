package com.possessedgrin.mixin;

import com.possessedgrin.PossessedGrin;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The demon's voice: every sound a possessed mob makes (growls, hurt cries...) comes out deep and wrong. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    private static final float DEMON_PITCH = 0.6F;

    @Inject(method = "getSoundPitch", at = @At("RETURN"), cancellable = true)
    private void possessedgrin$demonVoice(CallbackInfoReturnable<Float> cir) {
        if (PossessedGrin.isPossessed((LivingEntity) (Object) this)) {
            cir.setReturnValue(cir.getReturnValueF() * DEMON_PITCH);
        }
    }
}
