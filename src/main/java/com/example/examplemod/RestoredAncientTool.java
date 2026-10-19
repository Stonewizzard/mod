package com.example.examplemod;
import java.util.function.Consumer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
/** Tool behavior stays in native item components; only durability expenditure is intercepted. */
public class RestoredAncientTool extends Item {
    public RestoredAncientTool(Properties properties){super(properties);}
    @Override public <T extends LivingEntity> int damageItem(ItemStack stack,int amount,T entity,Consumer<ItemStack> broken){return SharedToolEffects.damage(stack,amount,entity);}
}
