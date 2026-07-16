package com.examinecraft.systems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.examinecraft.item.ModItems;
import com.examinecraft.systems.PlayerScore.ScoreData;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * Central score pipeline: applies the scoring rules, pays Swiss Ingot
 * milestones, and mirrors totals into the tab player list (hold Tab to see
 * each player's score). The player attachment is the source of truth; the
 * scoreboard objective exists purely for display.
 */
public final class ScoreManager {
	public static final int CRAFT_POINTS = 1;
	public static final int PVP_KILL_POINTS = 10;
	public static final int BOSS_FIRST_POINTS = 100;
	public static final int BOSS_REPEAT_POINTS = 10;
	public static final long PVP_COOLDOWN_MS = 30L * 60L * 1000L;
	public static final int MILESTONE_SIZE = 100;
	public static final int MILESTONE_INGOTS = 5;

	private static final String OBJECTIVE_NAME = "exa_score";

	private ScoreManager() {
	}

	public static void initialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			Scoreboard scoreboard = server.getScoreboard();
			Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
			if (objective == null) {
				objective = scoreboard.addObjective(OBJECTIVE_NAME, ObjectiveCriteria.DUMMY,
						Component.literal("Community Score").withStyle(ChatFormatting.GOLD),
						ObjectiveCriteria.RenderType.INTEGER, true, null);
			}
			scoreboard.setDisplayObjective(DisplaySlot.LIST, objective);
			// Clear the sidebar in case an earlier build claimed it.
			if (scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) == objective) {
				scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, null);
			}
		});
		ServerPlayerEvents.JOIN.register(player ->
				syncEntry(player.level().getServer(), player.getScoreboardName(), data(player).total()));
	}

	public static ScoreData data(ServerPlayer player) {
		ScoreData data = player.getAttached(PlayerScore.SCORE);
		return data == null ? ScoreData.EMPTY : data;
	}

	/** +1 the first time each item type is ever crafted; repeat crafts give nothing. */
	public static void onCrafted(ServerPlayer player, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
		ScoreData data = data(player);
		if (data.craftedTypes().contains(itemId)) {
			return;
		}
		Set<String> crafted = new HashSet<>(data.craftedTypes());
		crafted.add(itemId);
		apply(player, new ScoreData(data.total() + CRAFT_POINTS, data.craftPoints() + CRAFT_POINTS,
				data.pvpPoints(), data.bossPoints(), crafted, data.pvpCooldowns(),
				data.bossesKilled(), data.paidMilestones()));
	}

	/** +10 per player kill; the same victim is worth nothing for 30 minutes. */
	public static void onPlayerKill(ServerPlayer killer, ServerPlayer victim) {
		if (killer == victim) {
			return;
		}
		long now = System.currentTimeMillis();
		ScoreData data = data(killer);
		String victimId = victim.getUUID().toString();
		Long lastScored = data.pvpCooldowns().get(victimId);
		if (lastScored != null && now - lastScored < PVP_COOLDOWN_MS) {
			return;
		}
		Map<String, Long> cooldowns = new HashMap<>(data.pvpCooldowns());
		cooldowns.values().removeIf(time -> now - time >= PVP_COOLDOWN_MS);
		cooldowns.put(victimId, now);
		apply(killer, new ScoreData(data.total() + PVP_KILL_POINTS, data.craftPoints(),
				data.pvpPoints() + PVP_KILL_POINTS, data.bossPoints(), data.craftedTypes(),
				cooldowns, data.bossesKilled(), data.paidMilestones()));
	}

	/** +100 for the first kill of each boss type, +10 for repeats. First kills are broadcast. */
	public static void onBossKill(ServerPlayer killer, MinecraftServer server, EntityType<?> type) {
		String bossId = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
		ScoreData data = data(killer);
		boolean first = !data.bossesKilled().contains(bossId);
		int points = first ? BOSS_FIRST_POINTS : BOSS_REPEAT_POINTS;
		Set<String> bosses = new HashSet<>(data.bossesKilled());
		bosses.add(bossId);
		apply(killer, new ScoreData(data.total() + points, data.craftPoints(), data.pvpPoints(),
				data.bossPoints() + points, data.craftedTypes(), data.pvpCooldowns(),
				bosses, data.paidMilestones()));
		if (first) {
			server.getPlayerList().broadcastSystemMessage(Component.literal(
					killer.getScoreboardName() + " slew the " + prettyBossName(bossId)
							+ "! +" + points + " score!").withStyle(ChatFormatting.GOLD), false);
		}
	}

	/** Admin correction: overwrite the total. Never pays milestones (counter is clamped). */
	public static void setTotal(ServerPlayer player, int total) {
		total = Math.max(0, total);
		ScoreData data = data(player);
		player.setAttached(PlayerScore.SCORE, new ScoreData(total, data.craftPoints(),
				data.pvpPoints(), data.bossPoints(), data.craftedTypes(), data.pvpCooldowns(),
				data.bossesKilled(), total / MILESTONE_SIZE));
		syncEntry(player.level().getServer(), player.getScoreboardName(), total);
	}

	/** Admin correction: add (or subtract) points. Never pays milestones. */
	public static void addTotal(ServerPlayer player, int amount) {
		setTotal(player, data(player).total() + amount);
	}

	private static void apply(ServerPlayer player, ScoreData data) {
		data = payMilestones(player, data);
		player.setAttached(PlayerScore.SCORE, data);
		syncEntry(player.level().getServer(), player.getScoreboardName(), data.total());
	}

	private static ScoreData payMilestones(ServerPlayer player, ScoreData data) {
		int owed = data.total() / MILESTONE_SIZE;
		int paid = data.paidMilestones();
		if (owed <= paid) {
			return data;
		}
		for (int i = paid; i < owed; i++) {
			ItemStack ingots = new ItemStack(ModItems.SWISS_INGOT, MILESTONE_INGOTS);
			if (!player.getInventory().add(ingots)) {
				player.drop(ingots, false);
			}
		}
		int totalIngots = (owed - paid) * MILESTONE_INGOTS;
		player.sendSystemMessage(Component.literal("Milestone reached! +" + totalIngots
				+ " Swiss Ingots (score " + data.total() + ").").withStyle(ChatFormatting.GOLD));
		return new ScoreData(data.total(), data.craftPoints(), data.pvpPoints(), data.bossPoints(),
				data.craftedTypes(), data.pvpCooldowns(), data.bossesKilled(), owed);
	}

	private static void syncEntry(MinecraftServer server, String name, int total) {
		if (server == null) {
			return;
		}
		Scoreboard scoreboard = server.getScoreboard();
		Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
		if (objective == null) {
			return;
		}
		scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(name), objective).set(total);
	}

	private static String prettyBossName(String bossId) {
		String path = bossId.contains(":") ? bossId.substring(bossId.indexOf(':') + 1) : bossId;
		StringBuilder pretty = new StringBuilder();
		for (String word : path.split("_")) {
			if (!pretty.isEmpty()) {
				pretty.append(' ');
			}
			pretty.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return pretty.toString();
	}
}
