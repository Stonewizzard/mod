package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Use the native tool callback, so canceled breaks and Creative mining award no tool XP. */
public final class RestoredAncientPickaxe extends Item {
    public RestoredAncientPickaxe(Properties properties){super(properties);}
    @Override public boolean mineBlock(ItemStack tool,Level level,BlockState state,BlockPos pos,LivingEntity miner){
        boolean eligible=miner instanceof ServerPlayer player&&!player.isCreative()&&!player.isSpectator()&&tool.isCorrectToolForDrops(state)&&state.getDestroySpeed(level,pos)>0;
        boolean mined=super.mineBlock(tool,level,state,pos,miner);
        if(Boolean.getBoolean("examplemod.dropRegressionTests"))System.out.println("ASCENSION_MINING_DEBUG eligible="+eligible+" mined="+mined+" empty="+tool.isEmpty()+" miner="+miner.getClass()+" state="+state);
        if(mined&&eligible&&!tool.isEmpty())PickaxeProgression.gain(tool,1);
        return mined;
    }
}
