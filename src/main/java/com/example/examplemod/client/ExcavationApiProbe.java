package com.example.examplemod.client;
public final class ExcavationApiProbe {
 public static void run(){
  for(String name:new String[]{
   "net.minecraft.client.input.MouseButtonEvent","net.minecraft.client.input.MouseButtonInfo",
   "net.minecraft.client.input.KeyEvent","net.minecraft.world.item.component.CustomData",
   "net.minecraft.nbt.CompoundTag","net.minecraft.network.protocol.common.custom.CustomPacketPayload",
   "net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent",
   "net.neoforged.neoforge.network.registration.PayloadRegistrar",
   "net.neoforged.neoforge.network.handling.IPayloadContext",
   "net.neoforged.neoforge.network.PacketDistributor",
   "net.minecraft.network.codec.StreamCodec","net.minecraft.world.inventory.Slot",
   "net.minecraft.client.gui.screens.inventory.AbstractContainerScreen"
  })try{var c=Class.forName(name);System.out.println("EXCAVATION_API "+name);
   for(var f:c.getDeclaredFields())System.out.println(" F "+f.toGenericString());
   for(var x:c.getDeclaredConstructors())System.out.println(" C "+x.toGenericString());
   for(var m:c.getDeclaredMethods())if(!m.isSynthetic())System.out.println(" M "+m.toGenericString());
  }catch(Throwable e){System.out.println("EXCAVATION_API_ERROR "+name+" "+e);}
 }
}
