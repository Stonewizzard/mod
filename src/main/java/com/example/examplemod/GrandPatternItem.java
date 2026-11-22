package com.example.examplemod;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Move actual blocks, never a duplicating blueprint. Preflight everything before touching the
 * world.
 */
public final class GrandPatternItem extends Item {
  public GrandPatternItem(Properties p) {
    super(p);
  }

  public static void pack(
      ForgeRuneBlockEntity rune, net.minecraft.world.entity.player.Player p, InteractionHand hand) {
    var be = rune.core();
    if (be == null
        || !(p instanceof ServerPlayer player)
        || !rune.getBlockPos().equals(be.getBlockPos().below())) {
      AnvilWork.message(p, "Grand Pattern must be directly under the center of a Tier 6 anvil");
      return;
    }
    if (!p.getItemInHand(hand).isEmpty()) {
      AnvilWork.message(p, "Crouch with an empty hand to pack the workshop");
      return;
    }
    var level = player.level();
    var core = be.getBlockPos();
    var front = be.getBlockState().getValue(AncientAnvilBlock.FACING);
    var positions = new LinkedHashSet<BlockPos>();
    positions.addAll(ForgeRunes.sockets(core, front, 6));
    for (var stage : AnvilStages.all())
      for (var r : stage.blocks()) positions.add(r.at(core, front));
    for (var side : AnvilBasins.Side.values())
      positions.addAll(AnvilBasins.pool(core, front, side));
    for (var cell : AncientAnvilStructure.OCCUPIED) positions.add(cell.at(core, front));
    positions.add(core);
    var cells = new ListTag();
    for (var at : positions) {
      if (!level.hasChunkAt(at)
          || !level.getWorldBorder().isWithinBounds(at)
          || !level.mayInteract(player, at)) {
        AnvilWork.message(p, "Workshop crosses unavailable or protected space");
        return;
      }
      var block = level.getBlockState(at);
      var entity = level.getBlockEntity(at);
      if (entity != null
          && !(entity instanceof AncientAnvilBlockEntity)
          && !(entity instanceof ForgeRuneBlockEntity)) {
        AnvilWork.message(p, "Remove containers from required structure positions before packing");
        return;
      }
      if (net.neoforged.neoforge.common.CommonHooks.fireBlockBreak(
              level, player.gameMode.getGameModeForPlayer(), player, at, block)
          .isCanceled()) {
        AnvilWork.message(p, "Packing was blocked by world protection");
        return;
      }
      var c = new CompoundTag();
      c.putInt("X", at.getX() - core.getX());
      c.putInt("Y", at.getY() - core.getY());
      c.putInt("Z", at.getZ() - core.getZ());
      c.put("State", NbtUtils.writeBlockState(block));
      if (entity != null) c.put("Entity", entity.saveWithFullMetadata(level.registryAccess()));
      cells.add(c);
    }
    var crystals = new ListTag();
    for (var r : AnvilTierSix.requirements(AnvilStages.all(), AnvilTierSix.OBSIDIAN)) {
      var crystal = AnvilTierSix.crystal(level, r.at(core, front));
      if (crystal == null) {
        AnvilWork.message(p, "Restore all four crystals before packing");
        return;
      }
      var tag = new CompoundTag();
      tag.putDouble("X", crystal.getX() - core.getX());
      tag.putDouble("Y", crystal.getY() - core.getY());
      tag.putDouble("Z", crystal.getZ() - core.getZ());
      tag.putBoolean("Bottom", crystal.showsBottom());
      crystals.add(tag);
    }
    var data = new CompoundTag();
    data.put("Cells", cells);
    data.put("Crystals", crystals);
    data.putInt("Version", 1);
    // A bounded payload keeps custom modded inventories below network packet limits.
    if (data.toString().length() > 500000) {
      AnvilWork.message(p, "Empty bulky rune inventories before packing");
      return;
    }
    var packed = new ItemStack(ForgeRunes.PATTERN.get());
    packed.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    for (var r : AnvilTierSix.requirements(AnvilStages.all(), AnvilTierSix.OBSIDIAN))
      AnvilTierSix.crystal(level, r.at(core, front)).discard();
    for (var at : positions) level.removeBlockEntity(at);
    for (var at : positions) level.setBlock(at, Blocks.AIR.defaultBlockState(), 2);
    p.setItemInHand(hand, packed);
    AnvilWork.message(p, "Workshop packed: place the pattern to restore its original orientation");
  }

