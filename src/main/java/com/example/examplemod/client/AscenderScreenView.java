package com.example.examplemod.client;

import java.util.List;
import com.example.examplemod.AscenderMenu;
import com.example.examplemod.PickaxeProgression;

/** Plain machine UI; real slots and the cost list use one layout. */
public final class AscenderScreenView {
    public record Cost(String name,int count,int available){}
    public record State(boolean hasTool,PickaxeProgression.State progression,List<Cost> costs){}
    public static void draw(ExcavationScreenView.Canvas c,State state){
        c.fill(0,0,320,242,0xff373737);c.fill(1,1,318,240,0xffffffff);c.fill(3,3,314,236,0xffc6c6c6);c.fill(317,3,2,237,0xff555555);c.fill(3,239,316,2,0xff555555);
        c.text("Ascender",10,10,0xff404040);c.text("Tool",16,30,0xff404040);slot(c,18,44);
        if(state.hasTool()){
            var p=state.progression();c.text("Level "+p.level()+" / 100",45,43,0xff404040);c.text("Ascended "+p.ascensions()+"/10",45,54,0xff404040);
            String status=p.mastered()?"Mastered":p.needsAscension()?"Ascension ready":p.xp()+" / "+PickaxeProgression.xpRequired(p.level())+" XP";
            c.text(status,16,68,p.needsAscension()?0xff286341:0xff505050);
        }else c.text("Insert a pickaxe",45,48,0xff606060);
        c.text("Materials",16,80,0xff404040);
        for(int i=0;i<18;i++)slot(c,AscenderMenu.STORAGE_X+(i%6)*18,AscenderMenu.STORAGE_Y+(i/6)*18);
        c.fill(135,30,1,114,0xff909090);c.text("Ascension materials",145,30,0xff404040);
        if(state.hasTool()){
            var p=state.progression();
            if(p.mastered())c.text("Fully ascended",145,48,0xff286341);
            else {
                for(int i=0;i<state.costs().size();i++){
                    var cost=state.costs().get(i);String amount=cost.available()+"/"+cost.count();
                    c.text(fit(c,cost.name(),112),145,45+i*10,0xff404040);c.text(amount,309-c.textWidth(amount),45+i*10,cost.available()>=cost.count()?0xff286341:0xff90442c);
                }
                if(state.costs().isEmpty())c.text("No rolled modifiers",145,48,0xff606060);
            }
        }else c.text("Costs appear here",145,48,0xff606060);
        c.text("Inventory",AscenderMenu.PLAYER_X,150,0xff404040);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)slot(c,AscenderMenu.PLAYER_X+18*col,AscenderMenu.PLAYER_Y+18*row);
        for(int col=0;col<9;col++)slot(c,AscenderMenu.PLAYER_X+18*col,AscenderMenu.HOTBAR_Y);
    }
    private static String fit(ExcavationScreenView.Canvas c,String text,int width){while(c.textWidth(text)>width&&!text.isEmpty())text=text.substring(0,text.length()-1);return text;}
    private static void slot(ExcavationScreenView.Canvas c,int x,int y){c.fill(x-1,y-1,18,18,0xff373737);c.fill(x,y,17,17,0xffffffff);c.fill(x,y,16,16,0xff8b8b8b);}
    private AscenderScreenView(){}
}
