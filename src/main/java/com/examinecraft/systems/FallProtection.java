package com.examinecraft.systems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Player;

/**
 * Cancels fall damage for players who recently used the grappling hook.
 * Protection ends when the player lands (after a short grace period) or
 * after a hard timeout.
 */
public final class FallProtection {
	private FallProtection() {
	}

	/** Player UUID -> ticks since grapple. */
	private static final Map<UUID, Integer> PROTECTED = new HashMap<>();

	private static final int GRACE_TICKS = 10;
	private static final int MAX_TICKS = 20 * 30;

	public static void protect(ServerPlayer player) {
		PROTECTED.put(player.getUUID(), 0);
	}

	public static void initialize() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (entity instanceof Player player && source.is(DamageTypeTags.IS_FALL)
					&& PROTECTED.remove(player.getUUID()) != null) {
				return false;
			}
			return true;
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (PROTECTED.isEmpty()) {
				return;
			}
			PROTECTED.entrySet().removeIf(entry -> {
				ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
				if (player == null) {
					return true;
				}
				int ticks = entry.getValue() + 1;
				entry.setValue(ticks);
				return ticks > MAX_TICKS || (ticks > GRACE_TICKS && player.onGround());
			});
		});
	}
}
