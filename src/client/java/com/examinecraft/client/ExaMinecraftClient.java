package com.examinecraft.client;

import net.fabricmc.api.ClientModInitializer;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

import com.examinecraft.entity.ModEntityTypes;

public class ExaMinecraftClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntityTypes.THROWN_DRUG_POTION, ThrownItemRenderer::new);
	}
}
