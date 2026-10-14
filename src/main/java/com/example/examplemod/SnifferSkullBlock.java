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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A decorative sniffer head with four placement directions and a matching outline. */
public class SnifferSkullBlock extends HorizontalDirectionalBlock {
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

    public SnifferSkullBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // The face of the skull points toward the player placing it.
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
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
