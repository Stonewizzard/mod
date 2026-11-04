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
        for(String name:new String[]{"anvil","cut_copper","gold_block"})try(var in=r.getResourceOrThrow(Identifier.withDefaultNamespace("textures/block/"+name+".png")).open()){var png=ImageIO.read(in);if(png==null||png.getWidth()!=16||png.getHeight()!=16)throw new IllegalStateException("Invalid anvil material "+name);}
        for(String sprite:new String[]{"ancient_ingot","ancient_scrap"})try(var in=r.getResourceOrThrow(id("textures/item/"+sprite+".png")).open()){var png=ImageIO.read(in);if(png==null||png.getWidth()!=32||png.getHeight()!=32)throw new IllegalStateException("Invalid ancient ingot sprite");for(int i=0;i<32;i++)if((png.getRGB(0,i)>>>24)!=0||(png.getRGB(31,i)>>>24)!=0||(png.getRGB(i,0)>>>24)!=0||(png.getRGB(i,31)>>>24)!=0)throw new IllegalStateException("Ingot sprite clips its transparent margin");}
        for(int cell=0;cell<6;cell++)try(var in=r.getResourceOrThrow(id("models/block/ancient_anvil_cell_"+cell+".json")).open()){
            var model=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
            if(model.getAsJsonArray("elements").isEmpty())throw new IllegalStateException("Empty owned anvil cell");
            for(var value:model.getAsJsonArray("elements")){var e=value.getAsJsonObject();for(String corner:new String[]{"from","to"})for(var n:e.getAsJsonArray(corner))if(n.getAsDouble()<0||n.getAsDouble()>16)throw new IllegalStateException("Unowned anvil geometry");for(var face:e.getAsJsonObject("faces").entrySet()){var uv=face.getValue().getAsJsonObject().getAsJsonArray("uv");if(uv==null||uv.size()!=4)throw new IllegalStateException("Missing explicit UV");for(var n:uv)if(n.getAsDouble()<0||n.getAsDouble()>16)throw new IllegalStateException("Invalid anvil UV");}}
        }
        System.out.println("ANCIENT_ANVIL_VISUAL_CHECK: PASS: vanilla-resolution materials, six owned bounded explicit-UV cells, transparent ingot/scrap margins");
    }
    private AncientAnvilVisualRegression(){}
}
