package com.example.examplemod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.neoforged.neoforge.event.EventHooks;

public final class SkulkInfection {
    private SkulkInfection() {}

    /** Successful conversion consumes exactly one parasite, without killing/looting the host. */
    public static Mob infect(SkulkParasite parasite, Mob host) {
        if (!(host.level() instanceof ServerLevel level) || parasite.level() != level
                || !parasite.isAlive() || !SkulkParasite.isSupportedHost(host)) return null;
        float health = host.getHealth() / host.getMaxHealth();
        var destination = host.getType() == EntityTypes.ZOMBIE
                ? ExampleMod.SKULK_ZOMBIE.get() : ExampleMod.SKULK_COW.get();
        if (!EventHooks.canLivingConvert(host, destination, timer -> {})) return null;
        Mob converted;
        if (host instanceof Zombie zombie) {
            boolean baby = zombie.isBaby();
            converted = zombie.convertTo(ExampleMod.SKULK_ZOMBIE.get(), ConversionParams.single(zombie, true, true),
                    infected -> { infected.setBaby(baby); infected.setHealth(health * infected.getMaxHealth()); });
        } else if (host instanceof Cow cow) {
            int age = cow.getAge();
            converted = cow.convertTo(ExampleMod.SKULK_COW.get(), ConversionParams.single(cow, true, true),
                    infected -> { infected.setAge(age); infected.setHealth(health * infected.getMaxHealth()); });
        } else {
            return null;
        }
        if (converted != null) {
            EventHooks.onLivingConvert(host, converted);
            level.sendParticles(ParticleTypes.SCULK_SOUL, converted.getX(), converted.getY() + 0.5,
                    converted.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
            level.playSound(null, converted.blockPosition(), SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.HOSTILE, 0.8F, 0.8F);
            parasite.discard();
        }
        return converted;
    }
}
