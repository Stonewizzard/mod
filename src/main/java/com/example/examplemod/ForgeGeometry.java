package com.example.examplemod;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
/** Generated from tools/generate_formed_forge.py; cell shapes and UV models share one definition. */
public final class ForgeGeometry {
    private static final java.util.List<java.util.Map<Direction,VoxelShape>> SHAPES=java.util.List.of(
        WorkshopShapes.rotations(new double[][]{{0,0,0,16,11,16},{0,11,0,16,12,16},{0,12,0,16,16,16}}),
        WorkshopShapes.rotations(new double[][]{{0,0,0,16,11,16},{0,11,0,16,12,16},{0,12,0,16,16,16}}),
        WorkshopShapes.rotations(new double[][]{{0,0,0,16,11,16},{0,11,0,16,12,16},{0,12,0,16,16,16}}),
        WorkshopShapes.rotations(new double[][]{{0,0,0,16,11,16},{0,11,0,16,12,16},{0,12,0,16,16,16}}),
        WorkshopShapes.rotations(new double[][]{{0,0,0,16,11,16},{0,11,0,16,12,16},{0,12,0,16,16,16}}),
        WorkshopShapes.rotations(new double[][]{{0,0,0,16,11,16},{0,11,0,16,12,16},{0,12,0,16,16,16}}),
        WorkshopShapes.rotations(new double[][]{{0,0,0,16,11,16},{0,11,0,16,12,16},{0,12,0,16,16,16}}),
        WorkshopShapes.rotations(new double[][]{{0,0,0,16,11,16},{0,11,0,16,12,16},{0,12,0,16,16,16}}),
        WorkshopShapes.rotations(new double[][]{{0,0,0,16,11,16},{0,11,0,16,12,16},{0,12,0,16,16,16}}),
        WorkshopShapes.rotations(new double[][]{{2,0,2,16,11,16},{2,11,2,16,12,16},{1,12,1,16,16,16}}),
        WorkshopShapes.rotations(new double[][]{{4,0,4,14,10,16},{3,10,3,15,12,16},{4,12,4,14,14,15}}),
        WorkshopShapes.rotations(new double[][]{{0,0,2,14,11,16},{0,11,2,14,12,16},{0,12,1,15,16,16}}),
        WorkshopShapes.rotations(new double[][]{{2,0,4,12,10,16},{1,10,3,13,12,16},{2,12,4,12,14,15}}),
        WorkshopShapes.rotations(new double[][]{{0,0,0,16,6,14},{0,6,0,16,16,14}}),
        WorkshopShapes.rotations(new double[][]{{0,0,0,16,4,14},{3,4,0,13,5,12},{0,4,0,3,16,14},{13,4,0,16,16,14},{3,5,12,13,16,14}}),
        WorkshopShapes.rotations(new double[][]{{0,0,0,16,6,14},{0,6,0,16,16,14}}),
        WorkshopShapes.rotations(new double[][]{{0,0,0,16,1,14},{0,1,0,16,4,14},{2,4,2,14,10,14},{4,10,4,12,16,14}}),
        WorkshopShapes.rotations(new double[][]{{4,0,4,6,12,14},{10,0,4,12,12,14},{6,0,4,10,12,6},{6,0,12,10,12,14},{3,12,3,6,16,15},{10,12,3,13,14,15},{6,12,3,10,14,6},{6,12,12,10,15,15}}),
        WorkshopShapes.rotations(new double[][]{{1,0,1,4,9,15},{12,0,1,15,9,15},{0,9,0,16,11,16},{0,11,0,16,13,16},{4,13,4,12,13.5,12}}),
        WorkshopShapes.rotations(new double[][]{{1,0,1,15,3,15},{0,3,0,16,4,16},{2,4,2,4,10,14},{12,4,2,14,8,14},{4,4,12,12,9,14},{4,4,4,12,5,12}})
    );
    public static VoxelShape shape(int cell,Direction facing){return SHAPES.get(cell).get(facing);}
    private ForgeGeometry(){}
}
