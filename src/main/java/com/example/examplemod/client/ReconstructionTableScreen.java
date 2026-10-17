package com.example.examplemod.client;

import com.example.examplemod.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class ReconstructionTableScreen extends AbstractContainerScreen<ReconstructionTableMenu> {
    public ReconstructionTableScreen(ReconstructionTableMenu menu,Inventory inventory,Component title){super(menu,inventory,title,320,236);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){
        boolean[] inserted=new boolean[5];int[] scores=new int[5];for(int i=0;i<5;i++){inserted[i]=ReconstructionTableMenu.accepts(i,menu.getSlot(i).getItem());scores[i]=ArtifactQuality.score(menu.getSlot(i).getItem());}
        var state=new ReconstructionScreenView.State(menu.pattern()==1,inserted,scores,!menu.getSlot(5).getItem().isEmpty(),menu.previewScore());
        ReconstructionScreenView.draw(new ExcavationScreenView.Canvas(){
            @Override public void fill(int x,int y,int w,int h,int color){g.fill(leftPos+x,topPos+y,leftPos+x+w,topPos+y+h,color);}
            @Override public void texture(Identifier id,int x,int y,int w,int h,float u,float v,int sw,int sh,int tw,int th,int color){g.blit(RenderPipelines.GUI_TEXTURED,id,leftPos+x,topPos+y,u,v,w,h,sw,sh,tw,th,color);}
            @Override public void text(String text,int x,int y,int color){g.text(font,text,leftPos+x,topPos+y,color,false);}
            @Override public int textWidth(String text){return font.width(text);}
        },state);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY){}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){
        int button=ReconstructionScreenView.buttonAt(event.x()-leftPos,event.y()-topPos);
        if(button>=0){if(event.button()==InputConstants.MOUSE_BUTTON_LEFT&&minecraft.getConnection()!=null)minecraft.getConnection().send(new ServerboundContainerButtonClickPacket(menu.containerId,button));return true;}
        return super.mouseClicked(event,doubleClick);
    }
}
