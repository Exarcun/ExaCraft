# Five New Weapons Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add 5 weapons to ExaMinecraft: Casting Wand (sky strike), Blink Crossbow (teleport bolt), Orbital Laser (delayed mega strike), Grilled Pizza (thrown), Tattoo Gun (close-range automatic).

**Architecture:** Each weapon is an `Item` subclass registered in `ModWeapons`, following existing patterns (`UziItem` hold-to-fire, `NinjaStarItem` throw, `GammaRayGunItem` raycast). Three new entities (`GrilledPizzaEntity`, `BlinkBoltEntity`, `OrbitalStrikeEntity`) registered in `ModEntityTypes` with client renderers in `ExaMinecraftClient`. Assets (placeholder textures, models, recipes, lang) generated/added in a final task.

**Tech Stack:** Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.154.2+26.2, Java 25, Mojang-blend mappings.

**Spec:** `docs/superpowers/specs/2026-07-15-five-weapons-design.md`

## Global Constraints

- MC 26.2 mappings quirks (all verified against decompiled sources):
  - Vanilla entity type constants live in `net.minecraft.world.entity.EntityTypes` (plural), e.g. `EntityTypes.LIGHTNING_BOLT`. The builder/generic class is `EntityType`.
  - Position an entity before spawning with `entity.snapTo(Vec3)`.
  - Explosion: `level.explode(@Nullable Entity source, double x, double y, double z, float r, boolean fire, Level.ExplosionInteraction interaction)`.
  - Cooldowns: `player.getCooldowns().addCooldown(ItemStack, int ticks)`.
  - Dust particles: `new DustParticleOptions(int rgbColor, float scale)`.
  - Entity save data: `readAdditionalSaveData(ValueInput)` / `addAdditionalSaveData(ValueOutput)`; `input.getIntOr("Name", default)`.
  - Identifier class: `net.minecraft.resources.Identifier` (not ResourceLocation) — but tasks below never need it directly; `ExaMinecraft.id(name)` wraps it.
- Every Gradle invocation needs: `$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot'` (PowerShell) first.
- **No unit-test infrastructure exists in this project** (Minecraft registry code cannot run outside the game). The test cycle per task is: `gradlew compileJava` must succeed. Final task verifies at runtime: `gradlew runServer` boots and loads recipes, `gradlew runClient` reaches the title screen.
- No emojis anywhere in code or scripts. ASCII only.
- Damage/tuning values come from the spec; do not invent different ones.
- Commit after every task.

---

### Task 1: Grilled Pizza

**Files:**
- Create: `src/main/java/com/examinecraft/entity/GrilledPizzaEntity.java`
- Create: `src/main/java/com/examinecraft/item/GrilledPizzaItem.java`
- Modify: `src/main/java/com/examinecraft/entity/ModEntityTypes.java` (add GRILLED_PIZZA)
- Modify: `src/main/java/com/examinecraft/item/ModWeapons.java` (add GRILLED_PIZZA item)
- Modify: `src/client/java/com/examinecraft/client/ExaMinecraftClient.java` (renderer)

**Interfaces:**
- Consumes: `ModItems.register`, `ModEntityTypes.register` (existing private helper), `ThrowableItemProjectile`.
- Produces: `ModWeapons.GRILLED_PIZZA` (Item), `ModEntityTypes.GRILLED_PIZZA` (EntityType<GrilledPizzaEntity>). Task 6 references item id `grilled_pizza`.

- [ ] **Step 1: Create GrilledPizzaEntity**

```java
package com.examinecraft.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import com.examinecraft.item.ModWeapons;

public class GrilledPizzaEntity extends ThrowableItemProjectile {
	private static final float DAMAGE = 6.0F;
	private static final int REGEN_TICKS = 3 * 20;
	private static final DustParticleOptions TOMATO_SPLAT = new DustParticleOptions(0xC63D2F, 1.2F);

	public GrilledPizzaEntity(EntityType<? extends GrilledPizzaEntity> type, Level level) {
		super(type, level);
	}

	public GrilledPizzaEntity(Level level, LivingEntity owner, net.minecraft.world.item.ItemStack stack) {
		super(ModEntityTypes.GRILLED_PIZZA, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return ModWeapons.GRILLED_PIZZA;
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 3) {
			for (int i = 0; i < 8; i++) {
				this.level().addParticle(TOMATO_SPLAT, this.getX(), this.getY(), this.getZ(),
						this.random.nextGaussian() * 0.1, this.random.nextGaussian() * 0.1, this.random.nextGaussian() * 0.1);
			}
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
			hitResult.getEntity().hurtServer(serverLevel, this.damageSources().thrown(this, this.getOwner()), DAMAGE);
			if (hitResult.getEntity() instanceof Player player) {
				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_TICKS, 0));
			}
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (!this.level().isClientSide()) {
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
					SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.8F, 1.0F);
			this.level().broadcastEntityEvent(this, (byte) 3);
			this.discard();
		}
	}
}
```

