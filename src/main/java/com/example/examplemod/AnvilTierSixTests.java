package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;

/** Actual crystals, exact 16:16 links, all glass colors, harmless craft pulses and protected construction. */
public final class AnvilTierSixTests {
    private static Identifier id(String n){return Identifier.fromNamespaceAndPath(ExampleMod.MODID,n);}
    public static void register(net.neoforged.neoforge.event.RegisterGameTestsEvent e){var env=e.registerEnvironment(id("anvil_tier6_environment"));e.registerTest(id("anvil_tier6"),new Test(new TestData<>(env,Level.OVERWORLD,id("empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));}
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> d){super(d);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Tier 6 network, normal End Crystals, colored lens, harmless forging and exact inventory construction");}
        @Override public void run(GameTestHelper h){
            var l=h.getLevel();var core=h.absolutePos(new BlockPos(20,8,20));var stages=AnvilStages.all();
            check(h,stages.size()==4&&stages.getLast().tier()==6&&stages.getLast().blocks().size()==45,"Tier 6 loads 16 energized caps, four obsidian caps and 25 lens blocks");
            var links=AnvilTierSix.links(stages);check(h,links.size()==16&&links.stream().map(AnvilTierSix.Link::from).distinct().count()==16&&links.stream().map(AnvilTierSix.Link::to).distinct().count()==16,"All sixteen energized blocks link to exactly one unique keystone");
            var targets=stages.get(2).blocks().stream().filter(r->r.tag().equals(AnvilStages.KEYSTONES)).map(AnvilStages.Requirement::offset).toList();check(h,links.stream().allMatch(v->targets.contains(v.to())),"Beam ends are actual canopy keystones");
            for(var cap:AnvilTierSix.requirements(stages,AnvilTierSix.OBSIDIAN)){var near=AnvilTierSix.adjacentTowers(stages,cap.offset());check(h,near.size()==2&&new HashSet<>(near).size()==2,"Every outer crystal has two separate adjacent tower targets");for(var v:near)check(h,(cap.offset().getX()==0?Math.signum(v.z-.5)==Math.signum(cap.offset().getZ()):Math.signum(v.x-.5)==Math.signum(cap.offset().getX())),"V beams reach adjacent towers rather than opposite towers");}
            check(h,AnvilGuidePayload.snapshot().decode().equals(stages)&&AnvilGuidePayload.snapshot().json().length()<65536,"Tier 6 geometry syncs to remote clients within the payload bound");
            var player=h.makeMockServerPlayerInLevel();player.setGameMode(GameType.SURVIVAL);
            for(var front:Direction.Plane.HORIZONTAL){
                prepare(l,core,front,AnvilTierSix.stainedGlass("light_blue"),false);refresh(l,core);tier(h,l,core,5,"Caps and lens alone preserve Tier 5 until real crystals are placed");
                player.snapTo(core.getX()+.5,core.getY()+2.5,core.getZ()+.5,0,90);player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.END_CRYSTAL,4));
                for(var req:AnvilTierSix.requirements(stages,AnvilTierSix.OBSIDIAN)){
                    var at=req.at(core,front);var stack=player.getMainHandItem();stack.useOn(new UseOnContext(l,player,InteractionHand.MAIN_HAND,stack,new BlockHitResult(Vec3.atCenterOf(at).add(0,.5,0),Direction.UP,at,false)));
                    var crystal=AnvilTierSix.crystal(l,at);check(h,crystal!=null&&crystal.getType()==net.minecraft.world.entity.EntityTypes.END_CRYSTAL&&crystal.getBeamTarget()==null,"Native End Crystal item places a normal unbound crystal on obsidian");
                }
                check(h,player.getMainHandItem().isEmpty(),"Manual crystal placement consumes exactly four items in Survival");refresh(l,core);tier(h,l,core,6,"Real End Crystals complete Tier 6 in all four facings");
                var first=AnvilTierSix.requirements(stages,AnvilTierSix.OBSIDIAN).getFirst().at(core,front);AnvilTierSix.crystal(l,first).discard();refresh(l,core);tier(h,l,core,5,"Losing a crystal downgrades without altering the remaining workshop");
                clearCrystals(l,core);l.destroyBlock(core,false);
            }
            var front=Direction.NORTH;prepare(l,core,front,Blocks.GLASS,true);
            var glasses=new ArrayList<Block>(List.of(Blocks.GLASS,Blocks.TINTED_GLASS));for(var dye:DyeColor.values())glasses.add(BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(dye.name().toLowerCase(Locale.ROOT)+"_stained_glass")));
            for(var glass:glasses){lens(l,core,front,glass);refresh(l,core);tier(h,l,core,6,"Each uniform clear/tinted/stained glass lens works");check(h,AnvilTierSix.inspect(l,core,front,stages).color()==AnvilTierSix.glassColor(glass),"Lens selects its actual glass color");}
            lens(l,core,front,AnvilTierSix.stainedGlass("red"));var lensPos=AnvilTierSix.requirements(stages,AnvilTierSix.GLASS).getFirst().at(core,front);l.setBlock(lensPos,AnvilTierSix.stainedGlass("blue").defaultBlockState(),3);refresh(l,core);tier(h,l,core,5,"One mismatched color invalidates Tier 6");lens(l,core,front,AnvilTierSix.stainedGlass("red"));refresh(l,core);
            var be=(AncientAnvilBlockEntity)l.getBlockEntity(core);recipe(be);var hammer=new ItemStack(AncientAnvilContent.IRON_FORGE_HAMMER.get());player.setItemInHand(InteractionHand.MAIN_HAND,hammer);float health=player.getHealth();
            for(int n=0;n<4;n++){be.lastStrike=l.getGameTime()-8;check(h,AnvilWork.strike(be,player,InteractionHand.MAIN_HAND),"Native hammer work produces the real Runestone craft");}
            check(h,be.output().is(AncientAnvilContent.RUNESTONE_ITEM.get())&&be.energyUntil>l.getGameTime()&&be.energyColor==0xb02e26,"A valid Tier 6 craft starts the red lens-colored energy pillar");
            check(h,player.getHealth()==health&&l.getBlockState(lensPos).is(AnvilTierSix.stainedGlass("red"))&&AnvilTierSix.inspect(l,core,front,stages).crystals()==4,"Energy pulse causes no damage and preserves glass and normal crystals");
            be.takeOutput();be.energyUntil=0;be.lastStrike=l.getGameTime()-8;check(h,!AnvilWork.strike(be,player,InteractionHand.MAIN_HAND)&&be.energyUntil==0,"Empty/invalid work cannot generate an energy blast");
            recipe(be);lens(l,core,front,AnvilTierSix.stainedGlass("light_blue"));be.lastStrike=l.getGameTime()-8;check(h,AnvilWork.strike(be,player,InteractionHand.MAIN_HAND)&&be.energyColor==0x3ab3da,"A changed uniform lens changes the next blast color");
            // Upgrade guide survives removed caps; existing solid blocks and containers are never replaced automatically.
            clearCrystals(l,core);l.destroyBlock(core,false);AnvilCanopyTests.prepare(l,core,front);AnvilStageTests.fill(l,core,front,stages.get(2));for(var req:AnvilTierSix.requirements(stages,AnvilTierSix.GLASS))l.setBlock(req.at(core,front),Blocks.AIR.defaultBlockState(),3);refresh(l,core);be=(AncientAnvilBlockEntity)l.getBlockEntity(core);
            check(h,AnvilGuide.next(l,core).tier()==6&&AnvilGuide.next(l,core).missing(l).size()==49,"Tier 5 previews all new blocks and manual crystals");
            player.getInventory().clearContent();player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(AncientAnvilContent.IRON_FORGE_HAMMER.get()));player.snapTo(core.getX()+.5,core.getY()+2.5,core.getZ()+.5,0,90);player.setShiftKeyDown(true);
            player.getInventory().setItem(1,new ItemStack(AncientAnvilContent.ENERGIZED_CRYSTAL_BLOCK_ITEM.get(),16));player.getInventory().setItem(2,new ItemStack(Items.OBSIDIAN,4));player.getInventory().setItem(3,new ItemStack(AnvilTierSix.stainedGlass("light_blue").asItem(),25));player.getInventory().setItem(4,new ItemStack(Items.END_CRYSTAL,4));player.getInventory().setItem(5,new ItemStack(AnvilTierSix.stainedGlass("red").asItem(),5));
            build(h,player,core,25);check(h,player.getInventory().countItem(AnvilTierSix.stainedGlass("red").asItem())==5&&AnvilTierSix.referenceGlass(l,core,front,stages)==AnvilTierSix.stainedGlass("light_blue"),"Automatic lens construction retains a single chosen color");
            for(var req:stages.getLast().blocks())if(!req.tag().equals(AnvilTierSix.GLASS)){check(h,l.getBlockState(req.at(core,front)).is(Blocks.IRON_BLOCK),"Old cap blocks remain intact until removed by the player");l.setBlock(req.at(core,front),Blocks.AIR.defaultBlockState(),3);}
            refresh(l,core);check(h,AnvilGuide.next(l,core).tier()==6,"Removing old caps keeps the Tier 6 upgrade guide active");build(h,player,core,20);
            check(h,player.getInventory().countItem(AncientAnvilContent.ENERGIZED_CRYSTAL_BLOCK_ITEM.get())==0&&player.getInventory().countItem(Items.OBSIDIAN)==0&&player.getInventory().countItem(AnvilTierSix.stainedGlass("light_blue").asItem())==0&&player.getInventory().countItem(Items.END_CRYSTAL)==4,"Builder charges exact 16/4/25 block costs and never uses End Crystal items");
            be.lastFoundationPlacement=l.getGameTime()-6;check(h,!AnvilFoundation.placeNext(player,core)&&AnvilGuide.next(l,core).missing(l).size()==4,"Builder leaves the four real crystals for manual placement");
            l.setBlock(lensPos,Blocks.CHEST.defaultBlockState(),3);var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)l.getBlockEntity(lensPos);chest.setItem(0,new ItemStack(Items.DIAMOND,7));player.getInventory().setItem(3,new ItemStack(AnvilTierSix.stainedGlass("light_blue").asItem()));be.lastFoundationPlacement=l.getGameTime()-6;check(h,!AnvilFoundation.placeNext(player,core)&&chest.getItem(0).getCount()==7&&player.getInventory().getItem(3).getCount()==1,"Repair preserves an obstructing container and all materials");
            System.out.println("ANVIL_TIER6_CHECK: PASS: 16 unique keystone links, 8 adjacent V connections, four normal native crystals/all facings, all18 glass lenses, mixed-color downgrade, harmless real hammer pulses/color, exact block-only build costs and protected repairs");h.succeed();
        }
    }
    public static void prepare(ServerLevel l,BlockPos core,Direction front,Block glass,boolean crystals){
        clearCrystals(l,core);AnvilCanopyTests.prepare(l,core,front);AnvilStageTests.fill(l,core,front,AnvilStages.all().get(2));
        for(var req:AnvilStages.all().getLast().blocks())l.setBlock(req.at(core,front),(req.tag().equals(AnvilTierSix.ENERGIZED)?AncientAnvilContent.ENERGIZED_CRYSTAL_BLOCK.get():req.tag().equals(AnvilTierSix.OBSIDIAN)?Blocks.OBSIDIAN:glass).defaultBlockState(),3);
        if(crystals)for(var req:AnvilTierSix.requirements(AnvilStages.all(),AnvilTierSix.OBSIDIAN)){var at=req.at(core,front);var e=net.minecraft.world.entity.EntityTypes.END_CRYSTAL.create(l,net.minecraft.world.entity.EntitySpawnReason.COMMAND);e.snapTo(at.getX()+.5,at.getY()+1,at.getZ()+.5,0,0);l.addFreshEntity(e);}
        refresh(l,core);
    }
    public static void recipe(AncientAnvilBlockEntity be){for(var slot:List.of(AnvilSlot.NORTH_WEST,AnvilSlot.NORTH_EAST,AnvilSlot.SOUTH_WEST,AnvilSlot.SOUTH_EAST))be.placeInput(slot,new ItemStack(Blocks.DEEPSLATE));be.placeInput(AnvilSlot.CENTER,new ItemStack(Items.GOLD_INGOT));}
    private static void lens(ServerLevel l,BlockPos core,Direction front,Block glass){for(var req:AnvilTierSix.requirements(AnvilStages.all(),AnvilTierSix.GLASS))l.setBlock(req.at(core,front),glass.defaultBlockState(),3);}
    public static void clearCrystals(ServerLevel l,BlockPos core){for(var e:l.getEntitiesOfClass(net.minecraft.world.entity.boss.enderdragon.EndCrystal.class,new AABB(core).inflate(20,8,20)))e.discard();}
    private static void build(GameTestHelper h,ServerPlayer p,BlockPos core,int n){var be=(AncientAnvilBlockEntity)p.level().getBlockEntity(core);for(int i=0;i<n;i++){be.lastFoundationPlacement=p.level().getGameTime()-6;check(h,AnvilFoundation.placeNext(p,core),"Native Tier 6 inventory placement "+i);}}
    private static void refresh(ServerLevel l,BlockPos p){AncientAnvilBlockEntity.refresh(l,p,l.getBlockState(p));}
    private static void tier(GameTestHelper h,Level l,BlockPos p,int t,String s){check(h,l.getBlockState(p).getValue(AncientAnvilBlock.TIER)==t,s);}
    private static void check(GameTestHelper h,boolean ok,String s){if(!ok)throw h.assertionException(s);}
    private AnvilTierSixTests(){}
}
