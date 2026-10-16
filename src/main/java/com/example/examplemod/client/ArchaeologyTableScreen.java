package com.example.examplemod.client;

import com.example.examplemod.*;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** A large excavation board with a top field kit and a separate inventory tab. */
public final class ArchaeologyTableScreen extends AbstractContainerScreen<ArchaeologyTableMenu> {
    private int selectedTool=2,currentCell=-1,lastHammerCell=-1;
    private boolean excavating;
    private long nextClientTick=Long.MIN_VALUE;
    public ArchaeologyTableScreen(ArchaeologyTableMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title,ExcavationLayout.WIDTH,ExcavationLayout.HEIGHT);
    }
    private boolean hasArtifact(){return ArchaeologyTableMenu.accepts(0,menu.getSlot(0).getItem());}
    private boolean hasTool(int tool){return tool>=1&&tool<=4&&ArchaeologyTableMenu.accepts(tool,menu.getSlot(tool).getItem());}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){
        int hover=ExcavationLayout.cellAt(mouseX-leftPos,mouseY-topPos);
        if(excavating)currentCell=hover;
        boolean[] tools=new boolean[5];for(int i=1;i<=4;i++)tools[i]=hasTool(i);
        var artifact=menu.getSlot(0).getItem();
        var state=new ExcavationScreenView.State(hasArtifact()?ExcavationStudy.read(artifact):null,
                artifact.is(ExampleMod.RARE_ANCIENT_ARTIFACT.get()),menu.inventoryView(),selectedTool,hover,tools,artifact.isEmpty()?"":artifact.getHoverName().getString());
        ExcavationScreenView.draw(new ExcavationScreenView.Canvas(){
            @Override public void fill(int x,int y,int w,int h,int color){g.fill(leftPos+x,topPos+y,leftPos+x+w,topPos+y+h,color);}
            @Override public void texture(Identifier id,int x,int y,int w,int h,float u,float v,int sw,int sh,int tw,int th,int color){
                g.blit(RenderPipelines.GUI_TEXTURED,id,leftPos+x,topPos+y,u,v,w,h,sw,sh,tw,th,color);
            }
            @Override public void text(String text,int x,int y,int color){g.text(font,text,leftPos+x,topPos+y,color,false);}
            @Override public int textWidth(String text){return font.width(text);}
        },state,(key,args)->Component.translatable("gui.examplemod.excavation."+key,args).getString());
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY){}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){
        if(event.button()==0){
            double x=event.x()-leftPos,y=event.y()-topPos;
            if(ExcavationLayout.inside(x,y,176,7,65,15)){menu.inventoryView(false);excavating=false;return true;}
            if(ExcavationLayout.inside(x,y,245,7,65,15)){menu.inventoryView(true);excavating=false;return true;}
            int tool=ExcavationLayout.toolAt(x,y);
            if(tool>0){selectedTool=tool;excavating=false;return true;}
            if(!menu.inventoryView() && hasArtifact() && menu.getCarried().isEmpty()){
                if(ExcavationLayout.inside(x,y,267,214,40,14)){send(0,-1);excavating=false;return true;}
                int cell=ExcavationLayout.cellAt(x,y);
                if(ExcavationStudy.coveredCell(cell)){
                    currentCell=cell;lastHammerCell=-1;excavating=true;attempt();return true;
                }
            }
        }
        return super.mouseClicked(event,doubleClick);
    }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){
        if(excavating){currentCell=ExcavationLayout.cellAt(event.x()-leftPos,event.y()-topPos);attempt();return true;}
        return super.mouseDragged(event,dx,dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event){
        if(excavating){excavating=false;lastHammerCell=-1;return true;}
        return super.mouseReleased(event);
    }
    @Override protected void containerTick(){super.containerTick();attempt();}
    @Override public void onClose(){excavating=false;super.onClose();}
    private void attempt(){
        if(!excavating || menu.inventoryView() || !hasArtifact() || !hasTool(selectedTool) || selectedTool==4
                || !menu.getCarried().isEmpty() || !ExcavationStudy.coveredCell(currentCell) || minecraft.level==null)return;
        if(selectedTool!=2 && ExcavationStudy.read(menu.getSlot(0).getItem()).complete())return;
        long now=minecraft.level.getGameTime();if(now<nextClientTick)return;
        // A hammer held still delivers one strike; moving or clicking again is deliberate.
        if(selectedTool==3 && lastHammerCell==currentCell)return;
        send(selectedTool,currentCell);nextClientTick=now+2;lastHammerCell=currentCell;
    }
    private void send(int tool,int cell){
        if(minecraft.getConnection()!=null)minecraft.getConnection().send(new ServerboundCustomPayloadPacket(new ExcavationPayload(menu.containerId,tool,cell)));
    }
    @Override protected void extractTooltip(GuiGraphicsExtractor g,int mouseX,int mouseY){
        super.extractTooltip(g,mouseX,mouseY);
        if(hoveredSlot!=null && hoveredSlot.index<5 && !hoveredSlot.hasItem()){
            g.setTooltipForNextFrame(font,Component.translatable("gui.examplemod.table_slot."+hoveredSlot.index),mouseX,mouseY);return;
        }
        double x=mouseX-leftPos,y=mouseY-topPos;int tool=ExcavationLayout.toolAt(x,y);
        if(tool>0){
            g.setTooltipForNextFrame(font,List.of(Component.translatable(ArchaeologyTableMenu.tool(tool).getDescriptionId()),
                    Component.translatable("gui.examplemod.excavation.help."+tool),
                    Component.translatable(hasTool(tool)?"gui.examplemod.excavation.select_hint":"gui.examplemod.excavation.missing_tool")),Optional.empty(),mouseX,mouseY);return;
        }
        if(!menu.inventoryView() && hasArtifact()){
            if(ExcavationLayout.inside(x,y,267,214,40,14))g.setTooltipForNextFrame(font,Component.translatable("gui.examplemod.excavation.reset_hint"),mouseX,mouseY);
            int cell=ExcavationLayout.cellAt(x,y);
            if(selectedTool==4 && hasTool(4) && ExcavationStudy.coveredCell(cell))
                g.setTooltipForNextFrame(font,Component.translatable(ExcavationStudy.relicCell(cell)?"gui.examplemod.excavation.scan.inclusion":"gui.examplemod.excavation.scan.cover"),mouseX,mouseY);
            else if(!excavating && selectedTool==2 && hasTool(2) && ExcavationStudy.coveredCell(cell) && ExcavationStudy.read(menu.getSlot(0).getItem()).depth(cell)>1)
                g.setTooltipForNextFrame(font,Component.translatable("gui.examplemod.excavation.hard_cover"),mouseX,mouseY);
        }
    }
}
