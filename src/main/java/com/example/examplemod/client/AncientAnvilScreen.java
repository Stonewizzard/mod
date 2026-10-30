package com.example.examplemod.client;

import com.example.examplemod.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public final class AncientAnvilScreen extends Screen {
    private final BlockPos core;private int left,top;private Button guide;
    public AncientAnvilScreen(BlockPos p){super(Component.translatable("block.examplemod.ancient_anvil"));core=p.immutable();}
    @Override protected void init(){left=(width-280)/2;top=(height-176)/2;guide=addRenderableWidget(Button.builder(Component.translatable(AncientAnvilPreview.active(minecraft.level,core)?"anvil.examplemod.hide":"anvil.examplemod.show"),b->{AncientAnvilPreview.toggle(minecraft.level,core);onClose();}).bounds(left+12,top+118,256,20).build());addRenderableWidget(Button.builder(Component.translatable("gui.back"),b->onClose()).bounds(left+12,top+146,256,20).build());}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float partial){
        g.fill(left,top,left+280,top+176,0xff28272a);g.fill(left+2,top+2,left+278,top+174,0xffd8c8a5);g.fill(left+6,top+6,left+274,top+29,0xff343638);g.text(font,title,left+12,top+14,0xffffd28a,false);
        int missing=minecraft.level==null?9:AncientAnvilBlockEntity.missing(minecraft.level,core).size();guide.active=missing>0;
        g.text(font,Component.translatable(missing==0?"anvil.examplemod.tier1":"anvil.examplemod.tier0"),left+12,top+40,missing==0?0xff74601e:0xff51483d,false);
        g.text(font,Component.translatable("anvil.examplemod.foundation",9-missing),left+12,top+59,0xff51483d,false);
        g.text(font,Component.translatable("anvil.examplemod.metals"),left+12,top+78,0xff51483d,false);
        g.text(font,Component.translatable("anvil.examplemod.mixed"),left+12,top+96,0xff51483d,false);super.extractRenderState(g,x,y,partial);
    }
    @Override public boolean isPauseScreen(){return false;}
}
