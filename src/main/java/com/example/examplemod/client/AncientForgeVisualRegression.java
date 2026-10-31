package com.example.examplemod.client;

import com.google.gson.*;
import com.example.examplemod.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/** Asset contracts only; the native-client screenshots remain the visual acceptance check. */
public final class AncientForgeVisualRegression {
    public static void run(ResourceManager resources)throws Exception{
        for(String name:new String[]{"deepslate_bricks","polished_blackstone","cut_copper","basalt_top"})try(var input=resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/block/"+name+".png")).open()){
            var png=ImageIO.read(input);
            if(png==null||png.getWidth()!=16||png.getHeight()!=16)throw new IllegalStateException("Invalid custom forge texture: "+name);
        }
        for(int cell=0;cell<20;cell++){
            String model=cell<18?"formed_forge_"+cell:cell==18?"ruined_ancient_forge_formed":"ruined_ancient_forge";
            var json=read(resources,"models/block/"+model+".json");
            for(var value:json.getAsJsonArray("elements")){
                var element=value.getAsJsonObject();
                for(String corner:new String[]{"from","to"})for(var coordinate:element.getAsJsonArray(corner))if(coordinate.getAsDouble()<0||coordinate.getAsDouble()>16)throw new IllegalStateException("Forge geometry leaves its owned cell: "+model);
                for(var face:element.getAsJsonObject("faces").entrySet()){
                    var uv=face.getValue().getAsJsonObject().getAsJsonArray("uv");
                    if(uv==null||uv.size()!=4)throw new IllegalStateException("Implicit/stretched UVs on "+model+"/"+face.getKey());
                    for(var coordinate:uv)if(coordinate.getAsDouble()<0||coordinate.getAsDouble()>16)throw new IllegalStateException("Forge UV outside texture: "+model);
                    if(uv.get(0).equals(uv.get(2))||uv.get(1).equals(uv.get(3)))throw new IllegalStateException("Collapsed forge texture face: "+model);
                }
            }
        }
        if(read(resources,"blockstates/formed_ruined_forge.json").getAsJsonObject("variants").size()!=72||read(resources,"blockstates/ruined_ancient_forge.json").getAsJsonObject("variants").size()!=8)throw new IllegalStateException("Missing formed/unformed forge rotations");
        System.out.println("ANCIENT_FORGE_VISUAL_CHECK: PASS: four vanilla-resolution materials, twenty bounded models, explicit noncollapsed UVs, all formed/unformed rotations");
    }
    private static Identifier id(String path){return Identifier.fromNamespaceAndPath(ExampleMod.MODID,path);}
    private static JsonObject read(ResourceManager resources,String path)throws Exception{try(var input=resources.getResourceOrThrow(id(path)).open()){return JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject();}}
    private AncientForgeVisualRegression(){}
}
