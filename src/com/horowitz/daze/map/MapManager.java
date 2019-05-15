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
import com.horowitz.commons.Settings;
import com.horowitz.daze.ScreenScanner;

public class MapManager {
  private final static Logger LOGGER = Logger.getLogger("MAIN");
  private List<DMap> _maps;
  private ScreenScanner _scanner;
  private MouseRobot _mouse;
  private Settings _settings;

  public MapManager(ScreenScanner scanner, Settings settings) {
    super();
    _scanner = scanner;
    _settings = settings;
    _mouse = _scanner.getMouse();
    _maps = new ArrayList<DMap>();
  }

  public synchronized void loadMaps() {
    _maps.clear();
    File mapsFolder = new File("storage/maps");
    if (mapsFolder.exists()) {
      LOGGER.info("Loading maps...");
      System.out.println("loading maps...");
      File[] mapFiles = mapsFolder.listFiles();
      for (File f : mapFiles) {
        try {
          if (f.isFile() && f.canRead()) {
            System.out.println(f.getName());
            DMap map = new JsonStorage().loadMap(f);
            _maps.add(map);
            LOGGER.info(map.getName());
          }
        } catch (IOException e) {
          LOGGER.warning("Failed to load " + f.getAbsolutePath());
          e.printStackTrace();
        }
      }

      System.out.println("Total " + _maps.size() + " maps loaded.");
      LOGGER.info("Total " + _maps.size() + " maps loaded.");
    }
  }

  public List<DMap> getMaps() {
    return _maps;
  }

  public List<String> getMapNames() {
    List<String> mapNames = new ArrayList<String>();
    for (DMap dMap : _maps) {
      mapNames.add(dMap.getName());
    }
    return mapNames;
  }

  public boolean gotoMap(DMap map)
      throws RobotInterruptedException, IOException, AWTException, PlaceUnreachableException {
    boolean success = false;
    if (map != null) {
      success = _scanner.gotoMap(map);
      if (success) {
        LOGGER.info("Entered map " + map.getName());
      } else {
        throw new PlaceUnreachableException("Map " + map.getName() + " not found!");
      }
    }
    return success;
  }

  public DMap getMap(String world, String name) {
    for (DMap map : _maps) {
      if (map.getWorld().equals(world) && map.getName().equals(name)) {
        return map;
      }
    }
    return null;
  }

  public Pixel findCamp(DMap map) throws RobotInterruptedException, IOException, AWTException {
    return _scanner.findCamp(map);
  }

  public boolean gotoPlace(String worldName, String mapName, String placeName)
      throws RobotInterruptedException, IOException, AWTException, PlaceUnreachableException {
    boolean success = false;
    DMap map = getMap(worldName, mapName);

    Place place = null;
    for (Place pl : map.getPlaces()) {
      if (pl.getName().equalsIgnoreCase(placeName)) {
        place = pl;
        break;
      }
    }
    success = place != null && gotoMap(map);
    if (success) {
      _mouse.mouseMove(_scanner.getSafePoint());
      _mouse.delay(100);
      // map.getAnchorImage()
      Pixel p = findCamp(map);
      if (p != null) {
        p.x += place.getCoords().x;
        p.y += place.getCoords().y;
        ensurePlace(p);
        Pixel pp = findCamp(map);
        if (pp != null) {
          p = pp;
          p.x += place.getCoords().x;
          p.y += place.getCoords().y;
        }
        success = checkPlaceIsPlayable(p);

      } else {
        throw new PlaceUnreachableException("Camp can't be found");
      }

    }
    return success;
  }

