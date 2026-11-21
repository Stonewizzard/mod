package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;

/**
 * End-to-end server interactions on a real Tier 6 workshop. Each path consumes real inputs and XP.
 */
public final class ForgeRuneIntegrationTests {
  public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent e) {
    var env =
        e.registerEnvironment(Identifier.parse("examplemod:forge_rune_integration_environment"));
    e.registerTest(
        Identifier.parse("examplemod:forge_rune_integration"),
        new Test(
            new TestData<>(
                env,
                Level.OVERWORLD,
                Identifier.parse("examplemod:empty_drop_test"),
                300,
                0,
                true,
                Rotation.NONE,
                false,
                1,
                1,
                false,
                0)));
  }

  private static class Test extends GameTestInstance {
    Test(TestData<Holder<TestEnvironmentDefinition<?>>> d) {
      super(d);
    }

    public MapCodec<? extends GameTestInstance> codec() {
      return MapCodec.unit(this);
    }

    protected MutableComponent typeDescription() {
      return Component.literal(
          "Full forge rune crafts, modifiers, automation, repair and relocation");
    }

    public void run(GameTestHelper h) {
      var l = h.getLevel();
      var core = h.absolutePos(new BlockPos(20, 8, 20));
      AnvilTierSixTests.prepare(l, core, Direction.NORTH, AnvilTierSix.stainedGlass("blue"), true);
      var be = (AncientAnvilBlockEntity) l.getBlockEntity(core);
      var player = h.makeMockServerPlayerInLevel();
      player.setGameMode(GameType.SURVIVAL);
      player.snapTo(core.getX() + 7, core.getY() + 2, core.getZ() + 7);
      check(h, be.getBlockState().getValue(AncientAnvilBlock.TIER) == 6, "Fixture reaches Tier 6");
      // Every accepted rune ingredient layout must actually craft and charge exactly its configured
      // XP.
      for (var kind : ForgeRune.values()) {
        var recipe =
            AnvilRecipes.all().stream()
                .filter(r -> r.resultItem() == ForgeRunes.ITEMS.get(kind).get())
                .findFirst()
                .orElseThrow();
        for (var e : recipe.inputs().entrySet())
          be.placeInput(
              e.getKey(), new ItemStack(BuiltInRegistries.ITEM.getValue(e.getValue().item())));
        player.setItemInHand(
            InteractionHand.MAIN_HAND, new ItemStack(AncientAnvilContent.IRON_FORGE_HAMMER.get()));
        be.extractExperience(Integer.MAX_VALUE, false);
        be.insertExperience(50000, false);
        int before = be.storedExperience();
        for (int i = 0; i < recipe.work(); i++) {
          be.lastStrike = -100;
          check(
              h,
              AnvilWork.strike(be, player, InteractionHand.MAIN_HAND),
              "Actual paid hammer hit for " + kind);
        }
        check(
            h,
            be.output().is(ForgeRunes.ITEMS.get(kind).get())
                && be.output().getCount() == 1
                && be.inputCount() == 0,
            "Exact reviewed output for " + kind);
        check(
            h,
            before - be.storedExperience() == recipe.xpPerHit() * recipe.work(),
            "Exact base price for " + kind);
        while (!be.output().isEmpty()) be.takeOutput();
      }
      var blank =
          AnvilRecipes.all().stream()
              .filter(r -> r.resultItem() == AncientAnvilContent.RUNESTONE_ITEM.get())
              .findFirst()
              .orElseThrow();
      var sockets = ForgeRunes.sockets(core, Direction.NORTH, 6).stream().toList();
      set(l, sockets.get(0), ForgeRune.SWIFT_FORGING);
      set(l, sockets.get(1), ForgeRune.EFFICIENCY);
      set(l, sockets.get(2), ForgeRune.DELIBERATION);
      load(be, blank);
      be.lastStrike = -100;
      int bank = be.storedExperience();
      check(
          h,
          AnvilWork.strike(be, player, InteractionHand.MAIN_HAND) && be.work() == 1,
          "Modified craft starts");
      check(
          h,
          bank - be.storedExperience() == 10,
          "Fractional discounted hit rounds only the cumulative debit up");
      var saved = be.saveWithFullMetadata(l.registryAccess());
      var copy =
          (AncientAnvilBlockEntity)
              BlockEntity.loadStatic(core, be.getBlockState(), saved, l.registryAccess());
      check(
          h,
          copy.xpCredit == be.xpCredit && copy.work() == 1,
          "Fractional XP credit and work persist");
      clearInputs(be);
      clear(l, sockets);
      // Zero wear is real, not merely a tooltip number.
      for (int i = 0; i < 100; i++) set(l, sockets.get(i), ForgeRune.TEMPERING);
      load(be, blank);
      be.lastStrike = -100;
      player.setItemInHand(
          InteractionHand.MAIN_HAND, new ItemStack(AncientAnvilContent.IRON_FORGE_HAMMER.get()));
      check(
          h,
          AnvilWork.strike(be, player, InteractionHand.MAIN_HAND)
              && player.getMainHandItem().getDamageValue() == 0,
          "100 Tempering runes eliminate wear");
      clearInputs(be);
      clear(l, sockets);
      set(l, sockets.get(0), ForgeRune.RESTORATION);
      set(l, sockets.get(1), ForgeRune.RESTORATION);
      var damaged = new ItemStack(Items.DIAMOND_PICKAXE);
      damaged.setDamageValue(10);
      damaged.set(
          net.minecraft.core.component.DataComponents.CUSTOM_NAME,
          Component.literal("Keep my components"));
      be.placeInput(AnvilSlot.CENTER, damaged);
      bank = be.storedExperience();
      ForgeRuneCrafting.tick(be);
      check(
          h,
          be.input(AnvilSlot.CENTER).getDamageValue() == 8
              && be.storedExperience() == bank - 2
              && be.input(AnvilSlot.CENTER).getHoverName().getString().equals("Keep my components"),
          "Restoration repairs any durable item, bank-only, preserves components");
      clearInputs(be);
      clear(l, sockets);
      // Expansion consumes only its own declared extra socket; misplaced extra inputs prevent
      // matching.
      set(l, sockets.get(0), ForgeRune.EXPANSION);
      var expansion = (ForgeRuneBlockEntity) l.getBlockEntity(sockets.get(0));
      ForgeRunes.members(be);
      expansion.setItem(0, new ItemStack(Items.DIAMOND));
      load(be, blank);
      check(
          h,
          AnvilRecipes.match(be) == null,
          "Stray expanded input cannot be silently consumed by ordinary recipes");
      clearInputs(be);
      expansion.removeItem(0, 1);
      var extraRecipe =
          new AnvilRecipes.Recipe(
              Identifier.parse("examplemod:integration_extra"),
              3,
              1,
              1,
              blank.inputs(),
              Items.DIAMOND,
              1,
              Map.of(),
              1,
              "testing",
              null,
              List.of(new AnvilRecipes.Input(Identifier.parse("minecraft:diamond"), null)),
              Items.AIR,
              1,
              false);
      load(be, blank);
      check(h, !extraRecipe.matches(be), "Expansion recipe rejects missing socket ingredient");
      expansion.setItem(0, new ItemStack(Items.DIAMOND));
      check(h, extraRecipe.matches(be), "Expansion adds a real ingredient beyond thirteen");
      be.finish(extraRecipe);
      check(
          h,
          expansion.getItem(0).isEmpty() && be.takeOutput().is(Items.DIAMOND),
          "Expanded workpiece consumed exactly once");
      clear(l, sockets);
      // Multiple same-template buffers cooperate without duplicating one source item.
      set(l, sockets.get(0), ForgeRune.BATCHING);
      set(l, sockets.get(1), ForgeRune.BATCHING);
      set(l, sockets.get(2), ForgeRune.COLLECTION);
      ForgeRunes.members(be);
      var b1 = (ForgeRuneBlockEntity) l.getBlockEntity(sockets.get(0));
      var b2 = (ForgeRuneBlockEntity) l.getBlockEntity(sockets.get(1));
      b1.recipeId = blank.id().toString();
      b2.recipeId = blank.id().toString();
      b1.setItem(0, new ItemStack(Items.DEEPSLATE, 3));
      b2.setItem(0, new ItemStack(Items.DEEPSLATE));
      b2.setItem(1, new ItemStack(Items.GOLD_INGOT));
      ForgeRuneCrafting.tick(be);
      check(
          h,
          be.inputCount() == 5 && b1.isEmpty() && b2.isEmpty() && be.work() == 0,
          "Multiple batch runes stage one craft and do not auto-hammer");
      be.finish(blank);
      ForgeRuneCrafting.tick(be);
      var collection = (ForgeRuneBlockEntity) l.getBlockEntity(sockets.get(2));
      check(
          h,
          be.output().isEmpty() && collection.getItem(0).getCount() == 4,
          "Collection buffers finished output for pipes");
      collection.clearContent();
      clear(l, sockets);
      // More than 100 mastery runes guarantees whole extra batches with no arbitrary count cap.
      for (int i = 0; i < 100; i++) set(l, sockets.get(i), ForgeRune.MASTER_CRAFTSMANSHIP);
      var bonusRecipe =
          new AnvilRecipes.Recipe(
              Identifier.parse("examplemod:integration_bonus"),
              6,
              1,
              1,
              blank.inputs(),
              Items.DIAMOND,
              64,
              Map.of(),
              1,
              "testing",
              null,
              List.of(),
              Items.AIR,
              1,
              true);
      load(be, blank);
      be.finish(bonusRecipe);
      check(
          h,
          be.takeOutput().getCount() == 64
              && be.takeOutput().getCount() == 64
              && be.output().isEmpty(),
          "Guaranteed mastery overflow retained as two collectible stacks");
      clear(l, sockets);
      // Same and changed recipe streaks persist and reset independently.
      load(be, blank);
      be.finish(blank);
      be.takeOutput();
      int streak = be.familiarStreak;
      load(be, blank);
      be.finish(blank);
      be.takeOutput();
      check(
          h,
          be.familiarStreak == streak + 1 && be.alternateStreak == 0,
          "Familiarity increments; repeated craft resets Alternation");
      set(l, sockets.get(0), ForgeRune.SPECIALIZATION);
      ForgeRunes.members(be);
      var specialized = (ForgeRuneBlockEntity) l.getBlockEntity(sockets.get(0));
      specialized.category = blank.category();
      check(
          h,
          AnvilWork.price(be, blank) < blank.xpPerHit(),
          "Specialization discounts selected category");
      specialized.category = "other";
      check(
          h,
          AnvilWork.price(be, blank) > blank.xpPerHit(),
          "Specialization penalizes other categories");
      clear(l, sockets);
      set(l, sockets.get(0), ForgeRune.FAMILIARITY);
      be.lastCompleted = blank.id().toString();
      be.familiarStreak = 20;
      check(
          h,
          AnvilWork.price(be, blank) < blank.xpPerHit(),
          "Familiarity discounts actual matching recipe");
      be.lastCompleted = "other";
      check(
          h,
          AnvilWork.price(be, blank) == blank.xpPerHit(),
          "Switching recipe loses Familiarity discount");
      clear(l, sockets);
      set(l, sockets.get(0), ForgeRune.ALTERNATION);
      be.alternateStreak = 20;
      check(
          h,
          AnvilWork.price(be, blank) < blank.xpPerHit(),
          "Alternation discounts a changed recipe");
      be.lastCompleted = blank.id().toString();
      check(
          h,
          AnvilWork.price(be, blank) == blank.xpPerHit(),
          "Repeating removes Alternation discount");
      clear(l, sockets);
      var trans =
          AnvilRecipes.all().stream()
              .filter(r -> r.id().getPath().equals("transmute_end"))
              .findFirst()
              .orElseThrow();
      load(be, trans);
      check(h, !trans.matches(be), "Domain conversion requires its selected rune");
      set(l, sockets.get(0), ForgeRune.END_TRANSMUTATION);
      check(h, trans.matches(be), "End rune enables the End conversion");
      for (int i = 1; i <= 100; i++) set(l, sockets.get(i), ForgeRune.MUTATION);
      int mutations = 0;
      for (int i = 0; i < 40; i++) {
        load(be, trans);
        be.finish(trans);
        if (be.takeOutput().is(Items.CHORUS_FRUIT)) mutations++;
      }
      check(
          h,
          mutations > 0 && mutations < 40,
          "Mutation replaces supported conversion results rather than doubling them");
      clearInputs(be);
      clear(l, sockets);
      set(l, sockets.get(0), ForgeRune.INVOCATION);
      ForgeRunes.members(be);
      player.setShiftKeyDown(false);
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ForgeRunes.SEAL.get(), 2));
      var summonHit = new BlockHitResult(Vec3.atCenterOf(core), Direction.UP, core, false);
      player
          .getMainHandItem()
          .useOn(
              new UseOnContext(
                  l, player, InteractionHand.MAIN_HAND, player.getMainHandItem(), summonHit));
      var bosses = l.getEntitiesOfClass(Forgemaster.class, new AABB(core).inflate(64));
      check(
          h,
          bosses.size() == 1 && player.getMainHandItem().getCount() == 1,
          "Invocation consumes one seal and summons one real Forgemaster");
      player
          .getMainHandItem()
          .useOn(
              new UseOnContext(
                  l, player, InteractionHand.MAIN_HAND, player.getMainHandItem(), summonHit));
      check(
          h,
          player.getMainHandItem().getCount() == 1,
          "An existing encounter does not consume a second seal");
      for (var boss : bosses) boss.discard();
      clear(l, sockets);
      // All relocation data belongs to a single unstackable item; blocked placement is lossless.
      set(l, core.below(), ForgeRune.GRAND_PATTERN);
      ForgeRunes.members(be);
      var grand = (ForgeRuneBlockEntity) l.getBlockEntity(core.below());
      be.insertExperience(100, false);
      int xp = be.storedExperience();
      be.placeInput(AnvilSlot.CENTER, new ItemStack(Items.DIAMOND));
      player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
      player.setShiftKeyDown(true);
      GrandPatternItem.pack(grand, player, InteractionHand.MAIN_HAND);
      check(
          h,
          player.getMainHandItem().is(ForgeRunes.PATTERN.get()) && l.getBlockState(core).isAir(),
          "Grand Pattern packs the real workshop once");
      var hit =
          new BlockHitResult(Vec3.atCenterOf(core.below()), Direction.UP, core.below(), false);
      var context =
          new UseOnContext(l, player, InteractionHand.MAIN_HAND, player.getMainHandItem(), hit);
      player.getMainHandItem().useOn(context);
      check(
          h,
          player.getMainHandItem().isEmpty()
              && l.getBlockEntity(core) instanceof AncientAnvilBlockEntity,
          "Grand Pattern restores and consumes the packed item");
      var restored = (AncientAnvilBlockEntity) l.getBlockEntity(core);
      check(
          h,
          restored.storedExperience() == xp && restored.input(AnvilSlot.CENTER).is(Items.DIAMOND),
          "Relocation preserves XP and inputs");
      AncientAnvilBlockEntity.refresh(l, core, restored.getBlockState());
      check(
          h,
          restored.getBlockState().getValue(AncientAnvilBlock.TIER) == 6,
          "Relocated fluids, caps, keystones and real crystals restore Tier 6");
      System.out.println(
          "FORGE_RUNE_INTEGRATION_CHECK: PASS: all 22 real ingredient crafts and XP prices,"
              + " modifiers, zero wear, fractional persistence, restoration/components, expanded"
              + " ingredients, multiple batching, collection, mastery overflow, sequence history,"
              + " whole-workshop XP/input/fluid/crystal relocation");
      h.succeed();
    }
  }

  static void set(ServerLevel l, BlockPos p, ForgeRune kind) {
    l.setBlock(p, ForgeRunes.BLOCKS.get(kind).get().defaultBlockState(), 3);
  }

  static void clear(ServerLevel l, List<BlockPos> sockets) {
    for (var p : sockets)
      if (l.getBlockState(p).getBlock() instanceof ForgeRuneBlock)
        l.setBlock(p, AncientAnvilContent.RUNESTONE.get().defaultBlockState(), 3);
  }

  static void load(AncientAnvilBlockEntity be, AnvilRecipes.Recipe r) {
    for (var e : r.inputs().entrySet())
      be.placeInput(
          e.getKey(),
          new ItemStack(
              e.getValue().item() != null
                  ? BuiltInRegistries.ITEM.getValue(e.getValue().item())
                  : Items.DEEPSLATE));
  }

  static void clearInputs(AncientAnvilBlockEntity be) {
    for (var slot : AnvilSlot.values()) be.takeInput(slot);
    while (!be.output().isEmpty()) be.takeOutput();
  }

  static void check(GameTestHelper h, boolean ok, String why) {
    if (!ok) throw h.assertionException(why);
  }
}
