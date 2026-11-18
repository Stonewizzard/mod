package com.example.examplemod.client;

import com.example.examplemod.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.*;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Tap sneak/use to toggle; hold for half a second to build from inventory. */
public final class AncientAnvilPreview {
    private static ClientLevel world;private static BlockPos core,heldCore;
    private static long pressedAt,lastSent=-100;
    public static boolean active(net.minecraft.world.level.Level l,BlockPos p){return l==world&&p.equals(core);}
    public static void toggle(ClientLevel l,BlockPos p){if(world!=l){world=l;core=null;heldCore=null;}core=p.equals(core)?null:p.immutable();}
    public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();var level=mc.level;
        if(world!=level){world=level;core=null;heldCore=null;}
        if(core!=null&&(!level.hasChunkAt(core)||!level.getBlockState(core).is(AncientAnvilContent.ANVIL.get())))core=null;
        if(level==null||mc.player==null||mc.gui.screen()!=null||!mc.player.isShiftKeyDown()||!mc.options.keyUse.isDown()){heldCore=null;return;}
        if(heldCore==null)return;
        var hit=mc.hitResult;
        if(!(hit instanceof BlockHitResult block)||!heldCore.equals(AnvilWork.core(level,block.getBlockPos()))){heldCore=null;return;}
        long now=level.getGameTime();
        if(now-pressedAt>=10&&now-lastSent>=AnvilFoundation.INTERVAL){
            core=heldCore; // A deliberate hold shows construction progress even if its initial tap hid the guide.
            if(AncientAnvilBlockEntity.missing(level,heldCore).isEmpty())return;
            lastSent=now;if(mc.getConnection()!=null)mc.getConnection().send(new ServerboundCustomPayloadPacket(new AnvilFoundationPayload(heldCore)));
        }
    }
    public static void click(PlayerInteractEvent.RightClickBlock e){
        if(!e.getEntity().isShiftKeyDown()||!(e.getLevel() instanceof ClientLevel level)||e.getHand()!=InteractionHand.MAIN_HAND)return;
        var at=AnvilWork.core(level,e.getPos());if(at==null)return;
        if(heldCore==null){toggle(level,at);heldCore=at.immutable();pressedAt=level.getGameTime();lastSent=-100;}
        e.setCancellationResult(InteractionResult.SUCCESS);e.setCanceled(true);
    }
    private AncientAnvilPreview(){}
}
