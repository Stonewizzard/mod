package com.example.examplemod.client;
import java.util.*;
import com.example.examplemod.*;
/** A compact two-page journal. Search is a native EditBox layered over its reserved top row. */
public final class FieldGuideView {
    public static final int WIDTH=320,HEIGHT=228,LINES=14;
    public static final int NONE=-1,CLOSE=-2,LIST_PREV=-3,LIST_NEXT=-4,ARTICLE_PREV=-5,ARTICLE_NEXT=-6,BACK=-7,LINKS=-8,GROUP=-9,CLEAR=-10;
    public static final int TAB=100,ROW=200,LINK=300;
    public static List<String> wrap(ExcavationScreenView.Canvas c,List<String> paragraphs,int width){
        var result=new ArrayList<String>();for(var paragraph:paragraphs){String line="";
            for(String word:paragraph.split(" ")){
                if(!line.isEmpty()&&c.textWidth(line+" "+word)>width){result.add(line);line="";}
                while(c.textWidth(word)>width&&word.length()>1){int n=word.length()-1;while(n>1&&c.textWidth(word.substring(0,n))>width)n--;if(!line.isEmpty()){result.add(line);line="";}result.add(word.substring(0,n));word=word.substring(n);}
                line+=(line.isEmpty()?"":" ")+word;
            }if(!line.isEmpty())result.add(line);result.add("");
        }return List.copyOf(result);
    }
    public static int pages(ExcavationScreenView.Canvas c,FieldGuide.Entry entry){return entry==null?1:Math.max(1,(wrap(c,entry.paragraphs(),134).size()+LINES-1)/LINES);}
    public static void draw(ExcavationScreenView.Canvas c,FieldGuideNavigation n,int hovered){
        c.fill(0,0,320,228,0xff463429);c.fill(2,2,316,224,0xff87613c);c.fill(6,5,308,218,0xffdfc797);c.fill(9,8,302,20,0xff354a3f);
        c.text("Ancient Field Guide",15,14,0xfff2e5c4);button(c,296,11,12,13,"X",hovered==CLOSE,true);
        c.fill(12,31,296,20,0xff786e54);c.fill(15,33,270,16,0xff292d27);c.text(fit(c,n.query().isEmpty()?"Search the guide...":n.query(),262),19,37,0xffc4c7b7);button(c,291,33,14,16,"X",hovered==CLEAR,!n.query().isEmpty());c.fill(155,54,3,166,0xffaa8e60);c.fill(158,54,2,166,0xfff0ddb5);c.fill(162,54,147,155,0xffebd9ae);
        for(int i=0;i<8;i++){
            var category=i==0?null:FieldGuide.Category.values()[i-1];int x=12+(i%2)*70,y=56+(i/2)*15;
            boolean active=n.category()==category;c.fill(x,y,67,13,active?0xff3e6458:hovered==TAB+i?0xffa78d65:0xffb8a075);
            c.text(fit(c,category==null?"All":category.tab,59),x+4,y+3,active?0xffe9f6d5:0xff453d2c);
        }
        if(n.category()==FieldGuide.Category.MODIFIERS)button(c,12,117,137,13,n.group().label,hovered==GROUP,true);
        else c.text(n.results().size()+" entries",16,120,0xff68573b);
        for(int row=0;row<FieldGuideNavigation.RESULTS_PER_PAGE;row++){
            int index=n.listPage()*FieldGuideNavigation.RESULTS_PER_PAGE+row;if(index>=n.results().size())break;var entry=n.results().get(index);int y=133+row*12;boolean active=n.selected()!=null&&entry.id().equals(n.selected().id());
            c.fill(12,y,137,11,active?0xff3d5b50:hovered==ROW+row?0xffd1b987:0xffc5ad7d);c.text(fit(c,entry.title(),127),16,y+2,active?0xffd7efcf:0xff493d2d);
        }
        if(n.results().isEmpty()){c.text("No matching entries",15,138,0xff695137);c.text("Try All or clear search",15,150,0xff695137);}
        button(c,12,212,15,12,"<",hovered==LIST_PREV,n.listPages()>1);button(c,134,212,15,12,">",hovered==LIST_NEXT,n.listPages()>1);c.text((n.listPage()+1)+" / "+n.listPages(),61,215,0xff574831);
        button(c,164,212,31,12,"Back",hovered==BACK,n.canBack());
        var entry=n.selected();
        if(entry==null){c.text("No article selected",171,66,0xff645039);return;}
        var title=wrap(c,List.of(entry.title()),109);for(int i=0;i<Math.min(2,title.size());i++)c.text(title.get(i),194,58+i*9,0xff3a5147);
        if(n.related()){
            c.text("Related articles",170,85,0xff5a4a32);
            for(int i=0;i<entry.links().size();i++){
                var link=FieldGuide.entry(entry.links().get(i));if(link==null)continue;int y=101+i*25;c.fill(168,y,137,23,hovered==LINK+i?0xffc6b788:0xffd6c292);
                var label=wrap(c,List.of(link.title()),128);for(int line=0;line<Math.min(2,label.size());line++)c.text(label.get(line),172,y+3+line*9,0xff425b4c);
            }
            if(entry.links().isEmpty())c.text("No linked articles",170,102,0xff69563a);
        }else{
            var lines=wrap(c,entry.paragraphs(),134);int page=Math.floorMod(n.articlePage(),pages(c,entry));
            for(int i=0;i<LINES;i++){int index=page*LINES+i;if(index<lines.size())c.text(lines.get(index),170,80+i*9,0xff443c2e);}
        }
        button(c,197,212,13,12,"<",hovered==ARTICLE_PREV,!n.related()&&pages(c,entry)>1);button(c,249,212,13,12,">",hovered==ARTICLE_NEXT,!n.related()&&pages(c,entry)>1);
        c.text((n.articlePage()+1)+"/"+pages(c,entry),215,215,0xff574831);button(c,267,212,39,12,n.related()?"Read":"Links",hovered==LINKS,!entry.links().isEmpty());
    }
    public static int hit(double x,double y,FieldGuideNavigation n){
        if(inside(x,y,296,11,12,13))return CLOSE;
        for(int i=0;i<8;i++)if(inside(x,y,12+(i%2)*70,56+(i/2)*15,67,13))return TAB+i;
        if(n.category()==FieldGuide.Category.MODIFIERS&&inside(x,y,12,117,137,13))return GROUP;
        for(int i=0;i<FieldGuideNavigation.RESULTS_PER_PAGE;i++)if(n.listPage()*FieldGuideNavigation.RESULTS_PER_PAGE+i<n.results().size()&&inside(x,y,12,133+i*12,137,11))return ROW+i;
        if(inside(x,y,12,212,15,12))return LIST_PREV;if(inside(x,y,134,212,15,12))return LIST_NEXT;
        if(inside(x,y,291,33,14,16))return CLEAR;
        if(inside(x,y,164,212,31,12))return BACK;
        if(n.selected()==null)return NONE;
        if(n.related())for(int i=0;i<n.selected().links().size();i++)if(inside(x,y,168,101+i*25,137,23))return LINK+i;
        if(inside(x,y,197,212,13,12))return ARTICLE_PREV;if(inside(x,y,249,212,13,12))return ARTICLE_NEXT;if(inside(x,y,267,212,39,12))return LINKS;
        return NONE;
    }
    static boolean inside(double x,double y,int left,int top,int w,int h){return x>=left&&x<left+w&&y>=top&&y<top+h;}
    private static void button(ExcavationScreenView.Canvas c,int x,int y,int w,int h,String label,boolean hover,boolean enabled){c.fill(x,y,w,h,enabled?(hover?0xff587d69:0xff466452):0xffa79a78);c.text(fit(c,label,w-4),x+2,y+2,enabled?0xffefe8cc:0xff6e634a);}
    private static String fit(ExcavationScreenView.Canvas c,String text,int width){if(c.textWidth(text)<=width)return text;while(text.length()>1&&c.textWidth(text+"..")>width)text=text.substring(0,text.length()-1);return text+"..";}
    private FieldGuideView(){}
}
