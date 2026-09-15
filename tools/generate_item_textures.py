#!/usr/bin/env python3
"""Generate the small hand-pixelled item sprites used by Ca Cauca Fishing.

The project deliberately keeps these as 16x16 RGBA PNGs: they stay readable beside
vanilla items, do not need a runtime renderer, and can be replaced by resource packs.
"""
from __future__ import annotations

from pathlib import Path
import struct
import zlib

OUT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/cauca_fishing/textures/item"
W = H = 16

# A restrained vanilla-adjacent palette.
INK = (27, 25, 31, 255)
DEEP = (45, 43, 49, 255)
SHADOW = (75, 67, 65, 255)
PAPER = (236, 218, 173, 255)
WHITE = (248, 246, 226, 255)
WATER = (78, 180, 211, 255)
WATER_DARK = (35, 110, 151, 255)
WOOD = (112, 64, 37, 255)
WOOD_LIGHT = (184, 112, 53, 255)
METAL = (164, 174, 182, 255)
METAL_LIGHT = (225, 232, 226, 255)
GOLD = (210, 153, 42, 255)
GOLD_LIGHT = (255, 218, 93, 255)
RED = (177, 64, 61, 255)
GREEN = (82, 153, 91, 255)
PURPLE = (119, 69, 164, 255)


def rgba(c):
    if len(c) == 4:
        return c
    return (*c, 255)


class Sprite:
    def __init__(self):
        self.p = [[(0, 0, 0, 0) for _ in range(W)] for _ in range(H)]

    def px(self, x: int, y: int, color):
        if 0 <= x < W and 0 <= y < H:
            self.p[y][x] = rgba(color)

    def rect(self, x0: int, y0: int, x1: int, y1: int, color):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.px(x, y, color)

    def line(self, x0: int, y0: int, x1: int, y1: int, color, width: int = 1):
        dx, sx = abs(x1 - x0), 1 if x0 < x1 else -1
        dy, sy = -abs(y1 - y0), 1 if y0 < y1 else -1
        err = dx + dy
        while True:
            r = width // 2
            for yy in range(y0 - r, y0 + r + 1):
                for xx in range(x0 - r, x0 + r + 1):
                    self.px(xx, yy, color)
            if x0 == x1 and y0 == y1:
                break
            e2 = 2 * err
            if e2 >= dy:
                err += dy
                x0 += sx
            if e2 <= dx:
                err += dx
                y0 += sy

    def poly(self, points, color, outline=INK):
        # Integer scanline fill, followed by crisp one-pixel edges.
        ys = [p[1] for p in points]
        for y in range(max(0, min(ys)), min(H - 1, max(ys)) + 1):
            xs = []
            for i, (x1, y1) in enumerate(points):
                x2, y2 = points[(i + 1) % len(points)]
                if y1 == y2:
                    continue
                if min(y1, y2) <= y < max(y1, y2):
                    xs.append(round(x1 + (y - y1) * (x2 - x1) / (y2 - y1)))
            xs.sort()
            for i in range(0, len(xs) - 1, 2):
                for x in range(xs[i], xs[i + 1] + 1):
                    self.px(x, y, color)
        for a, b in zip(points, points[1:] + points[:1]):
            self.line(a[0], a[1], b[0], b[1], outline)

    def circle(self, cx: int, cy: int, r: int, color, fill=False):
        for y in range(cy - r, cy + r + 1):
            for x in range(cx - r, cx + r + 1):
                d = (x - cx) ** 2 + (y - cy) ** 2
                if (fill and d <= r * r) or (not fill and (r - 1) ** 2 <= d <= r * r + 1):
                    self.px(x, y, color)

    def save(self, name: str):
        raw = bytearray()
        for row in self.p:
            raw.append(0)
            for c in row:
                raw.extend(c)
        def chunk(kind: bytes, data: bytes):
            return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xffffffff)
        png = b"\x89PNG\r\n\x1a\n"
        png += chunk(b"IHDR", struct.pack(">IIBBBBB", W, H, 8, 6, 0, 0, 0))
        png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
        png += chunk(b"IEND", b"")
        OUT.mkdir(parents=True, exist_ok=True)
        (OUT / f"{name}.png").write_bytes(png)