- [ ] **Step 2: Create GrilledPizzaItem**

```java
package com.examinecraft.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.examinecraft.entity.GrilledPizzaEntity;

/** Right-click to fling a spinning pizza. */
public class GrilledPizzaItem extends Item {
	private static final float SPEED = 1.5F;

	public GrilledPizzaItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6F, 0.8F);
		if (level instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileFromRotation(GrilledPizzaEntity::new, serverLevel, stack, player, 0.0F, SPEED, 1.0F);
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}
}
```

- [ ] **Step 3: Register entity type in ModEntityTypes**

Add after the NINJA_STAR field:

```java
	public static final EntityType<GrilledPizzaEntity> GRILLED_PIZZA = register("grilled_pizza",
			EntityType.Builder.<GrilledPizzaEntity>of(GrilledPizzaEntity::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.25F, 0.25F)
					.clientTrackingRange(4)
					.updateInterval(10));
```

- [ ] **Step 4: Register item in ModWeapons**

Add in the "Guns & ranged" section:

```java
	public static final Item GRILLED_PIZZA = ModItems.register("grilled_pizza", GrilledPizzaItem::new,
			new Item.Properties().stacksTo(16));
```

- [ ] **Step 5: Register renderer in ExaMinecraftClient**

Add after the NINJA_STAR line in `onInitializeClient`:

```java
		EntityRenderers.register(ModEntityTypes.GRILLED_PIZZA, ThrownItemRenderer::new);
```

- [ ] **Step 6: Compile**

Run: `$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot'; .\gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```
git add -A src
git commit -m "Add Grilled Pizza thrown weapon"
```

---

### Task 2: Casting Wand

**Files:**
- Create: `src/main/java/com/examinecraft/item/CastingWandItem.java`
- Modify: `src/main/java/com/examinecraft/item/ModWeapons.java`

**Interfaces:**
- Consumes: `ModItems.register`.
- Produces: `ModWeapons.CASTING_WAND` (Item). Task 6 references item id `casting_wand`.

- [ ] **Step 1: Create CastingWandItem**

```java
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
```

- [ ] **Step 2: Register in ModWeapons**

```java
	public static final Item CASTING_WAND = ModItems.register("casting_wand", CastingWandItem::new,
			new Item.Properties().stacksTo(1));
```

- [ ] **Step 3: Compile**

Run: `$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot'; .\gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```
git add -A src
git commit -m "Add Casting Wand sky-strike weapon"
```

---

### Task 3: Blink Crossbow

**Files:**
- Create: `src/main/java/com/examinecraft/entity/BlinkBoltEntity.java`
- Create: `src/main/java/com/examinecraft/item/BlinkCrossbowItem.java`
- Modify: `src/main/java/com/examinecraft/entity/ModEntityTypes.java`
- Modify: `src/main/java/com/examinecraft/item/ModWeapons.java` (item + hidden bolt item)
- Modify: `src/client/java/com/examinecraft/client/ExaMinecraftClient.java`

**Interfaces:**
- Consumes: `ModItems.register`, `ModItems.registerHidden` (for the technical bolt render item, same pattern as `BULLET`).
- Produces: `ModWeapons.BLINK_CROSSBOW`, `ModWeapons.BLINK_BOLT` (hidden Item), `ModEntityTypes.BLINK_BOLT`. Task 6 references item ids `blink_crossbow` and `blink_bolt`.

- [ ] **Step 1: Create BlinkBoltEntity**

Teleports the shooter to wherever it lands. Block hits teleport to the adjacent face so the player is never inside a wall; entity hits also deal damage.

```java
package com.examinecraft.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import com.examinecraft.item.ModWeapons;

public class BlinkBoltEntity extends ThrowableItemProjectile {
	private static final float DAMAGE = 4.0F;
	private static final int MAX_LIFETIME_TICKS = 80;

	public BlinkBoltEntity(EntityType<? extends BlinkBoltEntity> type, Level level) {
		super(type, level);
	}

