package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class AscenderBlock extends BaseEntityBlock {
    public AscenderBlock(BlockBehaviour.Properties properties){super(properties);}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(player instanceof ServerPlayer server&&level.getBlockEntity(pos) instanceof AscenderBlockEntity ascender)server.openMenu(ascender);
        return InteractionResult.SUCCESS;
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new AscenderBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){
        return level instanceof net.minecraft.server.level.ServerLevel?createTickerHelper(type,AscenderContent.ENTITY.get(),AscenderBlockEntity::tick):null;
    }
}
