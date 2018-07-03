package com.horowitz.daze.scan;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.logging.Logger;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.commons.Settings;
import com.horowitz.daze.GraphMazeRunner;
import com.horowitz.daze.ScreenScanner;

public class EnhancedScanner {
  private final static Logger LOGGER = Logger.getLogger("MAIN");

  private ScreenScanner scanner;
  private Settings settings;
  private MouseRobot mouse;
  private GraphMazeRunner mazeRunner;

  public EnhancedScanner(ScreenScanner scanner, Settings settings, GraphMazeRunner mazeRunner) {
    super();
    this.scanner = scanner;
    this.settings = settings;
    this.mouse = this.scanner.getMouse();
    this.mazeRunner = mazeRunner;
  }

  public void scanCurrentArea() throws IOException, AWTException, RobotInterruptedException {
    int tries = 0;
    Pixel p = null;
    do {
      // TODO check is loading
      tries++;
      LOGGER.info("Looking for diggy... " + tries);
      p = scanner.findDiggy(scanner.getScanArea());
      mouse.delay(700);
    } while (p == null && tries < 3);
    if (p != null) {
      LOGGER.info("Diggy visible...");
      // for (Direction d : Direction.values()) {
      // System.out.println(d.name());
      // }
      // scanner.writeAreaTS(scanner.getScanArea(), "scanarea.png");
    }
    // full scan of scan area
    Rectangle scanArea = new Rectangle(scanner.getScanArea());
    for (int y = scanArea.y + 30; y < scanArea.y + scanArea.height; y += 60) {
      for (int x = scanArea.x + 30; x < scanArea.x + scanArea.width; x += 60) {
        mouse.mouseMove(x, y);
        mouse.delay(150);
        Rectangle miniArea = new Rectangle(x - 61, y - 61, 122, 122);
        //scanner.writeAreaTS(miniArea, "miniarea.png");
        Pixel pp = scanner.scanOne("map/greenArrow.png", miniArea, false);
        if (pp != null) {
          LOGGER.info("BANG: " + pp);
          //return;
        }

      }
    }
    LOGGER.info("SCAN DONE!");

  }
}
