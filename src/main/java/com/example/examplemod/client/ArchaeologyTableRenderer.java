package com.example.examplemod.client;

import com.example.examplemod.ArchaeologyTableBlock;
import com.example.examplemod.ArchaeologyTableBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

/** Extract detached item states so shared renderers never read a live inventory during submission. */
public final class ArchaeologyTableRenderer implements BlockEntityRenderer<ArchaeologyTableBlockEntity,ArchaeologyTableRenderer.State> {
    private final ItemModelResolver resolver;
    public static final float[][] PLACES={{0,0,0.68F,8},{.70F,-.24F,.40F,-24},{1.18F,-.24F,.40F,18},{.70F,.24F,.40F,-15},{1.18F,.24F,.40F,15}};
    public static final class State extends BlockEntityRenderState {
        public Direction facing=Direction.NORTH;
        public List<ItemStackRenderState> items=List.of();
    }
    public ArchaeologyTableRenderer(BlockEntityRendererProvider.Context context){resolver=context.itemModelResolver();}
    ArchaeologyTableRenderer(ItemModelResolver resolver){this.resolver=resolver;}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(ArchaeologyTableBlockEntity table,State state,float partialTick,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking){
        BlockEntityRenderer.super.extractRenderState(table,state,partialTick,camera,breaking);
        state.facing=table.getBlockState().getValue(ArchaeologyTableBlock.FACING);
        var items=new ArrayList<ItemStackRenderState>(5);
        for(int i=0;i<5;i++){
            var item=new ItemStackRenderState();
            resolver.updateForTopItem(item,table.getItem(i==0&&table.getItem(0).isEmpty()?5:i),ItemDisplayContext.FIXED,table.getLevel(),null,(int)table.getBlockPos().asLong()+i);
            items.add(item);
        }
        state.items=List.copyOf(items);
    }
    public static void pose(PoseStack stack,Direction facing,int slot){
        var p=PLACES[slot];
        stack.translate(.5F,1.035F,.5F);
        stack.rotateDegrees(Axis.YP,180-facing.toYRot());
        stack.translate(p[0],0,p[1]);
        // Match vanilla's flat campfire-item orientation.
        stack.rotateDegrees(Axis.XP,90);
        stack.rotateDegrees(Axis.ZP,p[3]);
        stack.scale(p[2],p[2],p[2]);
    }
    @Override public void submit(State state,PoseStack stack,SubmitNodeCollector collector,CameraRenderState camera){
        for(int i=0;i<state.items.size();i++)if(!state.items.get(i).isEmpty()){
            stack.pushPose();pose(stack,state.facing,i);
            state.items.get(i).submit(stack,collector,state.lightCoords,OverlayTexture.NO_OVERLAY,0);
            stack.popPose();
        }
    }
}
