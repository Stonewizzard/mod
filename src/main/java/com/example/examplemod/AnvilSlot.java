package com.example.examplemod;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.phys.Vec3;

/** The thirteen marked positions. Coordinates are native pixels on the expanded face. */
public enum AnvilSlot {
    CENTER("center",16,11,29),
    NORTH_WEST("north_west",5,0,29),NORTH_EAST("north_east",27,0,29),
    SOUTH_WEST("south_west",5,23,29),SOUTH_EAST("south_east",27,23,29),
    NORTH_INNER("north_inner",16,4,29),EAST_INNER("east_inner",23,11,29),
    SOUTH_INNER("south_inner",16,18,29),WEST_INNER("west_inner",9,11,29),
    NORTH_OUTER("north_outer",16,-3,29),EAST_OUTER("east_outer",29,11,29),
    SOUTH_OUTER("south_outer",16,30,31),WEST_OUTER("west_outer",3,11,29);
    public final String id;public final double x,z,y;
    AnvilSlot(String id,double x,double z,double y){this.id=id;this.x=x;this.z=z;this.y=y;}
    public Vec3 local(){return new Vec3(x/16,y/16,z/16);}
    public Vec3 world(BlockPos core,Direction facing){return toWorld(core,facing,local());}
    public static AnvilSlot byId(String id){return Arrays.stream(values()).filter(s->s.id.equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown anvil position: "+id));}
    public static Vec3 toWorld(BlockPos core,Direction facing,Vec3 local){
        double x=local.x,z=local.z;int turns=turns(facing);
        for(int i=0;i<turns;i++){double nx=1-z;z=x;x=nx;}
        return new Vec3(core.getX()+x,core.getY()+local.y,core.getZ()+z);
    }
    public static Vec3 toLocal(BlockPos core,Direction facing,Vec3 world){
        double x=world.x-core.getX(),z=world.z-core.getZ();
        for(int i=0;i<(4-turns(facing))%4;i++){double nx=1-z;z=x;x=nx;}
        return new Vec3(x,world.y-core.getY(),z);
    }
    public static AnvilSlot nearest(BlockPos core,Direction facing,Vec3 hit){
        var local=toLocal(core,facing,hit);AnvilSlot nearest=null;double best=Double.MAX_VALUE;
        for(var slot:values()){
            if(Math.abs(local.y-slot.y/16)>.19)continue;
            double d=Math.pow(local.x-slot.x/16,2)+Math.pow(local.z-slot.z/16,2);
            if(d<best&&d<=Math.pow(2.65/16,2)){best=d;nearest=slot;}
        }
        return nearest;
    }
    private static int turns(Direction facing){return switch(facing){case EAST->1;case SOUTH->2;case WEST->3;default->0;};}
}
