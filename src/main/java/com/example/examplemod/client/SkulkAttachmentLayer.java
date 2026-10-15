package com.example.examplemod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Blend localized sculk growth and an embedded parasite into the native host model. */
public class SkulkAttachmentLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("examplemod", "textures/entity/skulk_parasite.png");
    private final SkulkAttachmentModel attachment;
    private final Identifier adultGrowthTexture;
    private final Identifier babyGrowthTexture;

    public SkulkAttachmentLayer(RenderLayerParent<S, M> parent, EntityRendererProvider.Context context, boolean cow) {
        super(parent);
        String species = cow ? "cow" : "zombie";
        adultGrowthTexture = Identifier.fromNamespaceAndPath("examplemod",
                "textures/entity/skulk_" + species + "_growth.png");
        babyGrowthTexture = Identifier.fromNamespaceAndPath("examplemod",
                "textures/entity/skulk_" + species + "_baby_growth.png");
        attachment = new SkulkAttachmentModel(context.bakeLayer(cow
                ? SkulkAttachmentModel.COW_LAYER : SkulkAttachmentModel.ZOMBIE_LAYER), cow);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
            S state, float headYaw, float headPitch) {
        if (state.isInvisible) return;
        M host = getParentModel();
        host.setupAnim(state);
        Identifier growth = state.isBaby ? babyGrowthTexture : adultGrowthTexture;
        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0);
        var layers = collector.order(1);
        // Native host models may use an opaque pass. Growth needs cutout alpha and a
        // slight depth offset so it stays visible on the same surface as the base skin.
        layers.submitModel(host, state, poseStack, RenderTypes.entityCutoutZOffset(growth),
                light, overlay, -1, null, state.outlineColor);
        var pose = new SkulkAttachmentState();
        pose.hostRoot = host.root().storePose();
        pose.hostBody = host.root().getChild("body").storePose();
        pose.hostHead = host.root().getChild("head").storePose();
        pose.ageInTicks = state.ageInTicks;
        pose.isBaby = state.isBaby;
        // In the full submission signature, -1 is the white tint; the final argument
        // is the outline color and must come from the entity's actual glowing state.
        layers.submitModel(attachment, pose, poseStack, attachment.renderType(TEXTURE),
                light, overlay, -1, null, state.outlineColor);
    }
}

