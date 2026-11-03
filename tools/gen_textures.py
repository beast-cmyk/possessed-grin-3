"""Generates the possessed-face overlay textures.

Each mob gets two PNGs with the same size/UV layout as its normal texture:
  <mob>_face.png  - eyes, angry brows and the grin (drawn with normal lighting)
  <mob>_eyes.png  - just the eyes (drawn fullbright, so they glow in the dark)

Faces are drawn from the ASCII grids below. Edit them to change the look:
  .  transparent (the mob's own face shows through)
  W  glowing white eye
  D  dark outline (brows, lips, grin corners)
  T  tooth          t  tooth, slightly darker (so separate teeth read)

Usage:
  python3 gen_textures.py <assets/possessedgrin dir>
  python3 gen_textures.py <assets dir> --preview out.png minecraft=<vanilla textures/entity dir> requiem=<requiem textures/entity dir>
"""
import os
import sys
from PIL import Image

COLORS = {
    "W": (255, 255, 255, 255),
    "D": (26, 6, 10, 255),
    "T": (236, 232, 218, 255),
    "t": (206, 200, 184, 255),
}

# --- face grids --------------------------------------------------------------

# 8x8 heads: zombies, skeletons (eyes on row 4)
FACE_8x8 = [
    "........",
    "........",
    ".D....D.",
    ".WD..DW.",
    ".WW..WW.",
    "D......D",
    "DTtTtTtD",
    ".DDDDDD.",
]

# 8x8 enderman head: its eyes are wider (3 px)
FACE_ENDERMAN = [
    "........",
    "........",
    "DD....DD",
    "WWD..DWW",
    "WWW..WWW",
    "D......D",
    "DTtTtTtD",
    ".DDDDDD.",
]

# 8x10 heads: villagers, wandering trader, witch, illagers, zombie villager
# (eyes on row 6, nose covers the middle two columns from row 7 down)
FACE_VILLAGER = [
    "........",
    "........",
    "........",
    "........",
    "DD....DD",
    ".WDDDDW.",
    ".WW..WW.",
    "D......D",
    "DTtTtTtD",
    ".DDDDDD.",
]

# 10x8 heads: piglins (snout covers columns 3-6 rows 4-7, tusks at columns 2 and 7)
FACE_PIGLIN = [
    "..........",
    ".D......D.",
    ".WDD..DDW.",
    ".WW....WW.",
    "..........",
    "D........D",
    "DT......TD",
    ".D......D.",
]
# front of the piglin snout (4x4): the grin carries on across its bottom
SNOUT_PIGLIN = [
    "....",
    "....",
    "D..D",
    "TtTt",
]

# 5x5 vex head
FACE_VEX = [
    ".....",
    "D...D",
    "WD.DW",
    "WW.WW",
    "DTtTD",
]

# --- mob list ----------------------------------------------------------------
# (namespace, id, texture size, [(x, y, grid), ...], texture used for the preview)
HEAD = (8, 8)

def head(grid):
    return [(HEAD[0], HEAD[1], grid)]

PIGLIN_PARTS = [(8, 8, FACE_PIGLIN), (32, 2, SNOUT_PIGLIN)]

MOBS = [
    # undead
    ("minecraft", "zombie",             (64, 64),  head(FACE_8x8),      "zombie/zombie.png"),
    ("minecraft", "husk",               (64, 64),  head(FACE_8x8),      "zombie/husk.png"),
    ("minecraft", "drowned",            (64, 64),  head(FACE_8x8),      "zombie/drowned.png"),
    ("minecraft", "giant",              (64, 64),  head(FACE_8x8),      None),
    ("minecraft", "skeleton",           (64, 32),  head(FACE_8x8),      "skeleton/skeleton.png"),
    ("minecraft", "stray",              (64, 32),  head(FACE_8x8),      "skeleton/stray.png"),
    ("minecraft", "wither_skeleton",    (64, 32),  head(FACE_8x8),      "skeleton/wither_skeleton.png"),
    ("minecraft", "zombie_villager",    (64, 64),  head(FACE_VILLAGER), "zombie_villager/zombie_villager.png"),
    ("minecraft", "zombified_piglin",   (64, 64),  PIGLIN_PARTS,        "piglin/zombified_piglin.png"),
    # villager folk
    ("minecraft", "villager",           (64, 64),  head(FACE_VILLAGER), "villager/villager.png"),
    ("minecraft", "wandering_trader",   (64, 64),  head(FACE_VILLAGER), "wandering_trader.png"),
    ("minecraft", "witch",              (64, 128), head(FACE_VILLAGER), "witch.png"),
    ("requiem",   "cured_villager",     (64, 64),  head(FACE_VILLAGER), "cured_villager.png"),
    # illagers
    ("minecraft", "pillager",           (64, 64),  head(FACE_VILLAGER), "illager/pillager.png"),
    ("minecraft", "vindicator",         (64, 64),  head(FACE_VILLAGER), "illager/vindicator.png"),
    ("minecraft", "evoker",             (64, 64),  head(FACE_VILLAGER), "illager/evoker.png"),
    ("minecraft", "illusioner",         (64, 64),  head(FACE_VILLAGER), "illager/illusioner.png"),
    # piglins
    ("minecraft", "piglin",             (64, 64),  PIGLIN_PARTS,        "piglin/piglin.png"),
    ("minecraft", "piglin_brute",       (64, 64),  PIGLIN_PARTS,        "piglin/piglin_brute.png"),
    ("requiem",   "cured_piglin",       (64, 64),  PIGLIN_PARTS,        "cured_piglin.png"),
    ("requiem",   "cured_piglin_brute", (64, 64),  PIGLIN_PARTS,        "cured_piglin_brute.png"),
    # others
    ("minecraft", "enderman",           (64, 32),  head(FACE_ENDERMAN), "enderman/enderman.png"),
    ("minecraft", "vex",                (32, 32),  [(5, 5, FACE_VEX)],  "illager/vex.png"),
]


