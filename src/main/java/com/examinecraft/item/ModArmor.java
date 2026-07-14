package com.examinecraft.item;

import java.util.List;
import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

import com.examinecraft.ExaMinecraft;

/**
 * All 10 community armor sets. Each set is one ArmorMaterial (stats cloned
 * from a vanilla tier) plus four items; worn looks come from the equipment
 * asset JSON + textures under assets/examinecraft/equipment/.
 */
public final class ModArmor {
	private ModArmor() {
	}

	public record ArmorSet(String id, Item helmet, Item chestplate, Item leggings, Item boots) {
		public List<Item> pieces() {
			return List.of(this.helmet, this.chestplate, this.leggings, this.boots);
		}
	}

	// Vanilla tier stat blocks: durability, defense (helmet, chest, legs, boots), enchant value, toughness, kb resistance.
	private record Tier(int durability, int helm, int chest, int legs, int boots, int enchantValue,
			Holder<SoundEvent> sound, float toughness, float knockbackResistance, TagKey<Item> repair) {
	}

	private static final Tier LEATHER = new Tier(5, 1, 3, 2, 1, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, ItemTags.REPAIRS_LEATHER_ARMOR);
	private static final Tier IRON = new Tier(15, 2, 6, 5, 2, 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, ItemTags.REPAIRS_IRON_ARMOR);
	private static final Tier DIAMOND = new Tier(33, 3, 8, 6, 3, 10, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, ItemTags.REPAIRS_DIAMOND_ARMOR);
	private static final Tier NETHERITE = new Tier(37, 3, 8, 6, 3, 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, ItemTags.REPAIRS_NETHERITE_ARMOR);

	public static final ArmorSet SWISSMAN = registerSet("swissman", NETHERITE);
	public static final ArmorSet BENITO = registerSet("benito", IRON);
	public static final ArmorSet TWITCH = registerSet("twitch", IRON);
	public static final ArmorSet MOD = registerSet("mod", IRON);
	public static final ArmorSet EXA = registerSet("exa", IRON);
	public static final ArmorSet PIG = registerSet("pig", IRON);
	public static final ArmorSet TWITCH_SUB = registerSet("twitch_sub", DIAMOND);
	public static final ArmorSet GAYMAN = registerSet("gayman", DIAMOND);
	public static final ArmorSet LAZYMAN = registerSet("lazyman", LEATHER);
	public static final ArmorSet GRILLED_PIZZA = registerSet("grilled_pizza", DIAMOND);

	public static final List<ArmorSet> ALL_SETS = List.of(SWISSMAN, BENITO, TWITCH, MOD, EXA,
			PIG, TWITCH_SUB, GAYMAN, LAZYMAN, GRILLED_PIZZA);

	private static ArmorSet registerSet(String id, Tier tier) {
		ArmorMaterial material = new ArmorMaterial(
				tier.durability(),
				Map.of(
						ArmorType.HELMET, tier.helm(),
						ArmorType.CHESTPLATE, tier.chest(),
						ArmorType.LEGGINGS, tier.legs(),
						ArmorType.BOOTS, tier.boots()),
				tier.enchantValue(),
				tier.sound(),
				tier.toughness(),
				tier.knockbackResistance(),
				tier.repair(),
				ResourceKey.create(EquipmentAssets.ROOT_ID, ExaMinecraft.id(id)));
		return new ArmorSet(id,
				ModItems.register(id + "_helmet", Item::new, new Item.Properties().humanoidArmor(material, ArmorType.HELMET)),
				ModItems.register(id + "_chestplate", Item::new, new Item.Properties().humanoidArmor(material, ArmorType.CHESTPLATE)),
				ModItems.register(id + "_leggings", Item::new, new Item.Properties().humanoidArmor(material, ArmorType.LEGGINGS)),
				ModItems.register(id + "_boots", Item::new, new Item.Properties().humanoidArmor(material, ArmorType.BOOTS)));
	}

	public static void initialize() {
		// Forces static initialization.
	}
}
