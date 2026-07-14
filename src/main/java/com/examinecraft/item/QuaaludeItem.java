package com.examinecraft.item;

import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Summons wolves bound to the user, topping up to a hard cap of {@value #CAP}.
 * Summoned wolves are tagged so re-use never exceeds the cap.
 */
public class QuaaludeItem extends Item {
	public static final String WOLF_TAG = "examinecraft_quaalude_wolf";
	private static final int CAP = 5;
	private static final double COUNT_RADIUS = 64.0;

	public QuaaludeItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		ItemStack result = super.finishUsingItem(stack, level, entity);
		if (level instanceof ServerLevel serverLevel && entity instanceof ServerPlayer player) {
			AABB area = player.getBoundingBox().inflate(COUNT_RADIUS);
			List<Wolf> owned = serverLevel.getEntitiesOfClass(Wolf.class, area,
					wolf -> wolf.entityTags().contains(WOLF_TAG) && wolf.isOwnedBy(player));
			for (int i = owned.size(); i < CAP; i++) {
				Wolf wolf = EntityTypes.WOLF.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
				if (wolf == null) {
					break;
				}
				wolf.snapTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
				wolf.tame(player);
				wolf.addTag(WOLF_TAG);
				serverLevel.addFreshEntity(wolf);
			}
		}
		return result;
	}
}
