package com.horowitz.daze;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import Catalano.Core.IntPoint;
import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.ColorFiltering;
import Catalano.Imaging.Filters.ExtractRGBChannel;
import Catalano.Imaging.Filters.ExtractRGBChannel.Channel;
import Catalano.Imaging.Filters.Threshold;
import Catalano.Imaging.Tools.Blob;

import com.horowitz.commons.ImageComparator;
import com.horowitz.commons.ImageData;
import com.horowitz.commons.MotionDetector;
import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.commons.SimilarityImageComparator;

public class GraphMazeRunner {

  private boolean _popups = false;
  private boolean _gates = false;
  private boolean _slow = false;
  private boolean _stopIt = false;

  private final class Visitor implements Graph.Visitor<Position> {
    private final Position _start;
    private final Graph<Position> _graph;

    private Visitor(Position start, Graph<Position> graph) {
      _start = start;
      _graph = graph;
    }

    private BufferedImage captureBlock(Pixel p) throws AWTException {
      return new Robot().createScreenCapture(new Rectangle(p.x, p.y, 60, 60));
    }

    @Override
    public boolean visit(Position vertex) throws Exception {
      if (!_stopIt) {
        LOGGER.info("VISITING [" + vertex._row + ", " + vertex._col + "] " + vertex._state);
        _support.firePropertyChange("CLICK", null, vertex);
        // LOGGER.info("STACKTRACE: " +
        // Thread.currentThread().getStackTrace().length);
        try {
          ensureArea(vertex, 0, 0);
          if (vertex._state == State.GREEN) {
            // //////////////////////
            Pixel coords = new Pixel(_start._coords.x + vertex._row * 60, _start._coords.y + vertex._col * 60);

            // is diggy around
            // YES, click then
            // NO, we have to wait until he comes
            Pixel p;
            int tries = 0;
            do {
              p = _scanner.lookForDiggyAroundHere(coords, 1);
              if (p == null)
                _mouse.delay(200);
            } while (!_stopIt && p == null && ++tries < 7);

            // THE CLICK
            _mouse.click(coords.x + 30, coords.y + 30);
            // _mouse.click(_start._coords.x + vertex._row * 60 + 30,
            // _start._coords.y + vertex._col * 60 + 30);

            if (_gates) {
              LOGGER.info("CHECK LOADING2...");
              if (checkIsLoading()) {
                recoverFromGate(vertex);
                return false;
              }
            }

            _mouse.delay(750);
            // ////////////////////

            // if (p == null) {
            // LOGGER.info("Diggy not near. Will wait a bit more...");
            // _mouse.delay(20000);
            // } else
            // _mouse.delay(500);

            if (checkIsReady()) {
              _stopIt = true;
              return false;
            }

            if (checkPopup()) {
              vertex._state = State.OBSTACLE;
              _support.firePropertyChange("STATE_CHANGED", State.GREEN, vertex);
              return false;
            } else if (!_stopIt && checkNoEnergy()) {
              LOGGER.info("OUT OF ENERGY...");
              LOGGER.info("Waiting 20 seconds");
              do {
                _mouse.delay(20000);
                _mouse.click(vertex._coords.x + 30, vertex._coords.y + 30);
                _mouse.delay(1000);
              } while (!_stopIt & checkNoEnergy());// both conditions must be
                                                   // checked!!!
              // TODO when OCR is introduced, check if the tile is too
              // expensive,
              // then leave it and move on
            } else {
              _mouse.delay(1000);
            }
            vertex._state = State.CHECKED;
            _support.firePropertyChange("GREEN_CLICKED", State.GREEN, vertex);

            // LOGGER.info("Sleep " + _pauseTime + " seconds");
            _mouse.delay(_pauseTime * 1000);
          }
          // END OF GREEN

          if (_popups && checkPopups()) {
            _mouse.delay(250);
          }

          if (vertex._state == State.START) {
            vertex._state = State.VISITED;
            _support.firePropertyChange("TOTAL_MATRIX", null, _explored);
            _support.firePropertyChange("DIGGY", State.START, vertex);
            // _support.firePropertyChange("STATE_CHANGED", State.START,
            // vertex);
            checkNeighbor(_graph, vertex, 0, -1);// north was 3rd
            checkNeighbor(_graph, vertex, 1, 0);
            checkNeighbor(_graph, vertex, 0, 1);
            checkNeighbor(_graph, vertex, -1, 0);
          } else {

            // _mouse.click(vertex._coords.x + 30, vertex._coords.y + 30);
            // _mouse.delay(6000);
            BufferedImage preClick = captureBlock(vertex._coords);
            _mouse.click(vertex._coords.x + 30, vertex._coords.y + 30);
            if (isWalkable(vertex, preClick) || (isSlow() && isWalkable(vertex, preClick))) {
              // wait until diggy arrives
              int tries = 0;
              boolean diggyHere = false;
              do {
                _mouse.delay(250);
                if (tries++ > 5 && tries % 4 == 0)
                  LOGGER.info("waiting diggy to arrive " + tries);
                diggyHere = _scanner.isDiggyExactlyHere(vertex._coords);
              } while (!diggyHere && tries < 30);

              // if (diggyHere)
              vertex._state = State.VISITED;
              // else
              // vertex._state = State.CHECKED;

              // TODO Do something about errors

              // if (_scanner.isDiggyExactlyHere(vertex._coords)) {
              // vertex._state = State.VISITED;
              // } else {
              // vertex._state = State.CHECKEDTWICE;
              // }
              // tadaaaa

              _support.firePropertyChange("STATE_CHANGED", null, vertex);
              _support.firePropertyChange("DIGGY", null, vertex);

              checkNeighbor(_graph, vertex, 0, -1);// north was 3rd
              checkNeighbor(_graph, vertex, 1, 0);
              checkNeighbor(_graph, vertex, 0, 1);
              checkNeighbor(_graph, vertex, -1, 0);

            } else {
              if (_gates) {
                LOGGER.info("CHECK LOADING...");
                if (checkIsLoading()) {
                  recoverFromGate(vertex);
                  return false;
                } else {
                  if (_slow)
                    _mouse.delay(1000);
                }
              }
              vertex._state = State.OBSTACLE;
              _support.firePropertyChange("STATE_CHANGED", null, vertex);
              return false;

            }
          }
        } catch (IOException | AWTException e1) {
          e1.printStackTrace();
          return false;
        }
        return true;
      } else
        return false;
    }

