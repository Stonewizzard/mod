package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Placeable egg: three 10-second stages for the current testing build. */
public class CruncherEggBlock extends Block {
    public static final IntegerProperty HATCH = IntegerProperty.create("hatch", 0, 2);
    public static final int STAGE_TICKS = 200;
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 14, 13);

    public CruncherEggBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HATCH, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        if (!oldState.is(this) && !level.isClientSide()) {
            level.scheduleTick(pos, this, STAGE_TICKS);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HATCH) < 2) {
            level.setBlock(pos, state.cycle(HATCH), 3);
            level.scheduleTick(pos, this, STAGE_TICKS);
            return;
        }
        Cruncher baby = ExampleMod.CRUNCHER.get().create(level, EntitySpawnReason.BREEDING);
        if (baby == null) {
            return;
        }
        baby.setAge(Cruncher.TEST_BABY_AGE);
        baby.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        if (!level.noCollision(baby)) {
            level.scheduleTick(pos, this, 100);
            return;
        }
        if (level.addFreshEntity(baby)) {
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 1.0F, 0.7F);
        }
    }
}
