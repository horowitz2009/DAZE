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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.ColorFiltering;

import com.horowitz.commons.GameLocator;
import com.horowitz.commons.ImageComparator;
import com.horowitz.commons.ImageData;
import com.horowitz.commons.ImageManager;
import com.horowitz.commons.ImageMask;
import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.MyImageIO;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.commons.Settings;
import com.horowitz.commons.SimilarityImageComparator;
import com.horowitz.commons.TemplateMatcher;
import com.horowitz.daze.map.DMap;

public class ScreenScanner {

  private final static Logger LOGGER = Logger.getLogger("MAIN");

  private static final boolean DEBUG = false;

  private ImageComparator _comparator;
  TemplateMatcher _matcher;
  private MouseRobot _mouse;

  private Pixel _br = null;
  private Pixel _tl = null;
  private boolean _optimized = false;
  private boolean _debugMode = false;

  private Rectangle _scanArea = null;
  private Rectangle _ping2Area;

  private GameLocator _gameLocator;

  private Map<String, ImageData> _imageDataCache;
  private Pixel _safePoint;
  private Pixel _parkingPoint;
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

  public boolean isWide() {
    return _wide;
  }

  public Pixel[] getShipLocations() {
    return _shipLocations;
  }

  public ScreenScanner(Settings settings) {
    _comparator = new SimilarityImageComparator(0.04, 2000);
    _matcher = new TemplateMatcher();
    // _matcher.setSimilarityThreshold(.91d);

    try {
      _mouse = new MouseRobot();
    } catch (AWTException e1) {
      e1.printStackTrace();
    }
    _gameLocator = new GameLocator();
    _imageDataCache = new Hashtable<String, ImageData>();

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle area = new Rectangle(20, 340, screenSize.width - 20 - 404, screenSize.height - 340 - 110);
    try {

      _tl = new Pixel(0, 0);
      _br = new Pixel(screenSize.width - 3, screenSize.height - 3);
      _popupAreaX = new Rectangle(650, 150, 760, 400);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public Rectangle generateWindowedArea(int width, int height) {
    if (_wide)
      width += 400;
    int xx = (getGameWidth() - width) / 2;
    int yy = (getGameHeight() - height) / 2;
    return new Rectangle(_tl.x + xx, _tl.y + yy, width, height);
  }

  private void setKeyAreas() throws IOException, AWTException, RobotInterruptedException {

    _optimized = true;

    Rectangle area;
    int xx;
    int yy;

    _scanArea = new Rectangle(_tl.x + 120, _tl.y + 85, getGameWidth() - 120 - 120, getGameHeight() - 85 - 85);
    _ping2Area = new Rectangle(_tl.x + 120, _tl.y + 19, getGameWidth() - 120 - 120, getGameHeight() - 85 - 19);
    _energyArea = new Rectangle(_tl.x + getGameWidth() / 2 - 18, _tl.y + 21, 200, 17);

    _scampArea = new Rectangle(_scanArea.x + 280, _scanArea.y + 415, getGameWidth() - 120 - 120 - 280, 65);
    // writeArea(_scanArea, "scanArea.png");

    xx = (getGameWidth() - 140) / 2;
    _logoArea = new Rectangle(_tl.x + xx, _tl.y + 75, 140, 170);

    _popupAreaX = new Rectangle(_tl.x + getGameWidth() / 2 + 144 - (_wide ? 200 : 0), _tl.y, 400 - 144 + (_wide ? 400
        : 0), getGameHeight() / 2 + 50);
    _diggyCaveArea = new Rectangle(_tl.x + getGameWidth() / 2 - 114, _tl.y + 53, 228, 171);

    xx = (getGameWidth() - 200 - (_wide ? 400 : 0)) / 2;
    _buttonArea = new Rectangle(_tl.x + xx, _br.y - (70 + 87), xx, 87);

    getImageData("diggyOnMap.bmp", _scanArea, 20, 19);
    getImageData("claim.bmp", _buttonArea, 36, 13);
    getImageData("camp/restartu.bmp", null, 22, 15);

    _safePoint = new Pixel(_br.x - 15, _br.y - 15);
    _parkingPoint = new Pixel(_br);

    _lastLocationButtonArea = new Rectangle(_menuBR.x - 108, _menuBR.y - 38, 60, 36);
    _mapButtonArea = new Rectangle(_menuBR.x - 108, _menuBR.y - 75, 60, 36);
    _campButtonArea = new Rectangle(_menuBR.x - 169, _menuBR.y - 75, 60, 36);

    getImageData("greenArrow.bmp", _lastLocationButtonArea, 17, 22);

    getImageData("map/diggyCave.bmp", _diggyCaveArea, -186, 49);
    getImageData("map/placeEntry.bmp", _scanArea, 28, 20);

    // int xxx = (getGameWidth() - 137) / 2;
    // _leftNumbersArea = new Rectangle(_tl.x, _tl.y, xxx, 72);
    // _rightNumbersArea = new Rectangle(_br.x - xxx, _tl.y, xxx, 72);
    //
    // _sailorsPos = scanOne("sailors.bmp", _rightNumbersArea, false);
    //
    //
    // _fishes = new Pixel[] { new Pixel(-94, 14), new Pixel(-169, -13), new
    // Pixel(-223, -49), new Pixel(-282, -89),
    // new Pixel(-354, -120) };
    //
    // _shipLocations = new Pixel[] { new Pixel(103, 83), new Pixel(103, 187),
    // new Pixel(103, 278) };
    // // _buildingLocations = new Pixel[] { new Pixel(54, -71), new Pixel(147,
    // // -100), new Pixel(-50, -120) };
    // _buildingLocations = new Pixel[] { new Pixel(147, -100) };
    //
    // // label area
    // _labelWidth = 380;
    // xx = (getGameWidth() - _labelWidth) / 2;
    // _labelArea = new Rectangle(_tl.x + xx, _tl.y + 71, _labelWidth, 66);
    // _levelArea = new Rectangle(_tl.x + xx, _tl.y + 360, _labelWidth, 35);
    //
    // // _popupArea = generateWindowedArea(324, 516);
    // _popupArea = generateWindowedArea(328, 520);
    // _popupAreaX = new Rectangle(_popupArea);
    // _popupAreaX.x += (20 + _popupAreaX.width / 2);
    // _popupAreaX.y -= 7;
    // _popupAreaX.height = 60;
    // _popupAreaX.width = 270 + _popupAreaX.width / 2;
    //
    // _popupAreaB = new Rectangle(_popupArea);
    // _popupAreaB.y = _popupAreaB.y + _popupAreaB.height - 125;
    // _popupAreaB.height = 125;
    //
    // _safePoint = new Pixel(_br.x - 15, _br.y - 15);
    // _parkingPoint = new Pixel(_br);
    //
    // getImageData("ROCK.bmp", _scanArea, 10, 44);
    // getImageData("pin.bmp", _scanArea, 6, 6);
    //
    // area = new Rectangle(_br.x - 110, _br.y - 75, 60, 40);
    // _anchorButton = getImageData("anchor.bmp", area, 20, 7);
    // _mapButton = getImageData("mapButton.bmp", area, 20, 7);
    //
    // area = new Rectangle(_br.x - 30, _tl.y + 100, 30, getGameHeight() / 2 -
    // 100);
    // ImageData sb = getImageData("scoreBoard.bmp", area, 0, 17);
    // sb.setDefaultArea(area);
    //
    // try {
    // Pixel sbp = scanPrecise(sb, null);
    // if (sbp != null) {
    // _zoomIn = new Pixel(sbp.x + 8, sbp.y + 108);
    // _zoomOut = new Pixel(sbp.x + 8, sbp.y + 141);
    // _fullScreen = new Pixel(sbp.x + 8, sbp.y + 179);
    // LOGGER.info("left toolbar ok!");
    // } else {
    // _zoomIn = null;
    // _zoomOut = null;
    // _fullScreen = null;
    // LOGGER.info("left toolbar NOT FOUND!");
    // }
    // } catch (AWTException e) {
    // e.printStackTrace();
    // } catch (RobotInterruptedException e) {
    // }
    //
    // // ATTENTION - Destinations are fixed in deserilizeDestinations()
    //
    // // getImageData("dest/missing.bmp", _scanArea, 41, 45);
    // getImageData("dest/setSail.bmp", _popupArea, 30, 6);
    //
    // ImageData gear2 = getImageData("buildings/gears2.bmp", _popupArea, 0, 0);
    // gear2.setColorToBypass(Color.BLACK);
    // ImageData wa = getImageData("buildings/whiteArrow.bmp", _popupArea, 0,
    // 0);
    // wa.setColorToBypass(Color.BLACK);
    //
    // getImageData("buildings/produce.bmp", _popupAreaB, 0, 0);
    // getImageData("buildings/produce2.bmp", _popupAreaB, 0, 0);
    // getImageData("buildings/produceGray.bmp", _popupAreaB, 0, 0);
    // getImageData("buildings/x.bmp", _popupAreaX, 10, 10);
    // getImageData("greenX.bmp", new Rectangle(_br.x - 28, _tl.y + 57, 22, 20),
    // 9, 9);
    //
    // /*
    // * _destinations.add(new Destination("Small Town", 5,
    // getImageData("buildings/SmallTown.bmp"),getImageData("buildings/SmallTownTitle.bmp")));
    // _destinations.add(new Destination("Coastline", 15,
    // *
    // getImageData("buildings/Coastline.bmp"),getImageData("buildings/coastlineTitle.bmp")));
    // */
    //
    // /*
    // * _hooray = new ImageData("Hooray.bmp", area, _comparator, 23, 6);
    // *
    // * getImageData("tags/zzz.bmp", _scanArea, 0, 7);
    // getImageData("tags/coins.bmp", _scanArea, 0, 9);
    // getImageData("tags/houses.bmp", _scanArea, 0, 9);
    // getImageData("tags/fire.bmp", _scanArea, 0,
    // 7);
    // * getImageData("tags/medical.bmp", _scanArea, 14, 9);
    // getImageData("tags/greenDown.bmp", _scanArea, 18, -35);
    // getImageData("buildings/Warehouse.bmp", _scanArea, 35, 0);
    // *
    // * area = new Rectangle(_br.x - 264, _tl.y, 264, 35);
    // getImageData("populationRed.bmp", area, 0, 0);
    // getImageData("populationBlue.bmp", area, 0, 0);
    // */
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

    _tl = new Pixel(0, 0);

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    _br = new Pixel(screenSize.width - 3, screenSize.height - 130);
    boolean notReally = false;
    if (fullScreen) {
      // use default
      setKeyAreas();
      return true;
    } else {
      boolean found = _gameLocator.locateGameArea(
          null, new ImageData(
          "camp.bmp", null, _comparator, 25, 48), false);
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

  private Pixel _rock = null;

  private Pixel _kitchen;

  public void reset() {
    _rock = null;
    _optimized = false;
    _tl = null;
    _br = null;
  }

  public boolean checkAndAdjustRock() throws IOException, AWTException, RobotInterruptedException {
    boolean needRecalc = true;
    if (_rock == null) {
      _rock = findRock();
      LOGGER.info("rock found for the first time.");
      needRecalc = true;
    } else {
      Pixel newRock = findRockAgain(_rock);
      needRecalc = !_rock.equals(newRock);
      _rock = newRock;
      if (!needRecalc) {
        LOGGER.info("rock found in the same place.");
        LOGGER.info("Skipping recalc...");
      }
    }

    Pixel goodRock = new Pixel(_tl.x + getGameWidth() / 2 + 93, _tl.y + 220);

    if (Math.abs(_rock.x - goodRock.x) > 5 && Math.abs(_rock.x - goodRock.y) > 2) {
      // need adjusting
      _mouse.drag2(_rock.x, _rock.y, goodRock.x, goodRock.y);
      _mouse.delay(1200);
      _rock = findRockAgain(goodRock);
      needRecalc = true;
    }

    return needRecalc;
  }

  public Pixel findRock() throws IOException, AWTException, RobotInterruptedException {
    Rectangle area = new Rectangle(_tl.x + 350, _tl.y + 43, 860, getGameHeight() - 43);

    Pixel p = scanOne("ROCK.bmp", area, false);
    // writeImage(area, "admArea1.png");
    if (p == null) {
      LOGGER.info("Rock try 2 ...");
      p = scanOne("ROCK.bmp", getScanArea(), false);
    }
    _rock = p;
    return p;
  }

  public Pixel findRockAgain(Pixel oldRock) throws IOException, AWTException, RobotInterruptedException {
    ImageData rockData = getImageData("ROCK.bmp");
    Rectangle area = rockData.getDefaultArea();
    if (oldRock != null) {
      int x = oldRock.x - rockData.get_xOff();
      int y = oldRock.y - rockData.get_yOff();
      area = new Rectangle(x - 20, y - 20, 31 + 40, 28 + 40);
    }
    Pixel p = scanOne("ROCK.bmp", area, false);
    if (p == null) {
      LOGGER.info("Rock not found in the same place.");
      LOGGER.info("Looking again for the rock...");
      p = findRock();
    }
    return p;
  }

  public void writeArea(Rectangle rect, String filename) {
    MyImageIO.writeArea(rect, filename);
  }

  public void writeImage(BufferedImage image, String filename) {
    MyImageIO.writeImage(image, filename);
  }

  public void captureGameAreaDT() {
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd  HH-mm-ss-SSS");
    String date = sdf.format(Calendar.getInstance().getTime());
    String filename = "popup " + date + ".png";
    captureGameArea(filename);
  }

  public void captureGameArea(String filename) {
    writeArea(new Rectangle(new Point(_tl.x, _tl.y), new Dimension(getGameWidth(), getGameHeight())), filename);
  }

  public Pixel locateImageCoords(String imageName, Rectangle[] area, int xOff, int yOff) throws AWTException,
      IOException, RobotInterruptedException {

    final Robot robot = new Robot();
    final BufferedImage image = ImageIO.read(ImageManager.getImageURL(imageName));
    Pixel[] mask = new ImageMask(imageName).getMask();
    BufferedImage screen;
    int turn = 0;
    Pixel resultPixel = null;
    // MouseRobot mouse = new MouseRobot();
    // mouse.saveCurrentPosition();
    while (turn < area.length) {

      screen = robot.createScreenCapture(area[turn]);
      List<Pixel> foundEdges = findEdge(image, screen, _comparator, null, mask);
      if (foundEdges.size() >= 1) {
        // found
        // AppConsole.print("found it! ");
        int y = area[turn].y;
        int x = area[turn].x;
        resultPixel = new Pixel(foundEdges.get(0).x + x + xOff, foundEdges.get(0).y + y + yOff);
        // System.err.println("AREA: [" + turn + "] " + area[turn]);
        break;
      }
      turn++;
    }
    // mouse.checkUserMovement();
    // AppConsole.println();
    return resultPixel;
  }

  public boolean isOptimized() {
    return _optimized && _br != null && _tl != null;
  }

  private List<Pixel> findEdge(final BufferedImage targetImage, final BufferedImage area, ImageComparator comparator,
      Map<Integer, Color[]> colors, Pixel[] indices) {
    if (DEBUG)
      try {
        MyImageIO.write(area, "PNG", new File("C:/area.png"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    List<Pixel> result = new ArrayList<Pixel>(8);
    for (int i = 0; i < (area.getWidth() - targetImage.getWidth()); i++) {
      for (int j = 0; j < (area.getHeight() - targetImage.getHeight()); j++) {
        final BufferedImage subimage = area.getSubimage(i, j, targetImage.getWidth(), targetImage.getHeight());
        if (DEBUG)
          try {
            MyImageIO.write(subimage, "PNG", new File("C:/subimage.png"));
          } catch (IOException e) {
            e.printStackTrace();
          }
        if (comparator.compare(targetImage, subimage, colors, indices)) {
          // System.err.println("FOUND: " + i + ", " + j);
          result.add(new Pixel(i, j));
          if (result.size() > 0) {// increase in case of trouble
            break;
          }
        }
      }
    }
    return result;
  }

  public void scan() {
    try {
      Robot robot = new Robot();

      BufferedImage screenshot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
      if (DEBUG)
        MyImageIO.write(screenshot, "PNG", new File("screenshot.png"));
    } catch (HeadlessException | AWTException | IOException e) {

      e.printStackTrace();
    }

  }

  public void compare(String imageName1, String imageName2) throws IOException {
    final BufferedImage image1 = ImageIO.read(ImageManager.getImageURL(imageName1));
    Pixel[] mask1 = new ImageMask(imageName1).getMask();
    final BufferedImage image2 = ImageIO.read(ImageManager.getImageURL(imageName2));
    Pixel[] mask2 = new ImageMask(imageName2).getMask();

    List<Pixel> res = compareImages(image1, image2, _comparator, mask2);

    // System.err.println(res);
  }

  public List<Pixel> compareImages(final BufferedImage image1, final BufferedImage image2, ImageComparator comparator,
      Pixel[] indices) {
    if (DEBUG)
      try {
        ImageIO.write(image2, "PNG", new File("area.png"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    List<Pixel> result = new ArrayList<Pixel>(8);
    for (int i = 0; i <= (image2.getWidth() - image1.getWidth()); i++) {
      for (int j = 0; j <= (image2.getHeight() - image1.getHeight()); j++) {
        final BufferedImage subimage = image2.getSubimage(i, j, image1.getWidth(), image1.getHeight());
        if (DEBUG)
          try {
            MyImageIO.write(subimage, "PNG", new File("subimage.png"));
          } catch (IOException e) {
            e.printStackTrace();
          }

        boolean b = comparator.compare(image1, image2, null, indices);
        // System.err.println("equal: " + b);
        indices = null;
        b = comparator.compare(image1, image2, null, indices);
        // System.err.println("equal2: " + b);
        List<Pixel> list = comparator.findSimilarities(image1, subimage, indices);
        // System.err.println("FOUND: " + list);
      }
    }
    return result;
  }

  public Pixel getBottomRight() {
    return _br;
  }

  public Pixel getTopLeft() {
    return _tl;
  }

  public int getGameWidth() {
    int width = isOptimized() ? _br.x - _tl.x : Toolkit.getDefaultToolkit().getScreenSize().width;
    return width != 0 ? width : Toolkit.getDefaultToolkit().getScreenSize().width;
  }

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

  public int getGameHeight() {
    if (isOptimized()) {
      return _br.y - _tl.y == 0 ? Toolkit.getDefaultToolkit().getScreenSize().height : _br.y - _tl.y;
    } else {
      return Toolkit.getDefaultToolkit().getScreenSize().height;
    }
  }

  public void addHandler(Handler handler) {
    LOGGER.addHandler(handler);
  }

  public ImageData generateImageData(String imageFilename) throws IOException {
    return new ImageData(imageFilename, null, _comparator, 0, 0);
  }

  public ImageData setImageData(String imageFilename) throws IOException {
    return getImageData(imageFilename, _scanArea, 0, 0);
  }

  public ImageData generateImageData(String imageFilename, int xOff, int yOff) throws IOException {
    return new ImageData(imageFilename, null, _comparator, xOff, yOff);
  }

  public ImageComparator getComparator() {
    return _comparator;
  }

  public Pixel getSafePoint() {
    return _safePoint;
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

  public ImageComparator getImageComparator() {
    return _comparator;
  }

  public List<Pixel> scanMany(String filename, BufferedImage screen, boolean click) throws RobotInterruptedException,
      IOException, AWTException {

    ImageData imageData = getImageData(filename);
    if (imageData == null)
      return new ArrayList<Pixel>(0);
    return scanMany(imageData, screen, click);
  }

  public List<Pixel> scanManyFast(String filename, BufferedImage screen, boolean click)
      throws RobotInterruptedException, IOException, AWTException {

    ImageData imageData = getImageData(filename);
    if (imageData == null)
      return new ArrayList<Pixel>(0);
    return scanMany(imageData, screen, click);
  }

  public List<Pixel> scanMany(ImageData imageData, BufferedImage screen, boolean click)
      throws RobotInterruptedException, IOException, AWTException {
    if (imageData == null)
      return new ArrayList<Pixel>(0);
    Rectangle area = imageData.getDefaultArea();
    if (screen == null)
      screen = new Robot().createScreenCapture(area);
    List<Pixel> matches = _matcher.findMatches(imageData.getImage(), screen, imageData.getColorToBypass());
    if (!matches.isEmpty()) {
      Collections.sort(matches);
      Collections.reverse(matches);

      // filter similar
      if (matches.size() > 1) {
        for (int i = matches.size() - 1; i > 0; --i) {
          for (int j = i - 1; j >= 0; --j) {
            Pixel p1 = matches.get(i);
            Pixel p2 = matches.get(j);
            if (Math.abs(p1.x - p2.x) <= 3 && Math.abs(p1.y - p2.y) <= 3) {
              // too close to each other
              // remove one
              matches.remove(j);
              --i;
            }
          }
        }
      }

      for (Pixel pixel : matches) {
        pixel.x += (area.x + imageData.get_xOff());
        pixel.y += (area.y + imageData.get_yOff());
        if (click)
          _mouse.click(pixel.x, pixel.y);
      }
    }
    return matches;
  }

  public List<Pixel> scanManyFast(ImageData imageData, BufferedImage screen, boolean click)
      throws RobotInterruptedException, IOException, AWTException {
    if (imageData == null)
      return new ArrayList<Pixel>(0);
    Rectangle area = imageData.getDefaultArea();
    if (screen == null)
      screen = new Robot().createScreenCapture(area);
    List<Pixel> matches = _matcher.findMatches(imageData.getImage(), screen, imageData.getColorToBypass());
    if (!matches.isEmpty()) {
      Collections.sort(matches);
      Collections.reverse(matches);

      // filter similar
      if (matches.size() > 1) {
        for (int i = matches.size() - 1; i > 0; --i) {
          for (int j = i - 1; j >= 0; --j) {
            Pixel p1 = matches.get(i);
            Pixel p2 = matches.get(j);
            if (Math.abs(p1.x - p2.x) <= 3 && Math.abs(p1.y - p2.y) <= 3) {
              // too close to each other
              // remove one
              matches.remove(j);
              --i;
            }
          }
        }
      }

      for (Pixel pixel : matches) {
        pixel.x += (area.x + imageData.get_xOff());
        pixel.y += (area.y + imageData.get_yOff());
        if (click)
          _mouse.click(pixel.x, pixel.y);
      }
    }
    return matches;
  }

  public Pixel scanPrecise(ImageData imageData, Rectangle area) throws AWTException, RobotInterruptedException {

    if (area == null) {
      area = imageData.getDefaultArea();
    }
    BufferedImage screen = new Robot().createScreenCapture(area);
    // writeImage2(area, "scoreboardArea.bmp");

    FastBitmap fbID = new FastBitmap(imageData.getImage());
    FastBitmap fbAREA = new FastBitmap(screen);

    // COLOR FILTERING
    ColorFiltering colorFiltering = new ColorFiltering(new IntRange(255, 255), new IntRange(255, 255), new IntRange(
        255, 255));
    colorFiltering.applyInPlace(fbID);
    colorFiltering.applyInPlace(fbAREA);

    Pixel pixel = _matcher.findMatch(fbID.toBufferedImage(), fbAREA.toBufferedImage(), null);
    LOGGER
        .fine("LOOKING FOR " + imageData.getName() + "  screen: " + area + " BYPASS: " + imageData.getColorToBypass());

    long start = System.currentTimeMillis();
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.fine("found : " + imageData.getName() + pixel + " " + (System.currentTimeMillis() - start));
    }
    return pixel;

  }

  public Pixel scanOne(ImageData imageData, Rectangle area, boolean click) throws AWTException,
      RobotInterruptedException {
    if (area == null) {
      area = imageData.getDefaultArea();
    }
    BufferedImage screen = new Robot().createScreenCapture(area);
    Pixel pixel = _matcher.findMatch(imageData.getImage(), screen, imageData.getColorToBypass());
    LOGGER
        .fine("LOOKING FOR " + imageData.getName() + "  screen: " + area + " BYPASS: " + imageData.getColorToBypass());

    long start = System.currentTimeMillis();
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.fine("found : " + imageData.getName() + pixel + " " + (System.currentTimeMillis() - start));
      if (click) {
        _mouse.click(pixel.x, pixel.y);
      }
    }
    return pixel;

  }

  public Pixel scanOne(String filename, Rectangle area, boolean click) throws RobotInterruptedException, IOException,
      AWTException {
    ImageData imageData = getImageData(filename);
    if (imageData == null)
      return null;
    if (area == null)
      area = imageData.getDefaultArea();
    if (area == null)
      area = new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize());

    BufferedImage screen = new Robot().createScreenCapture(area);
    if (_debugMode)
      writeImage(screen, imageData.getName() + "_area.png");
    long start = System.currentTimeMillis();
    Pixel pixel = _matcher.findMatch(imageData.getImage(), screen, imageData.getColorToBypass());
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.fine("found: " + imageData.getName() + pixel + " " + (System.currentTimeMillis() - start));
      if (click) {
        _mouse.click(pixel.x, pixel.y);
        _mouse.delay(100);
      }
    }
    return pixel;
  }

  public Pixel scanOneFast(ImageData imageData, Rectangle area, boolean click) throws AWTException,
      RobotInterruptedException {
    if (area == null) {
      area = imageData.getDefaultArea();
    }
    BufferedImage screen = new Robot().createScreenCapture(area);
    if (_debugMode) {
      writeImage(screen, imageData.getName() + "_area.png");
    }
    long start = System.currentTimeMillis();
    Pixel pixel = _comparator.findImage(imageData.getImage(), screen, imageData.getColorToBypass());
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.fine("found: " + imageData.getName() + pixel + " " + (System.currentTimeMillis() - start));
      if (click) {
        _mouse.click(pixel.x, pixel.y);
      }
    }
    return pixel;

  }

  public Pixel scanOneFast(String filename, Rectangle area, boolean click) throws RobotInterruptedException,
      IOException, AWTException {
    ImageData imageData = getImageData(filename);
    if (imageData == null)
      return null;
    if (area == null)
      area = imageData.getDefaultArea();
    if (area == null)
      area = new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize());

    BufferedImage screen = new Robot().createScreenCapture(area);
    long start = System.currentTimeMillis();
    Pixel pixel = _comparator.findImage(imageData.getImage(), screen, imageData.getColorToBypass());
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.fine("found: " + imageData.getName() + pixel + " " + (System.currentTimeMillis() - start));
      if (click) {
        _mouse.click(pixel.x, pixel.y);
        _mouse.delay(100);
      }
    }
    return pixel;
  }

  public TemplateMatcher getMatcher() {
    return _matcher;
  }

  public void setMatcher(TemplateMatcher matcher) {
    _matcher = matcher;
  }

  public MouseRobot getMouse() {
    return _mouse;
  }

  public void reduceThreshold() {
    _matcher.setSimilarityThreshold(.85d);
  }

  public void restoreThreshold() {
    _matcher.setSimilarityThreshold(.95d);

  }

  public boolean isDebugMode() {
    return _debugMode;
  }

  public void setDebugMode(boolean debugMode) {
    _debugMode = debugMode;
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

  public Pixel getRock() {
    return _rock;
  }

  public void setOptimized(boolean fullyOptimized) {
    _optimized = fullyOptimized;
  }

  public boolean isHome() throws AWTException, RobotInterruptedException {
    return scanOne(_mapButton, null, false) != null;
  }

  public boolean isMap() throws AWTException, RobotInterruptedException {
    return scanOne(_anchorButton, null, false) != null;
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

  public void fixRock() {

  }

  public boolean ensureHome() throws AWTException, IOException, RobotInterruptedException {
    boolean home = false;
    if (!isOptimized()) {
      home = locateGameArea(false);
      // for sure we're home
    } else {
      _mouse.click(getSafePoint());
      _mouse.delay(300);

      if (handlePopups())
        _mouse.delay(500);
      home = isHome();
    }

    if (!home) {
      // try popups first
      if (handlePopups()) {
        _mouse.delay(500);
      } else {
        if (isMap()) {
          scanOne(_anchorButton, null, true);
          _mouse.delay(500);
        }
      }
    }

    if (isHome()) {
      // fix zoom
      zoomOut();
      return checkAndAdjustRock();

    }

    return false;
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

  public Pixel scanPrecise(String filename, Rectangle area) throws AWTException, IOException, RobotInterruptedException {
    return scanPrecise(getImageData(filename), area);
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
  public Pixel lookForDiggyAroundHere(Pixel pp, int cellRange) throws IOException, RobotInterruptedException,
      AWTException {
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
      _kitchen = scanOneFast("camp/egypt/kitchen.bmp", area, false);
      LOGGER.info("kitchen... " + tries);
      _mouse.delay(2000);
    } while(_kitchen == null && tries < 5);
    return _kitchen != null;
  }
  
  public Pixel getKitchen() {
    return new Pixel(_kitchen);
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
        //writeArea(area, "mapsArea.jpg");
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

}
