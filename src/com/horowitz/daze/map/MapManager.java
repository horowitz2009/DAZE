package com.horowitz.daze.map;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MapManager {
  private final static Logger LOGGER = Logger.getLogger("MAIN");
  private List<DMap> maps;

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
}
