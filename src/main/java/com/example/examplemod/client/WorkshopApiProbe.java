package com.example.examplemod.client;
public final class WorkshopApiProbe {
 public static void run() {
  for(Class<?> c:new Class<?>[]{net.minecraft.world.item.Item.Properties.class,net.minecraft.world.item.Item.class,net.minecraft.world.item.ItemStack.class,net.minecraft.world.item.Rarity.class}) {
   for(var m:c.getDeclaredMethods())if(m.getName().matches(".*(spear|mace|shovel|hoe|Name|name|rarity|Color|color|Style|style).*"))System.out.println("WORKSHOP_API:"+m);
  }
  for(String n:new String[]{"net.minecraft.world.item.MaceItem","net.minecraft.world.item.SpearItem"})try{var c=Class.forName(n);for(var x:c.getConstructors())System.out.println("WORKSHOP_API:"+x);for(var m:c.getDeclaredMethods())System.out.println("WORKSHOP_API:"+m);}catch(Exception e){System.out.println("WORKSHOP_API:"+e);}
 }
}