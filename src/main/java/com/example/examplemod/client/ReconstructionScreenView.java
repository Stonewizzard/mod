package com.example.examplemod.client;

import com.example.examplemod.ReconstructionTableMenu;
import com.example.examplemod.ArtifactQuality;

/** Shared rendering/hit regions make the assembly layout independently reviewable. */
public final class ReconstructionScreenView {
    public static final int PICK_X=18,PICK_Y=49,PICK_W=78,PICK_H=19,BUILD_X=251,BUILD_Y=101,BUILD_W=54,BUILD_H=18;
    public record State(boolean selected,boolean[] inserted,int[] scores,boolean output,double average){}
    private static final String[] ICONS={"pickaxe_left_tip","pickaxe_head_bridge","pickaxe_right_tip","ancient_tool_connector","ancient_tool_handle"};
    public static boolean inside(double x,double y,int bx,int by,int w,int h){return x>=bx&&x<bx+w&&y>=by&&y<by+h;}
    public static int buttonAt(double x,double y){return inside(x,y,PICK_X,PICK_Y,PICK_W,PICK_H)?0:inside(x,y,BUILD_X,BUILD_Y,BUILD_W,BUILD_H)?1:-1;}
    public static void draw(ExcavationScreenView.Canvas c,State s){
        c.fill(0,0,320,236,0xff38281c);c.fill(2,2,316,232,0xffb08c4c);c.image(ExcavationScreenView.texture("block/table_parchment"),4,4,312,228,32,32,-1);
        c.fill(5,5,310,18,0xff354c39);c.text("Reconstruction Table",11,10,0xfff0e6c7);
        c.fill(12,32,90,96,0xffbaa171);c.fill(13,33,88,94,0xffe0cd9c);c.text("Patterns",19,37,0xff51432e);
        c.fill(PICK_X,PICK_Y,PICK_W,PICK_H,s.selected?0xff597d67:0xffa18d62);c.text("Pickaxe",36,55,s.selected?0xfff0e6c7:0xff3c4e37);
        c.image(ExcavationScreenView.texture("item/restored_ancient_pickaxe"),37,78,32,32,32,32,-1);
        c.fill(110,32,198,96,0xffa98e59);c.image(ExcavationScreenView.texture("block/table_mat"),112,34,194,92,32,32,-1);
        c.text(s.selected?"Pickaxe assembly":"Select a pattern",121,34,0xffe9e0bc);
        int count=0;
        for(int i=0;i<5;i++){
            int[] p=ReconstructionTableMenu.POSITIONS[i];slot(c,p[0],p[1]);
            if(!s.inserted[i])c.image(ExcavationScreenView.texture("item/"+ICONS[i]),p[0],p[1],16,16,32,32,s.selected?0x55ffffff:0x25ffffff);
            else count++;
            if(s.selected){String score=s.inserted[i]?(s.scores[i]<0?"--":String.valueOf(s.scores[i])):"";
                if(i<3)c.text(score,p[0]+(16-c.textWidth(score))/2,p[1]+18,0xffe1d9b5);else c.text(score,p[0]+22,p[1]+4,0xffe1d9b5);
            }
        }
        c.text(">",252,75,0xffd8cea9);slot(c,274,70);
        boolean ready=s.selected&&count==5&&!s.output;
        c.fill(BUILD_X,BUILD_Y,BUILD_W,BUILD_H,ready?0xff496f59:0xff9e956e);c.text("Assemble",BUILD_X+3,BUILD_Y+5,ready?0xfff0e6c7:0xffd0c8aa);
        c.text(s.output?"Take the assembled pickaxe":s.selected?(count+"/5 pieces"+(count==5?"   Score: "+ArtifactQuality.formatted(s.average)+"/100":"")):"Choose a pattern to begin",16,131,0xff4e563a);
        c.text("Inventory",20,149,0xff65583c);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)slot(c,79+18*col,147+18*row);
        c.text("Hotbar",24,217,0xff65583c);for(int col=0;col<9;col++)slot(c,79+18*col,213);
        for(int x:new int[]{5,312})for(int y:new int[]{5,228})c.fill(x,y,3,3,0xffd3ae63);
    }
    private static void slot(ExcavationScreenView.Canvas c,int x,int y){c.fill(x-1,y-1,18,18,0xff806541);c.fill(x,y,16,16,0xff8e805f);c.fill(x,y,16,1,0xff584c35);c.fill(x,y,1,16,0xff584c35);c.fill(x+1,y+16,16,1,0xffedddad);}
}
