package com.example.examplemod;

import com.google.gson.*;
import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

/** Server datapack recipes, deliberately separate from the crafting-table recipe type. */
public final class AnvilRecipes extends SimplePreparableReloadListener<List<AnvilRecipes.Recipe>> {
    public record Input(Identifier item,TagKey<Item> tag){
        boolean accepts(ItemStack stack){return stack.getCount()==1&&(tag!=null?stack.is(tag):BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(item));}
    }
    public record FluidInput(Identifier fluid,TagKey<net.minecraft.world.level.material.Fluid> tag){
        public boolean accepts(net.minecraft.world.level.material.Fluid value){return value!=null&&(tag!=null?value.defaultFluidState().is(tag):BuiltInRegistries.FLUID.getKey(AnvilBasins.canonical(value)).equals(fluid));}
    }
    public static Map<AnvilBasins.Side,FluidInput> readFluids(JsonObject json){
        var out=new EnumMap<AnvilBasins.Side,FluidInput>(AnvilBasins.Side.class);
        if(!json.has("fluids"))return Map.of();
        for(var entry:json.getAsJsonObject("fluids").entrySet()){
            var side=AnvilBasins.Side.valueOf(entry.getKey().toUpperCase(Locale.ROOT));var value=entry.getValue().getAsJsonObject();
            boolean fluid=value.has("fluid"),tag=value.has("tag");
            if(fluid==tag)throw new IllegalArgumentException("Each side requires exactly one fluid or fluid tag");
            Identifier id=Identifier.parse(value.get(fluid?"fluid":"tag").getAsString());
            if(fluid&&(!BuiltInRegistries.FLUID.containsKey(id)||BuiltInRegistries.FLUID.getValue(id)==net.minecraft.world.level.material.Fluids.EMPTY))throw new IllegalArgumentException("Unknown or empty fluid "+id);
            out.put(side,new FluidInput(fluid?BuiltInRegistries.FLUID.getKey(AnvilBasins.canonical(BuiltInRegistries.FLUID.getValue(id))):null,tag?TagKey.create(Registries.FLUID,id):null));
        }
        return Map.copyOf(out);
    }
    public record Recipe(Identifier id,int anvilTier,int hammerTier,int work,Map<AnvilSlot,Input> inputs,Item resultItem,int resultCount,Map<AnvilBasins.Side,FluidInput> fluids){
        public boolean fluidsMatch(AncientAnvilBlockEntity be){
            if(fluids.isEmpty())return true;
            var basins=AnvilBasins.inspect(be.getLevel(),be.getBlockPos(),be.getBlockState().getValue(AncientAnvilBlock.FACING));
            return basins.complete()&&fluids.entrySet().stream().allMatch(e->e.getValue().accepts(basins.fluids().get(e.getKey())));
        }
        // 26.3 binds item components after resource preparation. Create stacks only in a live world.
        public ItemStack result(){return new ItemStack(resultItem,resultCount);}
        public boolean matches(AncientAnvilBlockEntity be){
            if(resultCount>resultItem.getDefaultInstance().getMaxStackSize()||!fluidsMatch(be))return false;
            for(var slot:AnvilSlot.values()){var input=inputs.get(slot);var stack=be.input(slot);if(input==null?!stack.isEmpty():!input.accepts(stack))return false;}
            return true;
        }
    }
    private static volatile List<Recipe> recipes=List.of();
    public static List<Recipe> all(){return recipes;}
    public static Recipe match(AncientAnvilBlockEntity be){return recipes.stream().filter(r->r.matches(be)).findFirst().orElse(null);}
    public static void register(AddServerReloadListenersEvent event){event.addListener(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"anvil_recipes"),new AnvilRecipes());}
    @Override protected List<Recipe> prepare(ResourceManager manager,ProfilerFiller profiler){
        var result=new ArrayList<Recipe>();
        Map<Identifier,Resource> files=new TreeMap<>(Comparator.comparing(Identifier::toString));files.putAll(manager.listResources("anvil_recipes",id->id.getPath().endsWith(".json")));
        for(var entry:files.entrySet())try(var reader=entry.getValue().openAsReader()){
            var json=JsonParser.parseReader(reader).getAsJsonObject();if(json.has("enabled")&&!json.get("enabled").getAsBoolean())continue;
            String path=entry.getKey().getPath();var id=Identifier.fromNamespaceAndPath(entry.getKey().getNamespace(),path.substring("anvil_recipes/".length(),path.length()-5));
            var inputs=new EnumMap<AnvilSlot,Input>(AnvilSlot.class);
            for(var value:json.getAsJsonObject("ingredients").entrySet()){
                var ingredient=value.getValue().getAsJsonObject();boolean item=ingredient.has("item"),tag=ingredient.has("tag");
                if(item==tag)throw new IllegalArgumentException("Each position needs one item or tag");
                inputs.put(AnvilSlot.byId(value.getKey()),new Input(item?Identifier.parse(ingredient.get("item").getAsString()):null,tag?TagKey.create(Registries.ITEM,Identifier.parse(ingredient.get("tag").getAsString())):null));
            }
            if(inputs.isEmpty())throw new IllegalArgumentException("Anvil recipes must consume ingredients");
            var output=json.getAsJsonObject("result");var item=BuiltInRegistries.ITEM.getValue(Identifier.parse(output.get("id").getAsString()));
            int count=output.has("count")?output.get("count").getAsInt():1;
            if(item==null||item==Items.AIR||count<1||count>64)throw new IllegalArgumentException("Invalid anvil result");
            int tier=number(json,"anvil_tier",0),hammer=number(json,"hammer_tier",1),work=number(json,"work",4);
            if(tier<0||tier>2||hammer<1||work<1||work>10000)throw new IllegalArgumentException("Invalid work/tier requirements");
            var fluids=readFluids(json);
            result.add(new Recipe(id,fluids.isEmpty()?tier:Math.max(2,tier),hammer,work,Map.copyOf(inputs),item,count,fluids));
        }catch(Exception failure){ExampleMod.LOGGER.error("Cannot load anvil recipe {}",entry.getKey(),failure);}
        return List.copyOf(result);
    }
    private static int number(JsonObject json,String name,int fallback){return json.has(name)?json.get(name).getAsInt():fallback;}
    @Override protected void apply(List<Recipe> loaded,ResourceManager manager,ProfilerFiller profiler){recipes=loaded;}
}
