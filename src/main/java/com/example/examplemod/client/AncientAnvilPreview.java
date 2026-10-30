package com.example.examplemod.client;

import com.example.examplemod.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class AncientAnvilPreview {
    private static ClientLevel world;private static BlockPos core;
    public static boolean active(net.minecraft.world.level.Level l,BlockPos p){return l==world&&p.equals(core);}
    public static void toggle(ClientLevel l,BlockPos p){if(world!=l){world=l;core=null;}core=p.equals(core)?null:p.immutable();}
    public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){var l=Minecraft.getInstance().level;if(world!=l){world=l;core=null;}if(core!=null&&(!l.hasChunkAt(core)||!l.getBlockState(core).is(AncientAnvilContent.ANVIL.get())||AncientAnvilBlockEntity.missing(l,core).isEmpty()))core=null;}
    public static void click(PlayerInteractEvent.RightClickBlock e){if(e.getLevel() instanceof ClientLevel l&&e.getHand()==InteractionHand.MAIN_HAND&&e.getItemStack().isEmpty()&&l.getBlockState(e.getPos()).is(AncientAnvilContent.ANVIL.get())){Minecraft.getInstance().gui.setScreen(new AncientAnvilScreen(e.getPos()));e.setCancellationResult(InteractionResult.SUCCESS);e.setCanceled(true);}}
    private AncientAnvilPreview(){}
}
