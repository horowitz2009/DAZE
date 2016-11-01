package com.horowitz.daze.ocr;

import Catalano.Imaging.FastBitmap;

public class Collect {
public static void main(String[] args) {
  FastBitmap fb1 = new FastBitmap("dest/collect_contract_new.bmp");
  fb1.saveAsBMP("test/collect_gray.bmp");
}
}
