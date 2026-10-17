package com.example.examplemod;

import java.util.List;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ReconstructionContent {
    public static final DeferredItem<Item> LEFT=part("pickaxe_left_tip"),BRIDGE=part("pickaxe_head_bridge"),RIGHT=part("pickaxe_right_tip"),CONNECTOR=part("ancient_tool_connector"),HANDLE=part("ancient_tool_handle");
    public static final DeferredItem<Item> SHOVEL_HEAD=part("ancient_shovel_head"),HOE_HEAD=part("ancient_hoe_head"),MACE_HEAD=part("ancient_mace_head"),SPEAR_TIP=part("ancient_spear_tip");
    public static final DeferredBlock<ReconstructionTableBlock> TABLE=ExampleMod.BLOCKS.registerBlock("reconstruction_table",ReconstructionTableBlock::new,p->p.strength(2).sound(SoundType.WOOD).noOcclusion());
    public static final DeferredItem<BlockItem> TABLE_ITEM=ExampleMod.ITEMS.registerSimpleBlockItem("reconstruction_table",TABLE);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ReconstructionTableBlockEntity>> ENTITY=ExampleMod.BLOCK_ENTITIES.register("reconstruction_table",()->new BlockEntityType<>(ReconstructionTableBlockEntity::new,TABLE.get()));
    public static final DeferredHolder<MenuType<?>,MenuType<ReconstructionTableMenu>> MENU=ExampleMod.MENUS.register("reconstruction_table",()->new MenuType<>(ReconstructionTableMenu::new,FeatureFlags.DEFAULT_FLAGS));
    private static DeferredItem<Item> part(String id){return ExampleMod.ITEMS.registerSimpleItem(id,p->p.stacksTo(1).rarity(Rarity.UNCOMMON));}
    public static List<Item> pieces(){return List.of(LEFT.get(),BRIDGE.get(),RIGHT.get(),CONNECTOR.get(),HANDLE.get(),SHOVEL_HEAD.get(),HOE_HEAD.get(),MACE_HEAD.get(),SPEAR_TIP.get());}
    public static Item reward(ArtifactProfile.Find find){return switch(find){case PICKAXE_LEFT->LEFT.get();case PICKAXE_BRIDGE->BRIDGE.get();case PICKAXE_RIGHT->RIGHT.get();case TOOL_CONNECTOR->CONNECTOR.get();case TOOL_HANDLE->HANDLE.get();case SHOVEL_HEAD->SHOVEL_HEAD.get();case HOE_HEAD->HOE_HEAD.get();case MACE_HEAD->MACE_HEAD.get();case SPEAR_TIP->SPEAR_TIP.get();default->throw new IllegalArgumentException("Not a reconstruction piece");};}
    public static void register(){}
}

