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

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import org.xcontest.xctrack.config.Config;
import org.xcontest.xctrack.info.InfoCenter;
import org.xcontest.xctrack.settings.Profile;

public class StartTrackingScreen implements CommandListener {
	Form _form;
	Command _cmdBack;
	Command _cmdStart;
	ChoiceGroup _profile;
	
	public StartTrackingScreen() {
		_cmdBack = new Command("Back",Command.BACK,1);
		_cmdStart = new Command("Start",Command.OK,1);

		_profile = new ChoiceGroup("Log-in profile",ChoiceGroup.POPUP);
		Profile[] profiles = Config.getProfiles();
		for (int i = 0; i < profiles.length; i ++)
			_profile.append(profiles[i].getProfileName(), null);
		
		_form = new Form("Start tracking");
		_form.append(_profile);
		_form.addCommand(_cmdStart);
		_form.addCommand(_cmdBack);
		_form.setCommandListener(this);
	}
	
	public void show() {
		App.showScreen(_form);
	}

	public void commandAction(Command cmd, Displayable disp) {
		if (cmd == _cmdStart) {
			Profile profile = Config.getProfiles()[_profile.getSelectedIndex()];
			
			App.hideScreen(_form);
			InfoCenter.getInstance().startTracking(profile);
		}
		else if (cmd == _cmdBack) {
			App.hideScreen(_form);
		}
	}
	
}
