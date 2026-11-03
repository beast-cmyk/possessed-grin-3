package com.possessedgrin.mixin.client;

import com.possessedgrin.client.FaceDecals;
import com.possessedgrin.client.HeadTurns;
import com.possessedgrin.client.PossessedFaceFeatureRenderer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Every living-mob renderer gets:
 * <ul>
 *   <li>the possessed-face layer, added before the subclass layers (armor, held items...) so it sits on the skin;</li>
 *   <li>the head-turn animation: after the model has posed itself for this frame, a turning head gets twisted
 *       further around, and is put back once the frame is drawn.</li>
 * </ul>
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> implements FeatureRendererContext<T, M> {
    private static final String RENDER =
            "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V";

    @Shadow
    @Final
    protected List<FeatureRenderer<T, M>> features;

    @Shadow
    protected M model;

    @Unique
    private List<ModelPart> possessedgrin$turnedHeads = List.of();
    @Unique
    private float possessedgrin$turn;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void possessedgrin$addFaceLayer(EntityRendererFactory.Context ctx, M model, float shadowRadius, CallbackInfo ci) {
        this.features.add(new PossessedFaceFeatureRenderer<>(this));
    }

    @Inject(
            method = RENDER,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;setAngles(Lnet/minecraft/entity/Entity;FFFFF)V", shift = At.Shift.AFTER),
            require = 0
    )
    private void possessedgrin$turnHead(T entity, float yaw, float tickDelta, MatrixStack matrices,
                                        VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        float turn = HeadTurns.yawOffset(entity, tickDelta);
        if (turn == 0.0F) {
            return;
        }
        this.possessedgrin$turnedHeads = FaceDecals.headParts(this.model);
        this.possessedgrin$turn = turn;
        for (ModelPart head : this.possessedgrin$turnedHeads) {
            head.yaw += turn;
        }
    }

    @Inject(method = RENDER, at = @At("RETURN"))
    private void possessedgrin$restoreHead(T entity, float yaw, float tickDelta, MatrixStack matrices,
                                           VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        for (ModelPart head : this.possessedgrin$turnedHeads) {
            head.yaw -= this.possessedgrin$turn;
        }
        this.possessedgrin$turnedHeads = List.of();
    }
}
