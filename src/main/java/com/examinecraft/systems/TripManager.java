package com.examinecraft.systems;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Runs the DMT "trip": while active, pulses short Levitation bursts and
 * Darkness waves on the tripping player. The steady Nausea + Slow Falling
 * come from the item's consumable component.
 */
public final class TripManager {
	private TripManager() {
	}

	private static final Map<UUID, Integer> REMAINING_TICKS = new HashMap<>();

	private static final int LEVITATION_INTERVAL = 80;
	private static final int LEVITATION_LENGTH = 25;
	private static final int DARKNESS_INTERVAL = 70;
	private static final int DARKNESS_LENGTH = 50;

	public static void start(ServerPlayer player, int durationTicks) {
		REMAINING_TICKS.put(player.getUUID(), durationTicks);
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (REMAINING_TICKS.isEmpty()) {
				return;
			}
			Iterator<Map.Entry<UUID, Integer>> it = REMAINING_TICKS.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<UUID, Integer> entry = it.next();
				ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
				int remaining = entry.getValue() - 1;
				if (player == null || remaining <= 0) {
					it.remove();
					continue;
				}
				entry.setValue(remaining);
				if (remaining % LEVITATION_INTERVAL == 0) {
					player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, LEVITATION_LENGTH, 0));
				}
				if (remaining % DARKNESS_INTERVAL == 0) {
					player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DARKNESS_LENGTH, 0));
				}
			}
		});
	}
}
