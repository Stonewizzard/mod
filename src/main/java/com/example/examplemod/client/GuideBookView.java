package com.example.examplemod.client;

import com.example.examplemod.*;
import java.util.*;
import static com.example.examplemod.FieldGuideNavigation.Page.*;

/** Workshop Manual: one searchable library with native item illustrations and controls. Coordinates are GUI pixels. */
public final class GuideBookView {
    public static final int WIDTH=396,HEIGHT=224;
    private static final int INK=0xff332e26,MUTED=0xff675d4b,PAPER=0xfff0e4c4,RULE=0xffc4b38e;
    public record Region(int x,int y,int w,int h){}
    public record Line(String text,int x,int y){}
    /** Registry IDs only; the live screen creates stacks after world item components are bound. */
    public record Illustration(String item,int x,int y){}
    private static final String[] PATH={"sniffers","archaeology_table","reconstruction","modifier_basics","influences","ascension"};
    private static final String[] TASKS={"Find your first artifact","Uncover a component","Build your first tool","Understand modifiers","Customize your parts","Level and ascend"};
    private static FieldGuideView.Control b(String id,int x,int y,int w,int h,String label,boolean enabled){return new FieldGuideView.Control(id,x,y,w,h,label,enabled,"");}
    private static FieldGuideView.Control icon(String id,int x,int y,int w,int h,String label,String item){return new FieldGuideView.Control(id,x,y,w,h,label,true,item);}
    public static List<Illustration> illustrations(FieldGuideNavigation n){
        return n.page()==ARTICLE&&n.selected()!=null?List.of(new Illustration(n.selected().icon(),116,57)):List.of();
    }
    public static Region body(GuideDesign d,FieldGuideNavigation n){
        int tabs=n.selected()==null?0:(Math.min(7,n.selected().sections().size())+2)/3;
        int y=80+tabs*15;return new Region(116,y,262,198-y);
    }
    /** Paragraphs stay intact whenever they fit. Long blocks continue at sentence boundaries when possible. */
    public static List<List<Line>> reading(ExcavationScreenView.Canvas c,GuideDesign d,FieldGuideNavigation n){
        var entry=n.selected();if(entry==null)return List.of(List.of());
        var r=body(d,n);int rows=Math.max(1,r.h()/10),row=0;
        var pages=new ArrayList<List<Line>>();var page=new ArrayList<Line>();
        var section=entry.sections().get(Math.min(n.section(),entry.sections().size()-1));
        for(String paragraph:section.paragraphs()){
            var initial=FieldGuideView.wrap(c,List.of(paragraph),r.w());
            var blocks=initial.size()>rows?List.of(paragraph.split("(?<=[.!?])\\s+")):List.of(paragraph);
            for(String block:blocks){
                var lines=FieldGuideView.wrap(c,List.of(block),r.w());
                if(row>0&&row+lines.size()>rows){pages.add(List.copyOf(page));page.clear();row=0;}
                for(String line:lines){
                    if(row>=rows){pages.add(List.copyOf(page));page.clear();row=0;}
                    page.add(new Line(line,r.x(),r.y()+row++*10));
                }
            }
            if(!paragraph.matches("T\\d+:.*"))row++;
        }
        if(!page.isEmpty())pages.add(List.copyOf(page));return pages.isEmpty()?List.of(List.of()):List.copyOf(pages);
    }
    public static int pages(ExcavationScreenView.Canvas c,GuideDesign d,FieldGuideNavigation n){return reading(c,d,n).size();}
    private static String categoryLabel(ExcavationScreenView.Canvas c,FieldGuide.Category category){
        if(c.textWidth(category.tab)<=62)return category.tab;
        return switch(category){case CREATURES->"Mobs";case ARTIFACTS->"Relics";case EXCAVATION->"Dig";case EQUIPMENT->"Gear";case MODIFIERS->"Mods";default->category.tab;};
    }
    private static String topicLabel(ExcavationScreenView.Canvas c,FieldGuide.Group group){
        if(c.textWidth(group.label)<=98)return group.label;
        return switch(group){case HARVEST->"Loot and XP";case UTILITY->"Universal";case NETHER->"Nether";case END->"End";default->group.label;};
    }
    private static String topicIcon(FieldGuide.Group group){return switch(group){
        case ALL->"minecraft:enchanted_book";case SPEED->"minecraft:golden_pickaxe";case HARVEST->"minecraft:diamond";
        case AREA->"minecraft:iron_pickaxe";case RESONANCE->"minecraft:amethyst_shard";case SURVIVAL->"minecraft:shield";
        case UTILITY->"minecraft:anvil";case NETHER->"minecraft:blaze_powder";case END->"minecraft:ender_pearl";
    };}
    public static List<FieldGuideView.Control> controls(ExcavationScreenView.Canvas c,GuideDesign d,FieldGuideNavigation n,boolean related){
        var out=new ArrayList<FieldGuideView.Control>();
        int backWidth=Math.max(33,c.textWidth("Back")+8),homeWidth=Math.max(37,c.textWidth("Home")+8),clearWidth=Math.max(38,c.textWidth("Clear")+8);
        out.add(b("back",10,7,backWidth,16,"Back",n.canBack()));out.add(b("home",14+backWidth,7,homeWidth,16,"Home",n.page()!=HOME||n.listPage()>0));
        out.add(b("close",365,7,21,16,"X",true));out.add(b("clear",386-clearWidth,29,clearWidth,18,"Clear",!n.query().isEmpty()));
        int categoryIndex=0;for(var category:FieldGuide.Category.values())out.add(icon("category:"+category.name(),14,66+categoryIndex++*18,89,17,categoryLabel(c,category),FieldGuideView.categoryIcon(category)));
        if(n.page()==ARTICLE&&n.selected()!=null){
            var e=n.selected();
            if(related){
                out.add(b("related",278,204,103,14,"Back to text",true));
                for(int i=0;i<e.links().size();i++){var target=FieldGuide.entry(e.links().get(i));out.add(icon("link:"+i,116,100+i*23,262,21,target.title(),target.icon()));}
            }else{
                int count=e.sections().size(),first=count>7?(n.section()/6)*6:0;
                int visible=count>7?Math.min(6,count-first):count;
                for(int i=0;i<visible;i++){int index=first+i;out.add(b("section:"+index,116+i%3*87,80+i/3*15,85,14,e.sections().get(index).title(),true));}
                if(count>7){int i=visible;out.add(b("more_sections",116+i%3*87,80+i/3*15,85,14,"More >",true));}
                int page=Math.min(n.articlePage(),pages(c,d,n)-1);
                out.add(b("previous",116,204,28,14,"<",page>0));out.add(b("next",186,204,28,14,">",page+1<pages(c,d,n)));
                out.add(b("related",278,204,103,14,"Related",!e.links().isEmpty()));
            }
        }else if(n.page()==HOME){
            for(int i=0;i<6;i++)out.add(icon("open:"+PATH[i],116,80+i*19,262,18,TASKS[i],FieldGuide.entry(PATH[i]).icon()));
        }else if(n.page()==TOPICS){
            int i=0;for(var group:FieldGuide.Group.values()){out.add(icon("group:"+group.name(),116+(i%2)*131,88+(i/2)*22,125,20,topicLabel(c,group),topicIcon(group)));i++;}
        }else if(n.page()==INDEX){
            for(int i=0;i<FieldGuideNavigation.RESULTS_PER_PAGE;i++){
                int index=n.listPage()*FieldGuideNavigation.RESULTS_PER_PAGE+i;if(index>=n.results().size())break;var e=n.results().get(index);
                out.add(icon("row:"+i,116,79+i*17,262,17,e.title(),e.icon()));
            }
            out.add(b("list_previous",116,204,28,14,"<",n.listPage()>0));out.add(b("list_next",212,204,28,14,">",n.listPage()+1<n.listPages()));
        }
        return List.copyOf(out);
    }
    public static void activate(String id,GuideDesign d,FieldGuideNavigation n,ExcavationScreenView.Canvas measure){
        if(id.equals("more_sections")){int next=(n.section()/6+1)*6;n.section(next<n.selected().sections().size()?next:0);return;}
        if(id.startsWith("open:")){n.open(id.substring(5));return;}
        if(id.startsWith("section:")){n.section(Integer.parseInt(id.substring(8)));return;}
        if(id.equals("list_previous")||id.equals("list_next")){n.listPage(id.equals("list_next")?1:-1);return;}
        if(id.equals("previous")||id.equals("next")){n.turnPage(id.equals("next")?1:-1,pages(measure,d,n));return;}
        switch(id){
            case "back"->n.back();case "home","clear"->n.home();
            default->{
                if(id.startsWith("category:"))n.category(FieldGuide.Category.valueOf(id.substring(9)));
                else if(id.startsWith("group:"))n.group(FieldGuide.Group.valueOf(id.substring(6)));
                else if(id.startsWith("row:"))n.selectRow(Integer.parseInt(id.substring(4)));
                else if(id.startsWith("link:")&&n.selected()!=null)n.open(n.selected().links().get(Integer.parseInt(id.substring(5))));
            }
        }
    }
    public static void draw(ExcavationScreenView.Canvas c,GuideDesign d,FieldGuideNavigation n,boolean related){
        c.fill(0,3,WIDTH,HEIGHT-6,0xff211c19);c.fill(2,1,WIDTH-4,HEIGHT-2,d.accent);c.fill(6,25,WIDTH-12,HEIGHT-29,0xffb19b71);c.fill(9,27,WIDTH-18,HEIGHT-32,PAPER);
        c.fill(14,29,330,18,0xff27352f);c.fill(15,30,328,1,0xff151e19);
        c.text(d.title,(WIDTH-c.textWidth(d.title))/2,11,0xfff8efda);
        c.fill(12,53,94,147,0xffdfd0ad);c.text("CONTENTS",18,55,d.accent);c.fill(109,53,1,145,RULE);
        if(n.page()==HOME){c.text("What would you like to do?",116,59,d.accent);c.text("Choose a task or use Contents.",116,71,MUTED);}
        else if(n.page()==ARTICLE&&n.selected()!=null){
            var e=n.selected();
            var title=FieldGuideView.wrap(c,List.of(e.title()),238);for(int i=0;i<Math.min(2,title.size());i++)c.text(title.get(i),140,57+i*10,d.accent);
            if(related)c.text("Read next",116,83,MUTED);
            else{
                var pages=reading(c,d,n);int page=Math.min(n.articlePage(),pages.size()-1);
                for(var line:pages.get(page))c.text(line.text(),line.x(),line.y(),INK);
                c.text((page+1)+"/"+pages.size(),150,208,MUTED);
            }
        }else{
            String title=n.page()==TOPICS?"Browse modifiers by topic":!n.query().isEmpty()?"Search: "+n.query():n.category()==null?"All entries":n.category()==FieldGuide.Category.MODIFIERS?n.group().label:n.category().title;
            c.text(FieldGuideView.fit(c,title,262),116,n.page()==TOPICS?75:67,d.accent);
            if(n.page()==INDEX){
                if(n.results().isEmpty())c.text("No matches. Try fewer words.",116,127,INK);
                c.text((n.listPage()+1)+" / "+n.listPages(),155,208,MUTED);
            }
        }
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
