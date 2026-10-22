package com.example.examplemod;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
/** A native item-model tint; no texture replacement or per-frame item mutation. */
public final class ComponentAppearance {
    public static void refresh(ItemStack stack){
        if(!ComponentWork.component(stack))return;var influence=ComponentInfluences.current(stack);
        if(influence==ModifierInfluence.NONE)return;
        var existing=stack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA,CustomModelData.EMPTY);
        int color=influence==ModifierInfluence.NETHER?0xFFC09A:0xC2A0FF;
        var colors=new java.util.ArrayList<>(existing.colors());if(colors.isEmpty())colors.add(color);else colors.set(0,color);
        stack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(existing.floats(),existing.flags(),existing.strings(),List.copyOf(colors)));
    }
    private ComponentAppearance(){}
}
