package com.horowitz.daze;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.ColorFiltering;

import com.horowitz.commons.ImageComparator;
import com.horowitz.commons.ImageData;
import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.commons.SimilarityImageComparator;

public class SmartMazeRunner {

  private final static Logger LOGGER = Logger.getLogger("MAIN");

  private ScreenScanner _scanner;
  private MouseRobot _mouse;
  private Map<Point, Position> _matrix2;
  private List<Position> _matrix;

  private ImageComparator _comparator;

  public SmartMazeRunner(ScreenScanner scanner) {
    super();
    _scanner = scanner;
    _mouse = _scanner.getMouse();
    //_comparator = _scanner.getComparator();
    _comparator = new SimilarityImageComparator(0.04, 15000);
    _comparator.setErrors(4);
    _matrix = new ArrayList<Position>();
    _searchSequence = new ArrayList<Position>();
    setSearchSequence();
  }

  private void setSearchSequence() {
    _searchSequence.add(new Position(0, -1));
    _searchSequence.add(new Position(0, -2));
    _searchSequence.add(new Position(-1, -1));
    _searchSequence.add(new Position(-1, -2));
    _searchSequence.add(new Position(1, -1));
    _searchSequence.add(new Position(1, -2));
    _searchSequence.add(new Position(-1, 0));
    _searchSequence.add(new Position(-2, 0));
    _searchSequence.add(new Position(1, 0));
    _searchSequence.add(new Position(2, 0));
    _searchSequence.add(new Position(2, -1));
    _searchSequence.add(new Position(2, -2));
    _searchSequence.add(new Position(-2, -1));
    _searchSequence.add(new Position(-2, -2));

    _searchSequence.add(new Position(0, 1));
    _searchSequence.add(new Position(0, 2));
    _searchSequence.add(new Position(-1, 1));
    _searchSequence.add(new Position(-1, 2));
    _searchSequence.add(new Position(1, 1));
    _searchSequence.add(new Position(1, 2));
    _searchSequence.add(new Position(2, 1));
    _searchSequence.add(new Position(2, 2));
    _searchSequence.add(new Position(-2, 1));
    _searchSequence.add(new Position(-2, 2));
  }

  public void clearMatrix() {
    _matrix.clear();
  }

