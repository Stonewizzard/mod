package com.example.examplemod;

import java.util.List;
import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

/** Quality is retained data for future modifiers, never a current combat/stat multiplier. */
public final class ArtifactQuality {
    public static final String RECOVERED="examplemod:recovered",ASSEMBLY="examplemod:reconstruction";
    public static int score(ItemStack stack){
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(RECOVERED);
        return tag.contains("Quality")?Math.clamp(tag.getIntOr("Quality",0),0,100):-1;
    }
    public static void showRecoveredScore(ItemStack stack){
        int score=score(stack);if(score<0)return;
        stack.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("Excavation score: "+score+"/100").withStyle(net.minecraft.ChatFormatting.GRAY))));
    }
    public static double average(List<ItemStack> pieces){return pieces.isEmpty()?0:pieces.stream().mapToInt(s->Math.max(0,score(s))).sum()/(double)pieces.size();}
    public static double assemblyScore(ItemStack stack){
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(ASSEMBLY);
        return tag.getIntOr("Version",0)==1?Math.clamp(tag.getDoubleOr("Score",0),0,100):-1;
    }
    public static String formatted(double score){return String.format(Locale.ROOT,"%.1f",score);}
    public static ItemStack assemble(List<ItemStack> pieces){
        if(pieces.size()!=5)throw new IllegalArgumentException("A pickaxe has five pieces");
        var result=new ItemStack(ArchaeologyContent.PICKAXE.get());var record=new CompoundTag();var parts=new CompoundTag();int total=0;
        for(int i=0;i<5;i++){
            var stack=pieces.get(i);var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(RECOVERED).copy();
            int score=score(stack);total+=Math.max(0,score);data.putInt("Score",Math.max(0,score));data.putBoolean("Scored",score>=0);
            data.putString("Item",net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            parts.put(ReconstructionPattern.PICKAXE.parts.get(i),data);
        }
        record.putInt("Version",1);record.putInt("PartCount",5);record.putString("Pattern","pickaxe");record.putInt("ScoreTotal",total);record.putDouble("Score",total/5.0);record.put("Parts",parts);
        var root=new CompoundTag();root.put(ASSEMBLY,record);result.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
        result.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("Assembly score: "+formatted(total/5.0)+"/100").withStyle(net.minecraft.ChatFormatting.GRAY))));
        return result;
    }
}
