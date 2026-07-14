package com.examinecraft.systems;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;

import com.examinecraft.item.ModArmor;

/**
 * Exa's Armor per-piece bonuses: boots = Speed, leggings = Jump Boost,
 * chestplate = Regeneration. (The helmet's 90% projectile reduction lives
 * in LivingEntityMixin.) Effects are reapplied on a short cycle, ambient
 * and particle-free so they read as gear bonuses.
 */
public final class EquipmentBonuses {
	private EquipmentBonuses() {
	}

	private static final int REFRESH_INTERVAL = 20;
	private static final int EFFECT_DURATION = 60;

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % REFRESH_INTERVAL != 0) {
				return;
			}
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (player.getItemBySlot(EquipmentSlot.FEET).is(ModArmor.EXA.boots())) {
					player.addEffect(new MobEffectInstance(MobEffects.SPEED, EFFECT_DURATION, 0, true, false));
				}
				if (player.getItemBySlot(EquipmentSlot.LEGS).is(ModArmor.EXA.leggings())) {
					player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, EFFECT_DURATION, 1, true, false));
				}
				if (player.getItemBySlot(EquipmentSlot.CHEST).is(ModArmor.EXA.chestplate())) {
					player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION, 0, true, false));
				}
			}
		});
	}
}
