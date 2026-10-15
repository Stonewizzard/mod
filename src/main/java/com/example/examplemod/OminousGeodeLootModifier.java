package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/** Has a chance to add a geode to the ominous vault reward without replacing vanilla rewards. */
public class OminousGeodeLootModifier extends LootModifier {
    // One in five ominous vault rewards, in addition to the normal vault loot.
    public static final float ANCIENT_GEODE_CHANCE = 0.2F;
    public static final MapCodec<OminousGeodeLootModifier> CODEC = RecordCodecBuilder.mapCodec(
            instance -> codecStart(instance).apply(instance, OminousGeodeLootModifier::new));

    public OminousGeodeLootModifier(Optional<Holder<LootItemCondition>> condition, int priority) {
        super(condition, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() < ANCIENT_GEODE_CHANCE) {
            generatedLoot.add(new ItemStack(ExampleMod.ANCIENT_GEODE_ITEM.get()));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
