package com.examinecraft.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.examinecraft.ExaMinecraft;
import com.examinecraft.item.SniperRifleItem;

public final class ModNetworking {
	private ModNetworking() {
	}

	/** Sent by the client when the player left-clicks while anchored with the sniper rifle. */
	public record SniperFirePayload() implements CustomPacketPayload {
		public static final SniperFirePayload INSTANCE = new SniperFirePayload();
		public static final CustomPacketPayload.Type<SniperFirePayload> TYPE =
				new CustomPacketPayload.Type<>(ExaMinecraft.id("sniper_fire"));
		public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, SniperFirePayload> CODEC =
				StreamCodec.unit(INSTANCE);

		@Override
		public CustomPacketPayload.Type<SniperFirePayload> type() {
			return TYPE;
		}
	}

	public static void initialize() {
		PayloadTypeRegistry.serverboundPlay().register(SniperFirePayload.TYPE, SniperFirePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SniperFirePayload.TYPE,
				(payload, context) -> SniperRifleItem.handleFire(context.player()));
	}
}
