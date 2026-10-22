package com.example.examplemod;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.*;
/** A read-only reference station; no component crafting operation is performed here. */
public final class AppraisalContent {
    public static final DeferredBlock<AppraisalTableBlock> BLOCK=ExampleMod.BLOCKS.registerBlock("appraisal_table",AppraisalTableBlock::new,p->p.strength(2.5F).sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> ITEM=ExampleMod.ITEMS.registerSimpleBlockItem("appraisal_table",BLOCK);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<AppraisalTableBlockEntity>> ENTITY=ExampleMod.BLOCK_ENTITIES.register("appraisal_table",()->new BlockEntityType<>(AppraisalTableBlockEntity::new,BLOCK.get()));
    public static final DeferredHolder<MenuType<?>,MenuType<AppraisalMenu>> MENU=ExampleMod.MENUS.register("appraisal_table",()->new MenuType<>(AppraisalMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static void register(){}
    private AppraisalContent(){}
}
