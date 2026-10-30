"""Negative controls: prove the asset gate catches the reported failure modes."""
import unittest
from PIL import Image
from check_pixel_assets import problems

class AssetGateTests(unittest.TestCase):
 def sprite(self):
  im=Image.new('RGBA',(16,16));im.paste((140,100,50,255),(2,3,14,12));return im.resize((32,32),Image.Resampling.NEAREST)
 def test_valid(self):self.assertEqual([],problems(self.sprite()))
 def test_partial_alpha(self):
  im=self.sprite();im.paste((140,100,50,100),(6,6,8,8));self.assertTrue(any('alpha' in e for e in problems(im)))
 def test_missing_pixel(self):
  im=self.sprite();im.paste((0,0,0,0),(12,12,14,14));self.assertTrue(any('pinhole' in e for e in problems(im)))
 def test_loose_pixel(self):
  im=self.sprite();im.paste((140,100,50,255),(2,28,4,30));self.assertTrue(any('isolated' in e for e in problems(im)))
 def test_tiny_icon(self):
  im=Image.new('RGBA',(32,32));im.paste((140,100,50,255),(12,12,20,20));self.assertTrue(any('extent' in e for e in problems(im)))
 def test_wrong_grid(self):
  im=self.sprite();im.putpixel((7,7),(255,255,255,255));self.assertTrue(any('grid' in e for e in problems(im)))
 def test_clipped_edge(self):
  im=self.sprite();im.paste((140,100,50,255),(0,8,4,12));self.assertTrue(any('margin' in e for e in problems(im)))
 def test_transparent_block(self):self.assertTrue(any('opaque' in e for e in problems(self.sprite(),False)))
if __name__=='__main__':unittest.main()
