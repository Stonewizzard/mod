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
    public static final DeferredItem<Item> INGOT=ExampleMod.ITEMS.registerSimpleItem("ancient_ingot",p->p.rarity(Rarity.UNCOMMON));
    public static final DeferredBlock<Block> METAL=ExampleMod.BLOCKS.registerBlock("ancient_metal_block",Block::new,p->p.strength(5,6).sound(SoundType.METAL));
    public static final DeferredItem<BlockItem> METAL_ITEM=ExampleMod.ITEMS.registerSimpleBlockItem("ancient_metal_block",METAL);
    public static final DeferredBlock<AncientAnvilBlock> ANVIL=ExampleMod.BLOCKS.registerBlock("ancient_anvil",AncientAnvilBlock::new,p->p.strength(5,1200).sound(SoundType.ANVIL).pushReaction(net.minecraft.world.level.block.Blocks.OBSIDIAN.defaultBlockState().getPistonPushReaction()).noOcclusion().lightLevel(s->s.getValue(AncientAnvilBlock.TIER)==1?7:3));
    public static final DeferredBlock<AncientAnvilPartBlock> PART=ExampleMod.BLOCKS.registerBlock("ancient_anvil_part",AncientAnvilPartBlock::new,p->p.strength(5,1200).sound(SoundType.ANVIL).pushReaction(net.minecraft.world.level.block.Blocks.OBSIDIAN.defaultBlockState().getPistonPushReaction()).noOcclusion());
    public static final DeferredItem<BlockItem> ANVIL_ITEM=ExampleMod.ITEMS.registerSimpleBlockItem("ancient_anvil",ANVIL);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<AncientAnvilBlockEntity>> ENTITY=ExampleMod.BLOCK_ENTITIES.register("ancient_anvil",()->new BlockEntityType<>(AncientAnvilBlockEntity::new,ANVIL.get()));
    public static final TagKey<Block> FOUNDATION=TagKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ancient_anvil_foundation"));
    public static final TagKey<Item> SCRAP=TagKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ancient_alloy_scrap"));
    public static void register(){}
    private AncientAnvilContent(){}
}
