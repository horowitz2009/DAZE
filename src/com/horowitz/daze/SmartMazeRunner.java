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
    _comparator = _scanner.getComparator();
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
    int xx = position.x - 60;
    if (xx < 0)
      xx = 0;
    int yy = position.y - 60;
    if (yy < 0)
      yy = 0;
    Rectangle area = new Rectangle(xx, yy, 180, 180);
    LOGGER.info("Looking for Diggy in " + area);
    try {
      Pixel p = _scanner.findDiggy(area);
      if (p != null) {
        // first scan for greens
        // if no greens
        // move until find greens

        Position start = new Position(0, 0, Status.WALKABLE);
        _matrix.add(start);

        Position pos = lookForGreen(start, p);
        if (pos != null) {
          
        } else {
          //TODO not greens in the area
        }

        Rectangle rect = new Rectangle(p.x + 60, p.y, 60, 60);
        _mouse.mouseMove(p.x + 30 + 60, p.y + 58);
        _mouse.delay(500);
        _scanner.writeArea(rect, "east2.bmp");

        _mouse.mouseMove(p.x + 30 + 60, p.y + 58 + 60);
        _mouse.delay(500);
        rect = new Rectangle(p.x + 60, p.y + 60, 60, 60);
        _scanner.writeArea(rect, "eastsouth2.bmp");
        p.x += 30;
        p.y += 58;
        _mouse.mouseMove(p);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AWTException e) {
      e.printStackTrace();
    } catch (RobotInterruptedException e) {
    }
    LOGGER.info("DONE...");

  }

  private Position lookForGreen(Position start, Pixel coords) throws RobotInterruptedException, IOException, AWTException {
    boolean found = false;
    ImageData greenID = _scanner.getImageData("green.bmp");
    for (Position position : _searchSequence) {
      Pixel pp = new Pixel(coords.x + position._row * 60, coords.y + position._col * 60);
      Rectangle area = new Rectangle(pp.x, pp.y, 60, 60);

      _mouse.mouseMove(pp.x + 30, pp.y + 58);
      _mouse.delay(300);

      BufferedImage screen = new Robot().createScreenCapture(area);
      FastBitmap fb = new FastBitmap(screen);
      _greenColorFiltering.applyInPlace(fb);

      Pixel p = _comparator.findImage(greenID.getImage(), fb.toBufferedImage(), 
          greenID.getColorToBypass());
      if (p != null) {
        found = true;
        Position newPos = new Position(position._row, position._col, start, Status.GREEN);
        return newPos;
      } else {
        _mouse.delay(150);
      }
    }
    return null;
  }

  private List<Position> _searchSequence;
  private ColorFiltering _greenColorFiltering = new ColorFiltering(new IntRange(70, 140), new IntRange(110, 255),
      new IntRange(0, 65));
}
