package com.examinecraft.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Invisible countdown marker for the orbital laser. Charges for 5 seconds
 * with a rising red warning column, then drops the beam.
 */
public class OrbitalStrikeEntity extends Entity {
	public static final int FUSE_TICKS = 5 * 20;
	private static final float BLAST_POWER = 8.0F;
	private static final int CHARGE_COLUMN_HEIGHT = 24;
	private static final int BEAM_HEIGHT = 80;
	private static final DustParticleOptions WARNING_RED = new DustParticleOptions(0xFF2020, 1.5F);

	private int fuse = FUSE_TICKS;

	public OrbitalStrikeEntity(EntityType<?> type, Level level) {
		super(type, level);
		this.noPhysics = true;
	}

	@Override
	public void tick() {
		super.tick();
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		this.fuse--;
		if (this.fuse > 0) {
			for (int i = 0; i < CHARGE_COLUMN_HEIGHT; i += 2) {
				serverLevel.sendParticles(WARNING_RED, this.getX(), this.getY() + i, this.getZ(), 2, 0.3, 0.5, 0.3, 0.0);
			}
			serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + CHARGE_COLUMN_HEIGHT, this.getZ(),
					1, 0.1, 0.1, 0.1, 0.0);
			if (this.fuse % 20 == 0) {
				float pitch = 0.8F + (FUSE_TICKS - this.fuse) * 0.012F;
				serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
						SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 2.0F, pitch);
			}
			return;
		}
		for (int i = 0; i < BEAM_HEIGHT; i++) {
			serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + i, this.getZ(),
					3, 0.2, 0.4, 0.2, 0.01);
		}
		serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
		serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
				SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 4.0F, 0.6F);
		serverLevel.explode(this, this.getX(), this.getY(), this.getZ(), BLAST_POWER, true, Level.ExplosionInteraction.TNT);
		this.discard();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		this.fuse = input.getIntOr("Fuse", FUSE_TICKS);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putInt("Fuse", this.fuse);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		return false;
	}

	@Override
	public boolean isPickable() {
		return false;
	}
}
