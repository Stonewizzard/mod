package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public final class ReconstructionTests {
    public static void register(RegisterGameTestsEvent event){
        var env=event.registerEnvironment(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"reconstruction_environment"));
        event.registerTest(Identifier.fromNamespaceAndPath(ExampleMod.MODID,"reconstruction_table"),new Test(new TestData<>(env,Level.OVERWORLD,Identifier.fromNamespaceAndPath(ExampleMod.MODID,"empty_drop_test"),100,0,true,Rotation.NONE,false,1,1,false,0)));
    }
    private static final class Test extends GameTestInstance {
        Test(TestData<Holder<TestEnvironmentDefinition<?>>> data){super(data);}
        @Override public MapCodec<? extends GameTestInstance> codec(){return MapCodec.unit(this);}
        @Override protected MutableComponent typeDescription(){return Component.literal("Five-piece pickaxe reconstruction and preserved excavation scores");}
        @Override public void run(GameTestHelper helper){
            var level=helper.getLevel();var local=new BlockPos(3,1,3);var pos=helper.absolutePos(local);helper.setBlock(local.below(),Blocks.STONE);helper.setBlock(local,ReconstructionContent.TABLE.get());
            var table=(ReconstructionTableBlockEntity)level.getBlockEntity(pos);var player=helper.makeMockServerPlayerInLevel();var second=helper.makeMockServerPlayerInLevel();player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            player.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);second.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
            var menu=new ReconstructionTableMenu(41,player.getInventory(),table);var other=new ReconstructionTableMenu(42,second.getInventory(),table);
            var original=player.containerMenu;var oldSecond=second.containerMenu;player.containerMenu=menu;second.containerMenu=other;
            require(helper,menu.slots.size()==42&&!menu.ready()&&menu.pattern()==0&&!menu.clickMenuButton(player,1),"The bench must require selecting a pattern and all five pieces");
            new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(41,0).handle(player.connection);require(helper,menu.pattern()==1&&table.pattern()==1,"Native Pickaxe selection must synchronize the bench pattern");
            require(helper,!menu.getSlot(0).mayPlace(new ItemStack(ReconstructionContent.RIGHT.get()))&&!menu.getSlot(5).mayPlace(new ItemStack(Items.DIRT)),"Piece positions and output must reject wrong items");
            var finds=new ArtifactProfile.Find[]{ArtifactProfile.Find.PICKAXE_LEFT,ArtifactProfile.Find.PICKAXE_BRIDGE,ArtifactProfile.Find.PICKAXE_RIGHT,ArtifactProfile.Find.TOOL_CONNECTOR,ArtifactProfile.Find.TOOL_HANDLE};
            int[] scores={92,87,100,89,96};var recovered=new ArrayList<ItemStack>();
            for(int i=0;i<5;i++){
                var specimen=new ItemStack(ExampleMod.ANCIENT_ARTIFACT.get());new ArtifactProfile(300+i,i,ArtifactProfile.Matrix.SHALE,finds[i]).write(specimen);var study=ExcavationStudy.read(specimen);
                for(int cell=0;cell<ExcavationStudy.CELLS;cell++)while(study.depth(cell)>1&&!study.recoverable())study.work(1,cell);
                for(int cell=0;cell<ExcavationStudy.CELLS;cell++)study.work(2,cell);
                require(helper,study.complete()&&study.condition()==100,"All five new silhouettes must be completely excavatable");study.write(specimen);
                var root=specimen.get(DataComponents.CUSTOM_DATA).copyTag();var state=root.getCompoundOrEmpty(ExcavationStudy.KEY);state.putInt("Damage",100-scores[i]);root.put(ExcavationStudy.KEY,state);specimen.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
                var piece=ArchaeologyContent.reward(ExcavationStudy.read(specimen));require(helper,piece.is(ReconstructionPattern.PICKAXE.part(i))&&ArtifactQuality.score(piece)==scores[i]&&piece.has(DataComponents.LORE),"Recovered pieces must retain their exact score and display it as item information");recovered.add(piece.copy());
                player.getInventory().setItem(9+i,piece);require(helper,!menu.quickMoveStack(player,6+i).isEmpty()&&ItemStack.matches(table.getItem(i),recovered.get(i)),"Shift-click must route each scored piece to its matching blueprint position");
            }
            require(helper,menu.ready()&&menu.previewScore()==92.8,"Assembly preview must average all five scores without rounding to an integer");
            var saved=table.saveWithFullMetadata(level.registryAccess());var restored=BlockEntity.loadStatic(pos,table.getBlockState(),saved,level.registryAccess());
            require(helper,restored instanceof ReconstructionTableBlockEntity&&((ReconstructionTableBlockEntity)restored).pattern()==1,"Pattern selection must survive saving");
            for(int i=0;i<5;i++)require(helper,ItemStack.matches(((ReconstructionTableBlockEntity)restored).getItem(i),recovered.get(i)),"Stored component provenance must survive table saves");
            table.setItem(5,new ItemStack(Items.DIRT));require(helper,!menu.clickMenuButton(player,1),"A full output must consume nothing");for(int i=0;i<5;i++)require(helper,ItemStack.matches(table.getItem(i),recovered.get(i)),"Blocked assembly must retain every component");table.setItem(5,ItemStack.EMPTY);
            player.snapTo(pos.getX()+30,pos.getY(),pos.getZ());require(helper,!menu.clickMenuButton(player,1),"Remote players must not assemble tools");player.snapTo(pos.getX()+.5,pos.getY()+1,pos.getZ()+.5);
            new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(41,1).handle(player.connection);
            var result=table.getItem(5).copy();require(helper,result.is(ArchaeologyContent.PICKAXE.get())&&result.getMaxDamage()==(int)Math.round(1800*(1+PickaxeModifiers.totals(result).get(PickaxeTrait.DURABILITY))+PickaxeModifiers.totals(result).get(PickaxeTrait.RESERVE)),"Assembly must create the usable restored pickaxe with its score-scaled durability");
            var assembly=result.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY);require(helper,ArtifactQuality.assemblyScore(result)==92.8&&assembly.getDoubleOr("Score",-1)==92.8&&assembly.getIntOr("ScoreTotal",-1)==464,"The assembled tool must retain its aggregate score");
            for(int i=0;i<5;i++){require(helper,table.getItem(i).isEmpty(),"Successful assembly consumes exactly five pieces");var part=assembly.getCompoundOrEmpty("Parts").getCompoundOrEmpty(ReconstructionPattern.PICKAXE.parts.get(i));require(helper,part.getIntOr("Quality",-1)==scores[i]&&part.getLongOr("Seed",-1)==300+i&&part.getIntOr("Rarity",-1)==i,"The tool must preserve each original part score, seed and rarity for future modifiers");}
            require(helper,!other.clickMenuButton(second,1)&&ItemStack.matches(result,table.getItem(5)),"Two viewers must not duplicate the result");
            require(helper,!menu.quickMoveStack(player,5).isEmpty()&&table.getItem(5).isEmpty(),"The completed pickaxe must shift-click into inventory");
            // Native player save codec roundtrip checks score data after taking the item out of the workstation.
            var encoded=ItemStack.CODEC.encodeStart(level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),result).getOrThrow();
            var decoded=ItemStack.CODEC.parse(level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),encoded).getOrThrow();require(helper,ItemStack.matches(result,decoded),"The finished tool's complete provenance must survive item serialization");
            var blank=ReconstructionContent.pieces().stream().map(ItemStack::new).toList();require(helper,ArtifactQuality.average(blank)==0,"Unscored Creative parts must not receive an invented perfect excavation score");
            for(int i=0;i<5;i++)table.setItem(i,recovered.get(i).copy());table.setItem(5,result.copy());
            verifyPatterns(helper,menu,table,player,level);verifyModifiers(helper,level);verifyPresentation(helper);
            for(int i=0;i<5;i++)table.setItem(i,recovered.get(i).copy());table.setItem(5,result.copy());
            var bounds=helper.absoluteAABB(new net.minecraft.world.phys.AABB(0,0,0,8,8,8));level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,bounds).forEach(net.minecraft.world.entity.Entity::discard);level.destroyBlock(pos,true);
            var drops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,bounds);require(helper,drops.stream().mapToInt(e->e.getItem().getCount()).sum()==7,"Breaking the bench must return one block, five pieces and its completed output exactly once");
            require(helper,drops.stream().anyMatch(e->ItemStack.matches(e.getItem(),result)),"Dropped finished tools must preserve score data");
            verifyRecipe(helper);player.containerMenu=original;second.containerMenu=oldSecond;
            System.out.println("RECONSTRUCTION_CHECK: PASS: native pattern/assemble packets, five excavated components, exact individual scores, 92.8 assembly score, save/item serialization, shift-click, full-output/range/duplicate protection and block drops");helper.succeed();
        }
    }

    private static void verifyPatterns(GameTestHelper helper,ReconstructionTableMenu menu,ReconstructionTableBlockEntity table,net.minecraft.server.level.ServerPlayer player,net.minecraft.server.level.ServerLevel level){
        for(int i=0;i<6;i++)table.setItem(i,ItemStack.EMPTY);
        for(var pattern:ReconstructionPattern.values()){
            new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(41,10+pattern.id).handle(player.connection);
            require(helper,menu.pattern()==pattern.id,"Every native pattern selection must synchronize");
            var parts=new ArrayList<ItemStack>();
            for(int slot:pattern.slots){
                var part=new ItemStack(pattern.part(slot));var root=new net.minecraft.nbt.CompoundTag();var score=new net.minecraft.nbt.CompoundTag();score.putInt("Quality",80+slot);root.put(ArtifactQuality.RECOVERED,score);part.set(DataComponents.CUSTOM_DATA,CustomData.of(root));parts.add(part);
                require(helper,menu.getSlot(slot).mayPlace(part)&&table.canPlaceItem(slot,part),"Menu and automation must agree on each pattern's part roles");table.setItem(slot,part.copy());
            }
            if(pattern!=ReconstructionPattern.PICKAXE){
                table.setItem(0,new ItemStack(ReconstructionContent.LEFT.get()));require(helper,menu.getSlot(0).isActive()&&!menu.ready()&&!menu.clickMenuButton(player,1),"Spare pieces must remain removable and block assembly without being consumed");table.setItem(0,ItemStack.EMPTY);
            }
            require(helper,menu.ready(),"Every complete pattern must become assembleable");
            new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(41,1).handle(player.connection);var result=table.getItem(5);
            require(helper,result.is(pattern.output())&&result.getMaxDamage()==1800&&ArtifactQuality.assemblyScore(result)==ArtifactQuality.average(parts),"Every pattern must produce its usable scored tool");
            var data=result.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY);require(helper,data.getIntOr("PartCount",0)==parts.size()&&data.getStringOr("Pattern","").equals(pattern.key()),"Assembly provenance must retain its pattern and real part count");
            for(int slot:pattern.slots)require(helper,table.getItem(slot).isEmpty(),"Each assembly consumes exactly its required pieces");
            require(helper,!menu.clickMenuButton(player,1),"A full output must never assemble twice");table.setItem(5,ItemStack.EMPTY);
        }
        menu.clickMenuButton(player,11);var held=new ItemStack(ReconstructionContent.LEFT.get());table.setItem(0,held.copy());menu.clickMenuButton(player,12);
        require(helper,ItemStack.matches(table.getItem(0),held)&&menu.getSlot(0).isActive(),"Changing patterns must retain incompatible pieces, even with no inventory space");table.setItem(0,ItemStack.EMPTY);menu.clickMenuButton(player,11);
        require(helper,ArchaeologyContent.MACE.get() instanceof MaceItem,"The ancient mace must retain native smash behavior");
        require(helper,new ItemStack(ArchaeologyContent.SPEAR.get()).has(DataComponents.KINETIC_WEAPON),"The ancient spear must retain native charging/jab weapon components");
        System.out.println("ASSEMBLY_PATTERNS_CHECK: PASS: all seven native selections and usable outputs, real part counts, score preservation, no spare-item loss or duplicate assembly");
    }
    private static void verifyModifiers(GameTestHelper helper,net.minecraft.server.level.ServerLevel level){
        var a=new ArtifactModifiers.Definition("examplemod:test_a",8,0,60,.1,.3,"group");var b=new ArtifactModifiers.Definition("examplemod:test_b",2,0,60,.2,.4,"group");var c=new ArtifactModifiers.Definition("examplemod:test_c",5,2,80,1,2,"");
        var pool=new ArtifactModifiers.Pool(3,1,java.util.List.of(a,b,c));var profile=new ArtifactProfile(721,2,ArtifactProfile.Matrix.BASALT,ArtifactProfile.Find.SHOVEL_HEAD);
        var rolls=ArtifactModifiers.roll(profile,90,pool);require(helper,rolls.equals(ArtifactModifiers.roll(profile,90,pool))&&rolls.size()==2&&rolls.stream().map(ArtifactModifiers.Roll::id).distinct().count()==2,"Seeded rolls must be deterministic, unique and respect exclusive groups");
        require(helper,ArtifactModifiers.roll(profile,59,pool).isEmpty(),"Modifier entries must respect excavation score gates");
        var lower=ArtifactModifiers.roll(new ArtifactProfile(721,0,ArtifactProfile.Matrix.SHALE,profile.find()),90,pool);require(helper,lower.size()==1&&lower.stream().noneMatch(r->r.id().equals(c.id())),"Per-entry rarity gates must be respected");
        for(var find:ArtifactProfile.Find.values())require(helper,ArtifactModifierCatalog.pool(find).entries().isEmpty()!=ArtifactModifierCatalog.pickaxePart(find),"Only pickaxe-compatible parts should have active pools");
        var study=new ExcavationStudy(profile);for(int cell=0;cell<ExcavationStudy.CELLS;cell++)while(study.depth(cell)>1&&!study.complete())study.work(1,cell);for(int cell=0;cell<ExcavationStudy.CELLS;cell++)study.work(2,cell);
        var specimen=ArchaeologyContent.specimen(721,2);var root=specimen.get(DataComponents.CUSTOM_DATA).copyTag();var frozen=ArtifactModifiers.record(profile,90,pool);root.put(ArtifactModifiers.KEY,frozen);specimen.set(DataComponents.CUSTOM_DATA,CustomData.of(root));ArtifactModifiers.seal(specimen,study);
        var piece=ArchaeologyContent.reward(study,specimen);require(helper,ArtifactModifiers.read(piece).equals(rolls),"Recovery must preserve completion-time rolls rather than rerolling after pool edits");
        var result=ArtifactQuality.assemble(ReconstructionPattern.SHOVEL,java.util.List.of(piece,new ItemStack(ReconstructionContent.CONNECTOR.get()),new ItemStack(ReconstructionContent.HANDLE.get())));
        var part=result.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty(ArtifactQuality.ASSEMBLY).getCompoundOrEmpty("Parts").getCompoundOrEmpty("ancient_shovel_head");require(helper,part.getCompoundOrEmpty("Modifiers").equals(frozen),"Assembly must retain every modifier ID, raw value, scale and catalog revision per part");
        var encoded=ItemStack.CODEC.encodeStart(level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),result).getOrThrow();var decoded=ItemStack.CODEC.parse(level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),encoded).getOrThrow();require(helper,ItemStack.matches(result,decoded),"Modifier provenance must survive item serialization");
        System.out.println("MODIFIER_FOUNDATION_CHECK: PASS: deterministic weighted pools, eligibility, exclusive groups, pickaxe-only live pools, completion freeze, recovery/assembly/item-codec persistence");
    }
    private static void verifyPresentation(GameTestHelper helper){
        var pos=helper.absolutePos(new BlockPos(3,1,3));
        for(var facing:Direction.Plane.HORIZONTAL)for(var part:ArchaeologyTableBlock.Part.values()){
            var state=ExampleMod.ARCHAEOLOGY_TABLE.get().defaultBlockState().setValue(ArchaeologyTableBlock.FACING,facing).setValue(ArchaeologyTableBlock.PART,part);
            var boxes=state.getShape(helper.getLevel(),pos).toAabbs();
            var outside=rotatePoint(part==ArchaeologyTableBlock.Part.LEFT?2.5:13.5,2.5,facing);var seam=rotatePoint(part==ArchaeologyTableBlock.Part.LEFT?13.5:2.5,2.5,facing);
            require(helper,boxes.stream().anyMatch(b->b.contains(outside[0]/16,1.0/16,outside[1]/16))&&boxes.stream().noneMatch(b->b.contains(seam[0]/16,1.0/16,seam[1]/16)),"Rotated table outlines must keep the real outer legs and remove phantom legs at the seam");
        }

        for(int i=0;i<2;i++){
            var item=i==0?ArchaeologyContent.LEGENDARY.get():ArchaeologyContent.UNIQUE.get();int color=i==0?0xFFB52E:0xFF668B;var colors=new java.util.ArrayList<Integer>();
            new ItemStack(item).getStyledHoverName().visit((style,text)->{if(!text.isEmpty()&&style.getColor()!=null)colors.add(style.getColor().getValue());return java.util.Optional.empty();},net.minecraft.network.chat.Style.EMPTY);
            require(helper,!colors.isEmpty()&&colors.stream().allMatch(c->c==color),"Legendary and Unique names must keep distinct colors after native rarity styling");
        }
        require(helper,ArchaeologyContent.artifacts().size()==6&&ArchaeologyContent.artifacts().get(2)==ExampleMod.RARE_ANCIENT_ARTIFACT.get(),"Creative artifact order must match rarity progression");
        System.out.println("ARTIFACT_PRESENTATION_CHECK: PASS: native styled Legendary/Unique names and contiguous rarity order");
    }
    private static double[] rotatePoint(double x,double z,Direction facing){int turns=switch(facing){case EAST->1;case SOUTH->2;case WEST->3;default->0;};for(int i=0;i<turns;i++){double nx=16-z;z=x;x=nx;}return new double[]{x,z};}
    private static void verifyRecipe(GameTestHelper helper){
        var input=net.minecraft.world.item.crafting.CraftingInput.of(3,3,Arrays.asList(new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.OAK_PLANKS),ItemStack.EMPTY,new ItemStack(Items.CRAFTING_TABLE),ItemStack.EMPTY,new ItemStack(Items.STICK),ItemStack.EMPTY,new ItemStack(Items.STICK)));
        var recipe=helper.getLevel().getServer().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,helper.getLevel());require(helper,recipe.isPresent()&&recipe.get().value().assemble(input).is(ReconstructionContent.TABLE_ITEM.get()),"The starter reconstruction bench must have its inexpensive crafting recipe");
    }
    private static void require(GameTestHelper helper,boolean valid,String message){if(!valid)throw helper.assertionException(message);}
}
