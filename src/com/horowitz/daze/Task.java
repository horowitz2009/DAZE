package com.horowitz.daze;

import java.awt.AWTException;
import java.io.IOException;
import java.io.Serializable;

import com.horowitz.commons.RobotInterruptedException;

public class Task implements Cloneable, Serializable {

	private static final long serialVersionUID = -7911501509255495065L;
	private String _name;
	private int _frequency;
	private boolean _active;
	private String _imageName;
	private GameProtocol _protocol;
	private boolean _enabled;

	public Task(String name, int frequency) {
		super();
		_name = name;
		_frequency = frequency;
		_enabled = true;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public int getFrequency() {
		return _frequency;
	}

	public void setFrequency(int frequency) {
		_frequency = frequency;
	}

	public String getImageName() {
		return _imageName;
	}

	public void setImageName(String imageName) {
		_imageName = imageName;
	}

	public boolean isActive() {
		return _active;
	}

	public void setActive(boolean active) {
		_active = active;
	}

	public GameProtocol getProtocol() {
		return _protocol;
	}

	public void setProtocol(GameProtocol protocol) {
		_protocol = protocol;
	}

	public void update() throws RobotInterruptedException {
		if (_protocol != null)
			_protocol.update();
	}

	public void execute() throws RobotInterruptedException {
		if (_protocol != null)
			_protocol.execute();
	}

	public boolean isEnabled() {
		return _enabled;
	}

	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}

	public void preExecute() throws AWTException, IOException, RobotInterruptedException {

		if (_protocol.preExecute()) {
			update();
		}
		
	}

}
