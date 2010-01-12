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
import javax.microedition.lcdui.TextField;

import org.xcontest.live.LoginDomain;
import org.xcontest.live.TrackType;
import org.xcontest.xctrack.App;
import org.xcontest.xctrack.Util;
import org.xcontest.xctrack.config.Config;

public class ProfilePage implements ItemStateListener, CommandListener {
	
	
	Form _form;
	ChoiceGroup _choiceIsRegistered;
	Command _cmdSave;
	Command _cmdCancel;
	
	TextField _username;
	TextField _password;
//	ChoiceGroup _domain;
	
	TextField _firstname;
	TextField _surname;
	TextField _nickname;
	
	TextField _profileName;
	TextField _tracklogInterval;
	TextField _messageInterval;
	ChoiceGroup _trackType;
	
	LoginDomain[] _allDomains;
	TrackType[] _allTrackTypes;
	
	Profile[] _profiles;
	int _profileIdx;
	
	public ProfilePage(Profile[] profiles, int editIdx) {
		_profileIdx = editIdx;
		_profiles = profiles;
		createForm();
		if (editIdx >= 0)
			set(profiles[editIdx]);
		else
			clear();
	}

	private void createForm() {
		_choiceIsRegistered = new ChoiceGroup("Login",ChoiceGroup.EXCLUSIVE);
		_choiceIsRegistered.append("Anonymous", null);
		_choiceIsRegistered.append("XContest registered user", null);
		_choiceIsRegistered.setSelectedIndex(0,true);
		
		_profileName = new TextField("Profile","",64,TextField.ANY);
		
		_allDomains = LoginDomain.getAll();
		_allTrackTypes = TrackType.getAll();
		/*
		_domain = new ChoiceGroup("Domain",ChoiceGroup.POPUP);
		for (int i = 0; i < _allDomains.length; i ++)
			_domain.append(_allDomains[i].getName(), null);
		*/
		_username = new TextField("Username","",64,TextField.ANY);
		_password = new TextField("Password","",64,TextField.ANY | TextField.PASSWORD);

		_firstname = new TextField("Firstname","",64,TextField.ANY);
		_surname = new TextField("Surname","",64,TextField.ANY);
		_nickname = new TextField("Nickname","",64,TextField.ANY);

		_trackType = new ChoiceGroup("Track type",ChoiceGroup.POPUP);
		for (int i = 0; i < _allTrackTypes.length; i ++)
			_trackType.append(_allTrackTypes[i].getName(), null);

		_tracklogInterval = new TextField("Tracklog points interval (sec)","",8,TextField.DECIMAL);
		_messageInterval = new TextField("Position message interval (sec)","",8,TextField.NUMERIC);
		
		
		_cmdSave = new Command("Save",Command.OK, 1);
		_cmdCancel = new Command("Cancel",Command.BACK, 2);
		
		if (_profileIdx >= 0)
			_form = new Form("Settings / Profiles / " + _profiles[_profileIdx].getProfileName());
		else
			_form = new Form("Settings / Profiles / New");
		_form.setItemStateListener(this);
		_form.setCommandListener(this);
		
		_form.addCommand(_cmdSave);
		_form.addCommand(_cmdCancel);

		_form.append(_profileName);
		_form.append(_choiceIsRegistered);
		_form.append(_trackType);
		update();
	}
	
	public void show() {
		App.showScreen(_form);
	}
	
	private void update() {
		boolean reg = _choiceIsRegistered.getSelectedIndex() == 1;
		while (_form.size() > 3)
			_form.delete(_form.size()-1);
		if (reg) {
			_form.append(_username);
			_form.append(_password);
		}
		else {
			_form.append(_firstname);
			_form.append(_surname);
			_form.append(_nickname);
		}
		_form.append("Transfer/performance properties");
		_form.append(_tracklogInterval);
		_form.append(_messageInterval);
	}

	public void itemStateChanged(Item item) {
		if (item == _choiceIsRegistered) {
			update();
		}
	}
	
	private String formatDouble(double d) {
		if (Math.floor(d) == d)
			return ""+(int)d;
		else
			return ""+d;
	}
	
	private void clear() {
		_choiceIsRegistered.setSelectedIndex(0, true);
		
		_username.setString("");
		_password.setString("");
//		_domain.setSelectedIndex(0, true);
		
		_firstname.setString("");
		_surname.setString("");
		_nickname.setString("");
		
		_profileName.setString("My Profile");
		_trackType.setSelectedIndex(_allTrackTypes.length-1, true);
		_tracklogInterval.setString(formatDouble(Profile.DEFAULT_TRACKLOG_INTERVAL));
		_messageInterval.setString(""+(int)Profile.DEFAULT_MESSAGE_INTERVAL);
	}
	
