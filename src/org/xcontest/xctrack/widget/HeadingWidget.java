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

package org.xcontest.xctrack.widget;

import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Graphics;

import org.xcontest.xctrack.paint.GeneralFont;
import org.xcontest.xctrack.util.Format;
import org.xcontest.xctrack.widget.settings.DataSourceSettings;
import org.xcontest.xctrack.widget.settings.WidgetSettings;

public class HeadingWidget extends TextBoxWidget {

	private class Settings extends WidgetSettings {

		private class Data {
			boolean letters;
		}
		
		private ChoiceGroup _choiceLetters;
		
		public Object load(String str) {
			Data d = new Data();
			if (str == null || str.length() == 0)
				d.letters = false;
			else
				d.letters = str.charAt(0) == 'L';
			return d;
		}

		public String save(Object obj) {
			Data d = (Data)obj;
			return d.letters ? "L" : " ";
		}

		public void createForm(Vector items, Object obj) {
			Data d = (Data)obj;
			_choiceLetters = new ChoiceGroup("Display",ChoiceGroup.EXCLUSIVE);
			_choiceLetters.append("Degrees", null);
			_choiceLetters.append("Letters N-E-S-W", null);
			
			_choiceLetters.setSelectedIndex(d.letters ? 1 : 0, true);
			
			items.addElement(_choiceLetters);
		}

		public void saveForm(Object obj) {
			Data d = (Data)obj;
			d.letters = _choiceLetters.getSelectedIndex() == 1;
		}
		
	}
	
	private static final String[] _letters = new String[]{"N","N-NE","NE","NE-E","E","E-SE","SE","SE-S","S","S-SW","SW","SW-W","W","W-NW","NW","NW-N"};
	private int _idxSettings;
	private int _idxHeadingSource;
	
	public HeadingWidget() {
		super("Heading", GeneralFont.NumberFonts, 1);
		setAlignment(Graphics.HCENTER | Graphics.VCENTER);
		_idxSettings = addSettings(new Settings());
		_idxHeadingSource = addSettings(new DataSourceSettings(DataSourceSettings.HEADING));
	}

	protected void paint(Graphics g, Object[] objSettings) {
		Settings.Data d = (Settings.Data)objSettings[_idxSettings];
		DataSourceSettings.Data headingSource = (DataSourceSettings.Data)objSettings[_idxHeadingSource];
		double heading = WidgetInfo.getHeading(headingSource);
		String text;
		
		if (!Double.isNaN(heading)) {
			if (d.letters) {
				text = _letters[((4*(int)heading+45)/90)%16];
			}
			else
				text = Format.number(heading,0)+"°";
		}
		else
			text = "---";
		super.paint(g, objSettings, text, 0);
	}

	public String getName() {
		return "Heading";
	}
}
