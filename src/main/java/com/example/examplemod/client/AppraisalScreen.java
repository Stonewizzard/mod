package com.example.examplemod.client;
import com.example.examplemod.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
/** Selection and pagination are local presentation only; native slots handle all inventory actions. */
public final class AppraisalScreen extends AbstractContainerScreen<AppraisalMenu> {
    private int selected,listPage,detailPage;private ItemStack previous=ItemStack.EMPTY;
    private AppraisalReport.Report report=AppraisalReport.read(ItemStack.EMPTY);
    public AppraisalScreen(AppraisalMenu menu,Inventory inventory,Component title){super(menu,inventory,title,320,242);}
    private void refresh(){var item=menu.getSlot(0).getItem();if(!ItemStack.matches(previous,item)){previous=item.copy();report=AppraisalReport.read(item);selected=0;listPage=0;detailPage=0;}}
    private ExcavationScreenView.Canvas canvas(GuiGraphicsExtractor g){return new ExcavationScreenView.Canvas(){
        @Override public void fill(int x,int y,int w,int h,int color){g.fill(leftPos+x,topPos+y,leftPos+x+w,topPos+y+h,color);}
        @Override public void texture(Identifier id,int x,int y,int w,int h,float u,float v,int sw,int sh,int tw,int th,int color){g.blit(RenderPipelines.GUI_TEXTURED,id,leftPos+x,topPos+y,u,v,w,h,sw,sh,tw,th,color);}
        @Override public void text(String text,int x,int y,int color){g.text(font,text,leftPos+x,topPos+y,color,false);}
        @Override public int textWidth(String text){return font.width(text);}
    };}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){refresh();AppraisalView.draw(canvas(g),report,selected,listPage,detailPage,AppraisalView.hit(mouseX-leftPos,mouseY-topPos,listPage,report.entries().size()));}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY){}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){
        refresh();int hit=AppraisalView.hit(event.x()-leftPos,event.y()-topPos,listPage,report.entries().size());
        if(event.button()==0&&hit!=-1){
            int pages=Math.max(1,(report.entries().size()+AppraisalView.ROWS-1)/AppraisalView.ROWS);
            if(hit>=0){selected=hit;detailPage=0;}else if(hit==-2||hit==-3){listPage=Math.floorMod(listPage+(hit==-2?-1:1),pages);selected=Math.min(listPage*AppraisalView.ROWS,report.entries().size()-1);detailPage=0;}
            else{var paragraphs=report.entries().get(selected).lines();int lineCount=AppraisalView.wrap(canvas(null),paragraphs).size(),detailPages=Math.max(1,(lineCount+AppraisalView.LINES-1)/AppraisalView.LINES);detailPage=Math.floorMod(detailPage+(hit==-4?-1:1),detailPages);}return true;
        }
        return super.mouseClicked(event,doubleClick);
    }
}
