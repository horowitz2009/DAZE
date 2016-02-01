package com.horowitz.daze.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Agenda implements Serializable, Cloneable {
  private static final long serialVersionUID = -3179495716926724628L;
  private String name;
  private List<AgendaEntry> entries;

  public Agenda(String name) {
    super();
    this.name = name;
  }

  public Agenda() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<AgendaEntry> getEntries() {
    return entries;
  }

  public void setEntries(List<AgendaEntry> entries) {
    this.entries = entries;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    Object clone = super.clone();
    Agenda other = (Agenda) clone;
    other.entries = new ArrayList<AgendaEntry>(this.entries);
    for (AgendaEntry ae : other.entries) {
      ae = (AgendaEntry) ae.clone();
    }

    return clone;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entries == null) ? 0 : entries.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    Agenda other = (Agenda) obj;
    if (entries == null) {
      if (other.entries != null)
        return false;
    } else if (!entries.equals(other.entries))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

}
