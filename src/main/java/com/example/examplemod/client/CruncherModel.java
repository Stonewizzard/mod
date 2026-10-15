package com.example.examplemod.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Six-legged sniffer silhouette with large articulated jaws in place of a nose. */
public class CruncherModel extends EntityModel<CruncherRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("examplemod", "cruncher"), "main");
    private final ModelPart fullRoot;
    private final ModelPart head;
    private final ModelPart upperJaw;
    private final ModelPart lowerJaw;
    private final ModelPart[] legs = new ModelPart[6];

    public CruncherModel(ModelPart root) {
        super(root);
        fullRoot = root;
        ModelPart bone = root.getChild("root").getChild("bone");
        head = bone.getChild("body").getChild("head");
        upperJaw = head.getChild("upper_jaw");
        lowerJaw = head.getChild("lower_jaw");
        for (int i = 0; i < 6; i++) {
            legs[i] = bone.getChild("leg_" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, 5, 0));
        var bone = root.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.ZERO);
        var body = bone.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(62, 68).addBox(-12.5F, -14, -20, 25, 29, 40)
                .texOffs(62, 0).addBox(-12.5F, -14, -20, 25, 24, 40, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        for (int i = 0; i < 6; i++) {
            boolean left = i >= 3;
            int row = i % 3;
            bone.addOrReplaceChild("leg_" + i, CubeListBuilder.create().texOffs(left ? 0 : 32, 87 + row * 18)
                    .addBox(-3.5F, -1, -4, 7, 10, 8),
                    PartPose.offset(left ? 7.5F : -7.5F, 10, -15 + row * 15));
        }
        var head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(8, 15)
                .addBox(-6.5F, -7.5F, -9.5F, 13, 14, 11), PartPose.offset(0, 6.5F, -19.48F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(2, 0)
                .addBox(0, 0, -3, 1, 12, 7), PartPose.offset(6.51F, -7.5F, -3));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(48, 0)
                .addBox(-1, 0, -3, 1, 12, 7), PartPose.offset(-6.51F, -7.5F, -3));
        head.addOrReplaceChild("left_eye", CubeListBuilder.create().texOffs(176, 160)
                .addBox(0, 0, 0, 0.5F, 1, 3), PartPose.offset(6.51F, -4, -7.5F));
        head.addOrReplaceChild("right_eye", CubeListBuilder.create().texOffs(176, 160)
                .addBox(-0.5F, 0, 0, 0.5F, 1, 3), PartPose.offset(-6.51F, -4, -7.5F));
        // Dedicated UV islands keep the armored jaws separate from their teeth.
        var upper = head.addOrReplaceChild("upper_jaw", CubeListBuilder.create().texOffs(0, 160)
                .addBox(-8, 0, -13, 16, 4, 13), PartPose.offset(0, -3, -8));
        var lower = head.addOrReplaceChild("lower_jaw", CubeListBuilder.create().texOffs(64, 160)
                .addBox(-8, 0, -13, 16, 4, 13), PartPose.offset(0, 4, -8));
        head.addOrReplaceChild("jaw_hinges", CubeListBuilder.create().texOffs(192, 160)
                .addBox(-8.5F, -1, -9, 2, 5, 6)
                .texOffs(192, 160).addBox(6.5F, -1, -9, 2, 5, 6), PartPose.ZERO);
        var topTeeth = CubeListBuilder.create()
                .texOffs(128, 160).addBox(-7, 4, -13, 3, 4, 3)
                .texOffs(128, 160).addBox(4, 4, -13, 3, 4, 3)
                .texOffs(144, 160).addBox(-3, 4, -13, 2, 2, 3)
                .texOffs(144, 160).addBox(1, 4, -13, 2, 2, 3);
        var bottomTeeth = CubeListBuilder.create()
                .texOffs(144, 160).addBox(-5, -2, -13, 2, 2, 3)
                .texOffs(144, 160).addBox(-1, -2, -13, 2, 2, 3)
                .texOffs(144, 160).addBox(3, -2, -13, 2, 2, 3);
        for (int side : new int[] {-1, 1}) {
            int x = side < 0 ? -7 : 5;
            topTeeth.texOffs(144, 160).addBox(x, 4, -7, 2, 2, 3);
            bottomTeeth.texOffs(144, 160).addBox(x, -2, -4, 2, 2, 3);
        }
        upper.addOrReplaceChild("teeth", topTeeth, PartPose.ZERO);
        lower.addOrReplaceChild("teeth", bottomTeeth, PartPose.ZERO);
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(CruncherRenderState state) {
        super.setupAnim(state);
        head.yRot = state.yRot * Mth.DEG_TO_RAD;
        head.xRot = state.xRot * Mth.DEG_TO_RAD;
        for (int i = 0; i < 6; i++) {
            legs[i].xRot = Mth.cos(state.walkAnimationPos * 0.6662F + (i % 2 == 0 ? 0 : Mth.PI))
                    * state.walkAnimationSpeed * 1.1F;
        }
        float bite = state.crunching ? (0.5F + 0.5F * Mth.sin(state.ageInTicks * 1.4F)) * 0.45F : 0.08F;
        lowerJaw.xRot = bite;
        upperJaw.xRot = -bite * 0.25F;
        if (state.isBaby) {
            fullRoot.xScale = fullRoot.yScale = fullRoot.zScale = 0.5F;
            fullRoot.y = 12;
        }
    }
}
