package com.example.examplemod;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
/** Generated with six owned cell models by tools/generate_ancient_anvil.py. */
public final class AncientAnvilGeometry {
    private static final java.util.Map<Direction,VoxelShape> COMPACT=WorkshopShapes.rotations(new double[][]{{2,0,2,14,3,14},{5,3,5,11,10,12},{3,10,4,13,14,16},{3,14,4,13,15,16},{5,11,0,11,14,4}});
    private static final java.util.List<java.util.Map<Direction,VoxelShape>> CELLS=java.util.List.of(WorkshopShapes.rotations(new double[][]{{1,0,0,15,3,16},{2,3,0,14,4,16},{3,4,0,13,5,16},{3,5,1,13,7,16},{4,7,4,12,16,16},{3,15,3,13,16,16},{3.5,9,8,4,16,16},{3.25,10,10,3.5,11,14},{3.25,15,10,3.5,16,14},{3.25,11,9,3.5,15,10},{3.25,11,14,3.5,15,15},{3.25,11,11,3.5,12,13},{3.25,12,11,3.5,14,12},{3.25,14,12,3.5,15,13},{12,9,8,12.5,16,16},{12.5,10,10,12.75,11,14},{12.5,15,10,12.75,16,14},{12.5,11,9,12.75,15,10},{12.5,11,14,12.75,15,15},{12.5,11,11,12.75,12,13},{12.5,12,11,12.75,14,12},{12.5,14,12,12.75,15,13}}),WorkshopShapes.rotations(new double[][]{{1,0,13,15,3,16},{2,3,14,14,4,16}}),WorkshopShapes.rotations(new double[][]{{1,0,0,15,3,7},{2,3,0,14,4,6},{3,4,0,13,5,4},{3,5,0,13,7,4},{4,7,0,12,16,2},{3,15,0,13,16,6}}),WorkshopShapes.rotations(new double[][]{{1,7,15,15,9,16},{1,9,15,15,10,16},{3,7,12,13,11,16},{4,7,9,12,10,12},{5,7,6,11,9,9},{7,7,4,9,8,6}}),WorkshopShapes.rotations(new double[][]{{4,0,4,12,1,16},{3,0,3,13,2,16},{3,2,1,13,5,16},{2,5,0,14,7,16},{1,7,0,15,9,16},{1,9,0,15,10,16},{2,10,0,14,12,16},{3.5,0,8,4,1,16},{12,0,8,12.5,1,16}}),WorkshopShapes.rotations(new double[][]{{4,0,0,12,1,2},{3,0,0,13,2,6},{3,2,0,13,5,8},{2,5,0,14,7,11},{1,7,0,15,9,12},{1,9,0,15,10,5},{1,9,8,15,10,12},{1,9,5,9,10,8},{12,9,5,15,10,8},{2,10,0,14,12,5},{2,10,8,14,12,11},{2,10,5,9,12,8},{12,10,5,14,12,8},{9,9,5,12,9.5,8}}));
    public static VoxelShape shape(Direction facing){return COMPACT.get(facing);}
    public static VoxelShape cell(int cell,Direction facing){return CELLS.get(cell).get(facing);}
    private AncientAnvilGeometry(){}
}
