package com.example.examplemod.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class SkulkAttachmentModel extends EntityModel<SkulkAttachmentState> {
    public static final ModelLayerLocation ZOMBIE_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("examplemod", "skulk_zombie_attachment"), "main");
    public static final ModelLayerLocation COW_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("examplemod", "skulk_cow_attachment"), "main");
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart bodyGrowth;
    private final ModelPart headGrowth;
    private final ModelPart parasite;
    private final boolean cow;

    public SkulkAttachmentModel(ModelPart root, boolean cow) {
        super(root);
        this.cow = cow;
        body = root.getChild("body");
        head = root.getChild("head");
        bodyGrowth = body.getChild("growth");
        headGrowth = head.getChild("growth");
        parasite = bodyGrowth.getChild("parasite");
    }

    public static LayerDefinition createLayer(boolean cow) {
        var mesh = new MeshDefinition();
        var body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        var head = mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        var growth = body.addOrReplaceChild("growth", CubeListBuilder.create(), PartPose.ZERO);
        var headGrowth = head.addOrReplaceChild("growth", CubeListBuilder.create(), PartPose.ZERO);
        SkulkParasiteGeometry.addParasite(growth, "parasite", cow
                ? PartPose.offsetAndRotation(3, -5, 3.7F, -Mth.HALF_PI, 0, 0)
                : PartPose.offsetAndRotation(-2, 3, 2.6F, -Mth.HALF_PI, 0, 0));
        if (cow) {
            growth.addOrReplaceChild("embedded_spine", CubeListBuilder.create()
                    .texOffs(4, 16).addBox(-1, -7, 2.8F, 4, 3, 0.65F)
                    .texOffs(12, 16).addBox(-3.5F, -4, 2.8F, 3, 3, 0.5F)
                    .texOffs(20, 16).addBox(-4.5F, -1, 2.85F, 2.5F, 2, 0.4F)
                    .texOffs(0, 18).addBox(5.85F, -6, -0.5F, 0.5F, 3, 2.5F), PartPose.ZERO);
            headGrowth.addOrReplaceChild("temple_bud", CubeListBuilder.create()
                    .texOffs(0, 16).addBox(3.85F, -4, -2.5F, 0.5F, 2, 2), PartPose.ZERO);
            SkulkParasiteGeometry.addRoot(growth, "right_flank", new float[][]{
                    {3, -5, 3.5F}, {6.3F, -4.5F, 1.5F}, {6.5F, -3.5F, -1}, {6.3F, -2, -4}});
            SkulkParasiteGeometry.addRoot(growth, "over_spine", new float[][]{
                    {3, -5, 3.4F}, {0, -3, 3.4F}, {-4, -2.5F, 3.4F}, {-6.3F, -2, 1}, {-6.4F, -1, -2}});
            SkulkParasiteGeometry.addRoot(headGrowth, "ear_roots", new float[][]{
                    {4.2F, -1, 0}, {4.3F, -2.5F, -1}, {4.2F, -3.5F, -2.5F}});
        } else {
            growth.addOrReplaceChild("embedded_shoulder", CubeListBuilder.create()
                    .texOffs(4, 16).addBox(-4.15F, 0.2F, 1.8F, 3.5F, 3, 0.65F)
                    .texOffs(12, 16).addBox(-4.2F, 1, -1, 0.6F, 3.5F, 2.5F)
                    .texOffs(20, 16).addBox(-3.8F, 3, -2.35F, 2.4F, 1.5F, 0.55F)
                    .texOffs(0, 18).addBox(-2.2F, 5, -2.25F, 1.3F, 1.2F, 0.4F), PartPose.ZERO);
            headGrowth.addOrReplaceChild("temple_bud", CubeListBuilder.create()
                    .texOffs(0, 16).addBox(-4.2F, -6, -2.8F, 0.55F, 2.3F, 2.5F), PartPose.ZERO);
            SkulkParasiteGeometry.addRoot(growth, "chest_roots", new float[][]{
                    {-3.5F, 1.5F, 2.6F}, {-4.4F, 2.2F, 0}, {-4.3F, 3, -2.4F},
                    {-2, 4, -2.4F}, {-0.5F, 5.5F, -2.4F}, {1, 6.5F, -2.4F}});
            SkulkParasiteGeometry.addRoot(growth, "waist_roots", new float[][]{
                    {-3, 5, 2.4F}, {-4.4F, 6, 0}, {-4.2F, 7.5F, -2.4F}, {-2, 9, -2.4F}});
            SkulkParasiteGeometry.addRoot(headGrowth, "temple_roots", new float[][]{
                    {-4.2F, -1, 1.5F}, {-4.3F, -3.5F, -0.5F}, {-4.2F, -5, -2.5F}});
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(SkulkAttachmentState state) {
        super.setupAnim(state);
        root().loadPose(state.hostRoot);
        body.loadPose(state.hostBody);
        head.loadPose(state.hostHead);
        float bodyScale = state.isBaby ? 0.5F : 1.0F;
        bodyGrowth.xScale = bodyGrowth.yScale = bodyGrowth.zScale = bodyScale;
        headGrowth.xScale = headGrowth.yScale = headGrowth.zScale = state.isBaby ? 0.75F : 1.0F;
        parasite.xScale = parasite.yScale = parasite.zScale = cow ? 0.7F : 0.65F;
        SkulkParasiteGeometry.animate(parasite, state.ageInTicks * 0.09F, 0.06F);
    }
}

