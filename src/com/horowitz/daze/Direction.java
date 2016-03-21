package com.horowitz.daze;

import java.util.Arrays;

public class Direction {
  public static final Direction NORTH = new Direction("N", 0, -1);
  public static final Direction EAST = new Direction("E", 1, 0);
  public static final Direction SOUTH = new Direction("S", 0, 1);
  public static final Direction WEST = new Direction("W", -1, 0);
  public static final Direction[] ALL = new Direction[] { NORTH, EAST, SOUTH, WEST };
  private String name;
  private int xOff;
  private int yOff;

  public Direction(String name, int xOff, int yOff) {
    super();
    this.name = name;
    this.xOff = xOff;
    this.yOff = yOff;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getxOff() {
    return xOff;
  }

  public void setxOff(int xOff) {
    this.xOff = xOff;
  }

  public int getyOff() {
    return yOff;
  }

  public void setyOff(int yOff) {
    this.yOff = yOff;
  }

  public static Direction[] buildDirections(String s) {
    if (s == null || s.length() != 4)
      return Arrays.copyOf(ALL, 4);
    
    Direction d[] = new Direction[4];
    for (int i = 0; i < s.length(); i++) {
      String ss = s.substring(i, i + 1).toUpperCase();
      
      for (Direction direction : ALL) {
        if (direction.getName().equals(ss))
          d[i] = direction;
      }
    }
    return d;
  }

}