    private void recoverFromGate(Position vertex) throws RobotInterruptedException, IOException, AWTException {
      LOGGER.info("LOADING...");
      vertex._state = State.OBSTACLE;
      _support.firePropertyChange("STATE_CHANGED", null, vertex);

      // restore the
      // try first diggy
      _mouse.mouseMove(_scanner.getParkingPoint());
      Pixel p = findDiggyBig(5);
      if (p != null) {
        // good we can go back now
        _mouse.click(p.x + 30, p.y + 30);
        _mouse.delay(5000);
        _mouse.mouseMove(_scanner.getParkingPoint());
        p = findDiggyBig(15);
        if (p != null) {
          // excellent, we are back in the map
          vertex._coords = p;
          _start._coords = new Pixel(p.x - vertex._row * 60, p.y - vertex._col * 60);
        }
      }
    }

    private Pixel findDiggyBig(int tries) throws RobotInterruptedException, IOException, AWTException {
      Rectangle diggyArea = _scanner.getScanArea();
      int t = 0;
      Pixel p;
      do {
        _mouse.delay(100);
        p = _scanner.findDiggy(diggyArea);
      } while (p == null && t < tries);
      return p;
    }

    private boolean checkIsLoading() throws IOException, AWTException, RobotInterruptedException {
      _mouse.delay(133);
      int tries = 0;
      Pixel p;
      BufferedImage logoFiltered = _scanner.getImageData("logo.bmp").getImage();

      logoFiltered = filterLogo(logoFiltered, 1);
      // List<BufferedImage> images = new ArrayList<>();
      do {
        Rectangle area = _scanner._logoArea;
        BufferedImage areaFiltered = new Robot().createScreenCapture(area);
        areaFiltered = filterLogo(areaFiltered, 2 + tries);

        p = _scanner._matcher.findMatch(logoFiltered, areaFiltered, null);
        // p = _comparator.findImage(logoFiltered, areaFiltered, null);

        tries++;
        if (p == null)
          _mouse.delay(70);
      } while (p == null && tries < 5);

      if (p != null)
        LOGGER.info("detected loading in attempt " + tries);
      return p != null;
    }

    private boolean isWalkable(Position vertex, BufferedImage preClick) throws AWTException, RobotInterruptedException {
      int number = 20;
      Pixel p = vertex._coords;
      List<BufferedImage> images = new ArrayList<>();
      images.add(preClick);
      long start = System.currentTimeMillis();
      _mouse.delay(30);
      for (int i = 0; i < number; i++) {
        // _mouse.delay(25);
        if (i == 4) {
          _mouse.click(p.x + 30, p.y + 30);
          // LOGGER.info("click isWalkable");
        }
        images.add(captureBlock(p));
      }
      long end = System.currentTimeMillis();

      for (int i = 2; i < images.size(); i++) {
        BufferedImage image = images.get(i);
        List<Blob> blobs = new MotionDetector().detect(images.get(i - 2), image, -1);
        for (Blob blob : blobs) {
          FastBitmap fb2 = new FastBitmap(image);

          int cnt = 0;
          for (IntPoint point : blob.getPoints()) {
            if (fb2.getRed(point) == 151 && fb2.getGreen(point) == 235 && fb2.getBlue(point) == 81)
              cnt++;
            if (cnt > 3) {
              // BINGOOO
              // fb2.saveAsPNG("BLOB_" + blob.getCenter().y + "_" +
              // blob.getCenter().x + "_" + System.currentTimeMillis()
              // + ".png");
              LOGGER.info("isWalkable: true " + (end - start));
              return true;
            }
          }

        }

      }
      LOGGER.info("isWalkable: FALSE " + (end - start));
      return false;
    }

