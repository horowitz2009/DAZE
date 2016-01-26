package com.horowitz.daze.map;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonStorage {
  private String mapPath = "storage/maps";
  private Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public DMap loadMap(String name) throws IOException {
    return loadMap(new File(mapPath + "/" + name + ".json"));
  }

  public DMap loadMap(File mapFile) throws IOException {
    String json = FileUtils.readFileToString(mapFile);

    DMap map = gson.fromJson(json, DMap.class);

    return map;
  }

  public void saveMap(DMap map) throws IOException {

    String json = gson.toJson(map);

    FileUtils.writeStringToFile(new File(mapPath + "/" + map.getName() + ".json"), json);
  }

  public String getMapPath() {
    return mapPath;
  }

  public void setMapPath(String mapPath) {
    this.mapPath = mapPath;
  }

}
