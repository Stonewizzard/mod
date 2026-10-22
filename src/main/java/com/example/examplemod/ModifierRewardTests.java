package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import static com.example.examplemod.PickaxeTrait.*;

/** Native reward benchmarks and guidebook and migration regressions. */
public final class ModifierRewardTests {
    public static void register(RegisterGameTestsEvent event){
        var env=event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"modifier_rewards_environment"));
        for(int mode=0;mode<3;mode++)event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"modifier_rewards_"+mode),new Test(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),300,0,true,Rotation.NONE,false,1,1,false,0),mode));
    }
    private static final class Test extends GameTestInstance {
        final int mode;Test(TestData<Holder<TestEnvironmentDefinition<?>>> data,int mode){super(data);this.mode=mode;}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Rewarding builds and searchable field guide");}
        @Override public void run(GameTestHelper h){if(mode==0)balance(h);else if(mode==1)guidebook(h);else runtime(h);System.out.println("MODIFIER_REWARDS_CHECK_"+mode+": PASS");h.succeed();}
    }
    private static ItemStack tool(int quality,Map<PickaxeTrait,Double> traits){return ModifierExpansionTests.tool(quality,traits,ModifierScaling.S);}
    private static void balance(GameTestHelper h){
        var player=h.makeMockServerPlayerInLevel();player.setGameMode(GameType.SURVIVAL);player.setOnGround(true);var pos=h.absolutePos(new BlockPos(3,1,3));
        var nativePick=new ItemStack(Items.NETHERITE_PICKAXE);var lookup=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        var efficiency=lookup.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY);nativePick.enchant(efficiency,5);
        // Native efficiency contributes level^2+1 to a correct-tool mining speed above one.
        double vanilla=nativePick.getDestroySpeed(Blocks.STONE.defaultBlockState())+Math.pow(net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(efficiency,nativePick),2)+1;
        check(h,vanilla==35,"Benchmark must use the real native Netherite item and Efficiency V enchantment");
        for(int quality:new int[]{0,50,100,120}){
            var item=tool(quality,Map.of(SPEED,.5));var saved=item.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY).copy();player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,item);
            var event=new PlayerEvent.BreakSpeed(player,Blocks.STONE.defaultBlockState(),item.getDestroySpeed(Blocks.STONE.defaultBlockState()),pos);PickaxeEvents.speed(event);
            double expected=8*(1+2*quality/100.0)*(1+2*quality/100.0);
            check(h,Math.abs(event.getNewSpeed()-expected)<.001,"Quality and base modifier power must remain independent and predictable");
            if(quality==100)check(h,event.getNewSpeed()>vanilla*2,"An attainable unenchanted 100%-quality speed build must beat Efficiency V Netherite by over twofold");
            FieldGuide.entries();PickaxeTooltips.refresh(item);check(h,item.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY).equals(saved),"Reward refresh and reading definitions must not rewrite any recovered raw roll or source record");
            check(h,PickaxeTooltips.tool(item).size()<=16,"Item tooltip pages must retain actual values within their row budget");
        }
        var bare=tool(100,Map.of());bare.enchant(efficiency,5);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,bare);var boosted=new PlayerEvent.BreakSpeed(player,Blocks.STONE.defaultBlockState(),bare.getDestroySpeed(Blocks.STONE.defaultBlockState())+26,pos);PickaxeEvents.speed(boosted);
        check(h,boosted.getNewSpeed()==102,"The quality core must multiply, rather than replace, native Efficiency speed");
        for(var trait:PickaxeTrait.values())check(h,!ModifierDescriptions.description(trait).isBlank()&&trait.power>=1&&trait.cap>0,"All 120 modifiers require a reviewed effect and inspection description");
        var fortune=tool(100,Map.of(FORTUNE,2.0));fortune.enchant(lookup.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE),3);int advanced=0,normal=0;
        var p=h.absolutePos(new BlockPos(3,1,5));var reference=new ItemStack(Items.NETHERITE_PICKAXE);reference.enchant(lookup.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE),3);
        for(int i=0;i<160;i++){
            normal+=Block.getDrops(Blocks.DIAMOND_ORE.defaultBlockState(),h.getLevel(),p,null,player,reference).stream().mapToInt(ItemStack::getCount).sum();
            var drops=new ArrayList<net.minecraft.world.entity.item.ItemEntity>();drops.add(new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),p.getX(),p.getY(),p.getZ(),new ItemStack(Items.DIAMOND)));
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,fortune);var event=new BlockDropsEvent(h.getLevel(),p,Blocks.DIAMOND_ORE.defaultBlockState(),null,drops,player,fortune);PickaxeEvents.drops(event);advanced+=drops.stream().mapToInt(e->e.getItem().getCount()).sum();
        }
        check(h,advanced>normal*1.5,"Fortune modifiers must deliver substantially more native loot than Fortune III on the same ore");
        System.out.println("REWARD_BENCHMARK: unenchanted speed build=72; Efficiency V restored core=102; Efficiency V Netherite=35; Fortune modified="+advanced+", vanilla III="+normal);
    }
    private static void guidebook(GameTestHelper h){
        var entries=FieldGuide.entries();check(h,entries.size()==147&&entries.stream().filter(e->e.category()==FieldGuide.Category.MODIFIERS).count()==120,"The field guide must include all 27 chapters and all 120 modifier definitions");
        for(var category:FieldGuide.Category.values())check(h,!FieldGuide.search("",category,FieldGuide.Group.ALL).isEmpty(),"Every guide category must contain articles");
        for(var entry:entries){
            for(var link:entry.links())check(h,FieldGuide.entry(link)!=null,"Every related-article link must resolve");
            var icon=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.parse(entry.icon()));check(h,icon!=null&&icon!=Items.AIR,"Every guide icon must refer to a registered item");
        }
        for(var trait:PickaxeTrait.values())check(h,FieldGuide.entry(trait.id).paragraphs().contains(ModifierDescriptions.description(trait)),"Every modifier must use its actual generated gameplay description");
        check(h,FieldGuide.search("MaGnEt",FieldGuide.Category.MODIFIERS,FieldGuide.Group.ALL).stream().anyMatch(e->e.id().equals(MAGNET.id)),"Search must match case-insensitive partial modifier names");
        check(h,FieldGuide.search("wither rose",FieldGuide.Category.CREATURES,FieldGuide.Group.ALL).stream().anyMatch(e->e.id().equals("crunchers")),"Search must match multiple words in article text");
        check(h,FieldGuide.search("zzzznotpresentzzzz",null,FieldGuide.Group.ALL).isEmpty(),"No-match searches must return an empty result set");
        for(var entry:FieldGuide.search("",FieldGuide.Category.MODIFIERS,FieldGuide.Group.NETHER))check(h,ArtifactModifierCatalog.spec(PickaxeTrait.byId(entry.id())).influence()==ModifierInfluence.NETHER,"Topic filters must not leak foreign modifier definitions");
        var nav=new FieldGuideNavigation();nav.query("wither rose");nav.category(FieldGuide.Category.CREATURES);String selected=nav.selected().id();nav.open("cruncher_egg");check(h,nav.selected().id().equals("cruncher_egg")&&nav.query().isEmpty(),"Related navigation must open articles outside a prior search");nav.back();check(h,nav.query().equals("wither rose")&&nav.category()==FieldGuide.Category.CREATURES&&nav.selected().id().equals(selected),"Back must restore the query, category and prior article");
        nav.query("");nav.category(FieldGuide.Category.MODIFIERS);nav.listPage(10000);check(h,nav.listPage()<nav.listPages(),"Result pagination must stay bounded");nav.query("zzzznotpresentzzzz");check(h,nav.selected()==null&&nav.listPage()==0,"A no-results search must clear a stale article selection");
        var p=h.makeMockServerPlayerInLevel();var book=new ItemStack(FieldGuideContent.BOOK.get());book.set(DataComponents.CUSTOM_NAME,Component.literal("My notes"));var copy=book.copy();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,book);
        check(h,book.getItem().use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.SUCCESS&&ItemStack.matches(book,copy),"Reading must not consume or mutate the book on the dedicated server");
        check(h,FieldGuideContent.isGuide(new ItemStack(LegacyAppraisalContent.ITEM.get()))&&!(LegacyAppraisalContent.ITEM.get() instanceof BlockItem),"An old table item must open the guide and must no longer place a table");
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(3,1,3));level.setBlockAndUpdate(pos,LegacyAppraisalContent.BLOCK.get().defaultBlockState());var old=(LegacyAppraisalBlockEntity)level.getBlockEntity(pos);var stored=tool(100,Map.of(SPEED,.3,VEIN_MINER,1.0));old.setItem(0,stored.copy());
        var saved=old.saveWithFullMetadata(level.registryAccess());var decoded=net.minecraft.world.level.block.entity.BlockEntity.loadStatic(pos,old.getBlockState(),saved,level.registryAccess());check(h,decoded instanceof LegacyAppraisalBlockEntity legacy&&ItemStack.matches(stored,legacy.getItem(0)),"Old table saves must retain their exact stored item");
        LegacyAppraisalBlockEntity.tick(level,pos,old.getBlockState(),old);check(h,level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.BarrelBlockEntity,"Existing tables must retire into ordinary barrels");var barrel=(net.minecraft.world.level.block.entity.BarrelBlockEntity)level.getBlockEntity(pos);
        check(h,ItemStack.matches(stored,barrel.getItem(0))&&barrel.getItem(1).is(FieldGuideContent.BOOK.get()),"Retirement must preserve the exact stored tool and supply one guide");LegacyAppraisalBlockEntity.tick(level,pos,old.getBlockState(),old);int count=0;for(int i=0;i<barrel.getContainerSize();i++)count+=barrel.getItem(i).getCount();check(h,count==2,"Repeated retired-table ticks must not duplicate items");
        var part=ComponentWorkTests.component(ArtifactProfile.Find.PICKAXE_LEFT,100,5,33);check(h,ComponentWork.apply(ComponentStationKind.INFLUENCER,part,0,p.getRandom()),"Test influence must assign normally");
        check(h,!part.has(DataComponents.CUSTOM_MODEL_DATA)&&part.getHoverName().getStyle().getColor().getValue()==ModifierInfluence.NETHER.color,"Influence must color names without changing icon data");
        check(h,PickaxeTooltips.piece(part).stream().noneMatch(c->c.getString().contains("Uninfluenced")||c.getString().contains("Appraisal")),"Item tooltips must not retain removed-table instructions or influence clutter");
    }
    private static void runtime(GameTestHelper h){
        var p=h.makeMockServerPlayerInLevel();var pos=h.absolutePos(new BlockPos(3,1,3));p.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);var item=tool(100,Map.of(SOUL_FURNACE,1.0));p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,item);
        var drops=new ArrayList<net.minecraft.world.entity.item.ItemEntity>();drops.add(new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),pos.getX(),pos.getY(),pos.getZ(),new ItemStack(Items.RAW_IRON,4)));int xp=p.totalExperience;
        var event=new BlockDropsEvent(h.getLevel(),pos,Blocks.IRON_ORE.defaultBlockState(),null,drops,p,item);PickaxeEvents.drops(event);
        check(h,drops.stream().allMatch(e->e.getItem().is(Items.IRON_INGOT))&&p.totalExperience==xp&&MiningRuntime.charge(item)==0,"Soul Furnace must work alone using the innate core without consuming player XP");
        var low=tool(100,Map.of(RESTORATIVE_DISCHARGE,1.0));low.setDamageValue(100);MiningRuntime.addCharge(low,3);var c=new MiningContext(new BlockDropsEvent(h.getLevel(),pos,Blocks.STONE.defaultBlockState(),null,new ArrayList<>(),p,low),p);MiningChargeEffects.prepare(c);MiningChargeEffects.breaks(c);
        check(h,low.getDamageValue()==60,"An isolated charge spender must work from core generation and give its full repair reward");
        var anchor=tool(100,Map.of(VOID_ANCHOR,1.0));var target=p.position();levelFloor(h,target);MiningRuntime.edit(anchor,t->{t.putString("SafeDimension",h.getLevel().dimension().identifier().toString());t.putDouble("SafeX",target.x);t.putDouble("SafeY",target.y);t.putDouble("SafeZ",target.z);});p.setOnGround(false);p.snapTo(target.x,h.getLevel().getMinY()-3,target.z);MiningStatusEffects.tick(p,anchor);
        check(h,p.position().distanceToSqr(target)<.01&&anchor.getDamageValue()>0,"Void Anchor must have a useful emergency durability fallback without another modifier");
    }
    private static void levelFloor(GameTestHelper h,net.minecraft.world.phys.Vec3 p){var pos=BlockPos.containing(p);h.getLevel().setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());h.getLevel().setBlockAndUpdate(pos.above(),Blocks.AIR.defaultBlockState());}
    private static void check(GameTestHelper h,boolean value,String message){if(!value)throw h.assertionException(message);}
    private ModifierRewardTests(){}
}
