package com.horowitz.daze;

import java.awt.AWTException;
import java.awt.Point;
import java.io.IOException;
import java.util.logging.Logger;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;

public class MazeProtocol implements GameProtocol {

  private final static Logger LOGGER = Logger.getLogger("MAIN");
  private GraphMazeRunner _mazeRunner;
  private Pixel _initialMousePosition;

  private ScreenScanner _scanner;
  private MouseRobot _mouse;

  public MazeProtocol(ScreenScanner scanner, MouseRobot mouse, GraphMazeRunner mazeRunner) {
    _scanner = scanner;
    _mouse = mouse;
    _mazeRunner = mazeRunner;
  }
  
  

  public boolean preExecute() throws AWTException, IOException, RobotInterruptedException {
    Point position = _scanner.getMouse().getPosition();

    _initialMousePosition = new Pixel(position.x, position.y);
    assert _initialMousePosition != null;
    if (!_scanner.isPixelInArea(_initialMousePosition, _scanner.getScanArea())) {
      _initialMousePosition = null;
    }
    return true; // (_initialMousePosition != null);
  }

  /**
   * Executes only if preExecute returns true.
   * @throws RobotInterruptedException 
   */
  @Override
  public void update() throws RobotInterruptedException {
    // TODO
    // ensure we're in location.
    // Version 1: Just click the last location
    // Version 2: Go to location from a list

    boolean failure = false;

    try {
      Pixel p = _scanner.scanOne("greenArrow.bmp", _scanner._lastLocationButtonArea, false);
      if (p != null) {
        // not in location
        _mouse.click(p);
        LOGGER.info("Go to last location ...");
        LOGGER.info("Wait 10s ...");
        _mouse.delay(10000);
        p = _scanner.scanOne("greenArrow.bmp", _scanner._lastLocationButtonArea, false);
        if (p != null) {
          failure = true;
        }
      }
    } catch (IOException | AWTException e) {
      e.printStackTrace();
      failure = true;
    }

    if (failure) {
      LOGGER.info("Failed to initialize Maze protocol!");
    }

  }

  @Override
  public void execute() throws RobotInterruptedException {
    try {
      Pixel p;
      if (_initialMousePosition != null) {
        // start looking diggy from this position
        p = _scanner.lookForDiggyAroundHere(_initialMousePosition, 2);
      } else {
        p = _scanner.findDiggy(_scanner.getScanArea());
      }
      if (p != null) {
        // good, we have diggy. we can start...
        _mazeRunner.traverse(p);
      } else {
        _failures++;
        LOGGER.warning("COULDN'T FIND DIGGY... " + _failures);
        if (_failures >= 10) {
          _failures = 0;
          p = _scanner.scanOne("greenArrow.bmp", _scanner._campButtonArea, false);
          if (p != null) {
            LOGGER.info("Go to camp, then go back to last location");
            _mouse.click(p);
            LOGGER.info("Wait 12s...");
            _mouse.delay(12000);
          }
        }
      }

    } catch (Exception e) {
      if (e instanceof RobotInterruptedException)
        throw new RobotInterruptedException();
      LOGGER.warning("ERROR!");
      e.printStackTrace();
    }
  }

  private int _failures = 0;
}
