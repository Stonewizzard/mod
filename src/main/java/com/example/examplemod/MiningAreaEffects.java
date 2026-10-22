package com.example.examplemod;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import static com.example.examplemod.PickaxeTrait.*;

/** All shapes share one native-break budget and one recursion guard. Crouching explicitly enables them. */
final class MiningAreaEffects {
    static void apply(MiningContext c){
        if(!c.primary||!c.player.isShiftKeyDown()||c.player.gameMode.getGameModeForPlayer()==GameType.CREATIVE||c.player.gameMode.getGameModeForPlayer()==GameType.SPECTATOR)return;
        var targets=new LinkedHashSet<BlockPos>();var origin=c.event.getPos();
        if(c.ore&&c.chance(VEIN_MINER))connected(c,targets,origin,2+2*c.stage(VEIN_MINER),false);
        if(c.ore&&c.chance(ORE_BRANCH))connected(c,targets,origin,1+c.stage(ORE_BRANCH),true);
        if(c.ore&&(c.block.contains("nether")||c.block.equals("ancient_debris"))&&c.perk(INFERNAL_ECHO,60,4))connected(c,targets,origin,1+c.stage(INFERNAL_ECHO),false);
        Direction direction=direction(c.player.getLookAngle());
        if(c.chance(TUNNELER))for(int i=1;i<=1+c.stage(TUNNELER);i++)targets.add(origin.relative(direction,i));
        if(c.chance(BROADHEAD)){
            int cap=2+2*c.stage(BROADHEAD),added=0;
            for(int a=-1;a<=1&&added<cap;a++)for(int b=-1;b<=1&&added<cap;b++)if(a!=0||b!=0){
                targets.add(switch(direction.getAxis()){case X->origin.offset(0,a,b);case Y->origin.offset(a,0,b);case Z->origin.offset(a,b,0);});added++;
            }
        }
        if(c.chance(STAIRMAKER)){var horizontal=horizontal(c.player.getLookAngle());for(int i=1;i<=1+c.stage(STAIRMAKER);i++)targets.add(origin.relative(horizontal,i).below(i));}
        if(MiningConditions.soft(c.block)&&c.chance(SURFACE_SCRAPER))around(c,targets,2+2*c.stage(SURFACE_SCRAPER),s->MiningConditions.id(s).equals(c.block));
        if((c.block.equals("gravel")||c.block.endsWith("sand"))&&c.chance(GRAVEL_VACUUM))for(int i=1;i<=2+2*c.stage(GRAVEL_VACUUM);i++){var pos=origin.above(i);if(!c.level.hasChunkAt(pos)||!MiningConditions.id(c.level.getBlockState(pos)).equals(c.block))break;targets.add(pos);}
        if(c.block.equals("amethyst_cluster")&&c.chance(CRYSTAL_HARVEST))around(c,targets,1+c.stage(CRYSTAL_HARVEST),s->MiningConditions.id(s).equals("amethyst_cluster"));
        if(c.block.equals("obsidian")&&c.chance(OBSIDIAN_CHISEL))around(c,targets,1+c.stage(OBSIDIAN_CHISEL),s->MiningConditions.id(s).equals(c.block));
        if(c.perk(SURGE_DISCHARGE,40,5)){int count=0;for(var d:List.of(Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST)){if(count++>=1+c.stage(SURGE_DISCHARGE))break;targets.add(origin.relative(d));}}
        targets.remove(origin);int budget=12;MiningRuntime.begin(c.player.getUUID());
        try{for(var pos:targets){if(budget<=0||c.tool.isEmpty()||c.player.getMainHandItem()!=c.tool)break;if(!valid(c,pos))continue;budget--;c.player.gameMode.destroyBlock(pos);}}
        finally{MiningRuntime.end(c.player.getUUID());}
    }
    static boolean valid(MiningContext c,BlockPos pos){
        if(!c.level.hasChunkAt(pos)||!c.level.getWorldBorder().isWithinBounds(pos)||!c.level.mayInteract(c.player,pos)||pos.distSqr(c.event.getPos())>100)return false;
        if(pos.distToCenterSqr(c.player.getX(),c.player.getY(),c.player.getZ())>Math.pow(c.player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE)+2,2))return false;
        var state=c.level.getBlockState(pos);return !state.isAir()&&!state.hasBlockEntity()&&state.getDestroySpeed(c.level,pos)>0&&(!state.requiresCorrectToolForDrops()||c.tool.isCorrectToolForDrops(state));
    }
    private static void connected(MiningContext c,Set<BlockPos> targets,BlockPos origin,int max,boolean mixed){
        var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();queue.add(origin);seen.add(origin);int added=0;
        while(!queue.isEmpty()&&seen.size()<96&&added<max){var current=queue.remove();for(var d:Direction.values()){
            var p=current.relative(d);if(!seen.add(p)||p.distSqr(origin)>36||!c.level.hasChunkAt(p))continue;var state=c.level.getBlockState(p);
            if(!(mixed?MiningConditions.ore(state):MiningConditions.id(state).equals(c.block)))continue;
            queue.add(p);targets.add(p);if(++added>=max)break;
        }}
    }
    private static void around(MiningContext c,Set<BlockPos> targets,int max,java.util.function.Predicate<BlockState> match){int added=0;for(var pos:BlockPos.betweenClosed(c.event.getPos().offset(-1,-1,-1),c.event.getPos().offset(1,1,1))){if(pos.equals(c.event.getPos())||!c.level.hasChunkAt(pos))continue;if(match.test(c.level.getBlockState(pos))){targets.add(pos.immutable());if(++added>=max)break;}}}
    static Direction direction(net.minecraft.world.phys.Vec3 v){if(Math.abs(v.y)>Math.abs(v.x)&&Math.abs(v.y)>Math.abs(v.z))return v.y>0?Direction.UP:Direction.DOWN;return horizontal(v);}
    static Direction horizontal(net.minecraft.world.phys.Vec3 v){return Math.abs(v.x)>Math.abs(v.z)?v.x>0?Direction.EAST:Direction.WEST:v.z>0?Direction.SOUTH:Direction.NORTH;}
    private MiningAreaEffects(){}
}
