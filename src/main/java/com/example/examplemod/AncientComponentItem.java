package com.example.examplemod;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
/** Influence is visible without adding another line to every component tooltip. */
public final class AncientComponentItem extends Item {
    public AncientComponentItem(Properties properties){super(properties);}
    @Override public Component getName(ItemStack stack){var influence=ComponentInfluences.current(stack);var name=super.getName(stack);return influence==ModifierInfluence.NONE?name:name.copy().withStyle(s->s.withColor(influence.color));}
}
