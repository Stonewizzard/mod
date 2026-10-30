package com.example.examplemod;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
/** Generated with the block model by tools/generate_ancient_anvil.py. */
public final class AncientAnvilGeometry {
    private static final java.util.Map<Direction,VoxelShape> SHAPES=WorkshopShapes.rotations(new double[][]{
        {3,0,3,13,2,14},
        {4,2,5,12,3,13},
        {5,3,7,11,7,12},
        {4,7,6,12,9,14},
        {3,9,6,13,12,16},
        {3,12,6,13,13,16},
        {4,9,4,12,12,6},
        {5,10,2,11,12,4},
        {6,10,0,10,11,2}
    });
    public static VoxelShape shape(Direction facing){return SHAPES.get(facing);}
    private AncientAnvilGeometry(){}
}
