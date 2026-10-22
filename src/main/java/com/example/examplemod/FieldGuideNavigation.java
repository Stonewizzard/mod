package com.example.examplemod;

import java.util.*;

/** One place at a time: contents, a topic index, an entry list, or a reading spread. */
public final class FieldGuideNavigation {
    public enum Page { HOME, TOPICS, INDEX, ARTICLE }
    public static final int RESULTS_PER_PAGE=14;
    private Page page=Page.HOME;
    private String query="",selected="";
    private FieldGuide.Category category;
    private FieldGuide.Group group=FieldGuide.Group.ALL;
    private int listPage,articlePage;
    private List<FieldGuide.Entry> results=FieldGuide.entries();
    private record Visit(Page page,String query,FieldGuide.Category category,FieldGuide.Group group,String selected,int listPage,int articlePage){}
    private final Deque<Visit> history=new ArrayDeque<>();
    public Page page(){return page;}
    public String query(){return query;}
    public FieldGuide.Category category(){return category;}
    public FieldGuide.Group group(){return group;}
    public List<FieldGuide.Entry> results(){return results;}
    public int listPage(){return listPage;}
    public int articlePage(){return articlePage;}
    public FieldGuide.Entry selected(){return FieldGuide.entry(selected);}
    public boolean canBack(){return !history.isEmpty();}
    public int listPages(){return Math.max(1,(results.size()+RESULTS_PER_PAGE-1)/RESULTS_PER_PAGE);}
    public void home(){remember();reset();}
    private void reset(){page=Page.HOME;query="";category=null;group=FieldGuide.Group.ALL;selected="";listPage=articlePage=0;results=FieldGuide.entries();}
    /** Search is always global. Remember the page before typing, rather than every keystroke. */
    public void query(String value){
        String next=value==null?"":value.substring(0,Math.min(128,value.length()));
        if(next.equals(query))return;
        if(query.isEmpty())remember();
        query=next;category=null;group=FieldGuide.Group.ALL;filter();
        if(query.isEmpty())page=Page.HOME;
    }
    public void category(FieldGuide.Category value){
        remember();query="";category=value;group=FieldGuide.Group.ALL;filter();
        if(value==FieldGuide.Category.MODIFIERS)page=Page.TOPICS;
    }
    public void group(FieldGuide.Group value){remember();query="";category=FieldGuide.Category.MODIFIERS;group=value;filter();}
    private void filter(){results=FieldGuide.search(query,category,group);listPage=articlePage=0;selected="";page=Page.INDEX;}
    public void listPage(int direction){listPage=Math.clamp(listPage+direction,0,listPages()-1);}
    public void selectRow(int row){int index=listPage*RESULTS_PER_PAGE+row;if(row>=0&&row<RESULTS_PER_PAGE&&index<results.size())open(results.get(index).id());}
    public void turnPage(int direction,int pages){articlePage=Math.clamp(articlePage+direction,0,Math.max(1,pages)-1);}
    public void open(String id){if(FieldGuide.entry(id)==null)return;remember();selected=id;articlePage=0;page=Page.ARTICLE;}
    public void back(){
        if(history.isEmpty())return;var v=history.removeLast();page=v.page();query=v.query();category=v.category();group=v.group();selected=v.selected();listPage=v.listPage();articlePage=v.articlePage();results=FieldGuide.search(query,category,group);
    }
    private void remember(){if(history.size()>=32)history.removeFirst();history.addLast(new Visit(page,query,category,group,selected,listPage,articlePage));}
}