	private void set(Profile p) {
		_choiceIsRegistered.setSelectedIndex(p.isAnonymous() ? 0 : 1, true);
		_username.setString(p.getUsername());
		_password.setString(p.getPassword());
		
/*
		LoginDomain[] all = LoginDomain.getAll();
		int domIdx = 0;
		for (int i = 0; i < all.length; i ++) {
			if (all[i].getValue().equals(p.getDomain())) {
				domIdx = i;
				break;
			}
		}
		_domain.setSelectedIndex(domIdx, true);
*/
		_firstname.setString(p.getFirstname());
		_surname.setString(p.getSurname());
		_nickname.setString(p.getNickname());
		
		_profileName.setString(p.getProfileName());
		
		String tt = p.getTrackType();
		for (int i = 0; i < _allTrackTypes.length; i ++) {
			if (i == _allTrackTypes.length-1 || tt.equals(_allTrackTypes[i].getValue())) {
				_trackType.setSelectedIndex(i, true);
				break;
			}
		}
		
		_tracklogInterval.setString(formatDouble(p.getTracklogInterval()));
		_messageInterval.setString(""+(int)p.getMessageInterval());
		
		update();
	}

	private Profile get() {
		Profile p = new Profile();
		boolean isRegistered = _choiceIsRegistered.getSelectedIndex() != 0;
		
		if (isRegistered) {
//			p.setDomain(_allDomains[_domain.getSelectedIndex()].getValue());
			p.setDomain(_allDomains[0].getValue());
			p.setUsername(_username.getString());
			p.setPassword(_password.getString());
		}
		else {
			p.setFirstname(_firstname.getString());
			p.setSurname(_surname.getString());
			p.setNickname(_nickname.getString());
		}
		
		p.setProfileName(_profileName.getString());
		p.setTrackType(_allTrackTypes[_trackType.getSelectedIndex()].getValue());
		p.setTracklogInterval(Double.parseDouble(_tracklogInterval.getString()));
		p.setMessageInterval(Double.parseDouble(_messageInterval.getString()));
		
		p.setAnonymous(!isRegistered);
		
		return p;
	}

	public void commandAction(Command cmd, Displayable disp) {
		if (cmd == _cmdSave) {
			Profile p = get();
			// error checking
			if (p.getProfileName().equals("")) {
				Util.showError("Please enter profile name.");
				return;
			}
			for (int i = 0; i < _profiles.length; i ++) {
				if (i != _profileIdx && _profiles[i].getProfileName().equals(p.getProfileName())) {
					Util.showError("Profile with name '"+p.getProfileName()+"' already exists.\nPlease enter different name.");
					return;
				}
			}
			if (p.isAnonymous() && p.getNickname().equals("")) {
				Util.showError("Nickname is required for anonymous tracking.\nPlease enter your nickname.");
				return;
			}
			if (!p.isAnonymous() && p.getUsername().equals("")) {
				Util.showError("Username is required for registered user.\nPlease enter your username");
				return;
			}
			if (!p.isAnonymous() && p.getPassword().equals("")) {
				Util.showError("Password is required for registered user.\nPlease enter your password");
				return;
			}
			if (Config.isDebugMode()) {
				if (p.getTracklogInterval() < 0.001) {
					Util.showError("Tracklog points interval set too low - set to at least 0.001 (1000 points per second)");
					return;
				}
			}
			else {
				if (p.getTracklogInterval() < 0.1) {
					Util.showError("Tracklog points interval set too low - set to at least 0.1 (10 points per second)");
					return;
				}
			}
			if (p.getMessageInterval() < p.getTracklogInterval()) {
				Util.showError("Position message too low - set to value at least equal to tracklog points interval");
				return;
			}
			// this ensures we can make it into 64kb position message
			if (p.getMessageInterval() > 500*p.getTracklogInterval()) {
				Util.showError("Position message send interval too high - maximum allowed value is 500times the tracklog points interval");
				return;
			}
			
			
			if (_profileIdx < 0) {
				Profile[] newProfiles = new Profile[_profiles.length+1];
				for (int i = 0; i < _profiles.length; i ++)
					newProfiles[i] = _profiles[i];
				newProfiles[_profiles.length] = get();
				_profiles = newProfiles;
			}
			else {
				_profiles[_profileIdx] = get();
			}
			Config.setProfiles(_profiles);
			Config.writeAll();
			App.hideScreen(_form);
		}
		else if (cmd == _cmdCancel) {
			App.hideScreen(_form);
		}
	}
}
