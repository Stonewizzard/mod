package com.example.examplemod;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.event.EventHooks;

/** Consumes exposed debris at the actual digging spot and substitutes its rewards. */
public class SnifferDebrisLootModifier extends LootModifier {
    // Matches the Sniffer Egg's 1-in-15 weight in warm-ocean-ruin archaeology loot.
    public static final float CRUNCHER_EGG_CHANCE = 1.0F / 15.0F;
    public static final MapCodec<SnifferDebrisLootModifier> CODEC = RecordCodecBuilder.mapCodec(
            instance -> codecStart(instance).apply(instance, SnifferDebrisLootModifier::new));

    public SnifferDebrisLootModifier(Optional<Holder<LootItemCondition>> condition, int priority) {
        super(condition, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!(context.getOptional(LootContextParams.THIS_ENTITY) instanceof Sniffer sniffer)
                || sniffer.isBaby()) {
            return generatedLoot;
        }
        var level = context.getLevel();
        // Sniffer digging loot uses the feet as ORIGIN in 26.3. Its digging snout
        // is 2.25 blocks forward, matching the vanilla sniffer's head position.
        Vec3 snout = sniffer.position().add(sniffer.getForward().scale(2.25));
        BlockPos target = BlockPos.containing(snout.x(), sniffer.getY() + 0.2, snout.z()).below();
        if (!level.getBlockState(target).is(Blocks.ANCIENT_DEBRIS)
                || !level.getBlockState(target.above()).isAir()) {
            return generatedLoot;
        }
        // Do not turn ancient debris into ordinary seeds if mob griefing is disabled.
        generatedLoot.clear();
        if (!EventHooks.canEntityGrief(level, sniffer) || !level.destroyBlock(target, false, sniffer)) {
            return generatedLoot;
        }
        generatedLoot.add(new ItemStack(Items.NETHERITE_SCRAP));
        generatedLoot.add(ArchaeologyContent.specimen(context.getRandom(),false));
        if (context.getRandom().nextFloat() < CRUNCHER_EGG_CHANCE) {
            generatedLoot.add(new ItemStack(ExampleMod.CRUNCHER_EGG_ITEM.get()));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
