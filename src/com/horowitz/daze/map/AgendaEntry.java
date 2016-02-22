package com.horowitz.daze.map;

import java.io.Serializable;

public class AgendaEntry implements Cloneable, Serializable {
  private static final long serialVersionUID = 5684617885487400700L;

  private DMap map;
  private String placeName;

  public AgendaEntry(DMap map, String placeName) {
    super();
    this.map = map;
    this.placeName = placeName;
  }

  public AgendaEntry() {
    super();
  }

  public String getPlaceName() {
    return placeName;
  }

  public void setPlaceName(String placeName) {
    this.placeName = placeName;
  }

  @Override
  public String toString() {
    return map.toString() + " - " + placeName;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {

    return super.clone();
  }

  public DMap getMap() {
    return map;
  }

  public void setMap(DMap map) {
    this.map = map;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((map == null) ? 0 : map.hashCode());
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
    if (map == null) {
      if (other.map != null)
        return false;
    } else if (!map.equals(other.map))
      return false;
    if (placeName == null) {
      if (other.placeName != null)
        return false;
    } else if (!placeName.equals(other.placeName))
      return false;
    return true;
  }

}