def rod(name: str, shaft, accent):
    s = Sprite()
    # Shadow and a wood/cork grip at the lower-left.
    s.line(3, 14, 6, 11, INK, 4)
    s.line(3, 14, 6, 11, WOOD, 2)
    s.rect(2, 13, 4, 15, WOOD_LIGHT)
    s.line(5, 12, 14, 3, INK, 3)
    s.line(5, 12, 14, 3, shaft, 1)
    s.line(6, 11, 14, 3, accent, 1)
    s.px(14, 2, METAL_LIGHT)
    s.px(15, 1, METAL)
    s.line(7, 10, 8, 9, DEEP)
    s.line(10, 7, 11, 6, DEEP)
    # A fine line curls away from the tip, giving rods a readable silhouette.
    s.line(14, 3, 14, 6, (205, 232, 226, 220))
    s.line(14, 6, 12, 8, (205, 232, 226, 220))
    return s


def spool(name: str, color):
    s = Sprite()
    s.circle(8, 8, 6, INK, True)
    s.circle(8, 8, 5, color, True)
    s.circle(8, 8, 3, DEEP, True)
    s.circle(8, 8, 2, color, True)
    s.line(4, 4, 12, 12, (245, 245, 240, 150))
    s.line(4, 12, 12, 4, (35, 35, 42, 170))
    s.rect(7, 1, 8, 2, METAL)
    return s


def hook(name: str, scale: int, color):
    s = Sprite()
    if scale == 1:
        pts = [(6, 3), (8, 3), (8, 9), (7, 12), (5, 14), (3, 13), (3, 11), (5, 12), (6, 10)]
    elif scale == 2:
        pts = [(7, 2), (9, 2), (9, 9), (8, 12), (6, 14), (3, 14), (2, 12), (2, 10), (4, 12), (6, 11), (7, 9)]
    else:
        pts = [(7, 1), (10, 1), (10, 9), (9, 12), (7, 15), (3, 15), (1, 12), (1, 9), (4, 12), (6, 11), (7, 8)]
    s.poly(pts, color, INK)
    s.line(7, 3, 7, 8, METAL_LIGHT)
    # Barb, with the barbed variant deliberately emphasized.
    s.line(7, 10, 4, 9, GOLD_LIGHT if name == "barbed_hook" else INK)
    if name == "barbed_hook":
        s.px(3, 8, RED)
        s.px(2, 9, RED)
    return s


def worm(name: str, glow=False):
    s = Sprite()
    c = (77, 164, 79, 255) if glow else (170, 79, 70, 255)
    hi = (183, 255, 116, 255) if glow else (239, 133, 101, 255)
    s.line(3, 12, 5, 9, INK, 4)
    s.line(5, 9, 8, 11, INK, 4)
    s.line(8, 11, 10, 7, INK, 4)
    s.line(10, 7, 13, 5, INK, 4)
    s.line(3, 12, 5, 9, c, 2)
    s.line(5, 9, 8, 11, c, 2)
    s.line(8, 11, 10, 7, c, 2)
    s.line(10, 7, 13, 5, c, 2)
    s.px(4, 9, hi); s.px(8, 10, hi); s.px(11, 6, hi)
    return s


def grub():
    s = Sprite()
    s.poly([(4, 12), (3, 9), (5, 5), (9, 3), (12, 5), (12, 10), (9, 13)], (177, 166, 77), INK)
    s.line(5, 6, 10, 5, (238, 218, 116), 1)
    s.line(5, 9, 10, 8, (112, 111, 55), 1)
    s.px(11, 5, RED)
    return s


def minnow():
    s = Sprite()
    s.poly([(2, 8), (6, 4), (12, 5), (14, 8), (12, 11), (6, 12)], WATER, INK)
    s.poly([(6, 5), (4, 2), (8, 5)], WATER_DARK, INK)
    s.poly([(7, 11), (5, 14), (10, 11)], WATER_DARK, INK)
    s.line(4, 8, 12, 8, (188, 235, 229), 1)
    s.px(11, 7, WHITE); s.px(11, 7, INK)
    s.px(2, 8, (185, 235, 229))
    return s


def shrimp():
    s = Sprite()
    s.line(5, 3, 11, 4, INK, 3)
    s.line(11, 4, 13, 8, INK, 3)
    s.line(13, 8, 10, 12, INK, 3)
    s.line(10, 12, 5, 12, INK, 3)
    s.line(5, 3, 11, 4, (219, 105, 92), 2)
    s.line(11, 4, 13, 8, (219, 105, 92), 2)
    s.line(13, 8, 10, 12, (219, 105, 92), 2)
    s.line(10, 12, 5, 12, (219, 105, 92), 2)
    for x, y in [(7, 4), (10, 6), (11, 9), (8, 11)]:
        s.px(x, y, (255, 201, 146))
    s.line(4, 3, 2, 1, RED); s.line(4, 3, 2, 5, RED)
    return s


