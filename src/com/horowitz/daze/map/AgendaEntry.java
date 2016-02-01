package com.horowitz.daze.map;

import java.io.Serializable;

public class AgendaEntry implements Cloneable, Serializable {
  private static final long serialVersionUID = 5684617885487400700L;

  private String mapName;
  private String placeName;

  public AgendaEntry(String mapName, String placeName) {
    super();
    this.mapName = mapName;
    this.placeName = placeName;
  }

  public AgendaEntry() {
    super();
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
    return true;
  }

}
