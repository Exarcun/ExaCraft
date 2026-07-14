package com.examinecraft.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Golf club / baseball bat: hitting something sends it flying. */
public class LauncherItem extends Item {
	private static final double HORIZONTAL_POWER = 3.0;
	private static final double VERTICAL_POWER = 1.1;

	public LauncherItem(Properties properties) {
		super(properties);
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		Vec3 look = attacker.getLookAngle();
		Vec3 launch = new Vec3(look.x, 0.0, look.z).normalize()
				.scale(HORIZONTAL_POWER)
				.add(0.0, VERTICAL_POWER, 0.0);
		target.setDeltaMovement(launch);
		target.hurtMarked = true;
	}
}
