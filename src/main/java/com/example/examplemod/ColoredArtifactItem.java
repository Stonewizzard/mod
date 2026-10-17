package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Native default component initialization replaces ITEM_NAME; apply the color at name lookup. */
public final class ColoredArtifactItem extends Item {
    private final int nameColor;
    public ColoredArtifactItem(Properties properties,int nameColor){super(properties);this.nameColor=nameColor;}
    @Override public Component getName(ItemStack stack){return super.getName(stack).copy().withStyle(style->style.withColor(nameColor));}
}
