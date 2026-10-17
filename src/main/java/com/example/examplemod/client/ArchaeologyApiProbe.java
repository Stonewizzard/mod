package com.example.examplemod.client;
public final class ArchaeologyApiProbe {
 public static void run(){
  for(String name:new String[]{"net.minecraft.world.item.Item$Properties","net.minecraft.world.item.ItemStack","net.minecraft.world.item.enchantment.EnchantmentHelper","net.minecraft.world.item.ToolMaterial"})try{
   var c=Class.forName(name);for(var m:c.getDeclaredMethods())if(m.getName().matches(".*(urability|nchant|urtAndBreak|word|ickaxe|epair|xe).*"))System.out.println("ARCH_API "+m);
   for(var f:c.getFields())System.out.println("ARCH_API_FIELD "+f);
  }catch(Exception e){throw new RuntimeException(e);}
 }
}
