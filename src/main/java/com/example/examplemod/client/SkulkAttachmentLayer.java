package com.example.examplemod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** Blend localized sculk growth and an embedded parasite into the native host model. */
public class SkulkAttachmentLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("examplemod", "textures/entity/skulk_parasite.png");
    private final SkulkAttachmentModel attachment;
    private final boolean cow;

    public SkulkAttachmentLayer(RenderLayerParent<S, M> parent, EntityRendererProvider.Context context, boolean cow) {
        super(parent);
        this.cow = cow;
        attachment = new SkulkAttachmentModel(context.bakeLayer(cow
                ? SkulkAttachmentModel.COW_LAYER : SkulkAttachmentModel.ZOMBIE_LAYER), cow);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
            S state, float headYaw, float headPitch) {
        if (state.isInvisible) return;
        M host = getParentModel();
        host.setupAnim(state);
        String species = cow ? "cow" : "zombie";
        Identifier growth = Identifier.fromNamespaceAndPath("examplemod", "textures/entity/skulk_"
                + species + (state.isBaby ? "_baby" : "") + "_growth.png");
        collector.submitModel(host, state, poseStack, host.renderType(growth),
                light, LivingEntityRenderer.getOverlayCoords(state, 0), -1);
        var pose = new SkulkAttachmentState();
        pose.hostRoot = host.root().storePose();
        pose.hostBody = host.root().getChild("body").storePose();
        pose.hostHead = host.root().getChild("head").storePose();
        pose.ageInTicks = state.ageInTicks;
        pose.isBaby = state.isBaby;
        collector.submitModel(attachment, pose, poseStack, attachment.renderType(TEXTURE),
                light, LivingEntityRenderer.getOverlayCoords(state, 0), -1);
    }
}

