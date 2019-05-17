package com.horowitz.daze;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;

import javax.imageio.ImageIO;

import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.ColorFiltering;

import com.horowitz.commons.BaseScreenScanner;
import com.horowitz.commons.ImageComparator;
import com.horowitz.commons.ImageData;
import com.horowitz.commons.ImageManager;
import com.horowitz.commons.ImageMask;
import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.MyImageIO;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.commons.Settings;
import com.horowitz.commons.TemplateMatcher;
import com.horowitz.daze.map.DMap;
import com.horowitz.ziggy.DiggyFinder;

public class ScreenScanner extends BaseScreenScanner {

  private static final boolean DEBUG = false;

  private Rectangle _scanArea = null;
  private Rectangle _ping2Area;

  private Rectangle _labelArea;
  private Rectangle _levelArea;
  private Rectangle _productionArea3;
  private Rectangle _productionArea2;
  private Rectangle _warehouseArea;;
  private int _labelWidth;
  private Pixel[] _fishes;
  private Pixel[] _shipLocations;
  private Pixel[] _buildingLocations;

  public Rectangle _popupArea;
  public Rectangle _popupAreaX;
  public Rectangle _popupAreaB;
  public Rectangle _logoArea;
  private Pixel _zoomIn;
  private Pixel _zoomOut;
  private Pixel _fullScreen;
  private ImageData _mapButton;
  private ImageData _anchorButton;

  private Rectangle _leftNumbersArea;

  private Rectangle _rightNumbersArea;

  private Rectangle _energyArea;

  private Rectangle _buttonArea;

  public Pixel _menuBR;

  public Rectangle _lastLocationButtonArea;

  public Rectangle _mapButtonArea;

  public Rectangle _campButtonArea;

  public Pixel _eastButtons;

  public Pixel _westButtons;

  public Rectangle _diggyCaveArea;

  public Rectangle _scampArea;

  private boolean _wide;

  public String campLayout;

  public Rectangle _lastMatZone;

  public boolean isWide() {
    return _wide;
  }

  public Pixel[] getShipLocations() {
    return _shipLocations;
  }

  private DiggyFinder diggyFinder;

  public ScreenScanner(Settings settings) {
    super(settings);
    _popupAreaX = new Rectangle(650, 150, 760, 400);
    campLayout = settings.getProperty("camp.layout", "greece");
    diggyFinder = new DiggyFinder(this, settings);
  }

  public Rectangle generateWindowedArea(int width, int height) {
    if (_wide)
      width += 400;
    int xx = (getGameWidth() - width) / 2;
    int yy = (getGameHeight() - height) / 2;
    return new Rectangle(_tl.x + xx, _tl.y + yy, width, height);
  }

