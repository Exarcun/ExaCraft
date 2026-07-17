package com.examinecraft.systems;

import java.util.List;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Broadcasts rotating community tips: one message every 5 minutes, so each
 * message in the rotation repeats every 15 minutes. Silent while the server
 * is empty (the rotation still advances).
 */
public final class ServerAnnouncements {
	private static final int INTERVAL_TICKS = 5 * 60 * 20;

	private static final List<Component> MESSAGES = List.of(
			Component.empty()
					.append(Component.literal("Ogni 100 punti ricevi 5 ").withStyle(ChatFormatting.YELLOW))
					.append(Component.literal("Swiss Ingot").withStyle(ChatFormatting.RED)),
			Component.empty()
					.append(Component.literal("Spendi gli ").withStyle(ChatFormatting.YELLOW))
					.append(Component.literal("Swiss Ingot ").withStyle(ChatFormatting.RED))
					.append(Component.literal("nello shop").withStyle(ChatFormatting.YELLOW)),
			Component.empty()
					.append(Component.literal("Crafta Oggetti per la prima volta = 1 punto ").withStyle(ChatFormatting.YELLOW))
					.append(Component.literal("Uccidi ").withStyle(ChatFormatting.RED))
					.append(Component.literal("un giocatore = Guadagna 10 Punti").withStyle(ChatFormatting.YELLOW)));

	private static int tickCounter = 0;
	private static int nextMessage = 0;

	private ServerAnnouncements() {
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickCounter++;
			if (tickCounter < INTERVAL_TICKS) {
				return;
			}
			tickCounter = 0;
			Component message = MESSAGES.get(nextMessage);
			nextMessage = (nextMessage + 1) % MESSAGES.size();
			if (!server.getPlayerList().getPlayers().isEmpty()) {
				server.getPlayerList().broadcastSystemMessage(message, false);
			}
		});
	}
}
