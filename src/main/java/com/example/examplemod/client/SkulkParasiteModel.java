package com.example.examplemod.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class SkulkParasiteModel extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("examplemod", "skulk_parasite"), "main");
    private final ModelPart parasite;

    public SkulkParasiteModel(ModelPart root) {
        super(root);
        parasite = root.getChild("parasite");
    }

    public static LayerDefinition createBodyLayer() {
        var mesh = new MeshDefinition();
        SkulkParasiteGeometry.addParasite(mesh.getRoot(), "parasite", PartPose.offset(0, 20.6F, 1));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        SkulkParasiteGeometry.animate(parasite,
                state.ageInTicks * 0.12F + state.walkAnimationPos * 0.8F,
                0.08F + state.walkAnimationSpeed * 0.45F);
    }
}

