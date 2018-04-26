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

  public boolean isWide() {
    return _wide;
  }

  public Pixel[] getShipLocations() {
    return _shipLocations;
  }

  public ScreenScanner(Settings settings) {
    super(settings);
    _popupAreaX = new Rectangle(650, 150, 760, 400);
    campLayout = settings.getProperty("camp.layout", "greece");
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

    _scanArea = new Rectangle(_tl.x + 120, _tl.y + 85, getGameWidth() - 120 - 120, getGameHeight() - 85 - 85);
    _ping2Area = new Rectangle(_tl.x + 120, _tl.y + 19, getGameWidth() - 120 - 120, getGameHeight() - 85 - 19);
    _energyArea = new Rectangle(_tl.x + getGameWidth() / 2 - 100, _tl.y + 19, 300, 22);

    _scampArea = new Rectangle(_scanArea.x + 25, _scanArea.y + 415, getGameWidth() / 2, 65);
    // writeArea(_scanArea, "scanArea.png");

    xx = (getGameWidth() - 140) / 2;
    _logoArea = new Rectangle(_tl.x + xx, _tl.y + 75, 140, 170);

    _popupAreaX = new Rectangle(_tl.x + getGameWidth() / 2 + 144 - (_wide ? 200 : 0), _tl.y,
        400 - 144 + (_wide ? 400 : 0), getGameHeight() / 2 + 50);
    _diggyCaveArea = new Rectangle(_tl.x + getGameWidth() / 2 - 114, _tl.y + 53, 228, 171);

    _buttonArea = generateWindowedArea(576, 600);
    _buttonArea.y = _tl.y + getGameHeight() / 2;
    _buttonArea.height = getGameHeight() / 2;
    getImageData("diggyOnMap.bmp", _scanArea, 20, 19);
    getImageData("claim.bmp", _buttonArea, 36, 13);
    getImageData("claim2.bmp", _buttonArea, 36, 13);
    // getImageData("camp/restartC.png", null, 22, 15);

    // _safePoint = new Pixel(_br.x - 15, _br.y - 15);
    // _parkingPoint = new Pixel(_br);

    _lastLocationButtonArea = new Rectangle(_menuBR.x - 108, _menuBR.y - 38, 60, 36);
    _mapButtonArea = new Rectangle(_menuBR.x - 108, _menuBR.y - 75, 60, 36);
    _campButtonArea = new Rectangle(_menuBR.x - 169, _menuBR.y - 75, 60, 36);

    getImageData("greenArrow.bmp", _lastLocationButtonArea, 17, 22);

    getImageData("map/diggyCave.bmp", _diggyCaveArea, -186, 49);
    getImageData("map/placeEntry.bmp", _scanArea, 28, 20);
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
    LOGGER.fine("Locating game area ... ");

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    boolean notReally = false;
    if (fullScreen) {
      _tl = new Pixel(0, 0);
      _br = new Pixel(screenSize.width - 3, screenSize.height - 130);
      // use default
      setKeyAreas();
      return true;
    } else {
      boolean found = _gameLocator.locateGameArea(null, new ImageData("camp.bmp", null, _comparator, 25, 48), false);
      notReally = true;
      if (found) {
        _tl = _gameLocator.getTopLeft();
        _br = _gameLocator.getBottomRight();
        _menuBR = new Pixel(_br.x, _br.y);
        Rectangle area = new Rectangle(_br.x, _tl.y, screenSize.width - _br.x, getGameHeight() / 2);
        BufferedImage screen = new Robot().createScreenCapture(area);

        ImageData id = getImageData("gems.bmp");
        Pixel p = _comparator.findImage(id.getImage(), screen, id.getColorToBypass());
        if (p != null) {
          _br.x = _br.x + p.x + 41;
          if (notReally) {
            _tl.y = p.y - 43 - 12;
          }
          _wide = false;
        } else {
          LOGGER.warning("GEMS not found!");
          _br.x = screenSize.width - _tl.x - 1;
          _wide = true;
        }
        // _br.y += 48;
        LOGGER.fine("FINAL GAME COORDINATES: " + _tl + " - " + _br);

        setKeyAreas();
        return true;
      }
    }
    return false;
  }

  private Pixel _kitchen;

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
    int xx;
    Rectangle area;
    Pixel p = scanOneFast("greenX.bmp", null, true);
    found = p != null;
    if (!found) {

      // red x - wide popup
      xx = (getGameWidth() - 624) / 2;
      area = new Rectangle(_tl.x + xx + 624 - 30, _tl.y + 71, 60, 42);
      found = scanOneFast("redX.bmp", area, true) != null;

      // red x - tiny popup
      xx = (getGameWidth() - 282) / 2;
      area = new Rectangle(_tl.x + xx + 282, _tl.y + 71, 40, 40);
      found = found || scanOneFast("redX.bmp", area, true) != null;

    }

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
    Pixel p = scanOneFast("diggy2.bmp", area, false);
    if (p != null) {
      LOGGER.info("found diggy: " + p);
      p.x -= 12;
      p.y -= 45;
    } else {
      p = scanOneFast("diggy3.bmp", area, false);
      if (p != null) {
        LOGGER.info("found diggy happy: " + p);
      } else {

        p = scanOneFast("diggy_tired.bmp", area, false);

        if (p != null) {
          LOGGER.info("found diggy tired: " + p);
          p.x -= 11;
          p.y -= 45;
        } else {
          p = scanOne("bluepants.bmp", area, false);
          if (p != null) {
            LOGGER.info("found diggy almost tired: " + p);
            p.x -= 22;
            p.y -= 47;
          }
        }
      }
    }

    return p;
  }

  public boolean isDiggyExactlyHere(Pixel p) throws IOException, AWTException, RobotInterruptedException {
    Rectangle area = new Rectangle(p.x, p.y, 60, 60);
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
    Rectangle area = new Rectangle(pp.x - cellRange * 60, pp.y - cellRange * 60, cellRange * 60 * 2 + 60,
        cellRange * 60 * 2 + 60);
    Pixel res = findDiggy(area);
    // LOGGER.info("Looking for diggy in " + pp + " " + res);
    return res;
  }

  public boolean gotoCamp() throws RobotInterruptedException, IOException, AWTException {
    Rectangle area = new Rectangle(_br.x - 777, _tl.y + 64, 777, 422);
    int tries = 0;
    do {
      tries++;
      _mouse.click(_campButtonArea.x + 32, _mapButtonArea.y + 20);

      _kitchen = scanOneFast("camp/" + campLayout + "/kitchen.png", area, false);
      LOGGER.info("kitchen... " + tries);
      if (_kitchen == null) {
        _mouse.delay(100);
        handlePopups();
      }
    } while (_kitchen == null && tries < 8);
    return _kitchen != null;
  }

  public int checkCamp() throws AWTException, RobotInterruptedException, IOException {
    //LOGGER.info("checkCamp...");
    Rectangle carea = new Rectangle(_campButtonArea);
    carea.width -= 30;
    carea.x += 35;
    //writeArea(carea, "camparea.png");
    
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
    boolean mapMode = false;
    int tries = 0;
    do {
      tries++;
      _mouse.delay(300);
      mapMode = scanForMapButtons();
    } while (mapMode && tries < 15);
    return mapMode;
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
    int position = map.getPosition();
    int tries = 0;
    boolean mapMode = false;
    do {
      tries++;
      _mouse.click(_mapButtonArea.x + 32, _mapButtonArea.y + 20);
      _mouse.delay(2000);
      mapMode = scanForMapButtons();
      if (!mapMode)
        _mouse.delay(5000);
    } while (!mapMode && tries < 4);
    if (mapMode) {
      // GOOD
      LOGGER.info("MAP MODE! " + tries);

      if (position >= 0) {
        // ensure world
        // HARDCODED 4 worlds. I'm too lazy to make it right
        int y = 282;
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
        } else
          slot = 4;
        _mouse.click(_br.x - 37, _tl.y + y + 36 * slot);
        _mouse.delay(2000);
      }

      // click << button
      if (position > 90) {
        _mouse.click(_eastButtons.x + 12, _eastButtons.y + 63);
        _mouse.delay(1750);
        int pos = 100 - position;
        Pixel p = new Pixel(_eastButtons.x - 44, _eastButtons.y + 42);
        p.x = p.x - pos * 84;
        _mouse.click(p);
        _mouse.delay(2000);
        return true;
      } else {

        _mouse.click(_westButtons.x + 12, _westButtons.y + 42);
        _mouse.delay(1750);

        // look for main map
        Rectangle area = new Rectangle(_westButtons.x + 26, _westButtons.y - 18, _eastButtons.x - _westButtons.x - 26,
            _menuBR.y - _eastButtons.y + 18);
        // writeArea(area, "mapsArea.jpg");
        Pixel p = scanOne("map/mainMap.bmp", area, false);
        if (p != null) {
          p.x -= 23;
          LOGGER.info("Found main map...");
          int pos = position;
          int cnt = area.width / 84;
          int pp = p.x + pos * 84 + 42;
          while (pp > _eastButtons.x) {
            // click >> button
            _mouse.click(_eastButtons.x + 12, _eastButtons.y + 42);
            _mouse.delay(1750);
            pos -= cnt;
            pp = p.x + pos * 84 + 42;
          }
          assert pp < _eastButtons.x;
          _mouse.click(pp, _eastButtons.y + 42);
          _mouse.delay(2000);
          return true;
        }
      }
    } else {
      LOGGER.warning("Not sure I'm in maps screen!");
    }

    return false;
  }

  public Pixel findCamp() throws RobotInterruptedException, IOException, AWTException {
    // dragMapToRight();
    Pixel p = scanOne("map/scamp.bmp", _scampArea, false);
    if (p == null) {
      int tries = 0;
      do {
        dragMapToRight();
        _mouse.mouseMove(_safePoint);
        _mouse.delay(1000);
        p = scanOne("map/scamp.bmp", _scampArea, false);
        tries++;
      } while (p == null && tries < 5);
    }
    // p = scanOne("map/scamp.bmp", _scampArea, false);
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
}
