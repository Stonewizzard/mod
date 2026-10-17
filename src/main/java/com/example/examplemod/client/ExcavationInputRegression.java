package com.example.examplemod.client;

import com.example.examplemod.ExcavationLayout;
import com.example.examplemod.ExcavationPayload;
import com.example.examplemod.ExcavationStudy;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

/** Real 26.3 mouse events exercise the same handler used by ArchaeologyTableScreen. */
final class ExcavationInputRegression {
    private static final int CELL=6*ExcavationStudy.COLS+14;
    static void run(){
        // Verified against MouseHandler and AbstractContainerScreen: 26.3 uses SDL's 1/2/3 codes.
        require(InputConstants.MOUSE_BUTTON_LEFT==1,"26.3 primary mouse input must match the native SDL button code");
        for(int[] origin:new int[][]{{0,2},{48,20},{200,60}}){
            var f=new Fixture(origin[0],origin[1]);
            require(f.click(250,12,1)&&f.inventory,"Native primary click must open Inventory");
            f.click(165,137,1);require(f.requests.isEmpty(),"Inventory view must not excavate behind its slots");
            require(f.click(180,12,1)&&!f.inventory,"Native primary click must return to Excavate");
            require(f.click(280,220,1)&&f.requests.size()==1&&f.last().tool()==0&&f.last().cell()==-1,"Native primary click must send Recover for server validation even before a fresh client state arrives");
            require(f.tool==1,"Tabs and Recover must preserve the carried tool");
            int before=f.requests.size();f.click(250,12,3);f.click(280,220,3);f.click(165,137,3);
            require(!f.inventory&&f.requests.size()==before,"Secondary clicks must not activate primary-only controls");
            f.click(250,12,0);f.click(165,137,0);
            require(!f.inventory&&f.requests.size()==before,"The obsolete GLFW zero code must not stand in for a primary click");
            require(!f.click(177,47,1)&&!f.click(85,220,1),"Native field-kit and hotbar slots must retain their normal inventory clicks");
        }
        for(int[] origin:new int[][]{{0,2},{48,20}})for(int[] point:new int[][]{{257,214},{306,227},{282,221}}){
            var f=new Fixture(origin[0],origin[1]);require(f.click(point[0],point[1],1)&&f.requests.size()==1&&f.last().tool()==0,"The entire painted recovery button must work with the actual screen offset");
        }
        for(int tool=1;tool<=3;tool++){
            var f=new Fixture(48,20);f.tool=tool;
            if(tool==2){f.study.work(1,CELL);f.study.work(1,CELL);}
            int depth=f.study.depth(CELL);
            require(f.click(165,137,1)&&f.requests.size()==1&&f.last().tool()==tool&&f.last().cell()==CELL,
                    "A primary click must request the carried tool at the correct specimen cell");
            require(f.study.depth(CELL)<depth,"A click routed through the screen handler must actually remove cover");
            f.input.release(f.event(165,137,1));int count=f.requests.size();f.time+=4;f.input.tick();
            require(f.requests.size()==count&&!f.input.excavating(),"Mouse release must stop excavation");
        }
        var drill=new Fixture(48,20);drill.click(165,137,1);
        drill.input.drag(drill.event(175,137,1),48,20);require(drill.requests.size()==1,"Mouse movement must respect the stroke cooldown");
        drill.time+=4;drill.input.tick();require(drill.requests.size()==2&&drill.last().cell()==CELL+1,"Held drill must work at the dragged-to cell");
        drill.tool=0;drill.time+=2;drill.input.tick();
        require(drill.requests.size()==2&&!drill.input.excavating(),"Putting the tool back must stop held strokes");
        var hammer=new Fixture(0,2);hammer.tool=3;hammer.click(165,137,1);hammer.time+=8;hammer.input.tick();
        require(hammer.requests.size()==1,"A hammer held still must not repeat strikes");
        hammer.input.drag(hammer.event(175,137,1),0,2);require(hammer.requests.size()==2,"Dragging the hammer to a new cell must deliver a deliberate strike");
        hammer.input.stop();hammer.time+=8;hammer.input.tick();require(hammer.requests.size()==2,"Closing the screen must stop held strokes");
        for(int tool:new int[]{0,4}){var f=new Fixture(0,2);f.tool=tool;f.click(165,137,1);f.time+=2;f.input.tick();
            require(f.requests.isEmpty(),"Empty hands and the inspection lens must never remove cover");}
        var absent=new Fixture(0,2);absent.artifact=false;absent.click(165,137,1);require(absent.requests.isEmpty(),"An empty table must not send excavation strokes");
        System.out.println("EXCAVATION_INPUT_CHECK: PASS: native SDL mouse events, Inventory/Excavate/Recover clicks, three screen origins, all tools, real cover removal, dragging, cooldown, release and item-return stops; obsolete button zero rejected");
    }
    private static final class Fixture implements ExcavationInput.Session {
        final int left,top;final ExcavationInput input=new ExcavationInput(this);
        final List<ExcavationPayload> requests=new ArrayList<>();
        ExcavationStudy study=new ExcavationStudy();boolean inventory,artifact=true;int tool=1;long time=100;
        Fixture(int left,int top){this.left=left;this.top=top;}
        MouseButtonEvent event(double x,double y,int button){return new MouseButtonEvent(left+x,top+y,new MouseButtonInfo(button,0));}
        boolean click(double x,double y,int button){return input.click(event(x,y,button),left,top);}
        ExcavationPayload last(){return requests.getLast();}
        @Override public boolean inventoryView(){return inventory;}
        @Override public void inventoryView(boolean value){inventory=value;}
        @Override public boolean hasArtifact(){return artifact;}
        @Override public int carriedTool(){return tool;}
        @Override public boolean complete(){return study.complete();}
        @Override public boolean recoverable(){return study.recoverable();}
        @Override public long gameTime(){return time;}
        @Override public void send(int tool,int cell){
            requests.add(new ExcavationPayload(19,tool,cell));
            if(tool==0)study=new ExcavationStudy();else study.work(tool,cell);
        }
    }
    private static void require(boolean valid,String message){if(!valid)throw new AssertionError(message);}
}

