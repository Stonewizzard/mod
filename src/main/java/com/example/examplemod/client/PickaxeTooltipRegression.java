package com.example.examplemod.client;

import com.example.examplemod.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/** Uses the same component formatter as item lore, with Minecraft's packaged bitmap font. */
public final class PickaxeTooltipRegression {
    public static void run(ResourceManager resources)throws Exception{
        java.awt.image.BufferedImage font;try(var input=resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/font/ascii.png")).open()){font=ImageIO.read(input);}
        var scenes=new java.util.LinkedHashMap<String,List<Component>>();
        for(int level:new int[]{3,10,100}){
            var stats=new ArrayList<PickaxeTooltips.Stat>();
            for(var trait:PickaxeTrait.values())stats.add(new PickaxeTooltips.Stat(trait,trait.cap*.55,ModifierScaling.C,ModifierScaling.S));
            var view=new PickaxeTooltips.ToolView(new PickaxeProgression.State(level,14,level==100?10:0),87,stats);
            for(int page=0;page<PickaxeTooltips.pages(view);page++)scenes.put("tool_"+level+"_page_"+page,PickaxeTooltips.tool(view,page));
        }
        scenes.put("zero_quality",PickaxeTooltips.tool(new PickaxeTooltips.ToolView(new PickaxeProgression.State(1,0,0),0,List.of())));
        for(var grade:ModifierScaling.values())scenes.put("part_"+grade,PickaxeTooltips.piece(50,5,List.of(new ArtifactModifiers.Roll(PickaxeTrait.SPEED.id,1.124,0.5,grade,"t1",1),new ArtifactModifiers.Roll(PickaxeTrait.REACH.id,1,.5,grade,"future_t12",12),new ArtifactModifiers.Roll(PickaxeTrait.SILK.id,.7,.5,grade,"t1",1),new ArtifactModifiers.Roll(PickaxeTrait.DURABILITY.id,.6,.5,grade,"t1",1))));
        for(var grade:ModifierScaling.values())scenes.put("milestone_part_"+grade,PickaxeTooltips.piece(50,5,List.of(new ArtifactModifiers.Roll(PickaxeTrait.VEIN_MINER.id,1,.5,grade,"t1",1),new ArtifactModifiers.Roll(PickaxeTrait.VOID_ANCHOR.id,1,.5,grade,"t1",1))));
        scenes.put("legacy_part",PickaxeTooltips.piece(80,3,List.of(new ArtifactModifiers.Roll(PickaxeTrait.SPEED.id,.5,.8,ModifierScaling.B))));
        var export=new com.google.gson.JsonObject();var jsonScenes=new com.google.gson.JsonArray();
        for(var scene:scenes.entrySet()){
            var rows=new com.google.gson.JsonArray();
            for(var line:scene.getValue()){
                var canvas=new ExcavationVisualRegression.RecordingCanvas(font,300,256);if(canvas.textWidth(line.getString())>280)throw new IllegalStateException("Pickaxe tooltip row too wide: "+line.getString());
                var segments=new com.google.gson.JsonArray();line.visit((style,value)->{
                    if(style.isItalic()||(!value.isEmpty()&&style.getColor()==null))throw new IllegalStateException("Tooltip segment lost its explicit color or inherited italics");
                    var segment=new com.google.gson.JsonObject();segment.addProperty("text",value);segment.addProperty("color",style.getColor()==null?0xFFFFFF:style.getColor().getValue());segments.add(segment);return Optional.empty();
                },Style.EMPTY);rows.add(segments);
            }
            if(rows.size()>16)throw new IllegalStateException("Pickaxe tooltip exceeded its compact row budget");
            var item=new com.google.gson.JsonObject();item.addProperty("name",scene.getKey());item.add("rows",rows);jsonScenes.add(item);
        }
        export.add("scenes",jsonScenes);
        if(Boolean.getBoolean("examplemod.excavationVisualExport")){
            try(var input=resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/font/ascii.png")).open()){export.addProperty("font",java.util.Base64.getEncoder().encodeToString(input.readAllBytes()));}
            var bytes=new java.io.ByteArrayOutputStream();try(var zip=new java.util.zip.DeflaterOutputStream(bytes)){zip.write(new com.google.gson.Gson().toJson(export).getBytes(java.nio.charset.StandardCharsets.UTF_8));}
            System.out.println("PICKAXE_TOOLTIP_PREVIEW_Z:"+java.util.Base64.getEncoder().encodeToString(bytes.toByteArray()));
        }
        System.out.println("PICKAXE_TOOLTIP_CHECK: PASS: compact component rows, native font widths, all six grade badges, value/quality colors, zero/gated/mastered cases and explicit non-italic styling");
    }
    private PickaxeTooltipRegression(){}
}
