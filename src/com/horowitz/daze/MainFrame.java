package com.horowitz.daze;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.print.attribute.standard.Destination;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.ColorFiltering;

import com.horowitz.commons.DateUtils;
import com.horowitz.commons.ImageData;
import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.MyImageIO;
import com.horowitz.commons.MyLogger;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.commons.Service;
import com.horowitz.commons.Settings;
import com.horowitz.commons.TemplateMatcher;
import com.horowitz.ocr.OCRB;

public class MainFrame extends JFrame {

  private static final long serialVersionUID = 3458306923208534910L;

  private final static Logger LOGGER = Logger.getLogger("MAIN");

  private static String APP_TITLE = "Daze v0.01";

  private Settings _settings;
  private Stats _stats;
  private MouseRobot _mouse;
  private ScreenScanner _scanner;

  private JLabel _mouseInfoLabel;

  private CaptureDialog captureDialog;

  private boolean _stopAllThreads;

  private JTextField _findThisTF;

  private TemplateMatcher _matcher;

  private JToggleButton _fishToggle;
  private JToggleButton _shipsToggle;

  private JToggleButton _industriesToggle;
  // private JToggleButton _xpToggle;

  private List<Task> _tasks;

  private Task _fishTask;

  private Task _shipsTask;

  private Task _buildingsTask;

  private boolean _testMode;

  private JToggleButton _autoRefreshToggle;

  private OCRB _ocr;

