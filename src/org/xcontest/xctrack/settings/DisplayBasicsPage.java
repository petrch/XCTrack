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
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.TextField;

import org.xcontest.xctrack.App;
import org.xcontest.xctrack.Util;
import org.xcontest.xctrack.config.Config;


public class DisplayBasicsPage implements CommandListener, ItemCommandListener, ItemStateListener {

	private Form _form;
	private ChoiceGroup _keepBacklight;
	private Gauge _backlight;
	
	private TextField _repaintInterval;
	
	private Command _cmdOk;
	private Command _cmdTestBacklight;
	private boolean _hasBacklight;

	public DisplayBasicsPage() {
		_form = new Form("Display");
		_form.setItemStateListener(this);
		
		_repaintInterval = new TextField("Repaint interval (sec)","",8,TextField.DECIMAL);
		_repaintInterval.setString(formatDouble(Config.getWidgetPageRepaintInterval()));
		_form.append(_repaintInterval);
		
		_hasBacklight = Util.hasBacklightSetting();
		if (_hasBacklight) {
			_keepBacklight = new ChoiceGroup("Backlight while tracking",ChoiceGroup.MULTIPLE);
			_keepBacklight.append("Backlight ON by default", null);
			_keepBacklight.setSelectedIndex(0, Config.getKeepBacklight());
			
			_cmdTestBacklight = new Command("Test",Command.ITEM,1);
			_backlight = new Gauge("Backlight intensity",_hasBacklight,20,getBacklightGaugeVal(Config.getBacklightLevel()));
			_backlight.setDefaultCommand(_cmdTestBacklight);
			_backlight.setItemCommandListener(this);
			_form.append(_keepBacklight);
			_form.append(_backlight);
		}
		else {
			_form.append("Backlight settings is not supported for your device");
		}
		
		_cmdOk = new Command("OK",Command.OK,1);
		_form.addCommand(_cmdOk);
		_form.setCommandListener(this);
	}
	
	private int getBacklightLevel(int gaugeVal) {
		return (4*gaugeVal*gaugeVal+15*gaugeVal+9)/19;
	}
	
	private int getBacklightGaugeVal(int level) {
		return (int)Math.floor(0.5+(Math.sqrt(225 + 304*level)-15)/8);
	}

	private String formatDouble(double d) {
		if (Math.floor(d) == d)
			return ""+(int)d;
		else
			return ""+d;
	}
	
	public void show() {
		App.showScreen(_form);
	}
	
	public void hide() {
		App.hideScreen(_form);
	}

	public void commandAction(Command cmd, Displayable disp) {
		int cmdType = cmd.getCommandType();
		if (cmd == _cmdOk || cmdType == Command.SCREEN) {
			double interval = Double.parseDouble(_repaintInterval.getString());
			if (interval <= 0) {
				Util.showError("Invalid repaint interval set! Please enter positive value");
			}
			else {
				Config.setWidgetPageRepaintInterval(interval);
				if (_hasBacklight) {
					Config.setKeepBacklight(_keepBacklight.isSelected(0));
					Config.setBacklightLevel(getBacklightLevel(_backlight.getValue()));
				}
				Config.writeAll();
				hide();
			}
		}
	}

	public void commandAction(Command cmd, Item item) {
		if (cmd == _cmdTestBacklight) {
			new Thread(){
				public void run() {
					Util.setBacklight(getBacklightLevel(_backlight.getValue()));
					
					try {
						Thread.sleep(2000);
					}
					catch(InterruptedException e) {}

					Util.setBacklight(50);
				}
			}.start();
		}
	}
	
	public void itemStateChanged(Item item) {
		if (item == _keepBacklight) {
//			update();
		}
		else if (item == _backlight) {
			if (_backlight.getValue() == 0) {
				Util.showInfo("Setting backlight level to 0 may cause the screen locked up on some devices.\nAre you really sure you want to set level 0?");
			}
		}
	}
}


