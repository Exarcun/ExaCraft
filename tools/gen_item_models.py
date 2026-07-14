# Generates item model JSON + client item JSON for every texture in textures/item/.
# Existing files are left untouched, so hand-edited models are safe.
# Names listed in HANDHELD get the handheld parent (tools/weapons held like a sword).
# Re-run any time: python tools/gen_item_models.py

import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "examinecraft")
TEX_DIR = os.path.join(ASSETS, "textures", "item")
MODEL_DIR = os.path.join(ASSETS, "models", "item")
ITEMS_DIR = os.path.join(ASSETS, "items")

HANDHELD = {
    "hash_pipe",
    "grappling_hook",
    "perc_20",
    "perc_80",
    "needle",
    "ket_pot",
    "golf_club",
    "baseball_bat",
    "uzi",
    "sniper_rifle",
    "gamma_ray_gun",
}


def write_if_missing(path, data):
    if os.path.exists(path):
        return False
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", newline="\n") as f:
        json.dump(data, f, indent=2)
        f.write("\n")
    print("wrote", path)
    return True


def main():
    names = sorted(
        os.path.splitext(f)[0]
        for f in os.listdir(TEX_DIR)
        if f.endswith(".png")
    )
    for name in names:
        parent = "minecraft:item/handheld" if name in HANDHELD else "minecraft:item/generated"
        write_if_missing(
            os.path.join(MODEL_DIR, name + ".json"),
            {"parent": parent, "textures": {"layer0": "examinecraft:item/" + name}},
        )
        write_if_missing(
            os.path.join(ITEMS_DIR, name + ".json"),
            {"model": {"type": "minecraft:model", "model": "examinecraft:item/" + name}},
        )
    print("done:", len(names), "textures checked")


if __name__ == "__main__":
    main()
