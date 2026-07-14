package com.examinecraft.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import com.examinecraft.ExaMinecraft;

public final class ModEntityTypes {
	private ModEntityTypes() {
	}

	public static final EntityType<ThrownDrugPotion> THROWN_DRUG_POTION = register("thrown_drug_potion",
			EntityType.Builder.<ThrownDrugPotion>of(ThrownDrugPotion::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.25F, 0.25F)
					.clientTrackingRange(4)
					.updateInterval(10));

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, ExaMinecraft.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void initialize() {
		// Forces static initialization.
	}
}
