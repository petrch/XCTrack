/*
 *  XCTrack - XContest Live Tracking client for J2ME devices
 *  Copyright (C) 2009 Petr Chromec <petr@xcontest.org>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.xcontest.xctrack.settings;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;

import org.xcontest.xctrack.App;
import org.xcontest.xctrack.Util;
import org.xcontest.xctrack.config.Config;
import org.xcontest.xctrack.gps.GpsDeviceInfo;
import org.xcontest.xctrack.gps.GpsDriver;

public class GpsPage implements ItemStateListener, CommandListener {
	
	Form _form;
	GpsDriver[] _drivers;
	GpsDeviceInfo[] _devices;
	
	ChoiceGroup _choiceGpsDriver;
	ChoiceGroup _choiceGpsDevice;
	Command _cmdSave,_cmdCancel;
	
	GpsDriver _driver;
	GpsDeviceInfo _device, _origDevice;
	Thread _scanningThread;
	
	public GpsPage() {
		_driver = Config.getGpsDriver();
		_device = Config.getGpsDevice();
		_origDevice = _device;
		_scanningThread = null;
		
		if (Config.isDebugMode()) {
			_drivers = GpsDriver.getAllDrivers();
		}
		else {
			GpsDriver[] drivers = GpsDriver.getAllDrivers();
			int cnt = 0;
			for (int i = 0; i < drivers.length; i ++)
				if (!drivers[i].isForDebugModeOnly())
					cnt += 1;
			_drivers = new GpsDriver[cnt];
			cnt = 0;
			for (int i = 0; i < drivers.length; i ++)
				if (!drivers[i].isForDebugModeOnly())
					_drivers[cnt++] = drivers[i];
		}
		
		_form = new Form("Settings / GPS");
		_choiceGpsDriver = new ChoiceGroup("GPS Type",ChoiceGroup.POPUP);
		int idx = 0;
		for (int i = 0; i < _drivers.length; i ++) {
			_choiceGpsDriver.append(_drivers[i].getName(), null);
			if (_drivers[i] == _driver)
				idx = i;
		}
		_choiceGpsDriver.setSelectedIndex(idx,true);
		_form.append(_choiceGpsDriver);

		_choiceGpsDevice = new ChoiceGroup("GPS Device",ChoiceGroup.EXCLUSIVE);
		
		_cmdSave = new Command("OK",Command.OK, 1);
		_cmdCancel = new Command("Back",Command.BACK, 1);
		_form.addCommand(_cmdSave);
		_form.addCommand(_cmdCancel);
		_form.setItemStateListener(this);
		_form.setCommandListener(this);
		
		driverChanged();
	}
	
	private synchronized void scanFinished(GpsDeviceInfo[] devices) {
		if (_scanningThread == Thread.currentThread()) {
			_devices = devices;
			_form.delete(1);
			
			_choiceGpsDevice.deleteAll();
			if (_devices.length > 0) {
				int idx = 0;
				for (int i = 0; i < _devices.length; i ++) {
					_choiceGpsDevice.append(_devices[i].getName(), null);
					if (_origDevice != null && _devices[i].getAddress().equals(_origDevice.getAddress()))
						idx = i;
				}
				_choiceGpsDevice.setSelectedIndex(idx,true);
				_device = _devices[idx];
				_form.append(_choiceGpsDevice);
			}
			else {
				_form.append("(no device found)");
				_device = null;
			}
			
			_scanningThread = null;
		}
	}
	
	private synchronized void driverChanged() {
		_driver = _drivers[_choiceGpsDriver.getSelectedIndex()];
		
		if (_form.size() > 1)
			_form.delete(1);
		
		if (_driver.hasSingleDevice()) {
			try {
				_device = _driver.scanDevices()[0];
			}
			catch(InterruptedException e) {
				_device = null;			// should NEVER happen
			}
			if (_scanningThread != null)
				_scanningThread.interrupt();
			_scanningThread = null;
		}
		else if (_driver.hasFastScan()) {
			try {
				_devices = _driver.scanDevices();
			}
			catch(InterruptedException e) {
				_devices = null;		// should NEVER happen
			}
			
			_choiceGpsDevice.deleteAll();
			if (_devices.length > 0) {
				int idx = 0;
				for (int i = 0; i < _devices.length; i ++) {
					_choiceGpsDevice.append(_devices[i].getName(), null);
					if (_origDevice != null && _devices[i].getAddress().equals(_origDevice.getAddress()))
						idx = i;
				}
				_choiceGpsDevice.setSelectedIndex(idx,true);
				_device = _devices[idx];
				_form.append(_choiceGpsDevice);
			}
			else {
				_form.append("(no device found)");
				_device = null;
			}
			
			
			if (_scanningThread != null)
				_scanningThread.interrupt();
			_scanningThread = null;
		}
		else {
			_device = null;
			_form.append("Scanning...");
			if (_scanningThread != null)
				_scanningThread.interrupt();
			_scanningThread = new Thread() {
				public void run() {
					try {
						scanFinished(_driver.scanDevices());
					}
					catch (InterruptedException e) {} 
				}
			};
			_scanningThread.start();
		}
	}
	
	public void show() {
		App.showScreen(_form);
	}
	
	public void commandAction(Command cmd, Displayable disp) {
		if (cmd == _cmdSave) {
			if (_driver == null || _device == null) {
				Util.showError("Choose GPS device first");
			}
			else {
				Config.setGpsDriver(_driver);
				Config.setGpsDevice(_device);
				Config.writeAll();
				App.hideScreen(_form);
			}
		}
		else {
			App.hideScreen(_form);
		}
	}

	public void itemStateChanged(Item item) {
		if (item == _choiceGpsDriver) {
			driverChanged();
		}
		if (item == _choiceGpsDevice) {
			_device = _devices[_choiceGpsDevice.getSelectedIndex()];
		}
	}

}
