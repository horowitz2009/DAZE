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

  public List<String> getMapNames() {
    List<String> mapNames = new ArrayList<String>();
    for (DMap dMap : _maps) {
      mapNames.add(dMap.getName());
    }
    return mapNames;
  }

  public boolean gotoMap(DMap map) throws RobotInterruptedException, IOException, AWTException,
      PlaceUnreachableException {
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

  public Pixel findCamp() throws RobotInterruptedException, IOException, AWTException {
    return _scanner.findCamp();
  }

  public boolean gotoPlace(String worldName, String mapName, String placeName) throws RobotInterruptedException,
      IOException, AWTException, PlaceUnreachableException {
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
      _mouse.delay(1500);
      Pixel p = findCamp();
      if (p != null) {
        LOGGER.info("CAMP COORDS: " + p);
        p.x += place.getCoords().x;
        p.y += place.getCoords().y;
        ensurePlace(p);
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

      _mouse.delay(1000);
    }

  }

  private boolean checkPlaceIsPlayable(Pixel p) throws RobotInterruptedException, IOException, AWTException {
    _mouse.mouseMove(_scanner.getSafePoint());
    _mouse.delay(100);

    Rectangle area = new Rectangle(p.x + -35, p.y - 35, 70, 70);
    Pixel pd = _scanner.scanOne("map/placeDone.bmp", area, false);
    if (pd != null) {
      LOGGER.info("Place done! Moving forward...");
      return false;
    } else {
      _mouse.click(p);
      _mouse.delay(10);
      _mouse.mouseMove(_scanner.getSafePoint());
      _mouse.delay(1000);
      // _scanner.writeArea(_scanner._diggyCaveArea, "diggyCave.png");
      Pixel pc = _scanner.scanOne("map/diggyCave.bmp", null, false);
      if (pc != null) {
        area = new Rectangle(pc.x + 421, pc.y + 131, 57, 36);
        // Pixel pp = _scanner.scanOne("map/progressFull.bmp", area, false);
        // if (pp != null) {
        // LOGGER.info("This place is done! Moving forward...");
        // _mouse.click(pc.x + 456, pc.y + 6);
        // _mouse.delay(1000);
        // return false;
        // }
        // LOGGER.info("hmm");
        // find the entry
        Rectangle area2 = new Rectangle(pc.x + 193, pc.y + 155, 100, 280);
        Pixel pp = _scanner.scanOne("map/placeEntry2.bmp", area2, true);// CLICK!!!
        // if (pp == null) {
        // area2 = new Rectangle(pc.x +193, pc.y+365, 100, 60);
        // pp = _scanner.scanOne("map/placeEntry.bmp", area2, true);//CLICK!!!
        // }
        if (pp == null) {
          LOGGER.info("UH OH...");
          _mouse.click(pc.x + 456, pc.y + 6);
          _mouse.delay(1000);
          return false;
        }
        return pp != null;
      }
    }
    return false;
  }

  public boolean gotoCamp() throws RobotInterruptedException, IOException, AWTException {
    return _scanner.gotoCamp();
  }

  public void doKitchen() throws RobotInterruptedException, IOException, AWTException {
    _mouse.click(_scanner.getKitchen());
    _mouse.delay(2000);
    doMenu("camp/restartu.bmp");
    LOGGER.info("Kitchen done.");
  }
  
  public void doCaravans() throws RobotInterruptedException, IOException, AWTException {
    Pixel p = _scanner.getKitchen();
    p.x -= 686;
    _mouse.click(p);
    _mouse.delay(2000);
    doMenu("camp/restartu.bmp");
    LOGGER.info("Caravans done.");
  }

  public void doFoundry() throws RobotInterruptedException, IOException, AWTException {
    Pixel p = _scanner.getKitchen();
    p.x += 211;
    int xlim = _scanner.getBottomRight().x - 143;
    if (p.x > xlim) {
      int y = p.y + 205;
      int dist =  p.x - xlim;
      int x1 = p.x - 211;
      _mouse.drag(x1, y, x1 - dist, y);
      _mouse.delay(200);
      _scanner.gotoCamp();
      p = _scanner.getKitchen();
      p.x += 211;
    }
    _mouse.click(p);
    _mouse.delay(2000);
    doMenu("camp/restartF.bmp");
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
      //_scanner.writeArea(buttonArea, "kitchen" + i + ".jpg");
      buttonArea.y += 60;
    }
    
    //_scanner.writeArea(area, "foundry.jpg");
    
    // close the window
    _mouse.click(area.x + 87, area.y + 5);
    _mouse.delay(1000);
  }
  
}
