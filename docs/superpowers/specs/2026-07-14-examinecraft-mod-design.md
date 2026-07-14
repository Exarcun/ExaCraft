# ExaMinecraft — Fabric Community Mod

## Context

A gift mod for a Twitch chat community (EddyCarisma's channel). It adds 8 community-member NPCs with spawn eggs, trading, and catchphrases; 12 meme-themed items; 10 weapons; and 10 armor sets. It must be easy for chat members to install (Fabric Loader + Fabric API + one jar) and will run on a shared Fabric multiplayer server, so everything must work server-authoritative with vanilla-style client sync.

- **Target**: Minecraft 26.2, Fabric Loader 0.19.3, matching Fabric API (all newer than training data — exact versions MUST be pulled from https://fabricmc.net/develop / the Fabric meta API during scaffolding, not guessed).
- **Working dir**: `C:\Users\aapra\Videos\ExaMinecreft` (currently empty, not a git repo).
- **Design approved by user** in brainstorming (full Q&A in conversation). Placeholder policy: all textures, catchphrases, and trades are swappable data the user will replace later — keep them in obvious, isolated files.
- CLAUDE.md notes: no emojis in any code/resource files; ASCII only.

## Content Spec (approved)

### NPCs — 8, one shared engine
Single `CommunityNpcEntity` class driven by an `NpcProfile` record (id, display name, skin texture path, catchphrase list, trade list, gear pool). Characters: **EddyCarisma** (streamer), **Exarobot** (developer), **Mali**, **Sam**, **Josh**, **Dario**, **Illy**, **PeliSulPetto**.
- Player-style humanoid model, standard 64x64 skin PNG per NPC at `assets/examinecraft/textures/entity/npc/<name>.png` (placeholders now).
- Neutral AI: wanders, looks at players; retaliates when attacked (zombified-piglin-style anger), fighting with held item.
- Always-visible name tag.
- Right-click opens villager-style merchant screen; per-NPC placeholder trades defined in one table (each NPC wants different things, sells rare mod gear).
- Ambient catchphrases: every few minutes, broadcast a chat line to players within ~24 blocks; placeholder lines per NPC in the profile table.
- On spawn: random chance to hold a mod weapon and/or wear a mod armor set; drops carried gear on death.
- 8 spawn eggs with distinct colors.

### Items — 12
| Item | Type | Effect |
|---|---|---|
| Perc Potion | throwable splash | Slowness + strong Regeneration, 2 min |
| Opium Potion | throwable splash | Slowness + Resistance V (effectively invincible), 1 min |
| Narcan Potion | throwable splash | clears ALL status effects (milk-style) |
| Hash Pipe | consumable | Speed 5 min, deals 2 hearts damage on use |
| Joint | consumable | Speed 5 min |
| Lexotan | consumable | Slowness 1 min |
| Ket Vial | consumable | Nausea 2 min |
| LSD Blotter | consumable | Nausea 1 min; crafted from Paper + Opium Potion |
| Salvia Divinorum | herb ingredient | crafts into Salvia Bread (food) |
| Quaalude | consumable | spawns wolves tamed to user, hard cap 5 owned (re-use tops up to 5, never exceeds; tag mod-spawned wolves to count them) |
| DMT | consumable | shapeless recipe combining the 8 items above (Perc, Opium, Hash Pipe, Narcan, Salvia, LSD, Ket Vial, Quaalude); 1 min trip stack: Nausea + pulsing Darkness + short Levitation bursts + Slow Falling |
| Grappling Hook | tool | right-click: hook to aimed block, pull player there, negate fall damage until landing |

Plus Salvia Bread and a few invented crafting materials as needed (keep minimal).

### Weapons — 10
| Weapon | Type | Behavior |
|---|---|---|
| Perc 20 | melee (pill sword) | iron-sword-class stats |
| Perc 80 | melee (pill sword) | stronger + DOUBLE attack reach via entity interaction range attribute modifier |
| Needle | melee | fast dagger, Poison on hit |
| Ket Pot | melee | mid-tier sword |
| Golf Club | melee | on-hit: launch target flying (huge horizontal + upward velocity) |
| Baseball Bat | melee | same launch mechanic as Golf Club |
| Uzi | gun | hold right-click: rapid bullet projectiles, unlimited ammo |
| Sniper Rifle | gun | right-click toggles anchored stance (position locked, FOV zoom); left-click fires hitscan high-damage shot; unlimited ammo |
| Gamma Ray Gun | gun | hold right-click: continuous particle beam, high DPS raycast ticking |
| Ninja Star | ranged | stackable thrown projectile, bow-like damage |

### Armor — 10 sets (40 pieces)
One `ArmorMaterial` per set; recolored/textured per description; craftable.
1. **Swissman's** — netherite stats, red
2. **Benito's** — iron stats, black
3. **Twitch's** — iron stats, blue
4. **Mod's** — iron stats, green
5. **Exa's** — per-piece bonuses: Boots=Speed, Leggings=Jump Boost, Chest=Regeneration, Helmet=90% projectile damage reduction (damage event/mixin)
6. **Pig's** — iron stats, rose with tail on leggings texture
7. **Twitch Sub** — diamond stats, purple
8. **Gayman's** — diamond stats, rainbow
9. **Lazyman's** — leather stats, scrappy look
10. **Grilled Pizza** — diamond stats, pizza themed

### Obtaining & misc
- Everything: crafting recipes (invented, JSON), NPC trades, NPC drops.
- One custom creative tab "ExaMinecraft" containing everything.
- `en_us.json` lang for all names + flavor tooltips.

## Implementation Phases

Each phase compiles and is verified in-game before moving on.

### Phase 0 — Scaffold
1. `git init`; write the approved design spec to `docs/superpowers/specs/2026-07-14-examinecraft-mod-design.md`; commit.
2. Fetch correct versions from Fabric meta (`https://meta.fabricmc.net/v2/versions/...` and https://fabricmc.net/develop) for MC 26.2: loader 0.19.3, yarn mappings, loom, Fabric API, Java toolchain. Verify a JDK of the required version is installed (check `java -version`; guide install if missing).
3. Generate mod skeleton from the official Fabric example-mod template (mod id `examinecraft`), common + client entrypoints, mixin config.
4. Verify `gradlew build` and `gradlew runClient` work. Commit.

### Phase 1 — Items core
- Registration helper, creative tab, crafting materials, all simple consumables (Joint, Lexotan, Ket Vial, LSD Blotter, Hash Pipe, Salvia, Salvia Bread) via a consumable-builder (effects list + optional self-damage).
- Placeholder texture generation: small Python/Pillow script (in scratchpad) emitting 16x16 item PNGs with distinct silhouettes/palettes into resources; outputs committed.
- Item models, lang, recipes JSON.

### Phase 2 — Special items
- Thrown potion entity + Perc/Opium/Narcan splash items (Narcan = clear effects).
- DMT consumable (timed trip-stack handler: server tick scheduler applying Levitation bursts + Darkness pulses) + shapeless 8-ingredient recipe.
- Quaalude wolf spawner with owner-scoped cap of 5 (tag wolves with mod NBT/attachment, count before spawning).
- Grappling Hook: hook projectile, pull-velocity on player, fall-damage negation flag cleared on landing.

### Phase 3 — Melee weapons
- Perc 20/80 (reach attribute on Perc 80), Needle (poison on `postHit`), Ket Pot, Golf Club + Baseball Bat (shared launch-on-hit behavior).

### Phase 4 — Guns & ranged
- Small gun framework: bullet projectile entity, fire-rate handling via item use ticks.
- Uzi (auto-fire projectiles), Ninja Star (thrown, stackable), Gamma Ray Gun (per-tick raycast beam + particle line, both sides), Sniper Rifle (anchor toggle state on item/player, movement lock + FOV zoom on client, left-click hitscan via attack event while anchored).
- Multiplayer check: beams/particles visible to other clients (server-spawned particles).

### Phase 5 — Armor
- Armor material factory for the 9 standard sets (stats per tier, per-set textures: 40 item PNGs + layer_1/layer_2 worn textures via recolor script).
- Exa's set: equipped-tick attribute/effect application per piece + projectile damage reduction hook (Fabric damage event or `LivingEntity#damage` mixin — check what the 26.2 Fabric API offers).
- Recipes + lang.

### Phase 6 — NPCs
- `CommunityNpcEntity` (neutral, Angerable-style), `NpcProfile` table with all 8 characters, placeholder 64x64 skins, humanoid renderer with per-profile texture + always-visible name tag.
- Merchant trading (vanilla merchant screen, per-profile trade lists).
- Catchphrase broadcaster (random interval, radius-limited chat).
- Spawn-with-gear + death drops; 8 spawn eggs.

### Phase 7 — Polish & release
- `README.md` + `INSTALL.md` (2-minute chat-member guide: Fabric Loader installer -> drop Fabric API + jar in mods).
- Full verification (below), `gradlew build`, final jar in `build/libs/`.

## Key technical notes
- All gameplay logic server-side; client entrypoint only renderers/FOV/particles. Never trust client for damage.
- One registration helper per registry type; content defined declaratively in tables so the user can tweak names/trades/lines without touching logic.
- No emojis/non-ASCII anywhere in code or JSON.
- Reuse vanilla systems aggressively: vanilla status effects (no custom effects needed), vanilla merchant screen, vanilla armor rendering with custom materials.
- API details for 26.2 (registration signatures, ArmorMaterial shape, attribute names, damage events) may differ from training data — consult the bundled Fabric API javadocs/sources from the fetched version and the Fabric docs site before coding each phase.

## Verification
1. `gradlew build` passes clean.
2. `gradlew runClient`: creative tab shows all content; craft DMT + LSD + one armor set from recipes; use all 12 items (verify each effect, wolf cap, grapple with no fall damage); swing/fire all 10 weapons (reach diff between Perc 20/80, launch mechanic, sniper anchor toggle + zoom, beam DPS); wear all armor (Exa per-piece bonuses, 90% projectile reduction vs skeleton).
3. Spawn all 8 NPCs from eggs: name tags, trading screens with per-NPC trades, catchphrase broadcast, aggro on attack, gear drop on death.
4. `gradlew runServer` + connect dev client: repeat NPC + gun + potion checks in multiplayer; confirm second-client visibility of beams/particles/knockback (can use two dev client instances).
5. Confirm the built jar from `build/libs/` loads in a clean Fabric client install (the actual user experience).
