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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.ColorFiltering;

import com.horowitz.commons.DateUtils;
import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.MyImageIO;
import com.horowitz.commons.MyLogger;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.commons.Service;
import com.horowitz.commons.Settings;
import com.horowitz.commons.TemplateMatcher;
import com.horowitz.daze.map.Agenda;
import com.horowitz.daze.map.AgendaEntry;
import com.horowitz.daze.map.MapManager;
import com.horowitz.daze.map.PlaceUnreachableException;
import com.horowitz.daze.ocr.OCREnergy;
import com.horowitz.ocr.OCRB;

public class MainFrame extends JFrame {

  private static final long serialVersionUID = 3458306923208534910L;

  private final static Logger LOGGER = Logger.getLogger("MAIN");

  private static String APP_TITLE = "Daze v0.35a";

  private Settings _settings;
  private Stats _stats;
  private MouseRobot _mouse;
  private ScreenScanner _scanner;

  private JLabel _mouseInfoLabel;

  private CaptureDialog captureDialog;

  private boolean _stopAllThreads;

  private JTextField _findThisTF;

  private TemplateMatcher _matcher;

  private JToggleButton _popupsToggle;
  private JToggleButton _gatesToggle;

  private JToggleButton _ping2Toggle;
  // private JToggleButton _xpToggle;

  private List<Task> _tasks;

  private Task _mazeTask;

  private Task _shipsTask;

  private Task _buildingsTask;

  private boolean _testMode;

  private JToggleButton _autoRefreshToggle;

  private OCRB _ocr;

  protected long _lastTimeActivity = 0;

  private MapManager mapManager;

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
      int w = 450;// frame.getSize().width;
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
      if (!_settings.containsKey("popups")) {
        setDefaultSettings();
      }

      _stats = new Stats();
      _scanner = new ScreenScanner(_settings);
      ocrEnergy = new OCREnergy(_scanner.getComparator());
      _scanner.setDebugMode(_testMode);
      _matcher = _scanner.getMatcher();
      _mouse = _scanner.getMouse();

      _mazeRunner = new GraphMazeRunner(_scanner);

      _tasks = new ArrayList<Task>();

      // FISHING TASK
      _mazeTask = new Task("Maze Runner", 1);
      _mazeProtocol = new MazeProtocol(_scanner, _mouse, _mazeRunner);
      _mazeTask.setProtocol(_mazeProtocol);
      _tasks.add(_mazeTask);

