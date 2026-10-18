package com.example.examplemod.client;
public final class AscensionApiProbe {
 public static void run() throws Exception {
  for(String name:java.util.List.of("net.minecraft.world.item.Item","net.minecraft.world.item.ItemStack","net.minecraft.world.item.component.ItemAttributeModifiers","net.minecraft.world.level.block.BaseEntityBlock","net.minecraft.server.level.ServerPlayerGameMode","net.neoforged.neoforge.event.level.BlockEvent$BreakEvent")){
   Class<?> cls=Class.forName(name);
   for(var m:cls.getDeclaredMethods()) if(m.getName().matches("mineBlock|withModifierAdded|getTicker|createTickerHelper|destroyBlock"))System.out.println("ASCENSION_API "+m);
   if(name.contains("ItemAttributeModifiers")||name.contains("ServerPlayerGameMode")||name.endsWith(".ItemStack")){
    var file=java.nio.file.Files.createTempFile("asc-api",".class");
    try(var in=cls.getResourceAsStream("/"+name.replace('.','/')+".class")){java.nio.file.Files.copy(in,file,java.nio.file.StandardCopyOption.REPLACE_EXISTING);}
    var process=new ProcessBuilder("javap","-c","-p",file.toString()).redirectErrorStream(true).start();
    String out=new String(process.getInputStream().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
    boolean show=false;for(String line:out.split("\\R")){if(line.matches("  (public|private|protected).*"))show=line.contains("mineBlock(")||line.contains("destroyBlock(")||line.contains("withModifierAdded(");if(show)System.out.println("ASCENSION_BYTECODE "+line);}
    process.waitFor();java.nio.file.Files.deleteIfExists(file);
   }
  }
 }
}