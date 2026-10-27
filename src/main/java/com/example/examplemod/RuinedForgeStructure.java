package com.example.examplemod;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/** Local x is right, z is behind the hearth, y is up. Tier one occupies 3 x 3 x 4 blocks. */
public final class RuinedForgeStructure {
    public static final int TIER=1;
    public record Part(int x,int y,int z,String section){
        public BlockPos at(BlockPos core,Direction front){return core.relative(front.getClockWise(),x).relative(front.getOpposite(),z).above(y);}
    }
    public record Status(int present,List<Part> missing){
        public boolean complete(){return missing.isEmpty();}
        public Component message(){return complete()?Component.translatable("forge.examplemod.ready"):Component.translatable("forge.examplemod.incomplete",present,PARTS.size(),Component.translatable("forge.examplemod.section."+missing.getFirst().section()));}
    }
    public static final List<Part> PARTS;
    static {
        var parts=new ArrayList<Part>();
        for(int x=-1;x<=1;x++)for(int z=0;z<=2;z++)parts.add(new Part(x,-1,z,"foundation"));
        for(int x:new int[]{-1,1})for(int y=0;y<=1;y++)parts.add(new Part(x,y,1,"pillars"));
        for(int x=-1;x<=1;x++)parts.add(new Part(x,0,2,"back"));
        for(int y=1;y<=2;y++)parts.add(new Part(0,y,2,"chimney"));
        PARTS=List.copyOf(parts);
    }
    public static Status inspect(Level level,BlockPos core,Direction front){
        var missing=new ArrayList<Part>();
        for(int i=0;i<PARTS.size();i++){var part=PARTS.get(i);var pos=part.at(core,front);
            if(!level.hasChunkAt(pos)||!level.getWorldBorder().isWithinBounds(pos)||!(FormedForgePartBlock.belongs(level,pos,core,front,i)||level.getBlockState(pos).is(AncientForgeContent.MASONRY)&&level.getBlockEntity(pos)==null))missing.add(part);
        }
        return new Status(PARTS.size()-missing.size(),List.copyOf(missing));
    }
    private RuinedForgeStructure(){}
}
