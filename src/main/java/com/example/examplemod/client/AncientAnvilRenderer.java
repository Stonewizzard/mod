package com.example.examplemod.client;

import com.example.examplemod.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** Full-bright warm inlays gently pulse; the base remains a normal baked block model. */
public final class AncientAnvilRenderer implements BlockEntityRenderer<AncientAnvilBlockEntity,AncientAnvilRenderer.State> {
    private final ForgeGhostModel cube=new ForgeGhostModel();
    private static final Identifier MATERIAL=Identifier.fromNamespaceAndPath(ExampleMod.MODID,"textures/block/forge_stone.png");
    private static final Identifier GLOW=Identifier.fromNamespaceAndPath(ExampleMod.MODID,"textures/block/anvil_gold.png");
    public record Cell(int x,int z,boolean occupied){}
    public static final class State extends BlockEntityRenderState {public java.util.List<Cell> cells=java.util.List.of();public Direction facing=Direction.NORTH;public int glow;}
    public AncientAnvilRenderer(BlockEntityRendererProvider.Context c){}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(AncientAnvilBlockEntity be,State s,float partial,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking){
        BlockEntityRenderer.super.extractRenderState(be,s,partial,camera,breaking);s.cells=java.util.List.of();var l=be.getLevel();s.facing=be.getBlockState().getValue(AncientAnvilBlock.FACING);
        int tier=be.getBlockState().getValue(AncientAnvilBlock.TIER);int alpha=(int)((tier==1?150:65)+(tier==1?35:15)*Math.sin((l.getGameTime()+partial)*.06));s.glow=(alpha<<24)|0xff7c18;
        if(AncientAnvilPreview.active(l,be.getBlockPos())){var cells=new java.util.ArrayList<Cell>();for(var p:AncientAnvilBlockEntity.missing(l,be.getBlockPos()))cells.add(new Cell(p.getX()-be.getBlockPos().getX(),p.getZ()-be.getBlockPos().getZ(),!l.getBlockState(p).isAir()));s.cells=java.util.List.copyOf(cells);}
    }
    private void box(PoseStack p,SubmitNodeCollector c,Identifier texture,int color){c.order(1).submitModel(cube,new LivingEntityRenderState(),p,RenderTypes.entityTranslucent(texture),15728880,OverlayTexture.NO_OVERLAY,color,null,0);}
    @Override public void submit(State s,PoseStack p,SubmitNodeCollector c,CameraRenderState camera){
        for(var cell:s.cells){p.pushPose();p.translate(cell.x(),-1,cell.z());box(p,c,MATERIAL,cell.occupied()?0xD0FFD438:0x663CCFDF);p.popPose();}
        p.pushPose();p.translate(.5,0,.5);p.rotateDegrees(Axis.YP,switch(s.facing){case EAST->-90;case SOUTH->180;case WEST->90;default->0;});p.translate(-.5,0,-.5);
        for(float x:new float[]{.32F,.66F}){p.pushPose();p.translate(x,.813F,.44F);p.scale(.02F,.004F,.5F);box(p,c,GLOW,s.glow);p.popPose();}p.popPose();
    }
}
