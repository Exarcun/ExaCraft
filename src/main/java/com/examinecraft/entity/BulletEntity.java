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

public class BulletEntity extends ThrowableItemProjectile {
	private static final float DAMAGE = 4.0F;

	public BulletEntity(EntityType<? extends BulletEntity> type, Level level) {
		super(type, level);
	}

	public BulletEntity(Level level, LivingEntity owner, net.minecraft.world.item.ItemStack stack) {
		super(ModEntityTypes.BULLET, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return ModWeapons.BULLET;
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 3) {
			for (int i = 0; i < 4; i++) {
				this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
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
