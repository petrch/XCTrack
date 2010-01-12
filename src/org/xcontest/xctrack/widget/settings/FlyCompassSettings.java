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

public final class FlyCompassSettings extends WidgetSettings {
	public class Data {
		public int showLetters;
		public boolean northAtTop;
		public boolean displayWind;
		public boolean displayHeading;
		public boolean displayHeadingPoint;
	}
	
	public static final int AUTO=0;
	public static final int SHOW=1;
	public static final int HIDE=2;
	
	private ChoiceGroup _choiceNorthAtTop;
	private ChoiceGroup _choiceShowLetters;
	private ChoiceGroup _choiceDisplayOptions;
	
	
	
	public Object load(String str) {
		Data d = new Data();
		if (str != null && str.length() == 5) {
			d.showLetters = str.charAt(0)-'0';
			d.northAtTop = str.charAt(1) == 'N';
			d.displayWind = str.charAt(2) == 'W';
			d.displayHeading = str.charAt(3) == 'H';
			d.displayHeadingPoint = str.charAt(4) == 'P';
		}
		else {
			d.showLetters = AUTO;
			d.northAtTop = false;
			d.displayWind = true;
			d.displayHeading = false;
			d.displayHeadingPoint = false;
		}
		return d;
	}
	
	public String save(Object obj) {
		Data d = (Data)obj;
		return "" + d.showLetters +
					(d.northAtTop ? 'N' : ' ') +
					(d.displayWind ? 'W' : ' ') +
					(d.displayHeading ? 'H' : ' ') +
					(d.displayHeadingPoint ? 'P' : ' ');
	}
	
	public void createForm(Vector items, Object obj) {
		Data d = (Data)obj;
		
		_choiceNorthAtTop = new ChoiceGroup("Compass orientation",ChoiceGroup.EXCLUSIVE);
		_choiceNorthAtTop.append("Rotate with current heading", null);
		_choiceNorthAtTop.append("North always at top", null);
		_choiceShowLetters = new ChoiceGroup("Show NESW letters",ChoiceGroup.EXCLUSIVE);
		_choiceShowLetters.append("Auto", null);
		_choiceShowLetters.append("Show always", null);
		_choiceShowLetters.append("Hide", null);
		_choiceDisplayOptions = new ChoiceGroup("Display Options",ChoiceGroup.MULTIPLE);
		_choiceDisplayOptions.append("Show wind arrow", null);
		_choiceDisplayOptions.append("Show heading arrow", null);
		_choiceDisplayOptions.append("Show heading point", null);

		_choiceNorthAtTop.setSelectedIndex(d.northAtTop ? 1 : 0, true);
		_choiceShowLetters.setSelectedIndex(d.showLetters,true);
		_choiceDisplayOptions.setSelectedIndex(0, d.displayWind);
		_choiceDisplayOptions.setSelectedIndex(1, d.displayHeading);
		_choiceDisplayOptions.setSelectedIndex(2, d.displayHeadingPoint);
		
		items.addElement(_choiceNorthAtTop);
		items.addElement(_choiceShowLetters);
		items.addElement(_choiceDisplayOptions);
	}

	public void saveForm(Object obj) {
		Data d = (Data)obj;
		d.northAtTop = _choiceNorthAtTop.getSelectedIndex() == 1;
		d.showLetters = _choiceShowLetters.getSelectedIndex();
		d.displayWind = _choiceDisplayOptions.isSelected(0);
		d.displayHeading = _choiceDisplayOptions.isSelected(1);
		d.displayHeadingPoint = _choiceDisplayOptions.isSelected(2);
	}
}
