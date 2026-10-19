package com.example.examplemod;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Assignment is exclusively an Influencer action. An assigned influence is permanent. */
public final class ComponentInfluences {
    public static final String FIELD="Influence";
    public static ModifierInfluence current(ItemStack stack) {
        var influence=ModifierInfluence.byId(ComponentWork.state(stack).getStringOr(FIELD,""));
        return influence==null ? ModifierInfluence.NONE : influence;
    }
    public static boolean known(ItemStack stack) {
        return ModifierInfluence.byId(ComponentWork.state(stack).getStringOr(FIELD,""))!=null;
    }
    public static boolean can(ItemStack stack,ModifierInfluence target) {
        if(target==null || target==ModifierInfluence.NONE || !ComponentWork.mutable(stack) || !known(stack)
                || stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getBooleanOr(CreativePickaxeParts.PENDING,false))return false;
        var current=current(stack);
        if(current!=ModifierInfluence.NONE && current!=target)return false;
        var find=ComponentWork.find(stack);
        if(!ArtifactModifierCatalog.pickaxePart(find))return false;
        int rarity=ComponentWork.recovered(stack).getIntOr("Rarity",0);
        var pool=ArtifactModifierCatalog.pool(find,rarity,target);
        if(pool.entries().isEmpty())return false;
        String locked=ComponentWork.locked(stack);
        if(locked.isEmpty())return true;
        var fixed=ArtifactModifiers.read(stack).stream().filter(r->r.id().equals(locked)).toList();
        // Preserve unknown locks and reject an initial assignment that would conflict with one.
        return fixed.size()==1 && pool.entries().stream().anyMatch(d->d.id().equals(locked));
    }
    static CompoundTag reroll(ItemStack stack,ModifierInfluence target,RandomSource random) {
        if(!can(stack,target))throw new IllegalArgumentException("Invalid influence assignment");
        var recovered=ComponentWork.recovered(stack);
        int rarity=recovered.getIntOr("Rarity",0);
        var profile=new ArtifactProfile(random.nextLong(),rarity,ArtifactProfile.Matrix.SHALE,ComponentWork.find(stack));
        var fixed=ArtifactModifiers.read(stack).stream().filter(r->r.id().equals(ComponentWork.locked(stack))).toList();
        var pool=ArtifactModifierCatalog.pool(profile.find(),rarity,target);
        var record=ArtifactModifiers.record(profile,Math.clamp(recovered.getIntOr("Quality",0),0,100),pool,target,fixed);
        var rolls=ArtifactModifiers.read(record).stream()
            .map(r->fixed.stream().anyMatch(f->f.id().equals(r.id()))?r:r.withQuality(ArtifactQuality.score(stack)/100.0)).toList();
        return ArtifactModifiers.writeRolls(record,rolls);
    }
    public static String description(ItemStack stack) {
        if(!known(stack))return "Unknown influence";
        return current(stack)==ModifierInfluence.NONE?"Uninfluenced":current(stack).label+" influence";
    }
    private ComponentInfluences() {}
}
