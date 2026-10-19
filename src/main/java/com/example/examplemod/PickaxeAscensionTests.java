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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import static com.example.examplemod.PickaxeTrait.*;

public final class PickaxeAscensionTests {
    public static void register(RegisterGameTestsEvent event){
        var env=event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"pickaxe_ascension_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"pickaxe_ascension"),new Test(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),200,0,true,Rotation.NONE,false,1,1,false,0)));
    }
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> data){super(data);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Persistent pickaxe levels and atomic material-based ascensions");}
        @Override public void run(GameTestHelper h){
            verifyMining(h);verifyGates(h);verifyCapsAndSplitStacks(h);verifyInventory(h);
            System.out.println("PICKAXE_ASCENSION_CHECK: PASS: actual Survival mining, no Creative XP, all ten gates through level 100, linear bonuses and caps, duplicate-trait costs, atomic consumption, stable damage/enchants/provenance, native inventory routing, save/reload and block drops");h.succeed();
        }
    }
    private static void verifyMining(GameTestHelper h){
        var player=h.makeMockServerPlayerInLevel();player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);var pos=h.absolutePos(new BlockPos(3,1,3));player.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
        var tool=tool(50);player.setItemInHand(InteractionHand.MAIN_HAND,tool);h.getLevel().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());
        check(h,player.gameMode.destroyBlock(pos)&&PickaxeProgression.read(player.getMainHandItem()).xp()==1,"Actual successful Survival mining must award one tool XP");tool=player.getMainHandItem();
        boolean[] cancel={true};
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.level.block.BreakBlockEvent event)->{if(cancel[0]&&event.getPos().equals(pos))event.setCanceled(true);});
        h.getLevel().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());
        check(h,!player.gameMode.destroyBlock(pos)&&PickaxeProgression.read(tool).xp()==1,"Canceled block breaks must award no XP");cancel[0]=false;
        h.getLevel().setBlockAndUpdate(pos,Blocks.DIRT.defaultBlockState());player.gameMode.destroyBlock(pos);check(h,PickaxeProgression.read(tool).xp()==1,"Incorrect mining targets must not grant tool XP");
        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);h.getLevel().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());player.gameMode.destroyBlock(pos);check(h,PickaxeProgression.read(tool).xp()==1,"Creative mining must not level the tool");
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);PickaxeProgression.write(tool,new PickaxeProgression.State(9,PickaxeProgression.xpRequired(9)-1,0));
        h.getLevel().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());player.gameMode.destroyBlock(pos);check(h,PickaxeProgression.read(tool).level()==10&&PickaxeProgression.read(tool).needsAscension(),"Mining must reach and lock at the tenth level");
        h.getLevel().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());player.gameMode.destroyBlock(pos);check(h,PickaxeProgression.read(tool).xp()==0&&PickaxeProgression.read(tool).level()==10,"Mining at an unpaid gate must never bank XP");
        var legacy=new ItemStack(ArchaeologyContent.PICKAXE.get());check(h,PickaxeProgression.read(legacy).level()==1&&PickaxeProgression.gain(legacy,20)&&PickaxeProgression.read(legacy).level()==2,"Existing pickaxes without progression data must start at level one");
        var normal=new ItemStack(Items.DIAMOND_PICKAXE);check(h,!PickaxeProgression.gain(normal,100)&&!normal.has(DataComponents.CUSTOM_DATA),"Ordinary pickaxes must stay untouched");
    }
    private static void verifyGates(GameTestHelper h){
        for(int quality:new int[]{0,50,100}){
            var tool=tool(quality);tool.setDamageValue(100);var original=tool.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY).copy();
            var unbreaking=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING);tool.enchant(unbreaking,2);
            var customName=Component.literal("An old friend");tool.set(DataComponents.CUSTOM_NAME,customName);
            var storage=new SimpleContainer(AscenderBlockEntity.SIZE);storage.setItem(0,tool);
            double base=PickaxeModifiers.totals(tool).get(SPEED);int attributes=tool.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().size();
            for(int stage=1;stage<=10;stage++){
                check(h,PickaxeProgression.gain(tool,Integer.MAX_VALUE)&&PickaxeProgression.read(tool).level()==stage*10,"XP must stop at each successive gate");
                var before=tool.copy();check(h,!PickaxeProgression.gain(tool,1000)&&ItemStack.matches(before,tool),"Blocked XP must leave the tool unchanged");
                var costs=AscensionResources.requirements(tool);check(h,costs.stream().filter(c->c.item()==Items.REDSTONE).findFirst().orElseThrow().count()==4*stage,"Repeated speed modifiers must each contribute increasing costs, including zero-score rolls");
                fill(storage,costs,0);var last=costs.getLast();int materialSlot=costs.size();storage.getItem(materialSlot).shrink(1);
                var snapshot=new ArrayList<ItemStack>();for(int i=0;i<storage.getContainerSize();i++)snapshot.add(storage.getItem(i).copy());
                check(h,!AscensionResources.tryAscend(storage),"One missing material must prevent the entire ascension");for(int i=0;i<snapshot.size();i++)check(h,ItemStack.matches(snapshot.get(i),storage.getItem(i)),"Failed ascension must consume no resources and change no tool data");
                storage.getItem(materialSlot).grow(1);if(storage.getItem(materialSlot).isEmpty())storage.setItem(materialSlot,new ItemStack(last.item(),last.count()));
                check(h,AscensionResources.tryAscend(storage)&&PickaxeProgression.read(tool).ascensions()==stage&&!PickaxeProgression.read(tool).needsAscension(),"Each paid gate must ascend exactly once");
                for(int i=1;i<storage.getContainerSize();i++)check(h,storage.getItem(i).isEmpty(),"Ascension must consume the exact complete set of resources");
                check(h,!AscensionResources.tryAscend(storage),"A second viewer or tick must never pay twice for the same gate");
                check(h,PickaxeModifiers.totals(tool).get(SPEED)>=base*(1+stage*.1)-1e-8,"B-grade ascension bonuses must keep their original baseline while level bonuses add separately");
                check(h,tool.getDamageValue()==100&&tool.get(DataComponents.CUSTOM_NAME).equals(customName)&&net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(unbreaking,tool)==2,"Ascension must preserve damage, names and enchantments");
                check(h,tool.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().size()==attributes,"Refreshing reach must replace its native modifier rather than add duplicates");
                double reach=tool.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.BLOCK_INTERACTION_RANGE,4.5,EquipmentSlot.MAINHAND);check(h,Math.abs(reach-(4.5+PickaxeModifiers.totals(tool).get(REACH)))<1e-8,"Native reach must immediately reflect ascension");
                check(h,tool.getMaxDamage()==(int)Math.round(1800*(1+PickaxeModifiers.totals(tool).get(DURABILITY))),"Native durability must immediately reflect ascension");
                check(h,tool.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY).equals(original),"Original rolls, excavation scores and provenance must never change");
            }
            check(h,PickaxeProgression.read(tool).mastered()&&!PickaxeProgression.gain(tool,1)&&AscensionResources.requirements(tool).isEmpty(),"Final ascension at level 100 must complete progression");
            var ops=h.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);var decoded=ItemStack.CODEC.parse(ops,ItemStack.CODEC.encodeStart(ops,tool).getOrThrow()).getOrThrow();check(h,ItemStack.matches(tool,decoded),"All progression and effective attributes must survive item serialization");
        }
    }
    private static void verifyCapsAndSplitStacks(GameTestHelper h){
        for(var trait:PickaxeTrait.values()){
            var sample=tool(100);var root=sample.get(DataComponents.CUSTOM_DATA).copyTag();var assembly=root.getCompoundOrEmpty(ArtifactQuality.ASSEMBLY);var parts=assembly.getCompoundOrEmpty("Parts");
            for(String id:ReconstructionPattern.PICKAXE.parts){var part=parts.getCompoundOrEmpty(id);part.put("Modifiers",ArtifactModifiers.writeRolls(new CompoundTag(),List.of(new ArtifactModifiers.Roll(trait.id,trait.cap,1))));parts.put(id,part);}
            assembly.put("Parts",parts);root.put(ArtifactQuality.ASSEMBLY,assembly);sample.set(DataComponents.CUSTOM_DATA,CustomData.of(root));PickaxeProgression.write(sample,new PickaxeProgression.State(100,0,10));
            check(h,PickaxeModifiers.totals(sample).get(trait)==trait.cap,"Every modifier must respect its combined cap, including duplicate legacy rolls");
        }
        var tool=tool(100);PickaxeProgression.write(tool,new PickaxeProgression.State(100,0,9));PickaxeModifiers.applyToAssembly(tool);
        var inventory=new SimpleContainer(AscenderBlockEntity.SIZE);inventory.setItem(0,tool);var costs=AscensionResources.requirements(tool);fill(inventory,costs,0);
        check(h,costs.stream().anyMatch(c->c.item()==Items.ENDER_PEARL)&&AscensionResources.tryAscend(inventory),"Mixed resources and split pearl stacks must ascend together");
        for(int i=1;i<inventory.getContainerSize();i++)check(h,inventory.getItem(i).isEmpty(),"Split material stacks must be consumed exactly once");
    }
    private static void verifyInventory(GameTestHelper h){
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(3,1,4));level.setBlockAndUpdate(pos,AscenderContent.BLOCK.get().defaultBlockState());var table=(AscenderBlockEntity)level.getBlockEntity(pos);
        var player=h.makeMockServerPlayerInLevel();player.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);player.getInventory().clearContent();var menu=new AscenderMenu(68,player.getInventory(),table);
        check(h,menu.slots.size()==55&&menu.getSlot(0).getMaxStackSize()==1&&!menu.getSlot(0).mayPlace(new ItemStack(Items.DIAMOND_PICKAXE))&&!menu.getSlot(1).mayPlace(new ItemStack(Items.DIRT)),"Ascender slots must reject wrong tools and unrelated materials");
        var tool=tool(100);PickaxeProgression.gain(tool,Integer.MAX_VALUE);player.getInventory().setItem(9,tool);
        var entering=tool.copy();check(h,!menu.quickMoveStack(player,19).isEmpty()&&ItemStack.matches(table.getItem(0),entering),"Shift-click must route a restored tool into its single slot");tool=table.getItem(0);
        var costs=AscensionResources.requirements(tool);fill(table,costs,3);var saved=table.saveWithFullMetadata(level.registryAccess());var restored=BlockEntity.loadStatic(pos,table.getBlockState(),saved,level.registryAccess());
        check(h,restored instanceof AscenderBlockEntity,"Ascender inventory must reload");for(int i=0;i<table.getContainerSize();i++)check(h,ItemStack.matches(table.getItem(i),((AscenderBlockEntity)restored).getItem(i)),"Saving must retain all materials and tool data");
        var ticker=AscenderContent.BLOCK.get().getTicker(level,table.getBlockState(),AscenderContent.ENTITY.get());check(h,ticker!=null,"The actual block must register a server ticker");ticker.tick(level,pos,table.getBlockState(),table);
        tool=table.getItem(0);check(h,PickaxeProgression.read(tool).ascensions()==1,"The native block ticker must automatically ascend an eligible tool");for(var cost:costs)check(h,AscensionResources.available(table,cost.item())==3,"Only required quantities must be consumed");
        var copy=tool.copy();ticker.tick(level,pos,table.getBlockState(),table);check(h,ItemStack.matches(copy,table.getItem(0)),"A repeated native tick must not ascend or charge again");
        check(h,!menu.quickMoveStack(player,0).isEmpty()&&table.getItem(0).isEmpty(),"The upgraded tool must shift-click back out");
        table.setItem(0,copy.copy());var bounds=new net.minecraft.world.phys.AABB(pos).inflate(2);level.getEntitiesOfClass(ItemEntity.class,bounds).forEach(ItemEntity::discard);level.destroyBlock(pos,true);
        var drops=level.getEntitiesOfClass(ItemEntity.class,bounds);int expected=1+1+3*costs.size();check(h,drops.stream().mapToInt(e->e.getItem().getCount()).sum()==expected&&drops.stream().filter(e->ItemStack.matches(copy,e.getItem())).count()==1,"Breaking the Ascender must drop its block, upgraded tool and remaining materials exactly once");
    }
    private static void fill(net.minecraft.world.Container inventory,List<AscensionResources.Requirement> costs,int extra){
        for(int i=1;i<inventory.getContainerSize();i++)inventory.setItem(i,ItemStack.EMPTY);int slot=1;
        for(var cost:costs){int remaining=cost.count()+extra;while(remaining>0){var stack=new ItemStack(cost.item());int count=Math.min(remaining,stack.getMaxStackSize());stack.setCount(count);inventory.setItem(slot++,stack);remaining-=count;}}
    }
    private static ItemStack tool(int score){
        var parts=new ArrayList<ItemStack>();
        for(int i=0;i<CreativePickaxeParts.FINDS.size();i++){
            var find=CreativePickaxeParts.FINDS.get(i);var part=new ItemStack(ReconstructionContent.reward(find));var root=new CompoundTag();var data=new CompoundTag();data.putInt("Quality",score);data.putInt("Rarity",5);root.put(ArtifactQuality.RECOVERED,data);
            var profile=new ArtifactProfile(71+i,5,ArtifactProfile.Matrix.SHALE,find);var record=ArtifactModifiers.record(profile,score,new ArtifactModifiers.Pool(0,0,List.of()));var rolls=new ArrayList<ArtifactModifiers.Roll>();
            if(i<2)rolls.add(new ArtifactModifiers.Roll(SPEED.id,.1,score/100.0));if(i==0){rolls.add(new ArtifactModifiers.Roll(REACH.id,.5,score/100.0));rolls.add(new ArtifactModifiers.Roll(DURABILITY.id,.2,score/100.0));}
            root.put(ArtifactModifiers.KEY,ArtifactModifiers.writeRolls(record,rolls));part.set(DataComponents.CUSTOM_DATA,CustomData.of(root));parts.add(part);
        }
        return ArtifactQuality.assemble(parts);
    }
    private static void check(GameTestHelper h,boolean condition,String message){if(!condition)throw h.assertionException(message);}
    private PickaxeAscensionTests(){}
}
