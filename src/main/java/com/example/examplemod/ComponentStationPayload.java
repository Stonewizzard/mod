package com.example.examplemod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ComponentStationPayload(int menuId,int action,ItemStack expected) implements CustomPacketPayload {
    public static final Type<ComponentStationPayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"component_station_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ComponentStationPayload> STREAM_CODEC=StreamCodec.of(
        (buffer,payload)->{buffer.writeVarInt(payload.menuId);buffer.writeVarInt(payload.action);ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer,payload.expected);},
        buffer->new ComponentStationPayload(buffer.readVarInt(),buffer.readVarInt(),ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public static void register(RegisterPayloadHandlersEvent event){event.registrar("1").playToServer(TYPE,STREAM_CODEC,ComponentStationPayload::handle);}
    private static void handle(ComponentStationPayload payload,IPayloadContext context){context.enqueueWork(()->{
        if(context.player() instanceof ServerPlayer player)dispatch(payload,player);
    });}
    static void dispatch(ComponentStationPayload payload,ServerPlayer player){if(player.containerMenu instanceof ComponentStationMenu menu&&menu.containerId==payload.menuId)menu.action(player,payload.action,payload.expected);}
}
