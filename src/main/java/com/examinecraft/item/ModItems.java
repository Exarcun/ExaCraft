package com.examinecraft.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import com.examinecraft.ExaMinecraft;

public final class ModItems {
	private ModItems() {
	}

	/** Every registered item, in registration order; used to fill the creative tab. */
	public static final List<Item> CREATIVE_TAB_ITEMS = new ArrayList<>();

	private static final int SECONDS = 20;
	private static final int MINUTES = 60 * SECONDS;

	/** Zero-nutrition food properties so effect-only consumables can always be used. */
	private static final FoodProperties NO_NUTRITION = new FoodProperties.Builder().alwaysEdible().build();

	// --- Crafting materials ---

	public static final Item SWISS_INGOT = register("swiss_ingot", Item::new, new Item.Properties());

	public static final Item PIZZA_SLICE = register("pizza_slice", Item::new, new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.4F).build()));

	public static final Item SALVIA_DIVINORUM = register("salvia_divinorum", Item::new, new Item.Properties());

	public static final Item SALVIA_BREAD = register("salvia_bread", Item::new, new Item.Properties()
			.food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.7F).build()));

	// --- Effect consumables ---

	public static final Item JOINT = register("joint", Item::new, smoked(
			new MobEffectInstance(MobEffects.SPEED, 5 * MINUTES, 0)));

	public static final Item LEXOTAN = register("lexotan", Item::new, eaten(
			new MobEffectInstance(MobEffects.SLOWNESS, 1 * MINUTES, 0)));

	public static final Item KET_VIAL = register("ket_vial", Item::new, drunk(
			new MobEffectInstance(MobEffects.NAUSEA, 2 * MINUTES, 0)));

	public static final Item LSD_BLOTTER = register("lsd_blotter", Item::new, eaten(
			new MobEffectInstance(MobEffects.NAUSEA, 1 * MINUTES, 0)));

	public static final Item HASH_PIPE = register("hash_pipe", HashPipeItem::new, smoked(
			new MobEffectInstance(MobEffects.SPEED, 5 * MINUTES, 0)));

	// --- Special items ---

	public static final Item PERC_POTION = register("perc_potion",
			p -> new DrugPotionItem(p, false, List.of(
					new DrugPotionItem.EffectSpec(MobEffects.SLOWNESS, 2 * MINUTES, 0),
					new DrugPotionItem.EffectSpec(MobEffects.REGENERATION, 2 * MINUTES, 1))),
			new Item.Properties().stacksTo(16));

	public static final Item OPIUM_POTION = register("opium_potion",
			p -> new DrugPotionItem(p, false, List.of(
					new DrugPotionItem.EffectSpec(MobEffects.SLOWNESS, 1 * MINUTES, 1),
					new DrugPotionItem.EffectSpec(MobEffects.RESISTANCE, 1 * MINUTES, 4))),
			new Item.Properties().stacksTo(16));

	public static final Item NARCAN_POTION = register("narcan_potion",
			p -> new DrugPotionItem(p, true, List.of()),
			new Item.Properties().stacksTo(16));

	public static final Item QUAALUDE = register("quaalude", QuaaludeItem::new, eaten());

	public static final Item DMT = register("dmt", DmtItem::new, eaten(
			new MobEffectInstance(MobEffects.NAUSEA, 1 * MINUTES, 0),
			new MobEffectInstance(MobEffects.SLOW_FALLING, 1 * MINUTES, 0)));

	public static final Item GRAPPLING_HOOK = register("grappling_hook", GrapplingHookItem::new,
			new Item.Properties().stacksTo(1).useCooldown(1.5F));

	// --- Helpers ---

	private static Item.Properties eaten(MobEffectInstance... effects) {
		return effectConsumable(Consumables.defaultFood(), effects);
	}

	private static Item.Properties drunk(MobEffectInstance... effects) {
		return effectConsumable(Consumables.defaultDrink(), effects);
	}

	/** Smoked things behave like food but without eat particles. */
	private static Item.Properties smoked(MobEffectInstance... effects) {
		return effectConsumable(Consumables.defaultFood().hasConsumeParticles(false), effects);
	}

	private static Item.Properties effectConsumable(Consumable.Builder consumable, MobEffectInstance... effects) {
		for (MobEffectInstance effect : effects) {
			consumable.onConsume(new ApplyStatusEffectsConsumeEffect(effect));
		}
		return new Item.Properties().food(NO_NUTRITION, consumable.build());
	}

	public static <T extends Item> T register(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ExaMinecraft.id(name));
		T item = factory.apply(properties.setId(key));
		Registry.register(BuiltInRegistries.ITEM, key, item);
		CREATIVE_TAB_ITEMS.add(item);
		return item;
	}

	public static void initialize() {
		// Forces static initialization of all item fields.
	}
}
