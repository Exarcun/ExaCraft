package com.examinecraft.npc;

import java.util.List;

import net.minecraft.world.item.Items;

import com.examinecraft.item.ModArmor;
import com.examinecraft.item.ModItems;
import com.examinecraft.item.ModWeapons;

import static com.examinecraft.npc.NpcProfile.trade;

/**
 * The community roster. Placeholder catchphrases - swap the strings freely.
 * Trades: each NPC wants something different and sells rare mod gear.
 */
public final class NpcProfiles {
	private NpcProfiles() {
	}

	public static final NpcProfile EDDYCARISMA = new NpcProfile("eddycarisma", "EddyCarisma", 0xD32F2F,
			List.of(
					"Welcome to the stream!",
					"Chat, clip that!",
					"We go again."),
			List.of(
					trade(() -> Items.EMERALD, 8, () -> ModItems.PERC_POTION, 2),
					trade(() -> Items.GOLD_INGOT, 16, () -> ModArmor.SWISSMAN.chestplate(), 1),
					trade(() -> Items.DIAMOND, 3, () -> ModItems.JOINT, 4)));

	public static final NpcProfile EXAROBOT = new NpcProfile("exarobot", "Exarobot", 0x6E6E7E,
			List.of(
					"Compiling...",
					"Have you tried turning it off and on again?",
					"Shipping to production on a Friday."),
			List.of(
					trade(() -> Items.REDSTONE, 32, () -> ModWeapons.UZI, 1),
					trade(() -> Items.COPPER_INGOT, 20, () -> ModArmor.EXA.helmet(), 1),
					trade(() -> Items.IRON_INGOT, 10, () -> ModItems.GRAPPLING_HOOK, 1)));

	public static final NpcProfile MALI = new NpcProfile("mali", "Mali", 0x8E44AD,
			List.of(
					"Anyone got a spare potion?",
					"This is fine."),
			List.of(
					trade(() -> Items.POPPY, 12, () -> ModItems.OPIUM_POTION, 2),
					trade(() -> Items.EMERALD, 5, () -> ModItems.KET_VIAL, 3)));

	public static final NpcProfile SAM = new NpcProfile("sam", "Sam", 0xE67E22,
			List.of(
					"Fresh bread, get it while it's weird.",
					"I know a guy."),
			List.of(
					trade(() -> Items.WHEAT, 20, () -> ModItems.SALVIA_BREAD, 4),
					trade(() -> Items.EMERALD, 6, () -> ModItems.QUAALUDE, 2)));

	public static final NpcProfile JOSH = new NpcProfile("josh", "Josh", 0x27AE60,
			List.of(
					"Swing first, ask later.",
					"Batter up!"),
			List.of(
					trade(() -> Items.LEATHER, 10, () -> ModArmor.LAZYMAN.chestplate(), 1),
					trade(() -> Items.EMERALD, 4, () -> ModWeapons.BASEBALL_BAT, 1)));

	public static final NpcProfile DARIO = new NpcProfile("dario", "Dario", 0x2980B9,
			List.of(
					"Pizza is a food group.",
					"Grilled, never microwaved."),
			List.of(
					trade(() -> Items.COOKED_PORKCHOP, 8, () -> ModItems.PIZZA_SLICE, 12),
					trade(() -> Items.EMERALD, 10, () -> ModArmor.GRILLED_PIZZA.helmet(), 1)));

	public static final NpcProfile ILLY = new NpcProfile("illy", "Illy", 0xF06292,
			List.of(
					"The colors, dude. The colors.",
					"Stay hydrated."),
			List.of(
					trade(() -> Items.PAPER, 16, () -> ModItems.LSD_BLOTTER, 4),
					trade(() -> Items.EMERALD, 8, () -> ModItems.DMT, 1)));

	public static final NpcProfile PELISULPETTO = new NpcProfile("pelisulpetto", "PeliSulPetto", 0x795548,
			List.of(
					"Silent. Deadly. Hairy.",
					"You never saw me."),
			List.of(
					trade(() -> Items.BONE, 12, () -> ModWeapons.NINJA_STAR, 16),
					trade(() -> Items.EMERALD, 12, () -> ModWeapons.SNIPER_RIFLE, 1)));

	public static final List<NpcProfile> ALL = List.of(
			EDDYCARISMA, EXAROBOT, MALI, SAM, JOSH, DARIO, ILLY, PELISULPETTO);
}
