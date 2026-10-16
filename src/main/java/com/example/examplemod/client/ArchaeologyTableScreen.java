package com.example.examplemod.client;

import com.example.examplemod.ArchaeologyTableMenu;
import com.example.examplemod.ExampleMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Parchment notes, a felt artifact mat, and an engraved four-tool field kit. */
public final class ArchaeologyTableScreen extends AbstractContainerScreen<ArchaeologyTableMenu> {
    private static Identifier texture(String path){return Identifier.fromNamespaceAndPath(ExampleMod.MODID,"textures/"+path+".png");}
    private static final Identifier PAPER=texture("block/table_parchment"),MAT=texture("block/table_mat");
    private static final String[] GUIDES={"","archaeology_drill","brush_guide","archaeology_hammer","magnifying_glass"};
    public ArchaeologyTableScreen(ArchaeologyTableMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title,176,202);inventoryLabelY=108;
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){
        int x=leftPos,y=topPos;
        g.fill(x,y,x+176,y+202,0xff38281c);
        g.fill(x+2,y+2,x+174,y+200,0xffaf884a);
        g.blit(RenderPipelines.GUI_TEXTURED,PAPER,x+4,y+4,0F,0F,168,194,32,32,32,32);
        g.fill(x+5,y+5,x+171,y+21,0xff354c39);
        g.fill(x+8,y+23,x+168,y+24,0xffaa8853);
        g.blit(RenderPipelines.GUI_TEXTURED,MAT,x+19,y+35,0F,0F,68,56,32,32,32,32);
        g.outline(x+18,y+34,70,58,0xffa88345);
        g.fill(x+94,y+34,x+158,y+86,0xffaf9666);
        g.fill(x+95,y+35,x+157,y+85,0xffcdbb8e);
        for(int i=0;i<5;i++){
            var slot=menu.getSlot(i);slot(g,x+slot.x,y+slot.y,i==0);
            if(i>0 && !slot.hasItem())g.blit(RenderPipelines.GUI_TEXTURED,texture("item/"+GUIDES[i]),x+slot.x,y+slot.y,
                    0F,0F,16,16,32,32,32,32,0x55ffffff);
        }
        g.fill(x+8,y+101,x+168,y+102,0xffa88345);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)slot(g,x+8+col*18,y+120+row*18,false);
        for(int col=0;col<9;col++)slot(g,x+8+col*18,y+178,false);
        for(int dx:new int[]{5,168})for(int dy:new int[]{5,195})g.fill(x+dx,y+dy,x+dx+3,y+dy+3,0xffd1ac62);
    }
    private static void slot(GuiGraphicsExtractor g,int x,int y,boolean mat){
        g.fill(x-1,y-1,x+17,y+17,0xff806541);
        g.fill(x,y,x+16,y+16,mat?0xff263c2e:0xff8e805f);
        g.fill(x,y,x+16,y+1,0xff584c35);
        g.fill(x,y,x+1,y+16,0xff584c35);
        g.fill(x+1,y+16,x+17,y+17,0xffedddad);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY){
        g.text(font,title,10,9,0xfff0e6c7,false);
        g.text(font,Component.translatable("gui.examplemod.artifact"),24,25,0xff4d4931,false);
        g.text(font,Component.translatable("gui.examplemod.field_kit"),101,25,0xff4d4931,false);
        g.text(font,playerInventoryTitle,8,108,0xff51432e,false);
    }
    @Override protected void extractTooltip(GuiGraphicsExtractor g,int mouseX,int mouseY){
        super.extractTooltip(g,mouseX,mouseY);
        if(hoveredSlot!=null && hoveredSlot.index<5 && !hoveredSlot.hasItem())
            g.setTooltipForNextFrame(font,Component.translatable("gui.examplemod.table_slot."+hoveredSlot.index),mouseX,mouseY);
    }
}
