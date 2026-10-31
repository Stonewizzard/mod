package com.example.examplemod;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
/** Generated with six owned cell models by tools/generate_ancient_anvil.py. */
public final class AncientAnvilGeometry {
    private static final java.util.Map<Direction,VoxelShape> COMPACT=WorkshopShapes.rotations(new double[][]{{2,0,2,14,3,14},{5,3,5,11,10,12},{3,10,4,13,15,16},{3,14,4,13,15,16},{5,11,0,11,14,4}});
    private static final java.util.List<java.util.Map<Direction,VoxelShape>> CELLS=java.util.List.of(WorkshopShapes.rotations(new double[][]{{2,0,0,14,4,16},{3,4,0,13,6,16},{4,6,3,12,9,16},{5,9,5,11,16,15},{6,10,4,10,11,5},{6,14,4,10,15,5},{6,11,4,7,14,5},{9,11,4,10,14,5}}),WorkshopShapes.rotations(new double[][]{{2,0,14,14,4,16}}),WorkshopShapes.rotations(new double[][]{{2,0,0,14,4,6},{3,4,0,13,6,4},{4,6,0,12,9,1}}),WorkshopShapes.rotations(new double[][]{{2,3,13,14,8,16},{2,7,13,14,8,16},{4,2,8,12,6,13},{5,2,4,11,5,8},{6,2,0,10,4,4}}),WorkshopShapes.rotations(new double[][]{{3,0,0,13,3,16},{2,3,0,14,8,16},{2,7,0,14,8,16}}),WorkshopShapes.rotations(new double[][]{{3,0,0,13,3,11},{2,3,0,14,8,14},{2,7,0,14,8,14}}));
    public static VoxelShape shape(Direction facing){return COMPACT.get(facing);}
    public static VoxelShape cell(int cell,Direction facing){return CELLS.get(cell).get(facing);}
    private AncientAnvilGeometry(){}
}
