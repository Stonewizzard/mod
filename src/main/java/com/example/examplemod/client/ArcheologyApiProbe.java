package com.example.examplemod.client;
public final class ArcheologyApiProbe {
 public static void run() {
  for(String name:new String[]{
   "net.minecraft.world.level.block.BedBlock","net.minecraft.world.level.block.BarrelBlock","net.minecraft.world.level.block.BaseEntityBlock",
   "net.minecraft.world.level.block.entity.BlockEntity","net.minecraft.world.level.block.entity.BaseContainerBlockEntity","net.minecraft.world.level.block.entity.BarrelBlockEntity",
   "net.minecraft.world.inventory.AbstractContainerMenu","net.minecraft.world.inventory.MenuType","net.minecraft.world.inventory.ChestMenu","net.minecraft.world.ContainerHelper","net.minecraft.world.SimpleContainer",
   "net.minecraft.client.renderer.blockentity.BlockEntityRenderer","net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider","net.minecraft.client.renderer.blockentity.CampfireRenderer",
   "net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState","net.minecraft.client.renderer.blockentity.state.CampfireRenderState",
   "net.minecraft.client.renderer.item.ItemStackRenderState","net.minecraft.client.renderer.item.ItemModelResolver","net.minecraft.client.renderer.SubmitNodeCollector",
   "net.minecraft.client.gui.screens.inventory.AbstractContainerScreen","net.minecraft.client.gui.GuiGraphics","net.minecraft.client.gui.screens.inventory.HopperScreen",
   "net.neoforged.neoforge.client.event.RegisterMenuScreensEvent","net.neoforged.neoforge.registries.DeferredRegister$BlockEntities",
   "net.neoforged.neoforge.network.IContainerFactory","net.minecraft.server.level.ServerPlayer","net.minecraft.world.item.ItemDisplayContext",
   "net.minecraft.world.level.storage.ValueInput","net.minecraft.world.level.storage.ValueOutput","net.minecraft.world.level.block.entity.BlockEntityType",
   "net.minecraft.world.level.block.state.BlockBehaviour","net.minecraft.world.level.block.Block"
  }) try {
   Class<?> c=Class.forName(name);System.out.println("TABLE_API "+name);
   for(var f:c.getDeclaredFields())System.out.println(" F "+f.toGenericString());
   for(var x:c.getDeclaredConstructors())System.out.println(" C "+x.toGenericString());
   for(var m:c.getDeclaredMethods())if(!m.isSynthetic())System.out.println(" M "+m.toGenericString());
  }catch(Throwable t){System.out.println("TABLE_API_MISSING "+name+" "+t);}
 }
}