  public static void main(String[] args) {

    try {
      boolean isTestmode = false;
      if (args.length > 0) {
        for (String arg : args) {
          System.err.println(arg);
          if (arg.equalsIgnoreCase("test")) {
            isTestmode = true;
            break;
          }
        }
      }
      MainFrame frame = new MainFrame(isTestmode);
      frame.pack();
      frame.setSize(new Dimension(frame.getSize().width + 8, frame.getSize().height + 8));
      int w = 275;// frame.getSize().width;
      final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      int h = (int) (screenSize.height * 0.9);
      int x = screenSize.width - w;
      int y = (screenSize.height - h) / 2;
      frame.setBounds(x, y, w, h);

      frame.setVisible(true);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("serial")
  private void init() throws AWTException {

    try {

      // _ocr = new OCRB("ocr/digit");
      // _ocr.setErrors(1);

      // LOADING DATA
      _settings = Settings.createSettings("daze.properties");
      if (!_settings.containsKey("strategy")) {
        setDefaultSettings();
      }

      _stats = new Stats();
      _scanner = new ScreenScanner(_settings);
      _scanner.setDebugMode(_testMode);
      _matcher = _scanner.getMatcher();
      _mouse = _scanner.getMouse();

      _tasks = new ArrayList<Task>();

      // SCAN TASK - scanning and fixing game could be something out of tasks
      // list
      // _scanTask = new Task("Scan", 1);
      // _tasks.add(_scanTask);

      // FISHING TASK
      _fishTask = new Task("Fish", 1);
      // FishingProtocol fishingProtocol = new FishingProtocol(_scanner,
      // _mouse);
      // _fishTask.setProtocol(fishingProtocol);
      _tasks.add(_fishTask);

      _stopAllThreads = false;

    } catch (Exception e1) {
      System.err.println("Something went wrong!");
      e1.printStackTrace();
      System.exit(1);
    }

    initLayout();
    loadStats();

    reapplySettings();

    runSettingsListener();

    _mazeRunner = new SmartMazeRunner(_scanner);

  }

  private void setDefaultSettings() {
    // _settings.setProperty("fish", "true");
    // _settings.setProperty("ships", "true");
    // _settings.setProperty("industries", "true");
    // _settings.setProperty("slow", "false");
    // _settings.setProperty("autoSailors", "false");
    // _settings.setProperty("Buildings.SawMill1", "false");
    // _settings.setProperty("Buildings.SawMill2", "true");
    // _settings.setProperty("Buildings.Quarry", "true");
    // _settings.setProperty("Buildings.Foundry", "true");
    // _settings.setProperty("ShipProtocol", "SINGLE");
    // _settings.setProperty("autoSailors.speedProtocol", "SINGLE");
    // _settings.setProperty("autoSailors.defaultProtocol", "DEFAULT");
    // _settings.setProperty("autoSailors.upperThreshold", "1000");
    // _settings.setProperty("autoSailors.lowerThreshold", "600");
    _settings.saveSettingsSorted();
  }

  private void initLayout() {
    if (_testMode)
      APP_TITLE += " TEST";
    setTitle(APP_TITLE);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setAlwaysOnTop(true);

    JPanel rootPanel = new JPanel(new BorderLayout());
    getContentPane().add(rootPanel, BorderLayout.CENTER);

    // CONSOLE
    rootPanel.add(buildConsole(), BorderLayout.CENTER);

    // TOOLBARS
    JToolBar mainToolbar1 = createToolbar1();
    JToolBar mainToolbar2 = createToolbar2();
    // // List<JToolBar> mainToolbars3 = createToolbars3();
    // JToolBar mainToolbar4 = createToolbar4();

    JPanel toolbars = new JPanel(new GridLayout(0, 1));
    toolbars.add(mainToolbar1);
    toolbars.add(mainToolbar2);
    // // for (JToolBar jToolBar : mainToolbars3) {
    // // toolbars.add(jToolBar);
    // // }
    // toolbars.add(mainToolbar4);

    // toolbars.add(createToolbar5());

    Box north = Box.createVerticalBox();
    north.add(toolbars);
    // north.add(createStatsPanel());
    if (_testMode) {

      JToolBar testToolbar = createTestToolbar();
      toolbars.add(testToolbar);
      _findThisTF = new JTextField();
      Box box = Box.createHorizontalBox();
      box.add(_findThisTF);
      JButton findButton = new JButton(new AbstractAction("Find") {

        @Override
        public void actionPerformed(ActionEvent ae) {
          LOGGER.info("scan for " + _findThisTF.getText());
          final String filename = _findThisTF.getText();
          new Thread(new Runnable() {
            public void run() {
              try {

                _scanner.getImageData(filename);
                Pixel p = _scanner.scanOneFast(filename, null, true);
                if (p != null) {
                  LOGGER.info("found it: " + p);
                } else {
                  LOGGER.info(filename + " not found");
                  LOGGER.info("trying with redused threshold");
                  double old = _matcher.getSimilarityThreshold();
                  _matcher.setSimilarityThreshold(0.91d);
                  p = _scanner.scanOne(filename, null, true);
                  if (p != null) {
                    LOGGER.info("found it: " + p);
                  } else {
                    LOGGER.info(filename + " not found");
                  }
                  _matcher.setSimilarityThreshold(old);

                }
              } catch (RobotInterruptedException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              } catch (AWTException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
              }

            }
          }).start();

        }
      });
      box.add(findButton);
      north.add(box);

    }
    _mouseInfoLabel = new JLabel(" ");
    north.add(_mouseInfoLabel);
    rootPanel.add(north, BorderLayout.NORTH);

    final JTextArea shipLog = new JTextArea(5, 10);
    rootPanel.add(new JScrollPane(shipLog), BorderLayout.SOUTH);

    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new MyKeyEventDispatcher());
  }

  private Map<String, JLabel> _labels = new HashMap<String, JLabel>();

  private Component createStatsPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    GridBagConstraints gbc2 = new GridBagConstraints();
    JLabel l;
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc2.gridx = 2;
    gbc2.gridy = 1;

    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.anchor = GridBagConstraints.WEST;
    gbc2.insets = new Insets(2, 4, 2, 2);
    gbc2.anchor = GridBagConstraints.EAST;

    // S
    panel.add(new JLabel("S:"), gbc);
    l = new JLabel(" ");
    _labels.put("S", l);
    panel.add(l, gbc2);

    // C
    gbc.gridy++;
    gbc2.gridy++;
    panel.add(new JLabel("C:"), gbc);
    l = new JLabel(" ");
    _labels.put("C", l);
    panel.add(l, gbc2);

    // G
    gbc.gridy++;
    gbc2.gridy++;
    panel.add(new JLabel("G:"), gbc);
    l = new JLabel(" ");
    _labels.put("G", l);
    panel.add(l, gbc2);

    // RV
    gbc.gridy++;
    gbc2.gridy++;
    panel.add(new JLabel("RV:"), gbc);
    l = new JLabel(" ");
    _labels.put("RV", l);
    panel.add(l, gbc2);

    gbc.insets = new Insets(2, 12, 2, 2);
    gbc.gridx = 3;
    gbc2.gridx = 4;
    gbc.gridy = 0;
    gbc2.gridy = 0;

    // CP
    gbc.gridy++;
    gbc2.gridy++;
    panel.add(new JLabel("CP:"), gbc);
    l = new JLabel(" ");
    _labels.put("CP", l);
    panel.add(l, gbc2);

    // MC
    gbc.gridy++;
    gbc2.gridy++;
    panel.add(new JLabel("MC:"), gbc);
    l = new JLabel(" ");
    _labels.put("MC", l);
    panel.add(l, gbc2);

    // MX
    gbc.gridy++;
    gbc2.gridy++;
    panel.add(new JLabel("MX:"), gbc);
    l = new JLabel(" ");
    _labels.put("MX", l);
    panel.add(l, gbc2);

    // JK
    gbc.gridy++;
    gbc2.gridy++;
    panel.add(new JLabel("JK:"), gbc);
    l = new JLabel(" ");
    _labels.put("JK", l);
    panel.add(l, gbc2);

    gbc.insets = new Insets(2, 12, 2, 2);
    gbc.gridx = 5;
    gbc2.gridx = 6;
    gbc.gridy = 0;
    gbc2.gridy = 0;

    // BS
    gbc.gridy++;
    gbc2.gridy++;
    panel.add(new JLabel("BS:"), gbc);
    l = new JLabel(" ");
    _labels.put("BS", l);
    panel.add(l, gbc2);

    // RB
    gbc.gridy++;
    gbc2.gridy++;
    panel.add(new JLabel("R:"), gbc);
    l = new JLabel(" ");
    _labels.put("R", l);
    panel.add(l, gbc2);

    // NH
    gbc.gridy++;
    gbc2.gridy++;
    panel.add(new JLabel("N:"), gbc);
    l = new JLabel(" ");
    _labels.put("N", l);
    panel.add(l, gbc2);

    // IM
    gbc.gridy++;
    gbc2.gridy++;
    panel.add(new JLabel("IM:"), gbc);
    l = new JLabel(" ");
    _labels.put("IM", l);
    panel.add(l, gbc2);

    // FAKE
    gbc2.gridx++;
    gbc2.gridy++;
    gbc2.weightx = 1.0f;
    gbc2.weighty = 1.0f;
    panel.add(new JLabel(""), gbc2);

    return panel;
  }

