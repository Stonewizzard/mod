package com.example.examplemod.client;

import java.util.*;
import com.example.examplemod.*;

/** Presentation-neutral text measuring, control records and category icons. */
public final class FieldGuideView {
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
    public static String categoryIcon(FieldGuide.Category category){return switch(category){
        case START->"examplemod:ancient_field_guide";case CREATURES->"minecraft:sniffer_egg";case ARTIFACTS->"examplemod:ancient_artifact";case EXCAVATION->"minecraft:brush";case EQUIPMENT->"examplemod:restored_ancient_pickaxe";case STATIONS->"examplemod:reconstruction_table";case MODIFIERS->"minecraft:enchanted_book";
    };}
    public static boolean inside(double x,double y,int left,int top,int w,int h){return x>=left&&x<left+w&&y>=top&&y<top+h;}
    public static String fit(ExcavationScreenView.Canvas c,String text,int width){if(c.textWidth(text)<=width)return text;while(text.length()>1&&c.textWidth(text+"..")>width)text=text.substring(0,text.length()-1);return text+"..";}
    private FieldGuideView(){}
}
