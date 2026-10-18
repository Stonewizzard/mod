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
        int scenes=0;
        for(var kind:ComponentStationKind.values())for(int phase=0;phase<4;phase++){
            boolean filled=phase>0,corrupted=phase==3;var rows=new ArrayList<ComponentStationView.Row>();var offers=new ArrayList<ComponentStationView.Row>();
            for(int i=0;i<8&&filled;i++){var trait=PickaxeTrait.values()[i];rows.add(new ComponentStationView.Row(PickaxeTooltips.label(trait),"+"+PickaxeTooltips.value(trait,trait.cap),ModifierScaling.values()[i%6],0xFFD369,i==7,phase==2&&i==0,kind==ComponentStationKind.LOCKER&&phase==1));}
            for(int i=0;i<3&&filled;i++)offers.add(new ComponentStationView.Row(new String[]{"Mining speed","Underwater recovery","Block reach"}[i],new String[]{"+120%","+85%","+1 blocks"}[i],ModifierScaling.values()[3+i],0xCF9AFF,false,false,!corrupted));
            var state=new ComponentStationView.State(kind,filled,phase==2?120:80,corrupted,corrupted?"Corrupted: no more modifications":kind.description,List.copyOf(rows),List.copyOf(offers),filled&&!corrupted);
            var canvas=new ExcavationVisualRegression.RecordingCanvas(font,320,242);ComponentStationView.draw(canvas,state,0);scenes++;
            if(kind==ComponentStationKind.UNVEILER&&filled&&!corrupted)for(int i=0;i<3;i++)require(ComponentStationView.buttonAt(state,84,31+i*32)==10+i&&ComponentStationView.buttonAt(state,309.9,57.9+i*32)==10+i,"Unveiler choice bounds drifted");
            if(kind==ComponentStationKind.LOCKER&&phase==1)for(int i=0;i<8;i++)require(ComponentStationView.buttonAt(state,84+(i%2)*116,31+(i/2)*24)==20+i,"Locker modifier bounds drifted");
            if(kind!=ComponentStationKind.LOCKER&&kind!=ComponentStationKind.UNVEILER)require(ComponentStationView.buttonAt(state,100,136)==(filled&&!corrupted?0:-1),"Station action enabled state drifted");
            require(ComponentStationView.buttonAt(state,28,48)==-1&&ComponentStationView.buttonAt(state,80,162)==-1,"Action regions must not intercept real inventory slots");
            if(corrupted)require(ComponentStationView.buttonAt(state,100,40)==-1&&ComponentStationView.buttonAt(state,100,136)==-1,"Corrupted UI must reject all actions");
        }
        for(var kind:ComponentStationKind.values()){
            resources.getResourceOrThrow(Identifier.withDefaultNamespace("textures/block/"+kind.texture+".png"));resources.getResourceOrThrow(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"blockstates/"+kind.id+".json"));resources.getResourceOrThrow(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"items/"+kind.id+".json"));
        }
        System.out.println("COMPONENT_STATION_UI_CHECK: PASS: "+scenes+" empty/active/locked/restored/corrupted layouts, real Minecraft font bounds, exact choice and lock hit regions, inventory isolation and all six placeholder block resources");
    }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private ComponentStationVisualRegression(){}
}
