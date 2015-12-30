package com.horowitz.daze;

import java.awt.AWTException;
import java.io.IOException;

import com.horowitz.commons.RobotInterruptedException;

public interface GameProtocol {

  public void update();

  public void execute() throws RobotInterruptedException;

	public boolean preExecute() throws AWTException, IOException, RobotInterruptedException;
  
  
  
}
