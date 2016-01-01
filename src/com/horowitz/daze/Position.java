package com.horowitz.daze;

import com.horowitz.commons.Pixel;

public class Position {
  public int _row;
  public int _col;
  public Position _prev;
  public Status _state;
  public Pixel _coords;
  
  public Position(int row, int col, Position prev, Status state) {
    super();
    _row = row;
    _col = col;
    _prev = prev;
    _state = state;
    _coords = null;
  }

  public Position(int row, int col, Position prev) {
    this (row, col, prev, Status.UNKNOWN);
  }
  
  public Position(int row, int col) {
    this (row, col, null, Status.UNKNOWN);
  }
  
  public Position(int row, int col, Status state) {
    this (row, col, null, state);
  }
  




  public static State[] STATES = { new State("unknown")};
  

  public static class State {
    public String _name;

    public State(String name) {
      super();
      _name = name;
    }
    
    
  }


	public boolean same(Position otherPos) {
	  return this._row == otherPos._row && this._col == otherPos._col;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _col;
    result = prime * result + _row;
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
    Position other = (Position) obj;
    if (_col != other._col)
      return false;
    if (_row != other._row)
      return false;
    return true;
  }

  
}
