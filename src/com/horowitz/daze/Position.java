package com.horowitz.daze;

public class Position {
  public int _row;
  public int _col;
  public Position _prev;
  public Status _state;
  
  public Position(int row, int col, Position prev, Status state) {
    super();
    _row = row;
    _col = col;
    _prev = prev;
    _state = state;
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

  
}