	public BlinkBoltEntity(Level level, LivingEntity owner, net.minecraft.world.item.ItemStack stack) {
		super(ModEntityTypes.BLINK_BOLT, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return ModWeapons.BLINK_BOLT;
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide() && this.tickCount > MAX_LIFETIME_TICKS) {
			this.discard();
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		if (this.level() instanceof ServerLevel serverLevel) {
			hitResult.getEntity().hurtServer(serverLevel, this.damageSources().thrown(this, this.getOwner()), DAMAGE);
			this.teleportOwner(serverLevel, hitResult.getEntity().position());
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult hitResult) {
		super.onHitBlock(hitResult);
		if (this.level() instanceof ServerLevel serverLevel) {
			this.teleportOwner(serverLevel, Vec3.atBottomCenterOf(hitResult.getBlockPos().relative(hitResult.getDirection())));
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (!this.level().isClientSide()) {
			this.discard();
		}
	}

	private void teleportOwner(ServerLevel serverLevel, Vec3 target) {
		if (!(this.getOwner() instanceof LivingEntity owner) || owner.level() != serverLevel) {
			return;
		}
		serverLevel.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
				SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
		serverLevel.sendParticles(ParticleTypes.PORTAL, owner.getX(), owner.getY() + 1.0, owner.getZ(),
				20, 0.3, 0.5, 0.3, 0.1);
		owner.teleportTo(target.x, target.y, target.z);
		owner.resetFallDistance();
		serverLevel.playSound(null, target.x, target.y, target.z,
				SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.2F);
		serverLevel.sendParticles(ParticleTypes.PORTAL, target.x, target.y + 1.0, target.z,
				20, 0.3, 0.5, 0.3, 0.1);
	}
}
```

- [ ] **Step 2: Create BlinkCrossbowItem**

```java
package com.examinecraft.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.examinecraft.entity.BlinkBoltEntity;

/** Fires a bolt that teleports you to wherever it lands. */
public class BlinkCrossbowItem extends Item {
	private static final float SPEED = 3.5F;
	private static final int COOLDOWN_TICKS = 2 * 20;

	public BlinkCrossbowItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
		if (level instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileFromRotation(BlinkBoltEntity::new, serverLevel, stack, player, 0.0F, SPEED, 0.0F);
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
		return InteractionResult.SUCCESS;
	}
}
```

- [ ] **Step 3: Register entity type**

Add in `ModEntityTypes` after GRILLED_PIZZA:

```java
	public static final EntityType<BlinkBoltEntity> BLINK_BOLT = register("blink_bolt",
			EntityType.Builder.<BlinkBoltEntity>of(BlinkBoltEntity::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.15F, 0.15F)
					.clientTrackingRange(4)
					.updateInterval(10));
```

- [ ] **Step 4: Register items in ModWeapons**

```java
	public static final Item BLINK_CROSSBOW = ModItems.register("blink_crossbow", BlinkCrossbowItem::new,
			new Item.Properties().stacksTo(1));
```

And next to the BULLET technical item:

```java
	/** Technical item: what the blink bolt projectile renders as. Not craftable, not in the tab. */
	public static final Item BLINK_BOLT = ModItems.registerHidden("blink_bolt", Item::new, new Item.Properties());
```

- [ ] **Step 5: Register renderer**

```java
		EntityRenderers.register(ModEntityTypes.BLINK_BOLT, ThrownItemRenderer::new);
```

- [ ] **Step 6: Compile**

Run: `$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot'; .\gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```
git add -A src
git commit -m "Add Blink Crossbow teleport weapon"
```

---

### Task 4: Tattoo Gun

**Files:**
- Create: `src/main/java/com/examinecraft/item/TattooGunItem.java`
- Modify: `src/main/java/com/examinecraft/item/ModWeapons.java`

**Interfaces:**
- Consumes: `ModItems.register`, `ProjectileUtil.getHitResultOnViewVector` (as in `GammaRayGunItem`).
- Produces: `ModWeapons.TATTOO_GUN`. Task 6 references item id `tattoo_gun`.

- [ ] **Step 1: Create TattooGunItem**

Hold-to-use like the Uzi, but no projectile: a 4-block raycast each tick. Targets take rapid damage and get "inked" (Wither + squid ink particles).

```java
package com.examinecraft.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Hold right-click: close-range needle buzz that inks targets. */
public class TattooGunItem extends Item {
	private static final double RANGE = 4.0;
	private static final float DAMAGE_PER_TICK = 1.5F;
	private static final int WITHER_TICKS = 3 * 20;
	private static final int SOUND_INTERVAL_TICKS = 4;

	public TattooGunItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity user) {
		return 72000;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.NONE;
	}

	@Override
	public void onUseTick(Level level, LivingEntity user, ItemStack stack, int ticksRemaining) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		if (ticksRemaining % SOUND_INTERVAL_TICKS == 0) {
			serverLevel.playSound(null, user.getX(), user.getY(), user.getZ(),
					SoundEvents.BEE_LOOP_AGGRESSIVE, SoundSource.PLAYERS, 0.4F, 1.8F);
		}
		HitResult hit = ProjectileUtil.getHitResultOnViewVector(user,
				entity -> entity instanceof LivingEntity && entity != user && entity.isPickable(), RANGE);
		if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity target) {
			DamageSource source = user instanceof Player player
					? user.damageSources().playerAttack(player)
					: user.damageSources().mobAttack(user);
			target.hurtServer(serverLevel, source, DAMAGE_PER_TICK);
			target.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_TICKS, 0));
			Vec3 point = hit.getLocation();
			serverLevel.sendParticles(ParticleTypes.SQUID_INK, point.x, point.y, point.z, 3, 0.1, 0.1, 0.1, 0.02);
		}
	}
}
```

- [ ] **Step 2: Register in ModWeapons**

```java
	public static final Item TATTOO_GUN = ModItems.register("tattoo_gun", TattooGunItem::new,
			new Item.Properties().stacksTo(1));
