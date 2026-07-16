package com.examinecraft.systems;

import com.examinecraft.ExaMinecraft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Per-player home point set via /sethome, persisted on the player's save data. */
public final class PlayerHomes {
	public record Home(ResourceKey<Level> dimension, double x, double y, double z) {
		public static final Codec<Home> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(Home::dimension),
				Codec.DOUBLE.fieldOf("x").forGetter(Home::x),
				Codec.DOUBLE.fieldOf("y").forGetter(Home::y),
				Codec.DOUBLE.fieldOf("z").forGetter(Home::z)
		).apply(instance, Home::new));
	}

	public static final AttachmentType<Home> HOME = AttachmentRegistry.create(ExaMinecraft.id("home"),
			builder -> builder.persistent(Home.CODEC).copyOnDeath());

	private PlayerHomes() {
	}

	public static void initialize() {
		// Referencing the class from mod init is enough; the HOME attachment registers on class load.
	}
}
