package com.horowitz.daze;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.ColorFiltering;

import com.horowitz.commons.Pixel;
import com.horowitz.commons.SimilarityImageComparator;

public class GreenComparatorTest {

  private SimilarityImageComparator _comparator = new SimilarityImageComparator(0.04, 2000);
  private ColorFiltering _greenColorFiltering = new ColorFiltering(new IntRange(70, 140), new IntRange(110, 255),
      new IntRange(0, 65));

  @Test
  public void test() {
    // TemplateMatcher matcher = new TemplateMatcher();

    try {
      BufferedImage image = ImageIO.read(new File("images/green.bmp"));
      BufferedImage screen1 = ImageIO.read(new File("test/test1.bmp"));
      BufferedImage screen2 = ImageIO.read(new File("test/test2.bmp"));

      FastBitmap fb1 = new FastBitmap(screen1);
      _greenColorFiltering.applyInPlace(fb1);

      FastBitmap fb2 = new FastBitmap(screen2);
      _greenColorFiltering.applyInPlace(fb2);

      Pixel p = _comparator.findImage(image, fb1.toBufferedImage(), Color.BLACK);
      assertTrue(p != null);
      p = _comparator.findImage(image, fb2.toBufferedImage(), Color.BLACK);
      assertTrue(p == null);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  @Test
  public void test2() {
    // TemplateMatcher matcher = new TemplateMatcher();
    
    try {
      BufferedImage image = ImageIO.read(new File("images/diggy.bmp"));
      BufferedImage image2 = ImageIO.read(new File("images/diggy_tired.bmp"));
      BufferedImage screen1 = ImageIO.read(new File("test/d1.bmp"));
      BufferedImage screen2 = ImageIO.read(new File("test/d2.bmp"));
      BufferedImage screen3 = ImageIO.read(new File("test/d3.bmp"));
      BufferedImage screen4 = ImageIO.read(new File("test/d4.bmp"));
      BufferedImage screen5 = ImageIO.read(new File("test/d5.bmp"));
      BufferedImage screen6 = ImageIO.read(new File("test/test_diggy_tired.bmp"));
      BufferedImage screen7 = ImageIO.read(new File("test/test_diggy_tired2.bmp"));
      BufferedImage screen8 = ImageIO.read(new File("test/test_diggy_tired3.bmp"));
      
      Pixel p = _comparator.findImage(image, screen1, Color.WHITE);
      assertTrue(p != null);
      p = _comparator.findImage(image, screen2, Color.WHITE);
      assertTrue(p != null);
      p = _comparator.findImage(image, screen3, Color.WHITE);
      assertTrue(p == null);
      p = _comparator.findImage(image, screen4, Color.WHITE);
      assertTrue(p == null);
      p = _comparator.findImage(image2, screen5, Color.WHITE);
      assertTrue(p != null);
      p = _comparator.findImage(image2, screen6, Color.WHITE);
      assertTrue(p != null);
      p = _comparator.findImage(image2, screen7, Color.WHITE);
      assertTrue(p != null);
      p = _comparator.findImage(image2, screen8, Color.WHITE);
      assertTrue(p != null);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  
}