```

- [ ] **Step 3: Compile**

Run: `$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot'; .\gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```
git add -A src
git commit -m "Add Tattoo Gun close-range automatic"
```

---

### Task 5: Orbital Laser

**Files:**
- Create: `src/main/java/com/examinecraft/entity/OrbitalStrikeEntity.java`
- Create: `src/main/java/com/examinecraft/item/OrbitalLaserItem.java`
- Modify: `src/main/java/com/examinecraft/entity/ModEntityTypes.java`
- Modify: `src/main/java/com/examinecraft/item/ModWeapons.java`
- Modify: `src/client/java/com/examinecraft/client/ExaMinecraftClient.java` (NoopRenderer)

**Interfaces:**
- Consumes: `SniperSeatEntity` pattern for a minimal marker entity (defineSynchedData/read/addAdditionalSaveData/hurtServer/isPickable overrides).
- Produces: `ModWeapons.ORBITAL_LASER`, `ModEntityTypes.ORBITAL_STRIKE`. Task 6 references item id `orbital_laser`.

- [ ] **Step 1: Create OrbitalStrikeEntity**

Invisible marker that charges for 100 ticks (rising red column, escalating beacon pings), then fires: white particle beam + power-8 fire explosion, then discards. Fuse persists across save/load.

```java
package com.examinecraft.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Invisible countdown marker for the orbital laser. Charges for 5 seconds
 * with a rising red warning column, then drops the beam.
 */
public class OrbitalStrikeEntity extends Entity {
	public static final int FUSE_TICKS = 5 * 20;
	private static final float BLAST_POWER = 8.0F;
	private static final int CHARGE_COLUMN_HEIGHT = 24;
	private static final int BEAM_HEIGHT = 80;
	private static final DustParticleOptions WARNING_RED = new DustParticleOptions(0xFF2020, 1.5F);

	private int fuse = FUSE_TICKS;

	public OrbitalStrikeEntity(EntityType<?> type, Level level) {
		super(type, level);
		this.noPhysics = true;
	}

	@Override
	public void tick() {
		super.tick();
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		this.fuse--;
		if (this.fuse > 0) {
			for (int i = 0; i < CHARGE_COLUMN_HEIGHT; i += 2) {
				serverLevel.sendParticles(WARNING_RED, this.getX(), this.getY() + i, this.getZ(), 2, 0.3, 0.5, 0.3, 0.0);
			}
			serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + CHARGE_COLUMN_HEIGHT, this.getZ(),
					1, 0.1, 0.1, 0.1, 0.0);
			if (this.fuse % 20 == 0) {
				float pitch = 0.8F + (FUSE_TICKS - this.fuse) * 0.012F;
				serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
						SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 2.0F, pitch);
			}
			return;
		}
		for (int i = 0; i < BEAM_HEIGHT; i++) {
			serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + i, this.getZ(),
					3, 0.2, 0.4, 0.2, 0.01);
		}
		serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
		serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
				SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 4.0F, 0.6F);
		serverLevel.explode(this, this.getX(), this.getY(), this.getZ(), BLAST_POWER, true, Level.ExplosionInteraction.TNT);
		this.discard();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		this.fuse = input.getIntOr("Fuse", FUSE_TICKS);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putInt("Fuse", this.fuse);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		return false;
	}

	@Override
	public boolean isPickable() {
		return false;
	}
}
```

- [ ] **Step 2: Create OrbitalLaserItem**

```java
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
```

- [ ] **Step 3: Register entity type**

```java
	public static final EntityType<OrbitalStrikeEntity> ORBITAL_STRIKE = register("orbital_strike",
			EntityType.Builder.<OrbitalStrikeEntity>of(OrbitalStrikeEntity::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.5F, 0.5F)
					.clientTrackingRange(10)
					.updateInterval(10));