  @Override
  public InteractionResult useOn(UseOnContext c) {
    if (!(c.getLevel() instanceof ServerLevel level)
        || !(c.getPlayer() instanceof ServerPlayer player)) return InteractionResult.SUCCESS;
    var root =
        c.getItemInHand().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    if (root.getIntOr("Version", 0) != 1) return InteractionResult.FAIL;
    // The item places the Grand Pattern rune (one below the anvil core), not the core.
    // BlockPlaceContext also targets clicked plants/snow directly, like a normal block item.
    var origin = new BlockPlaceContext(c).getClickedPos().above();
    var cells = root.getListOrEmpty("Cells");
    if (cells.isEmpty() || cells.size() > 4096) return InteractionResult.FAIL;
    if (!player.getAbilities().mayBuild) return InteractionResult.FAIL;
    var offsets = new HashSet<BlockPos>();
    var plan = new LinkedHashMap<BlockPos, BlockState>();
    var snapshots = new ArrayList<net.neoforged.neoforge.common.util.BlockSnapshot>();
    for (int i = 0; i < cells.size(); i++) {
      var cell = cells.getCompoundOrEmpty(i);
      var delta = new BlockPos(cell.getIntOr("X", 0), cell.getIntOr("Y", 0), cell.getIntOr("Z", 0));
      var at = origin.offset(delta);
      if (!offsets.add(delta)
          || Math.abs(delta.getX()) > 32
          || Math.abs(delta.getZ()) > 32
          || Math.abs(delta.getY()) > 32) {
        AnvilWork.message(player, "This packed workshop has invalid block positions");
        return InteractionResult.FAIL;
      }
      var desired =
          NbtUtils.readBlockState(
              level.registryAccess().lookupOrThrow(Registries.BLOCK),
              cell.getCompoundOrEmpty("State"));
      if (desired.isAir()) continue; // Empty saved cells never erase terrain or require excavation.
      if (!level.hasChunkAt(at)
          || !level.isInWorldBounds(at)
          || !level.getWorldBorder().isWithinBounds(at)
          || !level.mayInteract(player, at)) {
        AnvilWork.message(
            player,
            "Cannot restore at "
                + at.getX()
                + ", "
                + at.getY()
                + ", "
                + at.getZ()
                + ": outside loaded, buildable space");
        return InteractionResult.FAIL;
      }
      var existing = level.getBlockState(at);
      if (level.getBlockEntity(at) != null
          || !existing.canBeReplaced() && !existing.equals(desired)) {
        AnvilWork.message(
            player,
            "Restore blocked by "
                + existing.getBlock().getName().getString()
                + " at "
                + at.getX()
                + ", "
                + at.getY()
                + ", "
                + at.getZ());
        return InteractionResult.FAIL;
      }
      plan.put(at, desired);
    }
    if (plan.isEmpty()) return InteractionResult.FAIL;
    for (var entry : plan.entrySet()) {
      var at = entry.getKey();
      if (level.getBlockState(at).equals(entry.getValue())) continue;
      snapshots.add(
          net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(), level, at));
      level.setBlock(at, entry.getValue(), 2);
    }
    var placeEvent =
        new BlockEvent.EntityMultiPlaceEvent(
            snapshots, level.getBlockState(c.getClickedPos()), player);
    if (NeoForge.EVENT_BUS.post(placeEvent).isCanceled()) {
      for (var snapshot : snapshots) snapshot.restore(2);
      AnvilWork.message(player, "Restoration was blocked by world protection");
      return InteractionResult.FAIL;
    }
    for (int i = 0; i < cells.size(); i++) {
      var cell = cells.getCompoundOrEmpty(i);
      if (!cell.contains("Entity")) continue;
      var at = origin.offset(cell.getIntOr("X", 0), cell.getIntOr("Y", 0), cell.getIntOr("Z", 0));
      var tag = cell.getCompoundOrEmpty("Entity").copy();
      tag.putInt("x", at.getX());
      tag.putInt("y", at.getY());
      tag.putInt("z", at.getZ());
      var entity = BlockEntity.loadStatic(at, level.getBlockState(at), tag, level.registryAccess());
      if (entity != null) level.setBlockEntity(entity);
    }
    var crystals = root.getListOrEmpty("Crystals");
    for (int i = 0; i < crystals.size(); i++) {
      var t = crystals.getCompoundOrEmpty(i);
      var entity = new EndCrystal(EntityTypes.END_CRYSTAL, level);
      entity.snapTo(
          origin.getX() + t.getDoubleOr("X", 0),
          origin.getY() + t.getDoubleOr("Y", 0),
          origin.getZ() + t.getDoubleOr("Z", 0),
          0,
          0);
      entity.setShowBottom(t.getBooleanOr("Bottom", true));
      level.addFreshEntity(entity);
    }
    c.getItemInHand().shrink(1); // Even Creative must move, not copy, stored XP/items/crystals.
    AnvilWork.message(player, "Workshop restored");
    return InteractionResult.SUCCESS;
  }
}
