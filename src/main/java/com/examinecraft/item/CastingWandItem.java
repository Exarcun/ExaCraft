package com.examinecraft.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Right-click: lightning strike + small blast wherever the crosshair points. */
public class CastingWandItem extends Item {
	private static final double RANGE = 64.0;
	private static final float BLAST_POWER = 1.5F;
	private static final int COOLDOWN_TICKS = 3 * 20;

	public CastingWandItem(Properties properties) {
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
			Vec3 pos = hit.getLocation();
			LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.TRIGGERED);
			if (bolt != null) {
				bolt.snapTo(pos);
				serverLevel.addFreshEntity(bolt);
			}
			serverLevel.explode(player, pos.x, pos.y, pos.z, BLAST_POWER, false, Level.ExplosionInteraction.TNT);
			player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
		}
		return InteractionResult.SUCCESS;
	}
}
