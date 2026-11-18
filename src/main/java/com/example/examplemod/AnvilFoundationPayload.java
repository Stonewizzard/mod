package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** The client supplies only an anvil position; targets and inventory costs are server-authored. */
public record AnvilFoundationPayload(BlockPos core) implements CustomPacketPayload {
    public static final Type<AnvilFoundationPayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"anvil_foundation"));
    public static final StreamCodec<RegistryFriendlyByteBuf,AnvilFoundationPayload> STREAM_CODEC=StreamCodec.of((b,p)->b.writeBlockPos(p.core),b->new AnvilFoundationPayload(b.readBlockPos()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public static void register(RegisterPayloadHandlersEvent event){event.registrar("1").playToServer(TYPE,STREAM_CODEC,(packet,context)->context.enqueueWork(()->{
        if(context.player() instanceof ServerPlayer player)AnvilFoundation.placeNext(player,packet.core);
    }));}
}
