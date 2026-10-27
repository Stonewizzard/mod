package com.example.examplemod;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

/** Reversible assembly; frame changes never consume a second set of materials or touch the core inventory. */
public final class ForgeAssembly {
    private record Key(ServerLevel level,BlockPos core){}
    private static final ThreadLocal<Set<Key>> MUTATING=ThreadLocal.withInitial(HashSet::new);
    public static boolean formed(net.minecraft.world.level.block.state.BlockState state){return state.is(AncientForgeContent.CORE.get())&&state.getValue(RuinedAncientForgeBlock.FORMED);}
    public static boolean form(ServerLevel level,BlockPos core){
        var state=level.getBlockState(core);if(!state.is(AncientForgeContent.CORE.get()))return false;
        var front=state.getValue(RuinedAncientForgeBlock.FACING);if(!RuinedForgeStructure.inspect(level,core,front).complete())return false;
        var key=new Key(level,core.immutable());if(!MUTATING.get().add(key))return formed(state);
        boolean success=false;
        try{
            for(int i=0;i<RuinedForgeStructure.PARTS.size();i++){
                var at=RuinedForgeStructure.PARTS.get(i).at(core,front);var original=level.getBlockState(at);
                if(FormedForgePartBlock.belongs(level,at,core,front,i))continue;
                var part=AncientForgeContent.PART.get().defaultBlockState().setValue(FormedForgePartBlock.INDEX,i).setValue(FormedForgePartBlock.FACING,front);
                if(!level.setBlock(at,part,Block.UPDATE_ALL)||!(level.getBlockEntity(at) instanceof ForgePartBlockEntity entity))return false;
                entity.original(original);
            }
            level.setBlock(core,state.setValue(RuinedAncientForgeBlock.FORMED,true),Block.UPDATE_ALL);success=true;return true;
        }finally{MUTATING.get().remove(key);if(!success)dismantle(level,core,front);}
    }
    public static void dismantle(ServerLevel level,BlockPos core,Direction front){
        var key=new Key(level,core.immutable());if(!MUTATING.get().add(key))return;
        try{
            for(int i=0;i<RuinedForgeStructure.PARTS.size();i++){
                var at=RuinedForgeStructure.PARTS.get(i).at(core,front);
                if(level.hasChunkAt(at)&&FormedForgePartBlock.belongs(level,at,core,front,i)&&level.getBlockEntity(at) instanceof ForgePartBlockEntity part)level.setBlock(at,part.original(),Block.UPDATE_ALL);
            }
            var state=level.getBlockState(core);if(formed(state))level.setBlock(core,state.setValue(RuinedAncientForgeBlock.FORMED,false),Block.UPDATE_ALL);
        }finally{MUTATING.get().remove(key);}
    }
    public static void tick(net.minecraft.world.level.Level level,BlockPos core,net.minecraft.world.level.block.state.BlockState state,ReconstructionTableBlockEntity entity){
        if(!(level instanceof ServerLevel server)||level.getGameTime()%10!=0)return;
        var front=state.getValue(RuinedAncientForgeBlock.FACING);
        if(RuinedForgeStructure.inspect(level,core,front).complete()){if(!formed(state))form(server,core);}
        else if(formed(state))dismantle(server,core,front);
    }
    private ForgeAssembly(){}
}
