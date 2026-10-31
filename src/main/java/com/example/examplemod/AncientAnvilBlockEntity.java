package com.example.examplemod;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class AncientAnvilBlockEntity extends BlockEntity {
    public AncientAnvilBlockEntity(BlockPos p,BlockState s){super(AncientAnvilContent.ENTITY.get(),p,s);}
    public static List<BlockPos> missing(Level level,BlockPos core){
        var result=new ArrayList<BlockPos>();
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){var p=core.offset(x,-1,z);if(!level.hasChunkAt(p)||!level.getWorldBorder().isWithinBounds(p)||!level.getBlockState(p).is(AncientAnvilContent.FOUNDATION)||level.getBlockEntity(p)!=null)result.add(p);}
        return List.copyOf(result);
    }
    public static void refresh(ServerLevel level,BlockPos pos,BlockState state){if(state.is(AncientAnvilContent.ANVIL.get())){AncientAnvilStructure.form(level,pos);var current=level.getBlockState(pos);int tier=missing(level,pos).isEmpty()?1:0;if(current.getValue(AncientAnvilBlock.TIER)!=tier)level.setBlock(pos,current.setValue(AncientAnvilBlock.TIER,tier),3);}}
    public static void tick(Level l,BlockPos p,BlockState s,AncientAnvilBlockEntity be){if(l instanceof ServerLevel server&&l.getGameTime()%10==0)refresh(server,p,s);}
}
