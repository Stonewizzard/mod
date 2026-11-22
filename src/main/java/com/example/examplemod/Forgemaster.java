package com.example.examplemod;

import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** First encounter: a persistent fireproof melee guardian with a visible, avoidable slam. */
public final class Forgemaster extends IronGolem {
  private final ServerBossEvent bar =
      new ServerBossEvent(
          java.util.UUID.randomUUID(),
          Component.literal("The Forgemaster"),
          BossEvent.BossBarColor.YELLOW,
          BossEvent.BossBarOverlay.PROGRESS);

  public Forgemaster(EntityType<? extends IronGolem> type, Level l) {
    super(type, l);
    setPersistenceRequired();
  }

  @Override
  public void aiStep() {
    super.aiStep();
    bar.setProgress(getHealth() / getMaxHealth());
    if (level() instanceof ServerLevel server) {
      if (getTarget() == null) {
        var p = server.getNearestPlayer(this, 32);
        if (p != null && !p.isCreative() && !p.isSpectator()) setTarget(p);
      }
      int phase = tickCount % 100;
      if (phase >= 80)
        server.sendParticles(ParticleTypes.FLAME, getX(), getY() + .1, getZ(), 12, 2, .1, 2, 0);
      if (phase == 99) {
        for (var victim : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(3)))
          if (!victim.isCreative() && !victim.isSpectator() && victim.onGround())
            victim.hurtServer(server, damageSources().mobAttack(this), 10);
        server.playSound(
            null, blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1, .65F);
      }
    }
  }

  @Override
  public void startSeenByPlayer(ServerPlayer p) {
    super.startSeenByPlayer(p);
    bar.addPlayer(p);
  }

  @Override
  public void stopSeenByPlayer(ServerPlayer p) {
    super.stopSeenByPlayer(p);
    bar.removePlayer(p);
  }

  public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder attributes() {
    return IronGolem.createAttributes()
        .add(Attributes.MAX_HEALTH, 350)
        .add(Attributes.ATTACK_DAMAGE, 14)
        .add(Attributes.MOVEMENT_SPEED, .25)
        .add(Attributes.ARMOR, 12);
  }
}
