package com.example.examplemod.client;
public final class PickaxeApiProbe {
 public static void run() {
  for(String name:new String[]{"net.neoforged.neoforge.event.level.BlockDropsEvent","net.neoforged.neoforge.event.entity.player.PlayerEvent$BreakSpeed","net.neoforged.neoforge.event.tick.PlayerTickEvent$Post","net.neoforged.neoforge.event.tick.PlayerTickEvent","net.minecraft.world.item.component.ItemAttributeModifiers","net.minecraft.world.entity.ai.attributes.AttributeModifier","net.minecraft.world.item.crafting.AbstractCookingRecipe","net.minecraft.world.item.crafting.SingleRecipeInput","net.minecraft.world.level.block.Block","net.minecraft.world.entity.item.ItemEntity","net.minecraft.world.item.enchantment.EnchantmentHelper"})try {
   var c=Class.forName(name);for(var x:c.getConstructors())System.out.println("PICKAXE_API:"+x);
   for(var m:c.getDeclaredMethods())if(!m.isSynthetic()&&(name.contains("Event")||name.contains("Modifiers")||name.contains("Recipe")||m.getName().matches(".*(Drops|Exp|PickUp|Owner|Enchant|Modifier|Target|Smelt).*")))System.out.println("PICKAXE_API:"+m);
  }catch(Exception e){System.out.println("PICKAXE_API:"+e);}
 }
}