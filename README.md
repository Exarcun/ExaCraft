# ExaMinecraft

A community mod gifted to EddyCarisma's Twitch chat.

- **Minecraft**: 26.2 (Java Edition)
- **Loader**: Fabric Loader 0.19.3 + Fabric API 0.154.2+26.2
- **Java**: 25 (for building; players just need the regular launcher)

## What's inside

- **8 community NPCs** with spawn eggs: EddyCarisma, Exarobot, Mali, Sam, Josh,
  Dario, Illy, PeliSulPetto. Friendly until you hit them. Right-click to trade
  (each one wants different things and sells rare gear). They chat catchphrases,
  sometimes spawn armed, and drop whatever they carry.
- **12 items**: Perc / Opium / Narcan throwable potions, Hash Pipe, Joint,
  Lexotan, Ket Vial, LSD Blotter, Salvia (+ Salvia Bread), Quaalude (wolf pack,
  cap 5), DMT (craft it from the other 8 - full trip), Grappling Hook.
- **15 weapons**: Perc 20 / Perc 80 (double reach), Needle (poison), Ket Pot,
  Golf Club & Baseball Bat (send things flying), Uzi, Sniper Rifle (right-click
  to anchor + zoom, left-click to fire), Gamma Ray Gun (beam), Ninja Star,
  Casting Wand (lightning strike at your crosshair), Blink Crossbow (teleport
  to wherever the bolt lands), Orbital Laser (paint a target, the beam drops
  5 seconds later - big crater), Grilled Pizza (thrown, heals players it hits),
  Tattoo Gun (close-range auto, inks targets with Wither).
- **10 armor sets**: Swissman's (netherite, red), Benito's, Twitch, Mod's,
  Exa's (per-piece bonuses: speed boots, jump leggings, regen chest, 90%
  projectile-proof helmet), Pig's, Twitch Sub, Gayman's (rainbow), Lazyman's,
  Grilled Pizza.

Everything lives in the **ExaMinecraft** creative tab and is craftable in
survival (most armor = vanilla piece + dye/ingredient in a crafting grid).

## Installing (players)

See [INSTALL.md](INSTALL.md) - two minutes, three files.

## Building from source

```
gradlew build
```

The jar lands in `build/libs/examinecraft-<version>.jar`.
Requires JDK 25 (set `JAVA_HOME` accordingly).

Dev testing: `gradlew runClient` / `gradlew runServer`.

## Swapping placeholder art and text

All placeholders are plain files - no code changes needed:

| What | Where |
|---|---|
| NPC skins (64x64 player format) | `src/main/resources/assets/examinecraft/textures/entity/npc/<name>.png` |
| Item textures (16x16) | `src/main/resources/assets/examinecraft/textures/item/*.png` |
| Worn armor textures | `src/main/resources/assets/examinecraft/textures/entity/equipment/` |
| Catchphrases + trades | `src/main/java/com/examinecraft/npc/NpcProfiles.java` |
| Item/armor names | `src/main/resources/assets/examinecraft/lang/en_us.json` |

The `tools/*.py` scripts regenerate placeholder assets but never overwrite
files that already exist, so your real art is safe. Rebuild the jar after
swapping files.
