package com.examinecraft.npc;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Everything that makes one community NPC unique. Edit the table in
 * NpcProfiles to tweak names, catchphrases, or trades - no logic changes needed.
 */
public record NpcProfile(
		String id,
		String displayName,
		int eggPrimaryColor,
		List<String> catchphrases,
		List<TradeSpec> trades) {

	/** One villager-style trade: pay costCount x cost, receive resultCount x result. */
	public record TradeSpec(Supplier<ItemLike> cost, int costCount, Supplier<ItemLike> result, int resultCount, int maxUses) {
		public ItemStack resultStack() {
			return new ItemStack(this.result.get(), this.resultCount);
		}
	}

	public static TradeSpec trade(Supplier<ItemLike> cost, int costCount, Supplier<ItemLike> result, int resultCount) {
		return new TradeSpec(cost, costCount, result, resultCount, 12);
	}
}
