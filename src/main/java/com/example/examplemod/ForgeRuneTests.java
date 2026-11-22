package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Runs real block entities, datapack recipes and transfer transactions on the game server. */
public final class ForgeRuneTests {
  public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent e) {
    var id = Identifier.parse("examplemod:forge_runes");
    var env = e.registerEnvironment(Identifier.parse("examplemod:forge_runes_environment"));
    e.registerTest(
        id,
        new Test(
            new TestData<>(
                env,
                Level.OVERWORLD,
                Identifier.parse("examplemod:empty_drop_test"),
                100,
                0,
                true,
                Rotation.NONE,
                false,
                1,
                1,
                false,
                0)));
  }

  static final class Test extends GameTestInstance {
    Test(TestData<Holder<TestEnvironmentDefinition<?>>> d) {
      super(d);
    }

    @Override
    public MapCodec<? extends GameTestInstance> codec() {
      return MapCodec.unit(this);
    }

    @Override
    protected MutableComponent typeDescription() {
      return Component.literal(
          "Forge runes: tiered membership, effects, exact recipes and transactional XP");
    }

    @Override
    public void run(GameTestHelper h) {
      var l = h.getLevel();
      var pos = h.absolutePos(new BlockPos(20, 8, 20));
      var front = Direction.NORTH;
      check(h, ForgeRune.values().length == 22, "Exactly 22 agreed forge runes");
      for (var rune : ForgeRune.values()) {
        var recipe =
            AnvilRecipes.all().stream()
                .filter(r -> r.resultItem() == ForgeRunes.ITEMS.get(rune).get())
                .findFirst()
                .orElseThrow();
        check(
            h,
            recipe.anvilTier() == rune.tier
                && recipe.inputs().size()
                    == (rune == ForgeRune.STORAGE
                            || rune == ForgeRune.EXTRACTION
                            || rune == ForgeRune.INPUT
                            || rune == ForgeRune.CONDUIT
                        ? 9
                        : 13),
            "Agreed recipe loads: " + rune);
      }
      check(h, ForgeRuneMath.swings(16, 100, 0) == 1, "Swift stops at one swing");
      check(
          h,
          ForgeRuneMath.hitPrice(10, 16, 1, 100, 0, 0)
              > ForgeRuneMath.hitPrice(10, 16, 1, 15, 0, 0),
          "Surplus Swift raises cost");
      check(
          h,
          ForgeRuneMath.efficiency(50) == .5
              && ForgeRuneMath.efficiency(552) > 0
              && ForgeRuneMath.efficiency(552) < ForgeRuneMath.efficiency(100),
          "Efficiency has uncapped diminishing returns");
      check(h, ForgeRuneMath.wear(100) == 0, "Tempering reaches zero wear");
      for (var p : AncientAnvilStructure.foundation(pos, front))
        l.setBlock(p, AncientAnvilContent.RUNESTONE.get().defaultBlockState(), 3);
      l.setBlock(pos, AncientAnvilContent.ANVIL.get().defaultBlockState(), 3);
      AncientAnvilBlockEntity.refresh(l, pos, l.getBlockState(pos));
      var be = (AncientAnvilBlockEntity) l.getBlockEntity(pos);
      var sockets = ForgeRunes.sockets(pos, front, 1).stream().toList();
      l.setBlock(
          sockets.get(0), ForgeRunes.BLOCKS.get(ForgeRune.STORAGE).get().defaultBlockState(), 3);
      AncientAnvilBlockEntity.refresh(l, pos, l.getBlockState(pos));
      check(
          h,
          be.getBlockState().getValue(AncientAnvilBlock.TIER) == 1,
          "Rune block entity preserves foundation tier");
      check(h, be.experienceCapacity() == 2000, "Storage adds capacity");
      l.setBlock(
          sockets.get(1), ForgeRunes.BLOCKS.get(ForgeRune.EXTRACTION).get().defaultBlockState(), 3);
      l.setBlock(
          sockets.get(2), ForgeRunes.BLOCKS.get(ForgeRune.INPUT).get().defaultBlockState(), 3);
      ForgeRunes.members(be);
      be.insertExperience(100, false);
      var extraction = (ForgeRuneBlockEntity) l.getBlockEntity(sockets.get(1));
      var input = (ForgeRuneBlockEntity) l.getBlockEntity(sockets.get(2));
      var port = new ForgeRuneAutomation.ExperiencePort(extraction);
      var resource = FluidResource.of(ForgeRuneAutomation.XP.get());
      try (var tx = Transaction.openRoot()) {
        check(h, port.extract(0, resource, 30, tx) == 30, "XP port extracts in transaction");
      }
      check(h, be.storedExperience() == 100, "Aborted transaction restores exact XP");
      try (var tx = Transaction.openRoot()) {
        check(h, port.extract(0, resource, 30, tx) == 30, "Abort restores transfer budget too");
        tx.commit();
      }
      check(h, be.storedExperience() == 70, "Committed XP export is exact");
      try (var tx = Transaction.openRoot()) {
        check(
            h, port.extract(0, resource, 30, tx) == 10, "All export shares a per-core rate budget");
        tx.commit();
      }
      try (var tx = Transaction.openRoot()) {
        check(
            h,
            new ForgeRuneAutomation.ExperiencePort(input).insert(0, resource, 20, tx) == 20,
            "Input permits transactional import");
        tx.commit();
      }
      check(h, be.storedExperience() == 80, "Input adds only committed liquid XP");
      var saved = be.saveWithFullMetadata(l.registryAccess());
      var loaded =
          (AncientAnvilBlockEntity)
              net.minecraft.world.level.block.entity.BlockEntity.loadStatic(
                  pos, be.getBlockState(), saved, l.registryAccess());
      check(h, loaded.storedExperience() == 80, "Bank survives save/load");
      l.setBlock(
          sockets.get(3), ForgeRunes.BLOCKS.get(ForgeRune.EFFICIENCY).get().defaultBlockState(), 3);
      check(
          h,
          !ForgeRunes.counts(be).containsKey(ForgeRune.EFFICIENCY),
          "Higher-tier runes remain inactive in a low-tier forge");
      check(
          h,
          ForgeRunes.sockets(pos, front, 6).size() == 552,
          "All 552 structural sockets remain eligible");
      System.out.println(
          "FORGE_RUNES_CHECK: PASS: 22 recipes, foundation replacement, tiers, no count caps,"
              + " swift/efficiency/tempering curves, bank, import/export transaction"
              + " rollback/commit and shared budget");
      h.succeed();
    }
  }

  static void check(GameTestHelper h, boolean ok, String why) {
    if (!ok) throw h.assertionException(why);
  }
}
