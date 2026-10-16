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
        @Override protected MutableComponent typeDescription(){return Component.literal("Excavation tools, retained specimens and multiplayer action validation");}
        @Override public void run(GameTestHelper helper){
            verifyTools(helper);var level=helper.getLevel();
            for(int x=0;x<8;x++)for(int z=0;z<8;z++)helper.setBlock(new BlockPos(x,0,z),Blocks.STONE);
            var local=new BlockPos(3,1,3);var pos=helper.absolutePos(local);var state=ExampleMod.ARCHAEOLOGY_TABLE.get().defaultBlockState();
            helper.setBlock(local,state);var player=helper.makeMockServerPlayerInLevel();var other=helper.makeMockServerPlayerInLevel();
            player.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);other.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+1.5);
            ExampleMod.ARCHAEOLOGY_TABLE.get().setPlacedBy(level,pos,state,player,new ItemStack(ExampleMod.ARCHAEOLOGY_TABLE_ITEM.get()));
            var table=ArchaeologyTableBlock.inventory(level,pos);var artifact=new ItemStack(ExampleMod.ANCIENT_ARTIFACT.get());
            artifact.set(DataComponents.CUSTOM_NAME,Component.literal("Saved specimen"));
            var custom=new net.minecraft.nbt.CompoundTag();custom.putString("unrelated","preserved");artifact.set(DataComponents.CUSTOM_DATA,CustomData.of(custom));table.setItem(0,artifact);
            var menu=new ArchaeologyTableMenu(22,player.getInventory(),table),collaborator=new ArchaeologyTableMenu(23,other.getInventory(),table);
            require(helper,!menu.studyAction(player,1,CENTER),"A packet must not operate a menu the sender has not opened");
            var old=player.containerMenu;var oldOther=other.containerMenu;player.containerMenu=menu;other.containerMenu=collaborator;
            require(helper,!menu.studyAction(player,1,CENTER),"A missing field-kit tool must block excavation");
            table.setItem(1,new ItemStack(ExampleMod.ARCHAEOLOGY_DRILL.get()));table.setItem(2,new ItemStack(net.minecraft.world.item.Items.BRUSH));
            require(helper,!menu.studyAction(player,999,CENTER)&&!menu.studyAction(player,1,Integer.MAX_VALUE)&&!menu.studyAction(player,1,-2),"Forged tools and coordinates must be ignored");
            require(helper,!menu.studyAction(other,1,CENTER),"One player must not issue actions through another player's menu");
            require(helper,menu.studyAction(player,1,CENTER)&&ExcavationStudy.read(table.getItem(0)).depth(CENTER)==2,"A valid drill action must remove one layer");
            require(helper,!menu.studyAction(player,1,CENTER),"Repeated same-tick packets must be throttled");
            require(helper,collaborator.studyAction(other,1,CENTER)&&ExcavationStudy.read(table.getItem(0)).depth(CENTER)==1,"Collaborating menus must read current server state without overwriting progress");
            var restored=BlockEntity.loadStatic(pos,state,table.saveWithFullMetadata(level.registryAccess()),level.registryAccess());
            require(helper,restored instanceof ArchaeologyTableBlockEntity && ItemStack.matches(((ArchaeologyTableBlockEntity)restored).getItem(0),table.getItem(0)),"Study data and artifact components must survive table serialization");
            var clientMenu=new ArchaeologyTableMenu(24,player.getInventory());
            require(helper,!clientMenu.getSlot(5).isActive()&&clientMenu.getSlot(32).isActive()&&menu.getSlot(5).isActive(),"Only the client's main inventory is hidden on the excavation tab");
            clientMenu.inventoryView(true);require(helper,clientMenu.getSlot(5).isActive(),"Inventory tab must restore ordinary slot interaction");
            var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),level.registryAccess());
            try {for(var packet:new ExcavationPayload[]{new ExcavationPayload(22,3,ExcavationStudy.CELLS-1),new ExcavationPayload(22,0,-1)}){
                buffer.clear();ExcavationPayload.STREAM_CODEC.encode(buffer,packet);require(helper,packet.equals(ExcavationPayload.STREAM_CODEC.decode(buffer)),"Payload must preserve the entire grid and reset action");
            }}finally{buffer.release();}
            helper.runAfterDelay(2,()->{
                require(helper,menu.studyAction(player,2,CENTER)&&ExcavationStudy.read(table.getItem(0)).depth(CENTER)==0,"Brush must remove the final sediment without damage");
                require(helper,ExcavationStudy.read(table.getItem(0)).condition()==100&&table.getItem(0).getCount()==1,"Prototype study must preserve the actual artifact item");
                helper.runAfterDelay(2,()->{
                    require(helper,menu.studyAction(player,0,-1),"Reset must be available for this non-consuming study");
                    var reset=ExcavationStudy.read(table.getItem(0));var root=table.getItem(0).get(DataComponents.CUSTOM_DATA).copyTag();
                    require(helper,reset.progress()==0&&reset.condition()==100&&root.getStringOr("unrelated","").equals("preserved")&&table.getItem(0).getHoverName().getString().equals("Saved specimen"),"Reset must preserve unrelated data and the artifact name");
                    player.snapTo(pos.getX()+30,pos.getY(),pos.getZ());require(helper,!menu.studyAction(player,1,CENTER),"Remote players must not excavate");
                    player.containerMenu=old;other.containerMenu=oldOther;helper.succeed();
                });
            });
        }
    }
    private static void verifyTools(GameTestHelper helper){
        var study=new ExcavationStudy();require(helper,study.progress()==0&&study.condition()==100,"New study must start covered and intact");
        require(helper,!study.work(2,CENTER)&&!study.work(4,CENTER),"Brush cannot break heavy cover, and magnifier cannot remove it");
        require(helper,study.work(3,CENTER)&&study.depth(CENTER)==1&&study.condition()==100,"One hammer strike should remove coarse cover safely");
        require(helper,study.work(3,CENTER)&&study.condition()<100,"Hammer must warn through condition loss when used near exposed material");
        var precision=new ExcavationStudy();precision.work(1,CENTER);precision.work(1,CENTER);precision.work(1,CENTER);
        require(helper,precision.condition()==99,"Drill must be less damaging but still require care at the surface");
        var safe=new ExcavationStudy();
        for(int i=0;i<ExcavationStudy.CELLS;i++)if(ExcavationStudy.coveredCell(i)){safe.work(1,i);safe.work(1,i);}
        for(int i=0;i<ExcavationStudy.CELLS;i++)safe.work(2,i);
        require(helper,safe.complete()&&safe.condition()==100&&!safe.work(3,CENTER),"Careful excavation must finish intact and stop accepting destructive strokes after completion");
        var item=new ItemStack(ExampleMod.RARE_ANCIENT_ARTIFACT.get());safe.write(item);
        require(helper,safe.equals(ExcavationStudy.read(item)),"Study state must travel with a removed artifact");
        require(helper,!new ExcavationStudy().work(-9,CENTER)&&!new ExcavationStudy().work(3,-1),"Out-of-range strokes must be ignored");
        var malformed=new net.minecraft.nbt.CompoundTag();var root=new net.minecraft.nbt.CompoundTag();malformed.putInt("Version",1);malformed.putByteArray("Depths",new byte[1]);root.put(ExcavationStudy.KEY,malformed);
        item.set(DataComponents.CUSTOM_DATA,CustomData.of(root));require(helper,ExcavationStudy.read(item).equals(new ExcavationStudy()),"Malformed study data must recover to a fresh board");
    }
    private static void require(GameTestHelper helper,boolean valid,String message){if(!valid)throw helper.assertionException(message);}
}
