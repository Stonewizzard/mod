package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.Unpooled;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public final class ExcavationTests {
    private static final int CENTER=6*ExcavationStudy.COLS+14;
    public static void register(RegisterGameTestsEvent event){
        var env=event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"excavation_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"excavation_study"),new StudyTest(new TestData<>(env,Level.OVERWORLD,
                Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));
    }
    private static final class StudyTest extends GameTestInstance {
        StudyTest(TestData<Holder<TestEnvironmentDefinition<?>>> data){super(data);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Sealed artifacts, tiered tools, persistent excavation and atomic recovery");}
        @Override public void run(GameTestHelper helper){
            verifyStudies(helper);verifyRecipes(helper);var level=helper.getLevel();
            for(int x=0;x<8;x++)for(int z=0;z<8;z++)helper.setBlock(new BlockPos(x,0,z),Blocks.STONE);
            var local=new BlockPos(3,1,3);var pos=helper.absolutePos(local);var state=ExampleMod.ARCHAEOLOGY_TABLE.get().defaultBlockState();
            helper.setBlock(local,state);var player=helper.makeMockServerPlayerInLevel();var other=helper.makeMockServerPlayerInLevel();
            player.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);other.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+1.5);
            ExampleMod.ARCHAEOLOGY_TABLE.get().setPlacedBy(level,pos,state,player,new ItemStack(ExampleMod.ARCHAEOLOGY_TABLE_ITEM.get()));
            var table=ArchaeologyTableBlock.inventory(level,pos);var artifact=new ItemStack(ExampleMod.ANCIENT_ARTIFACT.get());
            ArtifactProfile.legacy().write(artifact);artifact.set(DataComponents.CUSTOM_NAME,Component.literal("Saved specimen"));table.setItem(0,artifact);
            var menu=new ArchaeologyTableMenu(22,player.getInventory(),table);var collaborator=new ArchaeologyTableMenu(23,other.getInventory(),table);
            require(helper,!menu.studyAction(player,1,CENTER),"Unopened menu must reject packets");
            var old=player.containerMenu;var oldOther=other.containerMenu;player.containerMenu=menu;other.containerMenu=collaborator;
            for(int tier=0;tier<3;tier++){
                for(int role=1;role<=4;role++){var tool=new ItemStack(tier==0?ArchaeologyTableMenu.tool(role):ArchaeologyContent.upgrade(role,tier));tool.set(DataComponents.CUSTOM_NAME,Component.literal("Tool "+tier+"/"+role));table.setItem(role,tool);}
                verifyToolSwitches(helper,menu,player,table);
            }
            for(int role=1;role<=4;role++)table.setItem(role,new ItemStack(ArchaeologyTableMenu.tool(role)));
            require(helper,menu.carriedTool()==0&&!menu.studyAction(player,1,CENTER),"Stored tools cannot operate");
            menu.clicked(1,0,ContainerInput.PICKUP,player);
            require(helper,!menu.studyAction(player,2,CENTER)&&!menu.studyAction(other,1,CENTER)&&!menu.studyAction(player,999,CENTER)&&!menu.studyAction(player,1,Integer.MAX_VALUE),"Forged or wrong-cursor actions must be rejected");
            require(helper,menu.studyAction(player,1,CENTER)&&ExcavationStudy.read(table.getItem(0)).depth(CENTER)==2,"Native carried drill must remove a layer");
            require(helper,menu.getCarried().getDamageValue()==1,"Successful excavation must wear the actual cursor tool once");
            require(helper,!menu.studyAction(player,1,CENTER),"Same-tick requests must be throttled");
            other.getInventory().setItem(0,new ItemStack(ArchaeologyContent.PRECISION_DRILL.get()));collaborator.clicked(33,0,ContainerInput.PICKUP,other);
            require(helper,collaborator.carriedTool()==1&&collaborator.studyAction(other,1,CENTER)&&ExcavationStudy.read(table.getItem(0)).depth(CENTER)==1,"Second viewer must work against current shared server state");
            require(helper,!collaborator.studyAction(other,0,-1),"An unfinished specimen cannot be recovered or reset");
            var restored=BlockEntity.loadStatic(pos,state,table.saveWithFullMetadata(level.registryAccess()),level.registryAccess());
            require(helper,restored instanceof ArchaeologyTableBlockEntity&&ItemStack.matches(((ArchaeologyTableBlockEntity)restored).getItem(0),table.getItem(0)),"Study and profile must survive table serialization");
            var client=new ArchaeologyTableMenu(24,player.getInventory());
            require(helper,client.getSlot(5).isActive()&&!client.getSlot(6).isActive()&&client.getSlot(33).isActive(),"Output and hotbar remain available; main inventory is tabbed");
            client.inventoryView(true);require(helper,client.getSlot(6).isActive()&&!client.getSlot(5).mayPlace(new ItemStack(Items.DIRT)),"Inventory tab must work and output must reject deposits");
            var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),level.registryAccess());
            try {for(var packet:new ExcavationPayload[]{new ExcavationPayload(22,3,ExcavationStudy.CELLS-1),new ExcavationPayload(22,0,-1)}){buffer.clear();ExcavationPayload.STREAM_CODEC.encode(buffer,packet);require(helper,packet.equals(ExcavationPayload.STREAM_CODEC.decode(buffer)),"Packet roundtrip must retain recovery and grid coordinates");}}finally{buffer.release();}
            helper.runAfterDelay(4,()->{
                menu.clicked(2,0,ContainerInput.PICKUP,player);require(helper,menu.studyAction(player,2,CENTER)&&ExcavationStudy.read(table.getItem(0)).depth(CENTER)==0,"Swapping to brush must safely finish the last layer");
                require(helper,ExcavationStudy.read(table.getItem(0)).condition()==100,"Careful brush finish must preserve condition");
                var sealed=ArchaeologyContent.specimen(92,2);var finished=finish(ExcavationStudy.read(sealed));finished.write(sealed);table.setItem(0,sealed);table.setItem(5,new ItemStack(Items.DIRT));
                helper.runAfterDelay(2,()->{
                    require(helper,!menu.studyAction(player,0,-1)&&ItemStack.matches(table.getItem(0),sealed),"Full output must not consume the specimen");
                    table.setItem(5,ItemStack.EMPTY);
                    require(helper,menu.studyAction(player,0,-1)&&table.getItem(0).isEmpty()&&!table.getItem(5).isEmpty(),"Recovery consumes one input and publishes one reward");
                    var reward=table.getItem(5).copy();require(helper,!collaborator.studyAction(other,0,-1)&&ItemStack.matches(reward,table.getItem(5)),"Two viewers must not claim twice");
                    require(helper,!menu.quickMoveStack(player,5).isEmpty()&&table.getItem(5).isEmpty(),"Recovered output must shift-click into the player inventory");
                    menu.clicked(2,0,ContainerInput.PICKUP,player);menu.clicked(1,0,ContainerInput.PICKUP,player);int before=player.getInventory().countItem(menu.getCarried().getItem());
                    menu.removed(player);require(helper,menu.getCarried().isEmpty()&&player.getInventory().countItem(ExampleMod.ARCHAEOLOGY_DRILL.get())==before+1,"Closing must return the actual held tool exactly once");
                    player.containerMenu=old;other.containerMenu=oldOther;helper.succeed();
                });
            });
        }
    }
    private static void verifyToolSwitches(GameTestHelper helper,ArchaeologyTableMenu menu,net.minecraft.server.level.ServerPlayer player,Container table){
        var original=new ItemStack[5];for(int i=0;i<5;i++)original[i]=table.getItem(i).copy();
        for(int held=1;held<=4;held++)for(int target=1;target<=4;target++)if(held!=target){
            menu.clicked(held,0,ContainerInput.PICKUP,player);menu.clicked(target,0,ContainerInput.PICKUP,player);
            require(helper,ItemStack.matches(table.getItem(held),original[held])&&table.getItem(target).isEmpty()&&ItemStack.matches(menu.getCarried(),original[target]),
                    "One click must return the held tool home and pick up the clicked tool, preserving item components");
            require(helper,ItemStack.matches(table.getItem(0),original[0]),"Tool switching must leave the artifact unchanged");
            menu.clicked(held,0,ContainerInput.PICKUP,player);menu.clicked(held,0,ContainerInput.PICKUP,player);
            for(int i=1;i<=4;i++)require(helper,ItemStack.matches(table.getItem(i),original[i]),"Switching back must retain exactly one original tool in each slot");
            require(helper,menu.getCarried().isEmpty(),"Returning a tool normally must clear the cursor");
        }
        menu.clicked(4,0,ContainerInput.PICKUP,player);
        table.setItem(4,original[4].copy());menu.clicked(3,0,ContainerInput.PICKUP,player);
        require(helper,ItemStack.matches(menu.getCarried(),original[4])&&ItemStack.matches(table.getItem(4),original[4])&&ItemStack.matches(table.getItem(3),original[3]),
                "An occupied home slot must prevent a switch without overwriting or losing either tool");
        table.setItem(4,ItemStack.EMPTY);table.setItem(3,ItemStack.EMPTY);menu.clicked(3,0,ContainerInput.PICKUP,player);
        require(helper,ItemStack.matches(menu.getCarried(),original[4])&&table.getItem(3).isEmpty(),"An empty target is not a selectable tool");
        table.setItem(3,original[3].copy());menu.clicked(3,1,ContainerInput.PICKUP,player);
        require(helper,menu.carriedTool()==4&&ItemStack.matches(table.getItem(3),original[3]),"Secondary inventory clicks must keep native slot behavior");
        menu.clicked(4,0,ContainerInput.PICKUP,player);
        var predicted=new ArchaeologyTableMenu(25,player.getInventory());
        for(int i=1;i<=4;i++)predicted.getSlot(i).setByPlayer(original[i].copy());
        predicted.clicked(4,0,ContainerInput.PICKUP,player);predicted.clicked(3,0,ContainerInput.PICKUP,player);
        require(helper,ItemStack.matches(predicted.getSlot(4).getItem(),original[4])&&predicted.getSlot(3).getItem().isEmpty()&&ItemStack.matches(predicted.getCarried(),original[3]),
                "The client menu must predict the same lens-to-hammer switch as the server");
    }
    private static void verifyRecipes(GameTestHelper helper){
        var level=helper.getLevel();var manager=level.getServer().getRecipeManager();
        var unbreaking=level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING);
        for(int tier=1;tier<=2;tier++)for(int role=1;role<=4;role++){
            var base=new ItemStack(tier==1?ArchaeologyTableMenu.tool(role):ArchaeologyContent.upgrade(role,1));
            base.set(DataComponents.CUSTOM_NAME,Component.literal("Inherited tool"));
            if(role!=4){base.setDamageValue(7);base.enchant(unbreaking,2);}
            var input=new net.minecraft.world.item.crafting.SmithingRecipeInput(new ItemStack(tier==1?ArchaeologyContent.FRAGMENTS.get():ArchaeologyContent.CORE.get()),base,new ItemStack(tier==1?Items.DIAMOND:Items.NETHERITE_SCRAP));
            var recipe=manager.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMITHING,input,level);
            require(helper,recipe.isPresent(),"All eight native smithing upgrades must load and match");
            var upgraded=recipe.get().value().assemble(input);
            require(helper,upgraded.is(ArchaeologyContent.upgrade(role,tier))&&upgraded.getHoverName().getString().equals("Inherited tool"),"Smithing must preserve custom names on the correct tier");
            if(role!=4)require(helper,upgraded.getDamageValue()==7&&net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(unbreaking,upgraded)==2,"Upgrade must preserve damage and enchantments");
        }
        var parts=new net.minecraft.world.item.Item[]{ArchaeologyContent.SWORD_BLADE.get(),ArchaeologyContent.PICKAXE_HEAD.get(),ArchaeologyContent.AXE_HEAD.get()};
        var gear=new net.minecraft.world.item.Item[]{ArchaeologyContent.SWORD.get(),ArchaeologyContent.PICKAXE.get(),ArchaeologyContent.AXE.get()};
        int[][] pattern={{0,1,0,0,2,0,0,3,0},{0,1,0,2,3,2,0,3,0},{1,2,0,0,3,0,0,3,0}};
        for(int i=0;i<3;i++){
            final int kind=i;var stacks=java.util.Arrays.stream(pattern[i]).mapToObj(n->n==0?ItemStack.EMPTY:new ItemStack(n==1?parts[kind]:n==2?Items.IRON_INGOT:Items.STICK)).toList();
            var input=net.minecraft.world.item.crafting.CraftingInput.of(3,3,stacks);var recipe=manager.getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,level);
            require(helper,recipe.isPresent()&&recipe.get().value().assemble(input).is(gear[i]),"Every recovered equipment part must have a working restored-gear recipe");
        }
        System.out.println("ARCHAEOLOGY_RECIPE_CHECK: PASS: eight upgrades retain names, damage and enchantments; three restored equipment recipes");
    }
    private static ExcavationStudy finish(ExcavationStudy study){
        for(int i=0;i<ExcavationStudy.CELLS;i++)while(study.depth(i)>1&&!study.recoverable())study.work(1,i);
        for(int i=0;i<ExcavationStudy.CELLS;i++)study.work(2,i);
        return study;
    }
    private static void verifyStudies(GameTestHelper helper){
        var risky=new ExcavationStudy();risky.work(3,CENTER);risky.work(3,CENTER);
        require(helper,risky.condition()>=92&&risky.condition()<100,"Damage must be meaningful but capped per stroke");
        var precise=new ExcavationStudy();precise.work(1,1,CENTER);precise.work(1,1,CENTER);
        require(helper,precise.depth(CENTER)==1&&!precise.work(1,1,CENTER)&&precise.condition()==100,"Precision drill must stop at the dust layer without damage or wear");
        var brush=new ExcavationStudy();brush.work(1,CENTER);
        require(helper,!brush.copy().work(2,0,CENTER)&&brush.work(2,1,CENTER)&&brush.depth(CENTER)==1,"Upgraded brush must remove weathered cover that a field brush cannot");
        require(helper,ExcavationStudy.radius(4,2)>ExcavationStudy.radius(4,0)&&ArchaeologyContent.cooldown(1,1)<ArchaeologyContent.cooldown(1,0),"Tier effects must include lens reach and drill speed");
        var kinds=new java.util.HashSet<ArtifactProfile.Find>();var matrices=new java.util.HashSet<ArtifactProfile.Matrix>();
        for(int rarity=0;rarity<4;rarity++)for(int seed=0;seed<40;seed++){
            var stack=ArchaeologyContent.specimen(seed,rarity);var profile=ArtifactProfile.read(stack);kinds.add(profile.find());matrices.add(profile.matrix());
            require(helper,profile.equals(ArtifactProfile.roll(seed,rarity)),"A specimen must seal its deterministic profile");
            var a=ExcavationStudy.read(stack);a.work(1,CENTER);a.write(stack);ArtifactProfile.seal(stack,seed+1);
            require(helper,a.equals(ExcavationStudy.read(stack)),"Reinsertion must preserve progress and cannot reroll contents");
            var safe=finish(a);require(helper,safe.complete()&&safe.condition()==100,"Every rarity and matrix must be finishable with starter tools without damage");
            var reward=ArchaeologyContent.reward(safe);require(helper,!reward.isEmpty()&&reward.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty("examplemod:recovered").getIntOr("Quality",-1)==100,"Recovered components must record their quality and provenance");
        }
        require(helper,kinds.size()==5&&matrices.size()==5,"Seeded variation must exercise all find types and rock matrices");
        var old=new ItemStack(ExampleMod.ANCIENT_ARTIFACT.get());var root=new net.minecraft.nbt.CompoundTag();root.putString("unrelated","keep");var tag=new net.minecraft.nbt.CompoundTag();tag.putInt("Version",1);
        byte[] depths=new byte[ExcavationStudy.CELLS];java.util.Arrays.fill(depths,(byte)1);tag.putByteArray("Depths",depths);tag.putByteArray("Scars",new byte[ExcavationStudy.CELLS]);tag.putInt("Damage",17);root.put(ExcavationStudy.KEY,tag);old.set(DataComponents.CUSTOM_DATA,CustomData.of(root));ArtifactProfile.seal(old,123);
        var migrated=ExcavationStudy.read(old);require(helper,migrated.profile().find()==ArtifactProfile.Find.LEGACY&&migrated.condition()==83&&migrated.depth(CENTER)==1,"Version-one studies must preserve their footprint, condition and cover");migrated.write(old);
        require(helper,old.get(DataComponents.CUSTOM_DATA).copyTag().getStringOr("unrelated","").equals("keep"),"Study writes must preserve unrelated item data");
        var random=net.minecraft.util.RandomSource.create(222);int[] sniffer=new int[4],cruncher=new int[4];
        for(int i=0;i<1000;i++){sniffer[ArchaeologyContent.rarity(ArchaeologyContent.specimen(random,false))]++;cruncher[ArchaeologyContent.rarity(ArchaeologyContent.specimen(random,true))]++;}
        require(helper,sniffer[0]>600&&sniffer[1]>180&&sniffer[2]>20&&sniffer[3]==0&&cruncher[0]==0&&cruncher[1]==0&&cruncher[3]>130&&cruncher[2]>700,"Loot rarity weights must remain in their documented ranges");
        System.out.println("ARCHAEOLOGY_EXPANSION_CHECK: PASS: 160 sealed specimens, all matrices/finds, migration, safe field completion, tool tiers, provenance and weighted source rewards");
    }
    private static void require(GameTestHelper helper,boolean valid,String message){if(!valid)throw helper.assertionException(message);}
}
