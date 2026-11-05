package com.example.examplemod.client;

import com.google.gson.*;
import com.example.examplemod.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/** Resource/ownership contracts; the native screenshots are the visual review. */
public final class AncientAnvilVisualRegression {
    private static Identifier id(String path){return Identifier.fromNamespaceAndPath("examplemod",path);}
    private static JsonObject json(ResourceManager r,String path)throws Exception{try(var in=r.getResourceOrThrow(id(path)).open()){return JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}}
    public static void run(ResourceManager r)throws Exception{
        for(String name:new String[]{"runic_anvil_metal","runic_anvil_edge","runic_anvil_frame","runic_anvil_top","runic_anvil_top_glow","runic_anvil_panel","runic_anvil_panel_glow","runic_anvil_horn","runic_anvil_ember"})try(var in=r.getResourceOrThrow(id("textures/block/"+name+".png")).open()){
            var png=ImageIO.read(in);int size=name.startsWith("runic_anvil_top")?48:16;
            boolean animated=name.endsWith("_glow")||name.endsWith("_ember");
            if(png==null||png.getWidth()!=size||png.getHeight()!=size*(animated?5:1))throw new IllegalStateException("Invalid runic anvil material: "+name);
            if(animated){
                for(int y=0;y<png.getHeight();y++)for(int x=0;x<size;x++){int alpha=png.getRGB(x,y)>>>24;if(alpha!=0&&alpha!=255)throw new IllegalStateException("Filtered rune-mask pixels");}
                var animation=json(r,"textures/block/"+name+".png.mcmeta").getAsJsonObject("animation");
                if(animation.get("width").getAsInt()!=size||animation.get("height").getAsInt()!=size||animation.getAsJsonArray("frames").size()!=8)throw new IllegalStateException("Invalid slow rune animation");
            }
        }
        for(String name:new String[]{"runic_anvil_horn_long","runic_anvil_edge_long","runic_anvil_metal_long","runic_anvil_fall"})try(var in=r.getResourceOrThrow(id("textures/block/"+name+".png")).open()){
            var png=ImageIO.read(in);boolean horizontal=name.equals("runic_anvil_metal_long"),fall=name.endsWith("_fall");
            if(png==null||png.getWidth()!=(horizontal?32:16)||png.getHeight()!=(fall?256:horizontal?16:32))throw new IllegalStateException("Invalid native long-horn/fall texture");
            if(fall){var a=json(r,"textures/block/"+name+".png.mcmeta").getAsJsonObject("animation");if(a.getAsJsonArray("frames").size()!=16||a.get("frametime").getAsInt()!=3)throw new IllegalStateException("Lava fall must animate downward");}
        }
        var layout=json(r,"anvil_layout.json").getAsJsonArray("cells");
        if(layout.size()!=18||AncientAnvilStructure.CELLS.size()!=18)throw new IllegalStateException("Wrong centerpiece reservation");
        var designs=json(r,"anvil_designs.json").getAsJsonArray("designs");
        if(designs.size()!=4)throw new IllegalStateException("Missing design comparison models");
        for(var raw:designs){
            var config=raw.getAsJsonObject();String prefix=config.get("prefix").getAsString(),name=config.get("id").getAsString();
            String suffix=name.equals("base")?"":"_"+name;int headOffset=config.get("headOffset").getAsInt();
        int luminous=0;
        for(int cell=0;cell<layout.size();cell++){
            var offset=AncientAnvilStructure.CELLS.get(cell);var declared=layout.get(cell).getAsJsonObject();
            if(declared.get("x").getAsInt()!=offset.x()||declared.get("y").getAsInt()!=offset.y()||declared.get("z").getAsInt()!=offset.z())throw new IllegalStateException("Model/physics layout mismatch");
            var model=json(r,"models/block/"+prefix+"_cell_"+cell+".json");
            var body=cell==3?model.getAsJsonObject("children").getAsJsonObject("body"):model;
            if(cell==3){
                if(!model.get("loader").getAsString().equals("neoforge:composite")||!model.getAsJsonObject("children").getAsJsonObject("horn").get("loader").getAsString().equals("neoforge:obj"))throw new IllegalStateException("Missing baked tapered horn");
                try(var in=r.getResourceOrThrow(id("models/block/ancient_anvil_horn"+suffix+".obj")).open()){
                    if(in.readAllBytes().length<500)throw new IllegalStateException("Empty tapered horn mesh");
                }
            }
            if(!model.get("render_type").getAsString().equals("minecraft:cutout")||body.getAsJsonArray("elements").isEmpty())throw new IllegalStateException("Missing cutout geometry");
            for(var value:body.getAsJsonArray("elements")){
                var e=value.getAsJsonObject();ownedGeometry(e,offset);
                if(e.has("light_emission")&&e.get("light_emission").getAsInt()==15)luminous++;
                for(var f:e.getAsJsonObject("faces").entrySet()){
                    var face=f.getValue().getAsJsonObject();var uv=face.getAsJsonArray("uv");
                    if(uv==null||uv.size()!=4)throw new IllegalStateException("Missing explicit anvil UV");
                    for(var n:uv)if(n.getAsDouble()<0||n.getAsDouble()>16)throw new IllegalStateException("Invalid anvil UV");
                    String texture=face.get("texture").getAsString();
                    if(f.getKey().equals("up")&&(texture.equals("#top")||texture.equals("#top_glow"))){
                        var a=e.getAsJsonArray("from");var b=e.getAsJsonArray("to");
                        if(Math.abs(uv.get(0).getAsDouble()*3-(a.get(0).getAsDouble()+offset.x()*16+16))>1e-5||Math.abs(uv.get(2).getAsDouble()*3-(b.get(0).getAsDouble()+offset.x()*16+16))>1e-5||Math.abs(uv.get(1).getAsDouble()*3-(a.get(2).getAsDouble()+offset.z()*16-headOffset+16))>1e-5||Math.abs(uv.get(3).getAsDouble()*3-(b.get(2).getAsDouble()+offset.z()*16-headOffset+16))>1e-5)throw new IllegalStateException("Stretched or disconnected seal skin");
                    }
                }
            }
        }
        if(luminous<20)throw new IllegalStateException("Missing inset rune emission");
        }
        if(json(r,"blockstates/ancient_anvil_part.json").getAsJsonObject("variants").size()!=544||json(r,"blockstates/ancient_anvil.json").getAsJsonObject("variants").size()!=128)throw new IllegalStateException("Missing legacy/current anvil variants");
        for(int cell=0;cell<6;cell++)if(json(r,"models/block/legacy_ancient_anvil_cell_"+cell+".json").getAsJsonArray("elements").isEmpty())throw new IllegalStateException("Missing saved-world compatibility model");
        for(var design:AncientAnvilDesign.values())for(var facing:net.minecraft.core.Direction.Plane.HORIZONTAL){
            int[] diagonal={0};var outline=AncientAnvilOutline.shape(design,facing);
            outline.forAllEdges((x,y,z,X,Y,Z)->{if(Math.abs(X-x)>.01&&Math.abs(Z-z)>.01)diagonal[0]++;});
            var bounds=outline.bounds();if(diagonal[0]<12||bounds.maxY<1.9||Math.max(bounds.getXsize(),bounds.getZsize())<2.5)throw new IllegalStateException("Centerpiece silhouette or true horn edges missing");
        }
        System.out.println("ANCIENT_ANVIL_VISUAL_CHECK: PASS: eighteen owned cells, native-density continuous rune seal, binary-alpha emissive inlays, broad assembly silhouettes in four facings, legacy/current variants");
    }
    private static void ownedGeometry(JsonObject e,AncientAnvilStructure.Cell offset){
        if(!e.has("rotation")){
            for(String corner:new String[]{"from","to"})for(var n:e.getAsJsonArray(corner))if(n.getAsDouble()<0||n.getAsDouble()>16)throw new IllegalStateException("Unowned anvil geometry");return;
        }
        var r=e.getAsJsonObject("rotation");if(!r.get("axis").getAsString().equals("y")||r.get("angle").getAsDouble()!=45||r.get("rescale").getAsBoolean())throw new IllegalStateException("Unverified anvil rotation");
        var origin=r.getAsJsonArray("origin");double ox=origin.get(0).getAsDouble(),oz=origin.get(2).getAsDouble(),c=Math.sqrt(.5);
        for(int mask=0;mask<8;mask++){
            double x=e.getAsJsonArray((mask&1)==0?"from":"to").get(0).getAsDouble(),y=e.getAsJsonArray((mask&2)==0?"from":"to").get(1).getAsDouble()+offset.y()*16,z=e.getAsJsonArray((mask&4)==0?"from":"to").get(2).getAsDouble();
            double X=ox+(x-ox)*c+(z-oz)*c+offset.x()*16,Z=oz-(x-ox)*c+(z-oz)*c+offset.z()*16;
            if(X< -16||X>32||y<0||y>32||Z< -16||Z>32)throw new IllegalStateException("Rotated head escapes the owned assembly");
        }
    }
    private AncientAnvilVisualRegression(){}
}
