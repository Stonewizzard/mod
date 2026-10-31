package com.example.examplemod;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
/** Generated with six owned cell models by tools/generate_ancient_anvil.py. */
public final class AncientAnvilGeometry {
    private static final java.util.Map<Direction,VoxelShape> COMPACT=WorkshopShapes.rotations(new double[][]{{2,0,2,14,3,14},{5,3,5,11,10,12},{3,10,4,13,15,16},{3,14,4,13,15,16},{5,11,0,11,14,4}});
    private static final java.util.List<java.util.Map<Direction,VoxelShape>> CELLS=java.util.List.of(WorkshopShapes.rotations(new double[][]{{2,0,0,14,4,16},{3,4,0,13,5,16},{4,5,2,12,8,16},{5,8,4,11,11,16},{5,11,5,11,16,16},{7,11,4,9,14,5},{6,14,4,10,15,5}}),WorkshopShapes.rotations(new double[][]{{2,0,14,14,4,16}}),WorkshopShapes.rotations(new double[][]{{2,0,0,14,4,6},{3,4,0,13,5,4},{4,5,0,12,8,2},{5,8,0,11,11,1}}),WorkshopShapes.rotations(new double[][]{{2,4,13,14,7,16},{3,7,14,13,8,16},{4,2,8,12,6,13},{5,2,4,11,5,8},{6,2,0,10,4,4}}),WorkshopShapes.rotations(new double[][]{{4,0,3,12,2,16},{3,2,0,13,4,16},{2,4,0,14,7,16},{3,7,0,13,8,16}}),WorkshopShapes.rotations(new double[][]{{4,0,0,12,2,8},{3,2,0,13,4,12},{2,4,0,14,7,14},{3,7,0,13,8,13}}));
    public static VoxelShape shape(Direction facing){return COMPACT.get(facing);}
    public static VoxelShape cell(int cell,Direction facing){return CELLS.get(cell).get(facing);}
    private AncientAnvilGeometry(){}
}
