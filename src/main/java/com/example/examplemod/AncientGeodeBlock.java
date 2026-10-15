package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A stepped, dragon-egg-like geode. Crunchers consume it from the side. */
public class AncientGeodeBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(4, 0, 4, 12, 2, 12), Block.box(2, 2, 2, 14, 4, 14),
            Block.box(1, 4, 1, 15, 9, 15), Block.box(2, 9, 2, 14, 12, 14),
            Block.box(4, 12, 4, 12, 15, 12), Block.box(6, 15, 6, 10, 16, 10));

    public AncientGeodeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