  @Override
  protected void setKeyAreas() throws IOException, AWTException, RobotInterruptedException {
    super.setKeyAreas();

    Rectangle area;
    int xx;
    int yy;
    int westOffset = _settings.getInt("scanArea.westOffset", 132);
    int eastOffset = _settings.getInt("scanArea.eastOffset", 85);
    int northOffset = _settings.getInt("scanArea.northOffset", 76);
    int southOffset = _settings.getInt("scanArea.southOffset", 85);
    _scanArea = new Rectangle(_tl.x + westOffset, _tl.y + northOffset, getGameWidth() - westOffset - eastOffset,
        getGameHeight() - northOffset - southOffset);
    _scampArea = new Rectangle(_tl.x + 100, _br.y - 100, getGameWidth() - 200, 100);
    area = new Rectangle(_tl.x + 255, _br.y - 90, getGameWidth() - 255 - 338, 90);
    getImageData("images/campAnchor.png", area, 0, 0);

    Rectangle westButtonArea = new Rectangle(_tl.x, _br.y - 90, 300, 90);

    getImageData("images/campButton.png", westButtonArea, 9, 32);
    getImageData("images/mapButton.png", westButtonArea, 14, 19);
    Rectangle worldButtonArea = new Rectangle(_tl.x + 174, _tl.y + 62, (getGameWidth() / 2) - 174, 150);
    getImageData("images/EG_small.png", worldButtonArea, 11, 13);
    getImageData("images/EG_big.png", worldButtonArea, 12, 16);

    getImageData("images/mapBlack.png", _scampArea, 0, 0);

    // _lastMatZone = new Rectangle(_br.x - 148, _tl.y + 87, 148, 32);
    //
    // _ping2Area = new Rectangle(_tl.x + 120, _tl.y + 19, getGameWidth() - 120 -
    // 120, getGameHeight() - 85 - 19);
    // _energyArea = new Rectangle(_tl.x + getGameWidth() / 2 - 100, _tl.y + 19,
    // 300, 22);
    //
    // _scampArea = new Rectangle(_scanArea.x + 25, _scanArea.y + 415,
    // getGameWidth() / 2, 65);
    // // writeArea(_scanArea, "scanArea.png");
    //
    // xx = (getGameWidth() - 140) / 2;
    // _logoArea = new Rectangle(_tl.x + xx, _tl.y + 75, 140, 170);
    //

    _popupArea = generateWindowedArea(976, getGameHeight() - 120);
    _popupArea.y -= 50;

    _popupAreaX = new Rectangle(_tl.x + getGameWidth() / 2 + 100, _tl.y, getGameWidth() - 300,
        getGameHeight() / 2 + 50);
    getImageData("images/x.png", _popupAreaX, 17, 19);
    getImageData("images/buttonGet2x.png", _popupArea, 45, 5);
    getImageData("images/buttonCollectEnergy.png", _popupArea, 80, 18);
    getImageData("images/buttonRestartAll.png", _popupArea, 50, 5);
    // _diggyCaveArea = new Rectangle(_tl.x + getGameWidth() / 2 - 114, _tl.y + 53,
    // 228, 171);
    //
    // _buttonArea = generateWindowedArea(576, 600);
    // _buttonArea.y = _tl.y + getGameHeight() / 2;
    // _buttonArea.height = getGameHeight() / 2;
    // getImageData("diggyOnMap.bmp", _scanArea, 20, 19);
    // getImageData("claim.bmp", _buttonArea, 36, 13);
    // getImageData("claim2.bmp", _buttonArea, 36, 13);
    // // getImageData("camp/restartC.png", null, 22, 15);
    //
    // // _safePoint = new Pixel(_br.x - 15, _br.y - 15);
    // // _parkingPoint = new Pixel(_br);
    //
    // _lastLocationButtonArea = new Rectangle(_menuBR.x - 108, _menuBR.y - 38, 60,
    // 36);
    // _mapButtonArea = new Rectangle(_menuBR.x - 108, _menuBR.y - 75, 60, 36);
    // _campButtonArea = new Rectangle(_menuBR.x - 169, _menuBR.y - 75, 60, 36);
    //
    // getImageData("greenArrow.bmp", _lastLocationButtonArea, 17, 22);
    //
    // getImageData("map/placeEntry.bmp", _scanArea, 28, 20);
  }

  public boolean scanForMapButtons() throws RobotInterruptedException, IOException, AWTException {

    Rectangle area = new Rectangle(_menuBR.x - 423, _menuBR.y - 88, 44, 88);
    _eastButtons = scanOne("map/eastButtonGroup.bmp", area, false);
    if (_eastButtons != null) {
      area.x = _eastButtons.x - 634;
      if (area.x < 0)
        area.x = 0;
      area.width = 634 - 459 + 30;
      _westButtons = scanOne("map/westButtonGroup.bmp", area, false);
      return _westButtons != null;
    }
    return false;
  }

  public Pixel[] getBuildingLocations() {
    return _buildingLocations;
  }

  public Pixel[] getFishes() {
    return _fishes;
  }

  public Pixel getParkingPoint() {
    return _parkingPoint;
  }

  public Rectangle getProductionArea3() {
    return _productionArea3;
  }

  public Rectangle getProductionArea2() {
    return _productionArea2;
  }

  public Rectangle getWarehouseArea() {
    return _warehouseArea;
  }

  public Rectangle getScanArea() {
    return _scanArea;
  }

  public ImageData getImageData(String filename) throws IOException {
    return getImageData(filename, _scanArea, 0, 0);
  }

  public ImageData getImageData(String filename, Rectangle defaultArea, int xOff, int yOff) throws IOException {
    // if (!new File(filename).exists())
    // return null;

    if (_imageDataCache.containsKey(filename)) {
      return _imageDataCache.get(filename);
    } else {
      ImageData imageData = null;
      try {
        imageData = new ImageData(filename, defaultArea, _comparator, xOff, yOff);
      } catch (IOException e) {
        System.err.println(e);
        return null;
      }
      if (imageData != null)
        _imageDataCache.put(filename, imageData);
      return imageData;
    }
  }

