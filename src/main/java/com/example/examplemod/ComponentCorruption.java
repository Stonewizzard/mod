package com.example.examplemod;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** Initial corruption balance. Eligible outcomes are reweighted; locked rolls are never targets. */
public final class ComponentCorruption {
    public enum Outcome {
        STRONGER(18),WEAKER(18),HIGHER_GRADE(12),LOWER_GRADE(12),ADD(10),REMOVE(10),RESTORE(8),FRACTURE(8),UNCHANGED(4);
        public final int weight;Outcome(int weight){this.weight=weight;}
    }
    public static String apply(ItemStack stack,CompoundTag state,List<ArtifactModifiers.Roll> rolls,RandomSource random){
        var outcomes=new ArrayList<Outcome>();for(var outcome:Outcome.values())if(eligible(outcome,stack,rolls))outcomes.add(outcome);
        int choice=random.nextInt(outcomes.stream().mapToInt(o->o.weight).sum());Outcome selected=Outcome.UNCHANGED;
        for(var outcome:outcomes){choice-=outcome.weight;if(choice<0){selected=outcome;break;}}
        var targets=targets(selected,stack,rolls);int index=targets.isEmpty()?-1:targets.get(random.nextInt(targets.size()));
        String message;
        switch(selected){
            case STRONGER,WEAKER->{var old=rolls.get(index);var trait=PickaxeTrait.byId(old.id());double value=Math.min(trait.cap,old.value()*(selected==Outcome.STRONGER?1.25:.75));rolls.set(index,old.withValue(value));message=selected==Outcome.STRONGER?"Corruption strengthened a modifier":"Corruption weakened a modifier";}
            case HIGHER_GRADE,LOWER_GRADE->{var old=rolls.get(index);int step=selected==Outcome.HIGHER_GRADE?1:-1;var grade=ModifierScaling.values()[old.scaling().ordinal()+step];rolls.set(index,old.withScaling(grade));message=selected==Outcome.HIGHER_GRADE?"Corruption raised a scaling grade":"Corruption lowered a scaling grade";}
            case ADD->{
                int rarity=ComponentWork.recovered(stack).getIntOr("Rarity",0);
                var choices=AncientModifiers.eligible(ComponentWork.find(stack),rarity,rolls);
                var sampler=new java.util.Random(random.nextLong());
                var entry=ArtifactModifiers.weighted(choices,ArtifactModifiers.Definition::weight,sampler);
                var profile=new ArtifactProfile(random.nextLong(),rarity,ArtifactProfile.Matrix.SHALE,ComponentWork.find(stack));
                rolls.add(ArtifactModifiers.sample(entry,profile,Math.max(0,ArtifactQuality.score(stack))/100.0,sampler));
                message="Corruption added a modifier";
            }
            case REMOVE->{rolls.remove(index);message="Corruption removed an unlocked modifier";}
            case RESTORE->{state.putInt("Quality",Math.min(ComponentWork.MAX_QUALITY,ArtifactQuality.score(stack)+10));message="Corruption improved quality";}
            case FRACTURE->{state.putInt("Quality",Math.max(0,ArtifactQuality.score(stack)-10));message="Corruption damaged quality";}
            case UNCHANGED->message="Corrupted without another change";
            default->throw new IllegalStateException("Unhandled corruption");
        }
        state.putString("CorruptionOutcome",selected.name());return message;
    }
    static boolean eligible(Outcome outcome,ItemStack stack,List<ArtifactModifiers.Roll> rolls){return switch(outcome){
        case RESTORE->ArtifactQuality.score(stack)<ComponentWork.MAX_QUALITY;
        case FRACTURE->ArtifactQuality.score(stack)>0;
        case ADD->rolls.size()<8&&!AncientModifiers.eligible(ComponentWork.find(stack),ComponentWork.recovered(stack).getIntOr("Rarity",0),rolls).isEmpty();
        case UNCHANGED->true;
        default->!targets(outcome,stack,rolls).isEmpty();
    };}
    private static List<Integer> targets(Outcome outcome,ItemStack stack,List<ArtifactModifiers.Roll> rolls){
        var result=new ArrayList<Integer>();String locked=ComponentWork.locked(stack);
        for(int i=0;i<rolls.size();i++){
            var roll=rolls.get(i);if(roll.id().equals(locked))continue;var trait=PickaxeTrait.byId(roll.id());
            boolean eligible=switch(outcome){
                case REMOVE->true;
                case STRONGER->trait!=null&&roll.value()>0&&roll.value()<trait.cap;
                case WEAKER->trait!=null&&roll.value()>0;
                case HIGHER_GRADE->trait!=null&&roll.scaling()!=ModifierScaling.S;
                case LOWER_GRADE->trait!=null&&roll.scaling()!=ModifierScaling.F;
                default->false;
            };if(eligible)result.add(i);
        }
        return result;
    }
    private ComponentCorruption(){}
}