    private boolean tryPixel(BufferedImage filtered, int x) {
      // at least 2 to be true
      int ok = 0;
      boolean b1 = new Color(filtered.getRGB(x, x)).equals(Color.WHITE)
          || new Color(filtered.getRGB(x, x - 1)).equals(Color.WHITE)
          || new Color(filtered.getRGB(x, x + 1)).equals(Color.WHITE);
      if (b1)
        ok++;
      boolean b2 = new Color(filtered.getRGB(x, 60 - x)).equals(Color.WHITE)
          || new Color(filtered.getRGB(x, 59 - x)).equals(Color.WHITE)
          || new Color(filtered.getRGB(x, 61 - x)).equals(Color.WHITE);
      if (b2)
        ok++;
      boolean b3 = new Color(filtered.getRGB(60 - x, 60 - x)).equals(Color.WHITE)
          || new Color(filtered.getRGB(60 - x, 59 - x)).equals(Color.WHITE)
          || new Color(filtered.getRGB(60 - x, 61 - x)).equals(Color.WHITE);
      if (b3)
        ok++;
      boolean b4 = new Color(filtered.getRGB(60 - x, x)).equals(Color.WHITE)
          || new Color(filtered.getRGB(60 - x, x - 1)).equals(Color.WHITE)
          || new Color(filtered.getRGB(60 - x, x + 1)).equals(Color.WHITE);
      if (b4)
        ok++;
      // LOGGER.info(x + " " + b1 + " " + b2 + " " + b3 + " " + b4);

      return ok >= 3;
    }

    @Override
    public boolean canBeVisited(Position neighbor) {
      return neighbor._state != State.VISITED && neighbor._state != State.OBSTACLE;
    }

    private void ensureArea(Position pos, int rowOffset, int colOffset) throws RobotInterruptedException, IOException,
        AWTException {
      pos._coords = new Pixel(_start._coords.x + pos._row * 60, _start._coords.y + pos._col * 60);
      int xx = _start._coords.x + (pos._row + rowOffset) * 60;
      int yy = _start._coords.y + (pos._col + colOffset) * 60;
      // LOGGER.info("" + pos);
      Rectangle area = _scanner.getScanArea();
      int eastBorder = area.x + area.width;
      int westBorder = area.x;
      int southBorder = area.y + area.height;
      int northBorder = area.y;

      int xCorrection = 0;
      int yCorrection = 0;
      if (rowOffset == 0 && colOffset == 0) {
        // it's a vertex, not neighbor

        // CHECKED FOR SMALL DISTANCES
        // check east
        if (xx + 60 > eastBorder) {
          xCorrection = eastBorder - (xx + 60);// negative
        } else {
          // check west
          if (xx < westBorder) {
            xCorrection = westBorder - xx;
          }
        }
        // if (xCorrection > 0)
        // xCorrection = Math.min(60, xCorrection);

        // CHECKED FOR SMALL DISTANCES
        // check south
        if (yy + 60 > southBorder) {
          yCorrection = southBorder - (yy + 60);// negative
        } else {
          // check north
          if (yy < northBorder) {
            yCorrection = northBorder - yy;
          }
        }
        // if (yCorrection > 0)
        // yCorrection = Math.min(60, yCorrection);
        // ////////////// ALL /////////////////
      }

      // CHECKED
      if (xCorrection != 0 || yCorrection != 0) {
        if (xCorrection != 0) {
          // HORIZONTAL
          double width = _scanner.getScanArea().getWidth() - 20;
          int n = (int) ((xCorrection) / width);
          double theRest = (xCorrection / width) - n;
          theRest *= width;
          Pixel start;
          Pixel end;
          if (xCorrection < 0) {
            start = new Pixel(eastBorder - 10, _scanner.getTopLeft().y + _scanner.getGameHeight() / 2);
            end = new Pixel(start.x - (int) width, start.y);
          } else {
            start = new Pixel(westBorder + 10, _scanner.getTopLeft().y + _scanner.getGameHeight() / 2);
            end = new Pixel(start.x + (int) width, start.y);
          }
          for (int i = 0; i < n; ++i) {
            _mouse.drag3(start.x, start.y, end.x, end.y);
            _mouse.delay(100);
          }
          if (n != 0) {
            if ((int) theRest != 0) {
              end = new Pixel(start.x + (int) theRest, start.y);
              _mouse.drag3(start.x, start.y, end.x, end.y);
            }
          } else {
            end = new Pixel(start.x + xCorrection, start.y);
            _mouse.drag3(start.x, start.y, end.x, end.y);
          }
        }

        // CHECKED
        if (yCorrection != 0) {
          // VERTICAL
          double height = _scanner.getScanArea().getHeight() - 20;
          int n = (int) ((yCorrection) / height);
          double theRest = (yCorrection / height) - n;
          theRest *= height;
          Pixel start;
          Pixel end;
          if (yCorrection < 0) {
            // start from south -> go north
            start = new Pixel(_scanner.getTopLeft().x + _scanner.getGameWidth() / 2, southBorder - 10);
            end = new Pixel(start.x, start.y - (int) height);
          } else {
            start = new Pixel(_scanner.getTopLeft().x + _scanner.getGameWidth() / 2, northBorder + 10);
            end = new Pixel(start.x, start.y + (int) height);
          }
          for (int i = 0; i < n; ++i) {
            _mouse.drag3(start.x, start.y, end.x, end.y);
            _mouse.delay(100);
          }
          if (n != 0) {
            if ((int) theRest != 0) {
              end = new Pixel(start.x, start.y + (int) theRest);
              _mouse.drag3(start.x, start.y, end.x, end.y);
            }
          } else {
            end = new Pixel(start.x, start.y + yCorrection);
            _mouse.drag3(start.x, start.y, end.x, end.y);
          }
        }
        _start._coords.x += xCorrection;
        _start._coords.y += yCorrection;
        pos._coords = new Pixel(_start._coords.x + pos._row * 60, _start._coords.y + pos._col * 60);

        int totalXCorrection = xCorrection;
        int totalYCorrection = yCorrection;

        LOGGER.info("===============================");
        LOGGER.info("X correction: " + totalXCorrection);
        LOGGER.info("Y correction: " + totalYCorrection);
        LOGGER.info("===============================");
      }

    }

