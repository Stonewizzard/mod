package com.example.examplemod.client;

import java.util.*;
import com.example.examplemod.*;

/** Shared book layout; every painted control is also a real native Button on the screen. */
public final class FieldGuideView {
    public static final int WIDTH=320,HEIGHT=228,LINES=11,TEXT_WIDTH=126;
    private static final int INK=0xff443b2d,ACCENT=0xff32574c;
    public record Control(String id,int x,int y,int w,int h,String label,boolean enabled,String icon){}
    public static List<String> wrap(ExcavationScreenView.Canvas c,List<String> paragraphs,int width){
        var result=new ArrayList<String>();
        for(var paragraph:paragraphs){String line="";
            for(String word:paragraph.split(" ")){
                if(!line.isEmpty()&&c.textWidth(line+" "+word)>width){result.add(line);line="";}
                while(c.textWidth(word)>width&&word.length()>1){int n=word.length()-1;while(n>1&&c.textWidth(word.substring(0,n))>width)n--;if(!line.isEmpty()){result.add(line);line="";}result.add(word.substring(0,n));word=word.substring(n);}
                line+=(line.isEmpty()?"":" ")+word;
            }
            if(!line.isEmpty())result.add(line);result.add("");
        }
        if(!result.isEmpty())result.removeLast();return List.copyOf(result);
    }
    public static int pages(ExcavationScreenView.Canvas c,FieldGuide.Entry entry){return entry==null?1:Math.max(1,(wrap(c,entry.paragraphs(),TEXT_WIDTH).size()+LINES*2-1)/(LINES*2));}
    private static Control control(String id,int x,int y,int w,int h,String label,boolean enabled){return new Control(id,x,y,w,h,label,enabled,"");}
    public static List<Control> controls(ExcavationScreenView.Canvas c,FieldGuideNavigation n){
        var controls=new ArrayList<Control>();
        controls.add(control("back",12,10,32,15,"Back",n.canBack()));
        controls.add(control("home",48,10,35,15,"Home",n.page()!=FieldGuideNavigation.Page.HOME||!n.query().isEmpty()));
        controls.add(control("close",293,10,15,15,"X",true));
        controls.add(control("clear",291,33,17,17,"X",!n.query().isEmpty()));
        switch(n.page()){
            case HOME->{
                int i=0;for(var category:FieldGuide.Category.values()){
                    controls.add(new Control("category:"+category.name(),169,65+i++*20,134,18,category==FieldGuide.Category.START?"Getting started":category.tab,true,categoryIcon(category)));
                }
                controls.add(control("welcome",22,191,125,18,"Start here  >",true));
            }
            case TOPICS->{
                int i=0;for(var group:FieldGuide.Group.values()){
                    controls.add(control("group:"+group.name(),18+i/7*144,83+i%7*18,140,16,group==FieldGuide.Group.ALL?"All modifiers":group.label,true));i++;
                }
            }
            case INDEX->{
                for(int i=0;i<FieldGuideNavigation.RESULTS_PER_PAGE;i++){
                    int index=n.listPage()*FieldGuideNavigation.RESULTS_PER_PAGE+i;if(index>=n.results().size())break;var entry=n.results().get(index);
                    controls.add(control("row:"+i,18+i/7*144,83+i%7*18,140,16,entry.title(),true));
                }
                pagination(controls,n.listPage(),n.listPages());
            }
            case ARTICLE->{
                var entry=n.selected();if(entry==null)break;
                for(int i=0;i<entry.links().size();i++){
                    var target=FieldGuide.entry(entry.links().get(i));if(target!=null)controls.add(control("link:"+i,18+i%2*144,185+i/2*13,140,12,target.title(),true));
                }
                pagination(controls,n.articlePage(),pages(c,entry));
            }
        }
        return List.copyOf(controls);
    }
    private static void pagination(List<Control> c,int page,int pages){c.add(control("previous",18,213,24,12,"<",page>0));c.add(control("next",278,213,24,12,">",page+1<pages));}
    public static void activate(String id,FieldGuideNavigation n,ExcavationScreenView.Canvas measure){
        switch(id){
            case "back"->n.back();case "home"->n.home();case "clear"->n.query("");case "welcome"->n.open("welcome");
            case "previous","next"->{int direction=id.equals("next")?1:-1;if(n.page()==FieldGuideNavigation.Page.ARTICLE)n.turnPage(direction,pages(measure,n.selected()));else n.listPage(direction);}
            default->{
                if(id.startsWith("category:"))n.category(FieldGuide.Category.valueOf(id.substring(9)));
                else if(id.startsWith("group:"))n.group(FieldGuide.Group.valueOf(id.substring(6)));
                else if(id.startsWith("row:"))n.selectRow(Integer.parseInt(id.substring(4)));
                else if(id.startsWith("link:")&&n.selected()!=null){int i=Integer.parseInt(id.substring(5));if(i<n.selected().links().size())n.open(n.selected().links().get(i));}
            }
        }
    }
    public static void draw(ExcavationScreenView.Canvas c,FieldGuideNavigation n){
        // Stepped cover, page edges and a narrow stitched spine; quiet paper behind the text.
        c.fill(2,5,316,220,0xff182e29);c.fill(0,9,320,212,0xff233f36);c.fill(4,7,312,216,0xff456254);
        c.fill(8,28,304,194,0xffb49b72);c.fill(10,28,300,191,0xffdfcda3);c.fill(12,28,145,188,0xffeddfbc);c.fill(163,28,145,188,0xfff0e2c1);
        c.fill(156,28,2,188,0xffc8b48a);c.fill(158,28,4,188,0xffaa916b);c.fill(162,28,2,188,0xffdbc59b);
        c.fill(12,30,296,23,0xffeddfbc);c.fill(17,32,271,19,0xff847954);c.fill(18,33,269,17,0xfff7edcf);
        c.text("Ancient Field Guide",94,14,0xfff4e6c0);
        switch(n.page()){
            case HOME->{
                c.text("FIELD NOTES",24,66,ACCENT);c.fill(24,79,120,1,0xffaf9566);
                c.texture(net.minecraft.resources.Identifier.fromNamespaceAndPath(ExampleMod.MODID,"textures/item/ancient_field_guide.png"),65,86,32,32,0,0,32,32,32,32,-1);
                var intro=wrap(c,List.of("Discover ancient relics. Uncover their secrets. Rebuild something extraordinary."),120);
                for(int i=0;i<intro.size();i++)c.text(intro.get(i),24,127+i*10,INK);
                c.text("Contents",176,55,ACCENT);
            }
            case TOPICS->{c.text("Modifier reference",22,62,ACCENT);c.text("Choose a topic",22,73,0xff76634a);}
            case INDEX->{
                String heading=!n.query().isEmpty()?"Search results":n.category()==null?"All entries":n.category()==FieldGuide.Category.MODIFIERS?n.group().label:n.category().title;
                c.text(fit(c,heading,270),22,61,ACCENT);c.text(n.results().size()+" entries",22,72,0xff78684d);
                if(n.results().isEmpty()){c.text("Nothing found.",24,93,INK);c.text("Try a shorter search.",24,108,INK);}
                pageNumber(c,n.listPage(),n.listPages());
            }
            case ARTICLE->{
                var entry=n.selected();if(entry==null)break;
                var title=wrap(c,List.of(entry.title()),260);for(int i=0;i<Math.min(2,title.size());i++)c.text(title.get(i),22,59+i*10,ACCENT);
                var lines=wrap(c,entry.paragraphs(),TEXT_WIDTH);int page=Math.min(n.articlePage(),pages(c,entry)-1);
                for(int col=0;col<2;col++)for(int row=0;row<LINES;row++){
                    int index=page*LINES*2+col*LINES+row;if(index<lines.size())c.text(lines.get(index),24+col*144,82+row*9,INK);
                }
                c.fill(22,182,276,1,0xffc8b28c);pageNumber(c,page,pages(c,entry));
            }
        }
    }
    private static void pageNumber(ExcavationScreenView.Canvas c,int page,int pages){String text=(page+1)+" / "+pages;c.text(text,(WIDTH-c.textWidth(text))/2,216,0xff66553e);}
    public static void drawControl(ExcavationScreenView.Canvas c,Control b,boolean hover){
        int color=b.enabled()?(hover?0xff315d4f:0xff84764f):0xffb5a783;
        boolean toolbar=b.y()<30||b.id().equals("clear");
        if(toolbar){c.fill(b.x(),b.y(),b.w(),b.h(),b.enabled()?(hover?0xff5b7f68:0xff354e40):0xff516456);color=b.enabled()?0xfff5e9c7:0xff93a08a;}
        else if(hover&&b.enabled())c.fill(b.x(),b.y(),b.w(),b.h(),0xffdfcd9e);
        if(!toolbar)c.fill(b.x(),b.y()+b.h()-1,b.w(),1,0xffcbb78e);
        int indent=b.icon().isEmpty()?4:23;
        c.text(fit(c,b.label(),b.w()-indent-4),b.x()+indent,b.y()+(b.h()-8)/2,color);
    }
    public static String categoryIcon(FieldGuide.Category category){return switch(category){
        case START->"examplemod:example_block";case CREATURES->"minecraft:sniffer_egg";case ARTIFACTS->"examplemod:ancient_artifact";case EXCAVATION->"minecraft:brush";case EQUIPMENT->"examplemod:restored_ancient_pickaxe";case STATIONS->"examplemod:reconstruction_table";case MODIFIERS->"minecraft:enchanted_book";
    };}
    public static boolean inside(double x,double y,int left,int top,int w,int h){return x>=left&&x<left+w&&y>=top&&y<top+h;}
    public static String fit(ExcavationScreenView.Canvas c,String text,int width){if(c.textWidth(text)<=width)return text;while(text.length()>1&&c.textWidth(text+"..")>width)text=text.substring(0,text.length()-1);return text+"..";}
    private FieldGuideView(){}
}
