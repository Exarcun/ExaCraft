package com.examinecraft.systems;

import java.util.Set;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

/** Routes kill events into the score pipeline. Crafting is routed via ResultSlotMixin. */
public final class ScoreEvents {
	private static final Set<EntityType<?>> BOSSES = Set.of(
			EntityTypes.ENDER_DRAGON, EntityTypes.WARDEN, EntityTypes.WITHER);

	private ScoreEvents() {
	}

	public static void initialize() {
		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((level, killer, killed, damageSource) -> {
			if (!(killer instanceof ServerPlayer player)) {
				return;
			}
			if (killed instanceof ServerPlayer victim) {
				ScoreManager.onPlayerKill(player, victim);
			} else if (BOSSES.contains(killed.getType())) {
				ScoreManager.onBossKill(player, level.getServer(), killed.getType());
			}
		});
	}
}
