package com.example.examplemod.client;

import com.example.examplemod.ExcavationLayout;
import com.example.examplemod.ExcavationStudy;
import com.example.examplemod.ArtifactProfile;
import net.minecraft.resources.Identifier;

/** The same drawing plan drives the real screen and the headless visual review. */
public final class ExcavationScreenView {
    public interface Canvas {
        void fill(int x,int y,int width,int height,int color);
        void texture(Identifier id,int x,int y,int width,int height,float u,float v,int sourceWidth,int sourceHeight,int textureWidth,int textureHeight,int tint);
        void text(String text,int x,int y,int color);
        int textWidth(String text);
        default void image(Identifier id,int x,int y,int w,int h,int tw,int th,int tint){texture(id,x,y,w,h,0,0,tw,th,tw,th,tint);}
        default void outline(int x,int y,int w,int h,int color){fill(x,y,w,1,color);fill(x,y+h-1,w,1,color);fill(x,y,1,h,color);fill(x+w-1,y,1,h,color);}
    }
    public interface Labels {String get(String key,Object...args);}
    public record State(ExcavationStudy study,boolean rare,boolean inventory,int heldTool,int hoveredCell,boolean[] tools,String artifactName,int tier,boolean outputOccupied){
        public State(ExcavationStudy study,boolean rare,boolean inventory,int heldTool,int hoveredCell,boolean[] tools,String artifactName){this(study,rare,inventory,heldTool,hoveredCell,tools,artifactName,0,false);}
    }
    public static Identifier texture(String path){return Identifier.fromNamespaceAndPath("examplemod","textures/"+path+".png");}
    private static final Identifier PAPER=texture("block/table_parchment"),MAT=texture("block/table_mat"),RELIC=texture("gui/excavation/study_relic");
    private static final String[] GUIDES={"","archaeology_drill","brush_guide","archaeology_hammer","magnifying_glass"};
    public static void draw(Canvas c,State s,Labels labels){
        c.fill(0,0,320,236,0xff38281c);c.fill(2,2,316,232,0xffb08c4c);
        c.image(PAPER,4,4,312,228,32,32,-1);c.fill(5,5,310,18,0xff354c39);
        c.text(labels.get("title"),11,10,0xfff0e6c7);
        tab(c,176,labels.get("excavate"),!s.inventory);tab(c,245,labels.get("inventory"),s.inventory);
        c.text(labels.get("artifact"),16,27,0xff51432e);c.text(labels.get("field_kit")+(s.heldTool>0?" / "+new String[]{"I","II","III"}[s.tier]:""),159,27,0xff51432e);
        c.fill(16,35,36,30,0xffa98e59);c.fill(17,36,34,28,0xff38513d);slot(c,26,40,true);
        c.text(">",54,45,0xff806541);slot(c,65,40,false);
        if(s.study!=null){
            c.text(fit(c,s.study.profile().rarityName(),65),89,39,0xff51432e);
            c.text(fit(c,s.study.profile().matrix().label,65),89,51,0xff51432e);
        }else c.text(labels.get("recovered"),89,45,0xff51432e);
        for(int tool=1;tool<=4;tool++){
            int x=159+(tool-1)*38;
            boolean selected=s.heldTool==tool;
            c.fill(x,34,36,33,selected?0xff8caf96:0xffb29b6b);c.fill(x+1,35,34,31,selected?0xffd2d3a7:0xffd5c294);
            var pos=ExcavationLayout.SLOTS[tool];slot(c,pos[0],pos[1],false);
            if(!s.tools[tool])c.image(texture("item/"+GUIDES[tool]),pos[0],pos[1],16,16,32,32,0x55ffffff);
            String name=labels.get("tool."+tool);c.text(name,x+(36-c.textWidth(name))/2,57,s.tools[tool]||selected?0xff374f3a:0xff8e866d);
            if(selected)c.fill(x+2,65,32,2,0xff468473);
        }
        c.fill(17,69,286,126,0xff9c7b48);c.image(MAT,19,71,282,122,32,32,-1);
        if(s.inventory){
            c.text(labels.get("inventory"),79,97,0xffe1dfbe);
            for(int row=0;row<3;row++)for(int col=0;col<9;col++)slot(c,79+col*18,111+row*18,false);
            centered(c,labels.get("inventory_hint"),177,0xffd1d5b2);
            c.text(labels.get("stored_hint"),20,198,0xff5c583e);
        }else if(s.study==null){
            c.image(texture("item/magnifying_glass"),144,91,32,32,32,32,0x557aa18a);
            centered(c,labels.get("empty_board"),140,0xffd8dec2);centered(c,labels.get("empty_hint"),154,0xffa4bc9d);
            c.text(labels.get("ready"),20,198,0xff5c583e);
        }else {
            board(c,s);
            if(s.heldTool==0){
                String hint=labels.get("hold_tool_hint");int w=c.textWidth(hint)+12;
                c.fill(160-w/2,76,w,13,0xe8344e3c);c.text(hint,166-w/2,79,0xffe7dfb5);
            }
            int progress=s.study.progress(),condition=s.study.condition();
            c.text(labels.get("progress",progress),20,198,0xff355743);
            c.text(labels.get("condition",condition),190,198,condition<50?0xff9d4a37:0xff355743);
            c.fill(20,208,112,2,0xffa5956d);c.fill(20,208,112*progress/100,2,0xff468473);
            c.fill(190,208,110,2,0xffa5956d);c.fill(190,208,110*condition/100,2,condition<50?0xffb76746:0xff6d9a6b);
            if(s.study.recoverable()){
                var text=labels.get(s.outputOccupied?"output_full":s.study.condition()==0?"ruined":"complete");int w=c.textWidth(text)+12;
                c.fill(160-w/2,174,w,14,0xe8344e3c);c.text(text,166-w/2,177,0xffe7dfb5);
            }
        }
        c.text(labels.get("hotbar"),23,217,0xff66583b);
        for(int col=0;col<9;col++)slot(c,79+col*18,213,false);
        if(s.study!=null && !s.inventory){
            boolean enabled=s.study.recoverable()&&!s.outputOccupied;
            c.fill(257,214,50,14,0xffac8d55);c.fill(258,215,48,12,enabled?0xffe0cd9c:0xffb8aa87);
            String action=labels.get(s.study.condition()<60?"salvage":"recover");
            c.text(action,282-c.textWidth(action)/2,217,enabled?0xff355743:0xff82785c);
        }
        for(int x:new int[]{5,312})for(int y:new int[]{5,228})c.fill(x,y,3,3,0xffd3ae63);
    }
    private static void board(Canvas c,State s){
        int bx=ExcavationLayout.BOARD_X,by=ExcavationLayout.BOARD_Y;
        var find=s.study.profile().find();
        c.image(find==ArtifactProfile.Find.LEGACY||find==ArtifactProfile.Find.ALLOY?RELIC:texture("gui/excavation/"+find.path()),bx,by,280,120,128,64,-1);
        for(int cell=0;cell<ExcavationStudy.CELLS;cell++){
            if(!ExcavationStudy.coveredCell(cell))continue;
            int col=cell%ExcavationStudy.COLS,row=cell/ExcavationStudy.COLS,x=bx+col*10,y=by+row*10,depth=s.study.depth(cell);
            if(depth>0){
                var material=depth==1?texture("gui/excavation/"+(s.rare?"ash":"silt")):Identifier.withDefaultNamespace("textures/block/"+s.study.profile().matrix().texture+".png");
                int size=depth==1?32:16;
                c.texture(material,x,y,10,10,(col*4)%size,(row*4)%size,4,4,size,size,depth>=6?0xffc3c7cf:depth==2?0xffd2c9b6:-1);
                if(depth==2){c.fill(x+2,y+3,4,1,0x504b352b);c.fill(x+5,y+4,1,3,0x504b352b);}
                if(row==0||!ExcavationStudy.coveredCell(cell-ExcavationStudy.COLS)||s.study.depth(cell-ExcavationStudy.COLS)<depth)c.fill(x,y,10,1,0x709d8b64);
                if(row==ExcavationStudy.ROWS-1||!ExcavationStudy.coveredCell(cell+ExcavationStudy.COLS)||s.study.depth(cell+ExcavationStudy.COLS)<depth)c.fill(x,y+9,10,1,0x80433225);
            }else if(s.study.scar(cell)>0){c.fill(x+2,y+3,5,1,0xc094573e);c.fill(x+5,y+4,1,4,0xc094573e);}
            if(s.hoveredCell>=0 && s.heldTool>=1 && s.heldTool<=4 && ExcavationStudy.affects(s.heldTool,s.tier,s.hoveredCell,cell)){
                boolean danger=s.heldTool!=2 && s.heldTool!=4 && !(s.heldTool==1&&s.tier>0) && s.study.inclusion(cell) && depth<=ExcavationStudy.power(s.heldTool,s.tier);
                c.fill(x,y,10,10,danger?0x48e9a077:0x2886d7bb);
                if(s.heldTool==4 && s.study.inclusion(cell))c.outline(x+1,y+1,8,8,0x9991d4c3);
                else c.outline(x,y,10,10,danger?0x90d18b62:0x708bd1b1);
            }
        }
    }
    private static void tab(Canvas c,int x,String text,boolean active){
        c.fill(x,7,65,15,active?0xffe0cd9c:0xff6d7859);c.text(text,x+(65-c.textWidth(text))/2,11,active?0xff3e563c:0xffdeddb9);
    }
    private static void centered(Canvas c,String text,int y,int color){c.text(text,160-c.textWidth(text)/2,y,color);}
    private static String fit(Canvas c,String text,int width){
        if(c.textWidth(text)<=width)return text;
        while(!text.isEmpty()&&c.textWidth(text+"..")>width)text=text.substring(0,text.offsetByCodePoints(text.length(),-1));
        return text+"..";
    }
    private static void slot(Canvas c,int x,int y,boolean mat){
        c.fill(x-1,y-1,18,18,0xff806541);c.fill(x,y,16,16,mat?0xff263c2e:0xff8e805f);
        c.fill(x,y,16,1,0xff584c35);c.fill(x,y,1,16,0xff584c35);c.fill(x+1,y+16,16,1,0xffedddad);
    }
}
