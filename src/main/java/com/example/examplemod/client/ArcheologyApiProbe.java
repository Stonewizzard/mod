package com.example.examplemod.client;
public final class ArcheologyApiProbe {
 public static void run() {
  for(String name:new String[]{
"net.minecraft.client.gui.GuiGraphicsExtractor","net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context",
"net.minecraft.world.level.block.AbstractBedBlock","net.minecraft.world.Container","net.neoforged.neoforge.common.extensions.IBlockEntityExtension",
"net.minecraft.world.level.block.entity.BlockEntityType$Builder","net.minecraft.world.level.block.entity.BlockEntityType",
"net.minecraft.world.inventory.Slot","net.minecraft.world.level.block.state.BlockBehaviour$Properties"
  }) try {
   Class<?> c=Class.forName(name);System.out.println("TABLE_API "+name);
   for(var f:c.getDeclaredFields())System.out.println(" F "+f.toGenericString());
   for(var x:c.getDeclaredConstructors())System.out.println(" C "+x.toGenericString());
   for(var m:c.getDeclaredMethods())if(!m.isSynthetic())System.out.println(" M "+m.toGenericString());
  }catch(Throwable t){System.out.println("TABLE_API_MISSING "+name+" "+t);}
  for(String name:new String[]{"net.minecraft.client.renderer.blockentity.CampfireRenderer","net.minecraft.world.level.block.entity.BlockEntity"})try{
   var c=Class.forName(name);var path=java.nio.file.Files.createTempDirectory("table-bytecode").resolve(c.getSimpleName()+".class");
   try(var input=c.getResourceAsStream("/"+name.replace('.','/')+".class")){java.nio.file.Files.copy(input,path);}
   var process=new ProcessBuilder(System.getProperty("java.home")+"/bin/javap","-c","-p",path.toString()).redirectErrorStream(true).start();
   System.out.println("TABLE_BYTECODE "+name+"\\n"+new String(process.getInputStream().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));
  }catch(Exception e){System.out.println("TABLE_BYTECODE_ERROR "+e);}
 }
}