      _stopAllThreads = false;

    } catch (Exception e1) {
      System.err.println("Something went wrong!");
      e1.printStackTrace();
      System.exit(1);
    }

    mapManager = new MapManager(_scanner);
    mapManager.loadMaps();

    initLayout();

    // WIRING listeners
    _popupsToggle.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        boolean b = e.getStateChange() == ItemEvent.SELECTED;
        _mazeRunner.setPopups(b);
        _settings.setProperty("popups", "" + b);
        _settings.saveSettingsSorted();
      }
    });
    _gatesToggle.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        boolean b = e.getStateChange() == ItemEvent.SELECTED;
        _mazeRunner.setGates(b);
        _settings.setProperty("gates", "" + b);
        _settings.saveSettingsSorted();
      }
    });
    _slowToggle.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        boolean b = e.getStateChange() == ItemEvent.SELECTED;
        _mazeRunner.setSlow(b);
        _settings.setProperty("slow", "" + b);
        _settings.saveSettingsSorted();
      }
    });

    _mazeRunner.addPropertyChangeListener(_mazeCanvas.createPropertyChangeListener());

    _timeTF.getDocument().addDocumentListener(new DocumentListener() {

      @Override
      public void removeUpdate(DocumentEvent e) {
        int pauseTime = 5;
        try {
          pauseTime = Integer.parseInt(_timeTF.getText());
        } catch (NumberFormatException e1) {
          LOGGER.info("Not a number! Set time to 5sec...");
          pauseTime = 5;
        }
        _mazeRunner.setPauseTime(pauseTime);
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        int pauseTime = 5;
        try {
          pauseTime = Integer.parseInt(_timeTF.getText());
        } catch (NumberFormatException e1) {
          LOGGER.info("Not a number! Set time to 5sec...");
          pauseTime = 5;
        }
        _mazeRunner.setPauseTime(pauseTime);
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
      }
    });

    _mazeRunner.addPropertyChangeListener(new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        long now = System.currentTimeMillis();
        DateFormat sdf = DateFormat.getDateTimeInstance();
        String ds = sdf.format(Calendar.getInstance().getTime());
        _labels.get("LTA").setText(ds);

        _lastTimeActivity = now;
      }
    });

    _mazeRunner.addPropertyChangeListener("GREEN_CLICKED", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        long now = System.currentTimeMillis();
        DateFormat sdf = DateFormat.getDateTimeInstance();
        String ds = sdf.format(Calendar.getInstance().getTime());
        _labels.get("FS").setText(ds);

        _fstart = now;
      }
    });
    _mazeRunner.addPropertyChangeListener("CAPTURE", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (_ping2Toggle.isSelected())
          captureArea(_scanner.getPing2Area(), "ping/diggy ");
      }
    });

    // ///////////////////////

    loadStats();

    reapplySettings();

    runSettingsListener();

  }

  private void setDefaultSettings() {
    _settings.setProperty("popups", "false");
    _settings.setProperty("gates", "false");
    _settings.setProperty("slow", "false");
    _settings.setProperty("ping", "false");
    _settings.setProperty("ping2", "false");
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
    north.add(createStatsPanel());
    north.add(createAgendaManagerPanel());
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
    _mazeCanvas = new MazeCanvas();
    _mazeCanvas.setMinimumSize(new Dimension(400, 300));
    _mazeCanvas.setPreferredSize(new Dimension(400, 300));

    rootPanel.add(new JScrollPane(_mazeCanvas), BorderLayout.SOUTH);

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

    // T
    panel.add(new JLabel("FS:"), gbc);
    l = new JLabel(" ");
    _labels.put("FS", l);
    panel.add(l, gbc2);

    // IA
    gbc.gridy++;
    gbc2.gridy++;
    panel.add(new JLabel("Inactivities:"), gbc);
    l = new JLabel(" ");
    _labels.put("IA", l);
    panel.add(l, gbc2);

    // MR
    gbc.gridy++;
    gbc2.gridy++;
    panel.add(new JLabel("Mandatory Refreshes:"), gbc);
    l = new JLabel(" ");
    _labels.put("MR", l);
    panel.add(l, gbc2);

    // LTA
    gbc.gridy++;
    gbc2.gridy++;
    panel.add(new JLabel("Last Time Activity:"), gbc);
    l = new JLabel(" ");
    _labels.put("LTA", l);
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

  private JTextField _timeTF;

  private JTextField _regenTF;

  private JTextField _tileTF;

  private JToggleButton _slowToggle;

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
                scanDiggyFromHere(true);
              }
            }, "HMM");
            t.start();
          }
        }

        if (e.getKeyCode() == 120) {// F9
          if (!isRunning("HMM")) {
            Thread t = new Thread(new Runnable() {
              public void run() {
                // //addNewBuilding();
                scanDiggyFromHere(false);
              }
            }, "HMM");
            t.start();
          }
        }

        if (e.getKeyCode() == 121) {// F10
          if (!isRunning("HMM")) {
            Thread t = new Thread(new Runnable() {
              public void run() {
                _mazeRunner.testPosition();
              }
            }, "HMM");
            t.start();
          }

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
        // e.consume();
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
    // TEST GO TO MAP
    {
      AbstractAction action = new AbstractAction("Do Agenda") {
        public void actionPerformed(ActionEvent e) {
          doAgenda();
        }

      };
      mainToolbar1.add(action);
    }

    // TEST scan energy
    {
      AbstractAction action = new AbstractAction("SE") {
        public void actionPerformed(ActionEvent e) {
          Thread t = new Thread(new Runnable() {
            public void run() {
              try {
                scanEnergy();
              } catch (AWTException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }

            }
          });
          t.start();
        }

      };
      mainToolbar1.add(action);
    }
    // // STOP MAGIC
    // {
    // AbstractAction action = new AbstractAction("Stop") {
    // public void actionPerformed(ActionEvent e) {
    // Thread myThread = new Thread(new Runnable() {
    //
    // @Override
    // public void run() {
    // LOGGER.info("Stopping BB Gun");
    // _stopAllThreads = true;
    // }
    // });
    //
    // myThread.start();
    // }
    // };
    // mainToolbar1.add(action);
    // }

    {
      _regenTF = new JTextField("2335");
      mainToolbar1.add(_regenTF);
      _regenTF.getDocument().addDocumentListener(new DocumentListener() {

        @Override
        public void removeUpdate(DocumentEvent e) {
          recalcTime();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
          recalcTime();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }
      });
    }
    {
      _tileTF = new JTextField("100");
      mainToolbar1.add(_tileTF);
      _tileTF.getDocument().addDocumentListener(new DocumentListener() {

        @Override
        public void removeUpdate(DocumentEvent e) {
          recalcTime();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
          recalcTime();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }
      });
    }
    {
      _timeTF = new JTextField("5");
      mainToolbar1.add(_timeTF);
    }

    return mainToolbar1;
  }

  protected void recalcTime() {
    String regenS = _regenTF.getText();
    String tileS = _tileTF.getText();
    try {
      int regen = Integer.parseInt(regenS);
      int tilePrice = Integer.parseInt(tileS);
      double time = 3600 * tilePrice / regen;
      _timeTF.setText("" + ((int) time));
    } catch (NumberFormatException e) {
      LOGGER.warning("ENTER NUMBERS!");
    }

  }

  @SuppressWarnings("serial")
  private JToolBar createToolbar2() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    // SCAN
    {
      // SHIPS
      _popupsToggle = new JToggleButton("Popups");
      // _fishToggle.setSelected(true);
      toolbar.add(_popupsToggle);

      // SHIPS
      _gatesToggle = new JToggleButton("Gates");
      // _shipsToggle.setSelected(true);
      toolbar.add(_gatesToggle);

      // // BUILDINGS
      // _industriesToggle = new JToggleButton("Industries");
      // // _industriesToggle.setSelected(true);
      // _industriesToggle.addItemListener(new ItemListener() {
      // @Override
      // public void itemStateChanged(ItemEvent e) {
      // boolean b = e.getStateChange() == ItemEvent.SELECTED;
      // _buildingsTask.setEnabled(b);
      // _settings.setProperty("industries", "" + b);
      // _settings.saveSettingsSorted();
      //
      // }
      // });
      // toolbar.add(_industriesToggle);
      //
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

      _ping2Toggle = new JToggleButton("Ping2");
      // _autoSailorsToggle.setSelected(false);
      _ping2Toggle.addItemListener(new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent e) {
          boolean b = e.getStateChange() == ItemEvent.SELECTED;
          LOGGER.info("Ping2: " + (b ? "on" : "off"));
          _settings.setProperty("ping2", "" + b);
          _settings.saveSettingsSorted();

        }
      });

      toolbar.add(_ping2Toggle);

      _slowToggle = new JToggleButton("Slow");
      // _shipsToggle.setSelected(true);
      toolbar.add(_slowToggle);
    }
    {
      final JLabel greens = new JLabel("0");
      toolbar.add(greens);
      _mazeRunner.addPropertyChangeListener("GREEN_CLICKED", new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          int n = Integer.parseInt(greens.getText());
          n++;
          greens.setText("" + n);
        }
      });
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

        // _mouse.mouseMove(_scanner._menuBR);
        // _mouse.delay(1000);
        // if (_scanner.scanForMapButtons()) {
        // _mouse.delay(1000);
        // _mouse.mouseMove(_scanner._eastButtons);
        // _mouse.delay(1000);
        // _mouse.mouseMove(_scanner._westButtons);
        // } else {
        // LOGGER.info("COUNDNT FIND EASTWEST...");
        // }

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
    _lastTimeActivity = 0;

    try {
      _mouse.saveCurrentPosition();
      _fstart = System.currentTimeMillis();
      do {
        runOnce();
      } while (!_stopAllThreads);

    } catch (RobotInterruptedException e) {
      LOGGER.info("INTERRUPTED!");
      setTitle(APP_TITLE);
      // e.printStackTrace();
    }
  }

  private void runOnce() throws RobotInterruptedException {
    long mandatoryRefresh = _settings.getInt("autoRefresh.mandatoryRefresh", 45) * 60 * 1000;
    long now = System.currentTimeMillis();
    _mouse.checkUserMovement();
    // // 1. SCAN
    handlePopups(false);

    // STUCK PREVENTION - 10min inactivity -> refresh
    LOGGER.info("LTA: " + (now - _lastTimeActivity) + " > " + (10 * 60 * 1000) + "? "
        + (_lastTimeActivity != 0 && now - _lastTimeActivity > 10 * 60 * 1000));

    if (_lastTimeActivity != 0 && now - _lastTimeActivity > 10 * 60 * 1000) {
      LOGGER.info("refresh due to inactivity...");
      try {
        refresh(false);
      } catch (AWTException e) {
        LOGGER.info("FAILED TO refresh: " + e.getMessage());
      } catch (IOException e) {
        LOGGER.info("FAILED TO refresh: " + e.getMessage());
      }
      _fstart = System.currentTimeMillis();
    }

    // REFRESH
    // LOGGER.info("refresh ? " + _autoRefreshToggle.isSelected() + " - " +
    // mandatoryRefresh + " < " + (now - fstart));
    if (_autoRefreshToggle.isSelected() && mandatoryRefresh > 0 && now - _fstart >= mandatoryRefresh) {
      LOGGER.info("refresh time...");
      try {
        refresh(false);
      } catch (AWTException e) {
        LOGGER.info("FAILED TO refresh: " + e.getMessage());
      } catch (IOException e) {
        LOGGER.info("FAILED TO refresh: " + e.getMessage());
      }
      _fstart = System.currentTimeMillis();
    }

    // _mouse.checkUserMovement();
    // if (_pingToggle.isSelected()) {
    // ping();
    // }

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
  }

  private void doAgenda() {
    if (_agenda != null && !_agenda.getEntries().isEmpty()) {
      Thread myThread = new Thread(new Runnable() {
        @Override
        public void run() {
          _mazeRunner.setStopIt(false);
          try {
            do {
              List<AgendaEntry> agendas = _agenda.getEntries();
              for (AgendaEntry agenda : agendas) {
                if (_stopAllThreads)
                  break;

                final String directions = agenda.getDirections();
                handlePopups(false);
                try {
                  boolean success = mapManager.gotoPlace(agenda.getWorldName(), agenda.getMapName(),
                      agenda.getPlaceName());
                  if (success && !_stopAllThreads) {
                    LOGGER.info("WORKING ON " + agenda.toString());
                    // do this place
                    _fstart = System.currentTimeMillis();
                    long start = System.currentTimeMillis();
                    Thread runMazeThread = new Thread(new Runnable() {
                      public void run() {
                        try {
                          int tries = 0;
                          Pixel p;
                          do {
                            tries++;
                            LOGGER.info("Looking for diggy... " + tries);
                            p = _scanner.findDiggy(_scanner.getScanArea());
                            _mouse.delay(700);
                          } while (p == null && tries < 22);
                          if (p != null) {
                            LOGGER.info("TRAVERSE START!");
                            if (directions != null && directions.length() == 4) {
                              _mazeRunner.setDirections(Direction.buildDirections(directions));
                            }
                            _mazeRunner.traverse(p);
                            LOGGER.info("TARVERSE DONE!");
                          }

                        } catch (RobotInterruptedException e) {
                          LOGGER.info("INTERRUPTED...");
                        } catch (IOException e) {
                          e.printStackTrace();
                        } catch (AWTException e) {
                          e.printStackTrace();
                        } catch (Exception e) {
                          e.printStackTrace();
                        }
                      }
                    }, "RUN_MAZE");
                    runMazeThread.start();

                    // sleep
                    do {
                      _mouse.delay(1000, false);// DO NOT INTTERRUPT!!!
                      if (false)
                        scanEnergy();// EXPERIMENTAL!!!
                      if (!isRunning("RUN_MAZE")) {
                        break;
                      }
                      // LOGGER.info("tik tak... " + (System.currentTimeMillis()
                      // - _fstart) / 1000);
                    } while (System.currentTimeMillis() - start < _settings.getInt("agenda.inactiveTimeOut", 30)
                        * 60000);

                    // THAT'S IT. STOP IT IF NOT DONE ALREADY
                    if (isRunning("RUN_MAZE")) {
                      LOGGER.info("STOPPING maze runner...");
                      _mazeRunner.setStopIt(true);
                      while (isRunning("RUN_MAZE")) {
                        try {
                          Thread.sleep(5000);
                        } catch (InterruptedException e) {
                        }
                        if (isRunning("RUN_MAZE"))
                          LOGGER.info("maze runner still running...");
                      }
                    }

                    // REFRESH
                    if (_autoRefreshToggle.isSelected() && System.currentTimeMillis()
                        - start >= _settings.getInt("agenda.inactiveTimeOut", 30) * 30000) {
                      LOGGER.info("refresh time...");
                      try {
                        refresh(false);
                      } catch (AWTException e) {
                        LOGGER.info("FAILED TO refresh: " + e.getMessage());
                      } catch (IOException e) {
                        LOGGER.info("FAILED TO refresh: " + e.getMessage());
                      }
                      _fstart = System.currentTimeMillis();
                    }

                    LOGGER.info("MOVE TO NEXT PLACE...");
                  }
                } catch (PlaceUnreachableException e) {
                  LOGGER.warning(e.getMessage());
                  refresh(false);
                }
              }
              LOGGER.info("stop all threads: " + _stopAllThreads);
            } while (!_stopAllThreads);
          } catch (RobotInterruptedException e) {
            LOGGER.info("INTERRUPTED!");
          } catch (IOException e) {
            e.printStackTrace();
          } catch (AWTException e) {
            e.printStackTrace();
          }
        }
      }, "MAGIC");

      myThread.start();
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
      // _scanner.reset();

      boolean done = false;
      for (int i = 0; i < 17 && !done; i++) {
        LOGGER.info("after refresh recovery try " + (i + 1));

        handlePopups(i > 10);
        _mouse.delay(200);
        handlePopups(i > 10);
        _mouse.delay(200);
        handlePopups(i > 10);
        _mouse.delay(200);
        // LOCATE THE GAME
        if (_scanner.locateGameArea(false) && !_scanner.isWide()) {
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

  private GraphMazeRunner _mazeRunner;

  private MazeCanvas _mazeCanvas;

  private MazeProtocol _mazeProtocol;

  private long _fstart;

  private AgendaManagerUI _agendaManagerUI;

  protected Agenda _agenda;

  private void setAgenda(String agName) {
    _agendaManagerUI.setAgenda(agName);
  }

  private void handlePopups(boolean wide) throws RobotInterruptedException {
    try {
      LOGGER.info("Popups...");
      boolean found = false;
      Pixel p = null;
      Rectangle area = _scanner.generateWindowedArea(204, 648);// was 486
      if (_scanner.isOptimized()) {
        _mouse.mouseMove(_scanner.getParkingPoint());
        _mouse.delay(300);
      } else {
        wide = true;
      }

      long start = System.currentTimeMillis();
      area.y = _scanner.getTopLeft().y + _scanner.getGameHeight() / 2;

      if (wide) {
        area = _scanner.generateWindowedArea(800, _scanner.getGameHeight());
      }

      p = _scanner.scanOneFast("share.bmp", area, false);
      if (p != null) {
        _mouse.click(p.x + 34, p.y + 11);
        _mouse.delay(200);
      }

      p = _scanner.scanOneFast("X.bmp", _scanner._popupAreaX, false);
      if (p != null) {
        _mouse.click(p.x + 16, p.y + 16);
        _mouse.delay(200);
      } else {
        p = _scanner.scanOneFast("claim.bmp", null, true);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (AWTException e) {
      e.printStackTrace();
    }

  }

  private void reapplySettings() {

    // toggles

    boolean popups = "true".equalsIgnoreCase(_settings.getProperty("popups"));
    if (popups != _popupsToggle.isSelected()) {
      _popupsToggle.setSelected(popups);
    }

    boolean gates = "true".equalsIgnoreCase(_settings.getProperty("gates"));
    if (gates != _gatesToggle.isSelected()) {
      _gatesToggle.setSelected(gates);
    }

    boolean slow = "true".equalsIgnoreCase(_settings.getProperty("slow"));
    if (slow != _slowToggle.isSelected()) {
      _slowToggle.setSelected(slow);
    }

    boolean ping = "true".equalsIgnoreCase(_settings.getProperty("ping"));
    if (ping != _pingToggle.isSelected()) {
      _pingToggle.setSelected(ping);
    }

    boolean ping2 = "true".equalsIgnoreCase(_settings.getProperty("ping2"));
    if (ping2 != _ping2Toggle.isSelected()) {
      _ping2Toggle.setSelected(ping2);
    }

    boolean ar = "true".equalsIgnoreCase(_settings.getProperty("autoRefresh"));
    if (ar != _autoRefreshToggle.isSelected()) {
      _autoRefreshToggle.setSelected(ar);
    }

    // agenda
    String ag = _settings.getProperty("agenda", "DEFAULT");
    if (!ag.equals(_agenda != null ? _agenda.getName() : "")) {
      setAgenda(ag);
    }

  }

  private void stopMagic() {
    _mazeRunner.setStopIt(true);
    _stopAllThreads = true;
    LOGGER.info("Stopping...");
    int tries = 10;
    boolean stillRunning = true;
    _mouse.mouseMove(_scanner.getSafePoint());
    for (int i = 0; i < tries && stillRunning; ++i) {
      stillRunning = isRunning("MAGIC");
      if (stillRunning) {
        LOGGER.info("Magic still working...");
        try {
          Thread.sleep(5000);
          // for (int j = 0; j < 150; j++) {
          // _mouse.mouseMove(_scanner.getSafePoint());
          // Thread.sleep(100);
          // }
        } catch (InterruptedException e) {
        }
      } else {
        LOGGER.info("MAGIC STOPPED");
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

      } else if (r.startsWith("agenda")) {
        service.inProgress(r);
        stopMagic();
        doAgenda();
        captureScreen(null);
      } else if (r.startsWith("click")) {
        service.inProgress(r);
        processClick(r);

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
  
  private void processClick(String r) {
    try {
      String[] ss = r.split("_");
      int x = Integer.parseInt(ss[1]);
      int y = Integer.parseInt(ss[2]);
      _mouse.click(x, y);
      try {
        _mouse.delay(1000);
      } catch (RobotInterruptedException e) {
      }
    } finally {
      new Service().done(r);
    }
  }

  private void runSettingsListener() {
    Thread requestsThread = new Thread(new Runnable() {
      public void run() {
        // new Service().purgeAll();
        boolean stop = false;
        do {
          // LOGGER.info("......");
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
    captureArea(null, filename);
  }

  private void captureArea(Rectangle area, String filename) {
    if (filename == null)
      filename = "ping ";
    if (area == null) {
      final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      area = new Rectangle(0, 0, screenSize.width, screenSize.height);
    }
    writeImage(area, filename + DateUtils.formatDateForFile(System.currentTimeMillis()) + ".jpg");
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

  private void scanDiggyFromHere(boolean clearMatrix) {
    int seconds = Integer.parseInt(_timeTF.getText());
    _mazeRunner.doSomething(clearMatrix, seconds);
  }

  private JPanel createAgendaManagerPanel() {
    _agendaManagerUI = new AgendaManagerUI(mapManager);
    _agendaManagerUI.addListSelectionListener(new ListSelectionListener() {

      @SuppressWarnings("rawtypes")
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          JList list = (JList) e.getSource();
          _agenda = (Agenda) list.getSelectedValue();
          // _shipProtocolExecutor.setShipProtocol(_shipProtocol);
          if (_agenda != null) {
            _settings.setProperty("agenda", _agenda.getName());
            _settings.saveSettingsSorted();
          }
        }
      }
    });
    return _agendaManagerUI;
  }

  private OCREnergy ocrEnergy;

  private void scanEnergy() throws AWTException {
    Rectangle energyArea = _scanner.getEnergyArea();
    if (energyArea != null) {
      BufferedImage image = new Robot().createScreenCapture(energyArea);

      String res = ocrEnergy.scanImage(image);
      LOGGER.info("ENERGY: " + res);
    }
  }

}
