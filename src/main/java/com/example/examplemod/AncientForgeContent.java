package com.example.examplemod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Tier-one registrations. Its core and physical frame are retained when future tiers are introduced. */
public final class AncientForgeContent {
    public static final DeferredBlock<Block> BRICKS=ExampleMod.BLOCKS.registerBlock("ruined_forge_bricks",Block::new,p->p.strength(3.5F,6).sound(SoundType.DEEPSLATE_BRICKS));
    public static final DeferredItem<BlockItem> BRICKS_ITEM=ExampleMod.ITEMS.registerSimpleBlockItem("ruined_forge_bricks",BRICKS);
    public static final DeferredBlock<RuinedAncientForgeBlock> CORE=ExampleMod.BLOCKS.registerBlock("ruined_ancient_forge",RuinedAncientForgeBlock::new,p->p.strength(3.5F,6).sound(SoundType.DEEPSLATE).noOcclusion());
    public static final DeferredItem<BlockItem> CORE_ITEM=ExampleMod.ITEMS.registerSimpleBlockItem("ruined_ancient_forge",CORE);
    public static final DeferredBlock<FormedForgePartBlock> PART=ExampleMod.BLOCKS.registerBlock("formed_ruined_forge",FormedForgePartBlock::new,p->p.strength(3.5F,6).sound(SoundType.DEEPSLATE_BRICKS).noOcclusion());
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>,net.minecraft.world.level.block.entity.BlockEntityType<ForgePartBlockEntity>> PART_ENTITY=ExampleMod.BLOCK_ENTITIES.register("formed_ruined_forge",()->new net.minecraft.world.level.block.entity.BlockEntityType<>(ForgePartBlockEntity::new,PART.get()));
    public static final TagKey<Block> MASONRY=TagKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ruined_forge_masonry"));
    public static void register(){}
    private AncientForgeContent(){}
}
