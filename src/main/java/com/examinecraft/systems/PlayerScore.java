package com.examinecraft.systems;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.examinecraft.ExaMinecraft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/**
 * Per-player progression score, persisted on the player's save data.
 * Holds the anti-abuse state alongside the totals: which item types were
 * already crafted, per-victim PvP cooldowns, and which bosses were first-killed.
 */
public final class PlayerScore {
	public record ScoreData(int total, int craftPoints, int pvpPoints, int bossPoints,
			Set<String> craftedTypes, Map<String, Long> pvpCooldowns, Set<String> bossesKilled,
			int paidMilestones) {
		public static final ScoreData EMPTY = new ScoreData(0, 0, 0, 0, Set.of(), Map.of(), Set.of(), 0);

		public static final Codec<ScoreData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("total").forGetter(ScoreData::total),
				Codec.INT.fieldOf("craft_points").forGetter(ScoreData::craftPoints),
				Codec.INT.fieldOf("pvp_points").forGetter(ScoreData::pvpPoints),
				Codec.INT.fieldOf("boss_points").forGetter(ScoreData::bossPoints),
				Codec.STRING.listOf().<Set<String>>xmap(Set::copyOf, List::copyOf)
						.fieldOf("crafted_types").forGetter(ScoreData::craftedTypes),
				Codec.unboundedMap(Codec.STRING, Codec.LONG)
						.fieldOf("pvp_cooldowns").forGetter(ScoreData::pvpCooldowns),
				Codec.STRING.listOf().<Set<String>>xmap(Set::copyOf, List::copyOf)
						.fieldOf("bosses_killed").forGetter(ScoreData::bossesKilled),
				Codec.INT.fieldOf("paid_milestones").forGetter(ScoreData::paidMilestones)
		).apply(instance, ScoreData::new));
	}

	public static final AttachmentType<ScoreData> SCORE = AttachmentRegistry.create(ExaMinecraft.id("score"),
			builder -> builder.persistent(ScoreData.CODEC).copyOnDeath());

	private PlayerScore() {
	}

	public static void initialize() {
		// Referencing the class from mod init is enough; the SCORE attachment registers on class load.
	}
}
