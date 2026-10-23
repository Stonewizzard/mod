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
        var nav=new FieldGuideNavigation();nav.category(FieldGuide.Category.CREATURES);nav.query("wither rose");nav.selectRow(0);String selected=nav.selected().id();nav.open("cruncher_egg");check(h,nav.selected().id().equals("cruncher_egg"),"Related navigation must open articles");nav.back();check(h,nav.query().equals("wither rose")&&nav.page()==FieldGuideNavigation.Page.ARTICLE&&nav.selected().id().equals(selected),"Back must restore the query and prior article");
        nav.home();nav.category(FieldGuide.Category.MODIFIERS);check(h,nav.page()==FieldGuideNavigation.Page.TOPICS,"Modifiers must first show a clear topic index");nav.group(FieldGuide.Group.ALL);nav.listPage(10000);check(h,nav.listPage()<nav.listPages(),"Result pagination must stay bounded");nav.query("zzzznotpresentzzzz");check(h,nav.selected()==null&&nav.listPage()==0,"A no-results search must clear a stale article selection");
        var recipeInput=net.minecraft.world.item.crafting.CraftingInput.of(2,2,List.of(new ItemStack(Items.BOOK),ItemStack.EMPTY,ItemStack.EMPTY,new ItemStack(Items.PAPER)));
        var recipe=h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,recipeInput,h.getLevel());
        check(h,recipe.isPresent()&&recipe.get().value().assemble(recipeInput).is(FieldGuideContent.BOOK.get()),"A book and paper must craft the field guide in the normal inventory grid");
        var oldRecipe=net.minecraft.world.item.crafting.CraftingInput.of(3,3,List.of(ItemStack.EMPTY,new ItemStack(Items.PAPER),new ItemStack(Items.GLASS),new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.STICK),ItemStack.EMPTY,new ItemStack(Items.STICK)));
        check(h,h.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,oldRecipe,h.getLevel()).isEmpty(),"The retired Appraisal Table recipe must be absent");
        var p=h.makeMockServerPlayerInLevel();var book=new ItemStack(FieldGuideContent.BOOK.get());book.set(DataComponents.CUSTOM_NAME,Component.literal("My notes"));var copy=book.copy();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,book);
        check(h,book.getItem().use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.SUCCESS&&ItemStack.matches(book,copy),"Reading must not consume or mutate the book on the dedicated server");
        var retired=Identifier.fromNamespaceAndPath(ExampleMod.MODID,"appraisal_table");
        check(h,!net.minecraft.core.registries.BuiltInRegistries.BLOCK.containsKey(retired)&&!net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(retired),"The removed workstation must have no hidden block or item registration");
        var part=ComponentWorkTests.component(ArtifactProfile.Find.PICKAXE_LEFT,100,5,33);check(h,ComponentWork.apply(ComponentStationKind.INFLUENCER,part,0,p.getRandom()),"Test influence must assign normally");
        check(h,!part.has(DataComponents.CUSTOM_MODEL_DATA)&&part.getHoverName().getStyle().getColor().getValue()==ModifierInfluence.NETHER.color,"Influence must color names without changing icon data");
        check(h,PickaxeTooltips.piece(part).stream().noneMatch(c->c.getString().contains("Uninfluenced")||c.getString().contains("Appraisal")),"Item tooltips must not retain removed-table instructions or influence clutter");
        for(var roll:ArtifactModifiers.read(part)){var trait=PickaxeTrait.byId(roll.id());if(trait!=null)check(h,PickaxeTooltips.piece(part).stream().anyMatch(c->c.getString().startsWith(PickaxeTooltips.label(trait)+"  ")),"Recovered components must show their own rolled modifier values again");}
        var other=new ItemStack(ReconstructionContent.SHOVEL_HEAD.get());var recovered=new net.minecraft.nbt.CompoundTag();recovered.putInt("Quality",88);var oldRoot=new net.minecraft.nbt.CompoundTag();oldRoot.put(ArtifactQuality.RECOVERED,recovered);oldRoot.putInt(PickaxeTooltips.REVISION_KEY,7);other.set(DataComponents.CUSTOM_DATA,CustomData.of(oldRoot));other.set(DataComponents.LORE,new net.minecraft.world.item.component.ItemLore(List.of(Component.literal("Inspect at an Appraisal Table"))));
        check(h,PickaxeTooltips.refresh(other)&&ArtifactQuality.score(other)==88&&other.get(DataComponents.LORE).lines().stream().noneMatch(c->c.getString().contains("Appraisal")),"Old presentation must migrate for every component role, including non-pickaxe heads");

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
