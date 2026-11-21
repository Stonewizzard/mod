package com.example.examplemod;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.*;

/** Flat Tier 2 layout. Coordinates are relative to the anvil, front is its facing. */
public final class AnvilBasins {
    public enum Side { FRONT, RIGHT, BACK, LEFT;
        public Direction direction(Direction front){return switch(this){case FRONT->front;case RIGHT->front.getClockWise();case BACK->front.getOpposite();case LEFT->front.getCounterClockWise();};}
    }
    public record State(boolean complete,Map<Side,Fluid> fluids){}
    public static List<BlockPos> pool(BlockPos core,Direction front,Side side){
        var out=new ArrayList<BlockPos>();var dir=side.direction(front);
        for(int distance=3;distance<=5;distance++)for(int across=-(5-distance);across<=5-distance;across++){
            if(side==Side.FRONT&&(across==0||distance==5))continue;
            out.add(core.below().relative(dir,distance).relative(dir.getClockWise(),across));
        }
        return List.copyOf(out);
    }
    public static Set<BlockPos> frame(BlockPos core,Direction front){
        var out=new HashSet<BlockPos>();
        for(var side:Side.values()){
            var dir=side.direction(front);
            out.add(core.below().relative(dir,6));
            for(int distance=3;distance<=5;distance++)for(int sign:new int[]{-1,1})out.add(core.below().relative(dir,distance).relative(dir.getClockWise(),sign*(6-distance)));
        }
        for(int distance=3;distance<=5;distance++)out.add(core.below().relative(front,distance));
        return Set.copyOf(out);
    }
    public static State inspect(Level level,BlockPos core,Direction front){
        boolean complete=true;var fluids=new EnumMap<Side,Fluid>(Side.class);
        for(var p:frame(core,front))if(!level.hasChunkAt(p)||!level.getWorldBorder().isWithinBounds(p)||!level.getBlockState(p).is(AncientAnvilContent.FOUNDATION)||!ForgeRunes.allowedEntity(level,p))complete=false;
        for(var side:Side.values()){
            Fluid same=null;boolean valid=true;
            for(var p:pool(core,front,side)){
                if(!level.hasChunkAt(p)||!level.getWorldBorder().isWithinBounds(p)){valid=false;continue;}
                var state=level.getFluidState(p);
                // Basins contain actual fluid blocks, not waterlogged stairs or containers.
                if(state.isEmpty()||!ForgeRunes.allowedEntity(level,p)||!level.getBlockState(p).equals(state.createLegacyBlock())){valid=false;continue;}
                var fluid=canonical(state.getType());
                if(same==null)same=fluid;else if(!same.isSame(fluid))valid=false;
            }
            if(valid&&same!=null)fluids.put(side,same);else complete=false;
        }
        return new State(complete,Map.copyOf(fluids));
    }
    public static Fluid canonical(Fluid fluid){return fluid instanceof FlowingFluid flowing?flowing.getSource():fluid;}
    public static Map<Side,Identifier> fluidIds(Level level,BlockPos core,Direction front){
        var out=new EnumMap<Side,Identifier>(Side.class);inspect(level,core,front).fluids().forEach((side,fluid)->out.put(side,BuiltInRegistries.FLUID.getKey(fluid)));return Map.copyOf(out);
    }
    private AnvilBasins(){}
}