  public boolean locateGameArea(boolean fullScreen) throws AWTException, IOException, RobotInterruptedException {
    boolean found = _gameLocator.locateGameArea(null, getImageData("images/shopAnchor.png", null, 60, 54), false);
    if (found) {
      _tl = _gameLocator.getTopLeft();
      _br = _gameLocator.getBottomRight();
      Rectangle area = new Rectangle(_br.x - 220, _br.y - 715, 220, 145);
      Pixel p = scanOneFast(getImageData("images/gemAnchor.png", null, 140, -14), area, false);
      if (p != null) {
        _tl.y = p.y;
        setKeyAreas();
        return true;
      }
    }

    return false;
  }

  private Pixel _kitchen;
  private Pixel _caravan;
  private Pixel _foundry;

  public Pixel ensureAreaInGame(Rectangle area) throws RobotInterruptedException {
    Rectangle gameArea = new Rectangle(_tl.x, _tl.y, getGameHeight(), getGameHeight());
    int yy = area.y - gameArea.y;

    int x1 = getGameWidth() / 2;
    int y1 = getGameHeight() / 2;

    if (yy < 0) {
      // too north
      _mouse.drag(_tl.x + 5, y1, _tl.x + 5, y1 - yy + 20);
    } else {
      yy = _br.y - (area.y + area.height);
      if (yy < 0)
        // too south
        _mouse.drag(_tl.x + 5, y1, _tl.x + 5, y1 + yy - 20);
    }

    int xx = area.x - _tl.x;

    if (xx < 0) {
      // too west
      _mouse.drag(x1, _br.y - 5, x1 - xx + 20, _br.y - 5);
    } else {
      xx = _br.x - (area.x + area.width);
      if (xx < 0)
        // too east
        _mouse.drag(x1, _br.y - 5, x1 + xx - 20, _br.y - 5);
    }
    return new Pixel(xx, yy);
  }

  public Rectangle getPopupArea() {
    return _popupArea;
  }

  public Rectangle getLabelArea() {
    return _labelArea;
  }

  public Rectangle getLevelArea() {
    return _levelArea;
  }

  public void reduceThreshold() {
    _matcher.setSimilarityThreshold(.85d);
  }

  public void restoreThreshold() {
    _matcher.setSimilarityThreshold(.95d);
  }

  public Pixel getZoomIn() {
    return _zoomIn;
  }

  public void setZoomIn(Pixel zoomIn) {
    _zoomIn = zoomIn;
  }

  public Pixel getZoomOut() {
    return _zoomOut;
  }

  public void setZoomOut(Pixel zoomOut) {
    _zoomOut = zoomOut;
  }

  public Pixel getFullScreen() {
    return _fullScreen;
  }

  public void setFullScreen(Pixel fullScreen) {
    _fullScreen = fullScreen;
  }

  public void zoomOut() throws RobotInterruptedException, IOException {
    if (_zoomOut != null) {
      try {
        Pixel mp = scanOne(_mapButton, null, false);
        if (mp != null) {
          // we're home
          clickZoomOutIfNeeded(mp);
        }

        Pixel ap = scanOne(_anchorButton, null, false);
        if (ap != null) {
          // map opened
          clickZoomOutIfNeeded(mp);
        }
      } catch (AWTException e) {
        e.printStackTrace();
      }
    }

    LOGGER.info("Zooming done!");
  }

  private void clickZoomOutIfNeeded(Pixel mp) throws RobotInterruptedException, IOException, AWTException {
    Rectangle area = new Rectangle(_zoomOut.x - 4, _zoomOut.y, 8, 2);
    Pixel minus = scanOneFast("minus.bmp", area, false);
    if (minus != null) {
      LOGGER.info("ZOOMING OUT! WAIT!");
      _mouse.mouseMove(_zoomOut);
      _mouse.hold(3000);
      // for (int i = 0; i < 14; i++) {
      // _mouse.click(_zoomOut);
      // _mouse.delay(200);
      // }
      // _mouse.click(mp);
      // _mouse.mouseMove(_parkingPoint);
      // _mouse.delay(500);
    }
  }

