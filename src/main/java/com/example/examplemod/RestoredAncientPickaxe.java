package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Use the native tool callback, so canceled breaks and Creative mining award no tool XP. */
public final class RestoredAncientPickaxe extends RestoredAncientTool {
    public RestoredAncientPickaxe(Properties properties){super(properties);}
    @Override public boolean mineBlock(ItemStack tool,Level level,BlockState state,BlockPos pos,LivingEntity miner){
        // The server game mode is authoritative; GameTest mock players override spectator flags.
        var mode=miner instanceof ServerPlayer player?player.gameMode.getGameModeForPlayer():null;
        boolean eligible=(mode==net.minecraft.world.level.GameType.SURVIVAL||mode==net.minecraft.world.level.GameType.ADVENTURE)&&!MiningRuntime.secondary(miner.getUUID())&&tool.isCorrectToolForDrops(state)&&state.getDestroySpeed(level,pos)>0;
        boolean mined=super.mineBlock(tool,level,state,pos,miner);
        if(mined&&eligible&&!tool.isEmpty()){
            int before=PickaxeProgression.read(tool).level();PickaxeProgression.gain(tool,1+PickaxeEvents.stochastic(miner.getRandom(),PickaxeModifiers.totals(tool).get(PickaxeTrait.RALLY_MINING)));
            if(PickaxeProgression.read(tool).level()>before){var player=(ServerPlayer)miner;player.level().playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,net.minecraft.sounds.SoundSource.PLAYERS,.55F,1.35F);}
        }
        return mined;
    }
}
