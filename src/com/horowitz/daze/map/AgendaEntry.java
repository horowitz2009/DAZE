package com.horowitz.daze.map;

import java.io.Serializable;

import com.horowitz.commons.Deserializable;

public class AgendaEntry implements Cloneable, Serializable {
  private static final long serialVersionUID = 5684617885487400700L;
  private String worldName;
  private String mapName;
  private String placeName;
  private String directions;

  public AgendaEntry(DMap map, String placeName) {
    super();
    this.placeName = placeName;
  }

  public AgendaEntry(String worldName, String mapName, String placeName, String directions) {
    super();
    this.worldName = worldName;
    this.mapName = mapName;
    this.placeName = placeName;
    this.directions = directions;
  }

  public AgendaEntry() {
    super();
  }

  public String getWorldName() {
    return worldName;
  }

  public void setWorldName(String worldName) {
    this.worldName = worldName;
  }

  public String getPlaceName() {
    return placeName;
  }

  public void setPlaceName(String placeName) {
    this.placeName = placeName;
  }

  public String getDirections() {
    return directions;
  }

  public void setDirections(String directions) {
    this.directions = directions;
  }

  public String getMapName() {
    return mapName;
  }

  public void setMapName(String mapName) {
    this.mapName = mapName;
  }

  @Override
  public String toString() {
    return worldName + "::" + mapName + " - " + placeName;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {

    return super.clone();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((mapName == null) ? 0 : mapName.hashCode());
    result = prime * result + ((placeName == null) ? 0 : placeName.hashCode());
    result = prime * result + ((worldName == null) ? 0 : worldName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AgendaEntry other = (AgendaEntry) obj;
    if (mapName == null) {
      if (other.mapName != null)
        return false;
    } else if (!mapName.equals(other.mapName))
      return false;
    if (placeName == null) {
      if (other.placeName != null)
        return false;
    } else if (!placeName.equals(other.placeName))
      return false;
    if (worldName == null) {
      if (other.worldName != null)
        return false;
    } else if (!worldName.equals(other.worldName))
      return false;
    return true;
  }

}
