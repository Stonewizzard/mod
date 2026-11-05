package com.example.examplemod;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;

/** A grounded 3x3x2 centerpiece. Legacy indices 0..5 stay stable in saved worlds. */
public final class AncientAnvilStructure {
    public record Cell(int index,int x,int y,int z){public BlockPos at(BlockPos core,Direction facing){return core.relative(facing.getClockWise(),x).relative(facing.getOpposite(),z).above(y);}}
    public static final List<Cell> CELLS=cells();
    private static List<Cell> cells(){
        var result=new ArrayList<Cell>(List.of(new Cell(0,0,0,0),new Cell(1,0,0,-1),new Cell(2,0,0,1),new Cell(3,0,1,-1),new Cell(4,0,1,0),new Cell(5,0,1,1)));
        for(int x:new int[]{-1,1})for(int y=0;y<2;y++)for(int z=-1;z<=1;z++)result.add(new Cell(result.size(),x,y,z));
        return List.copyOf(result);
    }
    private record Key(ServerLevel level,BlockPos core){}
    private static final ThreadLocal<Set<Key>> CHANGING=ThreadLocal.withInitial(HashSet::new);
    public static boolean belongs(Level level,BlockPos at,BlockPos core,Direction facing,int cell){var s=level.getBlockState(at);return s.is(AncientAnvilContent.PART.get())&&s.getValue(AncientAnvilPartBlock.FACING)==facing&&s.getValue(AncientAnvilPartBlock.CELL)==cell&&AncientAnvilPartBlock.core(at,s).equals(core);}
    public static boolean room(Level level,BlockPos core,Direction facing,boolean entities){
        for(var cell:CELLS){var at=cell.at(core,facing);if(!level.hasChunkAt(at)||!level.getWorldBorder().isWithinBounds(at))return false;
            if(cell.index()!=0&&!belongs(level,at,core,facing,cell.index())&&(!level.getBlockState(at).isAir()||level.getBlockEntity(at)!=null))return false;
            if(entities){var shape=AncientAnvilGeometry.cell(cell.index(),facing);for(var e:level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new net.minecraft.world.phys.AABB(at)))if(!e.isSpectator())for(var box:shape.toAabbs())if(box.move(at).intersects(e.getBoundingBox()))return false;}
        }
        return true;
    }
    public static boolean form(ServerLevel level,BlockPos core){
        var state=level.getBlockState(core);if(!state.is(AncientAnvilContent.ANVIL.get()))return false;
        var facing=state.getValue(AncientAnvilBlock.FACING);
        // Do not dismantle a saved assembly just because a neighboring chunk is unloaded.
        for(var cell:CELLS)if(!level.hasChunkAt(cell.at(core,facing)))return false;
        boolean complete=state.getValue(AncientAnvilBlock.MONUMENTAL)&&CELLS.stream().filter(c->c.index()!=0).allMatch(c->belongs(level,c.at(core,facing),core,facing,c.index())&&level.getBlockState(c.at(core,facing)).getValue(AncientAnvilPartBlock.MONUMENTAL));
        if(!room(level,core,facing,!complete)){
            // A blocked legacy six-cell anvil keeps its old, safely bounded meshes.
            if(state.getValue(AncientAnvilBlock.MONUMENTAL)){
                clear(level,core,facing);
                level.setBlock(core,state.setValue(AncientAnvilBlock.LARGE,false).setValue(AncientAnvilBlock.MONUMENTAL,false),Block.UPDATE_ALL);
            }
            return false;
        }
        var key=new Key(level,core.immutable());if(!CHANGING.get().add(key))return false;
        boolean success=false;
        try{
            // Claim the surrounding cells before upgrading the central models.
            var order=new ArrayList<>(CELLS);order.sort(Comparator.comparingInt(c->c.index()==4?2:c.index()<6?1:0));
            for(var cell:order)if(cell.index()!=0){var at=cell.at(core,facing);if(belongs(level,at,core,facing,cell.index())&&level.getBlockState(at).getValue(AncientAnvilPartBlock.MONUMENTAL))continue;
                if(!level.setBlock(at,AncientAnvilContent.PART.get().defaultBlockState().setValue(AncientAnvilPartBlock.FACING,facing).setValue(AncientAnvilPartBlock.CELL,cell.index()).setValue(AncientAnvilPartBlock.MONUMENTAL,true),Block.UPDATE_ALL))return false;
            }
            level.setBlock(core,state.setValue(AncientAnvilBlock.LARGE,true).setValue(AncientAnvilBlock.MONUMENTAL,true),Block.UPDATE_ALL);success=true;return true;
        }finally{
            CHANGING.get().remove(key);
            if(!success){
                clear(level,core,facing);var remaining=level.getBlockState(core);
                if(remaining.is(AncientAnvilContent.ANVIL.get()))level.setBlock(core,remaining.setValue(AncientAnvilBlock.LARGE,false).setValue(AncientAnvilBlock.MONUMENTAL,false),Block.UPDATE_ALL);
            }
        }
    }
    public static void clear(ServerLevel level,BlockPos core,Direction facing){
        var key=new Key(level,core.immutable());if(!CHANGING.get().add(key))return;
        try{for(var cell:CELLS)if(cell.index()!=0){var at=cell.at(core,facing);if(level.hasChunkAt(at)&&belongs(level,at,core,facing,cell.index()))level.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);}}
        finally{CHANGING.get().remove(key);}
    }
    public static void brokenPart(ServerLevel level,BlockPos core,Direction facing){
        if(CHANGING.get().contains(new Key(level,core.immutable())))return;
        if(level.hasChunkAt(core)&&level.getBlockState(core).is(AncientAnvilContent.ANVIL.get()))level.destroyBlock(core,false);
        clear(level,core,facing);
    }
    private AncientAnvilStructure(){}
}
