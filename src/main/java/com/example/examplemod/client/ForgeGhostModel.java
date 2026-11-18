package com.example.examplemod.client;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
/** A translucent material cell; expanded slightly so occupied faces remain visible without z-fighting. */
final class ForgeGhostModel extends EntityModel<LivingEntityRenderState> {
    ForgeGhostModel(){this(.08F);}
    ForgeGhostModel(float inflation){super(createRoot(inflation));}
    private static net.minecraft.client.model.geom.ModelPart createRoot(float inflation){var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("cell",CubeListBuilder.create().texOffs(0,0).addBox(0,0,0,16,16,16,new CubeDeformation(inflation)),PartPose.ZERO);return LayerDefinition.create(mesh,64,32).bakeRoot();}
}
