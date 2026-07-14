package com.examinecraft.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import com.examinecraft.entity.ModEntityTypes;
import com.examinecraft.entity.SniperSeatEntity;

/**
 * Right-click toggles the anchored sniper stance (riding an invisible seat).
 * While anchored, left-click fires a high-damage hitscan shot (client sends
 * a SniperFirePayload; see ModNetworking).
 */
public class SniperRifleItem extends Item {
	private static final double RANGE = 128.0;
	private static final float DAMAGE = 20.0F;
	private static final int SHOT_COOLDOWN_TICKS = 20;

	public SniperRifleItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level instanceof ServerLevel serverLevel) {
			if (player.getVehicle() instanceof SniperSeatEntity seat) {
				player.stopRiding();
				seat.discard();
			} else if (player.onGround()) {
				SniperSeatEntity seat = ModEntityTypes.SNIPER_SEAT.create(serverLevel, EntitySpawnReason.TRIGGERED);
				if (seat != null) {
					seat.snapTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
					serverLevel.addFreshEntity(seat);
					player.startRiding(seat, true, true);
					serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
				}
			}
		}
		return InteractionResult.SUCCESS;
	}

	/** Server-side handler for the client fire packet. Validates stance and cooldown. */
	public static void handleFire(ServerPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (!(stack.getItem() instanceof SniperRifleItem)
				|| !(player.getVehicle() instanceof SniperSeatEntity)
				|| player.getCooldowns().isOnCooldown(stack)
				|| !(player.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		player.getCooldowns().addCooldown(stack, SHOT_COOLDOWN_TICKS);

		HitResult hit = ProjectileUtil.getHitResultOnViewVector(player,
				entity -> entity instanceof LivingEntity && entity != player && entity.isPickable(), RANGE);
		Vec3 from = player.getEyePosition();
		Vec3 end = hit.getType() == HitResult.Type.MISS
				? from.add(player.getLookAngle().scale(RANGE))
				: hit.getLocation();

		Vec3 direction = end.subtract(from);
		double length = direction.length();
		Vec3 step = direction.normalize();
		for (double d = 2.0; d < length; d += 2.0) {
			Vec3 point = from.add(step.scale(d));
			serverLevel.sendParticles(ParticleTypes.CRIT, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
		}

		if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living) {
			living.hurtServer(serverLevel, player.damageSources().indirectMagic(player, player), DAMAGE);
		}
		serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.6F, 1.8F);
	}
}
