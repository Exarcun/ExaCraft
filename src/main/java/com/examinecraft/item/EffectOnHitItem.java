package com.examinecraft.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Melee weapon that applies a status effect to whatever it hits. */
public class EffectOnHitItem extends Item {
	private final Holder<MobEffect> effect;
	private final int durationTicks;
	private final int amplifier;

	public EffectOnHitItem(Properties properties, Holder<MobEffect> effect, int durationTicks, int amplifier) {
		super(properties);
		this.effect = effect;
		this.durationTicks = durationTicks;
		this.amplifier = amplifier;
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		target.addEffect(new MobEffectInstance(this.effect, this.durationTicks, this.amplifier));
	}
}
