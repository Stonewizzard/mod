package com.example.examplemod;

import com.google.gson.JsonParser;
import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.material.*;

public final class AnvilBasinTests {
    private static Identifier id(String n){return Identifier.fromNamespaceAndPath(ExampleMod.MODID,n);}
    public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent e){var env=e.registerEnvironment(id("anvil_basin_environment"));e.registerTest(id("anvil_basins"),new Test(new TestData<>(env,Level.OVERWORLD,id("empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));}
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> d){super(d);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Tier 2 directional fluids, recipe constraints, downgrade and free empty ownership cells");}
        @Override public void run(GameTestHelper h){
            var l=h.getLevel();var core=h.absolutePos(new BlockPos(8,3,8));
            for(var front:Direction.Plane.HORIZONTAL){
                for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++)for(int y=-1;y<=2;y++)l.setBlock(core.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);
                l.setBlock(core,AncientAnvilContent.ANVIL.get().defaultBlockState().setValue(AncientAnvilBlock.FACING,front),3);
                for(var at:AncientAnvilStructure.foundation(core,front))l.setBlock(at,AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
                for(var at:AnvilBasins.frame(core,front))l.setBlock(at,AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
                var all=new HashSet<BlockPos>();for(var side:AnvilBasins.Side.values()){
                    var pool=AnvilBasins.pool(core,front,side);check(h,pool.size()==(side==AnvilBasins.Side.FRONT?6:9),"Front has two three-block pools; other sides have nine");
                    for(var at:pool){check(h,all.add(at)&&!AnvilBasins.frame(core,front).contains(at)&&!AncientAnvilStructure.foundation(core,front).contains(at),"Pools, frame and foundation do not overlap");l.setBlock(at,(side==AnvilBasins.Side.FRONT||side==AnvilBasins.Side.LEFT?Blocks.WATER:Blocks.LAVA).defaultBlockState(),3);}
                }
                AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));check(h,l.getBlockState(core).getValue(AncientAnvilBlock.TIER)==2,"Mixed sides form Tier 2 in every facing");
                var ids=AnvilBasins.fluidIds(l,core,front);check(h,ids.get(AnvilBasins.Side.FRONT).equals(Identifier.withDefaultNamespace("water"))&&ids.get(AnvilBasins.Side.RIGHT).equals(Identifier.withDefaultNamespace("lava")),"Detected fluid IDs follow the anvil facing");
                var be=(AncientAnvilBlockEntity)l.getBlockEntity(core);
                var json=JsonParser.parseString("{\"fluids\":{\"front\":{\"fluid\":\"minecraft:water\"},\"right\":{\"tag\":\"minecraft:lava\"}}}").getAsJsonObject();
                var requirements=AnvilRecipes.readFluids(json);
                var recipe=new AnvilRecipes.Recipe(id("fluid_test"),2,1,4,Map.of(AnvilSlot.CENTER,new AnvilRecipes.Input(Identifier.withDefaultNamespace("gold_ingot"),null)),Items.DIAMOND,1,requirements);
                be.placeInput(AnvilSlot.CENTER,new ItemStack(Items.GOLD_INGOT));check(h,recipe.matches(be),"Recipes accept fluid IDs and fluid tags together");
                var split=AnvilBasins.pool(core,front,AnvilBasins.Side.FRONT);for(var at:split)l.setBlock(at,Blocks.AIR.defaultBlockState(),3);for(int i=0;i<split.size();i++)l.setBlock(split.get(i),(i==0||i==1||i==4?Blocks.WATER:Blocks.LAVA).defaultBlockState(),3);
                AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));check(h,l.getBlockState(core).getValue(AncientAnvilBlock.TIER)==1&&!recipe.matches(be),"Both separated front pools must match, recipe stops immediately");
                for(var at:split)l.setBlock(at,Blocks.AIR.defaultBlockState(),3);for(var at:split)l.setBlock(at,Blocks.WATER.defaultBlockState(),3);
                var flow=AnvilBasins.pool(core,front,AnvilBasins.Side.RIGHT).getFirst();l.setBlock(flow,Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL,4),3);
                check(h,AnvilBasins.inspect(l,core,front).complete(),"Source and flowing forms of the same fluid agree");
                var border=AnvilBasins.frame(core,front).iterator().next();l.setBlock(border,Blocks.STONE.defaultBlockState(),3);AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));check(h,l.getBlockState(core).getValue(AncientAnvilBlock.TIER)==1,"Broken frame downgrades to Tier 1");l.setBlock(border,AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
                var foundation=core.below();l.setBlock(foundation,Blocks.STONE.defaultBlockState(),3);AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));check(h,l.getBlockState(core).getValue(AncientAnvilBlock.TIER)==0,"Basins cannot bypass Tier 1 foundation");l.setBlock(foundation,AncientAnvilContent.RUNESTONE.get().defaultBlockState(),3);
                var empty=AncientAnvilStructure.CELLS.get(24);var free=empty.at(core,front);
                l.setBlock(free,AncientAnvilContent.PART.get().defaultBlockState().setValue(AncientAnvilPartBlock.FACING,front).setValue(AncientAnvilPartBlock.CELL,24).setValue(AncientAnvilPartBlock.EXPANDED,true),3);
                AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));check(h,l.getBlockState(free).isAir()&&l.getBlockState(core).getValue(AncientAnvilBlock.EXPANDED),"Old invisible cells migrate without dismantling the anvil");
                l.setBlock(free,Blocks.CHEST.defaultBlockState(),3);AncientAnvilBlockEntity.refresh(l,core,l.getBlockState(core));check(h,l.getBlockState(free).is(Blocks.CHEST)&&l.getBlockState(core).getValue(AncientAnvilBlock.EXPANDED),"Free side cells can contain player blocks and stay intact");
                l.destroyBlock(core,false);check(h,l.getBlockState(free).is(Blocks.CHEST),"Breaking the anvil preserves neighboring blocks");
            }
            check(h,AnvilRecipes.readFluids(new com.google.gson.JsonObject()).isEmpty(),"Existing recipes need no fluid requirements");
            boolean invalid=false;try{AnvilRecipes.readFluids(JsonParser.parseString("{\"fluids\":{\"front\":{\"fluid\":\"missing:fluid\"}}}").getAsJsonObject());}catch(IllegalArgumentException expected){invalid=true;}check(h,invalid,"Unknown fluids are rejected, never treated as empty");
            System.out.println("ANVIL_BASIN_CHECK: PASS: all facings, independent fluids, split-front agreement, source/flow equivalence, ID/tag recipes, live downgrade, empty-cell migration and neighbor preservation");h.succeed();
        }
    }
    private static void check(GameTestHelper h,boolean ok,String why){if(!ok)throw h.assertionException(why);}
    private AnvilBasinTests(){}
}
