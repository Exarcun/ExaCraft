# Five New Weapons - Design Spec (2026-07-15)

Adds 5 weapons to ExaMinecraft (MC 26.2, Fabric). All follow existing conventions:
unlimited ammo, guns stack to 1, registered in `ModWeapons`, placeholder textures
via `tools/*.py` generators (skip existing PNGs), crafting recipes, ExaMinecraft
creative tab, lang entries in `en_us.json`.

## 1. Casting Wand (`casting_wand`)

- Item class `CastingWandItem`, stacks to 1.
- Right-click: raycast from eyes up to 64 blocks (blocks only, ClipContext COLLIDER).
- At hit position (server side): spawn vanilla `LightningBolt` + explosion power 1.5
  (block-breaking enabled). No effect if raycast misses within range.
- Cooldown: 60 ticks (3s) via `player.getCooldowns()`.

## 2. Blink Crossbow (`blink_crossbow`)

- Item class `BlinkCrossbowItem`, stacks to 1.
- Right-click fires `BlinkBoltEntity` (pattern: `BulletEntity`), speed ~3.5, no gravity
  drag concerns beyond default; despawns after ~64 blocks of travel (tick counter)
  with no teleport.
- On hit (block or entity): teleport the owner to the impact position, reset owner
  fall distance, no ender-pearl damage. Ender pearl sound + portal particles at both
  ends. Entity hit also deals 4 damage.
- Cooldown: 40 ticks (2s).

## 3. Orbital Laser (`orbital_laser`)

- Item class `OrbitalLaserItem`, stacks to 1.
- Right-click: raycast up to 128 blocks to a block. Spawns `OrbitalStrikeEntity`
  (invisible marker entity, no gravity, no collision) at the hit position.
- Marker counts down 100 ticks (5s):
  - Charge phase: rising column of DUST (red) + END_ROD particles, escalating
    pitch beacon/charge sound every 20 ticks.
  - At 0: vertical beam of particles from build height to ground (FLASH +
    END_ROD column), then explosion power 8 with fire, then discard.
- Rationale for entity over tick-scheduler: survives chunk logic, natural anchor
  for particles, saved/loaded for free.
- Cooldown: 300 ticks (15s).

## 4. Grilled Pizza (`grilled_pizza`)

- Item class `GrilledPizzaItem` (pattern: `NinjaStarItem`), stacks to 16, consumed
  on throw.
- `GrilledPizzaEntity` extends `ThrowableItemProjectile`, rendered as flat spinning
  item (same renderer approach as ninja star).
- On entity hit: 6 damage; if target is a Player, also Regeneration 3s (amplifier 0).
- On any hit: tomato-red splat (DUST) particles + player burp sound, discard.

## 5. Tattoo Gun (`tattoo_gun`)

- Item class `TattooGunItem` (pattern: `UziItem` hold-to-use), stacks to 1.
- While using, every tick (server): raycast 4 blocks ahead (entity clip); first
  living entity hit takes 1.5 damage (player attack damage source ok) and gets
  Wither 3s (amplifier 0) + SQUID_INK particles at hit point.
- Buzz sound (bee loop-ish, e.g. BEE_LOOP_AGGRESSIVE or NOTE_BLOCK_BIT) every few
  ticks while firing.
- No projectile entities. No cooldown (the range is the limiter).

## Shared infrastructure

- `ModEntityTypes`: register BLINK_BOLT, ORBITAL_STRIKE, GRILLED_PIZZA.
- Client init: renderers for blink bolt (reuse thrown-item or bullet renderer
  pattern), grilled pizza (spinning thrown item), orbital strike (NoopRenderer /
  empty - particles only).
- `ModWeapons`: 5 item registrations + recipes in data pack + textures + models
  + lang.
- Damage/energy numbers above are initial balance; tune later if needed.

## Verification

- `gradlew runServer` boots clean, "Loaded N recipes" includes the 5 new ones.
- `gradlew runClient` reaches title screen (validates models/renderers/assets).
