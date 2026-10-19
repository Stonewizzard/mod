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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import static com.example.examplemod.PickaxeTrait.*;

/** Influence assignment, weighted pools, workshop restrictions, and real final-drop collection. */
public final class InfluenceTests {
    public static void register(RegisterGameTestsEvent event) {
        var env=event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"influence_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"influences"),new Test(
            new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),500,0,true,Rotation.NONE,false,1,1,false,0)));
    }
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> data){super(data);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Permanent influence assignment, themed weighted pools and Phasebound mining");}
        @Override public void run(GameTestHelper h){
            defaults(h);pools(h);assignment(h);locks(h);unveiling(h);phasebound(h);
            System.out.println("INFLUENCE_CHECK: PASS: uninfluenced natural/Creative parts, optional tags, sixfold matching weights, zero foreign/exclusive leaks, permanent assignment, immutable quality/provenance/locks, influenced offers/corruption/saves/assembly, Phasebound native drops and full/partial inventories");
            h.succeed();
        }
    }
    private static void defaults(GameTestHelper h){
        boolean tagged=false;
        for(var find:CreativePickaxeParts.FINDS)for(int rarity=0;rarity<6;rarity++){
            for(int i=0;i<40;i++){
                var part=ComponentWorkTests.natural(find,80,rarity,i*719L);
                check(h,ComponentInfluences.current(part)==ModifierInfluence.NONE&&!ComponentWork.state(part).contains(ComponentInfluences.FIELD),"Excavation must never assign an influence");
                for(var roll:ArtifactModifiers.read(part))if(!AncientModifiers.ancient(roll)){
                    check(h,!roll.id().equals(PHASEBOUND.id),"Influence-only modifiers must never appear naturally");
                    if(ArtifactModifierCatalog.tag(roll.id())!=ModifierInfluence.NONE)tagged=true;
                }
            }
            var creative=CreativePickaxeParts.template(find,100);CreativePickaxeParts.materialize(creative,RandomSource.create(rarity+20));
            check(h,ComponentInfluences.current(creative)==ModifierInfluence.NONE,"Random Creative acquisition must also start uninfluenced");
        }
        check(h,tagged&&ArtifactModifierCatalog.spec(SPEED).influence()==ModifierInfluence.NONE,"Tagged modifiers may appear on uninfluenced parts; ordinary modifiers stay neutral");
        for(var sample:CreativeAncientParts.variants())check(h,ComponentInfluences.current(sample)==ModifierInfluence.NONE,"Ancient Creative samples must not bypass the Influencer");
    }
    private static void pools(GameTestHelper h){
        for(var influence:ModifierInfluence.values())for(int rarity=0;rarity<6;rarity++)for(var find:CreativePickaxeParts.FINDS){
            var pool=ArtifactModifierCatalog.pool(find,rarity,influence);
            for(var entry:pool.entries()){
                var spec=ArtifactModifierCatalog.spec(PickaxeTrait.byId(entry.id()));
                check(h,influence.allows(spec.influence(),spec.influenceOnly()),"Foreign influence and exclusive eligibility must be filtered before rolling");
                int base=ArtifactModifierCatalog.weights(find).get(PickaxeTrait.byId(entry.id()));
                check(h,entry.weight()==influence.weight(base,spec.influence()),"Only matching tags should receive the sixfold family weight");
            }
            for(int i=0;i<35;i++){
                var profile=new ArtifactProfile(i*0x9E3779B97F4A7C15L,rarity,ArtifactProfile.Matrix.SHALE,find);
                var record=ArtifactModifiers.record(profile,100,pool,influence,List.of());
                validate(h,influence,rarity,ArtifactModifiers.read(record),AncientModifiers.offers(record));
            }
        }
        var find=ArtifactProfile.Find.PICKAXE_RIGHT;
        int ordinary=0,nether=0,end=0;int n=6000;
        for(int i=0;i<n;i++){
            var profile=new ArtifactProfile(i*0x9E3779B97F4A7C15L,5,ArtifactProfile.Matrix.SHALE,find);
            if(ArtifactModifiers.roll(profile,100,ArtifactModifierCatalog.pool(find,5)).getFirst().id().equals(SMELTING.id))ordinary++;
            if(ArtifactModifiers.roll(profile,100,ArtifactModifierCatalog.pool(find,5,ModifierInfluence.NETHER)).getFirst().id().equals(SMELTING.id))nether++;
            if(ArtifactModifiers.roll(profile,100,ArtifactModifierCatalog.pool(find,5,ModifierInfluence.END)).getFirst().id().equals(PHASEBOUND.id))end++;
        }
        check(h,nether>ordinary*2.5&&Math.abs(nether/(double)n-72.0/174)<.025&&Math.abs(end/(double)n-72.0/174)<.025,
            "Observed themed rolls must follow boosted relative weights without becoming guaranteed");
    }
    private static void assignment(GameTestHelper h){
        var part=ComponentWorkTests.component(ArtifactProfile.Find.PICKAXE_RIGHT,80,5,71);
        var source=ComponentWork.recovered(part).copy();
        while(ComponentWork.can(ComponentStationKind.RESTORER,part,0))ComponentWork.apply(ComponentStationKind.RESTORER,part,0,RandomSource.create(2));
        check(h,ComponentWork.apply(ComponentStationKind.INFLUENCER,part,0,RandomSource.create(6)),"A recovered piece must accept its first permanent assignment");
        check(h,ComponentInfluences.current(part)==ModifierInfluence.NETHER&&ArtifactQuality.score(part)==120&&ComponentWork.recovered(part).equals(source),"Assignment must keep original excavation data and restored quality");
        for(int i=0;i<40;i++){
            var before=part.copy();
            check(h,!ComponentWork.apply(ComponentStationKind.INFLUENCER,part,1,RandomSource.create(i))&&ItemStack.matches(before,part),"An assigned influence must never change, even with a fresh request");
            check(h,!ComponentWork.apply(ComponentStationKind.INFLUENCER,part,-1,RandomSource.create(i))&&ItemStack.matches(before,part),"No uninfluence action exists");
            check(h,ComponentWork.apply(ComponentStationKind.INFLUENCER,part,0,RandomSource.create(i*911L)),"Same-influence rerolls must remain available");
            check(h,ArtifactQuality.score(part)==120&&ComponentWork.recovered(part).equals(source),"Repeated rerolls must preserve quality and original provenance");
            validate(h,ModifierInfluence.NETHER,5,ArtifactModifiers.read(part),ComponentWork.offers(part));
        }
        check(h,ItemStack.matches(part,roundtrip(h,part)),"Permanent influence and fixed choices must survive native item saves");
        var annulled=part.copy();
        while(ComponentWork.can(ComponentStationKind.ANULLER,annulled,0))ComponentWork.apply(ComponentStationKind.ANULLER,annulled,0,RandomSource.create(7));
        check(h,ArtifactModifiers.read(annulled).isEmpty()&&ComponentInfluences.current(annulled)==ModifierInfluence.NETHER
            &&!ComponentWork.apply(ComponentStationKind.INFLUENCER,annulled,1,RandomSource.create())
            &&ComponentWork.apply(ComponentStationKind.INFLUENCER,annulled,0,RandomSource.create(8)),"Annulment of every modifier must not clear or change the permanent influence");
        for(int seed=0;seed<120;seed++){
            var corrupted=part.copy();ComponentWork.apply(ComponentStationKind.CORRUPTOR,corrupted,0,RandomSource.create(seed*719L));
            validate(h,ModifierInfluence.NETHER,5,ArtifactModifiers.read(corrupted),ComponentWork.offers(corrupted));
            check(h,ComponentInfluences.current(corrupted)==ModifierInfluence.NETHER&&!ComponentWork.can(ComponentStationKind.INFLUENCER,corrupted,0),"Corruption must keep influence and permanently close the Influencer");
        }
        var unknown=part.copy();var root=unknown.get(DataComponents.CUSTOM_DATA).copyTag();var state=ComponentWork.state(unknown);state.putString(ComponentInfluences.FIELD,"future_influence");root.put(ComponentWork.KEY,state);unknown.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
        var saved=unknown.copy();check(h,!ComponentWork.apply(ComponentStationKind.INFLUENCER,unknown,0,RandomSource.create())&&ItemStack.matches(saved,unknown)&&ItemStack.matches(saved,roundtrip(h,unknown)),"Unknown future influence IDs must be preserved, not reinterpreted as uninfluenced");
    }
    private static void locks(GameTestHelper h){
        var conflicting=component(ArtifactProfile.Find.PICKAXE_RIGHT,5,List.of(new ArtifactModifiers.Roll(SMELTING.id,.3,.8,ModifierScaling.S,"t3",3)));
        ComponentWork.apply(ComponentStationKind.LOCKER,conflicting,0,RandomSource.create());
        var before=conflicting.copy();check(h,!ComponentWork.apply(ComponentStationKind.INFLUENCER,conflicting,1,RandomSource.create())&&ItemStack.matches(before,conflicting),"A foreign locked modifier must block an initial incompatible assignment");
        var fixed=ArtifactModifiers.read(conflicting).getFirst();
        for(int i=0;i<80;i++){
            check(h,ComponentWork.apply(ComponentStationKind.INFLUENCER,conflicting,0,RandomSource.create(i*991L))&&ArtifactModifiers.read(conflicting).contains(fixed),"Rerolls and ancient replacement must never alter or hide the locked roll");
        }
        var neutral=component(ArtifactProfile.Find.PICKAXE_LEFT,0,List.of(new ArtifactModifiers.Roll(SPEED.id,.1,.8,ModifierScaling.A,"t6",6)));
        ComponentWork.apply(ComponentStationKind.LOCKER,neutral,0,RandomSource.create());
        var roll=ArtifactModifiers.read(neutral).getFirst();
        check(h,ComponentWork.apply(ComponentStationKind.INFLUENCER,neutral,1,RandomSource.create(8))&&ArtifactModifiers.read(neutral).equals(List.of(roll)),"A neutral lock may accept an influence while consuming the guaranteed modifier slot");
        check(h,ComponentInfluences.current(neutral)==ModifierInfluence.END,"A locked Common part must still retain the chosen permanent influence");
    }
    private static void unveiling(GameTestHelper h){
        for(var influence:ModifierInfluence.CHOICES){
            boolean found=false;
            for(int seed=0;seed<120&&!found;seed++){
                var part=ComponentWorkTests.component(ArtifactProfile.Find.PICKAXE_RIGHT,80,5,seed);
                ComponentWork.apply(ComponentStationKind.INFLUENCER,part,ModifierInfluence.CHOICES.indexOf(influence),RandomSource.create(seed*719L));
                var offers=ComponentWork.offers(part);if(offers.isEmpty())continue;found=true;
                check(h,offers.equals(ComponentWork.offers(roundtrip(h,part))),"Influenced ancient choices must remain fixed across saves");
                var choice=offers.getFirst();
                check(h,ComponentWork.apply(ComponentStationKind.UNVEILER,part,0,RandomSource.create())&&ArtifactModifiers.read(part).contains(choice.withQuality(.8)),"Unveiling must commit the exact influenced offer");
                validate(h,influence,5,ArtifactModifiers.read(part),ComponentWork.offers(part));
            }
            check(h,found,"Influenced parts must still be able to discover ancient choices");
        }
        var part=component(ArtifactProfile.Find.PICKAXE_RIGHT,5,List.of(new ArtifactModifiers.Roll(AncientModifiers.ID,0,.8)));
        var root=part.get(DataComponents.CUSTOM_DATA).copyTag();var record=root.getCompoundOrEmpty(ArtifactModifiers.KEY);var choices=new CompoundTag();choices.putInt("Version",1);
        var offered=List.of(new ArtifactModifiers.Roll(SMELTING.id,.3,1,ModifierScaling.B,"t3",3),new ArtifactModifiers.Roll(SPEED.id,.2,1,ModifierScaling.B,"t5",5),new ArtifactModifiers.Roll(EXPERIENCE.id,.3,1,ModifierScaling.B,"t4",4));
        record.put(AncientModifiers.OFFERS,ArtifactModifiers.writeRolls(choices,offered));root.put(ArtifactModifiers.KEY,record);part.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
        check(h,ComponentWork.apply(ComponentStationKind.UNVEILER,part,0,RandomSource.create())&&ArtifactModifiers.read(part).getFirst().id().equals(SMELTING.id)&&ComponentInfluences.current(part)==ModifierInfluence.NONE,"Unveiling a saved tagged modifier must not assign influence");
    }
    private static void phasebound(GameTestHelper h){
        var player=h.makeMockServerPlayerInLevel();player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        var pos=h.absolutePos(new BlockPos(3,1,3));player.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        var tool=tool(100);player.getInventory().clearContent();player.setItemInHand(InteractionHand.MAIN_HAND,tool);
        var unrelated=new ItemEntity(h.getLevel(),player.getX()+2,player.getY(),player.getZ(),new ItemStack(Items.DIAMOND,20));h.getLevel().addFreshEntity(unrelated);
        var event=drop(h,player,pos,tool,new ItemStack(Items.DIAMOND,4));
        check(h,event.getDrops().isEmpty()&&player.getInventory().countItem(Items.DIAMOND)==4&&unrelated.isAlive()&&unrelated.getItem().getCount()==20,"Phasebound must move final drops directly to inventory once without touching unrelated world items");unrelated.discard();
        fill(player,tool);event=drop(h,player,pos,tool,new ItemStack(Items.DIAMOND,3));
        check(h,event.getDrops().size()==1&&event.getDrops().getFirst().getItem().getCount()==3&&player.getInventory().countItem(Items.DIAMOND)==0,"Full inventories must leave every drop in the world");
        player.getInventory().setItem(1,new ItemStack(Items.DIAMOND,63));event=drop(h,player,pos,tool,new ItemStack(Items.DIAMOND,3));
        check(h,player.getInventory().countItem(Items.DIAMOND)==64&&event.getDrops().size()==1&&event.getDrops().getFirst().getItem().getCount()==2,"Partial collection must leave precisely the uncollected remainder");
        player.getInventory().clearContent();event=drop(h,player,pos,tool(0),new ItemStack(Items.DIAMOND,3));
        check(h,event.getDrops().size()==1&&player.getInventory().countItem(Items.DIAMOND)==0,"Zero-quality Phasebound must have zero effect");
        int collected=0;var half=tool(50);
        for(int i=0;i<180;i++){player.getInventory().clearContent();event=drop(h,player,pos,half,new ItemStack(Items.DIAMOND));collected+=player.getInventory().countItem(Items.DIAMOND);}
        check(h,collected>55&&collected<125,"Quality must scale Phasebound's collection probability");

        var parts=parts(100);var nether=parts.getFirst();ComponentWork.apply(ComponentStationKind.INFLUENCER,nether,0,RandomSource.create(5));setRolls(nether,List.of(new ArtifactModifiers.Roll(SMELTING.id,1,1)));
        var combined=ArtifactQuality.assemble(parts);player.getInventory().clearContent();event=drop(h,player,pos,combined,new ItemStack(Items.RAW_IRON,2));
        check(h,event.getDrops().isEmpty()&&player.getInventory().countItem(Items.IRON_INGOT)==2&&event.getDroppedExperience()>0,"Phasebound must collect the post-smelting output without losing furnace XP");
        var provenance=combined.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY).getCompoundOrEmpty("Parts");
        check(h,provenance.getCompoundOrEmpty(ReconstructionPattern.PICKAXE.parts.get(2)).getCompoundOrEmpty("Customization").getStringOr(ComponentInfluences.FIELD,"").equals("end"),"Assembly must preserve each component's individual influence");
        var frozen=combined.copy();check(h,!ComponentWork.apply(ComponentStationKind.INFLUENCER,combined,0,RandomSource.create())&&ItemStack.matches(frozen,combined)&&ItemStack.matches(combined,roundtrip(h,combined)),"Finished tools must reject influence operations and retain saved part influence");
        check(h,AscensionResources.requirements(combined).stream().anyMatch(r->r.item()==Items.ENDER_PEARL),"Phasebound must have an ascension resource binding");

        player.getInventory().clearContent();player.setItemInHand(InteractionHand.MAIN_HAND,tool);h.getLevel().setBlockAndUpdate(pos,Blocks.DIAMOND_ORE.defaultBlockState());
        check(h,player.gameMode.destroyBlock(pos)&&player.getInventory().countItem(Items.DIAMOND)>0,"A real native mining action must execute direct collection");
    }
    private static ArrayList<ItemStack> parts(int score){
        var parts=new ArrayList<ItemStack>();
        for(var find:CreativePickaxeParts.FINDS){var part=ComponentWorkTests.component(find,score,5,45);setRolls(part,List.of());parts.add(part);}
        var right=parts.get(2);ComponentWork.apply(ComponentStationKind.INFLUENCER,right,1,RandomSource.create(4));setRolls(right,List.of(new ArtifactModifiers.Roll(PHASEBOUND.id,1,score/100.0)));return parts;
    }
    private static ItemStack tool(int score){return ArtifactQuality.assemble(parts(score));}
    private static ItemStack component(ArtifactProfile.Find find,int rarity,List<ArtifactModifiers.Roll> rolls){var part=ComponentWorkTests.component(find,80,rarity,7);setRolls(part,rolls);return part;}
    private static void setRolls(ItemStack part,List<ArtifactModifiers.Roll> rolls){var root=part.get(DataComponents.CUSTOM_DATA).copyTag();var record=root.getCompoundOrEmpty(ArtifactModifiers.KEY);root.put(ArtifactModifiers.KEY,ArtifactModifiers.writeRolls(record,rolls));part.set(DataComponents.CUSTOM_DATA,CustomData.of(root));}
    private static BlockDropsEvent drop(GameTestHelper h,net.minecraft.server.level.ServerPlayer player,BlockPos pos,ItemStack tool,ItemStack output){
        var drops=new ArrayList<ItemEntity>();drops.add(new ItemEntity(h.getLevel(),pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,output));
        var event=new BlockDropsEvent(h.getLevel(),pos,Blocks.DIAMOND_ORE.defaultBlockState(),null,drops,player,tool);NeoForge.EVENT_BUS.post(event);return event;
    }
    private static void fill(net.minecraft.server.level.ServerPlayer player,ItemStack tool){player.getInventory().clearContent();for(int i=0;i<player.getInventory().getContainerSize();i++)player.getInventory().setItem(i,new ItemStack(Items.STONE,64));player.getInventory().setItem(0,tool);}
    private static void validate(GameTestHelper h,ModifierInfluence influence,int rarity,List<ArtifactModifiers.Roll> rolls,List<ArtifactModifiers.Roll> offers){
        for(var list:List.of(rolls,offers))for(var roll:list){if(AncientModifiers.ancient(roll))continue;var trait=PickaxeTrait.byId(roll.id());var spec=ArtifactModifierCatalog.spec(trait);
            check(h,spec!=null&&influence.allows(spec.influence(),spec.influenceOnly())&&rarity>=spec.minimumRarity(),"Every rolled and offered modifier must respect influence and rarity eligibility");}
    }
    private static ItemStack roundtrip(GameTestHelper h,ItemStack stack){var ops=h.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);return ItemStack.CODEC.parse(ops,ItemStack.CODEC.encodeStart(ops,stack).getOrThrow()).getOrThrow();}
    private static void check(GameTestHelper h,boolean value,String message){if(!value)throw h.assertionException(message);}
    private InfluenceTests(){}
}
