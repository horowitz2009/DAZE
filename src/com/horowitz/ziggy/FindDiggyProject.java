package com.horowitz.ziggy;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.horowitz.commons.Pixel;
import com.horowitz.commons.Settings;
import com.horowitz.daze.ScreenScanner;

import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Add;
import Catalano.Imaging.Filters.And;
import Catalano.Imaging.Filters.Blur;
import Catalano.Imaging.Filters.ColorFiltering;
import Catalano.Imaging.Filters.Or;
import Catalano.Imaging.Filters.ReplaceColor;
import Catalano.Imaging.Filters.Threshold;
import Catalano.Imaging.Filters.Xor;
import Catalano.Imaging.Tools.Blob;
import Catalano.Imaging.Tools.BlobDetection;

public class FindDiggyProject {

  public FindDiggyProject() {
    // TODO Auto-generated constructor stub
  }

  public static void main(String[] args) {
    // compare2();
    // hmm();

    // hmmm();
    // helmet();
    //test1();
    stone();
    // ScreenScanner scanner = new
    // ScreenScanner(Settings.createSettings("daze.properties"));

    System.err.println("done");

  }

  private static void pants() {
    for (int i = 1; i <= 9; i++) {
      FastBitmap fb = new FastBitmap("DIGGY/d" + i + ".png");
      // blueish
      // ColorFiltering cf = new ColorFiltering(new IntRange(39, 100), new
      // IntRange(50, 145), new IntRange(115, 200));
      ColorFiltering cf = new ColorFiltering(new IntRange(39, 104), new IntRange(50, 150), new IntRange(97, 205));
      cf.applyInPlace(fb);
      fb.saveAsPNG("DIGGY/d" + i + "-BLUE.png");
    }
  }

  private static void helmet() {
    ColorFiltering cfy = new ColorFiltering(new IntRange(164, 242), new IntRange(142, 233), new IntRange(53, 116));
    ColorFiltering cfw = new ColorFiltering(new IntRange(198, 246), new IntRange(205, 249), new IntRange(211, 255));
    Threshold t = new Threshold(75);
    for (int i = 1; i <= 10; i++) {
      FastBitmap fb1 = new FastBitmap("DIGGY/d" + i + ".png");
      cfy.applyInPlace(fb1);
      fb1.toGrayscale();
      fb1.saveAsPNG("DIGGY/d" + i + "-Y.png");
      t.applyInPlace(fb1);
      FastBitmap fb2 = new FastBitmap("DIGGY/d" + i + ".png");
      cfw.applyInPlace(fb2);
      fb2.saveAsPNG("DIGGY/d" + i + "-W.png");
      fb2.toGrayscale();
      t.applyInPlace(fb2);
      Add add = new Add(fb2);
      add.applyInPlace(fb1);
      fb1.saveAsPNG("DIGGY/d" + i + "-YW.png");
      Blur blur = new Blur();
      blur.applyInPlace(fb1);
      fb1.saveAsPNG("DIGGY/d" + i + "-YWB.png");
      t.applyInPlace(fb1);
      fb1.saveAsPNG("DIGGY/d" + i + "-YWBT.png");
      BlobDetection bd = new BlobDetection(BlobDetection.Algorithm.EightWay);
      bd.setFilterBlob(true);
      bd.setMinArea(10 * 10);
      bd.setMaxArea(22 * 22);
      List<Blob> blobs = bd.ProcessImage(fb1);
      System.err.println("blobs:" + blobs.size());
      System.err.println(bd.getIdBiggestBlob());
      for (Blob blob : blobs) {
        System.err.println(blob.getCenter());
      }
    }

  }

  
  
  
  
  
  
