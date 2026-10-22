package com.example.examplemod.client;

import com.example.examplemod.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class ComponentStationVisualRegression {
    public static void run(ResourceManager resources)throws Exception{
        java.awt.image.BufferedImage font;try(var input=resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/font/ascii.png")).open()){font=ImageIO.read(input);}
        int scenes=0;var previews=new com.google.gson.JsonArray();
        for(var kind:ComponentStationKind.values())for(int phase=0;phase<4;phase++){
            boolean filled=phase>0,corrupted=phase==3;var rows=new ArrayList<ComponentStationView.Row>();var offers=new ArrayList<ComponentStationView.Row>();
            for(int i=0;i<8&&filled;i++){var trait=PickaxeTrait.values()[i];rows.add(new ComponentStationView.Row(PickaxeTooltips.label(trait),"T"+(i+1)+" +"+PickaxeTooltips.value(trait,trait.cap),ModifierScaling.values()[i%6],0xFFD369,i==7,phase==2&&i==0,kind==ComponentStationKind.LOCKER&&phase==1));}
            for(int i=0;i<3&&filled;i++)offers.add(new ComponentStationView.Row(new String[]{"Mining speed","Underwater recovery","Block reach"}[i],new String[]{"T1 +120%","T4 +85%","T12 +1 blocks"}[i],ModifierScaling.values()[3+i],0xCF9AFF,false,false,!corrupted));
            boolean enabled=filled&&!corrupted&&!(kind==ComponentStationKind.RESTORER&&phase==2);
            var influence=phase==2?ModifierInfluence.NETHER:phase==3?ModifierInfluence.END:ModifierInfluence.NONE;
            if(kind==ComponentStationKind.INFLUENCER){offers.clear();for(var target:ModifierInfluence.CHOICES)offers.add(new ComponentStationView.Row(influence==target?target.label+" (applied)":target.label,influence==ModifierInfluence.NONE?"Assign permanently":influence==target?"One use completed":"Cannot change",ModifierScaling.B,target.color,false,false,filled&&!corrupted&&influence==ModifierInfluence.NONE));}
            var state=new ComponentStationView.State(kind,filled,phase==2?120:80,corrupted,corrupted?"Corrupted: no more modifications":kind.description,List.copyOf(rows),List.copyOf(offers),enabled,influence);
            var canvas=new ExcavationVisualRegression.RecordingCanvas(font,320,242);ComponentStationView.draw(canvas,state,0);scenes++;
            if(phase==1&&(kind==ComponentStationKind.UNVEILER||kind==ComponentStationKind.LOCKER||kind==ComponentStationKind.RESTORER||kind==ComponentStationKind.INFLUENCER)){
                var preview=new com.google.gson.JsonObject();preview.addProperty("name",kind.id);preview.addProperty("width",320);preview.addProperty("height",242);preview.add("commands",canvas.commands);previews.add(preview);
            }
            if(kind==ComponentStationKind.UNVEILER&&filled&&!corrupted)for(int i=0;i<3;i++)require(ComponentStationView.buttonAt(state,84,31+i*32)==10+i&&ComponentStationView.buttonAt(state,309.9,57.9+i*32)==10+i,"Unveiler choice bounds drifted");
            if(kind==ComponentStationKind.LOCKER&&phase==1)for(int i=0;i<8;i++)require(ComponentStationView.buttonAt(state,84+(i%2)*116,31+(i/2)*24)==20+i,"Locker modifier bounds drifted");
            if(kind==ComponentStationKind.INFLUENCER){for(int i=0;i<ModifierInfluence.CHOICES.size();i++)require(ComponentStationView.buttonAt(state,85+i*116,32)==(offers.get(i).enabled()?30+i:-1),"Influence choices must respect permanent assignment and corrupted/empty states");require(ComponentStationView.buttonAt(state,100,136)==-1,"Influencer result rows must not intercept clicks");}
            if(kind!=ComponentStationKind.LOCKER&&kind!=ComponentStationKind.UNVEILER&&kind!=ComponentStationKind.INFLUENCER)require(ComponentStationView.buttonAt(state,100,136)==(enabled?0:-1),"Station action enabled state drifted");
            require(ComponentStationView.buttonAt(state,28,48)==-1&&ComponentStationView.buttonAt(state,80,162)==-1,"Action regions must not intercept real inventory slots");
            if(corrupted)require(ComponentStationView.buttonAt(state,100,40)==-1&&ComponentStationView.buttonAt(state,100,136)==-1,"Corrupted UI must reject all actions");
        }
        for(var kind:ComponentStationKind.values()){
            resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/block/"+kind.texture+".png"));resources.getResourceOrThrow(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"blockstates/"+kind.id+".json"));resources.getResourceOrThrow(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"items/"+kind.id+".json"));
        }
        if(Boolean.getBoolean("examplemod.excavationVisualExport")){
            var export=new com.google.gson.JsonObject();export.add("scenes",previews);try(var input=resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/font/ascii.png")).open()){export.addProperty("font",java.util.Base64.getEncoder().encodeToString(input.readAllBytes()));}
            var bytes=new java.io.ByteArrayOutputStream();try(var zip=new java.util.zip.DeflaterOutputStream(bytes)){zip.write(new com.google.gson.Gson().toJson(export).getBytes(java.nio.charset.StandardCharsets.UTF_8));}System.out.println("COMPONENT_STATION_PREVIEW_Z:"+java.util.Base64.getEncoder().encodeToString(bytes.toByteArray()));
        }
        System.out.println("COMPONENT_STATION_UI_CHECK: PASS: "+scenes+" empty/active/influenced/locked/restored/corrupted layouts, real Minecraft font bounds, exact choice and lock hit regions, permanent influence choices, inventory isolation and all seven placeholder block resources");
    }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private ComponentStationVisualRegression(){}
}
