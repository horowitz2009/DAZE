package com.horowitz.daze;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Add;
import Catalano.Imaging.Filters.And;
import Catalano.Imaging.Filters.ColorFiltering;
import Catalano.Imaging.Filters.Difference;
import Catalano.Imaging.Filters.Or;
import Catalano.Imaging.Filters.ReplaceColor;
import Catalano.Imaging.Filters.Threshold;
import Catalano.Imaging.Filters.Xor;

import com.horowitz.commons.SimilarityImageComparator;

public class MotionDetectorTest {

  private SimilarityImageComparator _comparator = new SimilarityImageComparator(0.04, 2000);
  private ColorFiltering _greenColorFiltering = new ColorFiltering(new IntRange(70, 140), new IntRange(110, 255),
      new IntRange(0, 65));

  @Test
  public void test() {
    processGate("gate1");
    processGate("gate2");
    processGate("gate3");
    processGate("gate4");
    processGate("gate5");
    processGate("gate6");
    FastBitmap fbAND = combineAND("gate", 1, 6);
    FastBitmap fbXOR = combineOR("gate", 1, 6);
    
    Xor xor = new Xor(fbAND);
    xor.applyInPlace(fbXOR);

    
    fbAND.toRGB();
    fbXOR.toRGB();
    ReplaceColor rc = new ReplaceColor(255, 255, 255);
    rc.ApplyInPlace(fbXOR, 255, 0, 0);
    Add add = new Add(255, 0, 0);
    add.setOverlayImage(fbAND);
    add.applyInPlace(fbXOR);

    fbXOR.saveAsBMP("temp/gates/RESULT.bmp");
  }

  public void processGate(String suffix) {
    FastBitmap fb1 = new FastBitmap("temp/gates/" + suffix + "1.bmp");
    FastBitmap fb2 = new FastBitmap("temp/gates/" + suffix + "2.bmp");
    assertTrue(fb1 != null);
    assertTrue(fb2 != null);

    fb1.toGrayscale();
    fb2.toGrayscale();

    Threshold threshold2 = new Threshold(50);
    threshold2.applyInPlace(fb2);
    fb2.saveAsBMP("temp/gates/" + suffix + "_resultA.bmp");

    // BinaryOpening opening = new BinaryOpening(2);
    // opening.applyInPlace(fb1);
    // opening.applyInPlace(fb2);
    fb1.saveAsBMP("temp/gates/" + suffix + "1b.bmp");
    fb2.saveAsBMP("temp/gates/" + suffix + "2b.bmp");

    fb2 = new FastBitmap("temp/gates/" + suffix + "2.bmp");
    fb2.toGrayscale();
    Difference difference = new Difference(fb1);

    Threshold threshold = new Threshold(15);

    difference.applyInPlace(fb2);
    // fb2.saveAsBMP("temp/gates/" + suffix + "_resultA.bmp");
    threshold.applyInPlace(fb2);
    fb2.saveAsBMP("temp/gates/" + suffix + "_result.bmp");
  }

  private FastBitmap combineAND(String suffix, int start, int end) {

    FastBitmap fb1 = new FastBitmap("temp/gates/" + suffix + start + "_resultA.bmp");
    for (int i = start + 1; i <= end; i++) {
      FastBitmap fb2 = new FastBitmap("temp/gates/" + suffix + i + "_resultA.bmp");
      And and = new And(fb2);
      and.applyInPlace(fb1);
    }
    fb1.saveAsBMP("temp/gates/" + suffix + "_resultAND.bmp");
    return fb1;
  }

  private FastBitmap combineOR(String suffix, int start, int end) {

    FastBitmap fb1 = new FastBitmap("temp/gates/" + suffix + start + "_resultA.bmp");
    for (int i = start + 1; i <= end; i++) {
      FastBitmap fb2 = new FastBitmap("temp/gates/" + suffix + i + "_resultA.bmp");
      Or or = new Or(fb2);
      or.applyInPlace(fb1);
    }
    fb1.saveAsBMP("temp/gates/" + suffix + "_resultOR.bmp");
    return fb1;
  }

}
