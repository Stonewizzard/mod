package com.example.examplemod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** A request contains only a menu id, tool and cell; all item changes are server-authored. */
public record ExcavationPayload(int menuId,int tool,int cell) implements CustomPacketPayload {
    public static final Type<ExcavationPayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"excavate"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ExcavationPayload> STREAM_CODEC=StreamCodec.of(
            (buffer,payload)->{buffer.writeVarInt(payload.menuId);buffer.writeVarInt(payload.tool);buffer.writeVarInt(payload.cell);},
            buffer->new ExcavationPayload(buffer.readVarInt(),buffer.readVarInt(),buffer.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public static void register(RegisterPayloadHandlersEvent event){event.registrar("1").playToServer(TYPE,STREAM_CODEC,ExcavationPayload::handle);}
    private static void handle(ExcavationPayload payload,IPayloadContext context){
        context.enqueueWork(()->{
            if(context.player() instanceof ServerPlayer player && player.containerMenu instanceof ArchaeologyTableMenu menu
                    && menu.containerId==payload.menuId)menu.studyAction(player,payload.tool,payload.cell);
        });
    }
}
