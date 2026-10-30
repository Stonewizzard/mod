package com.example.examplemod;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
/** Generated with the block model by tools/generate_ancient_anvil.py. */
public final class AncientAnvilGeometry {
    private static final java.util.Map<Direction,VoxelShape> SHAPES=WorkshopShapes.rotations(new double[][]{
        {2,0,2,14,2,14},
        {3,2,3,13,3,13},
        {4,3,4,12,5,12},
        {5,5,5,11,9,11},
        {4,9,4,12,10,12},
        {3,10,3,13,11,14},
        {2,11,3,14,12,16},
        {2,12,3,14,15,16},
        {2,15,3,14,16,16},
        {3,12,1,13,14,3},
        {4,14,1,12,15,3},
        {5,12,0,11,14,1},
        {3,4,4,4,9,12},
        {12,4,4,13,9,12}
    });
    public static VoxelShape shape(Direction facing){return SHAPES.get(facing);}
    private AncientAnvilGeometry(){}
}
