package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A sniffer head with 16 standing rotations and four flush wall directions. */
public class SnifferSkullBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty WALL_MOUNTED = BooleanProperty.create("wall_mounted");
    // Four 22.5-degree offsets within each cardinal quadrant: 4 x 4 = 16 directions.
    public static final IntegerProperty ROTATION_OFFSET = IntegerProperty.create("rotation_offset", 0, 3);
    // Same bounds as the JSON model: head, nose, lower beak, and both floppy ears.
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(3.125, 0.75, 7.25, 12.875, 14.25, 15.5),
            Block.box(3.125, 12, 0.5, 12.875, 13.5, 7.25),
            Block.box(3.125, 3, 0.5, 12.875, 12, 7.25),
            Block.box(2.3675, 0, 10.2425, 3.1175, 14.25, 15.4925),
            Block.box(12.8825, 0, 10.2425, 13.6325, 14.25, 15.4925));
    private static final VoxelShape EAST_SHAPE = turnClockwise(NORTH_SHAPE);
    private static final VoxelShape SOUTH_SHAPE = turnClockwise(EAST_SHAPE);
    private static final VoxelShape WEST_SHAPE = turnClockwise(SOUTH_SHAPE);

    // Move the back of a wall-mounted head from 15.5 to the block boundary at 16.
    private static final VoxelShape NORTH_WALL_SHAPE = NORTH_SHAPE.move(0, 0, 0.5 / 16.0);
    private static final VoxelShape EAST_WALL_SHAPE = turnClockwise(NORTH_WALL_SHAPE);
    private static final VoxelShape SOUTH_WALL_SHAPE = turnClockwise(EAST_WALL_SHAPE);
    private static final VoxelShape WEST_WALL_SHAPE = turnClockwise(SOUTH_WALL_SHAPE);

    private static final VoxelShape[] STANDING_SHAPES = createStandingShapes();

    public SnifferSkullBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WALL_MOUNTED, false)
                .setValue(ROTATION_OFFSET, 0));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis().isHorizontal()) {
            return defaultBlockState().setValue(FACING, clickedFace).setValue(WALL_MOUNTED, true);
        }
        // The model points north at index 0, so yaw 0 (looking south) faces the player.
        int index = Mth.floor(context.getRotation() * 16.0F / 360.0F + 0.5F) & 15;
        return withRotation(defaultBlockState(), index);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WALL_MOUNTED, ROTATION_OFFSET);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(WALL_MOUNTED)) {
            return switch (state.getValue(FACING)) {
                case EAST -> EAST_WALL_SHAPE;
                case SOUTH -> SOUTH_WALL_SHAPE;
                case WEST -> WEST_WALL_SHAPE;
                default -> NORTH_WALL_SHAPE;
            };
        }
        return STANDING_SHAPES[rotationIndex(state)];
    }


    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.getValue(WALL_MOUNTED) ? super.rotate(state, rotation)
                : withRotation(state, rotation.rotate(rotationIndex(state), 16));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.getValue(WALL_MOUNTED) ? super.mirror(state, mirror)
                : withRotation(state, mirror.mirror(rotationIndex(state), 16));
    }

    private static int rotationIndex(BlockState state) {
        int quadrant = switch (state.getValue(FACING)) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
        return quadrant * 4 + state.getValue(ROTATION_OFFSET);
    }

    private static BlockState withRotation(BlockState state, int index) {
        index &= 15;
        Direction facing = switch (index / 4) {
            case 1 -> Direction.EAST;
            case 2 -> Direction.SOUTH;
            case 3 -> Direction.WEST;
            default -> Direction.NORTH;
        };
        return state.setValue(FACING, facing).setValue(ROTATION_OFFSET, index % 4);
    }

    private static VoxelShape[] createStandingShapes() {
        VoxelShape[] shapes = new VoxelShape[16];
        for (int index = 0; index < 16; index++) {
            shapes[index] = rotatedOutline(index * Math.PI / 8.0);
        }
        return shapes;
    }

    private static VoxelShape rotatedOutline(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        VoxelShape[] result = {Shapes.empty()};
        // Voxels are axis-aligned: enclose each rotated model part in its bounding box.
        NORTH_SHAPE.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double lowX = Double.POSITIVE_INFINITY, lowZ = Double.POSITIVE_INFINITY;
            double highX = Double.NEGATIVE_INFINITY, highZ = Double.NEGATIVE_INFINITY;
            for (double x : new double[]{minX, maxX}) {
                for (double z : new double[]{minZ, maxZ}) {
                    double rx = 0.5 + cos * (x - 0.5) - sin * (z - 0.5);
                    double rz = 0.5 + sin * (x - 0.5) + cos * (z - 0.5);
                    lowX = Math.min(lowX, rx);
                    highX = Math.max(highX, rx);
                    lowZ = Math.min(lowZ, rz);
                    highZ = Math.max(highZ, rz);
                }
            }
            result[0] = Shapes.or(result[0], Shapes.box(lowX, minY, lowZ, highX, maxY, highZ));
        });
        return result[0].optimize();
    }

    private static VoxelShape turnClockwise(VoxelShape shape) {
        // VoxelShape coordinates use 0..1, unlike Block.box's 0..16 coordinates.
        VoxelShape[] result = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                result[0] = Shapes.or(result[0], Shapes.box(
                        1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
        return result[0].optimize();
    }
}
