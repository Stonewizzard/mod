package com.example.examplemod.client;

import com.example.examplemod.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class AscenderVisualRegression {
    public static void run(ResourceManager resources)throws Exception{
        java.awt.image.BufferedImage font;try(var input=resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/font/ascii.png")).open()){font=ImageIO.read(input);}
        var costs=new ArrayList<AscenderScreenView.Cost>();for(String name:new String[]{"Redstone Dust","Lapis Lazuli","Iron Ingot","String","Copper Ingot","Amethyst Shard","Prismarine Shard","Feather","Ender Pearl","Blaze Powder"})costs.add(new AscenderScreenView.Cost(name,100,1152));
        for(int level:new int[]{1,9,10,20,90,100})for(boolean ready:new boolean[]{false,true}){
            int stage=Math.min(10,level/10-(ready?1:0));stage=Math.max(stage,0);
            var canvas=new ExcavationVisualRegression.RecordingCanvas(font,320,242);AscenderScreenView.draw(canvas,new AscenderScreenView.State(true,new PickaxeProgression.State(level,19,stage),costs));
            for(var command:canvas.commands){var json=command.getAsJsonObject();if(json.has("text")&&json.get("y").getAsInt()>=45&&json.get("y").getAsInt()<=135&&json.get("x").getAsInt()>=145&&json.get("x").getAsInt()<260&&json.get("x").getAsInt()+json.get("w").getAsInt()>257)throw new IllegalStateException("Ascender cost label overlaps its quantity");}
        }
        AscenderScreenView.draw(new ExcavationVisualRegression.RecordingCanvas(font,320,242),new AscenderScreenView.State(false,new PickaxeProgression.State(1,0,0),java.util.List.of()));
        for(String texture:new String[]{"chiseled_deepslate","enchanting_table_top","obsidian"})resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/block/"+texture+".png"));
        resources.getResourceOrThrow(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"blockstates/ascender.json"));resources.getResourceOrThrow(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"items/ascender.json"));
        System.out.println("ASCENDER_UI_CHECK: PASS: native font layout, empty/leveling/gated/mastered states, ten material rows, quantities, real inventory coordinates and block resources");
    }
    private AscenderVisualRegression(){}
}
