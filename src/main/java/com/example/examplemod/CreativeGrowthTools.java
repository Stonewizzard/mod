package com.example.examplemod;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Fixed, clearly named comparisons; ordinary Creative test parts still roll randomly on acquisition. */
public final class CreativeGrowthTools {
    public static List<ItemStack> variants(){
        var result=new ArrayList<ItemStack>();
        for(var grade:ModifierScaling.values()){
            var tool=create(grade,100,123456);
            PickaxeProgression.write(tool,new PickaxeProgression.State(9,PickaxeProgression.xpRequired(9)-1,0));
            tool.set(DataComponents.ITEM_NAME,Component.literal("Scaling Test Pickaxe ["+grade+"]"));PickaxeModifiers.updateLore(tool);result.add(tool);
        }
        return List.copyOf(result);
    }
    static ItemStack create(ModifierScaling grade,int score,long seed){
        var pieces=new ArrayList<ItemStack>();var traits=new PickaxeTrait[]{PickaxeTrait.SPEED,PickaxeTrait.DURABILITY,PickaxeTrait.SILK,PickaxeTrait.REACH};
        for(int i=0;i<CreativePickaxeParts.FINDS.size();i++){
            var find=CreativePickaxeParts.FINDS.get(i);var piece=new ItemStack(ReconstructionContent.reward(find));var root=new CompoundTag();var data=new CompoundTag();data.putInt("Quality",score);data.putInt("Rarity",5);root.put(ArtifactQuality.RECOVERED,data);
            var profile=new ArtifactProfile(seed+i,5,ArtifactProfile.Matrix.SHALE,find);var record=ArtifactModifiers.record(profile,score,new ArtifactModifiers.Pool(0,0,List.of()));
            var rolls=i<4?List.of(new ArtifactModifiers.Roll(traits[i].id,i==3?.4:.2,score/100.0,grade)):List.<ArtifactModifiers.Roll>of();
            root.put(ArtifactModifiers.KEY,ArtifactModifiers.writeRolls(record,rolls));piece.set(DataComponents.CUSTOM_DATA,CustomData.of(root));pieces.add(piece);
        }
        var tool=ArtifactQuality.assemble(pieces);var root=tool.get(DataComponents.CUSTOM_DATA).copyTag();var progression=root.getCompoundOrEmpty(PickaxeProgression.KEY);progression.putLong("GrowthSeed",seed);root.put(PickaxeProgression.KEY,progression);tool.set(DataComponents.CUSTOM_DATA,CustomData.of(root));return tool;
    }
    private CreativeGrowthTools(){}
}