  private void ensurePlace(Pixel p) throws RobotInterruptedException {
    Rectangle area = _scanner.getScanArea();
    int distance = 0;
    if (p.x < area.x) {
      // 2
      distance = area.x + 5 - p.x;
      p.x = area.x + 5;
    } else if (p.x > area.x + area.getWidth()) {
      // 3
      distance = (int) (area.x + area.getWidth() - 5 - p.x);
      p.x = (int) (area.x + area.getWidth() - 5);
    }
    if (distance != 0) {
      // need dragging
      int x1;
      int x2;
      int y = area.y + area.height - 35;
      int d = distance / area.width;
      int rest = distance - (d * area.width);
      for (int i = 0; i < d; i++) {
        if (distance > 0) {
          // positive => move east
          x1 = area.x + 5;
          x2 = x1 + area.width;
        } else {
          x1 = area.x + area.width - 5;
          x2 = x1 - area.width;
        }
        _mouse.drag(x1, y, x2, y);
        _mouse.delay(100);
      }
      if (distance > 0) {
        // positive => move east
        x1 = area.x + 5;
        x2 = x1 + rest;
      } else {
        x1 = area.x + area.width - 5;
        x2 = x1 + rest;
      }
      _mouse.drag(x1, y, x2, y);

      _mouse.delay(3000);
    }

  }

  private boolean checkPlaceIsPlayable(Pixel p) throws RobotInterruptedException, IOException, AWTException {
    _mouse.mouseMove(_scanner.getSafePoint());
    _mouse.delay(100);

    Rectangle area = new Rectangle(p.x - 35, p.y - 60, 111, 90);
    Pixel pd = _scanner.scanOne("images/placeDone.png", area, false);
    if (pd != null) {
      LOGGER.info("Place done! Moving forward...");
      return false;
    } else {
      area = new Rectangle(p.x - 80, p.y, 125, 94);
      pd = _scanner.scanOne("images/placeClock.png", area, false);
      if (pd != null) {
        LOGGER.info("Repeatable not yet available! Moving forward...");
        return false;
      } else {
        LOGGER.info("Entering...");
        _mouse.click(p);
        _mouse.delay(100);
        return true;
      }
    }
  }

  public boolean gotoCamp() throws RobotInterruptedException, IOException, AWTException {
    return _scanner.gotoCamp(2);
  }

  public void doKitchen() throws RobotInterruptedException, IOException, AWTException {
    _mouse.click(_scanner.getKitchen());
    _mouse.delay(_settings.getInt("camp.delay", 850));
    _scanner.scanOneFast("images/buttonRestartAll.png", null, true);
    _mouse.delay(300);
    _scanner.scanOneFast("images/x.png", null, true);
    _mouse.delay(300);
    LOGGER.info("Kitchen done.");
  }

  public void doCaravans() throws RobotInterruptedException, IOException, AWTException {
    _mouse.click(_scanner.getCaravan());
    _mouse.delay(_settings.getInt("camp.delay", 850));
    _scanner.scanOneFast("images/buttonRestartAll.png", null, true);
    _mouse.delay(300);
    _scanner.scanOneFast("images/x.png", null, true);
    _mouse.delay(300);
    LOGGER.info("Caravans done.");
  }

  public void doFoundry() throws RobotInterruptedException, IOException, AWTException {
    _mouse.click(_scanner.getFoundry());
    _mouse.delay(_settings.getInt("camp.delay", 850));
    _scanner.scanOneFast("images/buttonRestartAll.png", null, true);
    _mouse.delay(300);
    _scanner.scanOneFast("images/x.png", null, true);
    _mouse.delay(300);
    LOGGER.info("Foundry done.");
  }

  public void doMenu(String imageName) throws RobotInterruptedException, IOException, AWTException {
    Rectangle area = _scanner.generateWindowedArea(621, 425);
    area.y = _scanner.getTopLeft().y + 149;
    area.x += 506;
    area.width = 95;

    Rectangle buttonArea = new Rectangle();
    buttonArea.x = area.x;
    buttonArea.width = area.width;
    buttonArea.height = 60;
    buttonArea.y = area.y + 49;

    for (int i = 0; i < 6; i++) {
      Pixel p = _scanner.scanOne(imageName, buttonArea, true);
      if (p != null) {
        _mouse.delay(300);
      }
      // _scanner.writeArea(buttonArea, "kitchen" + i + ".jpg");
      buttonArea.y += 60;
    }

    // _scanner.writeArea(area, "foundry.jpg");

    // close the window
    _mouse.click(area.x + 87, area.y + 5);
    _mouse.delay(1000);
  }

}
