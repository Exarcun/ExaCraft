package com.examinecraft.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

import com.examinecraft.entity.ModEntityTypes;
import com.examinecraft.entity.SniperSeatEntity;
import com.examinecraft.item.SniperRifleItem;
import com.examinecraft.net.ModNetworking;

public class ExaMinecraftClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntityTypes.THROWN_DRUG_POTION, ThrownItemRenderer::new);
		EntityRenderers.register(ModEntityTypes.BULLET, ThrownItemRenderer::new);
		EntityRenderers.register(ModEntityTypes.NINJA_STAR, ThrownItemRenderer::new);
		EntityRenderers.register(ModEntityTypes.GRILLED_PIZZA, ThrownItemRenderer::new);
		EntityRenderers.register(ModEntityTypes.BLINK_BOLT, ThrownItemRenderer::new);
		EntityRenderers.register(ModEntityTypes.SNIPER_SEAT, NoopRenderer::new);
		EntityRenderers.register(ModEntityTypes.ORBITAL_STRIKE, NoopRenderer::new);
		com.examinecraft.npc.ModNpcs.TYPES.values()
				.forEach(type -> EntityRenderers.register(type, NpcRenderer::new));

		// Left-click fires the sniper while anchored; cancel the vanilla swing.
		ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
			if (player.getVehicle() instanceof SniperSeatEntity
					&& player.getMainHandItem().getItem() instanceof SniperRifleItem) {
				ClientPlayNetworking.send(ModNetworking.SniperFirePayload.INSTANCE);
				return true;
			}
			return false;
		});
	}
}
