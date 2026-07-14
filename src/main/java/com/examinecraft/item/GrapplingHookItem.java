package com.examinecraft.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import com.examinecraft.systems.FallProtection;

/** Pulls the player to the block they are aiming at; the landing is fall-damage free. */
public class GrapplingHookItem extends Item {
	private static final double MAX_RANGE = 64.0;
	private static final double MAX_SPEED = 2.8;

	public GrapplingHookItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level instanceof ServerLevel && player instanceof ServerPlayer serverPlayer) {
			Vec3 from = player.getEyePosition();
			Vec3 to = from.add(player.getLookAngle().scale(MAX_RANGE));
			BlockHitResult hit = level.clip(new ClipContext(from, to,
					ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
			if (hit.getType() != HitResult.Type.BLOCK) {
				return InteractionResult.FAIL;
			}
			Vec3 pull = hit.getLocation().subtract(player.position());
			double distance = pull.length();
			Vec3 velocity = pull.normalize().scale(Math.min(MAX_SPEED, 0.7 + distance * 0.16)).add(0.0, 0.3, 0.0);
			player.setDeltaMovement(velocity);
			player.hurtMarked = true;
			FallProtection.protect(serverPlayer);
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 1.0F, 0.8F);
		}
		return InteractionResult.SUCCESS;
	}
}
