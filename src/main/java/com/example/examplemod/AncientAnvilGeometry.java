package com.example.examplemod;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
/** Generated with the block model by tools/generate_ancient_anvil.py. */
public final class AncientAnvilGeometry {
    private static final java.util.Map<Direction,VoxelShape> SHAPES=WorkshopShapes.rotations(new double[][]{
        {2,0,2,14,2,14},
        {3,2,3,13,3,13},
        {4,3,5,12,5,13},
        {5,5,6,11,9,12},
        {4,9,5,12,10,13},
        {4,10,6,12,12,16},
        {3,12,6,13,12.5,16},
        {3,12.5,6,13,15.5,16},
        {3,15.5,6,13,16,16},
        {4,12,4,12,15,6},
        {4,15,4,12,15.5,6},
        {5,12.5,2,11,14.5,4},
        {5,14.5,2,11,15,4},
        {6,13,1,10,14.5,2},
        {7,13.5,0,9,14.5,1},
        {4,4,6,5,9,12},
        {11,4,6,12,9,12}
    });
    public static VoxelShape shape(Direction facing){return SHAPES.get(facing);}
    private AncientAnvilGeometry(){}
}