  public boolean handlePopups() throws IOException, AWTException, RobotInterruptedException {
    boolean found = false;

    Pixel p = scanOneFast("images/buttonCollectEnergy.png", null, true);
    if (p != null) {
      _mouse.delay(400);
      found = true;
    }
    p = scanOneFast("images/x.png", null, true);
    found = p != null;
    LOGGER.info("popups done");

    return found;
  }

  public Rectangle getLeftNumbersArea() {
    return _leftNumbersArea;
  }

  public Rectangle getRightNumbersArea() {
    return _rightNumbersArea;
  }

  public Rectangle getEnergyArea() {
    return _energyArea;
  }

  public Pixel findDiggy(Rectangle area) throws IOException, AWTException, RobotInterruptedException {
    Pixel p = diggyFinder.findDiggy(area);
    if (p != null) {
      p.x -= 30;
      p.y -= 30;
    }
    return p;
    // Pixel p = scanOneFast("diggy2.bmp", area, false);
    // if (p != null) {
    // LOGGER.info("found diggy: " + p);
    // p.x -= 12;
    // p.y -= 45;
    // } else {
    // p = scanOneFast("diggy3.bmp", area, false);
    // if (p != null) {
    // LOGGER.info("found diggy happy: " + p);
    // } else {
    //
    // p = scanOneFast("diggy_tired.bmp", area, false);
    //
    // if (p != null) {
    // LOGGER.info("found diggy tired: " + p);
    // p.x -= 11;
    // p.y -= 45;
    // } else {
    // p = scanOne("bluepants.bmp", area, false);
    // if (p != null) {
    // LOGGER.info("found diggy almost tired: " + p);
    // p.x -= 22;
    // p.y -= 47;
    // }
    // }
    // }
    // }
    //
    // return p;
  }

  public boolean isDiggyExactlyHere(Pixel p) throws IOException, AWTException, RobotInterruptedException {
    Rectangle area = new Rectangle(p.x - 30, p.y - 30, 120, 120);
    return findDiggy(area) != null;
  }

  public boolean isPixelInArea(Pixel p, Rectangle area) {
    return (p.x >= area.x && p.x <= (area.x + area.getWidth()) && p.y >= area.y && p.y <= (area.y + area.getHeight()));
  }

  /**
   * Scanning for Diggy in specified by cellRange area. If cellRange is 0, it
   * scans only in the cell with pp in top left corner.
   * 
   * @param pp
   * @param cellRange
   * @return
   * @throws IOException
   * @throws RobotInterruptedException
   * @throws AWTException
   */
  public Pixel lookForDiggyAroundHere(Pixel pp, int cellRange)
      throws IOException, RobotInterruptedException, AWTException {
    Rectangle area = new Rectangle(pp.x - cellRange * 60 - 30, pp.y - cellRange * 90 - 30, cellRange * 60 * 2 + 120,
        cellRange * 60 * 2 + 120);
    Pixel res = findDiggy(area);
    // LOGGER.info("Looking for diggy in " + pp + " " + res);
    return res;
  }

  public boolean gotoCamp(int tries) throws RobotInterruptedException, IOException, AWTException {
    if (tries <= 0)
      return false;

    Pixel p = scanOneFast("images/campAnchor.png", null, false);
    if (p != null) {
      // we are in the camp already
      LOGGER.info("already in camp...");
    } else {
      /// looking for camp icon
      Pixel pp = scanOneFast("images/campButton.png", null, false);
      if (pp != null) {
        LOGGER.info("click camp button...");
        _mouse.click(pp);
        _mouse.mouseMove(_safePoint);
        _mouse.delay(1000);
        // try again now
        p = scanOneFast("images/campAnchor.png", null, false);
        if (p == null) {
          // try again
          return gotoCamp(tries - 1);
        }
      } else {
        // try with map
        pp = scanOneFast("images/mapButton.png", null, true);
        if (pp == null) {
          handlePopups();
        }
        _mouse.mouseMove(_safePoint);
        _mouse.delay(1500);
        return gotoCamp(tries - 1);
      }
    }
    if (p != null) {
      _kitchen = new Pixel(p.x + 393, p.y - 241);
      _caravan = new Pixel(p.x - 381, p.y - 241);
      _foundry = new Pixel(p.x + 688, p.y - 241);
      return true;
    }

    return false;
  }

