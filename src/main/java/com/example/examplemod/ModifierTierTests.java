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

/** Distribution tests use fixed seeds and wide statistical margins, not exact implementation sequences. */
public final class ModifierTierTests {
    public static void register(RegisterGameTestsEvent event) {
        var env = event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"modifier_tiers_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"modifier_tiers"),
            new Test(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),400,0,true,Rotation.NONE,false,1,1,false,0)));
    }
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> data) { super(data); }
        @Override public MapCodec<? extends GameTestInstance> codec() { return MapCodec.unit(this); }
        @Override protected MutableComponent typeDescription() { return Component.literal("Weighted value tiers, variable tier counts and saved component compatibility"); }
        @Override public void run(GameTestHelper h) {
            configuration(h);
            distributions(h);
            familyWeights(h);
            compatibility(h);
            workshops(h);
            System.out.println("MODIFIER_TIERS_CHECK: PASS: all rarity tier distributions, uniform within-band values, independent grades, one/nine-tier families, zero-weight gates, large weights, legacy/unknown tier saves, unveiling/scaling/corruption/restoration and assembly provenance");
            h.succeed();
        }
    }
    private static void configuration(GameTestHelper h) {
        var counts = new HashSet<Integer>();
        for (var trait : PickaxeTrait.values()) {
            var spec = ArtifactModifierCatalog.spec(trait);
            check(h,spec!=null,"Every live trait needs an explicit value-tier specification");
            counts.add(spec.tiers().size());
            var definition = new ArtifactModifiers.Definition(trait.id,1,spec.minimumRarity(),0,spec.tiers(),"");
            for (int rarity=spec.minimumRarity(); rarity<6; rarity++)
                check(h,definition.eligible(rarity,100),"Every enabled family needs at least one eligible tier");
            for (var tier : spec.tiers()) {
                check(h,tier.minimum()>=0&&tier.maximum()<=trait.cap,"Catalog value tiers must stay within runtime trait caps");
                check(h,tier.weight(4)>0,"Legendary artifacts must retain access to every band of an eligible family");
            }
        }
        check(h,counts.containsAll(Set.of(3,4,5,6)),"The live catalog must demonstrate independent tier counts");
        rejects(h,()->new ModifierTier("bad",0,0,1,List.of(1,1,1,1,1,1)));
        rejects(h,()->new ModifierTier("bad",1,1,0,List.of(1,1,1,1,1,1)));
        rejects(h,()->new ModifierTier("bad",1,Double.NaN,1,List.of(1,1,1,1,1,1)));
        rejects(h,()->new ModifierTier("bad",1,0,1,List.of(1,-1,1,1,1,1)));
        rejects(h,()->new ModifierTier("bad",1,0,1,List.of(1,1)));
        var tier = new ModifierTier("same",1,0,1,List.of(1,1,1,1,1,1));
        rejects(h,()->new ArtifactModifiers.Definition("examplemod:duplicate",1,0,0,List.of(tier,tier),""));
        var disabled = new ArtifactModifiers.Definition("examplemod:disabled",1,0,0,
            List.of(new ModifierTier("disabled",1,0,1,List.of(0,0,0,0,0,0))),"");
        check(h,ArtifactModifiers.roll(profile(1,5),100,new ArtifactModifiers.Pool(1,1,List.of(disabled))).isEmpty(),
            "A family without eligible tiers must be excluded rather than producing an invalid roll");
    }
    private static void distributions(GameTestHelper h) {
        int samples=12000;
        double previousRank=Double.MAX_VALUE;
        for (int rarity=0; rarity<6; rarity++) {
            var definition=speed(rarity);
            var counts=new HashMap<String,Integer>();
            double meanRank=0, withinBand=0;
            for (int i=0; i<samples; i++) {
                var profile=profile(i*0x9E3779B97F4A7C15L,rarity);
                var roll=ArtifactModifiers.sample(definition,profile,1,new Random(profile.seed()^0x510E527FADE682D1L));
                check(h,definition.contains(roll,rarity),"A sampled value must belong to its saved, eligible tier");
                check(h,roll.scaling()==ModifierScaling.roll(profile,SPEED.id),"Value tiers must not replace the independent F-S growth roll");
                counts.merge(roll.tierId(),1,Integer::sum);
                meanRank+=roll.tierRank();
                var tier=definition.tiers().stream().filter(t->t.id().equals(roll.tierId())).findFirst().orElseThrow();
                withinBand+=(roll.value()-tier.minimum())/(tier.maximum()-tier.minimum());
            }
            long total=0;
            for (var tier:definition.tiers()) total+=tier.weight(rarity);
            for (var tier:definition.tiers()) {
                double expected=tier.weight(rarity)/(double)total;
                double observed=counts.getOrDefault(tier.id(),0)/(double)samples;
                check(h,Math.abs(observed-expected)<.02,"Observed tier frequencies must follow the configured rarity weights");
                check(h,tier.weight(rarity)>0?counts.getOrDefault(tier.id(),0)>0:!counts.containsKey(tier.id()),
                    "Enabled tiers must be reachable; zero-weight tiers must never roll");
            }
            check(h,Math.abs(withinBand/samples-.5)<.02,"Base values should remain uniform inside their selected band");
            meanRank/=samples;
            check(h,meanRank<previousRank,"Higher artifact rarities should improve average value tier without guaranteeing one");
            previousRank=meanRank;
            if (rarity==0) check(h,counts.keySet().equals(Set.of("t6","t5","t4"))&&counts.get("t6")>samples*.90,
                "Common speed must heavily favor +4-12% while occasionally reaching the +12-40% bands");
        }
    }
    private static void familyWeights(GameTestHelper h) {
        var single=new ArtifactModifiers.Definition("examplemod:single",1,0,0,.1,.1,"");
        var tiers=new ArrayList<ModifierTier>();
        for (int rank=1; rank<=9; rank++) tiers.add(new ModifierTier("band_"+rank,rank,rank,rank,List.of(1,1,1,1,1,1)));
        var many=new ArtifactModifiers.Definition("examplemod:many",1,0,0,tiers,"");
        var pool=new ArtifactModifiers.Pool(1,1,List.of(single,many));
        int count=0;
        var seen=new HashSet<Integer>();
        for (int i=0; i<16000; i++) {
            var roll=ArtifactModifiers.roll(profile(i*0x9E3779B97F4A7C15L,0),100,pool).getFirst();
            if (roll.id().equals(many.id())) { count++; seen.add(roll.tierRank()); }
        }
        check(h,count>7500&&count<8500&&seen.size()==9,
            "Adding nine tiers must not make a family more common than an equally weighted one-tier family");
        var large=new ArtifactModifiers.Definition("examplemod:large",Integer.MAX_VALUE,0,0,.1,.1,"");
        var largeOther=new ArtifactModifiers.Definition("examplemod:large_other",Integer.MAX_VALUE,0,0,.1,.1,"");
        var largePool=new ArtifactModifiers.Pool(1,1,List.of(large,largeOther));
        var seenIds=new HashSet<String>();
        for (int i=0; i<50; i++) seenIds.add(ArtifactModifiers.roll(profile(i*991L,0),100,largePool).getFirst().id());
        check(h,seenIds.size()==2,"Relative weights must not overflow a 32-bit total");
        var bigTiers=List.of(
            new ModifierTier("low",2,1,1,List.of(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE)),
            new ModifierTier("high",1,2,2,List.of(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE)));
        var big=new ArtifactModifiers.Definition("examplemod:big_tiers",1,0,0,bigTiers,"");
        var seenTiers=new HashSet<String>();
        var random=new Random(9);
        for (int i=0; i<50; i++) seenTiers.add(ArtifactModifiers.sample(big,profile(i,0),1,random).tierId());
        check(h,seenTiers.size()==2,"Tier weight totals must also use wide arithmetic");
    }
    private static void compatibility(GameTestHelper h) {
        var old=ComponentWorkTests.component(ArtifactProfile.Find.TOOL_HANDLE,80,3,912);
        var root=old.get(DataComponents.CUSTOM_DATA).copyTag();
        var record=root.getCompoundOrEmpty(ArtifactModifiers.KEY);
        record.putInt("CatalogRevision",4);
        root.put(ArtifactModifiers.KEY,record);
        old.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
        var frozen=record.copy();
        var rolls=ArtifactModifiers.read(old);
        check(h,rolls.stream().allMatch(r->r.tierRank()==0&&r.tierId().isEmpty()),"Old rolls must remain unclassified instead of being assigned a new tier");
        PickaxeTooltips.refresh(old);
        check(h,old.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactModifiers.KEY).equals(frozen),
            "Tooltip migration must never reroll or rewrite legacy modifier records");
        check(h,ItemStack.matches(old,roundtrip(h,old)),"Legacy rolls must survive the native item codec");
        var retired=new ArtifactModifiers.Roll(SPEED.id,.123,.8,ModifierScaling.A,"future:retired_band",12);
        var tag=new CompoundTag();tag.putInt("Version",1);
        check(h,ArtifactModifiers.read(ArtifactModifiers.writeRolls(tag,List.of(retired))).equals(List.of(retired)),
            "Unknown tier IDs and ranks above six must survive without current catalog lookups");
        check(h,retired.withValue(.2).tierRank()==12&&retired.withScaling(ModifierScaling.S).tierId().equals(retired.tierId()),
            "Value and grade mutations must retain original tier provenance");

        // An existing save may already contain three untiered offers. They stay selectable as saved.
        var ancient=ComponentWorkTests.component(ArtifactProfile.Find.PICKAXE_LEFT,80,3,111);
        var data=ancient.get(DataComponents.CUSTOM_DATA).copyTag();
        var hidden=new ArtifactModifiers.Roll(AncientModifiers.ID,0,.8,ModifierScaling.B);
        var choices=List.of(new ArtifactModifiers.Roll(SPEED.id,.6,1,ModifierScaling.A),
            new ArtifactModifiers.Roll(DURABILITY.id,.3,1,ModifierScaling.B),new ArtifactModifiers.Roll(EXPERIENCE.id,.4,1,ModifierScaling.C));
        var sealed=new CompoundTag();sealed.putInt("Version",1);sealed.putInt("CatalogRevision",4);
        ArtifactModifiers.writeRolls(sealed,List.of(hidden));
        var offers=new CompoundTag();offers.putInt("Version",1);
        sealed.put(AncientModifiers.OFFERS,ArtifactModifiers.writeRolls(offers,choices));
        data.put(ArtifactModifiers.KEY,sealed);ancient.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
        check(h,ComponentWork.offers(roundtrip(h,ancient)).equals(choices),"Existing saved offers must never be regenerated");
        check(h,ComponentWork.apply(ComponentStationKind.UNVEILER,ancient,0,RandomSource.create(3))
            &&ArtifactModifiers.read(ancient).equals(List.of(choices.getFirst().withQuality(.8))),
            "Legacy unveiling must commit its original value and grade without inventing a tier");
    }
    private static void workshops(GameTestHelper h) {
        var ancient=ComponentWorkTests.ancient(ArtifactProfile.Find.PICKAXE_LEFT,3);
        var offer=ComponentWork.offers(ancient).getFirst();
        check(h,offer.tierRank()>0&&ComponentWork.offers(ancient).equals(ComponentWork.offers(roundtrip(h,ancient))),
            "New fixed offers must save their individual value tiers");
        check(h,ComponentWork.apply(ComponentStationKind.UNVEILER,ancient,0,RandomSource.create(4))
            &&ArtifactModifiers.read(ancient).stream().anyMatch(r->r.id().equals(offer.id())&&r.tierId().equals(offer.tierId())&&r.tierRank()==offer.tierRank()),
            "Unveiling must preserve the selected offer's tier");
        var template=rolled(ArtifactProfile.Find.TOOL_HANDLE,4,80,761);
        var original=ArtifactModifiers.read(template);
        check(h,ComponentWork.apply(ComponentStationKind.SCALER,template,0,RandomSource.create(9)),
            "Scaling grades must still reroll on tiered parts");
        for (var roll:ArtifactModifiers.read(template)) {
            var before=original.stream().filter(r->r.id().equals(roll.id())).findFirst().orElseThrow();
            check(h,roll.value()==before.value()&&roll.tierId().equals(before.tierId())&&roll.tierRank()==before.tierRank(),
                "Scaling randomization must not reroll a modifier's value tier or base value");
        }
        var beforeRestore=ArtifactModifiers.read(template);
        ComponentWork.apply(ComponentStationKind.RESTORER,template,0,RandomSource.create(10));
        check(h,ArtifactModifiers.read(template).equals(beforeRestore),"Restoration must only change quality, never tier provenance");
        boolean added=false;
        for (int seed=0; seed<180; seed++) {
            var part=template.copy();
            var before=ArtifactModifiers.read(part);
            ComponentWork.apply(ComponentStationKind.CORRUPTOR,part,0,RandomSource.create(seed*719L));
            for (var roll:ArtifactModifiers.read(part)) {
                var prior=before.stream().filter(r->r.id().equals(roll.id())).findFirst();
                if (prior.isPresent()) check(h,roll.tierId().equals(prior.get().tierId())&&roll.tierRank()==prior.get().tierRank(),
                    "Corruption must preserve an existing roll's origin tier even if its strength changes");
                else {
                    added=true;
                    check(h,ArtifactModifierCatalog.pool(ArtifactProfile.Find.TOOL_HANDLE,4).entries().stream().anyMatch(d->d.contains(roll,4)),
                        "Modifiers added by corruption must use the same eligible weighted value tiers");
                }
            }
        }
        check(h,added,"The corruption integration check must actually exercise an added modifier");
        var parts=new ArrayList<ItemStack>();
        for (var find:CreativePickaxeParts.FINDS) parts.add(rolled(find,4,80,find.ordinal()+78));
        var tool=ArtifactQuality.assemble(parts);
        var saved=tool.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY).getCompoundOrEmpty("Parts");
        for (int i=0; i<parts.size(); i++)
            check(h,ArtifactModifiers.read(saved.getCompoundOrEmpty(ReconstructionPattern.PICKAXE.parts.get(i)).getCompoundOrEmpty("Modifiers")).equals(ArtifactModifiers.read(parts.get(i))),
                "Assembly must copy each part's exact value-tier metadata");
        check(h,ItemStack.matches(tool,roundtrip(h,tool)),"Tiered assembled tools must survive native serialization");
        String tooltip=PickaxeTooltips.piece(parts.getFirst()).stream().map(Component::getString).reduce("",String::concat);
        check(h,tooltip.contains("T"+ArtifactModifiers.read(parts.getFirst()).getFirst().tierRank()),"Piece tooltips must show the saved rolled tier");
    }
    private static ItemStack rolled(ArtifactProfile.Find find,int rarity,int score,long seed) {
        var part=ComponentWorkTests.component(find,score,rarity,seed);
        var profile=new ArtifactProfile(seed,rarity,ArtifactProfile.Matrix.SHALE,find);
        var record=new CompoundTag();record.putInt("Version",1);record.putInt("CatalogRevision",ArtifactModifierCatalog.REVISION);
        var root=part.get(DataComponents.CUSTOM_DATA).copyTag();
        root.put(ArtifactModifiers.KEY,ArtifactModifiers.writeRolls(record,ArtifactModifiers.roll(profile,score,ArtifactModifierCatalog.pool(find,rarity))));
        part.set(DataComponents.CUSTOM_DATA,CustomData.of(root));return part;
    }
    private static ArtifactProfile profile(long seed,int rarity) { return new ArtifactProfile(seed,rarity,ArtifactProfile.Matrix.SHALE,ArtifactProfile.Find.PICKAXE_LEFT); }
    private static ArtifactModifiers.Definition speed(int rarity) { return ArtifactModifierCatalog.pool(ArtifactProfile.Find.PICKAXE_LEFT,rarity).entries().stream().filter(d->d.id().equals(SPEED.id)).findFirst().orElseThrow(); }
    private static ItemStack roundtrip(GameTestHelper h,ItemStack stack) {
        var ops=h.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
        return ItemStack.CODEC.parse(ops,ItemStack.CODEC.encodeStart(ops,stack).getOrThrow()).getOrThrow();
    }
    private static void rejects(GameTestHelper h,Runnable action) {
        try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw h.assertionException("Invalid tier configuration should fail early");
    }
    private static void check(GameTestHelper h,boolean value,String message) { if(!value)throw h.assertionException(message); }
    private ModifierTierTests() {}
}
