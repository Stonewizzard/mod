package com.example.examplemod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
/** Common-side use is inert; the client interaction listener opens the read-only book. */
public final class FieldGuideItem extends Item {
    public FieldGuideItem(Properties properties){super(properties);}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){return InteractionResult.SUCCESS;}
    @Override public Component getName(ItemStack stack){return Component.translatable("item.examplemod.ancient_field_guide");}
}
