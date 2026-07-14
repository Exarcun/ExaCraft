# Generates all armor assets for the 10 sets:
#   - worn textures (recolors of vanilla armor textures, read from the MC client jar)
#   - equipment asset JSONs
#   - item icon textures (recolors of vanilla armor icons)
#   - crafting recipe JSONs
#   - lang entries (merged into en_us.json)
# Existing texture PNGs are skipped (hand-painted art is safe); JSONs are rewritten.
# Run: python tools/gen_armor_assets.py

import io
import json
import os
import zipfile

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "examinecraft")
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "examinecraft")
CLIENT_JAR = os.path.expanduser(
    os.path.join("~", ".gradle", "caches", "fabric-loom", "26.2", "minecraft-client-only.jar"))

PIECES = ["helmet", "chestplate", "leggings", "boots"]

# id, display name, vanilla texture base, tint mode, recipe spec
SETS = [
    {"id": "swissman", "name": "Swissman's", "base": "netherite", "tint": (215, 45, 45),
     "recipe": ("shaped", "examinecraft:swiss_ingot")},
    {"id": "benito", "name": "Benito's", "base": "iron", "tint": (55, 55, 60),
     "recipe": ("convert", "iron", ["minecraft:black_dye"])},
    {"id": "twitch", "name": "Twitch", "base": "iron", "tint": (80, 120, 230),
     "recipe": ("convert", "iron", ["minecraft:blue_dye"])},
    {"id": "mod", "name": "Mod's", "base": "iron", "tint": (70, 180, 90),
     "recipe": ("convert", "iron", ["minecraft:green_dye"])},
    {"id": "exa", "name": "Exa's", "base": "iron", "tint": (70, 205, 205),
     "recipe": ("shaped", "minecraft:copper_ingot")},
    {"id": "pig", "name": "Pig's", "base": "iron", "tint": (245, 150, 180),
     "recipe": ("convert", "iron", ["minecraft:pink_dye"])},
    {"id": "twitch_sub", "name": "Twitch Sub", "base": "diamond", "tint": (155, 80, 225),
     "recipe": ("convert", "diamond", ["minecraft:purple_dye"])},
    {"id": "gayman", "name": "Gayman's", "base": "diamond", "tint": "rainbow",
     "recipe": ("convert", "diamond",
                ["minecraft:red_dye", "minecraft:yellow_dye", "minecraft:lime_dye", "minecraft:blue_dye"])},
    {"id": "lazyman", "name": "Lazyman's", "base": "iron", "tint": (125, 110, 90),
     "recipe": ("convert", "leather", ["minecraft:rotten_flesh"])},
    {"id": "grilled_pizza", "name": "Grilled Pizza", "base": "diamond", "tint": (250, 195, 90),
     "recipe": ("convert", "diamond", ["examinecraft:pizza_slice"])},
]

SHAPED_PATTERNS = {
    "helmet": ["mmm", "m m"],
    "chestplate": ["m m", "mmm", "mmm"],
    "leggings": ["mmm", "m m", "m m"],
    "boots": ["m m", "m m"],
}


def rainbow_color(x, y):
    import colorsys
    hue = ((x + y) % 24) / 24.0
    r, g, b = colorsys.hsv_to_rgb(hue, 0.85, 1.0)
    return int(r * 255), int(g * 255), int(b * 255)


def recolor(img, tint, set_id):
    img = img.convert("RGBA")
    px = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            lum = (0.3 * r + 0.59 * g + 0.11 * b) / 255.0
            lum = 0.25 + lum * 0.85  # keep dark bases readable
            if tint == "rainbow":
                tr, tg, tb = rainbow_color(x, y)
            else:
                tr, tg, tb = tint
            px[x, y] = (min(255, int(tr * lum)), min(255, int(tg * lum)), min(255, int(tb * lum)), a)
    if set_id == "grilled_pizza":
        for i, (dx, dy) in enumerate([(7, 3), (19, 9), (37, 5), (49, 13), (13, 17), (43, 21)]):
            for yy in range(dy, min(dy + 2, h)):
                for xx in range(dx, min(dx + 2, w)):
                    if px[xx, yy][3] > 0:
                        px[xx, yy] = (200, 60, 50, 255)
    if set_id == "lazyman":
        for (dx, dy) in [(6, 4), (22, 8), (40, 6), (52, 14), (14, 20), (34, 24)]:
            if dx < w and dy < h and px[dx, dy][3] > 0:
                px[dx, dy] = (60, 50, 40, 255)
    return img


def write_png_if_missing(img, path):
    if os.path.exists(path):
        return
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    print("wrote", os.path.relpath(path, ROOT))


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", newline="\n") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def main():
    jar = zipfile.ZipFile(CLIENT_JAR)

    def jar_image(path):
        return Image.open(io.BytesIO(jar.read(path)))

    lang_path = os.path.join(ASSETS, "lang", "en_us.json")
    with open(lang_path) as f:
        lang = json.load(f)

    for s in SETS:
        sid, base, tint = s["id"], s["base"], s["tint"]

        # Worn equipment textures.
        for layer in ("humanoid", "humanoid_leggings"):
            src = jar_image(f"assets/minecraft/textures/entity/equipment/{layer}/{base}.png")
            out = os.path.join(ASSETS, "textures", "entity", "equipment", layer, sid + ".png")
            write_png_if_missing(recolor(src, tint, sid), out)

        # Equipment asset JSON.
        write_json(os.path.join(ASSETS, "equipment", sid + ".json"), {
            "layers": {
                "humanoid": [{"texture": "examinecraft:" + sid}],
                "humanoid_leggings": [{"texture": "examinecraft:" + sid}],
            }
        })

        # Item icons + lang + recipes per piece.
        for piece in PIECES:
            item_id = f"{sid}_{piece}"
            src = jar_image(f"assets/minecraft/textures/item/{base}_{piece}.png")
            out = os.path.join(ASSETS, "textures", "item", item_id + ".png")
            write_png_if_missing(recolor(src, tint, sid), out)

            lang[f"item.examinecraft.{item_id}"] = f"{s['name']} {piece.capitalize()}"

            recipe = s["recipe"]
            recipe_path = os.path.join(DATA, "recipe", item_id + ".json")
            if recipe[0] == "shaped":
                write_json(recipe_path, {
                    "type": "minecraft:crafting_shaped",
                    "key": {"m": recipe[1]},
                    "pattern": SHAPED_PATTERNS[piece],
                    "result": {"id": f"examinecraft:{item_id}"},
                })
            else:
                _, vanilla_tier, extras = recipe
                write_json(recipe_path, {
                    "type": "minecraft:crafting_shapeless",
                    "ingredients": [f"minecraft:{vanilla_tier}_{piece}"] + extras,
                    "result": {"id": f"examinecraft:{item_id}"},
                })

    with open(lang_path, "w", newline="\n") as f:
        json.dump(lang, f, indent="\t", ensure_ascii=True)
        f.write("\n")
    print("lang entries:", len(lang))
    print("done: 10 sets")


if __name__ == "__main__":
    main()
