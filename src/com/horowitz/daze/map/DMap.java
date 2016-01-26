package com.horowitz.daze.map;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.horowitz.commons.Deserializable;
import com.horowitz.commons.Deserializer;

public class DMap implements Cloneable, Serializable, Deserializable {
  private static final long serialVersionUID = 5684617885487400700L;
  private String name;
  private int priority;
  private int position;

  public DMap(String name) {
    super();
    this.name = name;
    this.priority = 2;
  }

  private List<Place> places;

  @Override
  public void deserialize(Deserializer deserializer) throws IOException {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Place> getPlaces() {
    return places;
  }

  public void setPlaces(List<Place> places) {
    this.places = places;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

}
