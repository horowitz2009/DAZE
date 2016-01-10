package com.horowitz.daze;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MazeCanvas extends JPanel {

  private static final long serialVersionUID = 6465388993021625830L;

  public MazeCanvas() {
    super();
    _blockSize = 6;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    // System.out.println("SIZE:" + getSize());
    int w = getSize().width;
    int h = getSize().height;

    _absCenter = new Point(w / 2 - _blockSize / 2, h / 2 - _blockSize / 2);

    // g.drawArc(_absCenter.x, _absCenter.y, _blockSize, _blockSize, 0, 360);

    g.setColor(Color.LIGHT_GRAY);

    // drawSomething(g);
    //
    for (Position position : Collections.synchronizedSet(_matrix)) {
      drawBlock(position, g);
    }
    
    if (_diggy != null) {
      drawDiggy(_diggy, g);
    }

    // for (int i = 0; i < w / blockSize; i++) {
    // for (int j = 0; j < h / blockSize; j++) {
    // g.drawRect(0 + i * blockSize, 0 + j * blockSize, blockSize, blockSize);
    //
    // }
    // }

    // g.drawLine(0, 0, 16, 16);
  }

  private Point _absCenter;
  private int _blockSize;

  private void drawBlock(Position pos, Color color, Graphics g) {
    Color oldColor = g.getColor();
    g.setColor(color);
    int x1 = _absCenter.x + pos._row * _blockSize + 1;
    int y1 = _absCenter.y + pos._col * _blockSize + 1;
    // int x2 = x1 + _blockSize - 2;
    // int y2 = y1 + _blockSize - 2;
    g.fillRect(x1, y1, _blockSize - 1, _blockSize - 1);
    g.setColor(oldColor);
    // g.drawRect(_absCenter.x + pos._row * _blockSize, _absCenter.y + pos._col
    // * _blockSize, _blockSize, _blockSize);
    // g.drawLine(x1, y1, x2, y1);
    // g.drawLine(x2, y1, x2, y2);
    // g.drawLine(x2, y2, x1, y2);
    // g.drawLine(x1, y2, x1, y1);
  }

  @Override
  public void paint(Graphics gc) {
    // gc.setFill(Color.GREEN);
    // gc.setStroke(Color.BLUE);
    // gc.setLineWidth(5);
    // gc.strokeLine(40, 10, 10, 40);
    // gc.fillOval(10, 60, 30, 30);
    // gc.strokeOval(60, 60, 30, 30);
    // gc.fillRoundRect(110, 60, 30, 30, 10, 10);
    // gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
    // gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
    // gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
    // gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);
    // gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
    // gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
    // gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);
    // gc.fillPolygon(new double[] { 10, 40, 10, 40 }, new double[] { 210, 210,
    // 240, 240 }, 4);
    // gc.strokePolygon(new double[] { 60, 90, 60, 90 }, new double[] { 210,
    // 210, 240, 240 }, 4);
    // gc.strokePolyline(new double[] { 110, 140, 110, 140 }, new double[] {
    // 210, 210, 240, 240 }, 4);
    super.paint(gc);
  }

  private void drawDiggy(Position pos, Graphics g) {
    drawBlock(pos, Color.yellow, g);
  }

  private void drawBlock(Position pos, Graphics g) {
    Color c = Color.white;
    if (pos._state != null) {
      if (pos._state == State.VISITED) {
        c = Color.white;
      } else if (pos._state == State.OBSTACLE) {
        c = Color.black;
      }
      if (pos._state == State.GREEN) {
        c = Color.green;
      } else if (pos._state == State.CHECKED) {
        c = Color.lightGray;
      }
    }
    drawBlock(pos, c, g);
  }

  private void drawSomething(Graphics g) {
    drawBlock(new Position(0, 0, State.UNKNOWN), g);
    drawBlock(new Position(0, 1, State.CHECKED), g);
    drawBlock(new Position(0, 2, State.VISITED), g);
    drawBlock(new Position(1, 2, State.OBSTACLE), g);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Canvas Test");

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setBounds(200, 200, ss.width / 4, ss.height / 4);

    frame.getContentPane().add(new MazeCanvas());

    frame.setVisible(true);
  }

  Position _diggy = null;

  private Set<Position> _matrix = new HashSet<>();

  public PropertyChangeListener createPropertyChangeListener() {
    final PropertyChangeListener listener = new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("DIGGY")) {
          _diggy = (Position) e.getNewValue();
        } else if (e.getPropertyName().equals("CLEARED")) {
          _matrix.clear();
        } else if (e.getPropertyName().equals("POS_REMOVED")) {
          _matrix.remove(e.getNewValue());
        } else if (e.getPropertyName().equals("GREEN_CLICKED")) {
          //DO NOTHING
        } else {
          Position pos = (Position) e.getNewValue();
          _matrix.add(pos);
        }

        repaint();

      }
    };
    return listener;
  }

}