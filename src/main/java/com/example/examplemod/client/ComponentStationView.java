package com.example.examplemod.client;

import java.util.List;
import com.example.examplemod.*;

/** One compact layout shared by all six temporary stations. */
public final class ComponentStationView {
    public record Row(String label,String value,ModifierScaling grade,int valueColor,boolean ancient,boolean locked,boolean enabled){}
    public record State(ComponentStationKind kind,boolean component,int quality,boolean corrupted,String status,List<Row> modifiers,List<Row> offers,boolean actionEnabled){}
    public static void draw(ExcavationScreenView.Canvas c,State s,int hovered){
        c.fill(0,0,320,242,0xff111820);c.fill(2,2,316,238,0xff26313b);c.fill(5,5,310,24,0xff151d25);c.fill(5,29,310,1,0xff000000|s.kind.color);
        c.text(s.kind.title,11,11,0xffe5e9e9);c.text(fit(c,s.status,224),84,21,0xffa6b7c3);
        c.text("Component",9,34,0xffaebcc5);slot(c,27,47);c.text("Quality",12,76,0xffaebcc5);
        c.text(s.component?s.quality+"%":"--",12,87,s.component?0xff000000|PickaxeTooltips.qualityColor(s.quality):0xff647581);
        if(s.corrupted)c.text("Corrupted",10,108,0xffee8a88);
        if(s.kind==ComponentStationKind.UNVEILER){
            for(int i=0;i<3;i++){
                int y=31+i*32;var row=i<s.offers.size()?s.offers.get(i):null;boolean enabled=row!=null&&row.enabled;card(c,84,y,226,27,enabled&&hovered==10+i,enabled?s.kind.color:0x4c5d69);
                c.text(row==null?"No ancient choice":fit(c,row.label,214),90,y+4,row==null?0xff748592:0xffd9e1e7);
                if(row!=null){c.text("Base "+row.value,90,y+15,0xff000000|row.valueColor);badge(c,row.grade,276,y+15);}
            }
            if(s.offers.isEmpty())c.text(s.component?"No hidden modifier":"Insert a recovered part",84,132,0xffa0afba);
        }else {
            for(int i=0;i<Math.min(8,s.modifiers.size());i++){
                var row=s.modifiers.get(i);int x=84+(i%2)*116,y=31+(i/2)*24;boolean clickable=s.kind==ComponentStationKind.LOCKER&&row.enabled;
                card(c,x,y,110,20,clickable&&hovered==20+i,row.locked?0x88b5d8:clickable?s.kind.color:0x4c5d69);
                c.text(fit(c,row.label,row.locked?87:101),x+4,y+2,0xffd9e1e7);if(row.locked)c.text("L",x+100,y+2,0xffa9d7ff);
                c.text(fit(c,row.value,75),x+4,y+11,0xff000000|row.valueColor);if(!row.ancient)badge(c,row.grade,x+88,y+11);
            }
            if(s.modifiers.isEmpty())c.text(s.component?"No modifiers":"Insert a recovered part",84,47,0xffa0afba);
            if(s.kind==ComponentStationKind.LOCKER)c.text(s.modifiers.stream().anyMatch(Row::locked)?"This part already has its permanent lock":"Choose one permanent lock",84,132,0xffa6b7c3);
            else {
                int fill=s.actionEnabled?(hovered==0?0xffd9e3e6:0xff000000|s.kind.color):0xff46525d;c.fill(84,130,226,17,fill);c.outline(84,130,226,17,0xff111820);
                String action=s.corrupted?"No further modifications":switch(s.kind){case ANULLER->"Annul random modifier";case CORRUPTOR->"Corrupt permanently";case SCALER->"Reroll unlocked scaling";case RESTORER->s.quality>=120?"Quality complete: 120%":"Restore +10 quality";default->"";};
                c.text(action,197-c.textWidth(action)/2,135,s.actionEnabled?0xff17212b:0xffa2acb4);
            }
        }
        c.text("Inventory",79,150,0xffbac6ce);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)slot(c,79+18*col,161+18*row);
        for(int col=0;col<9;col++)slot(c,79+18*col,219);
    }
    public static int buttonAt(State state,double x,double y){
        if(state.kind==ComponentStationKind.UNVEILER){for(int i=0;i<Math.min(3,state.offers.size());i++)if(state.offers.get(i).enabled&&inside(x,y,84,31+i*32,226,27))return 10+i;}
        else if(state.kind==ComponentStationKind.LOCKER){for(int i=0;i<Math.min(8,state.modifiers.size());i++)if(state.modifiers.get(i).enabled&&inside(x,y,84+(i%2)*116,31+(i/2)*24,110,20))return 20+i;}
        else if(state.actionEnabled&&inside(x,y,84,130,226,17))return 0;
        return -1;
    }
    private static boolean inside(double x,double y,int bx,int by,int w,int h){return x>=bx&&x<bx+w&&y>=by&&y<by+h;}
    private static void badge(ExcavationScreenView.Canvas c,ModifierScaling grade,int x,int y){c.text("["+grade.name()+"]",x,y,0xff000000|grade.color);}
    private static void card(ExcavationScreenView.Canvas c,int x,int y,int w,int h,boolean hovered,int accent){c.fill(x,y,w,h,hovered?0xff405365:0xff303e4b);c.outline(x,y,w,h,0xff000000|accent);}
    private static void slot(ExcavationScreenView.Canvas c,int x,int y){c.fill(x-1,y-1,18,18,0xff111820);c.fill(x,y,17,17,0xffc1c8cd);c.fill(x,y,16,16,0xff65727d);}
    private static String fit(ExcavationScreenView.Canvas c,String value,int width){if(c.textWidth(value)<=width)return value;while(!value.isEmpty()&&c.textWidth(value+"...")>width)value=value.substring(0,value.length()-1);return value+"...";}
    private ComponentStationView(){}
}
