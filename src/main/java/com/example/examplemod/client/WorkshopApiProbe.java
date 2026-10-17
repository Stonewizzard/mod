package com.example.examplemod.client;
public final class WorkshopApiProbe {
 public static void run() {
  try{
    var path=java.nio.file.Files.createTempFile("workshop-items",".class");
    try(var in=net.minecraft.world.item.Items.class.getResourceAsStream("/net/minecraft/world/item/Items.class")){java.nio.file.Files.copy(in,path,java.nio.file.StandardCopyOption.REPLACE_EXISTING);}
    var p=new ProcessBuilder("javap","-c","-p",path.toString()).redirectErrorStream(true).start();
    String out=new String(p.getInputStream().readAllBytes());String[] lines=out.split("\n");System.out.println("ITEM_BYTECODE: lines="+lines.length);
    for(int i=0;i<lines.length;i++)if(lines[i].contains("diamond_spear")||lines[i].contains("String mace")||lines[i].contains("Properties.spear")||lines[i].contains("MaceItem.create"))for(int j=Math.max(0,i-20);j<Math.min(lines.length,i+50);j++)System.out.println("ITEM_BYTECODE:"+lines[j]);
  }catch(Exception e){System.out.println("ITEM_BYTECODE:"+e);}
 }
}