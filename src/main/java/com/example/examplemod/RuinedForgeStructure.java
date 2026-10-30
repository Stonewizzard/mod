package com.example.examplemod;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/** The core is the back-center firebox; the existing frame extends two blocks in front. */
public final class RuinedForgeStructure {
    public static final int TIER=1,FIREBOX_CELL=14;
    /** Cell numbers remain stable for existing formed blocks and their saved masonry. */
    public record Part(int cell,int x,int y,int z,String section){
        public BlockPos at(BlockPos core,Direction front){return core.relative(front.getClockWise(),x).relative(front.getOpposite(),z).above(y);}
    }
    public record Status(int present,List<Part> missing){
        public boolean complete(){return missing.isEmpty();}
        public Component message(){return complete()?Component.translatable("forge.examplemod.ready"):Component.translatable("forge.examplemod.incomplete",present,PARTS.size(),Component.translatable("forge.examplemod.section."+missing.getFirst().section()));}
    }
    public static final List<Part> CELLS,PARTS;
    static {
        var cells=new ArrayList<Part>();
        for(int x=-1;x<=1;x++)for(int z=-2;z<=0;z++)add(cells,x,-1,z,"foundation");
        for(int x:new int[]{-1,1})for(int y=0;y<=1;y++)add(cells,x,y,-1,"pillars");
        for(int x=-1;x<=1;x++)add(cells,x,0,0,"back");
        for(int y=1;y<=2;y++)add(cells,0,y,0,"chimney");
        CELLS=List.copyOf(cells);PARTS=cells.stream().filter(p->p.cell()!=FIREBOX_CELL).toList();
    }
    private static void add(List<Part> cells,int x,int y,int z,String section){cells.add(new Part(cells.size(),x,y,z,section));}
    public static Status inspect(Level level,BlockPos core,Direction front){
        var missing=new ArrayList<Part>();
        for(var part:PARTS){var pos=part.at(core,front);
            if(!level.hasChunkAt(pos)||!level.getWorldBorder().isWithinBounds(pos)||!(FormedForgePartBlock.belongs(level,pos,core,front,part.cell())||level.getBlockState(pos).is(AncientForgeContent.MASONRY)&&level.getBlockEntity(pos)==null))missing.add(part);
        }
        return new Status(PARTS.size()-missing.size(),List.copyOf(missing));
    }
    private RuinedForgeStructure(){}
}