  private JToggleButton _pingToggle;

  private Container buildConsole() {
    final JTextArea outputConsole = new JTextArea(8, 14);

    Handler handler = new Handler() {

      @Override
      public void publish(LogRecord record) {
        String text = outputConsole.getText();
        if (text.length() > 3000) {
          int ind = text.indexOf("\n", 2000);
          if (ind <= 0)
            ind = 2000;
          text = text.substring(ind);
          outputConsole.setText(text);
        }
        outputConsole.append(record.getMessage());
        outputConsole.append("\n");
        outputConsole.setCaretPosition(outputConsole.getDocument().getLength());
        // outputConsole.repaint();
      }

      @Override
      public void flush() {
        outputConsole.repaint();
      }

      @Override
      public void close() throws SecurityException {
        // do nothing

      }
    };
    LOGGER.addHandler(handler);

    return new JScrollPane(outputConsole);
  }

  /**
   * 
   * @param click
   * @param attempt
   * @throws RobotInterruptedException
   * @deprecated
   */
  private void recalcPositions(boolean click, int attempt) throws RobotInterruptedException {
    try {
      if (!_scanner.isOptimized()) {
        scan();
      }

      if (_scanner.isOptimized()) {
        _mouse.click(_scanner.getSafePoint());
        _mouse.delay(200);
        _mouse.mouseMove(_scanner.getParkingPoint());

        _scanner.checkAndAdjustRock();
      }
      Pixel r = _scanner.getRock();
      if (r != null) {

        LOGGER.info("Recalc positions... ");
        for (Task task : _tasks) {
          task.update();
        }
      } else {
        LOGGER.info("CAN'T FIND THE ROCK!!!");
        handlePopups(false);
        if (attempt <= 2)
          recalcPositions(false, ++attempt);
        else
          r = null; // reset the hell
      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  private void record() {
    try {
      LOGGER.info("Recording the mouse movement (for now)");

      captureDialog = new CaptureDialog();
      if (_scanner.isOptimized()) {
        captureDialog.setBounds(_scanner.getTopLeft().x, _scanner.getTopLeft().y, _scanner.getGameWidth(),
            _scanner.getGameHeight());
      } else {
        captureDialog.setBounds(0, 0, 1679, 1009);
      }
      captureDialog.setVisible(true);
      try {
        while (true) {
          Point loc = MouseInfo.getPointerInfo().getLocation();
          // LOGGER.info("location: " + loc.x + ", " + loc.y);
          _mouseInfoLabel.setText("location: " + loc.x + ", " + loc.y);
          _mouse.delay(250, false);

        }
      } catch (RobotInterruptedException e) {
        LOGGER.info("interrupted");
      }
    } catch (Exception e1) {
      LOGGER.log(Level.WARNING, e1.getMessage());
      e1.printStackTrace();
    }

  }

  private boolean isRunning(String threadName) {
    boolean isRunning = false;
    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
    for (Iterator<Thread> it = threadSet.iterator(); it.hasNext();) {
      Thread thread = it.next();
      if (thread.getName().equals(threadName)) {
        isRunning = true;
        break;
      }
    }
    return isRunning;
  }

  private final class StatsListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals("TODO")) {
        _stats.register("TODO");
      }

    }
  }

  private final class MyKeyEventDispatcher implements KeyEventDispatcher {

    public boolean dispatchKeyEvent(KeyEvent e) {
      if (!e.isConsumed()) {
        // LOGGER.info("pressed " + e.getKeyCode());
        // e.consume();
        if (e.getKeyCode() == 119 || e.getKeyCode() == 65) {// F8 or a
          // LOGGER.info("pressed " + e.getKeyCode());
          if (!isRunning("HMM")) {
            Thread t = new Thread(new Runnable() {
              public void run() {
                // //addNewBuilding();
                scanDiggyFromHere();
              }
            }, "HMM");
            t.start();
          }
        }

        if (e.getKeyCode() == 88) {// X
          // massClick(1, (int) (_scanner.getXOffset() * 1.6), true);
        }
        if (e.getKeyCode() == 67) {// C
          // massClick(1, (int) (_scanner.getXOffset() * 3), true);
        }

        if (e.getKeyCode() == 65 || e.getKeyCode() == 18) {// A or Alt
          // massClick(2, true);
        }
        if (e.getKeyCode() == 83) {// S
          // massClick(2, (int) (_scanner.getXOffset() * 1.6), true);

        }
        if (e.getKeyCode() == 68) {// D
          // massClick(2, (int) (_scanner.getXOffset() * 3), true);
        }

        if (e.getKeyCode() == 81 || e.getKeyCode() == 32) {// Q or Space
          // massClick(4, true);
        }
        if (e.getKeyCode() == 87) {// W
          // massClick(4, (int) (_scanner.getXOffset() * 1.6), true);
        }
        if (e.getKeyCode() == 69) {// E
          // massClick(4, (int) (_scanner.getXOffset() * 3), true);
        }

        if (e.getKeyCode() == 77) {// M for MAILS
          // massClick(1, (int) (_scanner.getXOffset() / 2), true);
        }

        // LOGGER.info("key pressed: " + e.getExtendedKeyCode() + " >>> " +
        // e.getKeyCode());
        e.consume();
      }
      return false;
    }

  }

