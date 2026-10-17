package com.example.examplemod.client;
public final class WorkshopApiProbe {
 public static void run() {
  for(var m:net.minecraft.world.item.Item.Properties.class.getDeclaredMethods())System.out.println("WORKSHOP_API:"+m);
  try{
    String path=java.nio.file.Path.of(net.minecraft.world.item.Items.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
    var p=new ProcessBuilder("javap","-c","-p","-classpath",path,"net.minecraft.world.item.Items").redirectErrorStream(true).start();
    String out=new String(p.getInputStream().readAllBytes());String[] lines=out.split("\n");
    for(int i=0;i<lines.length;i++)if(lines[i].contains("String diamond_spear")||lines[i].contains("String mace"))for(int j=Math.max(0,i-3);j<Math.min(lines.length,i+65);j++)System.out.println("ITEM_BYTECODE:"+lines[j]);
  }catch(Exception e){System.out.println("WORKSHOP_API:"+e);}
 }
}