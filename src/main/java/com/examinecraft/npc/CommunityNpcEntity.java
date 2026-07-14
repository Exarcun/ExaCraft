package com.examinecraft.npc;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.examinecraft.item.ModArmor;
import com.examinecraft.item.ModWeapons;

/**
 * A community member: neutral until attacked, trades like a villager,
 * occasionally drops a catchphrase in chat, and may spawn holding mod gear
 * (guaranteed drops).
 */
public class CommunityNpcEntity extends PathfinderMob implements NeutralMob, Merchant {
	private static final UniformInt ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
	private static final int CATCHPHRASE_AVG_INTERVAL_TICKS = 1500;
	private static final double CATCHPHRASE_RADIUS = 24.0;

	private long persistentAngerEndTime = NeutralMob.NO_ANGER_END_TIME;
	@Nullable
	private EntityReference<LivingEntity> persistentAngerTarget;
	@Nullable
	private Player tradingPlayer;
	@Nullable
	private MerchantOffers offers;

	public CommunityNpcEntity(EntityType<? extends CommunityNpcEntity> type, Level level) {
		super(type, level);
	}

	public NpcProfile getProfile() {
		return ModNpcs.profileFor(this.getType());
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 40.0)
				.add(Attributes.MOVEMENT_SPEED, 0.3)
				.add(Attributes.ATTACK_DAMAGE, 4.0)
				.add(Attributes.FOLLOW_RANGE, 32.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
		this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, true));
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
			EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
		SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
		NpcProfile profile = this.getProfile();
		this.setCustomName(Component.literal(profile.displayName()));
		this.setCustomNameVisible(true);
		this.setPersistenceRequired();
		this.equipRandomGear();
		return result;
	}

	/** Chance to spawn holding a mod weapon and/or wearing a mod armor set; all guaranteed drops. */
	private void equipRandomGear() {
		if (this.random.nextFloat() < 0.6F) {
			List<net.minecraft.world.item.Item> weapons = List.of(
					ModWeapons.PERC_20, ModWeapons.PERC_80, ModWeapons.NEEDLE, ModWeapons.KET_POT,
					ModWeapons.GOLF_CLUB, ModWeapons.BASEBALL_BAT, ModWeapons.UZI, ModWeapons.NINJA_STAR);
			this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapons.get(this.random.nextInt(weapons.size()))));
			this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
		}
		if (this.random.nextFloat() < 0.4F) {
			ModArmor.ArmorSet set = ModArmor.ALL_SETS.get(this.random.nextInt(ModArmor.ALL_SETS.size()));
			this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(set.helmet()));
			this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(set.chestplate()));
			this.setGuaranteedDrop(EquipmentSlot.HEAD);
			this.setGuaranteedDrop(EquipmentSlot.CHEST);
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level() instanceof ServerLevel serverLevel) {
			this.updatePersistentAnger(serverLevel, true);
			if (this.isAlive() && !this.isAngry()
					&& this.random.nextInt(CATCHPHRASE_AVG_INTERVAL_TICKS) == 0) {
				this.broadcastCatchphrase(serverLevel);
			}
		}
	}

	private void broadcastCatchphrase(ServerLevel level) {
		List<String> phrases = this.getProfile().catchphrases();
		if (phrases.isEmpty()) {
			return;
		}
		String phrase = phrases.get(this.random.nextInt(phrases.size()));
		Component message = Component.literal("<" + this.getProfile().displayName() + "> " + phrase);
		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(this) < CATCHPHRASE_RADIUS * CATCHPHRASE_RADIUS) {
				player.sendSystemMessage(message);
			}
		}
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (this.isAlive() && !this.isAngry() && this.tradingPlayer == null) {
			if (!this.level().isClientSide() && !this.getOffers().isEmpty()) {
				this.setTradingPlayer(player);
				this.openTradingScreen(player, this.getDisplayName(), 1);
			}
			return InteractionResult.SUCCESS;
		}
		return super.mobInteract(player, hand);
	}

	// --- NeutralMob ---

	@Override
	public long getPersistentAngerEndTime() {
		return this.persistentAngerEndTime;
	}

	@Override
	public void setPersistentAngerEndTime(long endTime) {
		this.persistentAngerEndTime = endTime;
	}

	@Override
	public @Nullable EntityReference<LivingEntity> getPersistentAngerTarget() {
		return this.persistentAngerTarget;
	}

	@Override
	public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> persistentAngerTarget) {
		this.persistentAngerTarget = persistentAngerTarget;
	}

	@Override
	public void startPersistentAngerTimer() {
		this.setTimeToRemainAngry(ANGER_TIME.sample(this.random));
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		this.addPersistentAngerSaveData(output);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.readPersistentAngerSaveData(this.level(), input);
	}

	// --- Merchant ---

	@Override
	public void setTradingPlayer(@Nullable Player player) {
		this.tradingPlayer = player;
	}

	@Override
	public @Nullable Player getTradingPlayer() {
		return this.tradingPlayer;
	}

	@Override
	public MerchantOffers getOffers() {
		if (this.offers == null) {
			this.offers = new MerchantOffers();
			for (NpcProfile.TradeSpec spec : this.getProfile().trades()) {
				this.offers.add(new MerchantOffer(
						new ItemCost(spec.cost().get(), spec.costCount()),
						spec.resultStack(),
						spec.maxUses(), 2, 0.05F));
			}
		}
		return this.offers;
	}

	@Override
	public void overrideOffers(MerchantOffers offers) {
	}

	@Override
	public void notifyTrade(MerchantOffer offer) {
		offer.increaseUses();
	}

	@Override
	public void notifyTradeUpdated(ItemStack itemStack) {
	}

	@Override
	public int getVillagerXp() {
		return 0;
	}

	@Override
	public void overrideXp(int xp) {
	}

	@Override
	public boolean showProgressBar() {
		return false;
	}

	@Override
	public SoundEvent getNotifyTradeSound() {
		return SoundEvents.WANDERING_TRADER_YES;
	}

	@Override
	public boolean isClientSide() {
		return this.level().isClientSide();
	}

	@Override
	public boolean stillValid(Player player) {
		return this.tradingPlayer == player && this.isAlive() && player.distanceToSqr(this) < 64.0;
	}

	@Override
	public void die(net.minecraft.world.damagesource.DamageSource source) {
		this.setTradingPlayer(null);
		super.die(source);
	}
}
