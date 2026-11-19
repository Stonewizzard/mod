package com.example.examplemod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.neoforge.registries.*;

public final class AncientAnvilContent {
    public static final DeferredItem<Item> SCRAP_ITEM=ExampleMod.ITEMS.registerSimpleItem("ancient_scrap");
    public static final DeferredItem<Item> CRYSTAL=ExampleMod.ITEMS.registerSimpleItem("ancient_crystal",p->p.rarity(Rarity.RARE));
    public static final DeferredItem<Item> ENERGIZED_CRYSTAL=ExampleMod.ITEMS.registerSimpleItem("energized_ancient_crystal",p->p.rarity(Rarity.RARE));
    public static final DeferredBlock<Block> CRYSTAL_BLOCK=ExampleMod.BLOCKS.registerBlock("ancient_crystal_block",Block::new,p->p.strength(5,6).requiresCorrectToolForDrops().sound(SoundType.AMETHYST).lightLevel(s->7));
    public static final DeferredBlock<Block> ENERGIZED_CRYSTAL_BLOCK=ExampleMod.BLOCKS.registerBlock("energized_ancient_crystal_block",Block::new,p->p.strength(5,6).requiresCorrectToolForDrops().sound(SoundType.AMETHYST).lightLevel(s->12));
    public static final DeferredItem<BlockItem> ENERGIZED_CRYSTAL_BLOCK_ITEM=ExampleMod.ITEMS.registerSimpleBlockItem("energized_ancient_crystal_block",ENERGIZED_CRYSTAL_BLOCK,p->p.rarity(Rarity.RARE));
    public static final DeferredItem<BlockItem> CRYSTAL_BLOCK_ITEM=ExampleMod.ITEMS.registerSimpleBlockItem("ancient_crystal_block",CRYSTAL_BLOCK,p->p.rarity(Rarity.RARE));
    public static final DeferredItem<Item> INGOT=ExampleMod.ITEMS.registerSimpleItem("ancient_ingot",p->p.rarity(Rarity.UNCOMMON));
    public static final DeferredBlock<Block> METAL=ExampleMod.BLOCKS.registerBlock("ancient_metal_block",Block::new,p->p.strength(5,6).sound(SoundType.METAL));
    public static final DeferredItem<BlockItem> METAL_ITEM=ExampleMod.ITEMS.registerSimpleBlockItem("ancient_metal_block",METAL);
    public static final DeferredBlock<AncientAnvilBlock> ANVIL=ExampleMod.BLOCKS.registerBlock("ancient_anvil",AncientAnvilBlock::new,p->p.strength(5,1200).sound(SoundType.ANVIL).pushReaction(net.minecraft.world.level.block.Blocks.OBSIDIAN.defaultBlockState().getPistonPushReaction()).noOcclusion().lightLevel(s->s.getValue(AncientAnvilBlock.DESIGN)!=AncientAnvilDesign.BASE?12:s.getValue(AncientAnvilBlock.TIER)==1?7:3));
    public static final DeferredBlock<AncientAnvilPartBlock> PART=ExampleMod.BLOCKS.registerBlock("ancient_anvil_part",AncientAnvilPartBlock::new,p->p.strength(5,1200).sound(SoundType.ANVIL).pushReaction(net.minecraft.world.level.block.Blocks.OBSIDIAN.defaultBlockState().getPistonPushReaction()).noOcclusion());
    public static final DeferredItem<BlockItem> ANVIL_ITEM=ExampleMod.ITEMS.registerSimpleBlockItem("ancient_anvil",ANVIL);
    public static final DeferredBlock<Block> RUNESTONE=ExampleMod.BLOCKS.registerBlock("runestone",Block::new,p->p.strength(3.5F,6).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE));
    public static final DeferredBlock<Block> CANOPY_KEYSTONE=ExampleMod.BLOCKS.registerBlock("canopy_keystone",Block::new,p->p.strength(4,12).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE).lightLevel(s->12));
    public static final DeferredItem<BlockItem> CANOPY_KEYSTONE_ITEM=ExampleMod.ITEMS.registerSimpleBlockItem("canopy_keystone",CANOPY_KEYSTONE);
    public static final DeferredItem<BlockItem> RUNESTONE_ITEM=ExampleMod.ITEMS.registerSimpleBlockItem("runestone",RUNESTONE);
    public static final DeferredItem<ForgeHammerItem> IRON_FORGE_HAMMER=ExampleMod.ITEMS.registerItem("iron_forge_hammer",p->new ForgeHammerItem(p,1,1),p->p.stacksTo(1).durability(250).repairable(Items.IRON_INGOT));
    public static final java.util.Map<AncientAnvilDesign,DeferredItem<AncientAnvilDesignItem>> SAMPLES=new java.util.EnumMap<>(AncientAnvilDesign.class);
    static {for(var design:AncientAnvilDesign.values())if(design!=AncientAnvilDesign.BASE)SAMPLES.put(design,ExampleMod.ITEMS.registerItem("ancient_anvil_"+design.getSerializedName(),p->new AncientAnvilDesignItem(p,design),p->p));}
    // Keep old sample IDs registered for saved stacks/placed blocks, but the
    // selected Foundry design is crafted and recovered as the normal anvil.
    public static Item item(AncientAnvilDesign design){return design==AncientAnvilDesign.BASE||design==AncientAnvilDesign.FOUNDRY?ANVIL_ITEM.get():SAMPLES.get(design).get();}
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<AncientAnvilBlockEntity>> ENTITY=ExampleMod.BLOCK_ENTITIES.register("ancient_anvil",()->new BlockEntityType<>(AncientAnvilBlockEntity::new,ANVIL.get()));
    public static final TagKey<Block> FOUNDATION=TagKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ancient_anvil_foundation"));
    public static final TagKey<Item> SCRAP=TagKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ancient_alloy_scrap"));
    public static void register(){}
    private AncientAnvilContent(){}
}
