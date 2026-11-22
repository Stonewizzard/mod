package com.example.examplemod;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;

public final class ForgeRuneBlock extends BaseEntityBlock {
  public final ForgeRune kind;

  public ForgeRuneBlock(ForgeRune kind, BlockBehaviour.Properties p) {
    super(p);
    this.kind = kind;
  }

  @Override
  protected RenderShape getRenderShape(BlockState s) {
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos p, BlockState s) {
    return new ForgeRuneBlockEntity(p, s);
  }

  @Override
  protected InteractionResult useItemOn(
      ItemStack stack,
      BlockState s,
      Level l,
      BlockPos p,
      Player player,
      InteractionHand hand,
      BlockHitResult hit) {
    return interact(l, p, player, hand);
  }

  @Override
  protected InteractionResult useWithoutItem(
      BlockState s, Level l, BlockPos p, Player player, BlockHitResult hit) {
    return interact(l, p, player, InteractionHand.MAIN_HAND);
  }

  private InteractionResult interact(Level l, BlockPos p, Player player, InteractionHand hand) {
    if (l.isClientSide()) return InteractionResult.SUCCESS;
    if (l.getBlockEntity(p) instanceof ForgeRuneBlockEntity rune && !player.isSpectator())
      rune.interact(player, hand);
    return InteractionResult.SUCCESS;
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      Level l, BlockState s, BlockEntityType<T> type) {
    return l instanceof ServerLevel
        ? createTickerHelper(type, ForgeRunes.ENTITY.get(), ForgeRuneBlockEntity::tick)
        : null;
  }
}
