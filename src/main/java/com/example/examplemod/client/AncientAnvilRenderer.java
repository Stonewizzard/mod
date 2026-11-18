package com.example.examplemod.client;

import com.example.examplemod.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;

/** Physical workpieces and opt-in ghosts. All world reads happen during extraction. */
public final class AncientAnvilRenderer implements BlockEntityRenderer<AncientAnvilBlockEntity,AncientAnvilRenderer.State> {
    private final ForgeGhostModel cube=new ForgeGhostModel();
    private final ItemModelResolver resolver;
    private static final Identifier MATERIAL=Identifier.withDefaultNamespace("textures/block/deepslate_bricks.png");
    public record Cell(int x,int y,int z,boolean occupied){}
    public record Workpiece(ItemStackRenderState item,Vec3 at,boolean block,boolean output){}
    public static final class State extends BlockEntityRenderState {public List<Cell> cells=List.of();public List<Workpiece> pieces=List.of();public float turn;}
    public AncientAnvilRenderer(BlockEntityRendererProvider.Context c){resolver=c.itemModelResolver();}
    @Override public State createRenderState(){return new State();}
    @Override public AABB getRenderBoundingBox(AncientAnvilBlockEntity be){
        var core=be.getBlockPos();var facing=be.getBlockState().getValue(AncientAnvilBlock.FACING);int minX=core.getX(),minZ=core.getZ(),maxX=minX+1,maxZ=minZ+1;
        for(var cell:AncientAnvilStructure.CELLS){var at=cell.at(core,facing);minX=Math.min(minX,at.getX());minZ=Math.min(minZ,at.getZ());maxX=Math.max(maxX,at.getX()+1);maxZ=Math.max(maxZ,at.getZ()+1);}
        return new AABB(minX,core.getY()-1,minZ,maxX,core.getY()+3,maxZ);
    }
    @Override public void extractRenderState(AncientAnvilBlockEntity be,State s,float partial,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking){
        BlockEntityRenderer.super.extractRenderState(be,s,partial,camera,breaking);s.cells=List.of();s.pieces=List.of();var level=be.getLevel();if(level==null)return;
        var core=be.getBlockPos();var state=be.getBlockState();var facing=state.getValue(AncientAnvilBlock.FACING);boolean expanded=state.getValue(AncientAnvilBlock.EXPANDED);
        s.turn=switch(facing){case EAST->-90;case SOUTH->-180;case WEST->-270;default->0;};
        if(AncientAnvilPreview.active(level,core)){
            var cells=new ArrayList<Cell>();
            for(var p:AncientAnvilBlockEntity.missing(level,core))cells.add(new Cell(p.getX()-core.getX(),-1,p.getZ()-core.getZ(),!level.getBlockState(p).isAir()));
            if(!expanded)for(var cell:AncientAnvilStructure.CELLS)if(cell.index()!=0){var at=cell.at(core,facing);if(!AncientAnvilStructure.belongs(level,at,core,facing,cell.index()))cells.add(new Cell(at.getX()-core.getX(),at.getY()-core.getY(),at.getZ()-core.getZ(),!level.getBlockState(at).isAir()));}
            s.cells=List.copyOf(cells);
        }
        if(expanded){
            var pieces=new ArrayList<Workpiece>();
            for(var slot:AnvilSlot.values()){
                boolean output=slot==AnvilSlot.CENTER&&!be.output().isEmpty();var stack=output?be.output():be.input(slot);if(stack.isEmpty())continue;
                var item=new ItemStackRenderState();resolver.updateForTopItem(item,stack,ItemDisplayContext.FIXED,level,null,slot.ordinal());
                pieces.add(new Workpiece(item,slot.local(),stack.getItem() instanceof BlockItem,output));
            }
            s.pieces=List.copyOf(pieces);
        }
    }
    @Override public void submit(State s,PoseStack p,SubmitNodeCollector c,CameraRenderState camera){
        for(var cell:s.cells){p.pushPose();p.translate(cell.x(),cell.y(),cell.z());c.order(1).submitModel(cube,new LivingEntityRenderState(),p,RenderTypes.entityTranslucent(MATERIAL),15728880,OverlayTexture.NO_OVERLAY,cell.occupied()?0xD0FFD438:0x663CCFDF,null,0);c.submitShapeOutline(p,net.minecraft.world.phys.shapes.Shapes.box(-.005,0,-.005,1.005,cell.y()==-1?1.6:1.005,1.005),RenderTypes.lines(),cell.occupied()?0xFFFFD438:0xFF3CCFDF,2f,false);p.popPose();}
        for(var piece:s.pieces){
            float scale=piece.block()?(piece.output()?.60F:.34F):(piece.output()?.40F:.25F);
            p.pushPose();p.translate(.5,0,.5);p.rotateDegrees(Axis.YP,s.turn);
            p.translate(piece.at().x-.5,piece.at().y+(piece.block()?scale*.25:.022),piece.at().z-.5);
            if(!piece.block())p.rotateDegrees(Axis.XP,90);p.scale(scale,scale,scale);piece.item().submit(p,c,s.lightCoords,OverlayTexture.NO_OVERLAY,0);p.popPose();
        }
    }
}
