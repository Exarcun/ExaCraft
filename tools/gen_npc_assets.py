# Generates NPC placeholder assets:
#   - 64x64 player-format skins at textures/entity/npc/<id>.png (repaint these with real skins!)
#   - spawn egg item textures (recolor of the vanilla pig egg)
#   - lang entries for entities and spawn eggs
# Existing PNGs are skipped so real skins are never overwritten.
# Run: python tools/gen_npc_assets.py

import io
import json
import os
import zipfile

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "examinecraft")
CLIENT_JAR = os.path.expanduser(
    os.path.join("~", ".gradle", "caches", "fabric-loom", "26.2", "minecraft-client-only.jar"))

# id, display name, egg tint, (skin tone, hair, shirt, pants)
NPCS = [
    ("eddycarisma", "EddyCarisma", (211, 47, 47), ((235, 190, 160), (60, 40, 25), (200, 50, 50), (40, 40, 60))),
    ("exarobot", "Exarobot", (110, 110, 126), ((150, 155, 165), (90, 95, 105), (70, 75, 90), (50, 55, 65))),
    ("mali", "Mali", (142, 68, 173), ((225, 180, 150), (25, 20, 20), (140, 70, 180), (35, 35, 45))),
    ("sam", "Sam", (230, 126, 34), ((240, 200, 170), (170, 110, 40), (230, 130, 40), (60, 50, 40))),
    ("josh", "Josh", (39, 174, 96), ((220, 175, 145), (50, 35, 25), (40, 170, 90), (45, 45, 55))),
    ("dario", "Dario", (41, 128, 185), ((230, 185, 155), (30, 25, 20), (45, 125, 185), (55, 45, 40))),
    ("illy", "Illy", (240, 98, 146), ((240, 195, 170), (200, 120, 60), (240, 100, 150), (60, 45, 60))),
    ("pelisulpetto", "PeliSulPetto", (121, 85, 72), ((225, 180, 150), (40, 30, 22), (120, 85, 70), (35, 30, 28))),
]


def make_skin(skin_tone, hair, shirt, pants):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    def box(x0, y0, x1, y1, color):
        d.rectangle([x0, y0, x1 - 1, y1 - 1], fill=color + (255,))

    # Head (all faces) + hair cap + face details
    box(0, 0, 32, 16, skin_tone)
    box(8, 0, 24, 8, hair)          # top of head
    box(8, 8, 24, 10, hair)         # hair line on front/sides upper rows
    # eyes on front face (front face is x 8..16, y 8..16)
    d.point((10, 12), fill=(255, 255, 255, 255))
    d.point((11, 12), fill=(60, 60, 120, 255))
    d.point((13, 12), fill=(60, 60, 120, 255))
    d.point((14, 12), fill=(255, 255, 255, 255))
    d.point((12, 14), fill=(170, 110, 90, 255))  # mouth hint

    # Body
    box(16, 16, 40, 32, shirt)
    # Right arm / Left arm: sleeve top, skin hands
    box(40, 16, 56, 32, shirt)
    box(40, 26, 56, 32, skin_tone)
    box(32, 48, 48, 64, shirt)
    box(32, 58, 48, 64, skin_tone)
    # Right leg / Left leg
    box(0, 16, 16, 32, pants)
    box(16, 48, 32, 64, pants)
    return img


def tint_egg(base, tint):
    img = base.convert("RGBA")
    px = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            lum = 0.25 + (0.3 * r + 0.59 * g + 0.11 * b) / 255.0 * 0.85
            px[x, y] = (min(255, int(tint[0] * lum)), min(255, int(tint[1] * lum)), min(255, int(tint[2] * lum)), a)
    return img


def write_if_missing(img, path):
    if os.path.exists(path):
        return
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    print("wrote", os.path.relpath(path, ROOT))


def main():
    jar = zipfile.ZipFile(CLIENT_JAR)
    egg_base = Image.open(io.BytesIO(jar.read("assets/minecraft/textures/item/pig_spawn_egg.png")))

    lang_path = os.path.join(ASSETS, "lang", "en_us.json")
    with open(lang_path) as f:
        lang = json.load(f)

    for npc_id, display, egg_tint, skin_colors in NPCS:
        write_if_missing(make_skin(*skin_colors),
                os.path.join(ASSETS, "textures", "entity", "npc", npc_id + ".png"))
        write_if_missing(tint_egg(egg_base, egg_tint),
                os.path.join(ASSETS, "textures", "item", npc_id + "_spawn_egg.png"))
        lang[f"entity.examinecraft.{npc_id}"] = display
        lang[f"item.examinecraft.{npc_id}_spawn_egg"] = display + " Spawn Egg"

    with open(lang_path, "w", newline="\n") as f:
        json.dump(lang, f, indent="\t", ensure_ascii=True)
        f.write("\n")
    print("done:", len(NPCS), "NPCs")


if __name__ == "__main__":
    main()
