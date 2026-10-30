package com.example.examplemod.client;

import com.example.examplemod.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;

/** A small furnace-like alloying layout with native inventory slots and real buttons. */
public final class ForgeAlloyScreen extends AbstractContainerScreen<ForgeAlloyMenu> {
    private Button alloy;
    public ForgeAlloyScreen(ForgeAlloyMenu menu,Inventory inv,Component title){super(menu,inv,title,288,224);}
    private void send(int id){if(minecraft.getConnection()!=null)minecraft.getConnection().send(new ServerboundContainerButtonClickPacket(menu.containerId,id));}
    @Override protected void init(){super.init();alloy=addRenderableWidget(Button.builder(Component.translatable("alloy.examplemod.alloy"),b->send(1)).bounds(leftPos+180,topPos+94,88,20).build());}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){
        boolean melt=menu.melting();
        alloy.active=menu.ready();alloy.setMessage(Component.translatable(melt?"alloy.examplemod.melt":"alloy.examplemod.alloy"));
        g.fill(leftPos,topPos,leftPos+288,topPos+224,0xff28272a);g.fill(leftPos+2,topPos+2,leftPos+286,topPos+222,0xffc6c6c6);
        g.fill(leftPos+7,topPos+7,leftPos+281,topPos+26,0xff33383e);text(g,melt?"Artifact Melting":"Ancient Alloying",13,12,0xfff1d7a4);text(g,"TIER I",239,12,0xffc6b990);
        g.fill(leftPos+12,topPos+33,leftPos+276,topPos+121,0xff343638);
        text(g,melt?"Artifact":"Source",37,46,0xffd4c3a4);text(g,"Gold",85,46,0xffe6bd5b);text(g,"Iron",133,46,0xffc8d3db);text(g,">",166,65,0xffe6bd5b);
        text(g,"×1",44,81,0xffa5b0b2);text(g,melt?"":"×1",90,81,0xffa5b0b2);text(g,melt?"":"×2",136,81,0xffa5b0b2);
        text(g,"Spare parts",218,34,0xffa8adb0);
        for(int i=0;i<6;i++){var p=ForgeAlloyMenu.POSITIONS[i];slot(g,p[0],p[1]);}
        // Real item models explain the recipe without adding large instructional tooltips.
        Item[] ghosts={melt?null:AncientAnvilContent.SCRAP_ITEM.get(),melt?null:Items.GOLD_INGOT,melt?null:Items.IRON_INGOT,null,null,melt?AncientAnvilContent.SCRAP_ITEM.get():AncientAnvilContent.INGOT.get()};
        for(int i=0;i<6;i++)if(ghosts[i]!=null&&!menu.getSlot(i).hasItem()){var p=ForgeAlloyMenu.POSITIONS[i];g.item(new ItemStack(ghosts[i]),leftPos+p[0],topPos+p[1]);g.fill(leftPos+p[0],topPos+p[1],leftPos+p[0]+16,topPos+p[1]+16,0x99343638);}
        text(g,melt?"1 scrap":"4 ingots",180,80,0xffd1bc8d);text(g,melt?"1 artifact → 1 Ancient Scrap":"Scrap / debris / artifact",20,99,0xffc1c7c8);
        boolean spare=menu.getSlot(3).hasItem()||menu.getSlot(4).hasItem();text(g,spare?"Remove stored parts to process":melt&&(menu.getSlot(1).hasItem()||menu.getSlot(2).hasItem())?"Remove gold and iron to melt":"Ruined Forge / completed frame",14,123,spare?0xff9a421e:0xff50545b);
        text(g,"Inventory",63,133,0xff404044);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)slot(g,63+col*18,143+row*18);
        for(int col=0;col<9;col++)slot(g,63+col*18,201);
    }
    private void text(GuiGraphicsExtractor g,String s,int x,int y,int color){g.text(font,s,leftPos+x,topPos+y,color,false);}
    private void slot(GuiGraphicsExtractor g,int x,int y){g.fill(leftPos+x-1,topPos+y-1,leftPos+x+17,topPos+y+17,0xffeeeeee);g.fill(leftPos+x-1,topPos+y-1,leftPos+x+16,topPos+y,0xff373c42);g.fill(leftPos+x-1,topPos+y-1,leftPos+x,topPos+y+16,0xff373c42);g.fill(leftPos+x,topPos+y,leftPos+x+16,topPos+y+16,0xff777f82);}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
}
