package com.example.examplemod.client;

import com.example.examplemod.*;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.*;
import net.minecraft.client.gui.components.events.*;
import net.minecraft.client.input.*;

/** Uses native SDL button validation, press callbacks and container dispatch without a live audio engine. */
final class FieldGuideInputRegression {
    static void run(ExcavationScreenView.Canvas measure){
        require(InputConstants.MOUSE_BUTTON_LEFT==1,"Guide clicks must use SDL primary button 1");
        for(int[] origin:new int[][]{{0,6},{58,20},{200,90}}){
            var f=new Fixture(measure,origin[0],origin[1]);
            f.click("category:CREATURES",0);f.click("category:CREATURES",InputConstants.MOUSE_BUTTON_RIGHT);
            require(f.nav.page()==FieldGuideNavigation.Page.HOME,"Obsolete zero and secondary clicks must not open categories");
            f.click("category:CREATURES",InputConstants.MOUSE_BUTTON_LEFT);
            require(f.nav.page()==FieldGuideNavigation.Page.INDEX&&f.nav.category()==FieldGuide.Category.CREATURES,"Native category click must open its index");
            f.click("row:0",1);require(f.nav.page()==FieldGuideNavigation.Page.ARTICLE,"Native entry buttons must open articles");
            String first=f.nav.selected().id();f.click("next",1);require(f.nav.articlePage()==1,"Native next-page control must turn the reading spread");
            f.click("previous",1);require(f.nav.articlePage()==0,"Native previous-page control must turn back");
            f.click("previous",1);require(f.nav.articlePage()==0,"Disabled previous control must not wrap past the start");
            f.click("link:0",1);require(!f.nav.selected().id().equals(first),"Related-entry widgets must navigate");
            f.click("back",1);require(f.nav.selected().id().equals(first),"Native Back button must restore the previous article");
            f.click("home",1);f.click("category:MODIFIERS",1);require(f.nav.page()==FieldGuideNavigation.Page.TOPICS,"Modifier category must open topics");
            f.click("group:ALL",1);f.click("next",1);require(f.nav.listPage()==1,"Index pagination must work");f.click("row:0",1);f.click("back",1);require(f.nav.listPage()==1,"Back must preserve an index page");
            f.nav.query("magnet");f.refresh();f.click("row:0",1);f.click("back",1);require(f.nav.query().equals("magnet")&&f.nav.page()==FieldGuideNavigation.Page.INDEX,"Back must restore search results");
            f.click("clear",1);require(f.nav.query().isEmpty()&&f.nav.page()==FieldGuideNavigation.Page.HOME,"Clear-search button must return to contents");
            f.click("back",1);require(f.nav.query().equals("magnet")&&f.nav.page()==FieldGuideNavigation.Page.INDEX,"Back after Clear must restore the search");
            f.click("close",1);require(f.closed,"Native close control must activate");
        }
        System.out.println("FIELD_GUIDE_INPUT_CHECK: PASS: native Button click validation/press callbacks and container dispatch (audio omitted in headless datagen); SDL primary, zero/right rejection, category/topic/entry/link/Home/Back/Close/Clear, reading and index pagination at three screen origins");
    }
    private static final class Fixture extends AbstractContainerEventHandler {
        final FieldGuideNavigation nav=new FieldGuideNavigation();final List<GuiEventListener> children=new ArrayList<>();
        final ExcavationScreenView.Canvas measure;final int left,top;boolean closed;
        Fixture(ExcavationScreenView.Canvas measure,int left,int top){this.measure=measure;this.left=left;this.top=top;refresh();}
        @Override public List<? extends GuiEventListener> children(){return children;}
        void refresh(){children.clear();for(var control:FieldGuideView.controls(measure,nav))children.add(new FieldGuideButton(control,left,top,()->{
            if(control.id().equals("close"))closed=true;else FieldGuideView.activate(control.id(),nav,measure);
        },g->measure){
            // AbstractWidget.mouseClicked asks the live Minecraft singleton for its audio engine.
            // Datagen has no Minecraft instance: keep its native validation and press callback,
            // and skip only that audio lookup. Production uses the unmodified native method.
            @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){
                if(!active||!visible||!isValidClickButton(new MouseButtonInfo(event.button(),0))||!isMouseOver(event.x(),event.y()))return false;
                onClick(event,doubleClick);return true;
            }
        });}
        void click(String id,int button){
            var widget=children.stream().map(e->(FieldGuideButton)e).filter(e->e.control.id().equals(id)).findFirst().orElseThrow();
            mouseClicked(new MouseButtonEvent(widget.getX()+widget.getWidth()/2.0,widget.getY()+widget.getHeight()/2.0,new MouseButtonInfo(button,0)),false);
            refresh();
        }
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    private FieldGuideInputRegression(){}
}
