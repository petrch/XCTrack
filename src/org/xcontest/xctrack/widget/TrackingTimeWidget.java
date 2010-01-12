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

import javax.microedition.lcdui.Graphics;

import org.xcontest.xctrack.info.InfoCenter;
import org.xcontest.xctrack.paint.GeneralFont;
import org.xcontest.xctrack.util.Format;
import org.xcontest.xctrack.widget.settings.TimeDisplaySettings;

public class TrackingTimeWidget extends TextBoxWidget {

	private double _smallWHCoef;
	private int _settingsIdx;
		
	public TrackingTimeWidget() {
		super("Tracking", GeneralFont.NumberFonts, 2);
		
		GeneralFont f = GeneralFont.NumberFonts[0];
		int w1 = f.substringWidth("00:00", 0, 5);
		int w2 = f.substringWidth("00:00:00", 0, 8);
		int h = f.getHeight();
		
		_smallWHCoef = ((double)w1+w2)/(2*h);
		
		_settingsIdx = addSettings(new TimeDisplaySettings());
	}

	protected void paint(Graphics g, Object[] objSettings) {
		TimeDisplaySettings.Data s = (TimeDisplaySettings.Data)objSettings[_settingsIdx];
		int t = (int)InfoCenter.getInstance().getLiveInfo().getTrackingTime();
		int w = getWidth(g,objSettings);
		int h = getHeight(g,objSettings);
		boolean hhmmss;
		
		if (s.displayFormat == TimeDisplaySettings.AUTO)
			hhmmss = w >= h*_smallWHCoef;
		else
			hhmmss = s.displayFormat == TimeDisplaySettings.HHMMSS;
		
		
		if (hhmmss) {
			super.paint(g, objSettings, Format.number2(t/3600) + ":" +
							Format.number2((t/60)%60) + ":" +
							Format.number2(t%60), 0);
		}
		else {
			super.paint(g, objSettings, Format.number2(t/3600) + ":" +
							Format.number2((t/60)%60), 1);
		}
	}

	public String getName() {
		return "TrackingTime";
	}
}
