package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Vanilla mining is cancelled on the client; the server validates reach, sight and held hammer. */
public record AnvilStrikePayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<AnvilStrikePayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"anvil_strike"));
    public static final StreamCodec<RegistryFriendlyByteBuf,AnvilStrikePayload> STREAM_CODEC=StreamCodec.of((b,p)->b.writeBlockPos(p.pos),b->new AnvilStrikePayload(b.readBlockPos()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public static void register(RegisterPayloadHandlersEvent event){event.registrar("1").playToServer(TYPE,STREAM_CODEC,(packet,context)->context.enqueueWork(()->{
        if(!(context.player() instanceof ServerPlayer player)||player.isSpectator()||!player.isAlive()||!(player.getMainHandItem().getItem() instanceof ForgeHammerItem)||!player.level().mayInteract(player,packet.pos))return;
        var core=AnvilWork.core(player.level(),packet.pos);if(core==null)return;
        var sight=player.pick(player.blockInteractionRange(),0,false);
        if(!(sight instanceof BlockHitResult hit)||!core.equals(AnvilWork.core(player.level(),hit.getBlockPos())))return;
        if(player.level().getBlockEntity(core) instanceof AncientAnvilBlockEntity be)AnvilWork.strike(be,player,InteractionHand.MAIN_HAND);
    }));}
}
