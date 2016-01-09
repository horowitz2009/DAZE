package com.horowitz.daze;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Add;
import Catalano.Imaging.Filters.And;
import Catalano.Imaging.Filters.ColorFiltering;
import Catalano.Imaging.Filters.ExtractRGBChannel;
import Catalano.Imaging.Filters.Or;
import Catalano.Imaging.Filters.ReplaceColor;
import Catalano.Imaging.Filters.Xor;
import Catalano.Imaging.Filters.ExtractRGBChannel.Channel;
import Catalano.Imaging.Filters.Threshold;

public class Catalano {

  public static void main(String[] args) {
    Catalano catalano = new Catalano();

    try {
      catalano.extractGate(9);
//      catalano.extractCommon("temp/arr/a", 6, 
//          new ColorFiltering(new IntRange(54, 155), new IntRange(100, 240),
//          new IntRange(5, 85)), 70);
      System.err.println("DONE.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private ColorFiltering _greenColorFiltering = new ColorFiltering(new IntRange(70, 140), new IntRange(110, 255),
      new IntRange(0, 115));

  public void extractGreen() throws IOException {
    BufferedImage image1 = ImageIO.read(new File("gate.bmp"));
    FastBitmap fb1 = new FastBitmap(image1);

    BufferedImage image2 = ImageIO.read(new File("nogreen.bmp"));
    FastBitmap fb2 = new FastBitmap(image2);

    BufferedImage image3 = ImageIO.read(new File("red.bmp"));
    FastBitmap fb3 = new FastBitmap(image3);

    {
      ExtractRGBChannel extractRGBChannel = new ExtractRGBChannel(Channel.G);
      FastBitmap extractFB = extractRGBChannel.Extract(fb1);
      extractFB.saveAsBMP("gateGreen.bmp");

      FastBitmap extractFB2 = extractRGBChannel.Extract(fb2);
      extractFB2.saveAsBMP("nogreenGreen.bmp");

      FastBitmap extractFB3 = extractRGBChannel.Extract(fb3);
      extractFB3.saveAsBMP("redGreen.bmp");
    }
    {
      ExtractRGBChannel extractRGBChannel = new ExtractRGBChannel(Channel.R);
      FastBitmap extractFB = extractRGBChannel.Extract(fb1);
      extractFB.saveAsBMP("gateRed.bmp");

      FastBitmap extractFB2 = extractRGBChannel.Extract(fb2);
      extractFB2.saveAsBMP("nogreenRed.bmp");

      FastBitmap extractFB3 = extractRGBChannel.Extract(fb3);
      extractFB3.saveAsBMP("redRed.bmp");
    }

    _greenColorFiltering.applyInPlace(fb1);

    fb1.saveAsBMP("gateFiltered.bmp");
    ExtractRGBChannel extractGreen = new ExtractRGBChannel(Channel.G);
    FastBitmap extractFB = extractGreen.Extract(fb1);
    extractFB.saveAsBMP("gateG2.bmp");
    System.out.println("done");

    _greenColorFiltering = new ColorFiltering(new IntRange(70, 140), new IntRange(110, 255), new IntRange(0, 65));

    // tryGreen("gate", 1, 20);
    // tryGreen("gate", 2, 50);
    // tryGreen("gate", 3, 70);
    // tryGreen("gate", 4, 110);

    int low = 116;
    int high = 255;
    ColorFiltering cf = new ColorFiltering(new IntRange(low, high), new IntRange(low, high), new IntRange(low, high));

    applyFilter("gate", 11, cf, Channel.G);
    applyFilter("gateG", 11, cf, Channel.G);
    applyFilter("gateR", 11, cf, Channel.G);
    applyFilter("gold", 11, cf, Channel.G);
    applyFilter("goldG", 11, cf, Channel.G);
    applyFilter("goldR", 11, cf, Channel.G);
    applyFilter("pink", 11, cf, Channel.G);
    applyFilter("pinkG", 11, cf, Channel.G);
    applyFilter("pinkR", 11, cf, Channel.G);

    applyFilter("bell", 11, cf, Channel.G);
    applyFilter("bellG", 11, cf, Channel.G);
    applyFilter("bellR", 11, cf, Channel.G);

    applyFilter("hay", 11, cf, Channel.G);
    applyFilter("hayG", 11, cf, Channel.G);
    applyFilter("hayR", 11, cf, Channel.G);

    applyFilter("rack", 11, cf, Channel.G);
    applyFilter("rackG", 11, cf, Channel.G);
    applyFilter("rackR", 11, cf, Channel.G);

    applyFilter("bluegift", 11, cf, Channel.G);
    applyFilter("bluegiftG", 11, cf, Channel.G);
    applyFilter("bluegiftR", 11, cf, Channel.G);

    applyFilter("redgift", 11, cf, Channel.G);
    applyFilter("redgiftG", 11, cf, Channel.G);
    applyFilter("redgiftR", 11, cf, Channel.G);

  }

  public void extractGate(int numb) throws IOException {
    ColorFiltering cf = _greenColorFiltering;

    for (int i = 1; i <= numb; i++) {
      applyFilterGate("temp/gate" + i, "GR", cf, null);
    }
    process("temp/gate", numb);
  }

  public void extractCommon(String prefix, int numb, ColorFiltering cf, int threshold) throws IOException {
    for (int i = 1; i <= numb; i++) {
      applyFilterCommon(prefix + i, "GR", cf, threshold);
    }
    process(prefix, numb);
  }

  private void tryGreen(String prefix, int t, int lowgreen) throws IOException {
    BufferedImage image1 = ImageIO.read(new File(prefix + ".bmp"));
    FastBitmap fb1 = new FastBitmap(image1);

    _greenColorFiltering = new ColorFiltering(new IntRange(70, 140), new IntRange(lowgreen, 255), new IntRange(0, 65));
    _greenColorFiltering.applyInPlace(fb1);
    fb1.saveAsBMP(prefix + t + ".bmp");
  }

  private void applyFilter(String prefix, int t, ColorFiltering colorFiltering, Channel channel) throws IOException {
    BufferedImage image1 = ImageIO.read(new File(prefix + ".bmp"));
    FastBitmap fb1 = new FastBitmap(image1);
    if (channel != null) {
      ExtractRGBChannel extractChannel = new ExtractRGBChannel(channel);
      fb1 = extractChannel.Extract(fb1);
    }
    if (fb1.isRGB())
      fb1.toGrayscale();
    System.err.println(fb1.isRGB());
    fb1.saveAsBMP("temp/temp" + t + ".bmp");
    //
    // image1 = ImageIO.read(new File("temp.bmp"));
    // fb1 = new FastBitmap(image1);

    Threshold thr = new Threshold(170);
    thr.applyInPlace(fb1);
    // colorFiltering.applyInPlace(fb1);
    fb1.saveAsBMP(prefix + t + ".bmp");

  }

  private void applyFilterRed(String prefix, int t, ColorFiltering colorFiltering, Channel channel) throws IOException {
    BufferedImage image1 = ImageIO.read(new File(prefix + ".bmp"));
    FastBitmap fb1 = new FastBitmap(image1);
    // if (channel != null) {
    // ExtractRGBChannel extractChannel = new ExtractRGBChannel(channel);
    // fb1 = extractChannel.Extract(fb1);
    // }

    colorFiltering.applyInPlace(fb1);

    if (fb1.isRGB())
      fb1.toGrayscale();
    // fb1.saveAsBMP("temp.bmp");
    //
    // image1 = ImageIO.read(new File("temp.bmp"));
    // fb1 = new FastBitmap(image1);

    Threshold thr = new Threshold(40);
    thr.applyInPlace(fb1);
    fb1.saveAsBMP(prefix + t + ".bmp");

  }

  private void applyFilterGate(String prefix, String sign, ColorFiltering colorFiltering, Channel channel)
      throws IOException {
    BufferedImage image1 = ImageIO.read(new File(prefix + ".bmp"));
    FastBitmap fb1 = new FastBitmap(image1);

    ColorFiltering colorFiltering2 = new ColorFiltering(new IntRange(45, 80), new IntRange(95, 255), new IntRange(0,
        120));
    colorFiltering2.applyInPlace(fb1);
    if (fb1.isRGB())
      fb1.toGrayscale();

    fb1.saveAsBMP(prefix + "temp" + sign + ".bmp");

    Threshold thr = new Threshold(70);
    thr.applyInPlace(fb1);

    fb1.saveAsBMP(prefix + sign + ".bmp");
  }

  private void applyFilterCommon(String prefix, String sign, ColorFiltering colorFiltering, int threshold)
      throws IOException {
    BufferedImage image1 = ImageIO.read(new File(prefix + ".bmp"));
    FastBitmap fb1 = new FastBitmap(image1);

    colorFiltering.applyInPlace(fb1);
    if (fb1.isRGB())
      fb1.toGrayscale();

    fb1.saveAsBMP(prefix + "temp" + sign + ".bmp");

    Threshold thr = new Threshold(threshold);
    thr.applyInPlace(fb1);

    fb1.saveAsBMP(prefix + sign + ".bmp");
  }

  public void extractRed() throws IOException {

    int low = 116;
    int high = 255;
    ColorFiltering cf = new ColorFiltering(new IntRange(110, 200), new IntRange(0, 50), new IntRange(0, 40));

    int index = 23;

    applyFilterRed("gate", index, cf, Channel.R);
    applyFilterRed("gateG", index, cf, Channel.R);
    applyFilterRed("gateR", index, cf, Channel.R);
    applyFilterRed("gold", index, cf, Channel.R);
    applyFilterRed("goldG", index, cf, Channel.R);
    applyFilterRed("goldR", index, cf, Channel.R);
    applyFilterRed("pink", index, cf, Channel.R);
    applyFilterRed("pinkG", index, cf, Channel.R);
    applyFilterRed("pinkR", index, cf, Channel.R);

    applyFilterRed("bell", index, cf, Channel.R);
    applyFilterRed("bellG", index, cf, Channel.R);
    applyFilterRed("bellR", index, cf, Channel.R);

    applyFilterRed("hay", index, cf, Channel.R);
    applyFilterRed("hayG", index, cf, Channel.R);
    applyFilterRed("hayR", index, cf, Channel.R);

    applyFilterRed("rack", index, cf, Channel.R);
    applyFilterRed("rackG", index, cf, Channel.R);
    applyFilterRed("rackR", index, cf, Channel.R);

    applyFilterRed("bluegift", index, cf, Channel.R);
    applyFilterRed("bluegiftG", index, cf, Channel.R);
    applyFilterRed("bluegiftR", index, cf, Channel.R);

    applyFilterRed("redgift", index, cf, Channel.R);
    applyFilterRed("redgiftG", index, cf, Channel.R);
    applyFilterRed("redgiftR", index, cf, Channel.R);

  }

  private void process(String prefix, int n) {
    FastBitmap fbAND = processAND(prefix, n);
    FastBitmap fbXOR = processOR(prefix, n);

    Xor xor = new Xor(fbAND);
    xor.applyInPlace(fbXOR);
    // try {
    // // This is the final result
    // ImageIO.write(fbXOR.toBufferedImage(), "BMP", new File("ocr/digitXOR" +
    // prefix + ".bmp"));
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }

    fbAND.toRGB();
    fbXOR.toRGB();
    ReplaceColor rc = new ReplaceColor(255, 255, 255);
    rc.ApplyInPlace(fbXOR, 255, 0, 0);
    Add add = new Add(255, 0, 0);
    add.setOverlayImage(fbAND);
    add.applyInPlace(fbXOR);
    try {
      // This is the final result
      ImageIO.write(fbXOR.toBufferedImage(), "BMP", new File(prefix + "FINAL.bmp"));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private FastBitmap processAND(String prefix, int numImages) {
    try {
      BufferedImage image = ImageIO.read(new File(prefix + 1 + "GR.bmp"));
      FastBitmap fb1 = new FastBitmap(image);

      // fb1.toGrayscale();
      for (int i = 2; i <= numImages; i++) {

        image = ImageIO.read(new File(prefix + i + "GR.bmp"));
        FastBitmap fb = new FastBitmap(image);
        // fb.toGrayscale();

        And and = new And(fb);
        and.applyInPlace(fb1);
        // ImageIO.write(fb1.toBufferedImage(), "BMP", new File(prefix + i +
        // ".bmp"));
      }
      ImageIO.write(fb1.toBufferedImage(), "BMP", new File(prefix + "AND.bmp"));
      System.out.println("Done.");
      return fb1;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private FastBitmap processOR(String prefix, int numImages) {
    try {
      BufferedImage image = ImageIO.read(new File(prefix + 1 + "GR.bmp"));
      FastBitmap fb1 = new FastBitmap(image);

      // fb1.toGrayscale();
      for (int i = 2; i <= numImages; i++) {

        image = ImageIO.read(new File(prefix + i + "GR.bmp"));
        FastBitmap fb = new FastBitmap(image);
        // fb.toGrayscale();

        Or xor = new Or(fb);
        xor.applyInPlace(fb1);
      }
      ImageIO.write(fb1.toBufferedImage(), "BMP", new File(prefix + "OR.bmp"));
      System.out.println("Done.");

      return fb1;

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

}
