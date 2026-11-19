package com.example.examplemod;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Sync pack-defined higher-tier geometry to remote clients on login and /reload. */
public record AnvilGuidePayload(String json) implements CustomPacketPayload {
    public static final Type<AnvilGuidePayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"anvil_guide"));
    public static final StreamCodec<RegistryFriendlyByteBuf,AnvilGuidePayload> STREAM_CODEC=StreamCodec.of((b,p)->b.writeUtf(p.json,65536),b->new AnvilGuidePayload(b.readUtf(65536)));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public static void register(RegisterPayloadHandlersEvent e){e.registrar("1").playToClient(TYPE,STREAM_CODEC,(p,c)->c.enqueueWork(()->AnvilGuide.receive(p.decode())));}
    public static AnvilGuidePayload snapshot(){
        var out=new JsonArray();var codec=TagKey.<Block>codec(Registries.BLOCK);
        for(var stage:AnvilStages.all()){
            var s=new JsonObject();s.addProperty("tier",stage.tier());var blocks=new JsonArray();
            for(var req:stage.blocks()){
                var b=new JsonObject();b.addProperty("x",req.offset().getX());b.addProperty("y",req.offset().getY());b.addProperty("z",req.offset().getZ());b.add("tag",codec.encodeStart(JsonOps.INSTANCE,req.tag()).getOrThrow());blocks.add(b);
            }s.add("blocks",blocks);out.add(s);
        }
        return new AnvilGuidePayload(out.toString());
    }
    public List<AnvilStages.Stage> decode(){
        var out=new ArrayList<AnvilStages.Stage>();var codec=TagKey.<Block>codec(Registries.BLOCK);
        for(var element:JsonParser.parseString(json).getAsJsonArray()){
            var s=element.getAsJsonObject();var blocks=new ArrayList<AnvilStages.Requirement>();
            for(var value:s.getAsJsonArray("blocks")){var b=value.getAsJsonObject();blocks.add(new AnvilStages.Requirement(new BlockPos(b.get("x").getAsInt(),b.get("y").getAsInt(),b.get("z").getAsInt()),codec.parse(JsonOps.INSTANCE,b.get("tag")).getOrThrow()));}
            out.add(new AnvilStages.Stage(s.get("tier").getAsInt(),List.copyOf(blocks)));
        }return List.copyOf(out);
    }
    public static void sync(OnDatapackSyncEvent e){var packet=new ClientboundCustomPayloadPacket(snapshot());e.getRelevantPlayers().filter(p->p.connection.hasChannel(TYPE)).forEach(p->p.connection.send(packet));}
}
