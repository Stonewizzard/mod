package com.example.examplemod.client;

import com.example.examplemod.*;
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
    private final ExcavationInput input;
    public ArchaeologyTableScreen(ArchaeologyTableMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title,ExcavationLayout.WIDTH,ExcavationLayout.HEIGHT);
        input=new ExcavationInput(new ExcavationInput.Session(){
            @Override public boolean inventoryView(){return menu.inventoryView();}
            @Override public void inventoryView(boolean value){menu.inventoryView(value);}
            @Override public boolean hasArtifact(){return ArchaeologyTableScreen.this.hasArtifact();}
            @Override public int carriedTool(){return menu.carriedTool();}
            @Override public boolean complete(){return ExcavationStudy.read(menu.getSlot(0).getItem()).complete();}
            @Override public long gameTime(){return minecraft.level==null?Long.MIN_VALUE:minecraft.level.getGameTime();}
            @Override public void send(int tool,int cell){ArchaeologyTableScreen.this.send(tool,cell);}
        });
    }
    private boolean hasArtifact(){return ArchaeologyTableMenu.accepts(0,menu.getSlot(0).getItem());}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){
        int hover=ExcavationLayout.cellAt(mouseX-leftPos,mouseY-topPos);
        input.hover(hover);
        boolean[] tools=new boolean[5];for(int i=1;i<=4;i++)tools[i]=ArchaeologyTableMenu.accepts(i,menu.getSlot(i).getItem());
        var artifact=menu.getSlot(0).getItem();
        var state=new ExcavationScreenView.State(hasArtifact()?ExcavationStudy.read(artifact):null,
                artifact.is(ExampleMod.RARE_ANCIENT_ARTIFACT.get()),menu.inventoryView(),menu.carriedTool(),hover,tools,artifact.isEmpty()?"":artifact.getHoverName().getString());
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
        return input.click(event,leftPos,topPos)||super.mouseClicked(event,doubleClick);
    }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){
        return input.drag(event,leftPos,topPos)||super.mouseDragged(event,dx,dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event){
        return input.release(event)||super.mouseReleased(event);
    }
    @Override protected void containerTick(){super.containerTick();input.tick();}
    @Override public void onClose(){input.stop();super.onClose();}
    private void send(int tool,int cell){
        if(minecraft.getConnection()!=null)minecraft.getConnection().send(new ServerboundCustomPayloadPacket(new ExcavationPayload(menu.containerId,tool,cell)));
    }
}
