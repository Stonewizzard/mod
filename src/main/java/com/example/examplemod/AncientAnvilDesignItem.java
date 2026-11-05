package com.example.examplemod;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.*;

/** Creative-only comparison item; delegates placement without replacing Block.asItem(). */
public final class AncientAnvilDesignItem extends Item {
    private final AncientAnvilDesign design;
    public AncientAnvilDesignItem(Properties properties,AncientAnvilDesign design){super(properties);this.design=design;}
    public AncientAnvilDesign design(){return design;}
    @Override public InteractionResult useOn(UseOnContext context){return AncientAnvilContent.ANVIL_ITEM.get().place(new BlockPlaceContext(context));}
}
