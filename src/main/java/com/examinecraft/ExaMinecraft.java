package com.examinecraft;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExaMinecraft implements ModInitializer {
	public static final String MOD_ID = "examinecraft";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("ExaMinecraft loading!");
		com.examinecraft.item.ModItems.initialize();
		com.examinecraft.item.ModCreativeTab.initialize();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