  public int checkCamp() throws AWTException, RobotInterruptedException, IOException {
    // LOGGER.info("checkCamp...");
    Rectangle carea = new Rectangle(_campButtonArea);
    carea.width -= 30;
    carea.x += 35;
    // writeArea(carea, "camparea.png");

    Pixel p = findImage("camp/camp1.png", carea, null);
    if (p != null)
      return 1;
    p = findImage("camp/camp2.png", carea, null);
    if (p != null)
      return 2;
    p = findImage("camp/camp3.png", carea, null);
    if (p != null)
      return 3;
    p = findImage("camp/camp4.png", carea, null);
    if (p != null)
      return 4;

    return 0;
  }

  public Pixel getKitchen() {
    return new Pixel(_kitchen);
  }

  public boolean checkIsStillInMap() throws RobotInterruptedException, IOException, AWTException {
    int tries = 0;
    Pixel p;
    do {
      tries++;
      _mouse.delay(300);
      p = scanOneFast("images/mapBlack.png", null, false);
      System.err.println("map balck: " + p);
    } while (p != null && tries < 15);
    return p != null;
  }

  public long checkIsLoading() throws RobotInterruptedException, IOException, AWTException {
    _mouse.delay(750);
    long start = System.currentTimeMillis();
    int xx = getGameWidth() / 2;
    int yy = getGameHeight() / 3;
    Rectangle area = new Rectangle(_tl.x + xx, _tl.y + yy, 400, yy);
    boolean found = false;
    do {
      _mouse.delay(300);
      found = scanOne("map/loadingG.png", area, false) != null;
      if (!found && System.currentTimeMillis() - start < 2000) {
        LOGGER.info("not found, but it's too early");
        found = true;
      }

    } while (found && (System.currentTimeMillis() - start < 9000));

    return (System.currentTimeMillis() - start);
  }

  public boolean gotoMap(DMap map) throws RobotInterruptedException, IOException, AWTException {

    gotoCamp(2);

    // now go to main map

    scanOneFast("images/mapButton.png", null, true);
    _mouse.delay(1000);
    Pixel p = scanOneFast("images/EG_small.png", null, false);
    if (p == null) {
      p = scanOneFast("images/EG_big.png", null, false);
    }
    LOGGER.info("EGYPT: " + p);
    if (p != null) {

      // HARDCODED 6 worlds. I'm too lazy to make it right
      int slot = 0;
      String w = map.getWorld();
      if (w == null)
        w = "temp";
      w = w.toLowerCase();

      if (w.startsWith("eg")) {
        slot = 0;
      } else if (w.startsWith("sc")) {
        slot = 1;
      } else if (w.startsWith("ch")) {
        slot = 2;
      } else if (w.startsWith("at")) {
        slot = 3;
      } else if (w.startsWith("gr")) {
        slot = 4;
      } else if (w.startsWith("nw")) {
        slot = 5;
      } else
        slot = 6;// the stories aka temp world

      p.y += 72 * slot;
      _mouse.click(p);
      _mouse.delay(500);

      LOGGER.info("goto " + map.getName());
      _mouse.click(p.x + map.getCoords().x, p.y + map.getCoords().y);
      _mouse.delay(2000);

      return true;

    }

    return false;
  }

  public Pixel findCamp(DMap map) throws RobotInterruptedException, IOException, AWTException {
    // dragMapToRight();
    long start = System.currentTimeMillis();
    Pixel p = scanOneFast(map.getAnchorImage(), _scampArea, false);
    if (p == null) {
      int tries = 0;
      do {
        // dragMapToRight();
        _mouse.mouseMove(_safePoint);
        _mouse.delay(1000);
        p = scanOne(map.getAnchorImage(), _scampArea, false);
        tries++;
      } while (p == null && tries < 5);
    }
    LOGGER.info("camp: " + p + " in " + (System.currentTimeMillis() - start));
    return p;
  }

  private void dragMapToRight() throws RobotInterruptedException {
    // make sure the map is fully dragged to its left part
    int x1 = _scanArea.x + 5;
    int x2 = _scanArea.x + _scanArea.width / 2 + 5;
    int y = _scanArea.y + _scanArea.height - 35;
    _mouse.drag(x1, y, x2, y);
    _mouse.delay(100);
    // _mouse.drag(x1, y, x2, y);
  }

  public Rectangle getPing2Area() {
    return _ping2Area;
  }

  @Override
  public void reset() {
    _imageDataCache.clear();
    super.reset();
  }

  public Pixel getCaravan() {
    return _caravan;
  }

  public Pixel getFoundry() {
    return _foundry;
  }
}
