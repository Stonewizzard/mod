package com.example.examplemod.client;

import com.example.examplemod.*;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class ReconstructionVisualRegression {
    public static void run(ResourceManager resources)throws Exception{
        java.awt.image.BufferedImage font;try(var input=resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/font/ascii.png")).open()){font=ImageIO.read(input);}
        var assets=new JsonObject();var scenes=new JsonArray();
        for(int pattern=0;pattern<=7;pattern++)for(int state=0;state<3;state++){
            var selected=ReconstructionPattern.byId(pattern);var inserted=new boolean[5];var scores=new int[]{92,87,100,89,96};
            if(state>0&&selected!=null)for(int slot:selected.slots)inserted[slot]=true;
            if(state==1&&selected!=null)inserted[selected.slots.getFirst()]=false;
            double average=0;if(selected!=null)for(int slot:selected.slots)average+=inserted[slot]?scores[slot]/(double)selected.parts.size():0;
            var view=new ReconstructionScreenView.State(pattern,inserted,scores,false,average,false);
            var canvas=new ExcavationVisualRegression.RecordingCanvas(font,320,242);ReconstructionScreenView.draw(canvas,view);
            for(int i=0;i<5;i++)if(inserted[i]){var p=ReconstructionTableMenu.POSITIONS[i];canvas.image(ExcavationScreenView.texture("item/"+selected.partId(i)),p[0],p[1],16,16,32,32,-1);}
            snapshot(resources,font,assets,scenes,canvas,(selected==null?"unselected":selected.key())+new String[]{"_empty","_partial","_ready"}[state]);
        }
        for(int state=0;state<3;state++){
            var canvas=new ExcavationVisualRegression.RecordingCanvas(font,320,242);var inserted=new boolean[5];int[] scores={-1,-1,-1,-1,-1};if(state==1)Arrays.fill(inserted,true);
            ReconstructionScreenView.draw(canvas,new ReconstructionScreenView.State(state==2?2:1,inserted,scores,state==0,0,state==2));
            if(state==0)canvas.image(ExcavationScreenView.texture("item/restored_ancient_pickaxe"),277,73,16,16,32,32,-1);
            snapshot(resources,font,assets,scenes,canvas,new String[]{"completed","unscored","spare_parts"}[state]);
        }
        for(var p:ReconstructionPattern.values()){
            int y=ReconstructionScreenView.PICK_Y+(p.id-1)*16;
            if(ReconstructionScreenView.buttonAt(14,y)!=10+p.id||ReconstructionScreenView.buttonAt(103.9,y+14.9)!=10+p.id)throw new IllegalStateException("Pattern hit region drifted");
        }
        if(ReconstructionScreenView.buttonAt(251,105)!=1||ReconstructionScreenView.buttonAt(304.9,122.9)!=1||ReconstructionScreenView.buttonAt(200,155)!=-1)throw new IllegalStateException("Assembly button overlaps storage");
        for(int i=0;i<6;i++){var p=ReconstructionTableMenu.POSITIONS[i];if(p[0]<112||p[0]+16>310||p[1]<44||p[1]+16>124)throw new IllegalStateException("Blueprint slot escaped its panel");}
        try(var input=resources.getResourceOrThrow(Identifier.fromNamespaceAndPath("examplemod","blockstates/reconstruction_table.json")).open()){
            var json=JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject();if(json.getAsJsonObject("variants").size()!=4)throw new IllegalStateException("Reconstruction table needs four facings");
        }
        for(String name:new String[]{"skulk_parasite_spawn_egg","cruncher_egg"})try(var input=resources.getResourceOrThrow(ExcavationScreenView.texture("item/"+name)).open()){
            var png=ImageIO.read(input);if(png.getWidth()!=32||png.getHeight()!=32)throw new IllegalStateException("Egg icon dimensions drifted");
            for(int i=0;i<32;i++)if((png.getRGB(0,i)>>>24)!=0||(png.getRGB(31,i)>>>24)!=0||(png.getRGB(i,0)>>>24)!=0||(png.getRGB(i,31)>>>24)!=0)throw new IllegalStateException("Egg icon clips outside its transparent margin");
        }
        if(Boolean.getBoolean("examplemod.excavationVisualExport")){
            var root=new JsonObject();root.add("assets",assets);root.add("scenes",scenes);root.add("animation",new JsonArray());try(var input=resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/font/ascii.png")).open()){root.addProperty("font",Base64.getEncoder().encodeToString(input.readAllBytes()));}
            var bytes=new ByteArrayOutputStream();try(var zip=new DeflaterOutputStream(bytes)){zip.write(new Gson().toJson(root).getBytes(StandardCharsets.UTF_8));}System.out.println("RECONSTRUCTION_PREVIEW_Z:"+Base64.getEncoder().encodeToString(bytes.toByteArray()));
            for(String path:new String[]{"oak_planks","spruce_planks","dark_oak_planks","stripped_spruce_log","stripped_oak_log","iron_block","smithing_table_top"})try(var input=resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/block/"+path+".png")).open()){System.out.println("WORKSHOP_TEXTURE:"+path+":"+Base64.getEncoder().encodeToString(input.readAllBytes()));}
        }
        System.out.println("RECONSTRUCTION_UI_CHECK: PASS: seven pattern blueprints in empty/partial/ready states, completed/unscored/spare states, native font/icons, precise hit bounds, egg margins and four block facings");
    }
    private static void snapshot(ResourceManager resources,java.awt.image.BufferedImage font,JsonObject assets,JsonArray scenes,ExcavationVisualRegression.RecordingCanvas canvas,String name)throws Exception{
        for(var id:canvas.textures)if(!assets.has(id.toString()))try(var input=resources.getResourceOrThrow(id).open()){byte[] bytes=input.readAllBytes();if(ImageIO.read(new ByteArrayInputStream(bytes))==null)throw new IllegalStateException("Invalid workshop icon "+id);assets.addProperty(id.toString(),Base64.getEncoder().encodeToString(bytes));}
        var scene=new JsonObject();scene.addProperty("name",name);scene.addProperty("width",320);scene.addProperty("height",242);scene.add("commands",canvas.commands);scenes.add(scene);
    }
}
