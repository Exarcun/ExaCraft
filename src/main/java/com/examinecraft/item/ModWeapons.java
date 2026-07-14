package com.examinecraft.item;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import com.examinecraft.ExaMinecraft;

public final class ModWeapons {
	private ModWeapons() {
	}

	/** Extra reach on top of the default ~3 block melee range. */
	private static final double PERC_80_EXTRA_REACH = 3.0;

	public static final Item PERC_20 = ModItems.register("perc_20", Item::new,
			new Item.Properties().sword(ToolMaterial.IRON, 3.0F, -2.4F));

	public static final Item PERC_80 = ModItems.register("perc_80", Item::new,
			new Item.Properties()
					.sword(ToolMaterial.IRON, 3.0F, -2.4F)
					.attributes(ItemAttributeModifiers.builder()
							.add(Attributes.ATTACK_DAMAGE,
									new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 7.0, AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND)
							.add(Attributes.ATTACK_SPEED,
									new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND)
							.add(Attributes.ENTITY_INTERACTION_RANGE,
									new AttributeModifier(ExaMinecraft.id("perc_80_reach"), PERC_80_EXTRA_REACH, AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND)
							.build()));

	public static final Item NEEDLE = ModItems.register("needle",
			p -> new EffectOnHitItem(p, MobEffects.POISON, 5 * 20, 0),
			new Item.Properties().sword(ToolMaterial.IRON, 1.0F, -1.0F));

	public static final Item KET_POT = ModItems.register("ket_pot", Item::new,
			new Item.Properties().sword(ToolMaterial.DIAMOND, 3.0F, -2.4F));

	public static final Item GOLF_CLUB = ModItems.register("golf_club", LauncherItem::new,
			new Item.Properties().sword(ToolMaterial.IRON, 2.0F, -2.8F));

	public static final Item BASEBALL_BAT = ModItems.register("baseball_bat", LauncherItem::new,
			new Item.Properties().sword(ToolMaterial.IRON, 3.0F, -2.8F));

	public static void initialize() {
		// Forces static initialization.
	}
}
