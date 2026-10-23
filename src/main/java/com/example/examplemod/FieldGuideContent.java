package com.example.examplemod;
import java.util.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;
public final class FieldGuideContent {
    private static final Map<GuideDesign,DeferredItem<Item>> EDITIONS=new EnumMap<>(GuideDesign.class);
    static {for(var design:GuideDesign.values())EDITIONS.put(design,ExampleMod.ITEMS.registerItem(design.itemId,p->new FieldGuideItem(p,design),p->p.stacksTo(1)));}
    public static final DeferredItem<Item> BOOK=EDITIONS.get(GuideDesign.JOURNAL);
    public static DeferredItem<Item> book(GuideDesign design){return EDITIONS.get(design);}
    /** Finalist exposure is deliberately separate from registration of the five review prototypes. */
    public static List<DeferredItem<Item>> finalists(){return List.of(book(GuideDesign.JOURNAL),book(GuideDesign.MANUAL),book(GuideDesign.TRAIL));}
    public static boolean isGuide(ItemStack stack){return stack.getItem() instanceof FieldGuideItem;}
    public static void register(){}
    private FieldGuideContent(){}
}
