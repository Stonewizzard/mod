package com.example.examplemod.client;

import com.example.examplemod.ExcavationLayout;
import com.example.examplemod.ExcavationStudy;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.MouseButtonEvent;

/** The screen's mouse path, independently exercisable without a window or graphics context. */
final class ExcavationInput {
    interface Session {
        boolean inventoryView();
        void inventoryView(boolean value);
        boolean hasArtifact();
        int carriedTool();
        default int tier(){return 0;}
        default boolean recoverable(){return complete();}
        boolean complete();
        long gameTime();
        void send(int tool,int cell);
    }
    private final Session session;
    private int currentCell=-1,lastHammerCell=-1;
    private boolean excavating;
    private long nextTick=Long.MIN_VALUE;
    ExcavationInput(Session session){this.session=session;}
    private static boolean primary(MouseButtonEvent event){return event.button()==InputConstants.MOUSE_BUTTON_LEFT;}

    boolean click(MouseButtonEvent event,int left,int top){
        stop();double x=event.x()-left,y=event.y()-top;
        if(primary(event)){
            if(ExcavationLayout.inside(x,y,176,7,65,15)){session.inventoryView(false);return true;}
            if(ExcavationLayout.inside(x,y,245,7,65,15)){session.inventoryView(true);return true;}
            if(!session.inventoryView()&&session.hasArtifact()&&session.recoverable()&&ExcavationLayout.inside(x,y,257,214,50,14)){
                session.send(0,-1);return true;
            }
        }
        int cell=ExcavationLayout.cellAt(x,y);
        if(!session.inventoryView()&&cell>=0){
            // Always consume work-surface clicks so a carried item is never placed or dropped here.
            if(primary(event)&&session.hasArtifact()&&session.carriedTool()>0&&ExcavationStudy.coveredCell(cell)){
                currentCell=cell;excavating=true;tick();
            }
            return true;
        }
        return ExcavationLayout.toolAt(x,y)>0;
    }
    boolean drag(MouseButtonEvent event,int left,int top){
        int cell=ExcavationLayout.cellAt(event.x()-left,event.y()-top);
        if(excavating&&primary(event)){currentCell=cell;tick();return true;}
        return !session.inventoryView()&&cell>=0;
    }
    boolean release(MouseButtonEvent event){
        if(excavating&&primary(event)){stop();return true;}
        return false;
    }
    void hover(int cell){if(excavating)currentCell=cell;}
    void stop(){excavating=false;lastHammerCell=-1;}
    boolean excavating(){return excavating;}
    void tick(){
        int tool=session.carriedTool();
        if(tool==0||session.inventoryView()||!session.hasArtifact()){stop();return;}
        if(!excavating||tool==4||!ExcavationStudy.coveredCell(currentCell))return;
        if(tool!=2&&session.complete())return;
        long now=session.gameTime();if(now==Long.MIN_VALUE||now<nextTick)return;
        if(tool==3&&lastHammerCell==currentCell)return;
        session.send(tool,currentCell);nextTick=now+com.example.examplemod.ArchaeologyContent.cooldown(tool,session.tier());lastHammerCell=currentCell;
    }
}
