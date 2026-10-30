package com.example.examplemod.client;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class AncientAnvilVisualRegression {
    private static Identifier id(String p){return Identifier.fromNamespaceAndPath("examplemod",p);}
    public static void run(ResourceManager r)throws Exception{
        for(String name:new String[]{"metal","gold","runes","top"})try(var in=r.getResourceOrThrow(id("textures/block/anvil_"+name+".png")).open()){var png=ImageIO.read(in);if(png==null||png.getWidth()!=32||png.getHeight()!=32)throw new IllegalStateException("Invalid anvil material "+name);}
        for(String sprite:new String[]{"ancient_ingot","ancient_scrap"})try(var in=r.getResourceOrThrow(id("textures/item/"+sprite+".png")).open()){var png=ImageIO.read(in);if(png==null||png.getWidth()!=32||png.getHeight()!=32)throw new IllegalStateException("Invalid ancient ingot sprite");for(int i=0;i<32;i++)if((png.getRGB(0,i)>>>24)!=0||(png.getRGB(31,i)>>>24)!=0||(png.getRGB(i,0)>>>24)!=0||(png.getRGB(i,31)>>>24)!=0)throw new IllegalStateException("Ingot sprite clips its transparent margin");}
        try(var in=r.getResourceOrThrow(id("models/block/ancient_anvil.json")).open()){
            var model=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
            for(var e:model.getAsJsonArray("elements")){var box=e.getAsJsonObject();for(String corner:new String[]{"from","to"})for(var n:box.getAsJsonArray(corner))if(n.getAsDouble()<0||n.getAsDouble()>16)throw new IllegalStateException("Anvil extends beyond its owned cell");for(var face:box.getAsJsonObject("faces").entrySet()){var uv=face.getValue().getAsJsonObject().getAsJsonArray("uv");if(uv==null||uv.size()!=4)throw new IllegalStateException("Implicit anvil texture coordinates");for(var n:uv)if(n.getAsDouble()<0||n.getAsDouble()>16)throw new IllegalStateException("Out-of-range anvil UV");}}
        }
        System.out.println("ANCIENT_ANVIL_VISUAL_CHECK: PASS: four custom materials, bounded explicit-UV model, transparent ingot/scrap margins");
    }
    private AncientAnvilVisualRegression(){}
}
