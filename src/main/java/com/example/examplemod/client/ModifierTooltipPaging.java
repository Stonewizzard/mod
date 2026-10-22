package com.example.examplemod.client;
import com.example.examplemod.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
/** On-item values stay browsable; prose definitions live in the field guide. */
public final class ModifierTooltipPaging {
    private static ItemStack hovered=ItemStack.EMPTY;private static int page;private static long lastTooltip;
    public static void tooltip(ItemTooltipEvent event){
        var stack=event.getItemStack();
        if(ArchaeologyContent.artifacts().stream().anyMatch(stack::is)){
            event.getToolTip().add(PickaxeTooltips.text(ArtifactModifierCatalog.rarity(ArchaeologyContent.rarity(stack)).label()+" specimen",PickaxeTooltips.MUTED));
            if(ArtifactProfile.sealed(stack))event.getToolTip().add(PickaxeTooltips.text(ArtifactProfile.read(stack).matrix().label+" matrix",PickaxeTooltips.MUTED));
            lastTooltip=0;return;
        }
        if(PickaxeModifiers.pattern(stack)==null||event.getEntity()==null){lastTooltip=0;return;}
        if(!sameBuild(hovered,stack))page=0;hovered=stack.copy();lastTooltip=System.nanoTime();
        var lore=stack.getOrDefault(DataComponents.LORE,ItemLore.EMPTY).lines();if(lore.isEmpty())return;
        var lines=event.getToolTip();int start=-1;
        for(int i=1;i+lore.size()<=lines.size();i++)if(lines.subList(i,i+lore.size()).equals(lore)){start=i;break;}
        if(start<0)return;lines.subList(start,start+lore.size()).clear();lines.addAll(start,PickaxeTooltips.tool(stack,page));
    }
    private static boolean sameBuild(ItemStack a,ItemStack b){
        if(a.isEmpty()||!a.is(b.getItem()))return false;
        var ad=a.getOrDefault(DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY);
        var bd=b.getOrDefault(DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY);return ad.equals(bd);
    }
    public static void scroll(ScreenEvent.MouseScrolled.Pre event){
        boolean shift=InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)||InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
        if(!shift||System.nanoTime()-lastTooltip>200000000L||hovered.isEmpty())return;
        int pages=PickaxeTooltips.pages(PickaxeTooltips.view(hovered));if(pages<=1)return;page=Math.floorMod(page+(event.getScrollDeltaY()>0?-1:1),pages);event.setCanceled(true);
    }
    private ModifierTooltipPaging(){}
}
