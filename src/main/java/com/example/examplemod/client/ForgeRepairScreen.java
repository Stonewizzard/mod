package com.example.examplemod.client;

import com.example.examplemod.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/** Native buttons expose the construction overlay without opening a server inventory. */
public final class ForgeRepairScreen extends Screen {
    private final BlockPos core;private int left,top;
    public ForgeRepairScreen(BlockPos core){super(Component.translatable("block.examplemod.ruined_ancient_forge"));this.core=core.immutable();}
    @Override protected void init(){
        left=(width-280)/2;top=(height-180)/2;
        boolean active=ForgePreview.active(minecraft.level,core);
        addRenderableWidget(Button.builder(Component.translatable(active?"forge.examplemod.hide_layout":"forge.examplemod.show_layout"),button->{ForgePreview.toggle(minecraft.level,core);onClose();}).bounds(left+12,top+119,256,20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.back"),button->onClose()).bounds(left+12,top+147,256,20).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){
        g.fill(left,top,left+280,top+180,0xff242b2d);g.fill(left+2,top+2,left+278,top+178,0xffd9c9a8);g.fill(left+6,top+6,left+274,top+28,0xff41484a);
        g.text(font,title,left+12,top+13,0xffffebcc,false);
        if(minecraft.level!=null){var state=minecraft.level.getBlockState(core);
            if(state.is(AncientForgeContent.CORE.get())){
                var status=RuinedForgeStructure.inspect(minecraft.level,core,state.getValue(RuinedAncientForgeBlock.FACING));int occupied=0;
                for(var part:status.missing())if(!minecraft.level.getBlockState(part.at(core,state.getValue(RuinedAncientForgeBlock.FACING))).isAir())occupied++;
                g.text(font,Component.translatable("forge.examplemod.repair_count",status.present(),RuinedForgeStructure.PARTS.size()),left+12,top+39,0xff342e28,false);
                g.text(font,Component.translatable("forge.examplemod.ghost_missing",status.missing().size()-occupied),left+12,top+57,0xff176979,false);
                g.text(font,Component.translatable("forge.examplemod.ghost_occupied",occupied),left+12,top+72,0xff8a4a1c,false);
                g.text(font,Component.translatable("forge.examplemod.repair_hint"),left+12,top+92,0xff514b40,false);
            }
        }
        super.extractRenderState(g,mouseX,mouseY,partialTick);
    }
    @Override public boolean isPauseScreen(){return false;}
}
