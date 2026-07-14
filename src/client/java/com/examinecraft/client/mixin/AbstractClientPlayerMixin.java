package com.examinecraft.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.player.AbstractClientPlayer;

import com.examinecraft.entity.SniperSeatEntity;
import com.examinecraft.item.SniperRifleItem;

/** Zooms the FOV while anchored in sniper stance with the rifle in hand. */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
	private static final float SNIPER_ZOOM = 0.35F;

	@Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
	private void examinecraft$sniperZoom(boolean firstPerson, float effectScale, CallbackInfoReturnable<Float> cir) {
		AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
		if (self.getVehicle() instanceof SniperSeatEntity
				&& self.getMainHandItem().getItem() instanceof SniperRifleItem) {
			cir.setReturnValue(cir.getReturnValue() * SNIPER_ZOOM);
		}
	}
}