    private int getInt(double n) {
      int i = (int) n;
      return i;
    }

    private boolean isAlreadyChecked(Graph<Position> graph, Position neighbor) {
      for (Position position : graph.getExplored()) {
        if (position.same(neighbor))
          return position._state == State.CHECKED || position._state == State.VISITED
              || position._state == State.OBSTACLE;
      }
      return false;
    }

    @Override
    public List<Position> prioritize(List<Position> neighbors) {

      List<Position> res = new ArrayList<>();
      List<Position> greens = new ArrayList<>();
      List<Position> rest = new ArrayList<>();
      for (Position position : neighbors) {
        if (position._state == State.GREEN)
          greens.add(position);
        else
          rest.add(position);
      }

      res.addAll(greens);
      res.addAll(rest);
      return res;
    }

    private void checkNeighbor(final Graph<Position> graph, Position vertex, int rowOffset, int colOffset)
        throws RobotInterruptedException, IOException, AWTException {
      Position neighborPos = new Position(vertex._row + rowOffset, vertex._col + colOffset);
      if (!isAlreadyChecked(graph, neighborPos)) {
        neighborPos._coords = new Pixel(_start._coords.x + neighborPos._row * 60, _start._coords.y + neighborPos._col
            * 60);

        if (graph.canBeVisited(neighborPos, this)) {

          ensureArea(neighborPos, 0, 0);
          neighborPos._state = State.CHECKED;

          if (lookForGreenHere(neighborPos._coords)) {
            neighborPos._state = State.GREEN;
            LOGGER.info("GREEN " + neighborPos);
            _support.firePropertyChange("STATE_CHANGED", null, neighborPos);
            // if (isGate(p)) { // TODO not reliable! to be improved
            // LOGGER.info("It is gate!!!");
            // neighborPos._state = State.OBSTACLE;// FOR NOW GATE IS OBSTACLE
            // _support.firePropertyChange("STATE_CHANGED", null, neighborPos);
            // }
          }

          if (isGate(neighborPos._coords)) { // TODO not reliable! to be
                                             // improved
            LOGGER.info("It is gate!!!");
            neighborPos._state = State.OBSTACLE;// FOR NOW GATE IS OBSTACLE
            _support.firePropertyChange("STATE_CHANGED", null, neighborPos);
          }
          boolean contains = false;
          for (Position npos : graph.getNeighbors(vertex)) {
            if (npos.same(neighborPos)) {
              contains = true;
              break;
            }
          }
          if (!contains) {
            graph.addEdge(vertex, neighborPos);
            // graph.addEdge(newPos, vertex);
          } else {
            LOGGER.info("FUNNY: " + vertex + " contains " + neighborPos);
          }
          graph.addExplored(neighborPos);
          _support.firePropertyChange("POS_ADDED", null, neighborPos);
        }
      }
    }
  }

  private final static Logger LOGGER = Logger.getLogger("MAIN");

