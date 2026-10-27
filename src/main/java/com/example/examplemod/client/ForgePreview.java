package com.example.examplemod.client;

import com.example.examplemod.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** One local visual guide, never server blocks or a shared player/world setting. */
public final class ForgePreview {
    private static ClientLevel world;private static BlockPos anchor;
    public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
        var level=Minecraft.getInstance().level;if(world!=level){world=level;anchor=null;}
        if(anchor!=null&&(!level.hasChunkAt(anchor)||!level.getBlockState(anchor).is(AncientForgeContent.CORE.get())||ForgeAssembly.formed(level.getBlockState(anchor))))anchor=null;
    }
    public static boolean active(net.minecraft.world.level.Level level,BlockPos core){return world==level&&core.equals(anchor);}
    public static void toggle(ClientLevel level,BlockPos core){if(world!=level){world=level;anchor=null;}anchor=core.equals(anchor)?null:core.immutable();}
    public static void click(PlayerInteractEvent.RightClickBlock event){
        if(!(event.getLevel() instanceof ClientLevel level)||event.getHand()!=InteractionHand.MAIN_HAND||!event.getItemStack().isEmpty())return;
        var core=event.getPos();var state=level.getBlockState(core);
        if(state.is(AncientForgeContent.PART.get())){core=FormedForgePartBlock.core(core,state);state=level.getBlockState(core);}
        if(!state.is(AncientForgeContent.CORE.get()))return;
        var status=RuinedForgeStructure.inspect(level,core,state.getValue(RuinedAncientForgeBlock.FACING));
        if(!status.complete()||event.getEntity().isShiftKeyDown()){
            Minecraft.getInstance().gui.setScreen(new ForgeRepairScreen(core));event.setCancellationResult(InteractionResult.SUCCESS);event.setCanceled(true);
        }
    }
    private ForgePreview(){}
}
