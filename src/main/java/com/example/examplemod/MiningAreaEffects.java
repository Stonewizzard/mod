package com.example.examplemod;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import static com.example.examplemod.PickaxeTrait.*;
import static com.example.examplemod.ModifierTuning.Key.*;

/** Crouching opts into bounded native extra breaks; secondary loot never starts another excavation. */
final class MiningAreaEffects {
    static void apply(MiningContext c){
        if(!c.primary||!c.player.isShiftKeyDown()||c.player.gameMode.getGameModeForPlayer()==GameType.CREATIVE||c.player.gameMode.getGameModeForPlayer()==GameType.SPECTATOR)return;
        var targets=new LinkedHashSet<BlockPos>();var origin=c.event.getPos();boolean infernal=false;
        if(c.ore&&c.chance(VEIN_MINER))connected(c,targets,origin,c.count(VEIN_MINER,AMOUNT),false);
        if(c.ore&&c.chance(ORE_BRANCH))connected(c,targets,origin,c.count(ORE_BRANCH,AMOUNT),true);
        if(c.ore&&(c.block.contains("nether")||c.block.equals("ancient_debris"))&&c.perk(INFERNAL_ECHO)){connected(c,targets,origin,c.count(INFERNAL_ECHO,AMOUNT),false);infernal=true;}
        Direction direction=direction(c.player.getLookAngle());
        if(c.chance(TUNNELER))for(int i=1;i<=c.count(TUNNELER,AMOUNT);i++)targets.add(origin.relative(direction,i));
        if(c.chance(BROADHEAD)){
            int stage=c.stage(BROADHEAD),radius=stage>=2?2:1,depth=stage%2==1?2:1;
            for(int d=0;d<depth;d++)for(int a=-radius;a<=radius;a++)for(int b=-radius;b<=radius;b++){
                var p=origin.relative(direction,d);targets.add(switch(direction.getAxis()){case X->p.offset(0,a,b);case Y->p.offset(a,0,b);case Z->p.offset(a,b,0);});
            }
        }
        if(c.chance(STAIRMAKER)){var horizontal=horizontal(c.player.getLookAngle());for(int i=1;i<=c.count(STAIRMAKER,AMOUNT);i++){var p=origin.relative(horizontal,i).below(i);targets.add(p);targets.add(p.above());}}
        if(MiningConditions.soft(c.block)&&c.chance(SURFACE_SCRAPER))around(c,targets,c.count(SURFACE_SCRAPER,AMOUNT),s->MiningConditions.id(s).equals(c.block));
        if((c.block.equals("gravel")||c.block.endsWith("sand"))&&c.chance(GRAVEL_VACUUM))for(int i=1;i<=c.count(GRAVEL_VACUUM,AMOUNT);i++){var p=origin.above(i);if(!c.level.hasChunkAt(p)||!MiningConditions.id(c.level.getBlockState(p)).equals(c.block))break;targets.add(p);}
        if(c.block.equals("amethyst_cluster")&&c.chance(CRYSTAL_HARVEST))around(c,targets,c.count(CRYSTAL_HARVEST,AMOUNT),s->MiningConditions.id(s).equals("amethyst_cluster"));
        if(c.block.equals("obsidian")&&c.chance(OBSIDIAN_CHISEL))around(c,targets,c.count(OBSIDIAN_CHISEL,AMOUNT),s->MiningConditions.id(s).equals(c.block));
        if(c.perk(SURGE_DISCHARGE)){int count=0;for(int radius=1;radius<=6&&count<c.count(SURGE_DISCHARGE,AMOUNT);radius++)for(var d:List.of(Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST)){targets.add(origin.relative(d,radius));count++;}}
        targets.remove(origin);int budget=(int)ModifierGameplay.MAX_EXTRA_BLOCKS;MiningRuntime.begin(c.player.getUUID(),c.soulFurnace||infernal);
        try{for(var p:targets){if(budget<=0||c.tool.isEmpty()||c.player.getMainHandItem()!=c.tool)break;if(!valid(c,p))continue;budget--;c.player.gameMode.destroyBlock(p);}}
        finally{MiningRuntime.end(c.player.getUUID());}
    }
    static boolean valid(MiningContext c,BlockPos pos){
        double range=ModifierGameplay.MAX_AREA_RANGE;
        if(!c.level.hasChunkAt(pos)||!c.level.getWorldBorder().isWithinBounds(pos)||!c.level.mayInteract(c.player,pos)||pos.distSqr(c.event.getPos())>range*range)return false;
        if(pos.distToCenterSqr(c.player.getX(),c.player.getY(),c.player.getZ())>Math.pow(range+c.player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE),2))return false;
        var state=c.level.getBlockState(pos);return !state.isAir()&&!state.hasBlockEntity()&&state.getDestroySpeed(c.level,pos)>0&&(!state.requiresCorrectToolForDrops()||c.tool.isCorrectToolForDrops(state));
    }
    private static void connected(MiningContext c,Set<BlockPos> targets,BlockPos origin,int max,boolean mixed){
        var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();queue.add(origin);seen.add(origin);int added=0;
        while(!queue.isEmpty()&&seen.size()<1024&&added<max){var current=queue.remove();for(var d:Direction.values()){
            var p=current.relative(d);if(!seen.add(p)||p.distSqr(origin)>ModifierGameplay.MAX_AREA_RANGE*ModifierGameplay.MAX_AREA_RANGE||!c.level.hasChunkAt(p))continue;var state=c.level.getBlockState(p);
            if(!(mixed?MiningConditions.ore(state):MiningConditions.id(state).equals(c.block)))continue;
            queue.add(p);targets.add(p);if(++added>=max)break;
        }}
    }
    private static void around(MiningContext c,Set<BlockPos> targets,int max,java.util.function.Predicate<BlockState> match){int added=0;for(int r=1;r<=3&&added<max;r++)for(var pos:BlockPos.betweenClosed(c.event.getPos().offset(-r,-r,-r),c.event.getPos().offset(r,r,r))){if(added>=max)break;if(pos.equals(c.event.getPos())||targets.contains(pos)||!c.level.hasChunkAt(pos))continue;if(match.test(c.level.getBlockState(pos))){targets.add(pos.immutable());added++;}}}
    static Direction direction(net.minecraft.world.phys.Vec3 v){if(Math.abs(v.y)>Math.abs(v.x)&&Math.abs(v.y)>Math.abs(v.z))return v.y>0?Direction.UP:Direction.DOWN;return horizontal(v);}
    static Direction horizontal(net.minecraft.world.phys.Vec3 v){return Math.abs(v.x)>Math.abs(v.z)?v.x>0?Direction.EAST:Direction.WEST:v.z>0?Direction.SOUTH:Direction.NORTH;}
    private MiningAreaEffects(){}
}
