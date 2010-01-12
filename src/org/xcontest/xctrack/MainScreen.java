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

package org.xcontest.xctrack;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.xcontest.xctrack.config.Config;
import org.xcontest.xctrack.gps.GpsDeviceInfo;
import org.xcontest.xctrack.info.InfoCenter;
import org.xcontest.xctrack.settings.Profile;
import org.xcontest.xctrack.settings.SettingsMenu;

public class MainScreen implements CommandListener, ScreenListener {

	private SettingsMenu _settings;
	private int _idxStart;
	private int _idxContinue;
	private int _idxSettings;
	private int _idxDebug;
	private int _idxExit;
	private Command _cmdOk;
	
	private List _list;
	
	public MainScreen() {
		_list = new List("XC Track (version "+App.getMidletVersion()+")",List.IMPLICIT);
		_idxStart = _list.append("Start Tracking", null);
		if (Config.getLastTrackKey() != null)
			_idxContinue = _list.append("Continue Tracking", null);
		else
			_idxContinue = -1;
		_idxSettings = _list.append("Settings", null);
		_idxDebug = -1;	// is appended to form in screenShown() if in debug mode
		_idxExit = _list.append("Exit", null);
		_cmdOk = new Command("Select", Command.OK, 1);
		_list.addCommand(_cmdOk);
		_list.setCommandListener(this);
		
		_settings = null;
		_lastTime = -1;
	}
	
	public void show() {
		App.showScreen(_list,this);
	}
	
	private void startTracking() {
		_dev = Config.getGpsDevice();
		Profile[] profiles = Config.getProfiles();
		
		if (_dev == null && profiles.length == 0) {
			Util.showError("Cannot start tracking - You must setup your GPS and your Profile first!\nPlease go to Settings->GPS and select your GPS device AND go to Settings->Profiles and create a new profile");
		}
		else if (_dev == null) {
			Util.showError("Cannot start tracking - You must setup your GPS first!\nPlease go to Settings->GPS and select your GPS device.");
		}
		else if (profiles.length == 0) {
			Util.showError("Cannot start tracking - You must setup your Profile first!\nPlease go to Settings->Profiles and create a new profile.");
		}
		else if (profiles.length == 1) {
			InfoCenter.getInstance().startTracking(profiles[0]);
		}
		else {
			new StartTrackingScreen().show();
		}
	}
	
	private void continueTracking() {
		_dev = Config.getGpsDevice();
		if (_dev == null) {
			Util.showError("Cannot start tracking - set GPS driver&device first");
		}
		else {
			InfoCenter.getInstance().continueTracking();
		}
	}
	
	private void showSettings() {
		if (_settings == null)
			_settings = new SettingsMenu();
		_settings.show();
	}
	
	private void showDebug() {
		new DebugScreen().show();
	}
	
	public void screenShown(Displayable disp, boolean explicit) {
//if (!explicit) continueTracking();

		if (Config.isDebugMode()) {
			if (_idxDebug < 0) {
				_idxDebug = _list.size()-1;
				_idxExit = _idxDebug+1;
				_list.insert(_idxDebug,"DEBUG", null);
			}
		}
		else {
			if (_idxDebug >= 0) {
				_list.delete(_idxDebug);
				_idxExit = _idxDebug;
				_idxDebug = -1;
			}
		}
	}
		
	public void commandAction(Command cmd, Displayable disp) {
		int idx = _list.getSelectedIndex();
		if (idx == _idxStart)
			startTracking();
		else if (idx == _idxContinue)
			continueTracking();
		else if (idx == _idxSettings)
			showSettings();
		else if (idx == _idxDebug)
			showDebug();
		else if (idx == _idxExit)
			App.exit();
	}

	GpsDeviceInfo _dev;
	int _lastTime;
}
