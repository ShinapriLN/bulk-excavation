package com.shinapri.bulkexcavation.mixin.client;

import com.shinapri.bulkexcavation.ClientSel;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixin {

    @Inject(
            method =
                    "render(Lnet/minecraft/client/util/math/MatrixStack;" +
                            "Lnet/minecraft/client/render/Frustum;" +
                            "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;" +
                            "DDDZ)V",
            at = @At("TAIL"),
            require = 0
    )
    private void bulkExcavation$renderPreview(
            MatrixStack matrices,
            Frustum frustum,
            VertexConsumerProvider.Immediate consumers,
            double cameraX,
            double cameraY,
            double cameraZ,
            boolean lateDebug,
            CallbackInfo ci
    ) {
        // DebugRenderer.render ถูกเรียกมากกว่าหนึ่ง phase
        if (!lateDebug) return;

        BlockPos p1 = ClientSel.pos1;
        BlockPos p2 = ClientSel.pos2;

        if (p1 == null || p2 == null) return;

        double minX = Math.min(p1.getX(), p2.getX());
        double minY = Math.min(p1.getY(), p2.getY());
        double minZ = Math.min(p1.getZ(), p2.getZ());

        double maxX = Math.max(p1.getX(), p2.getX()) + 1;
        double maxY = Math.max(p1.getY(), p2.getY()) + 1;
        double maxZ = Math.max(p1.getZ(), p2.getZ()) + 1;

        matrices.push();

        matrices.translate(
                -cameraX,
                -cameraY,
                -cameraZ
        );

        //? if <=1.21.10 {
        DebugRenderer.drawBox(
                matrices,
                consumers,
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ,
                0.2f,
                0.8f,
                1.0f,
                0.25f
        );
        //?}

        matrices.pop();
    }
}