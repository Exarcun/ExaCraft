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

import com.examinecraft.entity.GrilledPizzaEntity;

/** Right-click to fling a spinning pizza. */
public class GrilledPizzaItem extends Item {
	private static final float SPEED = 1.5F;

	public GrilledPizzaItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6F, 0.8F);
		if (level instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileFromRotation(GrilledPizzaEntity::new, serverLevel, stack, player, 0.0F, SPEED, 1.0F);
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}
}