  private PropertyChangeSupport _support;
  private ScreenScanner _scanner;
  private MouseRobot _mouse;
  private ImageComparator _comparator;

  private List<Position> _searchSequence;
  private Set<Position> _explored;

  private int _pauseTime = 5;

  public GraphMazeRunner(ScreenScanner scanner) {
    super();

    _support = new PropertyChangeSupport(this);
    _scanner = scanner;
    _mouse = _scanner.getMouse();
    _mouse.addPropertyChangeListener("DELAY", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        LOGGER.info("" + ((Integer) evt.getNewValue()) / 1000 + " seconds");
      }
    });

    // _comparator = _scanner.getComparator();
    _comparator = new SimilarityImageComparator(0.04, 15000);
    _comparator.setErrors(4);

    _explored = new HashSet<Position>();

    _searchSequence = new ArrayList<Position>();
    setSearchSequence2();

  }

  private void setSearchSequence2() {
    _searchSequence.add(new Position(0, -1));
    _searchSequence.add(new Position(1, -1));
    _searchSequence.add(new Position(1, 0));
    _searchSequence.add(new Position(1, 1));
    _searchSequence.add(new Position(0, 1));
    _searchSequence.add(new Position(-1, 1));
    _searchSequence.add(new Position(-1, 0));
    _searchSequence.add(new Position(-1, -1));

    /*
     * _searchSequence.add(new Position(-1, -2)); _searchSequence.add(new Position(0, -2)); _searchSequence.add(new Position(1, -2)); _searchSequence.add(new Position(2, -2)); _searchSequence.add(new
     * Position(2, -1)); _searchSequence.add(new Position(2, 0)); _searchSequence.add(new Position(2, 1)); _searchSequence.add(new Position(2, 2)); _searchSequence.add(new Position(1, 2));
     * _searchSequence.add(new Position(0, 2)); _searchSequence.add(new Position(-1, 2)); _searchSequence.add(new Position(-2, 2)); _searchSequence.add(new Position(-2, 1)); _searchSequence.add(new
     * Position(-2, 0)); _searchSequence.add(new Position(-2, -2));
     */
  }

  public void clearMatrix() {
    _explored.clear();
    _support.firePropertyChange("CLEARED", null, true);
  }

  public void clearMatrixPartially() {

    Iterator<Position> iterator = _explored.iterator();
    while (iterator.hasNext()) {
      Position position = (Position) iterator.next();
      if (position._state != State.OBSTACLE) {
        iterator.remove();
        _support.firePropertyChange("POS_REMOVED", null, position);
      }
    }
  }

  public void testPosition() {
    Point position = _scanner.getMouse().getPosition();
    int xx = position.x - 120;
    if (xx < 0)
      xx = 0;
    int yy = position.y - 120;
    if (yy < 0)
      yy = 0;
    Rectangle area = new Rectangle(xx, yy, 240, 240);
    // LOGGER.info("Looking for Diggy in " + area);
    try {
      Pixel p = _scanner.findDiggy(area);
      if (p != null) {

        p.x -= 60 * 1;
        // p.y += 30;

        _mouse.click(p.x + 30, p.y + 30);
        Robot robot = new Robot();
        Rectangle blockArea = new Rectangle(p.x, p.y, 60, 60);
        List<BufferedImage> images = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 60; i++) {
          // if (i == 5)
          // _mouse.click(p.x + 30, p.y + 30);
          // _mouse.delay(99);
          images.add(robot.createScreenCapture(blockArea));
        }
        long end = System.currentTimeMillis();
        // LOGGER.info("time: " + (start - end));
        int i = 0;
        // for (BufferedImage image : images) {
        // new FastBitmap(image).saveAsBMP("AA" + ++i + ".bmp");
        // }

        for (i = 1; i < images.size(); i++) {
          BufferedImage imagePrev = images.get(i - 1);
          BufferedImage image = images.get(i);
          List<Blob> blobs = new MotionDetector().detect(imagePrev, image, 4);
          for (Blob blob : blobs) {
            FastBitmap fb2 = new FastBitmap(image);

            // fb2.saveAsPNG("BLOB_" + blob.getCenter().y + "_" +
            // blob.getCenter().x + "_" + System.currentTimeMillis()
            // + ".png");

            // ArrayList<IntPoint> lst = blob.getPoints();
            // lst = PointsCloud.GetBoundingRectangle(lst);
            //
            //
            // int height = Math.abs(lst.get(0).x - lst.get(1).x);
            // int width = Math.abs(lst.get(0).y - lst.get(1).y);

            // IntRectangle bb = blob.getBoundingBox();
            // Crop crop = new Crop(bb.x, bb.y, bb.width, bb.height);
            // Crop crop = new Crop(lst.get(0).x - 2, lst.get(0).y - 2, width +
            // 5, height + 4);
            // crop.ApplyInPlace(fb2);

            int cnt = 0;
            for (IntPoint point : blob.getPoints()) {
              if (fb2.getRed(point) == 151 && fb2.getGreen(point) == 235 && fb2.getBlue(point) == 81)
                cnt++;
              if (cnt > 3) {
                // BINGOOO
                fb2.saveAsPNG("BLOB_" + blob.getCenter().y + "_" + blob.getCenter().x + "_"
                    + System.currentTimeMillis() + ".png");
                break;
              }
            }

            // try {
            // BufferedImage subimage =
            // image.getSubimage(blob.getBoundingBox().x,
            // blob.getBoundingBox().y,
            // blob.getBoundingBox().width, blob.getBoundingBox().height);
            //
            // _scanner.writeImage(subimage,
            // "BLOB_" + blob.getCenter().y + "_" + blob.getCenter().x + "_" +
            // System.currentTimeMillis() + ".png");
            // } catch (Throwable t) {
            // t.printStackTrace();
            // System.out.println(blob);
            // }
          }

          if (blobs.size() > 0) {
            // we have motion

          }
        }

      }
    } catch (RobotInterruptedException e) {
      LOGGER.info("interrupted...");
    } catch (AWTException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void traverse(Pixel p) throws Exception {
    clearMatrix();
    setStopIt(false);

    final Position start = new Position(0, 0, null, State.START);
    start._coords = p;
    final Graph<Position> graph = new Graph<>(_explored);

    graph.preOrderTraversal(start, new Visitor(start, graph));
    LOGGER.info("DONE!");

  }

  public void doSomething(boolean clearMatrix, int seconds) {
    _pauseTime = seconds;
    if (clearMatrix) {
      clearMatrix();
    } else {
      clearMatrixPartially();
    }
    Point position = _scanner.getMouse().getPosition();
    int xx = position.x - 120;
    if (xx < 0)
      xx = 0;
    int yy = position.y - 120;
    if (yy < 0)
      yy = 0;
    Rectangle area = new Rectangle(xx, yy, 240, 240);
    LOGGER.info("Looking for Diggy in " + area);
    try {
      Pixel p = _scanner.findDiggy(area);
      if (p != null) {
        final Position start = new Position(0, 0, null, State.START);
        start._coords = p;
        final Graph<Position> graph = new Graph<>(_explored);

        do {
          graph.preOrderTraversal(start, new Visitor(start, graph));
          LOGGER.info("DONE! STARTING AGAIN in 5 seconds");
          _mouse.delay(5000);
          // if (clearMatrix) {
          // clearMatrix();
          // } else {
          // clearMatrixPartially();
          // }
        } while (true);
      }
    } catch (Exception e) {
      LOGGER.info("Critical error occured!");
      e.printStackTrace();
    }
    LOGGER.info("DONE...");

  }

  private boolean checkPopups() throws IOException, AWTException, RobotInterruptedException {
    // LOGGER.info("all popups...");
    long start = System.currentTimeMillis();
    Rectangle area = _scanner.generateWindowedArea(204, 648);// was 486
    area.y = _scanner.getTopLeft().y + _scanner.getGameHeight() / 2;
    area.width = _scanner.getGameHeight() / 2;
    Pixel p = _scanner.scanOneFast("share.bmp", area, false);
    if (p != null) {
      _mouse.click(p.x + 34, p.y + 11);
      _mouse.delay(200);
    }

    p = _scanner.scanOneFast("X.bmp", _scanner._popupAreaX, false);
    if (p != null) {
      _mouse.click(p.x + 16, p.y + 16);
      LOGGER.info("click checkPopups");

      _mouse.delay(200);
    } else {
      p = _scanner.scanOneFast("claim.bmp", null, true);
    }
    // LOGGER.info("time: " + (System.currentTimeMillis() - start));
    return p != null;
  }

  private boolean checkIsReady() throws IOException, AWTException, RobotInterruptedException {
    // LOGGER.info("sign popup...");
    long start = System.currentTimeMillis();
    Rectangle area = _scanner.generateWindowedArea(137, 325);
    Pixel p = _scanner.scanOneFast("cleared.bmp", area, false);
    if (p != null) {
      _mouse.click(p.x + 42, p.y + 166);
      LOGGER.info("click congrats popup");

      _mouse.delay(200);
    } else {
      // repeatable scenario
      area = _scanner.generateWindowedArea(479, 648);
      area.y = _scanner.getTopLeft().y + 187;
      area.height = 62;
      area.x += 100;
      area.width = 150;
      p = _scanner.scanOneFast("clearedRep.bmp", area, false);
      if (p != null) {
        _mouse.click(p.x + 47, p.y + 248);//ok button could be elsewhere!!!
      }
    }
    LOGGER.info("time: " + (System.currentTimeMillis() - start));
    return p != null;
  }

  private boolean checkPopup() throws IOException, AWTException, RobotInterruptedException {
    // LOGGER.info("sign popup...");
    long start = System.currentTimeMillis();
    Rectangle area = _scanner.generateWindowedArea(520, 290);
    area.x = area.x + area.width - 70;
    area.y -= 18;
    area.width = 70;
    area.height = 80;

    Pixel p = _scanner.scanOneFast("X.bmp", area, false);
    if (p != null) {
      _mouse.click(p.x + 16, p.y + 16);
      LOGGER.info("click checkPopup");

      _mouse.delay(200);
    }
    // LOGGER.info("time: " + (System.currentTimeMillis() - start));
    return p != null;
  }

  private boolean checkNoEnergy() throws IOException, AWTException, RobotInterruptedException {
    // LOGGER.info("energy popup...");
    long start = System.currentTimeMillis();
    Rectangle area = _scanner.generateWindowedArea(458 + 10, 464 + 10);
    area.x = area.x + 90;
    area.y = area.y + 56;
    area.width = 150;
    area.height = 90;

    Pixel p = _scanner.scanOneFast("noEnergyPopup.bmp", area, false);
    if (p != null) {
      _mouse.click(p.x + 333, p.y - 35);
      LOGGER.info("click checkNoEnergy");
      _mouse.delay(200);
    }
    LOGGER.info("time: " + (System.currentTimeMillis() - start));
    return p != null;
  }

  private boolean isGate(Pixel pp) throws RobotInterruptedException, AWTException, IOException {
    //
    // _mouse.mouseMove(pp.x + 30, pp.y + 30);
    // _mouse.delay(100);
    Rectangle area = new Rectangle(pp.x + 14, pp.y + 10, 17 + 10, 12 + 12);
    BufferedImage image2 = new Robot().createScreenCapture(area);
    image2 = filterGate(image2);
    ImageData id = _scanner.getImageData("gate.bmp");
    Pixel ppp = _comparator.findImage(id.getImage(), image2, id.getColorToBypass());

    return (ppp != null);
  }

  private Position lookForGreen(Position pos) throws RobotInterruptedException, IOException, AWTException {
    LOGGER.info("looking for greens...");
    for (Position position : _searchSequence) {
      Pixel pp = new Pixel(pos._coords.x + position._row * 60, pos._coords.y + position._col * 60);
      if (lookForGreenHere(pp)) {
        Position newPos = new Position(position._row, position._col, pos, State.GREEN);
        newPos._coords = pp;
        LOGGER.info("Found one..." + newPos);
        return newPos;
      } else {
        _mouse.delay(150);
      }
    }
    return null;
  }

  private Position clickTheGreen(Position newPos) throws RobotInterruptedException, IOException, AWTException {
    // click the green
    int tries = 0;
    // do {
    tries++;
    _mouse.click(newPos._coords.x + 30, newPos._coords.y + 30);
    LOGGER.info("click clickTheGreen");
    _mouse.delay(500);
    // } while (lookForGreenHere2(newPos._coords) != null && tries < 5);

    // ////// TODO check is loading -> new matrix
    Pixel p;
    tries = 0;
    do {
      _mouse.delay(1500);
      p = lookForDiggyAroundHere(newPos._coords, tries % 2 + 1);
      tries++;
    } while (p == null && tries < 7);

    if (p != null) {
      LOGGER.info("Found diggy in attempt " + tries);

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

      if (rowCorrective != 0 || colCorrective != 0) {
        // need to move it

      }
      newPos._row += rowCorrective;
      newPos._col += colCorrective;
      newPos._coords = p;

    } else {
      LOGGER.info("UH OH! I Lost diggy...");
    }
    return newPos;
  }

  private Pixel lookForDiggyAroundHere(Pixel pp, int cellRange) throws IOException, RobotInterruptedException,
      AWTException {
    Rectangle area = new Rectangle(pp.x - cellRange * 60, pp.y - cellRange * 60, cellRange * 60 + 120 + 60,
        cellRange * 60 + 120 + 60);
    Pixel res = _scanner.findDiggy(area);
    LOGGER.info("Looking for diggy in " + pp + " " + res);
    return res;
  }

  private BufferedImage filterGreen(BufferedImage image) {
    FastBitmap fb1 = new FastBitmap(image);
    ExtractRGBChannel extractChannel = new ExtractRGBChannel(Channel.G);
    fb1 = extractChannel.Extract(fb1);
    // fb1.saveAsBMP("temp.bmp");
    Threshold thr = new Threshold(170);
    thr.applyInPlace(fb1);
    // colorFiltering.applyInPlace(fb1);
    // fb1.saveAsBMP("temp2.bmp");
    return fb1.toBufferedImage();
  }

  private BufferedImage filterLogo(BufferedImage image, int i) {
    FastBitmap fb1 = new FastBitmap(image);
    fb1.toGrayscale();
    Threshold thr = new Threshold(170);
    thr.applyInPlace(fb1);
    // fb1.saveAsPNG("logo"+i+".png");
    return fb1.toBufferedImage();
  }

  private BufferedImage filterGate(BufferedImage image) {
    FastBitmap fb1 = new FastBitmap(image);

    // ColorFiltering colorFiltering = new ColorFiltering(new IntRange(45, 80),
    // new IntRange(95, 255),
    // new IntRange(0, 120));
    ColorFiltering colorFiltering = new ColorFiltering(new IntRange(40, 90), new IntRange(90, 255),
        new IntRange(0, 125));
    colorFiltering.applyInPlace(fb1);

    if (fb1.isRGB())
      fb1.toGrayscale();
    Threshold thr = new Threshold(70);// was 80
    thr.applyInPlace(fb1);
    return fb1.toBufferedImage();
  }

  public static void main(String[] args) {
    try {
      BufferedImage image = ImageIO.read(new File("temp/mud.bmp"));
      FastBitmap fb1 = new FastBitmap(image);

      ColorFiltering colorFiltering = new ColorFiltering(new IntRange(54, 155), new IntRange(100, 240), new IntRange(5,
          85));
      colorFiltering.applyInPlace(fb1);

      if (fb1.isRGB())
        fb1.toGrayscale();
      fb1.saveAsBMP("mudGR.bmp");
      Threshold thr = new Threshold(70);
      thr.applyInPlace(fb1);
      fb1.saveAsBMP("mudTHR.bmp");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private BufferedImage filterArrows(BufferedImage image) {
    FastBitmap fb1 = new FastBitmap(image);

    ColorFiltering colorFiltering = new ColorFiltering(new IntRange(54, 155), new IntRange(100, 240), new IntRange(5,
        85));
    colorFiltering.applyInPlace(fb1);

    if (fb1.isRGB())
      fb1.toGrayscale();
    Threshold thr = new Threshold(70);
    thr.applyInPlace(fb1);
    return fb1.toBufferedImage();
  }

  private boolean lookForGreenHere(Pixel pp) throws AWTException, RobotInterruptedException, IOException {
    // 10px more from all sides
    Rectangle area = new Rectangle(pp.x - 10, pp.y - 10, 60 + 20, 60 + 20);
    BufferedImage image1 = new Robot().createScreenCapture(area);
    _mouse.mouseMove(pp.x + 30, pp.y + 30);
    _mouse.delay(100 + (isSlow() ? 600 : 0));
    BufferedImage image2 = new Robot().createScreenCapture(area);
    List<Blob> blobs = new MotionDetector().detect(image1, image2, 5 * 50);

    // for (Blob blob : blobs) {
    // FastBitmap fb2 = new FastBitmap(image2);
    //
    // Crop crop = new Crop(blob.getBoundingBox().x, blob.getBoundingBox().y,
    // blob.getBoundingBox().width,
    // blob.getBoundingBox().height);
    // crop.ApplyInPlace(fb2);
    // fb2.saveAsPNG("BLOB_" + blob.getCenter().y + "_" + blob.getCenter().x +
    // "_" + System.currentTimeMillis() + ".png");
    // }
    if (blobs.size() > 0) {
      for (Blob blob : blobs) {
        if (blob.getHeight() > 35 && blob.getWidth() > 35)
          return true;
      }

      // // we have movement, but let's see is it green
      // image2 = filterGreen(image2.getSubimage(0, 0, 25, 25));
      // ImageData id = _scanner.getImageData("greenTL.bmp");
      // Pixel ppp = _comparator.findImage(id.getImage(), image2,
      // id.getColorToBypass());
      // if (ppp != null)
      // return pp;
    }
    return false;
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

  public void addPropertyChangeListener(PropertyChangeListener arg0) {
    _support.addPropertyChangeListener(arg0);
  }

  public void addPropertyChangeListener(String arg0, PropertyChangeListener arg1) {
    _support.addPropertyChangeListener(arg0, arg1);
  }

  public void removePropertyChangeListener(PropertyChangeListener arg0) {
    _support.removePropertyChangeListener(arg0);
  }

  public void removePropertyChangeListener(String arg0, PropertyChangeListener arg1) {
    _support.removePropertyChangeListener(arg0, arg1);
  }

  public boolean isPopups() {
    return _popups;
  }

  public void setPopups(boolean popups) {
    _popups = popups;
  }

  public boolean isGates() {
    return _gates;
  }

  public void setGates(boolean gates) {
    _gates = gates;
  }

  public int getPauseTime() {
    return _pauseTime;
  }

  public void setPauseTime(int pauseTime) {
    _pauseTime = pauseTime;
  }

  public boolean isSlow() {
    return _slow;
  }

  public void setSlow(boolean slow) {
    _slow = slow;
  }

  public boolean isStopIt() {
    return _stopIt;
  }

  public void setStopIt(boolean stopIt) {
    _stopIt = stopIt;
  }
}
