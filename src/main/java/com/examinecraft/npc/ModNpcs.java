package com.examinecraft.npc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;

import com.examinecraft.ExaMinecraft;
import com.examinecraft.item.ModItems;

/** Registers one entity type + spawn egg per community member. */
public final class ModNpcs {
	private ModNpcs() {
	}

	private static final Map<EntityType<?>, NpcProfile> PROFILE_BY_TYPE = new HashMap<>();
	public static final Map<NpcProfile, EntityType<CommunityNpcEntity>> TYPES = new LinkedHashMap<>();

	public static NpcProfile profileFor(EntityType<?> type) {
		NpcProfile profile = PROFILE_BY_TYPE.get(type);
		if (profile == null) {
			throw new IllegalStateException("No NPC profile for entity type " + type);
		}
		return profile;
	}

	public static void initialize() {
		for (NpcProfile profile : NpcProfiles.ALL) {
			ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, ExaMinecraft.id(profile.id()));
			EntityType<CommunityNpcEntity> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, key,
					EntityType.Builder.of(CommunityNpcEntity::new, MobCategory.CREATURE)
							.sized(0.6F, 1.8F)
							.eyeHeight(1.62F)
							.clientTrackingRange(10)
							.build(key));
			PROFILE_BY_TYPE.put(type, profile);
			TYPES.put(profile, type);
			FabricDefaultAttributeRegistry.register(type, CommunityNpcEntity.createAttributes());
			ModItems.register(profile.id() + "_spawn_egg", Item::new,
					new Item.Properties().spawnEgg(type));
		}
	}
}
