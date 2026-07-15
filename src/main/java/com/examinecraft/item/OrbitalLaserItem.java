package com.examinecraft.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import com.examinecraft.entity.ModEntityTypes;
import com.examinecraft.entity.OrbitalStrikeEntity;

/** Right-click: paint a target up to 128 blocks away; the beam lands 5 seconds later. */
public class OrbitalLaserItem extends Item {
	private static final double RANGE = 128.0;
	private static final int COOLDOWN_TICKS = 15 * 20;

	public OrbitalLaserItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level instanceof ServerLevel serverLevel) {
			Vec3 from = player.getEyePosition();
			Vec3 to = from.add(player.getLookAngle().scale(RANGE));
			BlockHitResult hit = serverLevel.clip(new ClipContext(from, to,
					ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
			if (hit.getType() == HitResult.Type.MISS) {
				return InteractionResult.FAIL;
			}
			OrbitalStrikeEntity strike = ModEntityTypes.ORBITAL_STRIKE.create(serverLevel, EntitySpawnReason.TRIGGERED);
			if (strike != null) {
				strike.snapTo(hit.getLocation());
				serverLevel.addFreshEntity(strike);
				serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);
				player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
			}
		}
		return InteractionResult.SUCCESS;
	}
}
