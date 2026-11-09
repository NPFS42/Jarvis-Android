"""Creates original, non-sensitive Android store graphics. Draft; no UI screenshot is fabricated."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "play-assets"
OUT.mkdir(exist_ok=True)
FONT = Path("C:/Windows/Fonts/segoeuib.ttf")
FONT_REGULAR = Path("C:/Windows/Fonts/segoeui.ttf")

def font(size, bold=True):
    preferred = FONT if bold else FONT_REGULAR
    return ImageFont.truetype(str(preferred), size) if preferred.exists() else ImageFont.load_default()

# 512 x 512 store listing icon (not screenshot)
im = Image.new("RGB", (512, 512), (7, 20, 32))
draw = ImageDraw.Draw(im)
draw.rounded_rectangle((12, 12, 499, 499), radius=104, fill=(13, 35, 52), outline=(50, 165, 172), width=8)
draw.ellipse((88, 88, 423, 423), outline=(45, 126, 144), width=13)
draw.arc((88, 88, 423, 423), 197, 330, fill=(76, 236, 222), width=22)
draw.arc((113, 113, 399, 399), 5, 75, fill=(74, 158, 209), width=11)
draw.text((251, 238), "J", font=font(265), anchor="mm",
          fill=(226, 250, 248), stroke_width=3, stroke_fill=(6, 29, 45))
draw.ellipse((395, 127, 422, 154), fill=(99, 244, 217))
im.save(OUT / "icon-512.png", optimize=True)

banner = Image.new("RGB", (1024, 500), (7, 19, 29))
d = ImageDraw.Draw(banner)
for i in range(13):
    x = 570 + i * 37
    d.arc((x - 50, 50, x + 300, 410), 230, 318,
          fill=(17 + i, 63 + i * 5, 80 + i * 4), width=3)
d.rounded_rectangle((68, 65, 307, 322), radius=56, fill=(15, 39, 56),
                    outline=(74, 227, 209), width=5)
d.ellipse((105, 102, 268, 265), outline=(56, 170, 181), width=7)
d.text((187, 176), "J", font=font(160), anchor="mm", fill=(232, 255, 251))
d.text((340, 117), "JARVIS", font=font(98), fill=(231, 249, 249))
d.text((347, 245), "SOVEREIGN AI", font=font(36), fill=(89, 220, 206))
d.text((347, 314), "TAILNET-ONLY ANDROID CLIENT", font=font(23, False), fill=(151, 189, 204))
d.rounded_rectangle((340, 376, 828, 382), radius=3, fill=(59, 188, 172))
banner.save(OUT / "feature-1024x500.png", optimize=True)
print("play_assets_created", [(p.name,p.stat().st_size) for p in OUT.glob("*.png")])
