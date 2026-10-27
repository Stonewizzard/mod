package com.example.examplemod.client;

import com.example.examplemod.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

/** Display the saved output, or first input, on the forge's physical work pad. */
public final class AncientForgeRenderer implements BlockEntityRenderer<ReconstructionTableBlockEntity,AncientForgeRenderer.State> {
    private final ItemModelResolver resolver;
    private final ForgeGhostModel ghostModel=new ForgeGhostModel();
    public record Ghost(int x,int y,int z,boolean occupied){}
    public static final class State extends BlockEntityRenderState {public ItemStackRenderState item=new ItemStackRenderState();public java.util.List<Ghost> ghosts=java.util.List.of();public boolean formed;}
    public AncientForgeRenderer(BlockEntityRendererProvider.Context context){resolver=context.itemModelResolver();}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(ReconstructionTableBlockEntity forge,State state,float partialTick,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking){
        BlockEntityRenderer.super.extractRenderState(forge,state,partialTick,camera,breaking);state.item=new ItemStackRenderState();state.ghosts=java.util.List.of();
        if(!forge.getBlockState().is(AncientForgeContent.CORE.get()))return;
        state.formed=ForgeAssembly.formed(forge.getBlockState());
        if(ForgePreview.active(forge.getLevel(),forge.getBlockPos())&&!state.formed){
            var front=forge.getBlockState().getValue(RuinedAncientForgeBlock.FACING);var cells=new java.util.ArrayList<Ghost>();
            for(var part:RuinedForgeStructure.inspect(forge.getLevel(),forge.getBlockPos(),front).missing()){
                var at=part.at(forge.getBlockPos(),front);var delta=at.subtract(forge.getBlockPos());cells.add(new Ghost(delta.getX(),delta.getY(),delta.getZ(),!forge.getLevel().getBlockState(at).isAir()));
            }
            state.ghosts=java.util.List.copyOf(cells);
        }
        var item=forge.getItem(5);if(item.isEmpty())for(int i=0;i<5;i++)if(!forge.getItem(i).isEmpty()){item=forge.getItem(i);break;}
        resolver.updateForTopItem(state.item,item,ItemDisplayContext.FIXED,forge.getLevel(),null,(int)forge.getBlockPos().asLong());
    }
    @Override public void submit(State state,PoseStack stack,SubmitNodeCollector collector,CameraRenderState camera){
        for(var ghost:state.ghosts){
            stack.pushPose();stack.translate(ghost.x(),ghost.y(),ghost.z());
            collector.order(1).submitModel(ghostModel,new net.minecraft.client.renderer.entity.state.LivingEntityRenderState(),stack,net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(net.minecraft.resources.Identifier.fromNamespaceAndPath(ExampleMod.MODID,"textures/block/forge_stone.png")),15728880,OverlayTexture.NO_OVERLAY,ghost.occupied()?0x77F2A156:0x663CCFDF,null,0);
            stack.popPose();
        }
        if(!state.item.isEmpty()){
            stack.pushPose();stack.translate(.5,state.formed?.86:.34,.5);stack.rotateDegrees(Axis.XP,90);stack.scale(.5F,.5F,.5F);state.item.submit(stack,collector,state.lightCoords,OverlayTexture.NO_OVERLAY,0);stack.popPose();
        }
    }
}
