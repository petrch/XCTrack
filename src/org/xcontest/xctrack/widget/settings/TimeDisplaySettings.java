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

public final class TimeDisplaySettings extends WidgetSettings {
	
	public class Data {
		public int displayFormat;
	}

	// numbers correspond to the ChoiceGroup items indexes
	public static final int AUTO = 0;
	public static final int HHMM = 1;
	public static final int HHMMSS = 2;

	private ChoiceGroup _choiceDisplayFormat;
	
	public Object load(String str) {
		Data d = new Data();
		if (str == null)
			d.displayFormat = AUTO;
		else
			d.displayFormat = Integer.parseInt(str);
		return d;
	}

	public String save(Object obj) {
		Data d = (Data)obj;
		return ""+d.displayFormat;
	}

	public void createForm(Vector items, Object obj) {
		Data d = (Data)obj;

		_choiceDisplayFormat = new ChoiceGroup("Time Format", ChoiceGroup.EXCLUSIVE);
		_choiceDisplayFormat.append("Automatic", null);
		_choiceDisplayFormat.append("HH:MM", null);
		_choiceDisplayFormat.append("HH:MM:SS", null);
		
		_choiceDisplayFormat.setSelectedIndex(d.displayFormat, true);
		
		items.addElement(_choiceDisplayFormat);
	}

	public void saveForm(Object obj) {
		Data d = (Data)obj;
		d.displayFormat = _choiceDisplayFormat.getSelectedIndex();
	}
}
