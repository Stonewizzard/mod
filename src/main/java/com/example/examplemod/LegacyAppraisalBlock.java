package com.example.examplemod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
/** Loaded old tables migrate on the next server block-entity tick. */
public final class LegacyAppraisalBlock extends BaseEntityBlock {
    public LegacyAppraisalBlock(BlockBehaviour.Properties properties){super(properties);}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new LegacyAppraisalBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level instanceof net.minecraft.server.level.ServerLevel?createTickerHelper(type,LegacyAppraisalContent.ENTITY.get(),LegacyAppraisalBlockEntity::tick):null;}
}