  @SuppressWarnings("serial")
  private JToolBar createToolbar1() {
    JToolBar mainToolbar1 = new JToolBar();
    mainToolbar1.setFloatable(false);

    // SCAN
    {
      AbstractAction action = new AbstractAction("Scan") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                scan();
              } catch (RobotInterruptedException e) {
                e.printStackTrace();
              }
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }
    // RUN MAGIC
    {
      AbstractAction action = new AbstractAction("Run") {
        public void actionPerformed(ActionEvent e) {
          runMagic();
        }

      };
      mainToolbar1.add(action);
    }
    // STOP MAGIC
    {
      AbstractAction action = new AbstractAction("Stop") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {

            @Override
            public void run() {
              LOGGER.info("Stopping BB Gun");
              _stopAllThreads = true;
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }

    // RECORD
    {
      AbstractAction action = new AbstractAction("R") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              record();
            }
          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }

    // RESET BUILDINGS
    {
      AbstractAction action = new AbstractAction("Reset") {
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              // //clearBuildings();
              // try {
              // if (!_scanner.isOptimized()) {
              // scan();
              // }
              //
              // if (_scanner.isOptimized()) {
              // _mouse.savePosition();
              // locateIndustries();
              // _mouse.restorePosition();
              // } else {
              // LOGGER.info("I need to know where the game is!");
              // }
              // } catch (RobotInterruptedException e) {
              // LOGGER.log(Level.WARNING, e.getMessage());
              // e.printStackTrace();
              // } catch (IOException e) {
              // LOGGER.log(Level.WARNING, e.getMessage());
              // e.printStackTrace();
              // } catch (AWTException e) {
              // LOGGER.log(Level.WARNING, e.getMessage());
              // e.printStackTrace();
              // }
            }

          });

