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
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import static com.example.examplemod.PickaxeTrait.*;

public final class ComponentWorkTests {
    public static void register(RegisterGameTestsEvent event){
        var env=event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"component_work_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"component_work"),new Test(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),300,0,true,Rotation.NONE,false,1,1,false,0)));
    }
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> data){super(data);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Ancient offers, permanent locks, terminal corruption and 120% component restoration");}
        @Override public void run(GameTestHelper h){
            discovery(h);locks(h);corruption(h);restoration(h);creative(h);
            System.out.println("COMPONENT_WORK_CHECK: PASS: Epic+ ancient rolls, three fixed legal offers, real excavation recovery, one-time unveiling, permanent lock protection, all nine corruption outcomes and terminal guards, 120% restoration, immutable source quality and assembled provenance");h.succeed();
        }
    }
    private static void discovery(GameTestHelper h){
        for(int rarity=0;rarity<6;rarity++)for(var find:CreativePickaxeParts.FINDS){
            int hidden=0;
            for(int seed=0;seed<200;seed++){
                var profile=new ArtifactProfile(seed*0x9E3779B97F4A7C15L,rarity,ArtifactProfile.Matrix.OBSIDIAN,find);var pool=ArtifactModifierCatalog.pool(find,rarity);var tag=ArtifactModifiers.record(profile,100,pool);var rolls=ArtifactModifiers.read(tag);
                check(h,tag.equals(ArtifactModifiers.record(profile,100,pool))&&rolls.size()==ArtifactModifiers.roll(profile,100,pool).size(),"Ancient discovery must be deterministic and replace one roll without inflating modifier counts");
                long count=rolls.stream().filter(AncientModifiers::ancient).count();check(h,count<=1,"A component can have at most one hidden ancient roll");
                if(count==1){hidden++;var choices=AncientModifiers.offers(tag);check(h,rarity>=3&&choices.size()==3&&choices.stream().map(ArtifactModifiers.Roll::id).distinct().count()==3,"Ancient slots must be Epic+ and offer three different modifiers");
                    var eligible=AncientModifiers.eligible(find,rarity,rolls);for(var choice:choices)check(h,eligible.stream().anyMatch(d->d.id().equals(choice.id())&&choice.value()>=d.minimumValue()&&choice.value()<=d.maximumValue()),"Unveiling must respect part roles, tier ranges and harvest exclusivity");
                }
            }
            check(h,rarity<3?hidden==0:hidden>15&&hidden<170,"Only Epic and higher may discover ancient modifiers, without guaranteeing them");
        }
        var part=ancient(ArtifactProfile.Find.PICKAXE_LEFT,3);var root=part.get(DataComponents.CUSTOM_DATA).copyTag();long seed=ComponentWork.recovered(part).getLongOr("Seed",0);
        var specimen=ArchaeologyContent.specimen(seed,3);new ArtifactProfile(seed,3,ArtifactProfile.Matrix.OBSIDIAN,ArtifactProfile.Find.PICKAXE_LEFT).write(specimen);var study=ExcavationStudy.read(specimen);
        for(int cell=0;cell<ExcavationStudy.CELLS;cell++)while(study.depth(cell)>1&&!study.complete())study.work(1,cell);for(int cell=0;cell<ExcavationStudy.CELLS;cell++)study.work(2,cell);
        part=ArchaeologyContent.reward(study,specimen);check(h,ComponentWork.offers(part).size()==3,"An actual completed Epic excavation must carry its hidden offers through recovery");
        var choices=ComponentWork.offers(part);var decoded=roundtrip(h,part);check(h,ComponentWork.offers(decoded).equals(choices),"Offers must survive item saves without rerolling");
        boolean[] gibberish={false};for(var line:PickaxeTooltips.piece(part))line.visit((style,text)->{if(style.isObfuscated())gibberish[0]=true;return Optional.empty();},net.minecraft.network.chat.Style.EMPTY);check(h,gibberish[0],"Ancient rolls must use real obfuscated Minecraft text");
        var choice=choices.get(1);check(h,ComponentWork.apply(ComponentStationKind.UNVEILER,part,1,RandomSource.create(5)),"A displayed revelation must be selectable");
        check(h,ArtifactModifiers.read(part).stream().noneMatch(AncientModifiers::ancient)&&ArtifactModifiers.read(part).stream().anyMatch(r->r.id().equals(choice.id())&&r.value()==choice.value()&&r.scaling()==choice.scaling())&&ComponentWork.offers(part).isEmpty(),"Unveiling must commit exactly the chosen modifier, value and grade and consume the hidden slot");
        var done=part.copy();check(h,!ComponentWork.apply(ComponentStationKind.UNVEILER,part,0,RandomSource.create())&&ItemStack.matches(done,part),"A revealed component must not unveil again");
    }
    private static void locks(GameTestHelper h){
        var part=component(ArtifactProfile.Find.TOOL_HANDLE,100,5,1);var original=ArtifactModifiers.read(part).getFirst();
        check(h,ComponentWork.apply(ComponentStationKind.LOCKER,part,0,RandomSource.create(1)),"A revealed modifier must be lockable");
        var locked=part.copy();check(h,!ComponentWork.apply(ComponentStationKind.LOCKER,part,1,RandomSource.create())&&ItemStack.matches(locked,part),"The permanent lock cannot be changed or replaced");
        for(int i=0;i<50;i++)check(h,ComponentWork.apply(ComponentStationKind.SCALER,part,0,RandomSource.create(i))&&ArtifactModifiers.read(part).getFirst().equals(original),"Scaling rerolls must leave a locked modifier exactly unchanged");
        while(ComponentWork.can(ComponentStationKind.ANULLER,part,0))check(h,ComponentWork.apply(ComponentStationKind.ANULLER,part,0,RandomSource.create(7)),"An unlocked modifier must be annulable");
        check(h,ArtifactModifiers.read(part).equals(List.of(original))&&ComponentWork.locked(part).equals(original.id()),"Annulment must preserve the locked modifier even when list indexes change");
        var remaining=part.copy();check(h,!ComponentWork.apply(ComponentStationKind.SCALER,part,0,RandomSource.create())&&ItemStack.matches(remaining,part),"A component with only its locked modifier has no eligible scaling reroll");
        var veiled=ancient(ArtifactProfile.Find.PICKAXE_LEFT,5);int index=0;for(var roll:ArtifactModifiers.read(veiled)){if(AncientModifiers.ancient(roll))break;index++;}check(h,!ComponentWork.can(ComponentStationKind.LOCKER,veiled,index),"A hidden slot must be revealed before it can be locked");
    }
    private static void corruption(GameTestHelper h){
        var template=component(ArtifactProfile.Find.TOOL_HANDLE,80,5,2);ComponentWork.apply(ComponentStationKind.LOCKER,template,0,RandomSource.create(2));var protectedRoll=ArtifactModifiers.read(template).getFirst();var seen=EnumSet.noneOf(ComponentCorruption.Outcome.class);
        for(int seed=0;seed<600;seed++){
            var part=template.copy();check(h,ComponentWork.apply(ComponentStationKind.CORRUPTOR,part,0,RandomSource.create(seed*719L)),"Every uncorrupted component must be able to corrupt once");
            seen.add(ComponentCorruption.Outcome.valueOf(ComponentWork.state(part).getStringOr("CorruptionOutcome","")));
            check(h,ComponentWork.corrupted(part)&&ArtifactModifiers.read(part).stream().anyMatch(protectedRoll::equals),"Every corruption outcome must preserve the locked roll and mark the part final");
            var terminal=part.copy();for(var kind:ComponentStationKind.values())for(int choice=0;choice<8;choice++)check(h,!ComponentWork.apply(kind,part,choice,RandomSource.create())&&ItemStack.matches(terminal,part),"Corrupted parts must reject every station, including restoration and lock changes");
            check(h,ItemStack.matches(part,roundtrip(h,part)),"Corrupted state and protected rolls must survive saves");
        }
        check(h,seen.size()==ComponentCorruption.Outcome.values().length,"The initial corruption table must exercise every good, bad and unchanged outcome");
    }
    private static void restoration(GameTestHelper h){
        var parts=new ArrayList<ItemStack>();
        for(var find:CreativePickaxeParts.FINDS){var part=component(find,50,5,3);var source=ComponentWork.recovered(part).copy();
            while(ComponentWork.can(ComponentStationKind.RESTORER,part,0))check(h,ComponentWork.apply(ComponentStationKind.RESTORER,part,0,RandomSource.create()),"A mutable component must restore in bounded steps");
            check(h,ArtifactQuality.score(part)==120&&ComponentWork.recovered(part).equals(source),"Restoration must cap at 120 while retaining the actual original excavation record");
            var capped=part.copy();check(h,!ComponentWork.apply(ComponentStationKind.RESTORER,part,0,RandomSource.create())&&ItemStack.matches(capped,part),"Restoration at 120 must be a no-op");parts.add(part);
        }
        var tool=ArtifactQuality.assemble(parts);check(h,ArtifactQuality.assemblyScore(tool)==120,"An assembled average above 100 must not be clamped away");
        double expected=0;for(var part:parts)for(var roll:ArtifactModifiers.read(part))if(roll.id().equals(SPEED.id))expected+=roll.value()*1.2;
        check(h,Math.abs(PickaxeModifiers.totals(tool).get(SPEED)-Math.min(SPEED.cap,expected))<1e-9,"Each restored piece must contribute its full 120% value after assembly");
        var assembly=tool.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY).getCompoundOrEmpty("Parts");for(String id:ReconstructionPattern.PICKAXE.parts)check(h,assembly.getCompoundOrEmpty(id).getIntOr("Score",0)==120&&assembly.getCompoundOrEmpty(id).getCompoundOrEmpty("Customization").getIntOr("Quality",0)==120,"Assembly must remember each part's customization provenance");
        var fixed=tool.copy();for(var kind:ComponentStationKind.values())check(h,!ComponentWork.component(tool)&&!ComponentWork.apply(kind,tool,0,RandomSource.create())&&ItemStack.matches(fixed,tool),"Every workshop must reject finished tools without changing them");
        check(h,ItemStack.matches(tool,roundtrip(h,tool)),"Restored assembled quality and provenance must survive saves");
    }
    private static void creative(GameTestHelper h){
        var examples=CreativeAncientParts.variants();check(h,examples.size()==5,"Creative must provide one guaranteed ancient sample for every live pickaxe component role");
        var player=h.makeMockServerPlayerInLevel();player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        for(var example:examples){new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(36,example.copy()).handle(player.connection);check(h,ItemStack.matches(player.getMainHandItem(),example)&&ComponentWork.offers(player.getMainHandItem()).size()==3&&ArtifactQuality.score(example)==80,"Actual Creative inventory packets must preserve each sample's fixed offers and restorable quality");}
    }
    static ItemStack ancient(ArtifactProfile.Find find,int rarity){for(long seed=0;seed<1000;seed++){var part=natural(find,100,rarity,seed);if(!ComponentWork.offers(part).isEmpty())return part;}throw new IllegalStateException("No ancient seed found");}
    static ItemStack natural(ArtifactProfile.Find find,int score,int rarity,long seed){var stack=component(find,score,rarity,seed);var root=stack.get(DataComponents.CUSTOM_DATA).copyTag();var profile=new ArtifactProfile(seed,rarity,ArtifactProfile.Matrix.OBSIDIAN,find);root.put(ArtifactModifiers.KEY,ArtifactModifiers.record(profile,score,ArtifactModifierCatalog.pool(find,rarity)));stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));return stack;}
    static ItemStack component(ArtifactProfile.Find find,int score,int rarity,long seed){
        var stack=new ItemStack(ReconstructionContent.reward(find));var root=new CompoundTag();var recovered=new CompoundTag();recovered.putInt("Quality",score);recovered.putInt("Rarity",rarity);recovered.putLong("Seed",seed);recovered.putString("Find",find.name());root.put(ArtifactQuality.RECOVERED,recovered);
        var tag=new CompoundTag();tag.putInt("Version",1);tag.putLong("Seed",seed);var rolls=List.of(new ArtifactModifiers.Roll(SPEED.id,.1,score/100.0,ModifierScaling.B),new ArtifactModifiers.Roll(DURABILITY.id,.2,score/100.0,ModifierScaling.B),new ArtifactModifiers.Roll(REACH.id,.4,score/100.0,ModifierScaling.B));root.put(ArtifactModifiers.KEY,ArtifactModifiers.writeRolls(tag,rolls));stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));return stack;
    }
    private static ItemStack roundtrip(GameTestHelper h,ItemStack stack){var ops=h.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);return ItemStack.CODEC.parse(ops,ItemStack.CODEC.encodeStart(ops,stack).getOrThrow()).getOrThrow();}
    private static void check(GameTestHelper h,boolean value,String message){if(!value)throw h.assertionException(message);}
    private ComponentWorkTests(){}
}
