package com.example.examplemod.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class SkulkParasiteModel extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("examplemod", "skulk_parasite"), "main");
    private final ModelPart shell;
    private final ModelPart[] legs = new ModelPart[6];

    public SkulkParasiteModel(ModelPart root) {
        super(root);
        shell = root.getChild("shell");
        for (int i = 0; i < legs.length; i++) legs[i] = root.getChild("leg_" + i);
    }

    public static LayerDefinition createBodyLayer() {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4, -3, -5, 8, 4, 10), PartPose.offset(0, 21, 1));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-3, -2, -4, 6, 3, 4), PartPose.offset(0, 22, -4));
        for (int i = 0; i < 6; i++) {
            int side = i < 3 ? -1 : 1;
            float angle = (i % 3 - 1) * 0.5F;
            root.addOrReplaceChild("leg_" + i, CubeListBuilder.create().texOffs(0, 32)
                    .addBox(side < 0 ? -5 : 0, 0, -0.5F, 5, 1, 1),
                    PartPose.offsetAndRotation(side * 3.0F, 22, -2 + (i % 3) * 3, 0, side * angle, side * 0.25F));
        }
        for (int side : new int[]{-1, 1}) {
            root.addOrReplaceChild("pincer_" + side, CubeListBuilder.create().texOffs(16, 40)
                    .addBox(-0.5F, -0.5F, -4, 1, 1, 4),
                    PartPose.offsetAndRotation(side * 2, 22, -7, 0, side * -0.35F, 0));
            root.addOrReplaceChild("tendril_" + side, CubeListBuilder.create().texOffs(0, 40)
                    .addBox(-0.5F, -4, -0.5F, 1, 4, 1),
                    PartPose.offsetAndRotation(side * 2, 19, -1, -0.35F, 0, side * 0.45F));
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        shell.xRot = Mth.sin(state.ageInTicks * 0.1F) * 0.03F;
        for (int i = 0; i < legs.length; i++) {
            int side = i < 3 ? -1 : 1;
            legs[i].yRot += Mth.cos(state.walkAnimationPos * 1.4F + i * 1.2F)
                    * state.walkAnimationSpeed * 0.8F * side;
        }
    }
}
