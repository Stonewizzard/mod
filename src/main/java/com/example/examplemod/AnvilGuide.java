package com.example.examplemod;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/** The next additive tier, shared by rendering and authoritative inventory construction. */
public final class AnvilGuide {
    public enum Kind { BLOCK, CAP, FLUID }
    public record Target(BlockPos at,TagKey<Block> tag,Kind kind,Fluid fluid){
        public boolean matches(Level level){
            if(!level.hasChunkAt(at))return false;
            if(kind!=Kind.FLUID)return level.getBlockState(at).is(tag);
            var state=level.getFluidState(at);
            return !state.isEmpty()&&level.getBlockEntity(at)==null&&level.getBlockState(at).equals(state.createLegacyBlock())&&(fluid==null||AnvilBasins.canonical(state.getType()).isSame(fluid));
        }
    }
    public record Plan(int tier,List<Target> targets){
        public List<Target> missing(Level level){return targets.stream().filter(t->!t.matches(level)).toList();}
    }
    private static volatile List<AnvilStages.Stage> clientStages=List.of();
    public static void receive(List<AnvilStages.Stage> stages){clientStages=List.copyOf(stages);}
    public static Plan next(Level level,BlockPos core){
        var state=level.getBlockState(core);
        if(!state.is(AncientAnvilContent.ANVIL.get()))return new Plan(0,List.of());
        int next=state.getValue(AncientAnvilBlock.TIER)+1;var front=state.getValue(AncientAnvilBlock.FACING);var targets=new ArrayList<Target>();
        if(next==1)for(var p:AncientAnvilStructure.foundation(core,front))targets.add(new Target(p,AncientAnvilContent.FOUNDATION,Kind.BLOCK,null));
        else if(next==2){
            AnvilBasins.frame(core,front).stream().sorted(Comparator.<BlockPos>comparingInt(p->p.getX()).thenComparingInt(p->p.getZ())).forEach(p->targets.add(new Target(p,AncientAnvilContent.FOUNDATION,Kind.BLOCK,null)));
            for(var side:AnvilBasins.Side.values()){
                Fluid fluid=null;
                for(var p:AnvilBasins.pool(core,front,side))if(level.hasChunkAt(p)&&!level.getFluidState(p).isEmpty()){fluid=AnvilBasins.canonical(level.getFluidState(p).getType());break;}
                for(var p:AnvilBasins.pool(core,front,side))targets.add(new Target(p,null,Kind.FLUID,fluid));
            }
        }else if(next<=4){
            var stages=level.isClientSide()?clientStages:AnvilStages.all();
            for(var stage:stages)if(stage.tier()==next)for(var req:stage.blocks())targets.add(new Target(req.at(core,front),req.tag(),req.tag().equals(AnvilStages.CAPS)?Kind.CAP:Kind.BLOCK,null));
            targets.sort(Comparator.comparingInt(t->t.at().getY())); // Build pillars from their bases upward.
        }
        return new Plan(next,List.copyOf(targets));
    }
    private AnvilGuide(){}
}
