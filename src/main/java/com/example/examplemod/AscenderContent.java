package com.example.examplemod;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class AscenderContent {
    public static final DeferredBlock<AscenderBlock> BLOCK=ExampleMod.BLOCKS.registerBlock("ascender",AscenderBlock::new,p->p.strength(3).sound(SoundType.DEEPSLATE));
    public static final DeferredItem<BlockItem> ITEM=ExampleMod.ITEMS.registerSimpleBlockItem("ascender",BLOCK);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<AscenderBlockEntity>> ENTITY=ExampleMod.BLOCK_ENTITIES.register("ascender",()->new BlockEntityType<>(AscenderBlockEntity::new,BLOCK.get()));
    public static final DeferredHolder<MenuType<?>,MenuType<AscenderMenu>> MENU=ExampleMod.MENUS.register("ascender",()->new MenuType<>(AscenderMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static void register(){}
    private AscenderContent(){}
}
