package com.examinecraft.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import com.examinecraft.item.ModWeapons;

public class NinjaStarEntity extends ThrowableItemProjectile {
	private static final float DAMAGE = 7.0F;

	public NinjaStarEntity(EntityType<? extends NinjaStarEntity> type, Level level) {
		super(type, level);
	}

	public NinjaStarEntity(Level level, LivingEntity owner, net.minecraft.world.item.ItemStack stack) {
		super(ModEntityTypes.NINJA_STAR, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return ModWeapons.NINJA_STAR;
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 3) {
			for (int i = 0; i < 4; i++) {
				this.level().addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
			}
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
			hitResult.getEntity().hurtServer(serverLevel, this.damageSources().thrown(this, this.getOwner()), DAMAGE);
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (!this.level().isClientSide()) {
			this.level().broadcastEntityEvent(this, (byte) 3);
			this.discard();
		}
	}
}
