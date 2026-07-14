package com.examinecraft.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

import com.examinecraft.entity.BulletEntity;

/** Hold right-click for rapid fire. Unlimited ammo. */
public class UziItem extends Item {
	private static final int FIRE_INTERVAL_TICKS = 2;
	private static final float BULLET_SPEED = 3.0F;
	private static final float SPREAD = 6.0F;

	public UziItem(Properties properties) {
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
		if (level instanceof ServerLevel serverLevel && ticksRemaining % FIRE_INTERVAL_TICKS == 0) {
			Projectile.spawnProjectileFromRotation(BulletEntity::new, serverLevel, stack, user, 0.0F, BULLET_SPEED, SPREAD);
			serverLevel.playSound(null, user.getX(), user.getY(), user.getZ(),
					SoundEvents.DISPENSER_LAUNCH, SoundSource.PLAYERS, 0.5F, 1.6F);
		}
	}
}