  private static void stone() {
    ColorFiltering cfy = new ColorFiltering(new IntRange(44, 190), new IntRange(89, 255), new IntRange(0, 65));
    Threshold t = new Threshold(122);
    FastBitmap fb1 = new FastBitmap("images/stone1.png");
    cfy.applyInPlace(fb1);
    fb1.toGrayscale();
    t.applyInPlace(fb1);
    fb1.toRGB();
    fb1.saveAsPNG("images/stone1GR.png");
    
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  private static void gate() {
    ColorFiltering cfy = new ColorFiltering(new IntRange(44, 150), new IntRange(89, 255), new IntRange(0, 30));
    Threshold t = new Threshold(122);
    FastBitmap fb1 = new FastBitmap("images/gate1.png");
    cfy.applyInPlace(fb1);
    fb1.toGrayscale();
    t.applyInPlace(fb1);
    fb1.toRGB();
    fb1.saveAsPNG("images/gate1GR.png");

  }

  private static void pants2() {
    Threshold t = new Threshold(1);
    for (int i = 1; i <= 4; i++) {
      FastBitmap fb = new FastBitmap("DIGGY/pants" + i + ".png");
      fb.toGrayscale();
      t.applyInPlace(fb);
      fb.saveAsPNG("DIGGY/pants" + i + "-TS.png");
    }
  }

  private static void pants3() {
    FastBitmap fb1 = new FastBitmap("DIGGY/pants" + 1 + "-TS.png");
    for (int i = 2; i <= 4; i++) {
      FastBitmap fb = new FastBitmap("DIGGY/pants" + i + "-TS.png");
      And combo = new And(fb);
      combo.applyInPlace(fb1);
      fb1.saveAsPNG("DIGGY/pants" + i + "-AND.png");
    }
    FastBitmap fbAnd = new FastBitmap(fb1);
    fb1 = new FastBitmap("DIGGY/pants" + 1 + "-TS.png");
    for (int i = 2; i <= 4; i++) {
      FastBitmap fb = new FastBitmap("DIGGY/pants" + i + "-TS.png");
      Or or = new Or(fb);
      or.applyInPlace(fb1);
      fb1.saveAsPNG("DIGGY/pants" + i + "-OR.png");
    }
    Xor xor = new Xor(fbAnd);
    xor.applyInPlace(fb1);
    fb1.saveAsPNG("DIGGY/pants" + 4 + "-XOR.png");

    fbAnd.toRGB();
    fb1.toRGB();
    ReplaceColor rc = new ReplaceColor(255, 255, 255);
    rc.ApplyInPlace(fb1, 255, 0, 0);
    Add add = new Add(255, 0, 0);
    add.setOverlayImage(fbAnd);
    add.applyInPlace(fb1);
    fb1.saveAsPNG("DIGGY/pants" + 4 + "-FINAL.png");

  }

  private static void pants4() {
    Threshold t = new Threshold(1);
    for (int i = 1; i <= 9; i++) {
      FastBitmap fb = new FastBitmap("DIGGY/d" + i + ".png");
      // blueish
      // ColorFiltering cf = new ColorFiltering(new IntRange(39, 100), new
      // IntRange(50, 145), new IntRange(115, 200));
      ColorFiltering cf = new ColorFiltering(new IntRange(39, 104), new IntRange(50, 150), new IntRange(97, 205));
      cf.applyInPlace(fb);
      fb.toGrayscale();
      t.applyInPlace(fb);
      fb.saveAsPNG("DIGGY/d" + i + "-HAHA.png");
    }
  }

  private static void test1() {
    long start = System.currentTimeMillis();
    Threshold t = new Threshold(1);
    FastBitmap fb = new FastBitmap("DIGGY/test.png");
    ColorFiltering cf = new ColorFiltering(new IntRange(39, 104), new IntRange(50, 150), new IntRange(97, 205));
    cf.applyInPlace(fb);
    fb.toGrayscale();
    t.applyInPlace(fb);
    long end = System.currentTimeMillis();
    System.err.println("time: " + (end - start));
    ScreenScanner scanner = new ScreenScanner(Settings.createSettings("daze.properties"));
    FastBitmap screen = new FastBitmap(fb);
    FastBitmap image = new FastBitmap("DIGGY/pants4-FINAL.png");
    start = System.currentTimeMillis();
    List<Pixel> matches = scanner.findMatches(image.toBufferedImage(), screen.toBufferedImage(), Color.red);
    removeClosest(matches);
    matches = filterMatches(matches);
    end = System.currentTimeMillis();
    System.err.println("time: " + (end - start));
    System.err.println("matches: " + matches);

    fb.saveAsPNG("DIGGY/test-TS.png");
  }

  private static List<Pixel> filterMatches(List<Pixel> matches) {
    FastBitmap fb = new FastBitmap("DIGGY/test.png");
    ColorFiltering cfy = new ColorFiltering(new IntRange(164, 242), new IntRange(142, 233), new IntRange(53, 116));
    ColorFiltering cfw = new ColorFiltering(new IntRange(198, 246), new IntRange(205, 249), new IntRange(211, 255));
    Threshold t = new Threshold(75);

    List<Pixel> good = new ArrayList<>(3);

    for (Pixel p : matches) {
      BufferedImage helmetImage = fb.toBufferedImage().getSubimage(p.x - 16, p.y - 60, 55, 45);

      FastBitmap fb1 = new FastBitmap(helmetImage);
      cfy.applyInPlace(fb1);
      fb1.toGrayscale();
      t.applyInPlace(fb1);
      FastBitmap fb2 = new FastBitmap(helmetImage);
      cfw.applyInPlace(fb2);
      fb2.toGrayscale();
      t.applyInPlace(fb2);
      Add add = new Add(fb2);
      add.applyInPlace(fb1);
      Blur blur = new Blur();
      blur.applyInPlace(fb1);
      t.applyInPlace(fb1);
      BlobDetection bd = new BlobDetection(BlobDetection.Algorithm.EightWay);
      bd.setFilterBlob(true);
      bd.setMinArea(10 * 10);
      bd.setMaxArea(22 * 22);
      List<Blob> blobs = bd.ProcessImage(fb1);
      System.err.println("blobs:" + blobs.size());
      System.err.println(bd.getIdBiggestBlob());
      for (Blob blob : blobs) {
        System.err.println(blob.getCenter());
      }

      if (blobs.size() > 0) {
        // good we have helmet
        good.add(p);
      }

    }
    return good;
  }

  private static void hmmm() {
    FastBitmap fb = new FastBitmap("green1.png");
    ColorFiltering cf = new ColorFiltering(new IntRange(71, 112), new IntRange(150, 185), new IntRange(32, 61));
    cf.applyInPlace(fb);
    fb.saveAsPNG("green1g.png");
    fb.toGrayscale();
    Threshold ts = new Threshold(10);
    ts.applyInPlace(fb);
    fb.toRGB();
    fb.saveAsPNG("green1ts.png");
  }

  private static void hmm() {
    ScreenScanner scanner = new ScreenScanner(Settings.createSettings("daze.properties"));
    for (int i = 1; i <= 75; i++) {
      FastBitmap screen = new FastBitmap("area" + i + ".png");
      FastBitmap image = new FastBitmap("images/diggyPants.png");
      List<Pixel> matches = scanner.findMatches(image.toBufferedImage(), screen.toBufferedImage(), Color.red);
      removeClosest(matches);

      System.err.print(i + ": " + matches);
      if (!matches.isEmpty()) {
        Pixel p = matches.get(0);
        BufferedImage subimage = screen.toBufferedImage().getSubimage(p.x, p.y - 26, 13, 26);
        FastBitmap fbTeeth = new FastBitmap(subimage);
        fbTeeth.toGrayscale();
        Threshold ts = new Threshold(224);
        ts.applyInPlace(fbTeeth);
        fbTeeth.saveAsPNG("teeth" + i + ".png");
        System.err.println(" " + countPixels(fbTeeth, 250));
      }

    }
  }

  private static int countPixels(FastBitmap fb, int threshold) {
    int cnt = 0;
    for (int x = 0; x < fb.getHeight(); x++) {
      for (int y = 0; y < fb.getWidth(); y++) {
        if (fb.getGray(x, y) > threshold)
          cnt++;

      }
    }
    return cnt;
  }

  private static void removeClosest(List<Pixel> matches) {
    List<Pixel> toRemove = new ArrayList<>();
    if (matches.size() > 1) {
      for (int i = 0; i < matches.size() - 1; i++) {
        Pixel p1 = matches.get(i);
        Pixel p2 = matches.get(i + 1);
        if (Math.abs(p1.x - p2.x) <= 2 && Math.abs(p1.y - p2.y) <= 2) {
          toRemove.add(p1);
        }
      }
    }
    matches.removeAll(toRemove);

  }

  private static void compare2() {
    FastBitmap fb1 = new FastBitmap("dd1.png");
    FastBitmap fb2 = new FastBitmap("dd2.png");
    // ColorFiltering cf = new ColorFiltering(new IntRange(min, max), green, blue)
    int[] argb = fb1.getARGB(9, 4); // y, x
    System.err.println("size:" + fb1.getWidth() + ", " + fb1.getHeight());
    int dt = 4;
    for (int x = 0; x < fb1.getWidth(); x++) {
      for (int y = 0; y < fb1.getHeight(); y++) {
        int[] p1 = fb1.getARGB(y, x);
        int[] p2 = fb2.getARGB(y, x);
        if (Math.abs(p1[1] - p2[1]) <= dt && Math.abs(p1[2] - p2[2]) <= dt && Math.abs(p1[3] - p2[3]) <= dt) {
          // good
        } else {
          fb1.setAlpha(y, x, 0);
          fb1.setRed(y, x, 255);
          fb1.setGreen(y, x, 0);
          fb1.setBlue(y, x, 0);
        }
      }

    }

    fb1.saveAsPNG("ddRESULT.png");
  }

  private static void printArr(int[] arr) {
    for (int i : arr) {
      System.err.print(i + " ");
    }
    System.err.println();
  }
}