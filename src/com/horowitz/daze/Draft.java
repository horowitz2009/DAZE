package com.horowitz.daze;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;

import com.horowitz.commons.ImageData;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;

public class Draft extends JFrame {

  public Draft() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setTitle("DRAFT");
  }

  public static void main(String[] args) {
    Draft frame = new Draft();
    frame.pack();
    frame.setSize(new Dimension(frame.getSize().width + 8, frame.getSize().height + 8));
    int w = 275;// frame.getSize().width;
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int h = (int) (screenSize.height * 0.9);
    int x = screenSize.width - w;
    int y = (screenSize.height - h) / 2;
    frame.setBounds(x, y, w, h);

    JButton button = new JButton(new AbstractAction("SCAN") {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        try {
          ScreenScanner scanner = new ScreenScanner(null);
          boolean hmm = false;
          hmm = scanner.locateGameArea(false);
          
//          ImageData beta = scanner.getImageData("beta.bmp");
//          BufferedImage screen = new Robot().createScreenCapture(new Rectangle(0, 0, 500, 500));
//          Pixel p = scanner.getComparator().findImage(beta.getImage(), screen, beta.getColorToBypass());
//          if (p != null) {
//            System.err.println("FOUND IT");
//          }
          if (hmm)
            System.err.println("BAMMMM");
          System.err.println("DONE.");
        } catch (Exception | RobotInterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    frame.getContentPane().add(button);

    frame.setVisible(true);

  }

}
