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
    private final FieldGuideNavigation navigation;private final GuideDesign design;
    private final List<GuideBookButton> buttons=new ArrayList<>();
    private EditBox search;private int left,top;private double horizontalScale=1;private boolean syncing,dirty,related;
    public FieldGuideScreen(FieldGuideNavigation navigation){this(navigation,GuideDesign.MANUAL);}
    public FieldGuideScreen(FieldGuideNavigation navigation,GuideDesign design){super(Component.literal(design.title));this.navigation=navigation;this.design=design;}
    public FieldGuideNavigation navigation(){return navigation;}
    public GuideDesign design(){return design;}
    public boolean related(){return related;}
    public List<FieldGuideView.Control> controls(){return GuideBookView.controls(canvas(null),design,navigation,related);}
    /** Actual native widget bounds, also observed by the development OS-input harness. */
    public List<FieldGuideView.Control> liveControls(){return controls().stream().map(c->new FieldGuideView.Control(c.id(),px(c.x()),top+c.y(),px(c.x()+c.w())-px(c.x()),c.h(),c.label(),c.enabled(),c.icon())).toList();}
    public int searchX(){return search.getX()+Math.min(70,search.getWidth()/2);}
    public int searchY(){return search.getY()+5;}
    private int px(int x){return left+(int)Math.round(x*horizontalScale);}
    @Override protected void init(){
        int fittedWidth=Math.min(GuideBookView.WIDTH,width-8);horizontalScale=fittedWidth/(double)GuideBookView.WIDTH;
        left=(width-fittedWidth)/2;top=(height-GuideBookView.HEIGHT)/2;buttons.clear();
        // Native white text on a dark inset avoids the old dark-text/drop-shadow overprint.
        search=new EditBox(font,px(20),top+34,px(336)-px(20),11,Component.literal("Search the whole guide"));
        search.setBordered(false);search.setTextColor(0xfff4efde);search.setTextColorUneditable(0xffc1c8bc);search.setMaxLength(128);search.setValue(navigation.query());search.setHint(Component.empty());
        search.setResponder(value->{if(!syncing){navigation.query(value);related=false;dirty=true;}});addRenderableWidget(search);rebuildButtons();
    }
    private ExcavationScreenView.Canvas canvas(GuiGraphicsExtractor g){return new ExcavationScreenView.Canvas(){
        @Override public void fill(int x,int y,int w,int h,int color){g.fill(px(x),top+y,px(x+w),top+y+h,color);}
        @Override public void texture(Identifier id,int x,int y,int w,int h,float u,float v,int sw,int sh,int tw,int th,int color){g.blit(RenderPipelines.GUI_TEXTURED,id,px(x),top+y,u,v,px(x+w)-px(x),h,sw,sh,tw,th,color);}
        @Override public void text(String text,int x,int y,int color){g.text(font,text,px(x),top+y,color,false);}
        @Override public int textWidth(String text){return (int)Math.ceil(font.width(text)/horizontalScale);}
    };}
    private void rebuildButtons(){
        String focused=getFocused() instanceof GuideBookButton b?b.control.id():"";
        if(getFocused() instanceof GuideBookButton)setFocused(null);
        for(var button:buttons)removeWidget(button);buttons.clear();
        for(var control:controls()){
            var button=new GuideBookButton(control,left,top,horizontalScale,design,navigation,()->{
                if(control.id().equals("close")){onClose();return;}
                if(control.id().equals("related"))related=!related;
                else {GuideBookView.activate(control.id(),design,navigation,canvas(null));related=false;}
                syncing=true;search.setValue(navigation.query());syncing=false;dirty=true;
            },this::canvas);
            buttons.add(button);addRenderableWidget(button);if(control.id().equals(focused)&&button.active)setFocused(button);
        }
        dirty=false;
    }
    @Override public void tick(){super.tick();if(dirty)rebuildButtons();}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){
        if(dirty)rebuildButtons();GuideBookView.draw(canvas(g),design,navigation,related);if(search.getValue().isEmpty())canvas(g).text("Search the whole guide...",23,35,0xffc5cebf);super.extractRenderState(g,mouseX,mouseY,partialTick);
    }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){if(dirty)rebuildButtons();return super.mouseClicked(event,doubleClick);}
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double scrollX,double scrollY){
        if(scrollY==0||!FieldGuideView.inside((mouseX-left)/horizontalScale,mouseY-top,12,71,372,130))return super.mouseScrolled(mouseX,mouseY,scrollX,scrollY);
        int direction=scrollY>0?-1:1;
        if(navigation.page()==FieldGuideNavigation.Page.ARTICLE&&!related)navigation.turnPage(direction,GuideBookView.pages(canvas(null),design,navigation));
        else if(navigation.page()==FieldGuideNavigation.Page.INDEX)navigation.listPage(direction);else return false;
        dirty=true;return true;
    }
}
