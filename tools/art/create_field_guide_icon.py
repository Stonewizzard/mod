"""Original 32px pixel sprite. Run with Pillow to regenerate the committed PNG."""
from pathlib import Path
from PIL import Image, ImageDraw
image=Image.new('RGBA',(32,32));p=ImageDraw.Draw(image)
# All coordinates are logical pixels. Flat palette, stepped edges, no filtering.
colors={'outline':'#181e1c','leather':'#564035','spine':'#76533a','spineLight':'#a77a49','paper':'#d9c38a','paperLight':'#f3e6b7','paperShade':'#a48a5a','cover':'#224e43','coverLight':'#346c57','coverShade':'#183d35','edge':'#102f2c','gold':'#b78a45','goldLight':'#f2cf72','goldShade':'#785334','gem':'#39b8a6','gemLight':'#a5e3bd','ribbon':'#9a4145'}
def poly(points,color):p.polygon(points,fill=colors[color])
def line(points,color,width=1):p.line(points,fill=colors[color],width=width)
# Back cover and parchment block.
poly([(3,9),(18,2),(21,2),(29,7),(29,23),(14,30),(11,30),(3,25)],'outline')
poly([(5,12),(18,6),(27,10),(27,22),(13,28),(5,24)],'leather')
poly([(12,22),(27,15),(27,22),(13,28),(12,27)],'paperShade')
poly([(13,22),(27,16),(27,20),(13,26)],'paper')
poly([(14,22),(26,17),(26,18),(14,24)],'paperLight')
line([(14,26),(26,21)],'paperShade')
line([(15,23),(25,19)],'paperShade')
poly([(21,23),(23,22),(23,29),(21,27)],'ribbon')
# Leather binding and the large teal front cover.
poly([(3,9),(6,8),(15,14),(15,26),(12,27),(3,22)],'leather')
poly([(4,10),(6,9),(13,14),(13,24),(12,25),(4,21)],'spine')
line([(5,11),(5,20),(12,24)],'spineLight')
poly([(6,7),(18,2),(21,2),(29,7),(29,15),(15,22),(12,22),(6,18)],'outline')
poly([(7,8),(19,3),(21,3),(28,7),(28,14),(14,21),(7,17)],'edge')
poly([(8,8),(19,4),(21,4),(27,7),(27,13),(14,20),(8,16)],'cover')
poly([(9,8),(19,4),(21,4),(26,7),(14,13)],'coverLight')
poly([(14,14),(26,8),(26,13),(14,19)],'coverShade')
line([(8,8),(13,12),(13,18)],'coverLight')
# Brass corners follow the angle of the cover.
poly([(9,8),(12,7),(12,8),(10,9),(10,10),(9,9)],'gold')
line([(10,8),(12,7)],'goldLight')
poly([(19,4),(21,4),(23,5),(22,6),(20,5),(18,6)],'gold')
line([(20,4),(22,5)],'goldLight')
poly([(24,12),(27,11),(27,14),(24,15),(24,14),(26,13),(26,12)],'gold')
poly([(10,16),(13,18),(16,17),(16,19),(14,20),(10,17)],'gold')
line([(12,18),(14,19),(15,18)],'goldLight')
# A deliberately chunky archaeological pick over an artifact seal.
poly([(14,9),(17,7),(20,7),(22,8),(23,10),(21,10),(20,9),(18,9),(16,10),(14,12)],'goldShade')
poly([(14,8),(17,6),(20,6),(22,7),(23,9),(21,9),(20,8),(18,8),(16,9),(14,11)],'gold')
line([(15,8),(17,7),(20,7)],'goldLight')
poly([(18,8),(20,9),(17,15),(15,16),(15,14)],'goldShade')
line([(19,9),(16,14)],'goldLight')
poly([(21,12),(23,13),(22,16),(20,17),(19,15)],'edge')
poly([(21,12),(23,13),(21,16),(20,15)],'gem')
line([(21,13),(20,15)],'gemLight')
# Copper/brass closure across the right edge, two binding bands.
poly([(26,12),(29,11),(29,14),(26,15)],'goldShade')
poly([(27,12),(29,12),(29,13),(27,14)],'goldLight')
line([(4,13),(7,15)],'gold')
line([(4,18),(7,20)],'gold')
path=Path(__file__).resolve().parents[2]/'src/main/resources/assets/examplemod/textures/item/ancient_field_guide.png'
path.parent.mkdir(parents=True,exist_ok=True);image.save(path)
