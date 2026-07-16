package com.examinecraft.command;

import java.util.Set;

import com.examinecraft.systems.PlayerHomes;
import com.examinecraft.systems.PlayerScore.ScoreData;
import com.examinecraft.systems.ScoreManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Relative;

/**
 * Server commands. /spawn teleports any player to the community spawn point;
 * /sethome and /home manage each player's personal home point.
 */
public final class ModCommands {
	private static final double SPAWN_X = 54.5;
	private static final double SPAWN_Y = 121.0;
	private static final double SPAWN_Z = 100.5;

	private static final PermissionCheck ADMIN_CHECK = new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER);

	private ModCommands() {
	}

	public static void initialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("spawn").executes(context -> {
				ServerPlayer player = context.getSource().getPlayerOrException();
				ServerLevel overworld = context.getSource().getServer().overworld();
				player.teleportTo(overworld, SPAWN_X, SPAWN_Y, SPAWN_Z,
						Set.of(Relative.X_ROT, Relative.Y_ROT), 0.0F, 0.0F, false);
				overworld.playSound(null, SPAWN_X, SPAWN_Y, SPAWN_Z,
						SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
				context.getSource().sendSuccess(() -> Component.literal("Whoosh! Welcome to spawn."), false);
				return 1;
			}));

			dispatcher.register(Commands.literal("sethome").executes(context -> {
				ServerPlayer player = context.getSource().getPlayerOrException();
				PlayerHomes.Home home = new PlayerHomes.Home(player.level().dimension(),
						player.getX(), player.getY(), player.getZ());
				player.setAttached(PlayerHomes.HOME, home);
				context.getSource().sendSuccess(() -> Component.literal(String.format(
						"Home set at %d, %d, %d.", (int) home.x(), (int) home.y(), (int) home.z())), false);
				return 1;
			}));

			dispatcher.register(Commands.literal("home").executes(context -> {
				ServerPlayer player = context.getSource().getPlayerOrException();
				PlayerHomes.Home home = player.getAttached(PlayerHomes.HOME);
				if (home == null) {
					context.getSource().sendFailure(Component.literal("You haven't set a home yet - use /sethome."));
					return 0;
				}
				ServerLevel level = context.getSource().getServer().getLevel(home.dimension());
				if (level == null) {
					context.getSource().sendFailure(Component.literal("Your home's world no longer exists."));
					return 0;
				}
				player.teleportTo(level, home.x(), home.y(), home.z(),
						Set.of(Relative.X_ROT, Relative.Y_ROT), 0.0F, 0.0F, false);
				level.playSound(null, home.x(), home.y(), home.z(),
						SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
				context.getSource().sendSuccess(() -> Component.literal("Whoosh! Welcome home."), false);
				return 1;
			}));

			dispatcher.register(Commands.literal("score")
					.executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						ScoreData data = ScoreManager.data(player);
						int toNext = ScoreManager.MILESTONE_SIZE - (data.total() % ScoreManager.MILESTONE_SIZE);
						context.getSource().sendSuccess(() -> Component.literal(String.format(
								"Score: %d (crafting %d, PvP %d, bosses %d). %d points to your next %d Swiss Ingots.",
								data.total(), data.craftPoints(), data.pvpPoints(), data.bossPoints(),
								toNext, ScoreManager.MILESTONE_INGOTS)), false);
						return data.total();
					})
					.then(Commands.literal("set")
							.requires(Commands.hasPermission(ADMIN_CHECK))
							.then(Commands.argument("player", EntityArgument.player())
									.then(Commands.argument("amount", IntegerArgumentType.integer(0))
											.executes(context -> {
												ServerPlayer target = EntityArgument.getPlayer(context, "player");
												int amount = IntegerArgumentType.getInteger(context, "amount");
												ScoreManager.setTotal(target, amount);
												context.getSource().sendSuccess(() -> Component.literal(
														"Set " + target.getScoreboardName() + "'s score to "
																+ amount + " (admin changes never pay ingots)."), true);
												return amount;
											}))))
					.then(Commands.literal("add")
							.requires(Commands.hasPermission(ADMIN_CHECK))
							.then(Commands.argument("player", EntityArgument.player())
									.then(Commands.argument("amount", IntegerArgumentType.integer())
											.executes(context -> {
												ServerPlayer target = EntityArgument.getPlayer(context, "player");
												int amount = IntegerArgumentType.getInteger(context, "amount");
												ScoreManager.addTotal(target, amount);
												int total = ScoreManager.data(target).total();
												context.getSource().sendSuccess(() -> Component.literal(
														"Added " + amount + " to " + target.getScoreboardName()
																+ "'s score (now " + total + "; admin changes never pay ingots)."), true);
												return total;
											})))));
		});
	}
}
