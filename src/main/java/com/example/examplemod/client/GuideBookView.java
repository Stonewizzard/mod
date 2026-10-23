package com.example.examplemod.client;

import com.example.examplemod.*;
import java.util.*;
import static com.example.examplemod.FieldGuideNavigation.Page.*;

/** Five information architectures sharing content, navigation and native controls. All coordinates are GUI pixels. */
public final class GuideBookView {
    public static final int WIDTH=396,HEIGHT=224;
    private static final int INK=0xff332e26,MUTED=0xff675d4b,PAPER=0xfff0e4c4,RULE=0xffc4b38e;
    public record Region(int x,int y,int w,int h){}
    public record Line(String text,int x,int y){}
    private static final String[] PATH={"sniffers","archaeology_table","reconstruction","modifier_basics","influences","ascension"};
    private static final String[] TASKS={"Find your first artifact","Uncover a component","Build your first tool","Understand modifiers","Customize your parts","Level and ascend"};
    private static FieldGuideView.Control b(String id,int x,int y,int w,int h,String label,boolean enabled){return new FieldGuideView.Control(id,x,y,w,h,label,enabled,"");}
    private static FieldGuideView.Control icon(String id,int x,int y,int w,int h,String label,String item){return new FieldGuideView.Control(id,x,y,w,h,label,true,item);}
    public static Region body(GuideDesign d,FieldGuideNavigation n){
        int tabs=n.selected()==null?0:(Math.min(7,n.selected().sections().size())+2)/3;
        int y=tabsY(d)+tabs*15;
        return switch(d){
            case MANUAL->new Region(116,y,262,198-y);
            case REFERENCE->new Region(172,y,208,198-y);
            case ATLAS->new Region(146,85,230,111);
            default->new Region(20,y,356,198-y);
        };
    }
    private static String ribbonLabel(FieldGuide.Category category){return category==FieldGuide.Category.CREATURES?"Mobs":category==FieldGuide.Category.EQUIPMENT?"Gear":category.tab;}
    private static int tabsY(GuideDesign d){return d==GuideDesign.TRAIL||d==GuideDesign.REFERENCE?96:80;}
    /** Paragraphs stay intact whenever they fit on a column. Long blocks alone may continue onto a following page. */
    public static List<List<Line>> reading(ExcavationScreenView.Canvas c,GuideDesign d,FieldGuideNavigation n){
        var entry=n.selected();if(entry==null)return List.of(List.of());
        var r=body(d,n);int columns=d==GuideDesign.JOURNAL?2:1;
        int gap=18,colWidth=(r.w()-(columns-1)*gap)/columns,rows=Math.max(1,r.h()/10);
        var pages=new ArrayList<List<Line>>();var page=new ArrayList<Line>();int col=0,row=0;
        var section=entry.sections().get(Math.min(n.section(),entry.sections().size()-1));
        for(String paragraph:section.paragraphs()){
            var initial=FieldGuideView.wrap(c,List.of(paragraph),colWidth);
            // A long description continues at a sentence boundary, avoiding fragments across the gutter.
            var blocks=initial.size()>rows?List.of(paragraph.split("(?<=[.!?])\\s+")):List.of(paragraph);
            for(String block:blocks){
                var lines=FieldGuideView.wrap(c,List.of(block),colWidth);
                if(row>0&&row+lines.size()>rows){col++;row=0;}
                if(col>=columns){pages.add(List.copyOf(page));page.clear();col=row=0;}
                for(String line:lines){
                    if(row>=rows){col++;row=0;}if(col>=columns){pages.add(List.copyOf(page));page.clear();col=row=0;}
                    page.add(new Line(line,r.x()+col*(colWidth+gap),r.y()+row++*10));
                }
            }
            if(!paragraph.matches("T\\d+:.*"))row++;
        }
        if(!page.isEmpty())pages.add(List.copyOf(page));return pages.isEmpty()?List.of(List.of()):List.copyOf(pages);
    }
    public static int pages(ExcavationScreenView.Canvas c,GuideDesign d,FieldGuideNavigation n){return reading(c,d,n).size();}
    public static List<FieldGuideView.Control> controls(ExcavationScreenView.Canvas c,GuideDesign d,FieldGuideNavigation n,boolean related){
        var out=new ArrayList<FieldGuideView.Control>();
        out.add(b("back",10,7,33,16,"Back",n.canBack()));out.add(b("home",47,7,37,16,"Home",n.page()!=HOME||n.listPage()>0));
        out.add(b("close",365,7,21,16,"X",true));out.add(b("clear",348,29,38,18,"Clear",!n.query().isEmpty()));
        if(d==GuideDesign.MANUAL){int i=0;for(var category:FieldGuide.Category.values())out.add(b("category:"+category.name(),14,66+i++*18,89,17,category.tab,true));}
        if(d==GuideDesign.TRAIL||d==GuideDesign.REFERENCE){
            var categories=FieldGuide.Category.values();int x=14;
            int minimum=Arrays.stream(categories).mapToInt(category->c.textWidth(ribbonLabel(category))+8).sum();
            int extra=Math.max(0,(372-minimum)/categories.length);
            for(var category:categories){
                String label=ribbonLabel(category);int w=c.textWidth(label)+8+extra;
                out.add(b("category:"+category.name(),x,52,w,17,label,true));x+=w;
            }
        }
        if(d==GuideDesign.REFERENCE&&n.page()!=TOPICS){
            for(int i=0;i<8;i++){int index=n.listPage()*8+i;if(index>=n.results().size())break;out.add(b("row:"+i,14,76+i*15,146,14,n.results().get(index).title(),true));}
        }
        if(n.page()==ARTICLE&&n.selected()!=null){
            var e=n.selected();
            if(related){
                out.add(b("related",278,204,103,14,"Return to article",true));
                for(int i=0;i<e.links().size();i++){var target=FieldGuide.entry(e.links().get(i));out.add(icon("link:"+i,d==GuideDesign.MANUAL?116:d==GuideDesign.REFERENCE?172:20,100+i*23,d==GuideDesign.MANUAL?262:d==GuideDesign.REFERENCE?208:356,21,target.title(),target.icon()));}
            }else{
                int x=d==GuideDesign.MANUAL?116:d==GuideDesign.REFERENCE?172:20,w=d==GuideDesign.MANUAL?262:d==GuideDesign.REFERENCE?208:356;
                int count=e.sections().size(),first=count>7?(n.section()/6)*6:0;
                int visible=count>7?Math.min(6,count-first):count;
                for(int i=0;i<visible;i++){
                    int index=first+i;
                    if(d==GuideDesign.ATLAS)out.add(b("section:"+index,20,85+i*16,116,15,e.sections().get(index).title(),true));
                    else out.add(b("section:"+index,x+i%3*(w/3),tabsY(d)+i/3*15,w/3-2,14,e.sections().get(index).title(),true));
                }
                if(count>7){int i=visible;
                    if(d==GuideDesign.ATLAS)out.add(b("more_sections",20,85+i*16,116,15,"More >",true));
                    else out.add(b("more_sections",x+i%3*(w/3),tabsY(d)+i/3*15,w/3-2,14,"More >",true));
                }
                int page=Math.min(n.articlePage(),pages(c,d,n)-1);
                out.add(b("previous",d==GuideDesign.REFERENCE?172:20,204,28,14,"<",page>0));out.add(b("next",d==GuideDesign.REFERENCE?242:90,204,28,14,">",page+1<pages(c,d,n)));
                out.add(b("related",278,204,103,14,"Related articles",!e.links().isEmpty()));
            }
        }else if(n.page()==HOME){
            switch(d){
                case JOURNAL->{
                    out.add(icon("welcome",22,88,165,29,"Start your first dig",FieldGuideView.categoryIcon(FieldGuide.Category.START)));
                    out.add(b("open:book_controls",22,161,165,21,"How to use this book",true));
                    int i=0;for(var cat:FieldGuide.Category.values())out.add(icon("category:"+cat.name(),211,71+i++*19,164,18,cat==FieldGuide.Category.START?"Getting started":cat.tab,FieldGuideView.categoryIcon(cat)));
                }
                case MANUAL->{for(int i=0;i<6;i++)out.add(icon("open:"+PATH[i],116,80+i*19,262,18,TASKS[i],FieldGuide.entry(PATH[i]).icon()));}
                case TRAIL->{for(int i=0;i<6;i++)out.add(icon("open:"+PATH[i],20+(i%2)*181,91+(i/2)*35,175,32,(i+1)+". "+TASKS[i],FieldGuide.entry(PATH[i]).icon()));}
                case REFERENCE->{out.add(b("welcome",180,104,194,26,"New here? Start your first dig",true));out.add(b("topics",180,140,194,24,"Browse modifier topics",true));}
                case ATLAS->{int i=0;for(var cat:FieldGuide.Category.values()){out.add(icon("category:"+cat.name(),20+(i%2)*181,78+(i/2)*30,175,28,cat.title,FieldGuideView.categoryIcon(cat)));i++;}}
            }
        }else if(n.page()==TOPICS){
            int x=d==GuideDesign.MANUAL?116:20,w=d==GuideDesign.MANUAL?262:356;
            int i=0;for(var group:FieldGuide.Group.values()){out.add(b("group:"+group.name(),x+(i%2)*(w/2),88+(i/2)*22,w/2-6,20,group.label,true));i++;}
        }else if(n.page()==INDEX){
            if(d!=GuideDesign.REFERENCE)for(int i=0;i<8;i++){
                int index=n.listPage()*8+i;if(index>=n.results().size())break;var e=n.results().get(index);
                if(d==GuideDesign.JOURNAL||d==GuideDesign.ATLAS)out.add(icon("row:"+i,20+(i/4)*181,89+(i%4)*27,175,25,e.title(),e.icon()));
                else {int x=d==GuideDesign.MANUAL?116:20,w=d==GuideDesign.MANUAL?262:356;out.add(icon("row:"+i,x,83+i*14,w,13,e.title(),""));}
            }
        }
        if(n.page()==INDEX||d==GuideDesign.REFERENCE){
            int x=d==GuideDesign.REFERENCE?14:20;
            out.add(b("list_previous",x,204,28,14,"<",n.listPage()>0));out.add(b("list_next",x+96,204,28,14,">",n.listPage()+1<n.listPages()));
        }
        return List.copyOf(out);
    }
    public static void activate(String id,GuideDesign d,FieldGuideNavigation n,ExcavationScreenView.Canvas measure){
        if(id.equals("more_sections")){int next=(n.section()/6+1)*6;n.section(next<n.selected().sections().size()?next:0);return;}
        if(id.equals("topics")){n.category(FieldGuide.Category.MODIFIERS);return;}
        if(id.startsWith("open:")){n.open(id.substring(5));return;}
        if(id.startsWith("section:")){n.section(Integer.parseInt(id.substring(8)));return;}
        if(id.equals("list_previous")||id.equals("list_next")){n.listPage(id.equals("list_next")?1:-1);return;}
        if(id.equals("previous")||id.equals("next")){n.turnPage(id.equals("next")?1:-1,pages(measure,d,n));return;}
        switch(id){
            case "back"->n.back();case "home","clear"->n.home();case "welcome"->n.open("welcome");
            default->{
                if(id.startsWith("category:"))n.category(FieldGuide.Category.valueOf(id.substring(9)));
                else if(id.startsWith("group:"))n.group(FieldGuide.Group.valueOf(id.substring(6)));
                else if(id.startsWith("row:"))n.selectRow(Integer.parseInt(id.substring(4)));
                else if(id.startsWith("link:")&&n.selected()!=null)n.open(n.selected().links().get(Integer.parseInt(id.substring(5))));
            }
        }
    }
    public static void draw(ExcavationScreenView.Canvas c,GuideDesign d,FieldGuideNavigation n,boolean related){
        // Quiet page surfaces, pixel edges and a restrained leather frame. No decoration beneath body text.
        c.fill(0,3,WIDTH,HEIGHT-6,0xff211c19);c.fill(2,1,WIDTH-4,HEIGHT-2,d.accent);c.fill(6,25,WIDTH-12,HEIGHT-29,0xffb19b71);c.fill(9,27,WIDTH-18,HEIGHT-32,PAPER);
        c.fill(14,29,330,18,0xff27352f);c.fill(15,30,328,1,0xff151e19);
        c.text(d.title,(WIDTH-c.textWidth(d.title))/2,11,0xfff8efda);
        if(d==GuideDesign.JOURNAL){int gutter=n.page()==ARTICLE?body(d,n).y():52;c.fill(196,gutter,2,199-gutter,0xffd2c19b);c.fill(198,gutter,1,199-gutter,0xffe4d4b2);}
        if(d==GuideDesign.MANUAL){c.fill(12,53,94,147,0xffdfd0ad);c.text("CONTENTS",18,55,d.accent);c.fill(109,53,1,145,RULE);}
        if(d==GuideDesign.REFERENCE)c.fill(164,73,1,126,RULE);
        if(n.page()==HOME){
            switch(d){
                case JOURNAL->{c.text("FIELD NOTES",24,66,d.accent);c.text("Find. Uncover. Rebuild.",24,126,INK);c.text("An ancient craft awaits.",24,141,MUTED);c.text("CONTENTS",215,56,d.accent);}
                case MANUAL->{c.text("What would you like to do?",116,59,d.accent);c.text("Choose a task or use Contents.",116,71,MUTED);}
                case TRAIL->{c.text("YOUR FIRST ANCIENT TOOL",20,76,d.accent);c.text("Follow any step. Nothing is locked.",20,198,MUTED);}
                case REFERENCE->{c.text("QUICK REFERENCE",180,82,d.accent);c.text("Search any term above.",180,182,MUTED);}
                case ATLAS->{c.text("EXPLORE ANCIENT DISCOVERIES",20,60,d.accent);}
            }
        }else if(n.page()==ARTICLE&&n.selected()!=null){
            var e=n.selected();int x=d==GuideDesign.MANUAL?116:d==GuideDesign.REFERENCE?172:20,w=d==GuideDesign.MANUAL?262:d==GuideDesign.REFERENCE?208:356;
            // Two-line headings reserve their own space and never cross a book gutter.
            var title=FieldGuideView.wrap(c,List.of(e.title()),w);for(int i=0;i<Math.min(2,title.size());i++)c.text(title.get(i),x,(d==GuideDesign.TRAIL||d==GuideDesign.REFERENCE?73:57)+i*10,d.accent);
            if(related)c.text("Read next",x,d==GuideDesign.TRAIL||d==GuideDesign.REFERENCE?84:77,MUTED);
            else{
                var pages=reading(c,d,n);int page=Math.min(n.articlePage(),pages.size()-1);
                for(var line:pages.get(page))c.text(line.text(),line.x(),line.y(),INK);
                String counter=(page+1)+"/"+pages.size();c.text(counter,(d==GuideDesign.REFERENCE?206:54),208,MUTED);
            }
        }else{
            int x=d==GuideDesign.MANUAL?116:20;
            String title=n.page()==TOPICS?"Browse modifiers by topic":!n.query().isEmpty()?"Search: "+n.query():n.category()==null?"All entries":n.category()==FieldGuide.Category.MODIFIERS?n.group().label:n.category().title;
            if(d!=GuideDesign.REFERENCE||n.page()==TOPICS)c.text(FieldGuideView.fit(c,title,d==GuideDesign.MANUAL?262:356),x,n.page()==TOPICS?75:70,d.accent);
            if(n.page()==INDEX){
                if(d==GuideDesign.REFERENCE){c.text(n.results().size()+" matches",180,87,d.accent);c.text("Choose an entry on the left.",180,106,INK);}
                if(n.results().isEmpty()){c.text("No matches. Try fewer words.",d==GuideDesign.REFERENCE?180:x,127,INK);}
            }
        }
        if(n.page()==INDEX||d==GuideDesign.REFERENCE){String count=(n.listPage()+1)+" / "+n.listPages();c.text(count,d==GuideDesign.REFERENCE?53:59,208,MUTED);}
    }
    public static void drawControl(ExcavationScreenView.Canvas c,GuideDesign d,FieldGuideNavigation n,FieldGuideView.Control b,boolean hover){
        var activeCategory=n.page()==ARTICLE&&n.selected()!=null?n.selected().category():n.category();
        boolean selected=b.id().equals("section:"+n.section())||b.id().equals("category:"+(activeCategory==null?"":activeCategory.name()));
        int bg=hover&&b.enabled()?0xffd4c39a:selected?0xffd9c9a5:0xffe7d9b7;
        boolean toolbar=b.y()<27;
        c.fill(b.x(),b.y(),b.w(),b.h(),toolbar?(hover?0xff586b56:d.accent):bg);
        c.fill(b.x(),b.y()+b.h()-1,b.w(),1,toolbar?0xff1c3029:RULE);
        if(selected&&!toolbar)c.fill(b.x(),b.y(),2,b.h()-1,d.accent);
        if(hover&&b.enabled())c.fill(b.x(),b.y(),b.w(),1,toolbar?0xffd6d5b4:d.accent);
        int inset=b.icon().isEmpty()?4:23,available=b.w()-inset-4;
        int color=!b.enabled()?(toolbar?0xff96a293:0xff978970):toolbar?0xfffaf2d9:INK;
        var lines=FieldGuideView.wrap(c,List.of(b.label()),available);int max=b.h()>=24?2:1;
        if(lines.size()>max){lines=new ArrayList<>(lines.subList(0,max));lines.set(max-1,FieldGuideView.fit(c,String.join(" ",FieldGuideView.wrap(c,List.of(b.label()),available).subList(max-1,FieldGuideView.wrap(c,List.of(b.label()),available).size())),available));}
        int y=b.y()+(b.h()-lines.size()*9)/2+1;for(String line:lines){c.text(line,b.x()+inset,y,color);y+=9;}
    }
    private GuideBookView(){}
}