```

- [ ] **Step 4: Register item in ModWeapons**

```java
	public static final Item ORBITAL_LASER = ModItems.register("orbital_laser", OrbitalLaserItem::new,
			new Item.Properties().stacksTo(1));
```

- [ ] **Step 5: Register renderer**

```java
		EntityRenderers.register(ModEntityTypes.ORBITAL_STRIKE, NoopRenderer::new);
```

- [ ] **Step 6: Compile**

Run: `$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot'; .\gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```
git add -A src
git commit -m "Add Orbital Laser delayed strike weapon"
```

---

### Task 6: Assets, recipes, lang, and runtime verification

**Files:**
- Modify: `tools/gen_textures.py` (6 new placeholder textures)
- Modify: `tools/gen_item_models.py` (HANDHELD set)
- Create: `src/main/resources/data/examinecraft/recipe/casting_wand.json`, `blink_crossbow.json`, `orbital_laser.json`, `grilled_pizza.json`, `tattoo_gun.json`
- Modify: `src/main/resources/assets/examinecraft/lang/en_us.json`
- Generated: textures, `models/item/*.json`, `items/*.json` for the 6 new ids

**Interfaces:**
- Consumes: item ids from Tasks 1-5: `grilled_pizza`, `casting_wand`, `blink_crossbow`, `blink_bolt` (hidden), `tattoo_gun`, `orbital_laser`.
- Produces: complete asset set; mod is playable.

- [ ] **Step 1: Add texture functions to tools/gen_textures.py**

Follow the file's existing style (16x16 PIL drawings, `save(img, "name")` at the bottom where the other items are saved). Simple recognizable placeholders — the user repaints these later. Example shapes (adjust freely to match the file's helper conventions):

```python
def casting_wand():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    d.line([(3, 12), (11, 4)], fill=(101, 67, 33, 255), width=2)  # shaft
    d.ellipse([9, 1, 14, 6], fill=(120, 200, 255, 255), outline=(40, 40, 40, 255))  # crystal tip
    d.point((11, 3), fill=(255, 255, 255, 255))
    return img


def blink_crossbow():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    d.line([(2, 8), (13, 8)], fill=(101, 67, 33, 255), width=2)  # stock
    d.arc([3, 2, 12, 13], 200, 340, fill=(150, 60, 200, 255), width=2)  # purple bow
    d.line([(8, 3), (8, 12)], fill=(220, 220, 220, 255))  # string/bolt
    return img


def orbital_laser():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    d.rectangle([4, 4, 11, 13], fill=(90, 90, 100, 255), outline=(40, 40, 40, 255))  # remote body
    d.rectangle([6, 6, 9, 8], fill=(255, 40, 40, 255))  # red button
    d.line([(7, 1), (7, 4)], fill=(180, 180, 190, 255))  # antenna
    return img


def grilled_pizza():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    d.ellipse([2, 2, 13, 13], fill=(222, 170, 80, 255), outline=(120, 70, 20, 255))  # crust
    d.ellipse([4, 4, 11, 11], fill=(200, 60, 40, 255))  # sauce
    d.point((6, 6), fill=(255, 230, 130, 255))  # cheese specks
    d.point((9, 8), fill=(255, 230, 130, 255))
    d.point((7, 9), fill=(255, 230, 130, 255))
    return img


def tattoo_gun():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    d.rectangle([4, 5, 11, 9], fill=(60, 60, 70, 255), outline=(30, 30, 30, 255))  # body
    d.line([(11, 7), (14, 7)], fill=(200, 200, 210, 255))  # needle
    d.rectangle([6, 9, 8, 13], fill=(60, 60, 70, 255))  # grip
    return img


def blink_bolt():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    d.line([(4, 11), (11, 4)], fill=(150, 60, 200, 255), width=2)  # purple bolt
    d.point((12, 3), fill=(255, 255, 255, 255))
    return img
```

And the matching save calls where the file saves the other textures:

```python
save(casting_wand(), "casting_wand")
save(blink_crossbow(), "blink_crossbow")
save(orbital_laser(), "orbital_laser")
save(grilled_pizza(), "grilled_pizza")
save(tattoo_gun(), "tattoo_gun")
save(blink_bolt(), "blink_bolt")
```

- [ ] **Step 2: Run texture generator**

Run: `python tools/gen_textures.py`
Expected: `wrote ...` lines for exactly the 6 new PNGs (existing PNGs are skipped — never use `--force`).

- [ ] **Step 3: Add handheld entries and run model generator**

In `tools/gen_item_models.py`, add to `HANDHELD`:

```python
    "casting_wand",
    "blink_crossbow",
    "orbital_laser",
    "tattoo_gun",
```

(`grilled_pizza` and `blink_bolt` stay flat/generated.)

Run: `python tools/gen_item_models.py`
Expected: `wrote ...` lines for models/item and items JSON of the 6 new ids.

- [ ] **Step 4: Create the 5 recipe files**

`src/main/resources/data/examinecraft/recipe/casting_wand.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "key": {
    "d": "minecraft:diamond",
    "b": "minecraft:blaze_rod"
  },
  "pattern": [
    "  d",
    " b ",
    "b  "
  ],
  "result": {
    "id": "examinecraft:casting_wand"
  }
}
```

`src/main/resources/data/examinecraft/recipe/blink_crossbow.json`:

```json
{
  "type": "minecraft:crafting_shapeless",
  "ingredients": [
    "minecraft:crossbow",
    "minecraft:ender_pearl",
    "minecraft:ender_pearl"
  ],
  "result": {
    "id": "examinecraft:blink_crossbow"
  }
}
```

`src/main/resources/data/examinecraft/recipe/orbital_laser.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "key": {
    "g": "minecraft:glowstone",
    "i": "minecraft:iron_block",
    "r": "minecraft:redstone_block",
    "d": "minecraft:diamond_block"
  },
  "pattern": [
    "ggg",
    "idi",
    "iri"
  ],
  "result": {
    "id": "examinecraft:orbital_laser"
  }
}
```

`src/main/resources/data/examinecraft/recipe/grilled_pizza.json`:

```json
{
  "type": "minecraft:crafting_shapeless",
  "ingredients": [
    "examinecraft:pizza_slice",
    "minecraft:coal"
  ],
  "result": {
    "id": "examinecraft:grilled_pizza",
    "count": 4
  }
}
```

`src/main/resources/data/examinecraft/recipe/tattoo_gun.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "key": {
    "i": "minecraft:iron_ingot",
    "n": "examinecraft:needle",
    "r": "minecraft:redstone"
  },
  "pattern": [
    "ii ",
    "irn"
  ],
  "result": {
    "id": "examinecraft:tattoo_gun"
  }
}
```

- [ ] **Step 5: Add lang entries**

In `src/main/resources/assets/examinecraft/lang/en_us.json`, alongside the other item entries (match the file's tab indentation):

```json
	"item.examinecraft.casting_wand": "Casting Wand",
	"item.examinecraft.blink_crossbow": "Blink Crossbow",
	"item.examinecraft.orbital_laser": "Orbital Laser",
	"item.examinecraft.grilled_pizza": "Grilled Pizza",
	"item.examinecraft.tattoo_gun": "Tattoo Gun",
	"item.examinecraft.blink_bolt": "Blink Bolt",
	"entity.examinecraft.grilled_pizza": "Grilled Pizza",
	"entity.examinecraft.blink_bolt": "Blink Bolt",
	"entity.examinecraft.orbital_strike": "Orbital Strike"
```

(The project gives hidden items and projectile entities lang entries too — see the existing `item.examinecraft.bullet` / `entity.examinecraft.bullet` pair; mirror that.)

- [ ] **Step 6: Runtime verification - server**

Run: `$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot'; .\gradlew runServer` (background, then stop it)
Expected: boots to "Done"; the "Loaded N recipes" count went up by 5 vs. the previous run; no `examinecraft` errors in the log.

- [ ] **Step 7: Runtime verification - client**

Run: `$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot'; .\gradlew runClient` (background, wait for title screen, then stop)
Expected: reaches title screen; no missing-model or renderer errors for the 6 new ids in the log.

- [ ] **Step 8: Commit**

```
git add -A
git commit -m "Add assets, recipes, and lang for the five new weapons"
```
