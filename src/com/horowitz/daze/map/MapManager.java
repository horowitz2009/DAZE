package com.horowitz.daze.map;

import java.awt.AWTException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.daze.ScreenScanner;

public class MapManager {
  private final static Logger LOGGER = Logger.getLogger("MAIN");
  private List<DMap> maps;
  private ScreenScanner scanner;

  public MapManager(ScreenScanner scanner) {
    super();
    this.scanner = scanner;
  }

  public void loadMaps() {
    maps = new ArrayList<DMap>();
    File mapsFolder = new File("storage/maps");
    if (mapsFolder.exists()) {
      LOGGER.info("Loading maps...");

      File[] mapFiles = mapsFolder.listFiles();
      for (File f : mapFiles) {
        try {
          DMap map = new JsonStorage().loadMap(f);
          maps.add(map);
          LOGGER.info(map.getName());
        } catch (IOException e) {
          LOGGER.warning("Failed to load " + f.getAbsolutePath());
        }
      }

      LOGGER.info("Total " + maps.size() + " maps loaded.");
    }
  }

  public List<DMap> getMaps() {
    return maps;
  }

  public void gotoMap(String mapName) throws RobotInterruptedException, IOException, AWTException {
    DMap map = getMap(mapName);
    if (map != null) {
      scanner.gotoMap(map.getPosition());
      
      
      
    }
  }

  public DMap getMap(String name) {
    for (DMap map : maps) {
      if (map.getName().equals(name)) {
        return map;
      }
    }
    return null;
  }
}
