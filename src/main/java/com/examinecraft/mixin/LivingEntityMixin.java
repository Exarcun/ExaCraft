package com.examinecraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import com.examinecraft.item.ModArmor;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	private static final float EXA_HELMET_PROJECTILE_MULTIPLIER = 0.1F;

	@ModifyVariable(method = "hurtServer", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private float examinecraft$exaHelmetProjectileReduction(float damage, net.minecraft.server.level.ServerLevel level, DamageSource source) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (source.is(DamageTypeTags.IS_PROJECTILE)
				&& self.getItemBySlot(EquipmentSlot.HEAD).is(ModArmor.EXA.helmet())) {
			return damage * EXA_HELMET_PROJECTILE_MULTIPLIER;
		}
		return damage;
	}
}
