package com.example.examplemod;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;

/** Six real owned cells, not a render model stretched through neighboring blocks. */
public final class AncientAnvilStructure {
    public record Cell(int index,int y,int z){public BlockPos at(BlockPos core,Direction facing){return core.relative(facing.getOpposite(),z).above(y);}}
    public static final List<Cell> CELLS=List.of(new Cell(0,0,0),new Cell(1,0,-1),new Cell(2,0,1),new Cell(3,1,-1),new Cell(4,1,0),new Cell(5,1,1));
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
        var facing=state.getValue(AncientAnvilBlock.FACING);if(!room(level,core,facing,!state.getValue(AncientAnvilBlock.LARGE)))return false;
        var key=new Key(level,core.immutable());if(!CHANGING.get().add(key))return false;
        boolean success=false;
        try{
            for(var cell:CELLS)if(cell.index()!=0){var at=cell.at(core,facing);if(belongs(level,at,core,facing,cell.index()))continue;
                if(!level.setBlock(at,AncientAnvilContent.PART.get().defaultBlockState().setValue(AncientAnvilPartBlock.FACING,facing).setValue(AncientAnvilPartBlock.CELL,cell.index()),Block.UPDATE_ALL))return false;
            }
            level.setBlock(core,state.setValue(AncientAnvilBlock.LARGE,true),Block.UPDATE_ALL);success=true;return true;
        }finally{CHANGING.get().remove(key);if(!success)clear(level,core,facing);}
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
