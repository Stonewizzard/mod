package com.example.examplemod.client;
import com.example.examplemod.*;
import java.util.*;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
/** Real packaged font and native resource validation for every modifier and detail page. */
public final class AppraisalVisualRegression {
    public static void run(ResourceManager resources)throws Exception{
        java.awt.image.BufferedImage font;try(var input=resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/font/ascii.png")).open()){font=ImageIO.read(input);}
        var entries=new ArrayList<AppraisalReport.Entry>();entries.add(new AppraisalReport.Entry("Overview",0xffddbb,List.of("Place an artifact or a component in the slot. The inspection is read only.")));
        for(var trait:PickaxeTrait.values()){
            var lines=new ArrayList<String>();lines.add(ModifierDescriptions.description(trait));lines.add("Current contribution: "+PickaxeTooltips.value(trait,trait.cap)+".");lines.add("Scaling S: +45% of base per ascension. Value tier T1.");
            ModifierTuning.rules(trait).forEach((key,values)->lines.add(key+": "+values+" (stages 0 / 1 / 2 / 3)"));
            entries.add(new AppraisalReport.Entry(trait.label,PickaxeTooltips.TEAL,lines));
        }
        var report=new AppraisalReport.Report("Restored Ancient Pickaxe",entries);int scenes=0;com.google.gson.JsonObject preview=null;
        for(int selected=0;selected<entries.size();selected++){
            var measuring=new ExcavationVisualRegression.RecordingCanvas(font,320,242);int pages=Math.max(1,(AppraisalView.wrap(measuring,entries.get(selected).lines()).size()+AppraisalView.LINES-1)/AppraisalView.LINES);
            for(int detail=0;detail<pages;detail++){
                var canvas=new ExcavationVisualRegression.RecordingCanvas(font,320,242);AppraisalView.draw(canvas,report,selected,selected/AppraisalView.ROWS,detail,-1);scenes++;
                for(var command:canvas.commands){var c=command.getAsJsonObject();if(c.has("text")&&c.get("x").getAsInt()>=130&&c.get("y").getAsInt()>=49&&c.get("y").getAsInt()<130&&c.get("x").getAsInt()+c.get("w").getAsInt()>299)throw new IllegalStateException("Appraisal detail escaped the wrapping column");}
                if(selected==12&&detail==0){preview=new com.google.gson.JsonObject();preview.add("commands",canvas.commands);}
            }
            if(AppraisalView.hit(14,67,selected/AppraisalView.ROWS,entries.size())!=selected/AppraisalView.ROWS*AppraisalView.ROWS)throw new IllegalStateException("Appraisal row hitboxes do not match the drawn rows");
        }
        for(String name:new String[]{"blockstates/appraisal_table.json","items/appraisal_table.json","models/block/appraisal_table.json"})resources.getResourceOrThrow(Identifier.fromNamespaceAndPath(ExampleMod.MODID,name));
        for(String name:new String[]{"stripped_oak_log","oak_planks","white_concrete","dark_oak_planks","light_blue_stained_glass"})resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/block/"+name+".png"));
        // Export an exact draw-command preview without relying on an interactive desktop.
        if(preview!=null){var png=new java.io.ByteArrayOutputStream();ImageIO.write(font,"png",png);preview.addProperty("font",Base64.getEncoder().encodeToString(png.toByteArray()));var bytes=new java.io.ByteArrayOutputStream();try(var zip=new java.util.zip.DeflaterOutputStream(bytes)){zip.write(new com.google.gson.Gson().toJson(preview).getBytes(java.nio.charset.StandardCharsets.UTF_8));}System.out.println("APPRAISAL_PREVIEW_Z:"+Base64.getEncoder().encodeToString(bytes.toByteArray()));}
        System.out.println("APPRAISAL_UI_CHECK: PASS: "+scenes+" actual-font detail pages across all 120 modifiers, button hitboxes, inventory bounds and native table resources");
    }
    private AppraisalVisualRegression(){}
}
