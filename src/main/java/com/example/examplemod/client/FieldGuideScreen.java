package com.example.examplemod.client;
import com.example.examplemod.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
/** Native text entry supplies keyboard editing, clipboard and focus handling. */
public final class FieldGuideScreen extends Screen {
    private final FieldGuideNavigation navigation;private EditBox search;private int left,top;private boolean syncing;
    public FieldGuideScreen(FieldGuideNavigation navigation){super(Component.literal("Ancient Field Guide"));this.navigation=navigation;}
    @Override protected void init(){
        left=(width-FieldGuideView.WIDTH)/2;top=(height-FieldGuideView.HEIGHT)/2;
        search=new EditBox(font,left+15,top+33,290,16,Component.literal("Search field guide"));search.setMaxLength(128);search.setValue(navigation.query());search.setHint(Component.literal("Search the guide..."));search.setResponder(value->{if(!syncing)navigation.query(value);});addRenderableWidget(search);setInitialFocus(search);
    }
    private ExcavationScreenView.Canvas canvas(GuiGraphicsExtractor g){return new ExcavationScreenView.Canvas(){
        @Override public void fill(int x,int y,int w,int h,int color){g.fill(left+x,top+y,left+x+w,top+y+h,color);}
        @Override public void texture(Identifier id,int x,int y,int w,int h,float u,float v,int sw,int sh,int tw,int th,int color){g.blit(RenderPipelines.GUI_TEXTURED,id,left+x,top+y,u,v,w,h,sw,sh,tw,th,color);}
        @Override public void text(String text,int x,int y,int color){g.text(font,text,left+x,top+y,color,false);}
        @Override public int textWidth(String text){return font.width(text);}
    };}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){
        FieldGuideView.draw(canvas(g),navigation,FieldGuideView.hit(mouseX-left,mouseY-top,navigation));
        var entry=navigation.selected();if(entry!=null){var item=BuiltInRegistries.ITEM.getValue(Identifier.parse(entry.icon()));if(item!=null&&item!=Items.AIR)g.item(new ItemStack(item),left+172,top+58);}
        super.extractRenderState(g,mouseX,mouseY,partialTick);
    }
    private void synchronizeSearch(){syncing=true;search.setValue(navigation.query());syncing=false;}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){
        if(event.button()!=0)return super.mouseClicked(event,doubleClick);int hit=FieldGuideView.hit(event.x()-left,event.y()-top,navigation);
        if(hit==FieldGuideView.NONE)return super.mouseClicked(event,doubleClick);
        if(hit==FieldGuideView.CLOSE){onClose();return true;}
        if(hit>=FieldGuideView.LINK){int i=hit-FieldGuideView.LINK;var entry=navigation.selected();if(entry!=null&&i<entry.links().size())navigation.open(entry.links().get(i));}
        else if(hit>=FieldGuideView.ROW)navigation.selectRow(hit-FieldGuideView.ROW);
        else if(hit>=FieldGuideView.TAB){int i=hit-FieldGuideView.TAB;navigation.category(i==0?null:FieldGuide.Category.values()[i-1]);}
        else switch(hit){case FieldGuideView.LIST_PREV->navigation.listPage(-1);case FieldGuideView.LIST_NEXT->navigation.listPage(1);case FieldGuideView.ARTICLE_PREV->{if(!navigation.related())navigation.turnPage(-1,FieldGuideView.pages(canvas(null),navigation.selected()));}case FieldGuideView.ARTICLE_NEXT->{if(!navigation.related())navigation.turnPage(1,FieldGuideView.pages(canvas(null),navigation.selected()));}case FieldGuideView.BACK->navigation.back();case FieldGuideView.LINKS->navigation.related(!navigation.related());case FieldGuideView.GROUP->navigation.nextGroup();default->{}}
        synchronizeSearch();return true;
    }
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double scrollX,double scrollY){
        if(scrollY==0)return super.mouseScrolled(mouseX,mouseY,scrollX,scrollY);double x=mouseX-left,y=mouseY-top;int direction=scrollY>0?-1:1;
        if(FieldGuideView.inside(x,y,12,117,137,92)){navigation.listPage(direction);return true;}
        if(navigation.selected()!=null&&!navigation.related()&&FieldGuideView.inside(x,y,164,78,144,131)){navigation.turnPage(direction,FieldGuideView.pages(canvas(null),navigation.selected()));return true;}
        return super.mouseScrolled(mouseX,mouseY,scrollX,scrollY);
    }
}
