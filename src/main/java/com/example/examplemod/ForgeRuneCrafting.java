package com.example.examplemod;

import java.util.*;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;

/** Single coordinator per anvil: fair batching, output collection and XP-only restoration. */
public final class ForgeRuneCrafting {
  public static List<ForgeRuneBlockEntity> expansions(AncientAnvilBlockEntity be) {
    return ForgeRunes.members(be).stream()
        .filter(r -> r.kind() == ForgeRune.EXPANSION)
        .sorted(
            Comparator.comparingInt(
                    (ForgeRuneBlockEntity r) -> r.getBlockPos().getY() - be.getBlockPos().getY())
                .thenComparingInt(r -> r.getBlockPos().getX() - be.getBlockPos().getX())
                .thenComparingInt(r -> r.getBlockPos().getZ() - be.getBlockPos().getZ()))
        .toList();
  }

  public static void tick(AncientAnvilBlockEntity be) {
    var members = ForgeRunes.members(be);
    var counts = ForgeRunes.counts(be);
    if (!be.output().isEmpty())
      for (var rune : members)
        if (rune.kind() == ForgeRune.COLLECTION) {
          var stack = be.output();
          var dest = be.getLevel().getBlockEntity(rune.getBlockPos().above());
          if (dest instanceof Container chest) {
            var left = moveInto(chest, stack);
            int n = stack.getCount() - left.getCount();
            if (n > 0) {
              be.takeOutputAmount(n);
              break;
            }
          } else {
            var left = rune.insert(stack);
            int n = stack.getCount() - left.getCount();
            if (n > 0) {
              be.takeOutputAmount(n);
              break;
            }
          }
        }
    if (!be.output().isEmpty()) return;
    int restore = counts.getOrDefault(ForgeRune.RESTORATION, 0);
    var center = be.input(AnvilSlot.CENTER);
    if (restore > 0
        && be.inputCount() == 1
        && center.isDamageableItem()
        && center.getDamageValue() > 0
        && expansions(be).stream().allMatch(r -> r.getItem(0).isEmpty())) {
      int n = Math.min(center.getDamageValue(), Math.min(restore, be.storedExperience()));
      if (n > 0) {
        be.extractExperience(n, false);
        center.setDamageValue(center.getDamageValue() - n);
        be.sync();
      }
      return;
    }
    if (be.inputCount() != 0 || expansions(be).stream().anyMatch(r -> !r.getItem(0).isEmpty()))
      return;
    var batch =
        members.stream().filter(r -> r.kind() == ForgeRune.BATCHING && r.recipe() != null).toList();
    if (batch.isEmpty()) return;
    for (int step = 0; step < batch.size(); step++) {
      int index = Math.floorMod(be.batchCursor + step, batch.size());
      var r = batch.get(index).recipe();
      if (r.anvilTier() > be.getBlockState().getValue(AncientAnvilBlock.TIER)
          || !r.fluidsMatch(be)
          || !r.runesMatch(be)) continue;
      var needs = new ArrayList<AnvilRecipes.Input>(r.inputs().values());
      needs.addAll(r.extras());
      var reservations = new ArrayList<int[]>();
      var used = new HashMap<String, Integer>();
      boolean ready = true;
      for (var need : needs) {
        boolean found = false;
        for (int b = 0; b < batch.size() && !found; b++)
          if (batch.get(b).recipeId.equals(r.id().toString()))
            for (int slot = 0; slot < 27; slot++) {
              var item = batch.get(b).getItem(slot);
              var key = b + ":" + slot;
              int reserved = used.getOrDefault(key, 0);
              if (item.getCount() > reserved && need.accepts(item.copyWithCount(1))) {
                reservations.add(new int[] {b, slot});
                used.put(key, reserved + 1);
                found = true;
                break;
              }
            }
        if (!found) {
          ready = false;
          break;
        }
      }
      if (!ready) continue;
      var sockets = expansions(be);
      if (sockets.size() < r.extras().size()) continue;
      var taken = new ArrayList<ItemStack>();
      for (var res : reservations) taken.add(batch.get(res[0]).removeItem(res[1], 1));
      int n = 0;
      for (var slot : r.inputs().keySet()) be.placeInput(slot, taken.get(n++));
      for (int e = 0; e < r.extras().size(); e++) sockets.get(e).setItem(0, taken.get(n++));
      be.batchCursor = (index + 1) % batch.size();
      be.setChanged();
      return;
    }
  }

  public static ItemStack moveInto(Container chest, ItemStack offered) {
    var left = offered.copy();
    for (int i = 0; i < chest.getContainerSize() && !left.isEmpty(); i++) {
      if (!chest.canPlaceItem(i, left)) continue;
      var old = chest.getItem(i);
      if (!old.isEmpty() && !ItemStack.isSameItemSameComponents(old, left)) continue;
      int n =
          Math.min(
              left.getCount(),
              Math.min(chest.getMaxStackSize(left), left.getMaxStackSize()) - old.getCount());
      if (n <= 0) continue;
      chest.setItem(i, left.copyWithCount(old.getCount() + n));
      left.shrink(n);
    }
    chest.setChanged();
    return left;
  }

  private ForgeRuneCrafting() {}
}
