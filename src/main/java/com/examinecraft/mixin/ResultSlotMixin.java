package com.examinecraft.mixin;

import com.examinecraft.systems.ScoreManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

/** Awards first-craft score when a player takes output from a crafting grid. */
@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {
	@Inject(method = "onTake", at = @At("HEAD"))
	private void examinecraft$awardCraftScore(Player player, ItemStack carried, CallbackInfo ci) {
		if (player instanceof ServerPlayer serverPlayer) {
			ScoreManager.onCrafted(serverPlayer, carried);
		}
	}
}
