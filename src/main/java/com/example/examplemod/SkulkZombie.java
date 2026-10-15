package com.example.examplemod;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;

/** A zombie host with the vanilla zombie behavior and persistent infection. */
public class SkulkZombie extends Zombie {
    public SkulkZombie(EntityType<? extends SkulkZombie> type, Level level) {
        super(type, level);
        xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.23 * 1.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0 * 1.5);
    }
}
