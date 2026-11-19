package com.example.examplemod;

import com.google.gson.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

/** Additive, reloadable structure requirements; never claims or consumes workshop blocks. */
public final class AnvilStages extends SimplePreparableReloadListener<List<AnvilStages.Stage>> {
    public static final TagKey<Block> CAPS=TagKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ancient_anvil_caps"));
    public static final TagKey<Block> KEYSTONES=TagKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"ancient_anvil_keystones"));
    public record Requirement(BlockPos offset,TagKey<Block> tag){
        public BlockPos at(BlockPos core,Direction front){return core.relative(front.getClockWise(),offset.getX()).above(offset.getY()).relative(front.getOpposite(),offset.getZ());}
        public boolean matches(Level level,BlockPos core,Direction front){var at=at(core,front);return level.hasChunkAt(at)&&level.getWorldBorder().isWithinBounds(at)&&level.getBlockState(at).is(tag)&&AnvilTierSix.matchesShape(level.getBlockState(at),tag);}
    }
    public record Stage(int tier,List<Requirement> blocks){
        public boolean complete(Level level,BlockPos core,Direction front){return blocks.stream().allMatch(r->r.matches(level,core,front)||AnvilTierSix.upgradedCap(level,core,front,tier,r));}
    }
    private static volatile List<Stage> stages=List.of();
    public static List<Stage> all(){return stages;}
    public static int tier(Level level,BlockPos core,Direction front,int baseTier){
        int tier=baseTier;
        for(var stage:stages){if(stage.tier()!=tier+1||(!stage.complete(level,core,front)||stage.tier()==6&&!AnvilTierSix.inspect(level,core,front,stages).complete()))break;tier=stage.tier();}
        return tier;
    }
    public static void register(AddServerReloadListenersEvent event){event.addListener(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"anvil_stages"),new AnvilStages());}
    @Override protected List<Stage> prepare(ResourceManager manager,ProfilerFiller profiler){
        var out=new TreeMap<Integer,Stage>();
        var files=new TreeMap<Identifier,Resource>(Comparator.comparing(Identifier::toString));
        files.putAll(manager.listResources("anvil_stages",id->id.getPath().endsWith(".json")));
        for(var entry:files.entrySet())try(var reader=entry.getValue().openAsReader()){
            var json=JsonParser.parseReader(reader).getAsJsonObject();if(json.has("enabled")&&!json.get("enabled").getAsBoolean())continue;
            int tier=json.get("tier").getAsInt();if(tier<3||tier>6)throw new IllegalArgumentException("Only workshop stages 3 through 6 are supported");
            var blocks=new ArrayList<Requirement>();var used=new HashSet<BlockPos>();
            for(var group:json.getAsJsonArray("groups")){
                var g=group.getAsJsonObject();var tag=TagKey.create(Registries.BLOCK,Identifier.parse(g.get("tag").getAsString()));
                for(var offset:g.getAsJsonArray("positions")){
                    var xyz=offset.getAsJsonArray();if(xyz.size()!=3)throw new IllegalArgumentException("Position needs x, y, z");
                    var p=new BlockPos(xyz.get(0).getAsInt(),xyz.get(1).getAsInt(),xyz.get(2).getAsInt());
                    if(Math.abs(p.getX())>32||Math.abs(p.getZ())>32||p.getY()<-16||p.getY()>32||!used.add(p))throw new IllegalArgumentException("Invalid or duplicate stage position "+p);
                    blocks.add(new Requirement(p,tag));
                }
            }
            if(blocks.isEmpty()||out.containsKey(tier))throw new IllegalArgumentException("Empty or duplicate stage "+tier);
            out.put(tier,new Stage(tier,List.copyOf(blocks)));
        }catch(Exception failure){ExampleMod.LOGGER.error("Cannot load anvil stage {}",entry.getKey(),failure);}
        return List.copyOf(out.values());
    }
    @Override protected void apply(List<Stage> loaded,ResourceManager manager,ProfilerFiller profiler){stages=loaded;}
}
