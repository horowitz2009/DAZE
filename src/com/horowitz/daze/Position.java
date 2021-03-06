package com.horowitz.daze;

import com.horowitz.commons.Pixel;

public class Position {
  public int _row;
  public int _col;
  public Position _prev;
  public State _state;
  public Pixel _coords;
  
  public Position(int row, int col, Position prev, State state) {
    super();
    _row = row;
    _col = col;
    _prev = prev;
    _state = state;
    _coords = null;
  }

  public Position(int row, int col, Position prev) {
    this (row, col, prev, State.UNKNOWN);
  }
  
  public Position(int row, int col) {
    this (row, col, null, State.UNKNOWN);
  }
  
  public Position(int row, int col, State state) {
    this (row, col, null, state);
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
    result = prime * result + ((_state == null) ? 0 : _state.hashCode());
    return result;
  }


  
  @Override
  public String toString() {
    return _row + ", " + _col + " " + _state;
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
    if (_state != other._state)
      return false;
    return true;
  }

  
}
