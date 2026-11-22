package com.example.examplemod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;

public final class InvocationSealItem extends Item {
  public InvocationSealItem(Properties p) {
    super(p);
  }

  @Override
  public InteractionResult useOn(UseOnContext c) {
    var p = c.getPlayer();
    if (p == null || p.isSpectator()) return InteractionResult.PASS;
    var core = AnvilWork.core(c.getLevel(), c.getClickedPos());
    if (core == null) return InteractionResult.PASS;
    if (!(c.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
    var be = (AncientAnvilBlockEntity) level.getBlockEntity(core);
    AncientAnvilBlockEntity.refresh(level, core, be.getBlockState());
    if (ForgeRunes.counts(be).getOrDefault(ForgeRune.INVOCATION, 0) == 0) {
      AnvilWork.message(p, "Requires an active Tier 4 Rune of Invocation");
      return InteractionResult.SUCCESS;
    }
    if (!level
        .getEntitiesOfClass(Forgemaster.class, new net.minecraft.world.phys.AABB(core).inflate(64))
        .isEmpty()) {
      AnvilWork.message(p, "A Forgemaster is already nearby");
      return InteractionResult.SUCCESS;
    }
    var front = be.getBlockState().getValue(AncientAnvilBlock.FACING);
    var at = core.relative(front, 7).above();
    if (!level.hasChunkAt(at) || !level.mayInteract(p, at)) {
      return InteractionResult.SUCCESS;
    }
    var boss = ForgeRunes.FORGEMASTER.get().create(level, EntitySpawnReason.TRIGGERED);
    if (boss == null) return InteractionResult.SUCCESS;
    boss.snapTo(at.getX() + .5, at.getY(), at.getZ() + .5, 0, 0);
    if (!level.noCollision(boss)) {
      AnvilWork.message(p, "Clear a 2 x 3 space seven blocks in front of the anvil");
      return InteractionResult.SUCCESS;
    }
    if (level.addFreshEntity(boss)) {
      if (!p.getAbilities().instabuild) c.getItemInHand().shrink(1);
      boss.setTarget(p);
    }
    return InteractionResult.SUCCESS;
  }
}
