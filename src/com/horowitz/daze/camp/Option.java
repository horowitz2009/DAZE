package com.horowitz.daze.camp;

import java.io.*;

import com.horowitz.commons.*;

public class Option implements Cloneable, Serializable, Deserializable {
  private static final long serialVersionUID = 4586051312367178243L;
  private String name;
  private String abbr;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Option(String name) {
    super();
    this.name = name;
  }

  @Override
  public void deserialize(Deserializer deserializer) throws IOException {
    // TODO Auto-generated method stub

  }

}
