package com.example.examplemod.client;
import java.util.*;
import com.example.examplemod.*;
/** Pixel-aligned notebook layout. Detail pages never overlap the native item slots. */
public final class AppraisalView {
    public static final int ROWS=6,LINES=10;
    public static List<String> wrap(ExcavationScreenView.Canvas c,List<String> paragraphs){
        var result=new ArrayList<String>();for(var paragraph:paragraphs){String line="";
            for(String word:paragraph.split(" ")){
                if(!line.isEmpty()&&c.textWidth(line+" "+word)>165){result.add(line);line="";}
                // Stable IDs and modded item names can have unusually long words.
                while(c.textWidth(word)>165&&word.length()>1){int n=word.length()-1;while(n>1&&c.textWidth(word.substring(0,n))>165)n--;if(!line.isEmpty()){result.add(line);line="";}result.add(word.substring(0,n));word=word.substring(n);}
                line+=(line.isEmpty()?"":" ")+word;
            }
            if(!line.isEmpty())result.add(line);result.add("");
        }return List.copyOf(result);
    }
    public static void draw(ExcavationScreenView.Canvas c,AppraisalReport.Report report,int selected,int listPage,int detailPage,int hovered){
        c.fill(0,0,320,242,0xff39332b);c.fill(2,2,316,238,0xffb49c73);c.fill(5,5,310,24,0xff34453e);
        c.text("Appraisal Table",12,11,0xfff2e4c4);c.text("Inventory",79,150,0xff453c2e);
        c.fill(8,32,111,114,0xff746a53);c.fill(123,32,189,114,0xffe2d3ad);
        slot(c,27,43);c.text("Inspect",52,47,0xfff2e4c4);
        int count=report.entries().size(),pages=Math.max(1,(count+ROWS-1)/ROWS);listPage=Math.floorMod(listPage,pages);
        for(int i=0;i<ROWS;i++){int index=listPage*ROWS+i;if(index>=count)break;int y=65+i*11;boolean active=index==selected;
            c.fill(10,y,107,10,active?0xff344e49:hovered==index?0xff8a8068:0xff635e4b);var entry=report.entries().get(index);c.text(fit(c,entry.name(),99),13,y+1,active?0xff9ce5d5:0xffe6ddc6);
        }
        button(c,10,132,"<",hovered==-2);button(c,101,132,">",hovered==-3);c.text((listPage+1)+" / "+pages,45,135,0xffeee2c8);
        var entry=report.entries().get(Math.clamp(selected,0,count-1));c.text(fit(c,entry.name(),175),129,36,0xff344e49);
        var lines=wrap(c,entry.lines());int detailPages=Math.max(1,(lines.size()+LINES-1)/LINES);detailPage=Math.floorMod(detailPage,detailPages);
        for(int i=0;i<LINES;i++){int index=detailPage*LINES+i;if(index<lines.size())c.text(lines.get(index),130,49+i*8,0xff423b30);}
        button(c,128,132,"<",hovered==-4);button(c,294,132,">",hovered==-5);c.text("Details "+(detailPage+1)+" / "+detailPages,178,135,0xff5e513b);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)slot(c,79+18*col,161+18*row);for(int col=0;col<9;col++)slot(c,79+18*col,219);
        c.text("READ ONLY",10,164,0xff554b3a);
    }
    static int hit(double x,double y,int listPage,int count){
        if(x>=10&&x<117&&y>=65&&y<131){int index=listPage*ROWS+(int)(y-65)/11;return index<count?index:-1;}
        if(y>=132&&y<144){if(x>=10&&x<24)return -2;if(x>=101&&x<115)return -3;if(x>=128&&x<142)return -4;if(x>=294&&x<308)return -5;}return -1;
    }
    private static String fit(ExcavationScreenView.Canvas c,String text,int width){if(c.textWidth(text)<=width)return text;while(text.length()>1&&c.textWidth(text+"..")>width)text=text.substring(0,text.length()-1);return text+"..";}
    private static void button(ExcavationScreenView.Canvas c,int x,int y,String label,boolean hover){c.fill(x,y,14,12,hover?0xff668678:0xff4b5c4c);c.text(label,x+4,y+2,0xfff1e5ca);}
    private static void slot(ExcavationScreenView.Canvas c,int x,int y){c.fill(x-1,y-1,18,18,0xff3c382e);c.fill(x,y,17,17,0xff8b826a);c.fill(x+1,y+1,16,16,0xffa3977a);}
    private AppraisalView(){}
}
