package com.example.examplemod;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

/** All part mutations pass through this policy. Completed tools and sealed artifacts never qualify. */
public final class ComponentWork {
    public static final String KEY="examplemod:component_work";
    public static final int MAX_QUALITY=120,RESTORE_STEP=10;
    public static ArtifactProfile.Find find(ItemStack stack){
        for(var find:ArtifactProfile.Find.values()){
            switch(find){
                case PICKAXE_LEFT,PICKAXE_BRIDGE,PICKAXE_RIGHT,TOOL_CONNECTOR,TOOL_HANDLE,SHOVEL_HEAD,HOE_HEAD,MACE_HEAD,SPEAR_TIP->{if(stack.is(ReconstructionContent.reward(find)))return find;}
                case PICKAXE_HEAD->{if(stack.is(ArchaeologyContent.PICKAXE_HEAD.get()))return find;}
                case SWORD_BLADE->{if(stack.is(ArchaeologyContent.SWORD_BLADE.get()))return find;}
                case AXE_HEAD->{if(stack.is(ArchaeologyContent.AXE_HEAD.get()))return find;}
                case RESONANT_CORE->{if(stack.is(ArchaeologyContent.CORE.get()))return find;}
                default->{}
            }
        }
        return null;
    }
    public static boolean component(ItemStack stack){return !stack.isEmpty()&&find(stack)!=null&&recovered(stack).contains("Quality");}
    public static CompoundTag recovered(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(ArtifactQuality.RECOVERED);}
    public static CompoundTag state(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(KEY);}
    public static boolean corrupted(ItemStack stack){return state(stack).getBooleanOr("Corrupted",false);}
    public static String locked(ItemStack stack){return state(stack).getStringOr("Locked","");}
    public static boolean mutable(ItemStack stack){return component(stack)&&!corrupted(stack);}
    public static List<ArtifactModifiers.Roll> offers(ItemStack stack){return AncientModifiers.offers(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompoundOrEmpty(ArtifactModifiers.KEY));}
    public static boolean can(ComponentStationKind kind,ItemStack stack,int choice){
        if(!mutable(stack))return false;var rolls=ArtifactModifiers.read(stack);String lock=locked(stack);
        return switch(kind){
            case UNVEILER->choice>=0&&choice<offers(stack).size()&&rolls.stream().anyMatch(AncientModifiers::ancient)&&AncientModifiers.eligible(find(stack),recovered(stack).getIntOr("Rarity",0),rolls).stream().anyMatch(d->d.id().equals(offers(stack).get(choice).id()));
            case LOCKER->lock.isEmpty()&&choice>=0&&choice<rolls.size()&&!AncientModifiers.ancient(rolls.get(choice))&&PickaxeTrait.byId(rolls.get(choice).id())!=null;
            case ANULLER->rolls.stream().anyMatch(r->!r.id().equals(lock));
            case SCALER->rolls.stream().anyMatch(r->!r.id().equals(lock)&&PickaxeTrait.byId(r.id())!=null);
            case RESTORER->ArtifactQuality.score(stack)<MAX_QUALITY;
            case CORRUPTOR->true;
        };
    }
    public static boolean apply(ComponentStationKind kind,ItemStack stack,int choice,RandomSource random){
        if(!can(kind,stack,choice))return false;
        var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var state=root.getCompoundOrEmpty(KEY).copy();var record=root.getCompoundOrEmpty(ArtifactModifiers.KEY).copy();
        var rolls=new ArrayList<>(ArtifactModifiers.read(record));String lock=locked(stack),message;
        switch(kind){
            case UNVEILER->{var selected=offers(stack).get(choice);for(int i=0;i<rolls.size();i++)if(AncientModifiers.ancient(rolls.get(i))){rolls.set(i,new ArtifactModifiers.Roll(selected.id(),selected.value(),ArtifactQuality.score(stack)/100.0,selected.scaling()));break;}record.remove(AncientModifiers.OFFERS);message="Unveiled "+PickaxeTooltips.label(PickaxeTrait.byId(selected.id()));}
            case LOCKER->{state.putString("Locked",rolls.get(choice).id());message="Modifier locked permanently";}
            case ANULLER->{var candidates=new ArrayList<Integer>();for(int i=0;i<rolls.size();i++)if(!rolls.get(i).id().equals(lock))candidates.add(i);rolls.remove((int)candidates.get(random.nextInt(candidates.size())));message="One unlocked modifier annulled";}
            case SCALER->{int rarity=recovered(stack).getIntOr("Rarity",0);var find=find(stack);for(int i=0;i<rolls.size();i++){var old=rolls.get(i);if(!old.id().equals(lock)&&PickaxeTrait.byId(old.id())!=null){var profile=new ArtifactProfile(random.nextLong(),rarity,ArtifactProfile.Matrix.SHALE,find);rolls.set(i,new ArtifactModifiers.Roll(old.id(),old.value(),old.qualityScale(),ModifierScaling.roll(profile,old.id())));}}message="Unlocked scaling grades rerolled";}
            case RESTORER->{int quality=Math.min(MAX_QUALITY,ArtifactQuality.score(stack)+RESTORE_STEP);state.putInt("Quality",quality);message="Quality restored to "+quality+"%";}
            case CORRUPTOR->{message=ComponentCorruption.apply(stack,state,rolls,random);state.putBoolean("Corrupted",true);}
            default->throw new IllegalStateException("Unknown station");
        }
        if(rolls.stream().noneMatch(AncientModifiers::ancient))record.remove(AncientModifiers.OFFERS);
        if(!record.isEmpty()||!rolls.isEmpty()){record.putInt("Version",1);root.put(ArtifactModifiers.KEY,ArtifactModifiers.writeRolls(record,rolls));}
        state.putInt("Version",1);state.putInt("Actions",state.getIntOr("Actions",0)+1);state.putString("LastAction",message);root.put(KEY,state);
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));stack.set(DataComponents.LORE,new ItemLore(PickaxeTooltips.piece(stack)));PickaxeTooltips.markCurrent(stack);return true;
    }
    private ComponentWork(){}
}
