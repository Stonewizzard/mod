package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import java.util.Comparator;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

/** Check fitted solid bodies against native neighboring bones, not just UV validity. */
final class SculkAttachmentFitRegression {
    static void run() {
        var types=BuiltInRegistries.ENTITY_TYPE.stream().filter(DefaultAttributes::hasSupplier)
                .filter(com.example.examplemod.SkulkStatus::supports)
                .sorted(Comparator.comparingInt(type->-BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath().length())).toList();
        int checked=0,adjusted=0,remaining=0;
        for(var entry:LayerDefinitions.createRoots().entrySet()) {
            String key=entry.getKey().toString();
            if(!key.endsWith("#main"))continue;
            String name=key.split("#")[0].replace("minecraft:","");
            var found=types.stream().filter(type->{String id=BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
                return name.equals(id)||name.startsWith(id+"_")||name.endsWith("_"+id);}).findFirst();
            if(found.isEmpty())continue;
            var type=found.get();
            var geometry=new AdaptiveSculkSkin(entry.getValue().bakeRoot());
            if(geometry.anchor==null)continue;
            var old=SculkAttachmentPlacement.original(geometry);
            var fitted=SculkAttachmentPlacement.fit(geometry,type);
            int before=SculkAttachmentPlacement.collisions(geometry,old);
            int after=SculkAttachmentPlacement.collisions(geometry,fitted);
            require(after<=before,"Fitting worsened core clipping: "+key+" "+before+" -> "+after);
            boolean changed=!old.point.equals(fitted.point)||!old.normal.equals(fitted.normal)||old.scale!=fitted.scale;
            if(type==EntityTypes.PANDA || type==EntityTypes.SNIFFER || type==ExampleMod.CRUNCHER.get())
                require(!changed,"Protected placement changed: "+key);
            if(type==EntityTypes.IRON_GOLEM || type==EntityTypes.LLAMA || type==EntityTypes.TRADER_LLAMA)
                require(after==0,"Reported clipping must be cleared: "+key+" "+after);
            if(changed) {
                adjusted++;
                System.out.println("SCULK_FIT_ADJUSTED: "+key+" "+before+" -> "+after);
            }
            if(after>0){remaining++;System.out.println("SCULK_FIT_REMAINING: "+key+" "+after);}
            checked++;
        }
        require(checked>70,"Expected all native host model families");
        System.out.println("SCULK_FIT_CHECK: "+checked+" models, "+adjusted+" adjusted, "+remaining+" remaining overlaps");
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
