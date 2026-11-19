package com.example.examplemod;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/** The next tier, shared by rendering and authoritative inventory construction. */
public final class AnvilGuide {
    public enum Kind { BLOCK, CAP, KEYSTONE, FLUID, ENERGIZED, OBSIDIAN, GLASS, CRYSTAL, SLAB }
    public record Target(BlockPos at,TagKey<Block> tag,Kind kind,Fluid fluid,Block glass,TagKey<Block> alternative){
        public Target(BlockPos at,TagKey<Block> tag,Kind kind,Fluid fluid){this(at,tag,kind,fluid,null,null);}
        public boolean acceptsBlock(Block block){return kind==Kind.GLASS?block.defaultBlockState().is(tag)&&(glass==null?AnvilTierSix.glassKey(block)!=null:AnvilTierSix.sameGlass(glass,block)):block.defaultBlockState().is(tag)||alternative!=null&&block.defaultBlockState().is(alternative);}
        public boolean matches(Level level){
            if(!level.hasChunkAt(at))return false;
            if(kind==Kind.CRYSTAL)return AnvilTierSix.crystal(level,at.below())!=null;
            if(kind!=Kind.FLUID)return acceptsBlock(level.getBlockState(at).getBlock())&&AnvilTierSix.matchesShape(level.getBlockState(at),tag);
            var state=level.getFluidState(at);
            return !state.isEmpty()&&level.getBlockEntity(at)==null&&level.getBlockState(at).equals(state.createLegacyBlock())&&(fluid==null||AnvilBasins.canonical(state.getType()).isSame(fluid));
        }
    }
    public record Plan(int tier,List<Target> targets){public List<Target> missing(Level level){return targets.stream().filter(t->!t.matches(level)).toList();}}
    private static volatile List<AnvilStages.Stage> clientStages=List.of();
    public static void receive(List<AnvilStages.Stage> stages){clientStages=List.copyOf(stages);}
    public static List<AnvilStages.Stage> stages(Level level){return level.isClientSide()?clientStages:AnvilStages.all();}
    public static Plan next(Level level,BlockPos core){
        var state=level.getBlockState(core);if(!state.is(AncientAnvilContent.ANVIL.get()))return new Plan(0,List.of());
        int next=state.getValue(AncientAnvilBlock.TIER)+1;var front=state.getValue(AncientAnvilBlock.FACING);var targets=new ArrayList<Target>();var stages=stages(level);
        if(next<=6&&AnvilTierSix.canUpgrade(level,core,front,stages))next=6;
        if(next==1)for(var p:AncientAnvilStructure.foundation(core,front))targets.add(new Target(p,AncientAnvilContent.FOUNDATION,Kind.BLOCK,null));
        else if(next==2){
            AnvilBasins.frame(core,front).stream().sorted(Comparator.<BlockPos>comparingInt(p->p.getX()).thenComparingInt(p->p.getZ())).forEach(p->targets.add(new Target(p,AncientAnvilContent.FOUNDATION,Kind.BLOCK,null)));
            for(var side:AnvilBasins.Side.values()){
                Fluid fluid=null;for(var p:AnvilBasins.pool(core,front,side))if(level.hasChunkAt(p)&&!level.getFluidState(p).isEmpty()){fluid=AnvilBasins.canonical(level.getFluidState(p).getType());break;}
                for(var p:AnvilBasins.pool(core,front,side))targets.add(new Target(p,null,Kind.FLUID,fluid));
            }
        }else if(next<=6){
            var glass=AnvilTierSix.referenceGlass(level,core,front,stages);
            for(var stage:stages)if(stage.tier()==next)for(var req:stage.blocks()){
                Kind kind=req.tag().equals(AnvilStages.KEYSTONES)?Kind.KEYSTONE:req.tag().equals(AnvilStages.CAPS)?Kind.CAP:req.tag().equals(AnvilTierSix.ENERGIZED)?Kind.ENERGIZED:req.tag().equals(AnvilTierSix.OBSIDIAN)?Kind.OBSIDIAN:req.tag().equals(AnvilTierSix.GLASS)?Kind.GLASS:req.tag().equals(AnvilTierSix.COLLAR_SLABS)?Kind.SLAB:Kind.BLOCK;
                var alternative=kind==Kind.CAP?(next==3?AnvilTierSix.ENERGIZED:next==4?AnvilTierSix.OBSIDIAN:null):null;
                targets.add(new Target(req.at(core,front),req.tag(),kind,null,kind==Kind.GLASS?glass:null,alternative));
                if(next==6&&kind==Kind.OBSIDIAN)targets.add(new Target(req.at(core,front).above(),null,Kind.CRYSTAL,null));
            }
            targets.sort(Comparator.comparingInt(t->t.at().getY()));
        }
        return new Plan(next,List.copyOf(targets));
    }
    private AnvilGuide(){}
}
