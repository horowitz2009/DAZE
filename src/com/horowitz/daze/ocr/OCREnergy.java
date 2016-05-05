package com.horowitz.daze.ocr;

import java.awt.image.BufferedImage;
import java.io.IOException;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Threshold;

import com.horowitz.commons.DateUtils;
import com.horowitz.commons.ImageComparator;
import com.horowitz.ocr.OCRB;
import com.horowitz.ocr.OCRe;

public class OCREnergy {

  private final static String OCR_PATH = "energy";
  private final static String OCR_PREFIX = "s";

  private OCRB ocrb;

  public OCREnergy(ImageComparator comparator) throws IOException {
    super();
    ocrb = new OCRB(OCR_PATH + "/" + OCR_PREFIX, comparator);
  }

  public String scanImage(BufferedImage image) {
    FastBitmap fb = new FastBitmap(image);
    if (fb.isRGB())
      fb.toGrayscale();
    Threshold t = new Threshold(200);
    t.applyInPlace(fb);
    fb.saveAsBMP("energy_" + DateUtils.formatDateForFile(System.currentTimeMillis()) + ".bmp");
    return ocrb.scanImage(fb.toBufferedImage());
  }

  // ////////////////////////////

  public static void main(String[] args) {
    OCRe ocr = new OCRe();
    Threshold t = new Threshold(200);
    ocr.setThreshold(t);
    ocr.learn("ocrEnergy", OCR_PREFIX, "images/" + OCR_PATH, true);
  }

}
