# Currency & Global Score System — Design

Date: 2026-07-16
Target release: ExaCraft 1.3.0 (MC 26.2, Fabric)
Status: approved by user (this session)

## Goal

Two linked systems for the community SMP:

1. **Currency** — the existing Swiss Ingot item becomes the server currency with a
   controlled supply (no crafting recipe).
2. **Global score** — persistent per-player progression: small points for crafting,
   larger points for PvP kills, big points for boss kills. Score milestones pay out
   Swiss Ingots, linking the two systems.

Out of scope for v1 (handled manually or deferred):

- NPC shops: the user configures trades in-game via the EasyNPC UI. Swiss Ingot is a
  normal registered item, so no integration code is needed (verify once in-game).
- Purchasable perks (extra home slots, teleports, titles): deferred to a later release.
- A `/leaderboard` command: the sidebar covers it.

## Architecture (Approach A — approved)

Score state lives in a **persistent Fabric attachment** on the player, following the
existing `PlayerHomes` pattern (Codec-serialized, `copyOnDeath`). A vanilla scoreboard
objective is used **only for sidebar display** and is rebuilt from attachment data; the
attachment is always the source of truth and survives `/scoreboard` resets.

Rejected alternatives:

- Vanilla scoreboard as the store — cannot hold the anti-abuse metadata (crafted-type
  set, cooldown map, boss flags), which would split state across two mechanisms.
- Datapack/command blocks — cannot express crafting detection or cooldowns; prior
  command-block systems on this server caused tick problems.

## Components

### `systems/PlayerScore.java` — data model

`ScoreData` record stored in one attachment (`examinecraft:score`), persistent,
`copyOnDeath`:

| Field | Type | Purpose |
|---|---|---|
| `total` | int | Lifetime score. Gain-only through gameplay (no loss on death). |
| `craftPoints` | int | Category subtotal for `/score` breakdown. |
| `pvpPoints` | int | Category subtotal. |
| `bossPoints` | int | Category subtotal. |
| `craftedTypes` | Set&lt;String&gt; (item IDs) | First-craft-only rule. |
| `pvpCooldowns` | Map&lt;String UUID, long epochMillis&gt; | Last scored kill per victim; entries older than 30 min pruned on write. |
| `bossesKilled` | Set&lt;String&gt; (entity type IDs) | First-kill-per-boss rule. |
| `paidMilestones` | int | Number of 100-point milestones already paid out. |

### `systems/ScoreManager.java` — award pipeline

Single entry point `award(ServerPlayer, Category, int points)`:

1. Update attachment (total + category subtotal).
2. Sync the sidebar objective (see Display).
3. Milestone check: while `total / 100 > paidMilestones`, pay **5 Swiss Ingots** per
   milestone into inventory (drop at the player's feet if full), increment
   `paidMilestones`, message the player.

Admin corrections (`/score set`) clamp `paidMilestones` to `total / 100` so a manual
set can neither trigger a payout burst nor create a negative hole.

### `systems/ScoreEvents.java` — event hooks

| Event | Points | Rule |
|---|---|---|
| Player crafts an item | +1 | Only the first time each item **type** is ever crafted by that player. Repeat crafts: 0. "Crafting" = taking output from a crafting grid (inventory 2x2 or crafting table); furnace, stonecutter, smithing etc. do NOT award points in v1. |
| Player kills another player | +10 | Same victim scores 0 for 30 minutes (real time, epoch millis) after a scored kill. |
| Player kills Ender Dragon, Warden, or Wither | +100 | Once per boss type per player; repeat kills of that boss type: +10. |

Hooks: PvP/boss kills via Fabric's server after-kill combat event; crafting via the
take-item-from-crafting-result hook. **Exact 26.2 API names must be verified against
decompiled sources during implementation** (`gradlew genSources`) — never guessed.

Victim-side: death costs nothing (gain-only design; avoids punishing new players and
rewarding spawn-camping).

### Display

- Scoreboard objective `exa_score`, sidebar slot, showing only the **top 5** players.
  The mod adds/removes entries itself so the sidebar never grows beyond 5.
- Updated on every score change and on player join.
- **Boss first-kills broadcast server-wide** (e.g. "Nico slew the Warden! +100").
  Milestone payouts message only the earner.

### Commands (`command/ModCommands.java`)

- `/score` — own total, per-category breakdown, progress to next payout
  ("37/100 to next 5 ingots").
- `/score set <player> <amount>` and `/score add <player> <amount>` — op-only
  (permission level 2+).

### Currency supply change

- **Delete `data/examinecraft/recipe/swiss_ingot.json`.** Swiss Ingots then enter the
  economy only via milestone payouts and NPC trades configured by the user.
- Existing ingots in circulation remain valid; Swissman armor recipes are unchanged.

## Error handling

- Missing attachment = zero score (attachment default), never null-crash.
- Payout with full inventory drops the ingots at the player's position.
- Sidebar objective is created on server start if missing; entries rebuilt from online
  players' attachments (offline players keep their last-synced entry).
- Cooldown map pruned on write to keep serialized data small.

## Testing & verification

- `gradlew runServer` headless: registries load, "Loaded N recipes" (count drops by 1
  after recipe removal), no attachment codec errors.
- `gradlew runClient` to title screen (client split-source compile safety).
- In-game on live server after deploy: first-craft +1 / repeat-craft 0; PvP kill +10
  then 0 within 30 min; boss broadcast; milestone payout at 100; sidebar top-5; EasyNPC
  trade editor lists Swiss Ingot.

## Deployment

Ship as **1.3.0**: `gradlew build` → copy jar to PC mods folder and
`fengarden:minecraft/mods/` → `sudo systemctl restart examinecraft` → copy to repo
`dist/exacraft-latest.jar` → commit and push (installers download that raw URL).
