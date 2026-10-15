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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Placeable egg with vanilla Sniffer Egg hatch timing, with a warped-nylium boost. */
public class CruncherEggBlock extends Block {
    public static final IntegerProperty HATCH = IntegerProperty.create("hatch", 0, 2);
    private static final int REGULAR_STAGE_TICKS = 24000 / 3;
    private static final int BOOSTED_STAGE_TICKS = 12000 / 3;
    private static final int RANDOM_HATCH_OFFSET_TICKS = 300;
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
            level.scheduleTick(pos, this, hatchDelay(level, pos, level.getRandom()));
        }
    }

    // Keep vanilla timing, using warped nylium as this Nether egg's hatch-boost block.
    static int hatchDelay(BlockGetter level, BlockPos pos, RandomSource random) {
        boolean boosted = level.getBlockState(pos.below()).is(Blocks.WARPED_NYLIUM);
        return (boosted ? BOOSTED_STAGE_TICKS : REGULAR_STAGE_TICKS)
                + random.nextInt(RANDOM_HATCH_OFFSET_TICKS);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HATCH) < 2) {
            level.setBlock(pos, state.cycle(HATCH), 3);
            level.scheduleTick(pos, this, hatchDelay(level, pos, random));
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