  public void doSomething() {
    Point position = _scanner.getMouse().getPosition();
    int xx = position.x - 90;
    if (xx < 0)
      xx = 0;
    int yy = position.y - 90;
    if (yy < 0)
      yy = 0;
    Rectangle area = new Rectangle(xx, yy, 180, 180);
    LOGGER.info("Looking for Diggy in " + area);
    try {
      Pixel p = _scanner.findDiggy(area);
      if (p != null) {
        Position start = new Position(0, 0, null, Status.WALKABLE);
        start._coords = p;
        _matrix.add(start);
        Position pos = start;
        do {
          pos = findGreenRecursive(pos);
          // TODO check popups, other conditions, etc.
        } while (pos != null);
      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (AWTException e) {
      e.printStackTrace();
    } catch (RobotInterruptedException e) {
    }
    LOGGER.info("DONE...");

  }

  private Position findGreenRecursive(Position pos) throws IOException, AWTException, RobotInterruptedException {
    Position newPos = lookForGreen(pos);
    if (newPos != null) {
      clickTheGreen(newPos);
      return newPos;
    }

    if (canMove(pos, 1, 0)) {
      newPos = new Position(pos._row + 1, pos._col + 0, pos, Status.WALKABLE);
      newPos._coords = new Pixel(pos._coords.x + 1 * 60, pos._coords.y + 0 * 60);
      _matrix.add(newPos);
      Position newPos2 = findGreenRecursive(newPos);
      if (newPos2 != null)
        return newPos2;
      _matrix.remove(newPos);
    }

    if (canMove(pos, 0, 1)) {
      newPos = new Position(pos._row + 0, pos._col + 1, pos, Status.WALKABLE);
      newPos._coords = new Pixel(pos._coords.x + 0 * 60, pos._coords.y + 1 * 60);
      _matrix.add(newPos);
      Position newPos2 = findGreenRecursive(newPos);
      if (newPos2 != null)
        return newPos2;
      _matrix.remove(newPos);
    }

    if (canMove(pos, 0, -1)) {
      newPos = new Position(pos._row + 0, pos._col - 1, pos, Status.WALKABLE);
      newPos._coords = new Pixel(pos._coords.x + 0 * 60, pos._coords.y - 1 * 60);
      _matrix.add(newPos);
      Position newPos2 = findGreenRecursive(newPos);
      if (newPos2 != null)
        return newPos2;
      _matrix.remove(newPos);
    }

    if (canMove(pos, -1, 0)) {
      newPos = new Position(pos._row - 1, pos._col + 0, pos, Status.WALKABLE);
      newPos._coords = new Pixel(pos._coords.x - 1 * 60, pos._coords.y + 0 * 60);
      _matrix.add(newPos);
      Position newPos2 = findGreenRecursive(newPos);
      if (newPos2 != null)
        return newPos2;
      _matrix.remove(newPos);
    }

    return null;
  }

  private Position clickTheGreen(Position newPos) throws RobotInterruptedException, IOException, AWTException {
    // click the green
    int tries = 0;
    do {
      tries++;
      _mouse.click(newPos._coords.x + 30, newPos._coords.y + 30);
      _mouse.delay(500);
    } while (lookForGreenHere(newPos._coords) != null && tries < 5);

    // ////// TODO check is loading -> new matrix
    Pixel p;
    tries = 0;
    do {
      _mouse.delay(1500);
      p = lookForDiggyHere(newPos._coords);
    } while (p == null && tries < 5);
    
    if (p != null) {
      int rowCorrective = 0;
      if (newPos._coords.x - p.x > 0)
        rowCorrective = -1;
      else if (newPos._coords.x - p.x < 0)
        rowCorrective = 1;

      int colCorrective = 0;
      if (newPos._coords.y - p.y > 0)
        colCorrective = -1;
      else if (newPos._coords.y - p.y < 0)
        colCorrective = 1;
      newPos._row += rowCorrective;
      newPos._col += colCorrective;

    } else {
      LOGGER.info("UH OH! I Lost diggy...");
    }
    return newPos;
  }

  private Position process(Position pos) throws IOException, AWTException, RobotInterruptedException {

    Position newPos = lookForGreen(pos);
    if (newPos != null) {
      // click the green
      int tries = 0;
      do {
        tries++;
        _mouse.click(newPos._coords.x + 30, newPos._coords.y + 30);
        _mouse.delay(500);
      } while (lookForGreenHere(newPos._coords) != null && tries < 5);

      // ////// TODO check is loading -> new matrix
      Pixel p = lookForDiggyHere(newPos._coords);
      if (p != null) {

      }
      if (_scanner.isDiggyHere(p)) {
        newPos._state = Status.WALKABLE;
      } else {
        newPos._state = Status.OBSTACLE;
      }

      return newPos;

    } else {
      // TODO no greens in the area
      // move then

      if (canMove(pos, 0, 1)) {
        pos = move(pos, 0, 1);
        pos = process(pos);

      }

    }

    // Rectangle rect = new Rectangle(p.x + 60, p.y, 60, 60);
    // _mouse.mouseMove(p.x + 30 + 60, p.y + 58);
    // _mouse.delay(500);
    // _scanner.writeArea(rect, "east2.bmp");
    //
    // _mouse.mouseMove(p.x + 30 + 60, p.y + 58 + 60);
    // _mouse.delay(500);
    // rect = new Rectangle(p.x + 60, p.y + 60, 60, 60);
    // _scanner.writeArea(rect, "eastsouth2.bmp");
    // p.x += 30;
    // p.y += 58;
    // _mouse.mouseMove(p);

    return null;
  }

  private boolean canMove(Position oldPos, int row, int col) {
    Position newPos = new Position(oldPos._row + row, oldPos._col + col, oldPos, Status.WALKABLE);
    boolean canMove = true;
    for (Position position : _matrix) {
      if (position.same(newPos)) {
        if (position._state == Status.UNKNOWN)
          continue;
        else if (position._state == Status.OBSTACLE) {// position._state ==
                                                      // Status.WALKABLE ||
          canMove = false;
          break;
        }
      }
    }
    return canMove;
  }

  private Position move(Position oldPos, int row, int col) throws RobotInterruptedException, IOException, AWTException {
    Position newPos = new Position(oldPos._row + row, oldPos._col + col, oldPos, Status.WALKABLE);

    Pixel p = oldPos._coords;
    p.x += row * 60;
    p.y += col * 60;
    // try to go there
    _mouse.click(p.x + 30, p.y + 30);
    _mouse.delay(1000);
    if (_scanner.isDiggyHere(p)) {
      newPos._state = Status.WALKABLE;
    } else {
      newPos._state = Status.OBSTACLE;
    }
    _matrix.add(newPos);
    return newPos;
  }

  private Position lookForGreen(Position pos) throws RobotInterruptedException, IOException, AWTException {

    for (Position position : _searchSequence) {
      Pixel pp = new Pixel(pos._coords.x + position._row * 60, pos._coords.y + position._col * 60);
      Pixel p = lookForGreenHere(pp);
      if (p != null) {
        Position newPos = new Position(position._row, position._col, pos, Status.GREEN);
        newPos._coords = pp;
        return newPos;
      } else {
        _mouse.delay(150);
      }
    }
    return null;
  }

  private Pixel lookForDiggyHere(Pixel pp) throws IOException, RobotInterruptedException, AWTException {
    Rectangle area = new Rectangle(pp.x - 60, pp.y - 60, 180, 180);
    return _scanner.findDiggy(area);
  }

  private Pixel lookForGreenHere(Pixel pp) throws IOException, RobotInterruptedException, AWTException {
    ImageData greenID = _scanner.getImageData("green2.bmp");
    Rectangle area = new Rectangle(pp.x-5, pp.y-5, 70, 70);

    _mouse.mouseMove(pp.x + 30, pp.y + 58);
    _mouse.delay(300);

    BufferedImage screen = new Robot().createScreenCapture(area);
    _scanner.writeArea(area, "areaGreen.bmp");
    FastBitmap fb = new FastBitmap(screen);
    _greenColorFiltering.applyInPlace(fb);

    _scanner.writeImage(fb.toBufferedImage(), "greenTest.bmp");
    Pixel p = _comparator.findImage(greenID.getImage(), fb.toBufferedImage(), greenID.getColorToBypass());
    return p;
  }

  private List<Position> _searchSequence;
  private ColorFiltering _greenColorFiltering = new ColorFiltering(new IntRange(70, 140), new IntRange(110, 255),
      new IntRange(0, 65));
}
