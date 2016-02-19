package com.horowitz.daze.map;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.horowitz.commons.Deserializable;
import com.horowitz.commons.Deserializer;

public class DMap implements Cloneable, Serializable, Deserializable {
  private static final long serialVersionUID = 5684617885487400700L;
  private String name;
  private String world;
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

  public String getWorld() {
    return world;
  }

  public void setWorld(String world) {
    this.world = world;
  }

  @Override
  public String toString() {
    String s = "";
    if (world != null)
      s = s + world + " ";
    s = s + name;
    return s;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((places == null) ? 0 : places.hashCode());
    result = prime * result + position;
    result = prime * result + priority;
    result = prime * result + ((world == null) ? 0 : world.hashCode());
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
    DMap other = (DMap) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (places == null) {
      if (other.places != null)
        return false;
    } else if (!places.equals(other.places))
      return false;
    if (position != other.position)
      return false;
    if (priority != other.priority)
      return false;
    if (world == null) {
      if (other.world != null)
        return false;
    } else if (!world.equals(other.world))
      return false;
    return true;
  }

}
