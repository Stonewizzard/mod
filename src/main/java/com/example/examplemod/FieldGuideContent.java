package com.example.examplemod;
import java.util.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;
public final class FieldGuideContent {
    private static final Map<GuideDesign,DeferredItem<Item>> EDITIONS=new EnumMap<>(GuideDesign.class);
    static {for(var design:GuideDesign.values())EDITIONS.put(design,ExampleMod.ITEMS.registerItem(design.itemId,p->new FieldGuideItem(p,design),p->p.stacksTo(1)));}
    public static final DeferredItem<Item> BOOK=EDITIONS.get(GuideDesign.MANUAL);
    public static DeferredItem<Item> book(GuideDesign design){return EDITIONS.get(design);}
    /** Only the selected Workshop Manual is registered and exposed. */
    public static List<DeferredItem<Item>> finalists(){return List.of(BOOK);}
    public static boolean isGuide(ItemStack stack){return stack.getItem() instanceof FieldGuideItem;}
    public static void register(){}
    private FieldGuideContent(){}
}
