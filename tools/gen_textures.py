# Placeholder texture generator for ExaMinecraft.
# Generates 16x16 item textures into src/main/resources/assets/examinecraft/textures/item/
# Re-run any time: python tools/gen_textures.py
# All output PNGs are placeholders meant to be repainted by hand later.

import os
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ITEM_DIR = os.path.join(ROOT, "src", "main", "resources", "assets", "examinecraft", "textures", "item")

T = (0, 0, 0, 0)  # transparent


def new_canvas():
    return Image.new("RGBA", (16, 16), T)


def save(img, name):
    os.makedirs(ITEM_DIR, exist_ok=True)
    path = os.path.join(ITEM_DIR, name + ".png")
    img.save(path)
    print("wrote", path)


def outline(draw, box, color):
    draw.rectangle(box, outline=color)


def pill(colors_left, colors_right, angle=True):
    """Capsule pill split into two colors, drawn diagonally."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    # diagonal capsule from bottom-left to top-right
    for i in range(10):
        x = 3 + i
        y = 12 - i
        c = colors_left if i < 5 else colors_right
        d.ellipse([x - 2, y - 2, x + 2, y + 2], fill=c)
    # darker outline pass
    for i in (0, 9):
        x = 3 + i
        y = 12 - i
        d.ellipse([x - 2, y - 2, x + 2, y + 2], outline=(40, 40, 40, 255))
    # shine
    d.point((4, 9), fill=(255, 255, 255, 220))
    d.point((5, 8), fill=(255, 255, 255, 180))
    return img


def flask(liquid, label=None):
    """Round-bottom potion flask with colored liquid."""
    img = new_canvas()
    d = ImageDraw.Draw(img)
    glass = (200, 220, 235, 255)
    dark = (60, 70, 90, 255)
    # neck
    d.rectangle([6, 1, 9, 5], fill=glass, outline=dark)
    # cork
    d.rectangle([6, 0, 9, 1], fill=(150, 110, 70, 255))
    # bulb
    d.ellipse([3, 4, 12, 14], fill=glass, outline=dark)
    # liquid inside bulb
    d.ellipse([4, 7, 11, 13], fill=liquid)
    # shine
    d.point((5, 6), fill=(255, 255, 255, 200))
    d.point((6, 5), fill=(255, 255, 255, 160))
    if label:
        d.rectangle([6, 9, 9, 11], fill=label)
    return img


def joint():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    paper = (240, 235, 220, 255)
    for i in range(9):
        x = 3 + i
        y = 12 - i
        d.rectangle([x, y, x + 1, y + 1], fill=paper)
    # lit tip (top right)
    d.rectangle([12, 2, 13, 3], fill=(255, 120, 30, 255))
    d.point((13, 1), fill=(255, 200, 60, 255))
    d.point((14, 0), fill=(160, 160, 160, 180))  # smoke
    # filter end
    d.rectangle([3, 12, 4, 13], fill=(210, 160, 90, 255))
    return img


def lexotan():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    # blister pack with two pills
    d.rectangle([2, 4, 13, 11], fill=(190, 200, 210, 255), outline=(70, 80, 90, 255))
    d.ellipse([4, 6, 7, 9], fill=(245, 245, 250, 255), outline=(120, 130, 140, 255))
    d.ellipse([9, 6, 12, 9], fill=(245, 245, 250, 255), outline=(120, 130, 140, 255))
    d.rectangle([2, 11, 13, 12], fill=(120, 190, 120, 255))
    return img


def ket_vial():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    glass = (215, 230, 240, 255)
    dark = (70, 80, 100, 255)
    d.rectangle([5, 3, 10, 13], fill=glass, outline=dark)
    d.rectangle([5, 8, 10, 13], fill=(180, 200, 255, 255))  # liquid
    d.rectangle([6, 1, 9, 3], fill=(90, 90, 100, 255))  # cap
    d.point((6, 4), fill=(255, 255, 255, 200))
    return img


def lsd_blotter():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    d.rectangle([2, 2, 13, 13], fill=(250, 248, 240, 255), outline=(120, 120, 120, 255))
    # perforation grid
    for x in (5, 9):
        d.line([(x, 2), (x, 13)], fill=(190, 190, 190, 255))
    for y in (5, 9):
        d.line([(2, y), (13, y)], fill=(190, 190, 190, 255))
    # rainbow dots per tab
    colors = [(255, 60, 60), (255, 160, 40), (255, 230, 60), (80, 200, 80),
              (70, 130, 255), (150, 80, 220), (255, 80, 180), (60, 220, 200), (255, 120, 90)]
    i = 0
    for cy in (3, 7, 11):
        for cx in (3, 7, 11):
            d.point((cx, cy), fill=colors[i] + (255,))
            i += 1
    return img


def hash_pipe():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    wood = (120, 80, 45, 255)
    dark = (80, 50, 25, 255)
    # stem diagonal
    for i in range(8):
        d.rectangle([3 + i, 11 - i, 4 + i, 12 - i], fill=wood)
    # bowl at top right
    d.ellipse([10, 1, 15, 6], fill=wood, outline=dark)
    d.ellipse([12, 2, 14, 4], fill=(40, 30, 20, 255))
    d.point((13, 0), fill=(160, 160, 160, 180))  # smoke
    # mouthpiece
    d.rectangle([2, 12, 3, 13], fill=dark)
    return img


def salvia():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    green = (70, 160, 60, 255)
    dark = (40, 110, 40, 255)
    # leaf
    d.polygon([(8, 1), (13, 6), (12, 11), (8, 14), (4, 11), (3, 6)], fill=green, outline=dark)
    # central vein
    d.line([(8, 2), (8, 13)], fill=dark)
    d.line([(8, 6), (5, 8)], fill=dark)
    d.line([(8, 6), (11, 8)], fill=dark)
    # stem
    d.rectangle([7, 14, 8, 15], fill=(100, 70, 40, 255))
    return img


def salvia_bread():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    crust = (190, 140, 80, 255)
    dark = (140, 95, 50, 255)
    d.ellipse([1, 5, 14, 12], fill=crust, outline=dark)
    # green swirl (the salvia)
    d.line([(4, 8), (11, 8)], fill=(90, 160, 70, 255))
    d.point((5, 7), fill=(90, 160, 70, 255))
    d.point((9, 9), fill=(90, 160, 70, 255))
    # scoring lines
    d.line([(5, 6), (6, 7)], fill=dark)
    d.line([(8, 6), (9, 7)], fill=dark)
    return img


def dmt():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    # crystal shards, orange-white
    c1 = (255, 180, 90, 255)
    c2 = (255, 230, 190, 255)
    dark = (180, 110, 40, 255)
    d.polygon([(5, 13), (3, 7), (6, 3), (8, 8)], fill=c1, outline=dark)
    d.polygon([(9, 13), (8, 6), (11, 2), (13, 8)], fill=c2, outline=dark)
    d.point((6, 5), fill=(255, 255, 255, 220))
    d.point((11, 4), fill=(255, 255, 255, 220))
    return img


def quaalude():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    # big round white tablet
    d.ellipse([2, 2, 13, 13], fill=(245, 245, 240, 255), outline=(150, 150, 145, 255))
    # score line
    d.line([(4, 8), (11, 8)], fill=(170, 170, 165, 255))
    # "714" imprint hint (dots)
    d.point((5, 5), fill=(150, 150, 145, 255))
    d.point((8, 5), fill=(150, 150, 145, 255))
    d.point((11, 5), fill=(150, 150, 145, 255))
    return img


def grappling_hook():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    metal = (130, 135, 145, 255)
    dark = (70, 75, 85, 255)
    # hook (top) - three prongs
    d.arc([4, 1, 12, 9], start=180, end=360, fill=dark)
    d.line([(4, 5), (4, 3)], fill=metal)
    d.line([(12, 5), (12, 3)], fill=metal)
    d.line([(8, 1), (8, 5)], fill=metal)
    # rope down
    rope = (180, 140, 80, 255)
    d.line([(8, 5), (8, 10)], fill=rope)
    d.point((7, 7), fill=rope)
    d.point((9, 9), fill=rope)
    # handle
    d.rectangle([6, 10, 9, 14], fill=(110, 75, 45, 255), outline=(70, 45, 25, 255))
    return img


def swiss_ingot():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    red = (200, 40, 40, 255)
    dark = (130, 20, 20, 255)
    d.polygon([(2, 10), (5, 6), (13, 6), (10, 10)], fill=red, outline=dark)
    d.rectangle([2, 10, 10, 12], fill=(170, 30, 30, 255), outline=dark)
    d.polygon([(10, 10), (13, 6), (13, 8), (10, 12)], fill=dark)
    # white cross (swiss)
    d.line([(6, 8), (8, 8)], fill=(255, 255, 255, 255))
    d.point((7, 7), fill=(255, 255, 255, 255))
    d.point((7, 9), fill=(255, 255, 255, 255))
    return img


def pizza_slice():
    img = new_canvas()
    d = ImageDraw.Draw(img)
    cheese = (250, 200, 90, 255)
    crust = (190, 130, 60, 255)
    d.polygon([(8, 14), (2, 3), (14, 3)], fill=cheese, outline=(160, 110, 40, 255))
    d.rectangle([2, 2, 14, 3], fill=crust)
    # pepperoni
    for (px, py) in ((6, 5), (10, 5), (8, 8)):
        d.ellipse([px - 1, py - 1, px + 1, py + 1], fill=(200, 60, 50, 255))
    return img


ITEMS = {
    "joint": joint,
    "lexotan": lexotan,
    "ket_vial": ket_vial,
    "lsd_blotter": lsd_blotter,
    "hash_pipe": hash_pipe,
    "salvia_divinorum": salvia,
    "salvia_bread": salvia_bread,
    "dmt": dmt,
    "quaalude": quaalude,
    "grappling_hook": grappling_hook,
    "swiss_ingot": swiss_ingot,
    "pizza_slice": pizza_slice,
    "perc_potion": lambda: flask((250, 210, 60, 255)),
    "opium_potion": lambda: flask((90, 60, 30, 255)),
    "narcan_potion": lambda: flask((240, 245, 255, 255), label=(60, 100, 220, 255)),
}


def main():
    for name, fn in ITEMS.items():
        save(fn(), name)


if __name__ == "__main__":
    main()
