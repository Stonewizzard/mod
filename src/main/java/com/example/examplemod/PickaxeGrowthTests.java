package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import static com.example.examplemod.PickaxeTrait.*;

public final class PickaxeGrowthTests {
    public static void register(RegisterGameTestsEvent event){
        var env=event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"pickaxe_growth_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"pickaxe_growth"),new Test(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),200,0,true,Rotation.NONE,false,1,1,false,0)));
    }
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> data){super(data);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Per-roll grades, additive growth and compatible saved tools");}
        @Override public void run(GameTestHelper h){
            verifyGrades(h);verifyAscensions(h);verifyLevels(h);verifyLegacy(h);verifyCreative(h);
            System.out.println("PICKAXE_GROWTH_CHECK: PASS: independent F-S grade rolls and tier weights, per-roll quality and grades, all 99 normal levels, 1-3 distinct selections, non-compounding formulas, no XP-only/gated growth, native stat refresh, stable future growth after serialization and legacy B migration");h.succeed();
        }
    }
    private static void verifyGrades(GameTestHelper h){
        double oldAsc=0,oldLevel=0;
        for(var grade:ModifierScaling.values()){check(h,grade.ascensionGrowth>oldAsc&&grade.levelGrowth>oldLevel,"Every grade must improve both ascension and normal-level scaling");oldAsc=grade.ascensionGrowth;oldLevel=grade.levelGrowth;}
        double common=0,unique=0;var seen=EnumSet.noneOf(ModifierScaling.class);
        for(int i=0;i<4000;i++){
            long seed=i*0x9E3779B97F4A7C15L;var profile=new ArtifactProfile(seed,0,ArtifactProfile.Matrix.SHALE,ArtifactProfile.Find.PICKAXE_LEFT);
            var low=ModifierScaling.roll(profile,SPEED.id);seen.add(low);common+=low.ordinal();unique+=ModifierScaling.roll(new ArtifactProfile(seed,5,profile.matrix(),profile.find()),SPEED.id).ordinal();
            check(h,ModifierScaling.roll(profile,SPEED.id)==low,"Grade seeds must remain deterministic");
            var record=ArtifactModifiers.record(profile,50,ArtifactModifierCatalog.pool(profile.find(),0));var parsed=ArtifactModifiers.read(record);
            check(h,parsed.equals(ArtifactModifiers.roll(profile,50,ArtifactModifierCatalog.pool(profile.find(),0)))&&record.getCompoundOrEmpty("Rolls").getCompoundOrEmpty("0").contains("Scaling"),"Every fresh roll must save its grade together with its original value");
        }
        check(h,seen.size()==6&&unique>common*2,"All grades must be possible, with substantially better average grades at high rarity");
    }
    private static void verifyAscensions(GameTestHelper h){
        for(var grade:ModifierScaling.values())for(int score:new int[]{0,50,100}){
            var tool=tool(grade,score,181);var parts=assembly(tool).copy();double base=.2*score/100.0;
            PickaxeProgression.write(tool,new PickaxeProgression.State(10,0,0));check(h,PickaxeProgression.ascend(tool),"A gated graded tool must ascend");
            check(h,Math.abs(PickaxeModifiers.totals(tool).get(SPEED)-base*(1+grade.ascensionGrowth))<1e-10,"Ascension must scale each original roll by its own grade and quality");
            PickaxeProgression.write(tool,new PickaxeProgression.State(20,0,1));check(h,PickaxeProgression.ascend(tool)&&Math.abs(PickaxeModifiers.totals(tool).get(SPEED)-base*(1+2*grade.ascensionGrowth))<1e-10,"Ascension growth must add, never compound");
            check(h,assembly(tool).equals(parts),"Ascension must never edit sealed part records");
        }
        var mixed=tool(ModifierScaling.S,50,182);var root=mixed.get(DataComponents.CUSTOM_DATA).copyTag();var assembly=root.getCompoundOrEmpty(ArtifactQuality.ASSEMBLY);var parts=assembly.getCompoundOrEmpty("Parts");var bridge=parts.getCompoundOrEmpty(ReconstructionPattern.PICKAXE.parts.get(1));
        bridge.putInt("Score",100);var record=bridge.getCompoundOrEmpty("Modifiers");bridge.put("Modifiers",ArtifactModifiers.writeRolls(record,List.of(new ArtifactModifiers.Roll(SPEED.id,.1,1,ModifierScaling.F))));parts.put(ReconstructionPattern.PICKAXE.parts.get(1),bridge);assembly.put("Parts",parts);root.put(ArtifactQuality.ASSEMBLY,assembly);mixed.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
        PickaxeProgression.write(mixed,new PickaxeProgression.State(10,0,0));PickaxeProgression.ascend(mixed);
        check(h,Math.abs(PickaxeModifiers.totals(mixed).get(SPEED)-(.1*1.2+.1*1.03))<1e-10,"Two copies of a trait must retain independent scores and scaling grades");
    }
    private static void verifyLevels(GameTestHelper h){
        for(var grade:new ModifierScaling[]{ModifierScaling.F,ModifierScaling.S}){
            var tool=tool(grade,100,123456);var frozen=assembly(tool).copy();var counts=new HashSet<Integer>();
            var before=tool.copy();PickaxeProgression.gain(tool,1);check(h,growth(before).equals(growth(tool)),"Partial XP must not grant modifier growth");
            for(int target=2;target<=100;target++){
                if(PickaxeProgression.read(tool).needsAscension()){var counters=growth(tool).copy();PickaxeProgression.ascend(tool);check(h,counters.equals(growth(tool)),"Ascension must not also award normal-level growth");}
                var old=PickaxeModifiers.contributions(tool);var current=PickaxeProgression.read(tool);PickaxeProgression.gain(tool,PickaxeProgression.xpRequired(current.level())-current.xp());
                check(h,PickaxeProgression.read(tool).level()==target,"Each full XP requirement must gain exactly one level");
                int changed=0;var now=PickaxeModifiers.contributions(tool);
                for(int i=0;i<now.size();i++){int delta=now.get(i).levelHits()-old.get(i).levelHits();check(h,delta==0||delta==1,"A modifier can grow at most once per normal level");changed+=delta;}
                check(h,changed>=1&&changed<=3,"Every normal level must select one to three distinct positive modifiers");counts.add(changed);
                for(var c:now)check(h,Math.abs(PickaxeModifiers.totals(tool).get(c.trait())-c.base()*(1+current.ascensions()*grade.ascensionGrowth+c.levelHits()*grade.levelGrowth))<1e-9,"Level growth must add a small grade-scaled fraction of the original quality-adjusted roll");
                double reach=tool.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE,4.5,net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                check(h,Math.abs(reach-4.5-PickaxeModifiers.totals(tool).get(REACH))<1e-9&&tool.getMaxDamage()==(int)Math.round(1800*(1+PickaxeModifiers.totals(tool).get(DURABILITY))),"Native reach and durability must update as soon as a level grants growth");
                check(h,assembly(tool).equals(frozen),"Normal leveling must preserve every sealed raw roll and score");
                if(target==31){
                    var decoded=roundtrip(h,tool);int xp=PickaxeProgression.xpRequired(31);var copy=tool.copy();PickaxeProgression.gain(copy,xp);PickaxeProgression.gain(decoded,xp);check(h,ItemStack.matches(copy,decoded),"Saved tools must continue with the exact same future modifier selections");
                }
                if(PickaxeProgression.read(tool).needsAscension()){var paused=tool.copy();check(h,!PickaxeProgression.gain(tool,999)&&ItemStack.matches(paused,tool),"Unpaid gates must award neither XP nor growth");}
            }
            check(h,counts.size()==3&&PickaxeProgression.read(tool).level()==100,"The full leveling sequence must exercise one, two and three selections");
        }
        var zero=tool(ModifierScaling.S,0,123);PickaxeProgression.gain(zero,20);check(h,growth(zero).isEmpty()&&PickaxeModifiers.totals(zero).values().values().stream().allMatch(v->v==0),"Scaling and level growth must never revive a zero-quality part");
    }
    private static void verifyLegacy(GameTestHelper h){
        var tool=tool(ModifierScaling.B,50,909);var root=tool.get(DataComponents.CUSTOM_DATA).copyTag();var assembly=root.getCompoundOrEmpty(ArtifactQuality.ASSEMBLY);var parts=assembly.getCompoundOrEmpty("Parts");
        for(String id:ReconstructionPattern.PICKAXE.parts){var part=parts.getCompoundOrEmpty(id);var record=part.getCompoundOrEmpty("Modifiers");var rolls=record.getCompoundOrEmpty("Rolls");for(int i=0;i<record.getIntOr("Count",0);i++){var roll=rolls.getCompoundOrEmpty(""+i);roll.remove("Scaling");rolls.put(""+i,roll);}record.put("Rolls",rolls);part.put("Modifiers",record);parts.put(id,part);}
        assembly.put("Parts",parts);root.put(ArtifactQuality.ASSEMBLY,assembly);root.remove(PickaxeTooltips.REVISION_KEY);var progression=root.getCompoundOrEmpty(PickaxeProgression.KEY);progression.putInt("Version",1);progression.putInt("Level",30);progression.putInt("Ascensions",2);progression.remove("GrowthSeed");root.put(PickaxeProgression.KEY,progression);tool.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
        var frozen=assembly.copy();check(h,PickaxeTooltips.refresh(tool)&&Math.abs(PickaxeModifiers.totals(tool).get(SPEED)-.12)<1e-10,"Legacy modifiers must retain B's original +10% ascension rate");
        check(h,PickaxeProgression.read(tool).level()==30&&PickaxeProgression.read(tool).ascensions()==2&&growth(tool).isEmpty()&&assembly(tool).equals(frozen),"Tooltip refresh must not reroll grades, retroactively grant growth, or reset earned progress");
        var refreshed=tool.copy();check(h,!PickaxeTooltips.refresh(tool)&&ItemStack.matches(refreshed,tool),"Presentation migration must happen once");
        check(h,ItemStack.matches(tool,roundtrip(h,tool)),"Legacy data and refreshed styles must survive item serialization");
        root=tool.get(DataComponents.CUSTOM_DATA).copyTag();root.remove(PickaxeTooltips.REVISION_KEY);tool.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
        var player=h.makeMockServerPlayerInLevel();var inventory=new net.minecraft.world.SimpleContainer(AscenderBlockEntity.SIZE);inventory.setItem(0,tool);player.containerMenu=new AscenderMenu(79,player.getInventory(),inventory);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        check(h,tool.get(DataComponents.CUSTOM_DATA).copyTag().getIntOr(PickaxeTooltips.REVISION_KEY,0)==PickaxeTooltips.REVISION&&assembly(tool).equals(frozen),"Open-container items must get the same safe one-time presentation refresh as player inventory items");
    }
    private static void verifyCreative(GameTestHelper h){
        var tools=CreativeGrowthTools.variants();var player=h.makeMockServerPlayerInLevel();var pos=h.absolutePos(new BlockPos(3,1,5));player.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);check(h,tools.size()==6,"Creative must offer one comparison tool per scaling grade");
        for(int i=0;i<tools.size();i++){
            var tool=tools.get(i);var state=PickaxeProgression.read(tool);var grade=ModifierScaling.values()[i];check(h,state.level()==9&&state.xp()==PickaxeProgression.xpRequired(9)-1&&PickaxeModifiers.contributions(tool).stream().allMatch(c->c.roll().scaling()==grade),"Comparison tools must have a consistent grade and be one mined block from an ascension gate");
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(36,tool.copy()).handle(player.connection);
            check(h,ItemStack.matches(player.getMainHandItem(),tool),"A real Creative inventory packet must preserve each comparison tool's grade and saved progress");
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);h.getLevel().setBlockAndUpdate(pos,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());player.gameMode.destroyBlock(pos);
            check(h,PickaxeProgression.read(player.getMainHandItem()).level()==10&&!growth(player.getMainHandItem()).isEmpty(),"One native Survival mining action must level an acquired comparison tool and grant grade-scaled growth");
        }
    }
    static ItemStack tool(ModifierScaling grade,int score,long seed){return CreativeGrowthTools.create(grade,score,seed);}
    private static CompoundTag growth(ItemStack tool){return tool.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(PickaxeProgression.KEY).getCompoundOrEmpty(PickaxeGrowth.COUNTERS);}
    private static CompoundTag assembly(ItemStack tool){return tool.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY);}
    private static ItemStack roundtrip(GameTestHelper h,ItemStack tool){var ops=h.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);return ItemStack.CODEC.parse(ops,ItemStack.CODEC.encodeStart(ops,tool).getOrThrow()).getOrThrow();}
    private static void check(GameTestHelper h,boolean condition,String message){if(!condition)throw h.assertionException(message);}
    private PickaxeGrowthTests(){}
}
