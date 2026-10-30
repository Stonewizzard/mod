package com.example.examplemod.client;

import com.example.examplemod.ReconstructionTableMenu;
import com.example.examplemod.ReconstructionPattern;
import com.example.examplemod.ArtifactQuality;

/** A compact smithing screen: pattern rack, assembly blueprint, then player storage. */
public final class ReconstructionScreenView {
    public static final int PICK_X=14,PICK_Y=33,PICK_W=90,PICK_H=15,BUILD_X=251,BUILD_Y=105,BUILD_W=54,BUILD_H=18;
    public record State(int pattern,boolean[] inserted,int[] scores,boolean output,double average,boolean spare){}
    public static boolean inside(double x,double y,int bx,int by,int w,int h){return x>=bx&&x<bx+w&&y>=by&&y<by+h;}
    public static int buttonAt(double x,double y){
        for(var p:ReconstructionPattern.values())if(inside(x,y,PICK_X,PICK_Y+(p.id-1)*16,PICK_W,PICK_H))return 10+p.id;
        return inside(x,y,BUILD_X,BUILD_Y,BUILD_W,BUILD_H)?1:-1;
    }
    public static void draw(ExcavationScreenView.Canvas c,State s){
        c.fill(0,0,320,242,0xff181a1d);c.fill(1,1,318,240,0xff55585c);c.fill(2,2,316,238,0xffc6c6c6);
        c.fill(2,2,315,1,0xffeeeeee);c.fill(2,2,1,237,0xffeeeeee);c.fill(317,3,1,237,0xff67676a);c.fill(3,239,314,1,0xff67676a);
        c.fill(7,7,306,19,0xff33383e);c.fill(7,25,306,1,0xff9c7350);c.text("Reconstruction",13,12,0xfff0e5d4);
        String title="PATTERNS";c.text(title,309-c.textWidth(title),13,0xffb9c2c6);
        panel(c,10,29,98,119,0xff43464b);
        for(var p:ReconstructionPattern.values()){
            int y=PICK_Y+(p.id-1)*16;boolean selected=p.id==s.pattern;
            c.fill(PICK_X,y,PICK_W,PICK_H,selected?0xff557481:0xff66686b);c.fill(PICK_X,y,PICK_W,1,selected?0xff9dc1c8:0xff929395);c.fill(PICK_X,y+14,PICK_W,1,0xff30353a);
            if(selected)c.fill(PICK_X,y,2,PICK_H,0xffdfab64);
            c.image(ExcavationScreenView.texture("item/"+p.outputId),18,y,14,14,32,32,-1);c.text(p.label,37,y+4,selected?0xffffffff:0xffe0e0df);
        }
        panel(c,112,29,198,119,0xff263e4b);
        for(int x=116;x<308;x+=8)c.fill(x,32,1,113,0xff2c4653);
        for(int y=32;y<146;y+=8)c.fill(115,y,193,1,0xff2c4653);
        var pattern=ReconstructionPattern.byId(s.pattern);int count=0;
        c.text(pattern==null?"Select a pattern":pattern.label,119,34,0xffdce9e7);
        if(pattern!=null){
            c.fill(188,60,2,40,0xff6c939a);
            if(pattern==ReconstructionPattern.PICKAXE){c.fill(165,62,48,1,0xff6c939a);c.fill(165,60,1,3,0xff6c939a);c.fill(213,60,1,3,0xff6c939a);}
        }
        for(int i=0;i<5;i++){
            int[] p=ReconstructionTableMenu.POSITIONS[i];String part=pattern==null?null:pattern.partId(i);
            if(part==null&&!s.spare)continue;
            slot(c,p[0],p[1],part!=null);
            if(part!=null&&!s.inserted[i])c.image(ExcavationScreenView.texture("item/"+part),p[0],p[1],16,16,32,32,0x66ffffff);
            if(part!=null&&s.inserted[i])count++;
            if(part!=null){String score=s.inserted[i]?(s.scores[i]<0?"--":String.valueOf(s.scores[i])):"";
                if(i<3)c.text(score,p[0]+(16-c.textWidth(score))/2,p[1]+18,0xffe3c894);else c.text(score,p[0]+22,p[1]+5,0xffe3c894);
            }
        }
        c.text(">",253,78,0xffadc7ce);slot(c,277,73,true);
        if(pattern!=null&&!s.output)c.image(ExcavationScreenView.texture("item/"+pattern.outputId),277,73,16,16,32,32,0x44ffffff);
        c.text("Result",269,60,0xffb5cbd0);
        boolean ready=pattern!=null&&count==pattern.parts.size()&&!s.output&&!s.spare;
        c.fill(BUILD_X,BUILD_Y,BUILD_W,BUILD_H,ready?0xff996d40:0xff484f55);c.fill(BUILD_X,BUILD_Y,BUILD_W,1,ready?0xffe4bc7b:0xff757c80);c.fill(BUILD_X,BUILD_Y+BUILD_H-1,BUILD_W,1,0xff151f28);
        c.text("Assemble",BUILD_X+3,BUILD_Y+5,ready?0xffffefcf:0xff8c989b);
        String status=s.output?"Ready to collect":s.spare?"Remove spare parts":pattern==null?"Choose a tool":count+"/"+pattern.parts.size()+" pieces";
        c.text(status,119,132,s.spare?0xffe6ab80:0xffd0dddd);
        if(pattern!=null&&!s.output&&!s.spare){String quality=ArtifactQuality.formatted(s.average)+"/100";c.text(quality,303-c.textWidth(quality),132,0xffd9be86);}
        c.fill(10,151,300,86,0xffa5a5a5);c.fill(10,151,300,1,0xff787878);c.text("Inventory",18,163,0xff404044);c.text("Hotbar",26,223,0xff404044);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)slot(c,79+18*col,161+18*row,true);
        for(int col=0;col<9;col++)slot(c,79+18*col,219,true);
        for(int x:new int[]{5,312})for(int y:new int[]{5,234}){c.fill(x,y,3,3,0xff5e5041);c.fill(x,y,2,1,0xffedc98b);}
    }
    private static void panel(ExcavationScreenView.Canvas c,int x,int y,int w,int h,int color){c.fill(x-1,y-1,w+2,h+2,0xffeeeeee);c.fill(x-1,y-1,w+2,1,0xff55595c);c.fill(x-1,y-1,1,h+2,0xff55595c);c.fill(x,y,w,h,color);}
    private static void slot(ExcavationScreenView.Canvas c,int x,int y,boolean active){c.fill(x-1,y-1,18,18,0xffeeeeee);c.fill(x-1,y-1,17,1,0xff373c42);c.fill(x-1,y-1,1,17,0xff373c42);c.fill(x,y,16,16,active?0xff777f82:0xff344d59);}
}
