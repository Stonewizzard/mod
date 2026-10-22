package com.example.examplemod.client;

import com.example.examplemod.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class FieldGuideScreen extends Screen {
    private final FieldGuideNavigation navigation;
    private final List<FieldGuideButton> buttons=new ArrayList<>();
    private EditBox search;private int left,top;private boolean syncing,dirty;
    public FieldGuideScreen(FieldGuideNavigation navigation){super(Component.literal("Ancient Field Guide"));this.navigation=navigation;}
    @Override protected void init(){
        left=(width-FieldGuideView.WIDTH)/2;top=(height-FieldGuideView.HEIGHT)/2;buttons.clear();
        search=new EditBox(font,left+21,top+36,262,12,Component.literal("Search all guide entries"));
        search.setBordered(false);search.setTextColor(0xff443b2d);search.setTextColorUneditable(0xff76634a);search.setMaxLength(128);search.setValue(navigation.query());search.setHint(Component.literal("Search all entries..."));
        search.setResponder(value->{if(!syncing){navigation.query(value);dirty=true;}});addRenderableWidget(search);rebuildButtons();
    }
    private ExcavationScreenView.Canvas canvas(GuiGraphicsExtractor g){return new ExcavationScreenView.Canvas(){
        @Override public void fill(int x,int y,int w,int h,int color){g.fill(left+x,top+y,left+x+w,top+y+h,color);}
        @Override public void texture(Identifier id,int x,int y,int w,int h,float u,float v,int sw,int sh,int tw,int th,int color){g.blit(RenderPipelines.GUI_TEXTURED,id,left+x,top+y,u,v,w,h,sw,sh,tw,th,color);}
        @Override public void text(String text,int x,int y,int color){g.text(font,text,left+x,top+y,color,false);}
        @Override public int textWidth(String text){return font.width(text);}
    };}
    private void rebuildButtons(){
        String focused=getFocused() instanceof FieldGuideButton b?b.control.id():"";
        for(var button:buttons)removeWidget(button);buttons.clear();
        for(var control:FieldGuideView.controls(canvas(null),navigation)){
            var button=new FieldGuideButton(control,left,top,()->{
                if(control.id().equals("close")){onClose();return;}
                FieldGuideView.activate(control.id(),navigation,canvas(null));
                syncing=true;search.setValue(navigation.query());syncing=false;dirty=true;
            },this::canvas);
            buttons.add(button);addRenderableWidget(button);if(control.id().equals(focused)&&button.active)setFocused(button);
        }
        dirty=false;
    }
    @Override public void tick(){super.tick();if(dirty)rebuildButtons();}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){
        if(dirty)rebuildButtons();FieldGuideView.draw(canvas(g),navigation);super.extractRenderState(g,mouseX,mouseY,partialTick);
    }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){
        if(dirty)rebuildButtons();return super.mouseClicked(event,doubleClick);
    }
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double scrollX,double scrollY){
        if(scrollY==0||!FieldGuideView.inside(mouseX-left,mouseY-top,12,55,296,154))return super.mouseScrolled(mouseX,mouseY,scrollX,scrollY);
        int direction=scrollY>0?-1:1;
        if(navigation.page()==FieldGuideNavigation.Page.ARTICLE)navigation.turnPage(direction,FieldGuideView.pages(canvas(null),navigation.selected()));
        else if(navigation.page()==FieldGuideNavigation.Page.INDEX)navigation.listPage(direction);else return false;
        dirty=true;return true;
    }
}
