package com.example.examplemod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;

public final class SkulkInfection {
    private SkulkInfection() {}

    /** Keep the exact host, its UUID and all native variant/ownership data. */
    public static Mob infect(SkulkParasite parasite, Mob host) {
        if (!(host.level() instanceof ServerLevel level) || parasite.level() != level
                || !parasite.isAlive() || !SkulkStatus.canInfect(host)) return null;
        SkulkStatus.infect(host);
        level.sendParticles(ParticleTypes.SCULK_SOUL, host.getX(), host.getY() + 0.5,
                host.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
        level.playSound(null, host.blockPosition(), SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.HOSTILE, 0.8F, 0.8F);
        parasite.discard();
        return host;
    }
}


