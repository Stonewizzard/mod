package com.example.examplemod;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Cached selection/collision shapes follow the real models, including half-specific table legs. */
public final class WorkshopShapes {
    private static final Map<Direction,VoxelShape> LEFT=rotations(new double[][]{
        {0,14,0,16,16,16},
        {1,0,1,4,14,4},
        {1,0,12,4,14,15},
        {4,10,1,16,14,3},
        {4,10,13,16,14,15},
        {0,3,2,16,5,14},
        {1,5,4,4,14,12},
        {4.5,10.25,0.75,14.5,13.5,1.25},
        {8.0,11.25,0.25,11.0,12.25,0.75},
        {1,16.002,1,15,16.02,15}
    });
    private static final Map<Direction,VoxelShape> RIGHT=rotations(new double[][]{
        {0,14,0,16,16,16},
        {12,0,1,15,14,4},
        {12,0,12,15,14,15},
        {0,10,1,12,14,3},
        {0,10,13,12,14,15},
        {0,3,2,16,5,14},
        {12,5,4,15,14,12},
        {1.5,10.25,0.75,11.5,13.5,1.25},
        {5.0,11.25,0.25,8.0,12.25,0.75},
        {2,16.002,3,6,16.02,7},
        {2,16.002,9,6,16.02,13},
        {10,16.002,3,14,16.02,7},
        {10,16.002,9,14,16.02,13}
    });
    private static final Map<Direction,VoxelShape> RECONSTRUCTION=rotations(new double[][]{
        {0,14,0,16,16,16},
        {1,12,1,15,14,15},
        {1,0,1,4,12,4},
        {1,0,12,4,12,15},
        {12,0,1,15,12,4},
        {12,0,12,15,12,15},
        {2,3,2,14,5,14},
        {4,9,1,12,12,3},
        {4,9,13,12,12,15},
        {6,10,0.5,10,11,1},
        {2,16.002,2,14,16.02,14}
    });
    public static VoxelShape archaeology(boolean right,Direction facing){return (right?RIGHT:LEFT).get(facing);}
    public static VoxelShape reconstruction(Direction facing){return RECONSTRUCTION.get(facing);}
    private static Map<Direction,VoxelShape> rotations(double[][] boxes){
        var shapes=new EnumMap<Direction,VoxelShape>(Direction.class);
        for(var facing:Direction.Plane.HORIZONTAL){
            int turns=switch(facing){case EAST->1;case SOUTH->2;case WEST->3;default->0;};var shape=Shapes.empty();
            for(var box:boxes){
                // Thin surface inlays are decorative; collision must end at the normal block top.
                if(box[1]>=16)continue;
                double x=box[0],z=box[2],X=box[3],Z=box[5];
                for(int turn=0;turn<turns;turn++){double nx=16-Z,nX=16-z;z=x;Z=X;x=nx;X=nX;}
                shape=Shapes.or(shape,Block.box(x,box[1],z,X,box[4],Z));
            }
            shapes.put(facing,shape.optimize());
        }
        return Map.copyOf(shapes);
    }
    private WorkshopShapes(){}
}
