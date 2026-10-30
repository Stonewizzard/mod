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
            for(var cell:RuinedForgeStructure.PARTS){
                int i=cell.cell();var at=cell.at(core,front);var original=level.getBlockState(at);
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
            for(var cell:RuinedForgeStructure.PARTS){
                int i=cell.cell();var at=cell.at(core,front);
                if(level.hasChunkAt(at)&&FormedForgePartBlock.belongs(level,at,core,front,i)&&level.getBlockEntity(at) instanceof ForgePartBlockEntity part)level.setBlock(at,part.original(),Block.UPDATE_ALL);
            }
            var state=level.getBlockState(core);if(formed(state))level.setBlock(core,state.setValue(RuinedAncientForgeBlock.FORMED,false),Block.UPDATE_ALL);
        }finally{MUTATING.get().remove(key);}
    }
    /** Old front-table cores move to the existing firebox without consuming their contents. */
    public static boolean migrate(ServerLevel level,BlockPos old,ReconstructionTableBlockEntity source){
        if(!source.legacyForge())return true;
        var state=level.getBlockState(old);var front=state.getValue(RuinedAncientForgeBlock.FACING);var target=old.relative(front.getOpposite(),2);
        if(!level.hasChunkAt(target)||!level.getWorldBorder().isWithinBounds(target))return false;
        var there=level.getBlockState(target);var entity=level.getBlockEntity(target);
        boolean oldCell=there.is(AncientForgeContent.PART.get())&&there.getValue(FormedForgePartBlock.INDEX)==RuinedForgeStructure.FIREBOX_CELL&&there.getValue(FormedForgePartBlock.FACING)==front&&entity instanceof ForgePartBlockEntity;
        if(!there.isAir()&&!oldCell&&!(there.is(AncientForgeContent.MASONRY)&&entity==null))return false;
        var original=oldCell?((ForgePartBlockEntity)entity).original():there;
        var oldKey=new Key(level,old.immutable());var newKey=new Key(level,target.immutable());
        if(!MUTATING.get().add(oldKey))return false;
        if(!MUTATING.get().add(newKey)){MUTATING.get().remove(oldKey);return false;}
        try{
            if(!level.setBlock(target,state.setValue(RuinedAncientForgeBlock.FORMED,false),Block.UPDATE_ALL)||!(level.getBlockEntity(target) instanceof ReconstructionTableBlockEntity moved))return false;
            for(int i=0;i<6;i++){moved.setItem(i,source.getItem(i).copy());source.setItem(i,net.minecraft.world.item.ItemStack.EMPTY);}
            moved.pattern(source.pattern());moved.currentForge();
            level.setBlock(old,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);
            if(!original.isAir())Block.dropResources(original,level,target);
            moved.setChanged();
        }finally{MUTATING.get().remove(oldKey);MUTATING.get().remove(newKey);}
        form(level,target);return true;
    }
    public static void tick(net.minecraft.world.level.Level level,BlockPos core,net.minecraft.world.level.block.state.BlockState state,ReconstructionTableBlockEntity entity){
        if(!(level instanceof ServerLevel server))return;
        if(entity.legacyForge()){migrate(server,core,entity);return;}
        if(level.getGameTime()%10==0){
            var front=state.getValue(RuinedAncientForgeBlock.FACING);
            if(RuinedForgeStructure.inspect(level,core,front).complete()){if(!formed(state))form(server,core);}
            else if(formed(state))dismantle(server,core,front);
        }
        entity.process.tick(server);
    }
    private ForgeAssembly(){}
}
