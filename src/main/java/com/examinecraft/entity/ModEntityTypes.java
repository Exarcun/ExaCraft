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

	public static final EntityType<BulletEntity> BULLET = register("bullet",
			EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.15F, 0.15F)
					.clientTrackingRange(4)
					.updateInterval(10));

	public static final EntityType<NinjaStarEntity> NINJA_STAR = register("ninja_star",
			EntityType.Builder.<NinjaStarEntity>of(NinjaStarEntity::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.25F, 0.25F)
					.clientTrackingRange(4)
					.updateInterval(10));

	public static final EntityType<GrilledPizzaEntity> GRILLED_PIZZA = register("grilled_pizza",
			EntityType.Builder.<GrilledPizzaEntity>of(GrilledPizzaEntity::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.25F, 0.25F)
					.clientTrackingRange(4)
					.updateInterval(10));

	public static final EntityType<BlinkBoltEntity> BLINK_BOLT = register("blink_bolt",
			EntityType.Builder.<BlinkBoltEntity>of(BlinkBoltEntity::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.15F, 0.15F)
					.clientTrackingRange(4)
					.updateInterval(10));

	public static final EntityType<SniperSeatEntity> SNIPER_SEAT = register("sniper_seat",
			EntityType.Builder.<SniperSeatEntity>of(SniperSeatEntity::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.4F, 0.4F)
					.clientTrackingRange(8)
					.updateInterval(10));

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, ExaMinecraft.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void initialize() {
		// Forces static initialization.
	}
}
