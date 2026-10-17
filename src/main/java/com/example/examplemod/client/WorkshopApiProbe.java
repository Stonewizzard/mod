package com.example.examplemod.client;
public final class WorkshopApiProbe {
 public static void run() {
  for(String name:new String[]{"net.minecraft.world.item.ItemStack","net.minecraft.world.item.Item$Properties","net.minecraft.world.item.Item","net.minecraft.world.item.Rarity"})try{
    var cls=Class.forName(name);var path=java.nio.file.Files.createTempFile("workshop",".class");
    try(var in=cls.getResourceAsStream("/"+name.replace('.','/')+".class")){java.nio.file.Files.copy(in,path,java.nio.file.StandardCopyOption.REPLACE_EXISTING);}
    var p=new ProcessBuilder("javap","-c","-p",path.toString()).redirectErrorStream(true).start();String[] lines=new String(p.getInputStream().readAllBytes()).split("\n");boolean print=false;
    for(String line:lines){if(line.startsWith("  ")&&!line.startsWith("    ")&&line.contains("(")){print=line.contains("getStyledHoverName(")||line.contains("getItemName(")||line.contains("getName(")||line.contains("getHoverName(")||line.contains("finalizeInitializer(");}if(print)System.out.println("COLOR_API:"+name+":"+line);}
  }catch(Exception e){System.out.println("COLOR_API:"+e);}
 }
}