def meat(name: str):
    palettes = {
        "fish_meat": ((180, 77, 69), (244, 139, 102)),
        "salted_fish": ((183, 144, 97), (247, 225, 168)),
        "smoked_fish": ((102, 67, 52), (176, 105, 67)),
        "dried_fish": ((181, 133, 67), (244, 195, 95)),
        "frozen_fish": ((100, 180, 218), (213, 246, 255)),
    }
    base, hi = palettes[name]
    s = Sprite()
    s.poly([(2, 9), (5, 4), (11, 3), (14, 6), (13, 11), (9, 13), (4, 12)], base, INK)
    s.line(5, 6, 11, 5, hi, 2)
    s.line(4, 10, 9, 9, hi, 1)
    s.px(12, 7, DEEP)
    if name == "salted_fish":
        for x, y in [(5, 8), (8, 6), (10, 10), (12, 6)]: s.px(x, y, WHITE)
    if name == "smoked_fish": s.rect(3, 12, 11, 13, INK)
    if name == "frozen_fish":
        s.line(4, 4, 6, 2, WHITE); s.line(11, 11, 13, 13, WHITE)
    return s


def fish_part(name: str):
    s = Sprite()
    if name == "fish_head":
        s.poly([(2, 5), (7, 3), (12, 5), (14, 9), (11, 13), (5, 12), (2, 9)], (156, 100, 69), INK)
        s.circle(10, 6, 1, INK, True); s.px(10, 6, WHITE)
        s.line(4, 9, 12, 9, (224, 156, 93))
        s.px(3, 8, RED)
    elif name == "fish_bone":
        s.line(3, 8, 13, 8, INK, 2)
        for x in [5, 7, 9, 11]: s.line(x, 8, x - 2, 5, WHITE); s.line(x, 8, x - 2, 11, WHITE)
        s.line(3, 8, 1, 6, WHITE); s.line(3, 8, 1, 10, WHITE)
    elif name == "fish_scale":
        s.poly([(2, 11), (4, 5), (8, 3), (13, 5), (14, 10), (9, 13)], (83, 169, 170), INK)
        for x, y in [(5, 6), (8, 5), (11, 7), (4, 9), (7, 8), (10, 10), (7, 11)]:
            s.px(x, y, (194, 242, 226))
    elif name == "fish_skin":
        s.poly([(2, 6), (7, 3), (14, 6), (12, 12), (5, 13)], (67, 134, 150), INK)
        for y in [5, 7, 9, 11]:
            s.line(4, y, 12, y + 1, (130, 207, 201))
    elif name == "fish_fin":
        s.poly([(2, 13), (6, 3), (10, 8), (14, 2), (12, 13)], (162, 72, 81), INK)
        s.line(4, 11, 6, 5, (239, 137, 128)); s.line(8, 11, 9, 7, (239, 137, 128))
    elif name == "fish_teeth":
        s.poly([(2, 4), (14, 4), (12, 12), (9, 8), (7, 13), (4, 8)], (231, 224, 191), INK)
        s.line(4, 5, 12, 5, WHITE)
    elif name == "poison_sac":
        s.circle(8, 8, 5, INK, True); s.circle(8, 8, 4, (92, 170, 79), True)
        s.px(6, 6, (191, 245, 96)); s.px(10, 9, (191, 245, 96)); s.px(8, 11, (191, 245, 96))
        s.line(8, 2, 8, 4, INK); s.px(8, 1, RED)
    return s


def bucket():
    s = Sprite()
    s.line(4, 4, 12, 4, INK, 1)
    s.line(4, 4, 3, 7, METAL, 1); s.line(12, 4, 13, 7, METAL, 1)
    s.poly([(3, 6), (13, 6), (12, 14), (4, 14)], METAL, INK)
    s.rect(4, 7, 12, 9, WATER_DARK)
    s.line(5, 8, 11, 8, WATER, 1)
    s.poly([(6, 11), (8, 10), (11, 11), (9, 13), (6, 13)], (219, 119, 90), INK)
    s.px(10, 11, WHITE)
    s.line(3, 5, 2, 2, METAL_LIGHT); s.line(13, 5, 14, 2, METAL_LIGHT)
    return s


def net():
    s = Sprite()
    s.line(2, 14, 7, 9, WOOD, 2); s.line(2, 14, 1, 15, INK)
    s.line(7, 9, 13, 3, INK, 2); s.line(7, 9, 13, 3, METAL, 1)
    for x in range(8, 15, 2): s.line(x, 2, x - 3, 10, (204, 214, 190), 1)
    for y in range(4, 13, 2): s.line(6, y, 14, y - 2, (204, 214, 190), 1)
    s.line(7, 9, 14, 2, INK); s.line(7, 9, 14, 12, INK)
    return s


