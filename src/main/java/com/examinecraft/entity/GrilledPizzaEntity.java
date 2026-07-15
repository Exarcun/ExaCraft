package com.examinecraft.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import com.examinecraft.item.ModWeapons;

public class GrilledPizzaEntity extends ThrowableItemProjectile {
	private static final float DAMAGE = 6.0F;
	private static final int REGEN_TICKS = 3 * 20;
	private static final DustParticleOptions TOMATO_SPLAT = new DustParticleOptions(0xC63D2F, 1.2F);

	public GrilledPizzaEntity(EntityType<? extends GrilledPizzaEntity> type, Level level) {
		super(type, level);
	}

	public GrilledPizzaEntity(Level level, LivingEntity owner, net.minecraft.world.item.ItemStack stack) {
		super(ModEntityTypes.GRILLED_PIZZA, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return ModWeapons.GRILLED_PIZZA;
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 3) {
			for (int i = 0; i < 8; i++) {
				this.level().addParticle(TOMATO_SPLAT, this.getX(), this.getY(), this.getZ(),
						this.random.nextGaussian() * 0.1, this.random.nextGaussian() * 0.1, this.random.nextGaussian() * 0.1);
			}
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
			hitResult.getEntity().hurtServer(serverLevel, this.damageSources().thrown(this, this.getOwner()), DAMAGE);
			if (hitResult.getEntity() instanceof Player player) {
				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_TICKS, 0));
			}
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (!this.level().isClientSide()) {
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
					SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.8F, 1.0F);
			this.level().broadcastEntityEvent(this, (byte) 3);
			this.discard();
		}
	}
}
