package com.examinecraft.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import com.examinecraft.item.ModWeapons;

/** Crossbow bolt that teleports its shooter to wherever it lands. */
public class BlinkBoltEntity extends ThrowableItemProjectile {
	private static final float DAMAGE = 4.0F;
	private static final int MAX_LIFETIME_TICKS = 80;

	public BlinkBoltEntity(EntityType<? extends BlinkBoltEntity> type, Level level) {
		super(type, level);
	}

	public BlinkBoltEntity(Level level, LivingEntity owner, net.minecraft.world.item.ItemStack stack) {
		super(ModEntityTypes.BLINK_BOLT, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return ModWeapons.BLINK_BOLT;
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide() && this.tickCount > MAX_LIFETIME_TICKS) {
			this.discard();
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		if (this.level() instanceof ServerLevel serverLevel) {
			hitResult.getEntity().hurtServer(serverLevel, this.damageSources().thrown(this, this.getOwner()), DAMAGE);
			this.teleportOwner(serverLevel, hitResult.getEntity().position());
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult hitResult) {
		super.onHitBlock(hitResult);
		if (this.level() instanceof ServerLevel serverLevel) {
			this.teleportOwner(serverLevel, Vec3.atBottomCenterOf(hitResult.getBlockPos().relative(hitResult.getDirection())));
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (!this.level().isClientSide()) {
			this.discard();
		}
	}

	private void teleportOwner(ServerLevel serverLevel, Vec3 target) {
		if (!(this.getOwner() instanceof LivingEntity owner) || owner.level() != serverLevel) {
			return;
		}
		serverLevel.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
				SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
		serverLevel.sendParticles(ParticleTypes.PORTAL, owner.getX(), owner.getY() + 1.0, owner.getZ(),
				20, 0.3, 0.5, 0.3, 0.1);
		owner.teleportTo(target.x, target.y, target.z);
		owner.resetFallDistance();
		serverLevel.playSound(null, target.x, target.y, target.z,
				SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.2F);
		serverLevel.sendParticles(ParticleTypes.PORTAL, target.x, target.y + 1.0, target.z,
				20, 0.3, 0.5, 0.3, 0.1);
	}
}
