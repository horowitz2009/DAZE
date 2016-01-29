package com.horowitz.daze.map;

import java.io.IOException;
import java.io.Serializable;

import com.horowitz.commons.Deserializable;
import com.horowitz.commons.Deserializer;

public class Agenda implements Cloneable, Serializable, Deserializable {
  private static final long serialVersionUID = 5684617885487400700L;

  private String mapName;
  private String placeName;

  public Agenda(String mapName, String placeName) {
    super();
    this.mapName = mapName;
    this.placeName = placeName;
  }

  @Override
  public void deserialize(Deserializer deserializer) throws IOException {
  }

  public String getMapName() {
    return mapName;
  }

  public void setMapName(String mapName) {
    this.mapName = mapName;
  }

  public String getPlaceName() {
    return placeName;
  }

  public void setPlaceName(String placeName) {
    this.placeName = placeName;
  }

  @Override
  public String toString() {
    return mapName + " - " + placeName;
  }

}
