package com.examinecraft.item;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import com.examinecraft.ExaMinecraft;

public final class ModCreativeTab {
	private ModCreativeTab() {
	}

	public static final ResourceKey<CreativeModeTab> KEY =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, ExaMinecraft.id("examinecraft"));

	public static void initialize() {
		CreativeModeTab tab = FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.examinecraft"))
				.icon(() -> new ItemStack(ModItems.LSD_BLOTTER))
				.displayItems((parameters, output) -> ModItems.CREATIVE_TAB_ITEMS.forEach(output::accept))
				.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KEY, tab);
	}
}
