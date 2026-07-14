package com.examinecraft.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.examinecraft.systems.TripManager;

/** The combined trip: component effects plus pulsing levitation/darkness via TripManager. */
public class DmtItem extends Item {
	private static final int TRIP_TICKS = 60 * 20;

	public DmtItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		ItemStack result = super.finishUsingItem(stack, level, entity);
		if (entity instanceof ServerPlayer player) {
			TripManager.start(player, TRIP_TICKS);
		}
		return result;
	}
}
