package com.example.examplemod.client;

import com.example.examplemod.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Disposable visual-QA decorations only. None are registered as tier requirements. */
final class TierSixPreviewScene {
    private static final Map<BlockPos,BlockState> original=new LinkedHashMap<>();
    private static volatile int placed;
    private static volatile String current="none";
    static int count(){return placed;}
    static String current(){return current;}
    static void reset(ServerLevel level){for(var e:original.entrySet())level.setBlock(e.getKey(),e.getValue(),3);original.clear();placed=0;current="none";}
    private static void place(ServerLevel l,BlockPos p,BlockState s){if(l.getBlockState(p).isAir()){original.putIfAbsent(p.immutable(),l.getBlockState(p));l.setBlock(p,s,3);}}
    static void apply(ServerLevel l,BlockPos core,String kind){
        reset(l);current=kind;var rune=AncientAnvilContent.RUNESTONE.get().defaultBlockState();var obs=Blocks.OBSIDIAN.defaultBlockState();var slab=BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace("polished_deepslate_slab")).defaultBlockState();
        if(kind.equals("prongs")){
            for(var dir:Direction.Plane.HORIZONTAL){place(l,core.relative(dir,5).above(15),rune);place(l,core.relative(dir,5).above(14),rune);place(l,core.relative(dir,4).above(14),rune);place(l,core.relative(dir,4).above(13),obs);}
        }else if(kind.equals("collars")){
            for(var req:AnvilTierSix.requirements(AnvilStages.all(),AnvilTierSix.OBSIDIAN)){
                var cap=req.at(core,Direction.NORTH);
                for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0)place(l,cap.offset(x,-1,z),Math.abs(x)+Math.abs(z)==2?obs:rune);
                for(var side:Direction.Plane.HORIZONTAL){place(l,cap.relative(side).below(2),rune);place(l,cap.relative(side),slab);}
            }
        }else if(kind.equals("entrances")){
            var stairs=BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace("polished_deepslate_stairs")).defaultBlockState();
            for(var dir:Direction.Plane.HORIZONTAL){var across=dir.getClockWise();
                for(int distance=7;distance<=14;distance++){
                    int height=distance>=14?-4:distance>=12?-3:distance>=10?-2:-1;boolean step=distance==13||distance==11||distance==9;
                    for(int w=-1;w<=1;w++){var p=core.relative(dir,distance).relative(across,w);for(int y=-4;y<height;y++)place(l,p.above(y),rune);place(l,p.above(height),step?stairs.setValue(BlockStateProperties.HORIZONTAL_FACING,dir.getOpposite()):rune);}
                }
                for(int side:new int[]{-2,2}){var gate=core.relative(dir,8).relative(across,side);place(l,gate.below(),rune);place(l,gate,rune);place(l,gate.above(),obs);}
            }
        }
        placed=original.size();
    }
    private TierSixPreviewScene(){}
}
