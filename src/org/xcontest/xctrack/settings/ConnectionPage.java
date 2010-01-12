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

import org.xcontest.xctrack.App;
import org.xcontest.xctrack.config.Config;

public class ConnectionPage implements ItemStateListener, CommandListener {
	
	Form _form;
	String _disclaimer = "You can find some advanced settings on this screen. If you are not sure what those mean, you are safe to leave the default values here.";
	ChoiceGroup _choiceConnectionType;
	ChoiceGroup _choiceHTTPPing;
	ChoiceGroup _choiceZLib;
	Command _cmdSave,_cmdCancel;
	
	TextField _textResendInterval;
	TextField _textReconnectInterval;
	
	
	public ConnectionPage() {
		createForm();
	}
	
	public void show() {
		App.showScreen(_form);
	}
	
	private int getProtocolIndex(int val) {
		if (val == Config.PROTOCOL_UDP)
			return 0;
		else if (val == Config.PROTOCOL_TCP)
			return 1;
		else
			return 2;
	}
	
	private int getPingIndex(int val) {
		if (val == Config.HTTP_PING_NEVER)
			return 0;
		else
			return 1;
	}
	
	private int getProtocolValue(int idx) {
		if (idx == 0)
			return Config.PROTOCOL_UDP;
		else
			return Config.PROTOCOL_TCP;
	}
	
	private int getPingValue(int idx) {
		if (idx == 0)
			return Config.HTTP_PING_NEVER;
		else
			return Config.HTTP_PING_ONCE;
	}

	private void createForm() {
		_choiceConnectionType = new ChoiceGroup("Protocol",ChoiceGroup.EXCLUSIVE);
		_choiceConnectionType.append("UDP", null);
		_choiceConnectionType.append("TCP", null);
		_choiceConnectionType.setSelectedIndex(getProtocolIndex(Config.getProtocol()),true);

		_choiceZLib = new ChoiceGroup("Data compression",ChoiceGroup.EXCLUSIVE);
		_choiceZLib.append("Use compression (Zlib)", null);
		_choiceZLib.append("None", null);
		_choiceZLib.setSelectedIndex(Config.getUseZLib() ? 0 : 1,true);
		
		_choiceHTTPPing = new ChoiceGroup("HTTP \"ping\"",ChoiceGroup.EXCLUSIVE);
		_choiceHTTPPing.append("Never", null);
		_choiceHTTPPing.append("On tracking start", null);
		_choiceHTTPPing.setSelectedIndex(getPingIndex(Config.getHTTPPingMode()),true);

		
		if (Config.isDebugMode()) {
			_textResendInterval = new TextField("Resend interval in case of no response (millisec)",""+Config.getResendInterval(),8,TextField.NUMERIC);
			_textReconnectInterval = new TextField("Reconnect interval (millisec)",""+Config.getReceiveReconnectInterval(),8,TextField.NUMERIC);
		}
		
		_cmdSave = new Command("OK",Command.OK, 1);
		_cmdCancel = new Command("Back",Command.BACK, 1);
		
		_form = new Form("Settings / Internet Connection");
		_form.setItemStateListener(this);
		_form.setCommandListener(this);
		
		_form.addCommand(_cmdSave);
		_form.addCommand(_cmdCancel);
		
		update();
	}

	public void itemStateChanged(Item item) {
		if (item == _choiceConnectionType || item == _choiceHTTPPing)
			update();
	}
	
	private void update() {
		_form.deleteAll();
		_form.append(_disclaimer);
		_form.append(_choiceConnectionType);
		_form.append(_choiceZLib);
		_form.append(_choiceHTTPPing);
		
		if (Config.isDebugMode()) {
			_form.append(_textResendInterval);
			_form.append(_textReconnectInterval);
		}
	}

	public void commandAction(Command cmd, Displayable disp) {
		if (cmd == _cmdSave) {
			Config.setProtocol(getProtocolValue(_choiceConnectionType.getSelectedIndex()));
			Config.setHTTPPingMode(getPingValue(_choiceHTTPPing.getSelectedIndex()));
			Config.setUseZLib(_choiceZLib.getSelectedIndex() == 0);
			if (Config.isDebugMode()) {
				Config.setResendInterval(Integer.parseInt(_textResendInterval.getString()));
				Config.setReceiveReconnectInterval(Integer.parseInt(_textReconnectInterval.getString()));
			}
			
			Config.writeAll();
			
			App.hideScreen(_form);
		}
		else {
			App.hideScreen(_form);
		}
	}

}
