package com.example.examplemod;
import java.util.function.Consumer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
/** Preserve native mace smash behavior, including its native attack callback. */
public final class RestoredAncientMace extends MaceItem {
    public RestoredAncientMace(Properties properties){super(properties);}
    @Override public <T extends LivingEntity> int damageItem(ItemStack stack,int amount,T entity,Consumer<ItemStack> broken){return SharedToolEffects.damage(stack,amount,entity);}
}
