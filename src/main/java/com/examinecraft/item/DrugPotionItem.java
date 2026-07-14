package com.examinecraft.item;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.examinecraft.entity.ThrownDrugPotion;

/** Throwable splash potion applying a fixed effect list (or clearing all effects). */
public class DrugPotionItem extends Item {
	public record EffectSpec(Holder<MobEffect> effect, int durationTicks, int amplifier) {
	}

	private final boolean clearAllEffects;
	private final List<EffectSpec> effects;

	public DrugPotionItem(Properties properties, boolean clearAllEffects, List<EffectSpec> effects) {
		super(properties);
		this.clearAllEffects = clearAllEffects;
		this.effects = effects;
	}

	public void applyTo(LivingEntity target) {
		if (this.clearAllEffects) {
			target.removeAllEffects();
		}
		for (EffectSpec spec : this.effects) {
			target.addEffect(new MobEffectInstance(spec.effect(), spec.durationTicks(), spec.amplifier()));
		}
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5F,
				0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
		if (level instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileFromRotation(ThrownDrugPotion::new, serverLevel, stack, player, -20.0F, 0.5F, 1.0F);
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}
}
