package com.horowitz.daze.camp;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.horowitz.commons.*;
import com.horowitz.daze.ScreenScanner;


public class CampManager {
  private final static Logger LOGGER = Logger.getLogger("MAIN");
  private ScreenScanner _scanner;
  private MouseRobot _mouse;
  private List<Option> _caravanOptions;
  private List<Option> _kitchenOptions;
  private List<Option> _foundryOptions;

  public CampManager(ScreenScanner scanner) {
    super();
    _scanner = scanner;
    _mouse = _scanner.getMouse();
  }

  public void loadData() {
    try {
      CampJsonStorage storage = new CampJsonStorage();
      _caravanOptions = storage.loadCaravanOptions();
      _kitchenOptions = storage.loadKitchenOptions();
      _foundryOptions = storage.loadFoundryOptions();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public List<Option> getCaravanOptions() {
    return _caravanOptions;
  }

  public List<Option> getKitchenOptions() {
    return _kitchenOptions;
  }

  public List<Option> getFoundryOptions() {
    return _foundryOptions;
  }
  
  public Pixel findKitchen() throws RobotInterruptedException, IOException, AWTException {
    Rectangle area = new Rectangle();
    return _scanner.scanOneFast("camp/egypt/kitchen.bmp", area, false);
  }
  

}
