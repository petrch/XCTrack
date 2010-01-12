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

package org.xcontest.xctrack.widget.settings;

import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.TextField;

public final class DataSourceSettings extends WidgetSettings {
	public class Data {
		public boolean gpsSource;
		public int averaging;
	}
	
	public static final int SPEED=1;
	public static final int HEADING=2;
	public static final int VARIO=3;
	
	private ChoiceGroup _choiceSource;
	private TextField _textAveraging;
	private int _type;
	
	public DataSourceSettings(int type) {
		_type = type;
	}
	
	public Object load(String str) {
		Data d = new Data();
		if (str == null || str.length() < 2) {
			d.gpsSource = false;
			d.averaging = 0;
		}
		else {
			d.gpsSource = str.charAt(0) == 'G';
			d.averaging = Integer.parseInt(str.substring(1));
		}
		return d;
	}

	public String save(Object obj) {
		Data d = (Data)obj;
		return (d.gpsSource ? "G" : " ") + d.averaging;
	}
	

	public void createForm(Vector items, Object obj) {
		Data d = (Data)obj;
		
		if (_type == VARIO) {
			_textAveraging = new TextField("Vario averaging (sec)", "", 8, TextField.NUMERIC);
			_textAveraging.setString(""+d.averaging);
			items.addElement(_textAveraging);
		}
		else {
			String title = _type == SPEED ? "Speed Data" : "Heading Data";
			_choiceSource = new ChoiceGroup(title,ChoiceGroup.EXCLUSIVE);
			_choiceSource.append("From GPS device", null);
			_choiceSource.append("Computed from position", null);
			_textAveraging = new TextField("Computed location averaging (sec)", "", 8, TextField.NUMERIC);
			
			_choiceSource.setSelectedIndex(d.gpsSource ? 0 : 1, true);
			_textAveraging.setString(""+d.averaging);
			
			items.addElement(_choiceSource);
			items.addElement(_textAveraging);
		}
	}

	public String validateForm() {
		if (Integer.parseInt(_textAveraging.getString()) < 0)
			return "Invalid averaging delay value. Please enter 0 or positive number of seconds";
		else
			return null;
	}

	public void saveForm(Object obj) {
		Data d = (Data)obj;
		d.averaging = Integer.parseInt(_textAveraging.getString());
		if (_type == VARIO)
			d.gpsSource = false;
		else
			d.gpsSource = _choiceSource.getSelectedIndex() == 0;
	}
}


