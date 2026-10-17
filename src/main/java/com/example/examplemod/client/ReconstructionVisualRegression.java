package com.example.examplemod.client;

import com.example.examplemod.ReconstructionTableMenu;
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
        for(int state=0;state<6;state++){
            var inserted=new boolean[5];var scores=new int[]{92,87,100,89,96};if(state>=2)Arrays.fill(inserted,true);if(state==2)inserted[2]=false;if(state==4)Arrays.fill(inserted,false);if(state==5)Arrays.fill(scores,-1);
            var view=new ReconstructionScreenView.State(state!=0,inserted,scores,state==4, state>=4?0:92.8);
            var canvas=new ExcavationVisualRegression.RecordingCanvas(font);ReconstructionScreenView.draw(canvas,view);
            for(int i=0;i<5;i++)if(inserted[i]){var p=ReconstructionTableMenu.POSITIONS[i];canvas.image(ExcavationScreenView.texture("item/"+new String[]{"pickaxe_left_tip","pickaxe_head_bridge","pickaxe_right_tip","ancient_tool_connector","ancient_tool_handle"}[i]),p[0],p[1],16,16,32,32,-1);}
            if(state==4)canvas.image(ExcavationScreenView.texture("item/restored_ancient_pickaxe"),274,70,16,16,32,32,-1);
            for(var id:canvas.textures)if(!assets.has(id.toString()))try(var input=resources.getResourceOrThrow(id).open()){byte[] bytes=input.readAllBytes();if(ImageIO.read(new ByteArrayInputStream(bytes))==null)throw new IllegalStateException("Invalid reconstruction texture "+id);assets.addProperty(id.toString(),Base64.getEncoder().encodeToString(bytes));}
            var scene=new JsonObject();scene.addProperty("name",new String[]{"unselected","empty","partial","ready","completed","unscored"}[state]);scene.addProperty("width",320);scene.addProperty("height",236);scene.add("commands",canvas.commands);scenes.add(scene);
        }
        for(int i=0;i<6;i++){var p=ReconstructionTableMenu.POSITIONS[i];if(p[0]<110||p[0]+16>308||p[1]<40||p[1]+16>124)throw new IllegalStateException("Blueprint slot escaped its assembly mat");}
        if(ReconstructionScreenView.buttonAt(18,49)!=0||ReconstructionScreenView.buttonAt(95.9,67.9)!=0||ReconstructionScreenView.buttonAt(251,101)!=1||ReconstructionScreenView.buttonAt(304.9,118.9)!=1||ReconstructionScreenView.buttonAt(200,140)!=-1)throw new IllegalStateException("Reconstruction button hit regions drifted");
        try(var input=resources.getResourceOrThrow(Identifier.fromNamespaceAndPath("examplemod","blockstates/reconstruction_table.json")).open()){
            var json=JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject();if(json.getAsJsonObject("variants").size()!=4)throw new IllegalStateException("Reconstruction table needs four facings");
        }
        if(Boolean.getBoolean("examplemod.excavationVisualExport")){
            var root=new JsonObject();root.add("assets",assets);root.add("scenes",scenes);root.add("animation",new JsonArray());try(var input=resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/font/ascii.png")).open()){root.addProperty("font",Base64.getEncoder().encodeToString(input.readAllBytes()));}
            var bytes=new ByteArrayOutputStream();try(var zip=new DeflaterOutputStream(bytes)){zip.write(new Gson().toJson(root).getBytes(StandardCharsets.UTF_8));}System.out.println("RECONSTRUCTION_PREVIEW_Z:"+Base64.getEncoder().encodeToString(bytes.toByteArray()));
        }
        System.out.println("RECONSTRUCTION_UI_CHECK: PASS: six selection/assembly/score states, native font and packaged icons, exact slot/button bounds and four block facings");
    }
}
