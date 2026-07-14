package com.examinecraft.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

import com.examinecraft.item.DrugPotionItem;
import com.examinecraft.item.ModItems;

/** Splash-potion-style projectile that applies the DrugPotionItem's effects in a radius. */
public class ThrownDrugPotion extends ThrowableItemProjectile {
	private static final double SPLASH_RADIUS = 4.0;

	public ThrownDrugPotion(EntityType<? extends ThrownDrugPotion> type, Level level) {
		super(type, level);
	}

	public ThrownDrugPotion(Level level, LivingEntity owner, net.minecraft.world.item.ItemStack stack) {
		super(ModEntityTypes.THROWN_DRUG_POTION, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.PERC_POTION;
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 3) {
			for (int i = 0; i < 8; i++) {
				this.level().addParticle(net.minecraft.core.particles.ParticleTypes.WITCH,
						this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
			}
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (this.level() instanceof ServerLevel serverLevel) {
			if (this.getItem().getItem() instanceof DrugPotionItem potion) {
				AABB area = this.getBoundingBox().inflate(SPLASH_RADIUS, SPLASH_RADIUS / 2.0, SPLASH_RADIUS);
				for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
					potion.applyTo(target);
				}
			}
			serverLevel.broadcastEntityEvent(this, (byte) 3);
			this.discard();
		}
	}
}
