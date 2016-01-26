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
}
