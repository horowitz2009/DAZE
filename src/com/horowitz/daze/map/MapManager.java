package com.horowitz.daze.map;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.daze.ScreenScanner;

public class MapManager {
  private final static Logger LOGGER = Logger.getLogger("MAIN");
  private List<DMap> _maps;
  private ScreenScanner _scanner;
  private MouseRobot _mouse;

  public MapManager(ScreenScanner scanner) {
    super();
    _scanner = scanner;
    _mouse = _scanner.getMouse();
  }

  public void loadMaps() {
    _maps = new ArrayList<DMap>();
    File mapsFolder = new File("storage/maps");
    if (mapsFolder.exists()) {
      LOGGER.info("Loading maps...");

      File[] mapFiles = mapsFolder.listFiles();
      for (File f : mapFiles) {
        try {
          DMap map = new JsonStorage().loadMap(f);
          _maps.add(map);
          LOGGER.info(map.getName());
        } catch (IOException e) {
          LOGGER.warning("Failed to load " + f.getAbsolutePath());
        }
      }

      LOGGER.info("Total " + _maps.size() + " maps loaded.");
    }
  }

  public List<DMap> getMaps() {
    return _maps;
  }

  public boolean gotoMap(String mapName) throws RobotInterruptedException, IOException, AWTException {
    boolean success = false;
    DMap map = getMap(mapName);
    if (map != null) {
      success = _scanner.gotoMap(map.getPosition());
      if (success) {
        LOGGER.info("Entered map " + map.getName());
      }
    }
    return success;
  }

  public DMap getMap(String name) {
    for (DMap map : _maps) {
      if (map.getName().equals(name)) {
        return map;
      }
    }
    return null;
  }

  public Pixel findCamp() throws RobotInterruptedException, IOException, AWTException {
    return _scanner.findCamp();
  }

  public boolean gotoPlace(String mapName, String placeName) throws RobotInterruptedException, IOException,
      AWTException {
    boolean success = false;
    DMap map = getMap(mapName);
    Place place = null;
    for (Place pl : map.getPlaces()) {
      if (pl.getName().equals(placeName)) {
        place = pl;
        break;
      }
    }
    success = place != null && gotoMap(mapName);
    if (success) {
      _mouse.mouseMove(_scanner.getSafePoint());
      _mouse.delay(1500);
      Pixel p = findCamp();
      if (p != null) {
        LOGGER.info("CAMP COORDS: " + p);
        p.x += place.getCoords().x;
        p.y += place.getCoords().y;
        success = checkPlaceIsPlayable(p);

      }

    }
    return success;
  }

  private boolean checkPlaceIsPlayable(Pixel p) throws RobotInterruptedException, IOException, AWTException {
    Rectangle area = new Rectangle(p.x + -35, p.y - 35, 70, 70);
    Pixel pd = _scanner.scanOne("map/placeDone.bmp", area, false);
    if (pd != null) {
      LOGGER.info("Place done! Moving forward...");
      return false;
    } else {
      _mouse.click(p);
      _mouse.delay(1000);
      // _scanner.writeArea(_scanner._diggyCaveArea, "diggyCave.png");
      Pixel pc = _scanner.scanOne("map/diggyCave.bmp", null, false);
      if (pc != null) {
        
        _mouse.mouseMove(pc);
        area = new Rectangle(pc.x + 421, pc.y + 131, 57, 36);
        Pixel pp = _scanner.scanOne("map/progressFull.bmp", area, false);
        if (pp != null) {
          LOGGER.info("This place is done! Moving forward...");
          _mouse.click(pc.x + 456, pc.y + 6);
          _mouse.delay(1000);
          return false;
        }
        LOGGER.info("hmm");
        //find the entry
        Rectangle area2 = new Rectangle(pc.x +193, pc.y+195, 100, 60);
        pp = _scanner.scanOne("map/placeEntry.bmp", area2, true);//CLICK!!!
        if (pp == null) {
          area2 = new Rectangle(pc.x +193, pc.y+365, 100, 60);
          pp = _scanner.scanOne("map/placeEntry.bmp", area2, true);//CLICK!!!
        }
        return pp != null;
      }
    }
    return false;
  }
}