          myThread.start();
        }
      };
      mainToolbar1.add(action);
    }

    return mainToolbar1;
  }

  @SuppressWarnings("serial")
  private JToolBar createToolbar2() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    // SCAN
    {
      // SHIPS
      _fishToggle = new JToggleButton("Fish");
      // _fishToggle.setSelected(true);
      _fishToggle.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          boolean b = e.getStateChange() == ItemEvent.SELECTED;
          _fishTask.setEnabled(b);
          _settings.setProperty("fish", "" + b);
          _settings.saveSettingsSorted();
        }
      });
      toolbar.add(_fishToggle);

      // SHIPS
      _shipsToggle = new JToggleButton("Ships");
      // _shipsToggle.setSelected(true);
      _shipsToggle.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          boolean b = e.getStateChange() == ItemEvent.SELECTED;
          _shipsTask.setEnabled(b);
          _settings.setProperty("ships", "" + b);
          _settings.saveSettingsSorted();
        }
      });
      toolbar.add(_shipsToggle);

      // BUILDINGS
      _industriesToggle = new JToggleButton("Industries");
      // _industriesToggle.setSelected(true);
      _industriesToggle.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          boolean b = e.getStateChange() == ItemEvent.SELECTED;
          _buildingsTask.setEnabled(b);
          _settings.setProperty("industries", "" + b);
          _settings.saveSettingsSorted();

        }
      });
      toolbar.add(_industriesToggle);

      _autoRefreshToggle = new JToggleButton("AR");
      _autoRefreshToggle.addItemListener(new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent e) {
          boolean b = e.getStateChange() == ItemEvent.SELECTED;
          LOGGER.info("Auto Refresh mode: " + (b ? "on" : "off"));
          _settings.setProperty("autoRefresh", "" + b);
          _settings.saveSettingsSorted();
        }
      });
      // _slowToggle.setSelected(false);
      toolbar.add(_autoRefreshToggle);

      _pingToggle = new JToggleButton("Ping");
      // _autoSailorsToggle.setSelected(false);
      _pingToggle.addItemListener(new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent e) {
          boolean b = e.getStateChange() == ItemEvent.SELECTED;
          LOGGER.info("Ping: " + (b ? "on" : "off"));
          _settings.setProperty("ping", "" + b);
          _settings.saveSettingsSorted();

        }
      });

      toolbar.add(_pingToggle);

      // _xpToggle = new JToggleButton("XP");
      // _xpToggle.setSelected(_mapManager.getMarketStrategy().equals("XP"));
      // _xpToggle.addItemListener(new ItemListener() {
      //
      // @Override
      // public void itemStateChanged(ItemEvent e) {
      // boolean b = e.getStateChange() == ItemEvent.SELECTED;
      // String strategy = b ? "XP" : "COINS";
      // LOGGER.info("MARKET STRATEGY: " + strategy);
      // _mapManager.setMarketStrategy(strategy);
      // }
      // });
      // toolbar.add(_xpToggle);

    }
    return toolbar;
  }

  @SuppressWarnings("serial")
  private List<JToolBar> createToolbars3() {
    List<JToolBar> toolbars = new ArrayList<>();
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    // DESTINATIONS GO HERE
    ButtonGroup bg = new ButtonGroup();

    JToggleButton toggle;

    // // COCOA 1
    // toggle = new JToggleButton("Cocoa 1.0");
    // toggle.addItemListener(new ItemListener() {
    //
    // @Override
    // public void itemStateChanged(ItemEvent e) {
    // LOGGER.info("Cocoa Protocol 1.0: ");
    // LOGGER.info("Send all ships to Cocoa plant, then all to market");
    // LOGGER.info("Time: 2h 30m ");
    // _shipsTask.setProtocol(_cocoaProtocol1);
    // _shipsTask.getProtocol().update();
    // }
    // });
    //
    // bg.add(toggle);
    // toolbar.add(toggle);

    // // COCOA 2
    // toggle = new JToggleButton("Cocoa 2.0");
    // JToggleButton selected = toggle;
    // toggle.addItemListener(new ItemListener() {
    //
    // @Override
    // public void itemStateChanged(ItemEvent e) {
    // LOGGER.info("Cocoa Protocol 2.0: ");
    // LOGGER.info("Send half ships to Cocoa plant");
    // LOGGER.info("One specific ship sells cocoa.");
    // LOGGER.info("The rest go to Gulf.");
    // LOGGER.info("Time: 2h");
    // _shipsTask.setProtocol(_cocoaProtocol2);
    // _shipsTask.getProtocol().update();
    // }
    // });
    //
    // bg.add(toggle);
    // toolbar.add(toggle);

    // // MANUAL SHIP PROTOCOL
    // toolbars.add(toolbar);
    // toolbar = new JToolBar();
    // toolbar.setFloatable(false);
    //
    // int itemsPerRow = 3;
    // int n = 0;
    // for (final Destination destination : _mapManager.getDestinations()) {
    //
    // toggle = new JToggleButton(destination.getName());
    // toggle.addItemListener(new ItemListener() {
    //
    // @Override
    // public void itemStateChanged(ItemEvent e) {
    // LOGGER.info("Simple protocol: ");
    // LOGGER.info("Send all ships to: " + destination.getName());
    // LOGGER.info("Time: " + destination.getTime());// TODO format time
    // _manualShipsProtocol.setDestination(destination);
    // _shipsTask.setProtocol(_manualShipsProtocol);
    // _shipsTask.getProtocol().update();
    // }
    // });
    // bg.add(toggle);
    // // toggle.setSelected(destination.getName().equals("Coastline"));
    //
    // n++;
    // if (n > itemsPerRow) {
    // toolbars.add(toolbar);
    // toolbar = new JToolBar();
    // toolbar.setFloatable(false);
    // n = 0;
    // }
    // toolbar.add(toggle);
    //
    // }
    //
    // selected.setSelected(true);
    return toolbars;
  }

  private JToolBar createToolbar5() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    // Temp bar for custom protocol
    return toolbar;
  }

  @SuppressWarnings("serial")
  private JToolBar createTestToolbar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    {
      Action action = new AbstractAction("Test") {

        @Override
        public void actionPerformed(ActionEvent e) {
          Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
              LOGGER.info("Testing...");
              if (!_scanner.isOptimized()) {
                try {
                  scan();
                } catch (RobotInterruptedException e) {
                  e.printStackTrace();
                }
              }

              if (_scanner.isOptimized()) {
                // DO THE JOB
                // ////test();
              } else {
                LOGGER.info("I need to know where the game is!");
              }
            }
          });

          myThread.start();
        }
      };

      toolbar.add(action);
    }
    return toolbar;
  }

  private void setupLogger() {
    try {
      MyLogger.setup();
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Problems with creating the log files");
    }
  }

  @SuppressWarnings("serial")
  private static class CaptureDialog extends JFrame {
    Point _startPoint;
    Point _endPoint;
    Rectangle _rect;
    boolean inDrag;

    public CaptureDialog() {
      super("hmm");
      setUndecorated(true);
      getRootPane().setOpaque(false);
      getContentPane().setBackground(new Color(0, 0, 0, 0.05f));
      setBackground(new Color(0, 0, 0, 0.05f));

      _startPoint = null;
      _endPoint = null;
      inDrag = false;

      // events

      addMouseListener(new MouseAdapter() {

        @Override
        public void mousePressed(MouseEvent e) {
          inDrag = true;

        }

        @Override
        public void mouseClicked(MouseEvent e) {

          if (e.getButton() == MouseEvent.BUTTON1) {
            if (_startPoint == null) {
              LOGGER.info("clicked once " + e.getButton() + " (" + e.getX() + ", " + e.getY() + ")");
              _startPoint = e.getPoint();
              repaint();
            } else {
              _endPoint = e.getPoint();
              // LOGGER.info("clicked twice " + e.getButton() +
              // " (" + e.getX() + ", " + e.getY() + ")");
              setVisible(false);
              LOGGER.info("AREA: " + _rect);
            }
          } else if (e.getButton() == MouseEvent.BUTTON3) {
            _startPoint = null;
            _endPoint = null;
            repaint();
          }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
          // LOGGER.info("REL:"+e);

          if (inDrag && _endPoint != null && _startPoint != null) {
            // LOGGER.info("end of drag " + e.getButton() + " (" +
            // e.getX() + ", " + e.getY() + ")");
            inDrag = false;
            setVisible(false);
            LOGGER.info("AREA: " + _rect);
            // HMM
            dispose();
          }

        }

      });

      addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
          // LOGGER.info("move " + e.getPoint());
          _endPoint = e.getPoint();
          repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
          if (_startPoint == null) {
            _startPoint = e.getPoint();
          }
          _endPoint = e.getPoint();
          repaint();
          // LOGGER.info("DRAG:" + e);
        }

      });

    }

    @Override
    public void paint(Graphics g) {
      super.paint(g);
      if (_startPoint != null && _endPoint != null) {
        g.setColor(Color.RED);
        int x = Math.min(_startPoint.x, _endPoint.x);
        int y = Math.min(_startPoint.y, _endPoint.y);
        int w = Math.abs(_startPoint.x - _endPoint.x);
        int h = Math.abs(_startPoint.y - _endPoint.y);
        _rect = new Rectangle(x, y, w, h);

        g.drawRect(x, y, w, h);

        // g.setColor(Color.GRAY);
        // g.drawString("[" + w + ", " + h + "]", w / 2 - 13, h / 2 -
        // 3);
        g.setColor(Color.RED);
        g.drawString(x + ", " + y + ", [" + w + ", " + h + "]", x + 3, y + 13);
      }
    }
  }

  private void scan() throws RobotInterruptedException {
    try {
      _mouse.savePosition();
      _scanner.reset();
      LOGGER.info("Scanning...");
      setTitle(APP_TITLE + " ...");
      boolean found = _scanner.locateGameArea(false);
      if (found) {
        // _scanner.checkAndAdjustRock();
        // _mapManager.update();
        // _buildingManager.update();

        LOGGER.info("Coordinates: " + _scanner.getTopLeft() + " - " + _scanner.getBottomRight());

        _scanner.zoomOut();

        LOGGER.info("GAME FOUND! DAZE READY!");
        setTitle(APP_TITLE + " READY");

        loadStats();
        _mouse.restorePosition();
      } else {
        LOGGER.info("CAN'T FIND THE GAME!");
        setTitle(APP_TITLE);
      }
    } catch (Exception e1) {
      LOGGER.log(Level.WARNING, e1.getMessage());
      e1.printStackTrace();
    }

  }

  private void loadStats() {
    // try {
    //
    // Iterator<String> i = _labels.keySet().iterator();
    // while (i.hasNext()) {
    // String key = (String) i.next();
    // _labels.get(key).setText("" + 0);
    // }
    //
    // List<DispatchEntry> des = new JsonStorage().loadDispatchEntries();
    // for (DispatchEntry de : des) {
    // JLabel l = _labels.get(de.getDest());
    // if (l != null) {
    // l.setText("" + (Integer.parseInt(l.getText()) + de.getTimes()));
    // }
    //
    // }
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
  }

  private Rectangle generateMiniArea(Pixel p) {
    return new Rectangle(p.x - 2 - 18, p.y - 50 + 35, 44, 60);
  }

  public MainFrame(boolean isTestmode) throws HeadlessException, AWTException {
    super();

    _testMode = isTestmode;
    setupLogger();
    init();
  }

  private void doMagic() {
    assert _scanner.isOptimized();
    setTitle(APP_TITLE + " RUNNING");
    _stopAllThreads = false;

    try {
      long start = System.currentTimeMillis();
      // initial recalc
      // recalcPositions(false, 2);
      for (Task task : _tasks) {
        if (task.isEnabled())
          task.update();
      }

      _mouse.saveCurrentPosition();
      long fstart = System.currentTimeMillis();
      do {
        long mandatoryRefresh = _settings.getInt("autoRefresh.mandatoryRefresh", 45) * 60 * 1000;
        long now = System.currentTimeMillis();
        _mouse.checkUserMovement();
        // 1. SCAN
        handlePopups(false);

        // REFRESH
        LOGGER.info("refresh ? " + _autoRefreshToggle.isSelected() + " - " + mandatoryRefresh + " < " + (now - fstart));
        if (_autoRefreshToggle.isSelected() && mandatoryRefresh > 0 && now - fstart >= mandatoryRefresh) {
          LOGGER.info("refresh time...");
          try {
            refresh(false);
          } catch (AWTException e) {
            LOGGER.info("FAILED TO refresh: " + e.getMessage());
          } catch (IOException e) {
            LOGGER.info("FAILED TO refresh: " + e.getMessage());
          }
          fstart = System.currentTimeMillis();
        }

        _mouse.checkUserMovement();
        if (_pingToggle.isSelected()) {
          ping();
        }

        _mouse.checkUserMovement();

        // 2. DO TASKS
        // long now = System.currentTimeMillis();
        // if (now - start > 11*60000) {
        for (Task task : _tasks) {
          if (task.isEnabled()) {
            try {
              _mouse.checkUserMovement();
              task.preExecute();
              _mouse.checkUserMovement();
              task.execute();
            } catch (AWTException e) {
              LOGGER.info("FAILED TO execute task: " + task.getName());
            } catch (IOException e) {
              LOGGER.info("FAILED TO execute task: " + task.getName());
            }
          }
        }
        // start = System.currentTimeMillis();
        // }

        _mouse.mouseMove(_scanner.getParkingPoint());

        _mouse.delay(200);

      } while (!_stopAllThreads);

    } catch (RobotInterruptedException e) {
      LOGGER.info("interrupted");
      setTitle(APP_TITLE);
      // e.printStackTrace();
    }
  }

  private void refresh(boolean bookmark) throws AWTException, IOException, RobotInterruptedException {
    deleteOlder("refresh", 5);
    LOGGER.info("Time to refresh...");
    _scanner.captureGameArea("refresh ");
    Pixel p;
    if (!bookmark) {
      if (_scanner.isOptimized()) {
        p = _scanner.getBottomRight();
        p.y += 4;
        p.x -= 4;
      } else {
        p = new Pixel(0, 510);
      }
      _mouse.click(p.x, p.y);
      try {
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_F5);
        robot.keyRelease(KeyEvent.VK_F5);
      } catch (AWTException e) {
      }
      try {
        Thread.sleep(15000);
      } catch (InterruptedException e) {
      }
      _scanner.reset();

      boolean done = false;
      for (int i = 0; i < 17 && !done; i++) {
        LOGGER.info("after refresh recovery try " + (i + 1));
        // LOCATE THE GAME
        if (_scanner.locateGameArea(false)) {
          LOGGER.info("Game located successfully!");
          done = true;
        } else {
          processRequests();
        }
        if (i > 8) {
          captureScreen("refresh trouble ");
        }
      }
      if (done) {
        // runMagic();
        captureScreen("refresh done ");
      } else {
        // blah
        // try bookmark
      }

      // not sure why shipsTasks gets off after refresh
      reapplySettings();

    } else {
      // try {
      // p = _scanner.generateImageData("tsFavicon2.bmp", 8, 7).findImage(new
      // Rectangle(0, 30, 400, 200));
      // _mouse.click(p.x, p.y);
      // } catch (IOException e) {
      // }
    }
  }

  private Long _lastPing = System.currentTimeMillis();

  private void ping() {
    if (System.currentTimeMillis() - _lastPing > _settings.getInt("ping.time", 120) * 1000) {
      captureScreen(null);
      _lastPing = System.currentTimeMillis();
    }

  }

  private Long _speedTime = null;

  private SmartMazeRunner _mazeRunner;

  private void setProtocol(String shipProtocolName) {
    // _shipProtocolManagerUI.setShipProtocol(shipProtocolName);
  }

  private void handlePopups(boolean fast) throws RobotInterruptedException {
    try {
      LOGGER.info("Popups...");
      boolean found = false;
      Pixel p = null;
      if (_scanner.isOptimized()) {
        _mouse.click(_scanner.getSafePoint());
        _mouse.delay(300);
      }
      found = _scanner.scanOneFast("anchor.bmp", null, true) != null;

      if (found)
        return;
      // reload
      long start = System.currentTimeMillis();
      long now, t1 = 0, t2 = 0, t3, t4;
      if (!fast) {
        Rectangle area = _scanner.generateWindowedArea(412, 550);
        p = _scanner.scanOneFast("reload.bmp", area, false);
        now = System.currentTimeMillis();

        t1 = now - start;
        t2 = now;
        if (p == null) {
          p = _scanner.scanOneFast("reload2.bmp", area, false);
          now = System.currentTimeMillis();
          t2 = now - t2;
        }

        found = p != null;
        if (found) {
          // check is this 'logged twice' message
          Pixel pp = _scanner.scanOne("accountLoggedTwice.bmp", area, false);
          if (pp != null) {
            LOGGER.info("Logged somewhere else. I'm done here!");
            _stopAllThreads = true;
            throw new RobotInterruptedException();
          } else {
            _mouse.click(p);
          }

          LOGGER.info("Game crashed. Reloading...");
          _mouse.delay(15000);
          boolean recovered = false;
          for (int i = 0; i < 15; i++) {
            scan();
            if (_scanner.isOptimized()) {
              recovered = true;
              break;
            }
            _mouse.delay(3000);
          }
          if (!recovered) {
            LOGGER.info("===========================");
            LOGGER.info("Game failed to recover!!!");
            LOGGER.info("===========================");
            return;
          }
        }
      }

      t3 = now = System.currentTimeMillis();
      found = _scanner.scanOneFast("buildings/x.bmp", null, true) != null;
      now = System.currentTimeMillis();
      t3 = now - t3;
      _mouse.delay(150);
      t4 = now = System.currentTimeMillis();
      found = _scanner.scanOneFast("anchor.bmp", null, true) != null;
      now = System.currentTimeMillis();
      t4 = now - t4;
      if (found)
        _mouse.delay(450);
      now = System.currentTimeMillis();
      LOGGER.info("[" + t1 + ",  " + t2 + ",  " + t3 + ",  " + t4 + "], TOTAL: " + (now - start) + " - " + found);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AWTException e) {
      e.printStackTrace();
    }

  }

  private void reapplySettings() {

    // toggles

    boolean fish = "true".equalsIgnoreCase(_settings.getProperty("fish"));
    if (fish != _fishToggle.isSelected()) {
      _fishToggle.setSelected(fish);
    }

    boolean ships = "true".equalsIgnoreCase(_settings.getProperty("ships"));
    if (ships != _shipsToggle.isSelected()) {
      _shipsToggle.setSelected(ships);
    }

    boolean industries = "true".equalsIgnoreCase(_settings.getProperty("industries"));
    if (industries != _industriesToggle.isSelected()) {
      _industriesToggle.setSelected(industries);
    }

    boolean slow = "true".equalsIgnoreCase(_settings.getProperty("autoRefresh"));
    if (slow != _autoRefreshToggle.isSelected()) {
      _autoRefreshToggle.setSelected(slow);
    }

    boolean ping = "true".equalsIgnoreCase(_settings.getProperty("ping"));
    if (ping != _pingToggle.isSelected()) {
      _pingToggle.setSelected(ping);
    }

  }

  private void stopMagic() {
    _stopAllThreads = true;
    LOGGER.info("Stopping...");
    int tries = 10;
    boolean stillRunning = true;
    for (int i = 0; i < tries && stillRunning; ++i) {
      stillRunning = isRunning("MAGIC");
      if (stillRunning) {
        LOGGER.info("Magic still working...");
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
      } else {
        LOGGER.info("INSOMNIA STOPPED");
        setTitle(APP_TITLE);
      }
    }
    _stopAllThreads = false;
  }

  private void processRequests() {
    Service service = new Service();

    String[] requests = service.getActiveRequests();
    for (String r : requests) {

      if (r.startsWith("stop")) {
        service.inProgress(r);
        stopMagic();
        captureScreen(null);

      } else if (r.startsWith("run") || r.startsWith("start")) {
        service.inProgress(r);
        stopMagic();
        runMagic();
        captureScreen(null);

      } else if (r.startsWith("refresh")) {
        service.inProgress(r);
        try {
          stopMagic();
          refresh(false);
        } catch (AWTException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        } catch (RobotInterruptedException e) {
          e.printStackTrace();
        }

      } else if (r.startsWith("ping") || r.startsWith("p")) {
        service.inProgress(r);
        LOGGER.info("Ping...");
        captureScreen(null);
        service.done(r);
      }
    }

    // service.purgeOld(1000 * 60 * 60);// 1 hour old
  }

  private void runSettingsListener() {
    Thread requestsThread = new Thread(new Runnable() {
      public void run() {
        // new Service().purgeAll();
        boolean stop = false;
        do {
          LOGGER.info("......");
          try {
            _settings.loadSettings();
            reapplySettings();
            processRequests();
          } catch (Throwable t) {
            // hmm
            t.printStackTrace();
          }
          try {
            Thread.sleep(20000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

        } while (!stop);
      }

    }, "REQUESTS");

    requestsThread.start();

  }

  private void deleteOlder(String prefix, int amountFiles) {
    File f = new File(".");
    File[] files = f.listFiles();
    List<File> targetFiles = new ArrayList<File>(6);
    int cnt = 0;
    for (File file : files) {
      if (!file.isDirectory() && file.getName().startsWith(prefix)) {
        targetFiles.add(file);
        cnt++;
      }
    }

    if (cnt > amountFiles) {
      // delete some files
      Collections.sort(targetFiles, new Comparator<File>() {
        public int compare(File o1, File o2) {
          if (o1.lastModified() > o2.lastModified())
            return 1;
          else if (o1.lastModified() < o2.lastModified())
            return -1;
          return 0;
        };
      });

      int c = cnt - 5;
      for (int i = 0; i < c; i++) {
        File fd = targetFiles.get(i);
        fd.delete();
      }
    }
  }

  private void captureScreen(String filename) {
    if (filename == null)
      filename = "ping ";
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    writeImage(new Rectangle(0, 0, screenSize.width, screenSize.height),
        filename + DateUtils.formatDateForFile(System.currentTimeMillis()) + ".jpg");
    deleteOlder("ping", 8);
  }

  public void writeImage(Rectangle rect, String filename) {
    try {
      writeImage(new Robot().createScreenCapture(rect), filename);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  public void writeImage(BufferedImage image, String filename) {

    try {
      int ind = filename.lastIndexOf("/");
      if (ind > 0) {
        String path = filename.substring(0, ind);
        File f = new File(path);
        f.mkdirs();
      }
      File file = new File(filename);
      MyImageIO.write(image, filename.substring(filename.length() - 3).toUpperCase(), file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void runMagic() {
    Thread myThread = new Thread(new Runnable() {
      @Override
      public void run() {
        LOGGER.info("Let's get rolling...");
        if (!_scanner.isOptimized()) {
          try {
            scan();
          } catch (RobotInterruptedException e) {
            e.printStackTrace();
          }
        }

        if (_scanner.isOptimized()) {
          // DO THE JOB
          doMagic();
        } else {
          LOGGER.info("I need to know where the game is!");
        }
      }
    }, "MAGIC");

    myThread.start();
  }

  private void scanDiggyFromHere() {
    _mazeRunner.doSomething();
  }
}
