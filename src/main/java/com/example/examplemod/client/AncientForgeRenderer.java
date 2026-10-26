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
    public static final class State extends BlockEntityRenderState {public ItemStackRenderState item=new ItemStackRenderState();}
    public AncientForgeRenderer(BlockEntityRendererProvider.Context context){resolver=context.itemModelResolver();}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(ReconstructionTableBlockEntity forge,State state,float partialTick,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking){
        BlockEntityRenderer.super.extractRenderState(forge,state,partialTick,camera,breaking);state.item=new ItemStackRenderState();
        if(!forge.getBlockState().is(AncientForgeContent.CORE.get()))return;
        var item=forge.getItem(5);if(item.isEmpty())for(int i=0;i<5;i++)if(!forge.getItem(i).isEmpty()){item=forge.getItem(i);break;}
        resolver.updateForTopItem(state.item,item,ItemDisplayContext.FIXED,forge.getLevel(),null,(int)forge.getBlockPos().asLong());
    }
    @Override public void submit(State state,PoseStack stack,SubmitNodeCollector collector,CameraRenderState camera){
        if(state.item.isEmpty())return;stack.pushPose();stack.translate(.5,.47,.5);stack.mulPose(Axis.XP.rotationDegrees(90));stack.scale(.5F,.5F,.5F);state.item.submit(stack,collector,state.lightCoords,OverlayTexture.NO_OVERLAY,0);stack.popPose();
    }
}
