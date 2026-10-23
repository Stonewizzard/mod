package com.example.examplemod.client;
import com.example.examplemod.*;
import java.util.*;
/** Structural checks complement (and never substitute for) real-window screenshot review. */
final class GuideBookRegression {
    static void run(java.awt.image.BufferedImage font){
        var measure=new ExcavationVisualRegression.RecordingCanvas(font,396,224);int count=0;
        for(var design:GuideDesign.values()){
            var n=new FieldGuideNavigation();check(measure,design,n,false);
            for(var category:FieldGuide.Category.values()){n.category(category);check(measure,design,n,false);}
            for(var group:FieldGuide.Group.values()){n.group(group);check(measure,design,n,false);}
            for(var entry:FieldGuide.entries()){
                n.open(entry.id());
                for(int section=0;section<entry.sections().size();section++){
                    n.section(section);check(measure,design,n,false);count++;
                    var r=GuideBookView.body(design,n);
                    for(var page:GuideBookView.reading(measure,design,n))for(var line:page){
                        if(line.x()<r.x()||line.y()<r.y()||line.x()+measure.textWidth(line.text())>r.x()+r.w()||line.y()+8>r.y()+r.h())throw new AssertionError("Guide body overflow: "+design+" / "+entry.id());
                    }
                }
                check(measure,design,n,true);
            }
            n.query("zzzznotpresentzzzz");check(measure,design,n,false);
        }
        System.out.println("FIVE_BOOK_LAYOUT_CHECK: PASS: "+count+" sections, five designs, every entry, categories, topics, empty search, body bounds and control overlap");
    }
    private static void check(ExcavationScreenView.Canvas c,GuideDesign d,FieldGuideNavigation n,boolean related){
        var controls=GuideBookView.controls(c,d,n,related);var ids=new HashSet<String>();
        for(var b:controls){
            if(b.y()==52&&c.textWidth(b.label())>b.w()-8)throw new AssertionError("Truncated guide category: "+b.label());
            if(!ids.add(b.id()))throw new AssertionError("Duplicate guide control: "+d+" "+b.id());
            if(b.x()<0||b.y()<0||b.x()+b.w()>396||b.y()+b.h()>224)throw new AssertionError("Guide control outside frame");
            for(var other:controls)if(b!=other&&b.x()<other.x()+other.w()&&b.x()+b.w()>other.x()&&b.y()<other.y()+other.h()&&b.y()+b.h()>other.y())throw new AssertionError("Guide controls overlap: "+d+" "+n.page()+" "+b.id()+" / "+other.id());
        }
    }
    private GuideBookRegression(){}
}
