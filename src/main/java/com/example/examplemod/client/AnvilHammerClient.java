package com.example.examplemod.client;

import com.example.examplemod.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.client.Minecraft;

public final class AnvilHammerClient {
    private static ClientLevel world;private static long last=-100;
    public static void click(PlayerInteractEvent.LeftClickBlock event){
        if(!(event.getLevel() instanceof ClientLevel level)||!(event.getItemStack().getItem() instanceof ForgeHammerItem)||AnvilWork.core(level,event.getPos())==null)return;
        event.setCanceled(true);if(world!=level){world=level;last=-100;}
        if((event.getAction()==PlayerInteractEvent.LeftClickBlock.Action.START||event.getAction()==PlayerInteractEvent.LeftClickBlock.Action.CLIENT_HOLD)&&level.getGameTime()-last>=8){
            last=level.getGameTime();Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new AnvilStrikePayload(event.getPos())));
        }
    }
    private AnvilHammerClient(){}
}
