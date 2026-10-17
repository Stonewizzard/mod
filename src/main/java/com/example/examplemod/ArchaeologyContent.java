package com.example.examplemod;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.registries.DeferredItem;

/** Registration and inventory semantics shared by the table, loot and creative tabs. */
public final class ArchaeologyContent {
    public static final DeferredItem<Item> UNCOMMON=ExampleMod.ITEMS.registerSimpleItem("uncommon_ancient_artifact",p->p.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> EPIC=ExampleMod.ITEMS.registerSimpleItem("epic_ancient_artifact",p->p.stacksTo(1).rarity(Rarity.EPIC));
    // Acquisition is deliberately left to future progression; neither item enters source loot rolls.
    public static final DeferredItem<Item> LEGENDARY=ExampleMod.ITEMS.registerItem("legendary_ancient_artifact",p->new ColoredArtifactItem(p,0xFFB52E),p->p.stacksTo(1).rarity(Rarity.EPIC));
    public static final DeferredItem<Item> UNIQUE=ExampleMod.ITEMS.registerItem("unique_ancient_artifact",p->new ColoredArtifactItem(p,0xFF668B),p->p.stacksTo(1).rarity(Rarity.EPIC));
    public static final DeferredItem<Item> PICKAXE_HEAD=part("ancient_pickaxe_head"),SWORD_BLADE=part("ancient_sword_blade"),AXE_HEAD=part("ancient_axe_head"),CORE=part("resonant_core");
    public static final DeferredItem<Item> FRAGMENTS=ExampleMod.ITEMS.registerSimpleItem("ancient_fragments");
    public static final DeferredItem<Item> PRECISION_DRILL=tool("precision_drill",768),PRECISION_BRUSH=tool("precision_brush",256),PRECISION_HAMMER=tool("precision_hammer",640),PRECISION_LENS=lens("precision_lens");
    public static final DeferredItem<Item> RESONANT_DRILL=tool("resonant_drill",1536),RESONANT_BRUSH=tool("resonant_brush",512),RESONANT_HAMMER=tool("resonant_hammer",1280),RESONANT_LENS=lens("resonant_lens");
    public static final DeferredItem<Item> SWORD=ExampleMod.ITEMS.registerSimpleItem("restored_ancient_sword",p->p.sword(ToolMaterial.DIAMOND,3,-2.4F).durability(1800).repairable(Items.IRON_INGOT).rarity(Rarity.RARE));
    public static final DeferredItem<Item> PICKAXE=ExampleMod.ITEMS.registerSimpleItem("restored_ancient_pickaxe",p->p.pickaxe(ToolMaterial.DIAMOND,1,-2.8F).durability(1800).repairable(Items.IRON_INGOT).rarity(Rarity.RARE));
    public static final DeferredItem<Item> AXE=ExampleMod.ITEMS.registerSimpleItem("restored_ancient_axe",p->p.axe(ToolMaterial.DIAMOND,5,-3F).durability(1800).repairable(Items.IRON_INGOT).rarity(Rarity.RARE));
    public static final DeferredItem<Item> SHOVEL=ExampleMod.ITEMS.registerSimpleItem("restored_ancient_shovel",p->p.shovel(ToolMaterial.DIAMOND,1.5F,-3F).durability(1800).repairable(Items.IRON_INGOT).rarity(Rarity.RARE));
    public static final DeferredItem<Item> HOE=ExampleMod.ITEMS.registerSimpleItem("restored_ancient_hoe",p->p.hoe(ToolMaterial.DIAMOND,-3,0).durability(1800).repairable(Items.IRON_INGOT).rarity(Rarity.RARE));
    public static final DeferredItem<net.minecraft.world.item.MaceItem> MACE=ExampleMod.ITEMS.registerItem("restored_ancient_mace",net.minecraft.world.item.MaceItem::new,p->p.durability(1800).attributes(net.minecraft.world.item.MaceItem.createAttributes()).component(DataComponents.TOOL,net.minecraft.world.item.MaceItem.createToolProperties()).component(DataComponents.WEAPON,new net.minecraft.world.item.component.Weapon(1)).enchantable(15).repairable(Items.IRON_INGOT).rarity(Rarity.RARE));
    // Same kinetic timing/range parameters as the native diamond spear.
    public static final DeferredItem<Item> SPEAR=ExampleMod.ITEMS.registerSimpleItem("restored_ancient_spear",p->p.spear(ToolMaterial.DIAMOND,1.05F,1.075F,.5F,3F,10F,6.5F,5.1F,10F,4.6F).durability(1800).repairable(Items.IRON_INGOT).rarity(Rarity.RARE));
    private static DeferredItem<Item> part(String id){return ExampleMod.ITEMS.registerSimpleItem(id,p->p.stacksTo(1).rarity(Rarity.UNCOMMON));}
    private static DeferredItem<Item> tool(String id,int life){return ExampleMod.ITEMS.registerSimpleItem(id,p->p.durability(life).enchantable(12).repairable(Items.IRON_INGOT));}
    private static DeferredItem<Item> lens(String id){return ExampleMod.ITEMS.registerSimpleItem(id,p->p.stacksTo(1));}
    public static void register(){} // Force registration before the item register is attached to the bus.
    public static List<Item> artifacts(){return List.of(ExampleMod.ANCIENT_ARTIFACT.get(),UNCOMMON.get(),ExampleMod.RARE_ANCIENT_ARTIFACT.get(),EPIC.get(),LEGENDARY.get(),UNIQUE.get());}
    public static List<Item> materials(){return List.of(PICKAXE_HEAD.get(),SWORD_BLADE.get(),AXE_HEAD.get(),CORE.get(),FRAGMENTS.get());}
    public static List<Item> tools(){return List.of(PRECISION_DRILL.get(),PRECISION_BRUSH.get(),PRECISION_HAMMER.get(),PRECISION_LENS.get(),RESONANT_DRILL.get(),RESONANT_BRUSH.get(),RESONANT_HAMMER.get(),RESONANT_LENS.get(),PICKAXE.get(),AXE.get(),SHOVEL.get(),HOE.get());}
    public static int role(ItemStack stack){
        if(stack.isEmpty())return 0;
        for(int i=1;i<=4;i++)if(stack.is(ArchaeologyTableMenu.tool(i))||stack.is(upgrade(i,1))||stack.is(upgrade(i,2)))return i;
        return 0;
    }
    public static Item upgrade(int role,int tier){return switch(role){case 1->tier==1?PRECISION_DRILL.get():RESONANT_DRILL.get();case 2->tier==1?PRECISION_BRUSH.get():RESONANT_BRUSH.get();case 3->tier==1?PRECISION_HAMMER.get():RESONANT_HAMMER.get();case 4->tier==1?PRECISION_LENS.get():RESONANT_LENS.get();default->Items.AIR;};}
    public static int tier(ItemStack stack){int role=role(stack);return role==0?0:stack.is(upgrade(role,2))?2:stack.is(upgrade(role,1))?1:0;}
    public static int cooldown(int role,int tier){return role==3?8-tier*2:role==1?Math.max(2,4-tier*2):2;}
    public static int rarity(ItemStack stack){return stack.is(UNIQUE.get())?5:stack.is(LEGENDARY.get())?4:stack.is(EPIC.get())?3:stack.is(ExampleMod.RARE_ANCIENT_ARTIFACT.get())?2:stack.is(UNCOMMON.get())?1:0;}
    public static ItemStack specimen(RandomSource random,boolean geode){
        int roll=random.nextInt(100),rarity=geode?(roll<80?2:3):(roll<70?0:roll<95?1:2);
        return specimen(random.nextLong(),rarity);
    }
    public static ItemStack specimen(long seed,int rarity){
        rarity=Math.clamp(rarity,0,5);
        var stack=new ItemStack(switch(rarity){case 4->LEGENDARY.get();case 5->UNIQUE.get();case 1->UNCOMMON.get();case 2->ExampleMod.RARE_ANCIENT_ARTIFACT.get();case 3->EPIC.get();default->ExampleMod.ANCIENT_ARTIFACT.get();});
        ArtifactProfile.roll(seed,rarity).write(stack);return stack;
    }
    public static ItemStack reward(ExcavationStudy study,ItemStack source){
        var result=reward(study);if(result.is(FRAGMENTS.get()))return result;
        ArtifactModifiers.seal(source,study);ArtifactModifiers.transfer(source,result);return result;
    }
    public static ItemStack reward(ExcavationStudy study){
        var profile=study.profile();boolean salvage=study.condition()<60||profile.find()==ArtifactProfile.Find.LEGACY||profile.find()==ArtifactProfile.Find.ALLOY;
        var stack=new ItemStack(salvage?FRAGMENTS.get():switch(profile.find()){case PICKAXE_HEAD->PICKAXE_HEAD.get();case SWORD_BLADE->SWORD_BLADE.get();case AXE_HEAD->AXE_HEAD.get();case RESONANT_CORE->CORE.get();case PICKAXE_LEFT,PICKAXE_BRIDGE,PICKAXE_RIGHT,TOOL_CONNECTOR,TOOL_HANDLE,SHOVEL_HEAD,HOE_HEAD,MACE_HEAD,SPEAR_TIP->ReconstructionContent.reward(profile.find());default->FRAGMENTS.get();});
        if(salvage){stack.setCount(1+profile.rarity()+study.condition()/40);return stack;}
        var tag=new CompoundTag();tag.putInt("Quality",study.condition());tag.putInt("Score",study.condition());tag.putInt("Rarity",profile.rarity());tag.putString("Matrix",profile.matrix().name());tag.putString("Find",profile.find().name());tag.putLong("Seed",profile.seed());tag.putInt("Strokes",study.strokes());
        var root=new CompoundTag();root.put("examplemod:recovered",tag);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
        // Fixed item name survives storage. Custom player names still take precedence.
        if(!salvage)stack.set(DataComponents.ITEM_NAME,Component.literal((study.condition()>=90?"Pristine ":study.condition()>=75?"Fine ":"Worn ")+stack.getHoverName().getString()));
        ArtifactQuality.showRecoveredScore(stack);
        ArtifactModifiers.seal(stack,study);
        return stack;
    }
}