# --- decals for every other mob (stretched over the front of the head) -------
# (file name, grid, eyes-only glow copy?)
DRAGON_EYES = [
    "................",
    ".DD..........DD.",
    ".WWD........DWW.",
    ".WWWD......DWWW.",
    "..WWWD....DWWW..",
    "...WW......WW...",
    "................",
]
DRAGON_GRIN = [
    "D..........D",
    "DD........DD",
    ".DTtTtTtTtD.",
    "..DtTtTtTD..",
    "...DDDDDD...",
]
DECALS = [
    ("generic_face", FACE_8x8),         # any mob without a hand-made face
    ("dragon_eyes", DRAGON_EYES),       # Ender Dragon skull
    ("dragon_grin", DRAGON_GRIN),       # Ender Dragon snout tip
]
# glow-only copies (just the W pixels), drawn fullbright
GLOW_DECALS = [("generic_eyes", FACE_8x8), ("dragon_eyes_glow", DRAGON_EYES)]


def paint(size, parts):
    face = Image.new("RGBA", size, (0, 0, 0, 0))
    eyes = Image.new("RGBA", size, (0, 0, 0, 0))
    for ox, oy, grid in parts:
        width = len(grid[0])
        for y, row in enumerate(grid):
            assert len(row) == width, f"ragged grid row: {row!r}"
            for x, ch in enumerate(row):
                if ch == ".":
                    continue
                face.putpixel((ox + x, oy + y), COLORS[ch])
                if ch == "W":
                    eyes.putpixel((ox + x, oy + y), COLORS[ch])
    return face, eyes


def generate(out_dir):
    made = {}
    for ns, name, size, parts, _ in MOBS:
        d = os.path.join(out_dir, "textures", "entity", "possessed", ns)
        os.makedirs(d, exist_ok=True)
        face, eyes = paint(size, parts)
        face.save(os.path.join(d, f"{name}_face.png"))
        eyes.save(os.path.join(d, f"{name}_eyes.png"))
        made[(ns, name)] = face
    d = os.path.join(out_dir, "textures", "entity", "possessed")
    for name, grid in DECALS:
        face, _ = paint((len(grid[0]), len(grid)), [(0, 0, grid)])
        face.save(os.path.join(d, f"{name}.png"))
    for name, grid in GLOW_DECALS:
        _, eyes = paint((len(grid[0]), len(grid)), [(0, 0, grid)])
        eyes.save(os.path.join(d, f"{name}.png"))
    return made


# --- preview: rough front view of each head, before and after ----------------

def front_view(tex, parts):
    ox, oy, grid = parts[0]
    w, h = len(grid[0]), len(grid)
    if parts is PIGLIN_PARTS:
        canvas = tex.crop((8, 8, 18, 16)).copy()
        canvas.alpha_composite(tex.crop((32, 2, 36, 6)), (3, 4))   # snout
        canvas.alpha_composite(tex.crop((3, 1, 4, 3)), (2, 6))     # tusk
        canvas.alpha_composite(tex.crop((3, 5, 4, 7)), (7, 6))     # tusk
        return canvas
    if grid is FACE_VILLAGER:
        canvas = Image.new("RGBA", (8, 11), (0, 0, 0, 0))
        canvas.alpha_composite(tex.crop((8, 8, 16, 18)), (0, 0))
        canvas.alpha_composite(tex.crop((26, 2, 28, 6)), (3, 7))   # nose
        return canvas
    return tex.crop((ox, oy, ox + w, oy + h)).copy()


def preview(made, dirs, out_png, scale=14, per_row=6):
    cells = []
    for ns, name, size, parts, rel in MOBS:
        if rel is None or ns not in dirs:
            continue
        base = Image.open(os.path.join(dirs[ns], rel)).convert("RGBA")
        after = base.copy()
        after.alpha_composite(made[(ns, name)])
        a, b = front_view(base, parts), front_view(after, parts)
        cells.append((a.resize((a.width * scale, a.height * scale), Image.NEAREST),
                      b.resize((b.width * scale, b.height * scale), Image.NEAREST)))
    gap = 12
    cw = max(c[0].width for c in cells)
    ch = max(c[0].height for c in cells) * 2 + 6
    rows = (len(cells) + per_row - 1) // per_row
    sheet = Image.new("RGBA", (per_row * (cw + gap) + gap, rows * (ch + gap * 2) + gap), (40, 40, 46, 255))
    for i, (a, b) in enumerate(cells):
        cx = gap + (i % per_row) * (cw + gap) + (cw - a.width) // 2
        cy = gap + (i // per_row) * (ch + gap * 2)
        sheet.alpha_composite(a, (cx, cy))
        sheet.alpha_composite(b, (cx, cy + ch // 2 + 3))
    sheet.save(out_png)


if __name__ == "__main__":
    made = generate(sys.argv[1])
    if "--preview" in sys.argv:
        i = sys.argv.index("--preview")
        dirs = dict(arg.split("=", 1) for arg in sys.argv[i + 2:])
        preview(made, dirs, sys.argv[i + 1])
    print(f"generated faces for {len(made)} mobs")
