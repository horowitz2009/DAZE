package com.horowitz.daze;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class Hmmmm {
  public JComponent makeUI() {
    Box box = Box.createVerticalBox();
    DragMouseAdapter dh = new DragMouseAdapter(JFrame.getFrames()[0]);
    box.addMouseListener(dh);
    box.addMouseMotionListener(dh);

    int idx = 0;
    for (JComponent c : Arrays.asList(new JLabel("<html>111<br>11<br>11"), new JButton("2"), new JCheckBox("3"),
        new JTextField(14))) {
      box.add(createToolbarButton(idx++, c));
    }
    JPanel p = new JPanel(new BorderLayout());
    p.add(box, BorderLayout.NORTH);
    return p;
  }

  private static JComponent createToolbarButton(int i, JComponent c) {
    JLabel l = new JLabel(String.format(" %04d ", i));
    l.setOpaque(true);
    l.setBackground(Color.RED);
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
        BorderFactory.createLineBorder(Color.BLUE, 2)));
    p.add(l, BorderLayout.WEST);
    p.add(c);
    p.setOpaque(false);
    return p;
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        createAndShowGUI();
      }
    });
  }

  public static void createAndShowGUI() {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new Hmmmm().makeUI());
    f.setSize(320, 240);
    f.setLocationRelativeTo(null);
    f.setVisible(true);
  }
}