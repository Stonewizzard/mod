package com.example.examplemod;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.*;

/** Registration and exact structural membership, shared by effects, automation and relocation. */
public final class ForgeRunes {
  public static final Map<ForgeRune, DeferredBlock<ForgeRuneBlock>> BLOCKS =
      new EnumMap<>(ForgeRune.class);
  public static final Map<ForgeRune, DeferredItem<BlockItem>> ITEMS =
      new EnumMap<>(ForgeRune.class);

  static {
    for (var kind : ForgeRune.values()) {
      var block =
          ExampleMod.BLOCKS.registerBlock(
              kind.id(),
              p -> new ForgeRuneBlock(kind, p),
              p ->
                  p.strength(3.5F, 6)
                      .requiresCorrectToolForDrops()
                      .sound(net.minecraft.world.level.block.SoundType.DEEPSLATE));
      BLOCKS.put(kind, block);
      ITEMS.put(kind, ExampleMod.ITEMS.registerSimpleBlockItem(kind.id(), block));
    }
  }

  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ForgeRuneBlockEntity>>
      ENTITY =
          ExampleMod.BLOCK_ENTITIES.register(
              "forge_rune",
              () ->
                  new BlockEntityType<>(
                      ForgeRuneBlockEntity::new,
                      BLOCKS.values().stream()
                          .map(DeferredBlock::get)
                          .toArray(ForgeRuneBlock[]::new)));
  public static final DeferredItem<Item> CORE =
      ExampleMod.ITEMS.registerSimpleItem("forgemaster_core", p -> p.rarity(Rarity.EPIC));
  public static final DeferredItem<InvocationSealItem> SEAL =
      ExampleMod.ITEMS.registerItem(
          "forgemaster_seal", InvocationSealItem::new, p -> p.rarity(Rarity.RARE));
  public static final DeferredItem<GrandPatternItem> PATTERN =
      ExampleMod.ITEMS.registerItem(
          "packed_grand_pattern", GrandPatternItem::new, p -> p.stacksTo(1).rarity(Rarity.EPIC));
  public static final DeferredHolder<
          net.minecraft.world.entity.EntityType<?>,
          net.minecraft.world.entity.EntityType<Forgemaster>>
      FORGEMASTER =
          ExampleMod.ENTITIES.registerEntityType(
              "forgemaster",
              Forgemaster::new,
              net.minecraft.world.entity.MobCategory.MONSTER,
              b -> b.sized(1.4F, 2.7F).fireImmune().clientTrackingRange(12));

  public static void register() {}

  public static boolean allowedEntity(Level level, BlockPos p) {
    return level.getBlockEntity(p) == null
        || level.getBlockEntity(p) instanceof ForgeRuneBlockEntity;
  }

  public static Set<BlockPos> sockets(BlockPos core, Direction front, int tier) {
    var out = new LinkedHashSet<BlockPos>();
    if (tier >= 1) out.addAll(AncientAnvilStructure.foundation(core, front));
    if (tier >= 2) out.addAll(AnvilBasins.frame(core, front));
    for (var stage : AnvilStages.all())
      if (stage.tier() <= tier)
        for (var r : stage.blocks())
          if ((r.tag().equals(AncientAnvilContent.FOUNDATION)
              || r.tag().location().getPath().equals("ancient_anvil_runes")))
            out.add(r.at(core, front));
    return Collections.unmodifiableSet(out);
  }

  public static List<ForgeRuneBlockEntity> members(AncientAnvilBlockEntity core) {
    if (core.getLevel() == null) return List.of();
    var out = new ArrayList<ForgeRuneBlockEntity>();
    var level = core.getLevel();
    int tier = core.getBlockState().getValue(AncientAnvilBlock.TIER);
    for (var p :
        sockets(core.getBlockPos(), core.getBlockState().getValue(AncientAnvilBlock.FACING), tier))
      if (level.hasChunkAt(p)
          && level.getBlockEntity(p) instanceof ForgeRuneBlockEntity rune
          && rune.kind().tier <= tier) {
        if (rune.owner == null
            || !level.hasChunkAt(rune.owner)
            || !(level.getBlockEntity(rune.owner) instanceof AncientAnvilBlockEntity other)
            || !sockets(
                    other.getBlockPos(),
                    other.getBlockState().getValue(AncientAnvilBlock.FACING),
                    other.getBlockState().getValue(AncientAnvilBlock.TIER))
                .contains(p)) rune.owner = core.getBlockPos();
        if (core.getBlockPos().equals(rune.owner)) out.add(rune);
      }
    return out;
  }

  public static Map<ForgeRune, Integer> counts(AncientAnvilBlockEntity core) {
    var counts = new EnumMap<ForgeRune, Integer>(ForgeRune.class);
    for (var rune : members(core)) counts.merge(rune.kind(), 1, Integer::sum);
    return counts;
  }

  public static void interact(
      net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
    if (!event.getEntity().isShiftKeyDown()
        || event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;
    var state = event.getLevel().getBlockState(event.getPos());
    if (!(state.getBlock() instanceof ForgeRuneBlock block)) return;
    if (block.kind != ForgeRune.BATCHING && block.kind != ForgeRune.SPECIALIZATION) return;
    var held = event.getItemStack();
    // Vanilla skips block interaction when sneaking with a held item. Templates must still reach
    // the rune.
    // Unrecognized BlockItems retain vanilla sneak-placement, so chests can be placed on top.
    if (held.isEmpty()
        || AnvilRecipes.all().stream().anyMatch(r -> r.resultItem() == held.getItem()))
      event.setUseBlock(net.minecraft.util.TriState.TRUE);
  }

  private ForgeRunes() {}
}
