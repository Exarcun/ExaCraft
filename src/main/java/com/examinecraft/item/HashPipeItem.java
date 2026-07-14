package com.examinecraft.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Grants its consumable effects but hurts the smoker (2 hearts). */
public class HashPipeItem extends Item {
	private static final float SELF_DAMAGE = 4.0F;

	public HashPipeItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		ItemStack result = super.finishUsingItem(stack, level, entity);
		if (level instanceof ServerLevel serverLevel) {
			entity.hurtServer(serverLevel, entity.damageSources().magic(), SELF_DAMAGE);
		}
		return result;
	}
}
