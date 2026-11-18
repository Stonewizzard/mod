package com.example.examplemod.client;

import com.example.examplemod.*;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;

/** Render the actual assembled silhouette, never the nose's collision staircase. */
public final class AncientAnvilOutline {
    private static final Map<Direction,VoxelShape> EXPANDED=new EnumMap<>(Direction.class);
    static {for(var facing:Direction.Plane.HORIZONTAL)EXPANDED.put(facing,new Edges(null,facing));}
    public static VoxelShape expanded(Direction facing){return EXPANDED.get(facing);}
    private static final Map<AncientAnvilDesign,Map<Direction,VoxelShape>> SHAPES=new EnumMap<>(AncientAnvilDesign.class);
    static {for(var design:AncientAnvilDesign.values()){var facings=new EnumMap<Direction,VoxelShape>(Direction.class);for(var facing:Direction.Plane.HORIZONTAL)facings.put(facing,new Edges(design,facing));SHAPES.put(design,facings);}}
    public static VoxelShape shape(Direction facing){return shape(AncientAnvilDesign.BASE,facing);}
    public static VoxelShape shape(AncientAnvilDesign design,Direction facing){return SHAPES.get(design).get(facing);}

    public static void extract(ExtractBlockOutlineRenderStateEvent event){
        var level=event.getLevel();var state=event.getBlockState();var at=event.getBlockPos();
        BlockPos core;
        if(state.is(AncientAnvilContent.ANVIL.get()))core=at;
        else if(state.is(AncientAnvilContent.PART.get()))core=AncientAnvilPartBlock.core(at,state);
        else return;
        var owner=level.getBlockState(core);
        if(!owner.is(AncientAnvilContent.ANVIL.get())||!owner.getValue(AncientAnvilBlock.LARGE)||!owner.getValue(AncientAnvilBlock.MONUMENTAL))return;
        var facing=owner.getValue(AncientAnvilBlock.FACING);
        boolean expanded=owner.getValue(AncientAnvilBlock.EXPANDED);
        for(var cell:AncientAnvilStructure.CELLS)if((expanded||cell.index()<18)&&cell.index()!=0&&!AncientAnvilStructure.belongs(level,cell.at(core,facing),core,facing,cell.index()))return;
        var position=core.immutable();var outline=expanded?expanded(facing):shape(owner.getValue(AncientAnvilBlock.DESIGN),facing);
        var hit=net.minecraft.client.Minecraft.getInstance().hitResult;
        var slot=expanded&&hit instanceof net.minecraft.world.phys.BlockHitResult block?AnvilSlot.nearest(core,facing,block.getLocation()):null;
        var v=slot==null?null:slot.world(core,facing).subtract(net.minecraft.world.phys.Vec3.atLowerCornerOf(core));
        VoxelShape target=v==null?null:Shapes.box(v.x-.13,v.y+.006,v.z-.13,v.x+.13,v.y+.012,v.z+.13);boolean contrast=event.isHighContrast();
        // Extract immutable data here. Rendering never captures or reads the level.
        event.addCustomRenderer((renderState,collector,poses,levelState)->{
            var camera=levelState.cameraRenderState.pos;
            poses.pushPose();poses.translate(position.getX()-camera.x,position.getY()-camera.y,position.getZ()-camera.z);
            if(contrast)collector.submitShapeOutline(poses,outline,RenderTypes.lines(),0xFF000000,5f,false);
            collector.submitShapeOutline(poses,outline,RenderTypes.lines(),contrast?0xFFFFFFFF:0x66000000,contrast?3f:1.5f,false);
            if(target!=null)collector.submitShapeOutline(poses,target,RenderTypes.lines(),0xFFFFD582,2f,false);
            poses.popPose();return true;
        });
    }

    /** Visual-only shape: native outline submission consumes the precise edges.
     * Collision and picking continue to use the bounded per-cell voxel shapes. */
    private static final class Edges extends VoxelShape {
        private final List<double[]> lines;
        private final DoubleList[] coordinates=new DoubleList[3];
        Edges(AncientAnvilDesign design,Direction facing){
            super(solid());var edges=new ArrayList<double[]>();
            (design==null?ExpandedAncientAnvilGeometry.outlineBody(facing):AncientAnvilGeometry.outlineBody(design,facing)).forAllEdges((x,y,z,X,Y,Z)->edges.add(new double[]{x,y,z,X,Y,Z}));
            for(var edge:(design==null?ExpandedAncientAnvilGeometry.noseEdges(facing):AncientAnvilGeometry.noseEdges(design,facing)))edges.add(edge.clone());
            lines=List.copyOf(edges);
            for(int axis=0;axis<3;axis++){
                double lo=Double.POSITIVE_INFINITY,hi=Double.NEGATIVE_INFINITY;
                for(var e:lines){lo=Math.min(lo,Math.min(e[axis],e[axis+3]));hi=Math.max(hi,Math.max(e[axis],e[axis+3]));}
                coordinates[axis]=DoubleArrayList.wrap(new double[]{lo,hi});
            }
        }
        private static DiscreteVoxelShape solid(){var shape=new BitSetDiscreteVoxelShape(1,1,1);shape.fill(0,0,0);return shape;}
        @Override public DoubleList getCoords(Direction.Axis axis){return coordinates[axis.ordinal()];}
        @Override public void forAllEdges(Shapes.DoubleLineConsumer consumer){for(var e:lines)consumer.consume(e[0],e[1],e[2],e[3],e[4],e[5]);}
    }
    private AncientAnvilOutline(){}
}
