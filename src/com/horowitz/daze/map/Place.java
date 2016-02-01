package com.horowitz.daze.map;

import java.io.IOException;
import java.io.Serializable;

import com.horowitz.commons.Deserializable;
import com.horowitz.commons.Deserializer;
import com.horowitz.commons.Pixel;

public class Place implements Cloneable, Serializable, Deserializable {
  private static final long serialVersionUID = -771156176023851865L;
  private String name;

  public Place(String name, Pixel coords, boolean repeatable) {
    super();
    this.name = name;
    this.coords = coords;
    this.repeatable = repeatable;
  }

  private Pixel coords;
  private boolean repeatable;

  @Override
  public void deserialize(Deserializer deserializer) throws IOException {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Pixel getCoords() {
    return coords;
  }

  public void setCoords(Pixel coords) {
    this.coords = coords;
  }

  public boolean isRepeatable() {
    return repeatable;
  }

  public void setRepeatable(boolean repeatable) {
    this.repeatable = repeatable;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((coords == null) ? 0 : coords.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + (repeatable ? 1231 : 1237);
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
    Place other = (Place) obj;
    if (coords == null) {
      if (other.coords != null)
        return false;
    } else if (!coords.equals(other.coords))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (repeatable != other.repeatable)
      return false;
    return true;
  }

}
