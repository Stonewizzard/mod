package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import static com.example.examplemod.PickaxeTrait.*;

/** Conditions are evaluated at the actual targeted block; no client-only prediction mutates state. */
final class MiningConditions {
    static String id(BlockState state){return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();}
    static boolean ore(BlockState state){String id=id(state);return id.equals("ancient_debris")||id.endsWith("_ore")||state.is(TagKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath("c","ores")));}
    static boolean gem(String id){return id.contains("diamond_ore")||id.contains("emerald_ore")||id.contains("lapis_ore");}
    static boolean stone(String id){return id.equals("stone")||id.contains("deepslate")||id.equals("andesite")||id.equals("granite")||id.equals("diorite")||id.equals("tuff")||id.equals("calcite")||netherStone(id)||id.equals("end_stone");}
    static boolean netherStone(String id){return id.contains("netherrack")||id.contains("basalt")||id.contains("blackstone");}
    static boolean soft(String id){return id.contains("sand")||id.contains("gravel")||id.contains("clay")||id.contains("dirt");}
    static boolean lava(Level level,BlockPos pos){for(var p:BlockPos.betweenClosed(pos.offset(-2,-1,-2),pos.offset(2,1,2)))if(level.hasChunkAt(p)&&level.getFluidState(p).is(FluidTags.LAVA))return true;return false;}
    static boolean still(Player p){return p.getDeltaMovement().horizontalDistanceSqr()<.0025;}
    static double extra(Player player,BlockPos pos,BlockState state,ItemStack tool,PickaxeModifiers.Bonuses stats){
        String id=id(state);var level=player.level();int light=level.getMaxLocalRawBrightness(pos);double extra=0;
        if(stone(id))extra+=stats.get(STRATUM_SPEED);if(ore(state))extra+=stats.get(VEIN_SPEED);if(soft(id))extra+=stats.get(SOFT_ROCK);
        if(pos.getY()<0)extra+=stats.get(DEEP_SPEED);
        if(light>=12&&level.canSeeSky(pos.above()))extra+=stats.get(SUN_SPEED);if(light<8)extra+=stats.get(NIGHT_SPEED);
        if(player.isInWater()||level.isRainingAt(player.blockPosition()))extra+=stats.get(WET_SPEED);
        if(level.dimension()==Level.NETHER||stats.get(HEAT_SPEED)>0&&lava(level,pos))extra+=stats.get(HEAT_SPEED);
        if(still(player))extra+=stats.get(STEADY_SPEED);
        extra+=stats.get(RHYTHM_SPEED)*MiningRuntime.chain(tool,level.getGameTime())/5;
        if(MiningRuntime.varied(tool,level.getGameTime(),id))extra+=stats.get(DIVERSITY_SPEED);
        double life=1-tool.getDamageValue()/(double)Math.max(1,tool.getMaxDamage());
        if(life<.25)extra+=stats.get(FRAGILE_SPEED);if(life>.75)extra+=stats.get(CALIBRATED_SPEED);
        return Math.min(8,extra);
    }
    private MiningConditions(){}
}
