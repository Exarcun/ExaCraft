package com.examinecraft.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

/** Hold right-click: close-range needle buzz that inks targets. */
public class TattooGunItem extends Item {
	private static final double RANGE = 4.0;
	private static final float DAMAGE_PER_TICK = 1.5F;
	private static final int WITHER_TICKS = 3 * 20;
	private static final int SOUND_INTERVAL_TICKS = 4;

	public TattooGunItem(Properties properties) {
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
		if (ticksRemaining % SOUND_INTERVAL_TICKS == 0) {
			serverLevel.playSound(null, user.getX(), user.getY(), user.getZ(),
					SoundEvents.BEE_LOOP_AGGRESSIVE, SoundSource.PLAYERS, 0.4F, 1.8F);
		}
		HitResult hit = ProjectileUtil.getHitResultOnViewVector(user,
				entity -> entity instanceof LivingEntity && entity != user && entity.isPickable(), RANGE);
		if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity target) {
			DamageSource source = user instanceof Player player
					? user.damageSources().playerAttack(player)
					: user.damageSources().mobAttack(user);
			target.hurtServer(serverLevel, source, DAMAGE_PER_TICK);
			target.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_TICKS, 0));
			Vec3 point = hit.getLocation();
			serverLevel.sendParticles(ParticleTypes.SQUID_INK, point.x, point.y, point.z, 3, 0.1, 0.1, 0.1, 0.02);
		}
	}
}
