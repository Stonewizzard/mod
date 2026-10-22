package com.example.examplemod;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;
public final class FieldGuideContent {
    public static final DeferredItem<Item> BOOK=ExampleMod.ITEMS.registerItem("ancient_field_guide",FieldGuideItem::new,p->p.stacksTo(1));
    public static boolean isGuide(ItemStack stack){return stack.getItem() instanceof FieldGuideItem;}
    public static void register(){}
    private FieldGuideContent(){}
}
