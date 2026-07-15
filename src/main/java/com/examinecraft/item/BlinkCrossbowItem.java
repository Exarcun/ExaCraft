package com.examinecraft.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.examinecraft.entity.BlinkBoltEntity;

/** Fires a bolt that teleports you to wherever it lands. */
public class BlinkCrossbowItem extends Item {
	private static final float SPEED = 3.5F;
	private static final int COOLDOWN_TICKS = 2 * 20;

	public BlinkCrossbowItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
		if (level instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileFromRotation(BlinkBoltEntity::new, serverLevel, stack, player, 0.0F, SPEED, 0.0F);
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
		return InteractionResult.SUCCESS;
	}
}
