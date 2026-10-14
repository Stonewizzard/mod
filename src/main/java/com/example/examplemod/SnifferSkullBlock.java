package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A decorative sniffer head with four placement directions and a matching outline. */
public class SnifferSkullBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty WALL_MOUNTED = BooleanProperty.create("wall_mounted");
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

    public SnifferSkullBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WALL_MOUNTED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Side placements face outward from the clicked surface, like wall heads.
        // Top/bottom placements use the player's horizontal direction.
        Direction clickedFace = context.getClickedFace();
        boolean wallMounted = clickedFace.getAxis().isHorizontal();
        Direction facing = wallMounted ? clickedFace : context.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(FACING, facing).setValue(WALL_MOUNTED, wallMounted);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WALL_MOUNTED);
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
        return switch (state.getValue(FACING)) {
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
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
