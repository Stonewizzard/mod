package com.example.examplemod;
import java.util.*;
/** Search, categories and bounded reading history, independent of rendering and game state. */
public final class FieldGuideNavigation {
    public static final int RESULTS_PER_PAGE=6;
    private String query="",selected="welcome";private FieldGuide.Category category;private FieldGuide.Group group=FieldGuide.Group.ALL;
    private int listPage,articlePage;private boolean related;private List<FieldGuide.Entry> results=FieldGuide.entries();
    private record Visit(String query,FieldGuide.Category category,FieldGuide.Group group,String selected,int listPage,int articlePage,boolean related){}
    private final Deque<Visit> history=new ArrayDeque<>();
    public String query(){return query;}public FieldGuide.Category category(){return category;}public FieldGuide.Group group(){return group;}
    public List<FieldGuide.Entry> results(){return results;}public int listPage(){return listPage;}public int articlePage(){return articlePage;}public boolean related(){return related;}
    public FieldGuide.Entry selected(){return FieldGuide.entry(selected);}public boolean canBack(){return !history.isEmpty();}
    public int listPages(){return Math.max(1,(results.size()+RESULTS_PER_PAGE-1)/RESULTS_PER_PAGE);}
    public void query(String value){query=value==null?"":value.substring(0,Math.min(128,value.length()));filter();}
    public void category(FieldGuide.Category value){remember();category=value;group=FieldGuide.Group.ALL;filter();}
    public void nextGroup(){if(category!=FieldGuide.Category.MODIFIERS)return;remember();group=FieldGuide.Group.values()[(group.ordinal()+1)%FieldGuide.Group.values().length];filter();}
    private void filter(){results=FieldGuide.search(query,category,group);listPage=0;articlePage=0;related=false;selected=results.isEmpty()?"":results.getFirst().id();}
    public void listPage(int direction){listPage=Math.floorMod(listPage+direction,listPages());}
    public void selectRow(int row){int index=listPage*RESULTS_PER_PAGE+row;if(index>=0&&index<results.size()){remember();selected=results.get(index).id();articlePage=0;related=false;}}
    public void turnPage(int direction,int pages){articlePage=Math.floorMod(articlePage+direction,Math.max(1,pages));}
    public void related(boolean value){related=value;}
    public void open(String id){if(FieldGuide.entry(id)==null)return;remember();query="";category=null;group=FieldGuide.Group.ALL;results=FieldGuide.entries();selected=id;listPage=0;articlePage=0;related=false;for(int i=0;i<results.size();i++)if(results.get(i).id().equals(id)){listPage=i/RESULTS_PER_PAGE;break;}}
    public void back(){if(history.isEmpty())return;var v=history.removeLast();query=v.query();category=v.category();group=v.group();selected=v.selected();listPage=v.listPage();articlePage=v.articlePage();related=v.related();results=FieldGuide.search(query,category,group);}
    private void remember(){if(history.size()>=32)history.removeFirst();history.addLast(new Visit(query,category,group,selected,listPage,articlePage,related));}
}