def knife():
    s = Sprite()
    s.poly([(2, 12), (5, 9), (13, 2), (15, 2), (14, 5), (7, 12)], METAL_LIGHT, INK)
    s.line(6, 10, 13, 4, METAL, 1)
    s.poly([(2, 12), (6, 12), (10, 15), (7, 15)], WOOD, INK)
    s.line(3, 13, 7, 14, WOOD_LIGHT)
    return s


def journal():
    s = Sprite()
    s.poly([(2, 2), (8, 3), (8, 14), (2, 13)], (108, 67, 42), INK)
    s.poly([(8, 3), (14, 2), (14, 13), (8, 14)], (130, 82, 49), INK)
    s.line(8, 3, 8, 14, GOLD_LIGHT, 1)
    s.line(4, 5, 7, 6, PAPER); s.line(4, 7, 7, 8, PAPER)
    s.line(10, 5, 13, 4, PAPER); s.line(10, 7, 13, 6, PAPER)
    s.px(10, 10, GOLD_LIGHT); s.px(11, 10, GOLD_LIGHT); s.px(10, 11, GOLD_LIGHT)
    return s


def ancient_coin():
    s = Sprite()
    s.circle(8, 8, 6, INK, True); s.circle(8, 8, 5, GOLD, True); s.circle(8, 8, 4, GOLD_LIGHT, True)
    s.line(6, 5, 10, 5, GOLD); s.line(6, 6, 9, 8, GOLD); s.line(9, 8, 6, 10, GOLD)
    s.line(6, 10, 10, 11, GOLD)
    s.px(4, 7, (255, 233, 130)); s.px(12, 9, (151, 99, 26))
    return s


def map_fragment():
    s = Sprite()
    s.poly([(2, 3), (6, 2), (9, 4), (14, 2), (14, 13), (10, 14), (6, 12), (2, 14)], PAPER, INK)
    s.line(6, 2, 6, 12, (158, 119, 73)); s.line(9, 4, 9, 14, (158, 119, 73))
    s.line(4, 10, 7, 8, GREEN, 1); s.line(7, 8, 10, 10, GREEN, 1); s.line(10, 10, 12, 6, RED, 1)
    s.px(12, 6, RED); s.px(13, 5, RED)
    return s


def main():
    rods = {
        "basic_fishing_rod": ((146, 104, 67), (220, 177, 84)),
        "reinforced_rod": ((105, 113, 124), (208, 211, 220)),
        "swift_rod": ((66, 154, 182), (202, 246, 247)),
        "heavy_rod": ((83, 83, 91), (185, 132, 81)),
        "deepwater_rod": ((49, 78, 128), (105, 201, 227)),
        "lucky_rod": ((81, 145, 80), (255, 218, 73)),
    }
    for name, (shaft, accent) in rods.items(): rod(name, shaft, accent).save(name)
    for name, color in {
        "basic_line": (146, 146, 151), "strong_line": (95, 96, 119),
        "fine_line": (212, 225, 222), "reinforced_line": (76, 145, 188),
    }.items(): spool(name, color).save(name)
    for name, (scale, color) in {
        "small_hook": (1, METAL), "medium_hook": (2, METAL),
        "large_hook": (3, METAL), "barbed_hook": (3, (185, 143, 73)),
        "deep_hook": (3, (104, 102, 194)),
    }.items(): hook(name, scale, color).save(name)

    worm("worm").save("worm")
    grub().save("grub")
    minnow().save("minnow")
    shrimp().save("shrimp")
    worm("glow_worm", glow=True).save("glow_worm")
    meat("fish_meat").save("fish_meat_bait")
    special = Sprite()
    special.circle(8, 8, 5, INK, True); special.circle(8, 8, 4, PURPLE, True)
    special.line(4, 10, 12, 5, GOLD_LIGHT, 1); special.px(7, 5, WHITE); special.px(10, 9, WHITE)
    special.save("special_bait")

    for name in ["fish_meat", "salted_fish", "smoked_fish", "dried_fish", "frozen_fish"]: meat(name).save(name)
    for name in ["fish_head", "fish_bone", "fish_scale", "fish_skin", "fish_fin", "fish_teeth", "poison_sac"]: fish_part(name).save(name)
    bucket().save("fish_bucket")
    net().save("fish_net")
    knife().save("fishing_knife")
    journal().save("fishermans_journal")
    ancient_coin().save("ancient_coin")
    map_fragment().save("map_fragment")
    print(f"wrote {len(list(OUT.glob('*.png')))} item textures to {OUT}")


if __name__ == "__main__":
    main()
