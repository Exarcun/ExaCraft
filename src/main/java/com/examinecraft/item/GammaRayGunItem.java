package com.examinecraft.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Hold right-click for a continuous high-damage energy beam. */
public class GammaRayGunItem extends Item {
	private static final double RANGE = 24.0;
	private static final int DAMAGE_INTERVAL_TICKS = 2;
	private static final float DAMAGE_PER_HIT = 3.0F;

	public GammaRayGunItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return 72000;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.NONE;
	}

	@Override
	public void onUseTick(Level level, LivingEntity user, ItemStack stack, int ticksRemaining) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		HitResult hit = ProjectileUtil.getHitResultOnViewVector(user,
				entity -> entity instanceof LivingEntity && entity != user && entity.isPickable(), RANGE);
		Vec3 from = user.getEyePosition();
		Vec3 end = hit.getType() == HitResult.Type.MISS
				? from.add(user.getLookAngle().scale(RANGE))
				: hit.getLocation();

		Vec3 direction = end.subtract(from);
		double length = direction.length();
		Vec3 step = direction.normalize();
		for (double d = 1.0; d < length; d += 1.0) {
			Vec3 point = from.add(step.scale(d));
			serverLevel.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
		}

		if (ticksRemaining % DAMAGE_INTERVAL_TICKS == 0) {
			if (hit instanceof EntityHitResult entityHit) {
				Entity target = entityHit.getEntity();
				if (target instanceof LivingEntity living) {
					living.hurtServer(serverLevel, user.damageSources().indirectMagic(user, user), DAMAGE_PER_HIT);
				}
			}
			serverLevel.playSound(null, user.getX(), user.getY(), user.getZ(),
					SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.6F, 2.0F);
		}
	}
}
