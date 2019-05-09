package com.horowitz.ziggy;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.commons.Settings;
import com.horowitz.daze.ScreenScanner;

import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Concurrent.Filters.Threshold;
import Catalano.Imaging.Filters.Add;
import Catalano.Imaging.Filters.Blur;
import Catalano.Imaging.Filters.ColorFiltering;
import Catalano.Imaging.Tools.Blob;
import Catalano.Imaging.Tools.BlobDetection;

public class DiggyFinder {

  private ScreenScanner scanner;
  private Settings settings;

  public DiggyFinder(ScreenScanner scanner, Settings settings) {
    super();
    this.scanner = scanner;
    this.settings = settings;
  }

  public Pixel findDiggy(Rectangle area) throws RobotInterruptedException, IOException, AWTException {
    BufferedImage screen = new Robot().createScreenCapture(area);
    long start = System.currentTimeMillis();

    Threshold t = new Threshold(1);
    FastBitmap fb = new FastBitmap(screen);
    ColorFiltering cf = new ColorFiltering(new IntRange(39, 104), new IntRange(50, 150), new IntRange(97, 205));
    cf.applyInPlace(fb);
    fb.toGrayscale();
    t.applyInPlace(fb);
    long end = System.currentTimeMillis();
    System.err.println("time1: " + (end - start));
    FastBitmap image = new FastBitmap("images/pantsUNI.png");
    start = System.currentTimeMillis();
    List<Pixel> matches = scanner.findMatches(image.toBufferedImage(), fb.toBufferedImage(), Color.red);
    //System.err.println("matches: " + matches);
    if (!matches.isEmpty()) {
      removeClosest(matches);
      matches = filterMatches(screen, matches);
      end = System.currentTimeMillis();
      System.err.println("time2: " + (end - start));
      System.err.println("matches: " + matches);
    }
    if (!matches.isEmpty()) {
      Pixel p = matches.get(0);
      p.x += (area.x + 6);
      p.y += (area.y - 19);
      return p;
    }
    return null;
  }

  private List<Pixel> filterMatches(BufferedImage screen, List<Pixel> matches) {
    ColorFiltering cfy = new ColorFiltering(new IntRange(164, 242), new IntRange(142, 233), new IntRange(53, 116));
    ColorFiltering cfw = new ColorFiltering(new IntRange(198, 246), new IntRange(205, 249), new IntRange(211, 255));
    Threshold t = new Threshold(75);

    List<Pixel> good = new ArrayList<>(3);
    FastBitmap fb = new FastBitmap(screen);
    for (Pixel p : matches) {

      try {
        if (p.x - 16 < 0 || p.y - 60 < 0) {
          //skip
          continue;
        }
          
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
        //System.err.println("blobs:" + blobs.size());
        //System.err.println(bd.getIdBiggestBlob());
        //for (Blob blob : blobs) {
        //  System.err.println(blob.getCenter());
        //}

        if (blobs.size() > 0) {
          // good we have helmet
          good.add(p);
        }
      } catch (Exception e) {
        System.err.println("error:" + p);
        e.printStackTrace();
      }

    }
    return good;
  }

  private void removeClosest(List<Pixel> matches) {
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

  public Pixel findDiggyOLD(Rectangle area) throws RobotInterruptedException, IOException, AWTException {
    BufferedImage screen = new Robot().createScreenCapture(area);
    List<Pixel> matches = scanner.scanManyFast("images/diggyPants2.png", screen, false);
    if (!matches.isEmpty()) {
      Pixel p = matches.get(0);
      // BufferedImage subimage = screen.getSubimage(p.x, p.y - 26, 13, 26);
      // FastBitmap fbTeeth = new FastBitmap(subimage);
      // fbTeeth.toGrayscale();
      // Threshold ts = new Threshold(224);
      // ts.applyInPlace(fbTeeth);
      // int cnt = countPixels(fbTeeth, 250);
      // if (cnt >= 5 && cnt <= 20) {
      p.x += (area.x + 6);
      p.y += (area.y - 19);
      // return p;
      // }
      return p;
    }
    return null;
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

}
