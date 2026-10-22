package com.example.examplemod;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.*;
/** Hidden registry compatibility only. There is no Appraisal menu, recipe or placeable item. */
public final class LegacyAppraisalContent {
    public static final DeferredBlock<LegacyAppraisalBlock> BLOCK=ExampleMod.BLOCKS.registerBlock("appraisal_table",LegacyAppraisalBlock::new,p->p.strength(2.5F));
    public static final DeferredItem<Item> ITEM=ExampleMod.ITEMS.registerItem("appraisal_table",FieldGuideItem::new,p->p.stacksTo(1));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<LegacyAppraisalBlockEntity>> ENTITY=ExampleMod.BLOCK_ENTITIES.register("appraisal_table",()->new BlockEntityType<>(LegacyAppraisalBlockEntity::new,BLOCK.get()));
    public static void register(){}
    private LegacyAppraisalContent(){}
}
