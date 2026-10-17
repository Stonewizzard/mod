package com.example.examplemod;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

/** Creative catalog entries are unrolled templates; each server-side acquisition gets a fresh seed. */
public final class CreativePickaxeParts {
    public static final String PENDING="examplemod:creative_pickaxe_roll";
    public static final List<ArtifactProfile.Find> FINDS=List.of(ArtifactProfile.Find.PICKAXE_LEFT,ArtifactProfile.Find.PICKAXE_BRIDGE,ArtifactProfile.Find.PICKAXE_RIGHT,ArtifactProfile.Find.TOOL_CONNECTOR,ArtifactProfile.Find.TOOL_HANDLE);
    private static final List<String> LABELS=List.of("Left Tip","Head Bridge","Right Tip","Connector","Handle");
    public static List<ItemStack> variants(){
        var result=new ArrayList<ItemStack>();for(var find:FINDS)for(int score:new int[]{0,50,100})result.add(template(find,score));return result;
    }
    public static ItemStack template(ArtifactProfile.Find find,int score){
        if(!FINDS.contains(find))throw new IllegalArgumentException("Not a pickaxe test part");score=Math.clamp(score,0,100);
        var stack=new ItemStack(ReconstructionContent.reward(find));var root=new CompoundTag();var record=new CompoundTag();record.putInt("Quality",score);record.putInt("Score",score);record.putInt("Rarity",5);record.putString("Find",find.name());record.putBoolean("CreativeTest",true);root.put(ArtifactQuality.RECOVERED,record);root.putBoolean(PENDING,true);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
        stack.set(DataComponents.ITEM_NAME,Component.literal("Random Test "+LABELS.get(FINDS.indexOf(find))+" ["+score+"/100]"));
        stack.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("Unique-tier rolls on entering inventory"),Component.literal("Excavation score: "+score+"/100"))));return stack;
    }
    public static boolean materialize(ItemStack stack,RandomSource random){
        var custom=stack.get(DataComponents.CUSTOM_DATA);if(custom==null)return false;var root=custom.copyTag();if(!root.getBooleanOr(PENDING,false))return false;
        ArtifactProfile.Find find=null;for(var candidate:FINDS)if(stack.is(ReconstructionContent.reward(candidate))){find=candidate;break;}if(find==null)return false;
        var record=root.getCompoundOrEmpty(ArtifactQuality.RECOVERED);int score=Math.clamp(record.getIntOr("Quality",0),0,100);long seed=random.nextLong();
        var profile=new ArtifactProfile(seed,5,ArtifactProfile.Matrix.OBSIDIAN,find);record.putLong("Seed",seed);record.putInt("Rarity",5);record.putString("Find",find.name());root.put(ArtifactQuality.RECOVERED,record);root.remove(PENDING);root.put(ArtifactModifiers.KEY,ArtifactModifiers.record(profile,score,ArtifactModifierCatalog.pool(find,5)));
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));stack.set(DataComponents.LORE,new ItemLore(PickaxeModifiers.pieceLore(stack)));return true;
    }
    private CreativePickaxeParts(){}
}
