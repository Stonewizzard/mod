package com.example.examplemod.client;

import com.example.examplemod.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Compact eight-stat pages, browsable without opening a separate workbench. */
public final class ModifierTooltipPaging {
    private static ItemStack hovered=ItemStack.EMPTY;
    private static int page;
    private static long lastTooltip;
    public static void tooltip(ItemTooltipEvent event){
        var stack=event.getItemStack();if(PickaxeModifiers.pattern(stack)==null||event.getEntity()==null)return;
        if(!ItemStack.isSameItemSameComponents(hovered,stack)){hovered=stack.copy();page=0;}
        lastTooltip=System.nanoTime();
        var lore=stack.getOrDefault(DataComponents.LORE,ItemLore.EMPTY).lines();
        var lines=event.getToolTip();int start=-1;
        for(int i=1;i+lore.size()<=lines.size();i++)if(lines.subList(i,i+lore.size()).equals(lore)){start=i;break;}
        if(start<0)return;lines.subList(start,start+lore.size()).clear();lines.addAll(start,PickaxeTooltips.tool(stack,page));
    }
    public static void scroll(ScreenEvent.MouseScrolled.Pre event){
        var minecraft=Minecraft.getInstance();
        boolean shift=InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)||InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
        if(!shift||System.nanoTime()-lastTooltip>200000000L||hovered.isEmpty())return;
        int pages=PickaxeTooltips.pages(PickaxeTooltips.view(hovered));if(pages<=1)return;
        page=Math.floorMod(page+(event.getScrollDeltaY()>0?-1:1),pages);event.setCanceled(true);
    }
    private ModifierTooltipPaging(){}
}
