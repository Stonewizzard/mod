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
    private final ForgeGhostModel cube=new ForgeGhostModel(0);
    private final ItemModelResolver resolver;
    private static final Identifier MATERIAL=Identifier.fromNamespaceAndPath(ExampleMod.MODID,"textures/block/runestone.png");
    public record Cell(int x,int y,int z,boolean occupied,AnvilGuide.Kind kind){}
    public record Workpiece(ItemStackRenderState item,Vec3 at,boolean block,boolean output){}
    public static final class State extends BlockEntityRenderState {public List<Cell> cells=List.of();public List<Workpiece> pieces=List.of();public float turn,time;public List<AnvilEnergyRenderer.Beam> beams=List.of();}
    public AncientAnvilRenderer(BlockEntityRendererProvider.Context c){resolver=c.itemModelResolver();}
    @Override public State createRenderState(){return new State();}
    @Override public AABB getRenderBoundingBox(AncientAnvilBlockEntity be){
        var core=be.getBlockPos();var facing=be.getBlockState().getValue(AncientAnvilBlock.FACING);int minX=core.getX(),minZ=core.getZ(),maxX=minX+1,maxZ=minZ+1;
        for(var cell:AncientAnvilStructure.CELLS){var at=cell.at(core,facing);minX=Math.min(minX,at.getX());minZ=Math.min(minZ,at.getZ());maxX=Math.max(maxX,at.getX()+1);maxZ=Math.max(maxZ,at.getZ()+1);}
        for(var at:AncientAnvilStructure.foundation(core,facing)){minX=Math.min(minX,at.getX());minZ=Math.min(minZ,at.getZ());maxX=Math.max(maxX,at.getX()+1);maxZ=Math.max(maxZ,at.getZ()+1);}
        int minY=core.getY()-1,maxY=core.getY()+3;
        if(be.getLevel()!=null)for(var target:AnvilGuide.next(be.getLevel(),core).targets()){var at=target.at();minX=Math.min(minX,at.getX());minZ=Math.min(minZ,at.getZ());maxX=Math.max(maxX,at.getX()+1);maxZ=Math.max(maxZ,at.getZ()+1);minY=Math.min(minY,at.getY());maxY=Math.max(maxY,at.getY()+1);}
        if(be.getBlockState().getValue(AncientAnvilBlock.TIER)>=5){minX=Math.min(minX,core.getX()-18);minZ=Math.min(minZ,core.getZ()-18);maxX=Math.max(maxX,core.getX()+19);maxZ=Math.max(maxZ,core.getZ()+19);minY=Math.min(minY,core.getY()-5);maxY=Math.max(maxY,core.getY()+50);}
        return new AABB(minX,minY,minZ,maxX,maxY,maxZ);
    }
    @Override public void extractRenderState(AncientAnvilBlockEntity be,State s,float partial,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking){
        BlockEntityRenderer.super.extractRenderState(be,s,partial,camera,breaking);s.cells=List.of();s.pieces=List.of();s.beams=List.of();var level=be.getLevel();if(level==null)return;
        s.time=level.getGameTime()+partial;s.beams=AnvilEnergyRenderer.extract(be,partial);
        var core=be.getBlockPos();var state=be.getBlockState();var facing=state.getValue(AncientAnvilBlock.FACING);boolean expanded=state.getValue(AncientAnvilBlock.EXPANDED);
        s.turn=switch(facing){case EAST->-90;case SOUTH->-180;case WEST->-270;default->0;};
        if(AncientAnvilPreview.active(level,core)){
            var cells=new ArrayList<Cell>();
            for(var target:AnvilGuide.next(level,core).missing(level)){var p=target.at();cells.add(new Cell(p.getX()-core.getX(),p.getY()-core.getY(),p.getZ()-core.getZ(),!level.getBlockState(p).isAir(),target.kind()));}
            if(!expanded)for(var cell:AncientAnvilStructure.OCCUPIED)if(cell.index()!=0){var at=cell.at(core,facing);if(!AncientAnvilStructure.belongs(level,at,core,facing,cell.index()))cells.add(new Cell(at.getX()-core.getX(),at.getY()-core.getY(),at.getZ()-core.getZ(),!level.getBlockState(at).isAir(),AnvilGuide.Kind.BLOCK));}
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
    @Override public int getViewDistance(){return 128;}
    @Override public void submit(State s,PoseStack p,SubmitNodeCollector c,CameraRenderState camera){
        AnvilEnergyRenderer.submit(s.beams,s.time,p,c);
        for(var cell:s.cells){
            var material=switch(cell.kind()){case KEYSTONE->Identifier.fromNamespaceAndPath(ExampleMod.MODID,"textures/block/canopy_keystone.png");case CAP->Identifier.withDefaultNamespace("textures/block/gold_block.png");case ENERGIZED->Identifier.fromNamespaceAndPath(ExampleMod.MODID,"textures/block/energized_ancient_crystal_side.png");case OBSIDIAN->Identifier.withDefaultNamespace("textures/block/obsidian.png");case GLASS,CRYSTAL->Identifier.withDefaultNamespace("textures/block/glass.png");case SLAB->Identifier.withDefaultNamespace("textures/block/polished_deepslate.png");case FLUID->Identifier.withDefaultNamespace("textures/block/blue_stained_glass.png");default->MATERIAL;};
            int color=cell.occupied()?0xFFFFD438:switch(cell.kind()){case KEYSTONE->0xFFFF9B18;case CAP->0xFFFFC65C;case ENERGIZED->0xFF82E4F0;case OBSIDIAN->0xFF9E79DC;case GLASS->0xFFB8F3FB;case CRYSTAL->0xFFF385FF;case FLUID->0xFF649EFF;default->0xFF3CCFDF;};
            p.pushPose();p.translate(cell.x(),cell.y(),cell.z());if(cell.kind()==AnvilGuide.Kind.SLAB)p.scale(1,.5F,1);c.order(1).submitModel(cube,new LivingEntityRenderState(),p,RenderTypes.entityTranslucent(material),15728880,OverlayTexture.NO_OVERLAY,(color&0xFFFFFF)|(cell.occupied()?0xA0000000:0x66000000),null,0);c.submitShapeOutline(p,net.minecraft.world.phys.shapes.Shapes.box(0,0,0,1,1,1),RenderTypes.lines(),color,2f,false);p.popPose();
        }
        for(var piece:s.pieces){
            float scale=piece.block()?(piece.output()?.60F:.34F):(piece.output()?.40F:.25F);
            p.pushPose();p.translate(.5,0,.5);p.rotateDegrees(Axis.YP,s.turn);
            p.translate(piece.at().x-.5,piece.at().y+(piece.block()?scale*.25:.022),piece.at().z-.5);
            if(!piece.block())p.rotateDegrees(Axis.XP,90);p.scale(scale,scale,scale);piece.item().submit(p,c,s.lightCoords,OverlayTexture.NO_OVERLAY,0);p.popPose();
        }
    }
}
