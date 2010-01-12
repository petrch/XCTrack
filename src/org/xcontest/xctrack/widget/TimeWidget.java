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

import java.util.Calendar;

import javax.microedition.lcdui.Graphics;

import org.xcontest.xctrack.paint.GeneralFont;
import org.xcontest.xctrack.util.Format;
import org.xcontest.xctrack.widget.settings.TimeDisplaySettings;
import org.xcontest.xctrack.widget.settings.TimeZoneSettings;


public class TimeWidget extends TextBoxWidget {

	private double _smallWHCoef;
	private int _idxSettingsTZ;
	private int _idxSettingsDisplay;
		
	public TimeWidget() {
		super("Time", GeneralFont.NumberFonts, 2);
		
		GeneralFont f = GeneralFont.NumberFonts[0];
		int w1 = f.substringWidth("00:00", 0, 5);
		int w2 = f.substringWidth("00:00:00", 0, 8);
		int h = f.getHeight();
		
		_smallWHCoef = ((double)w1+w2)/(2*h);
		
		_idxSettingsDisplay = addSettings(new TimeDisplaySettings());
		_idxSettingsTZ = addSettings(new TimeZoneSettings());
	}

	protected void paint(Graphics g, Object[] settings) {
		TimeDisplaySettings.Data dispSettings = (TimeDisplaySettings.Data)settings[_idxSettingsDisplay];
		TimeZoneSettings.Data tzSettings = (TimeZoneSettings.Data)settings[_idxSettingsTZ];
		Calendar cal = Calendar.getInstance(tzSettings.timezone);
		int w = getWidth(g,settings);
		int h = getHeight(g,settings);
		boolean hhmmss;
		
		if (dispSettings.displayFormat == TimeDisplaySettings.AUTO)
			hhmmss = w >= h*_smallWHCoef;
		else
			hhmmss = dispSettings.displayFormat == TimeDisplaySettings.HHMMSS;
		
		if (hhmmss)
			super.paint(g, settings, Format.number2(cal.get(Calendar.HOUR_OF_DAY)) + ":" +
					Format.number2(cal.get(Calendar.MINUTE)) + ":" +
					Format.number2(cal.get(Calendar.SECOND)), 0);
		else
			super.paint(g, settings, Format.number2(cal.get(Calendar.HOUR_OF_DAY)) + ":" +
					Format.number2(cal.get(Calendar.MINUTE)), 1);
	}
	
	public String getName() {
		return "Time";
	}
}
