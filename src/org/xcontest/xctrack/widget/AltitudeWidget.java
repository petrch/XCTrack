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

import org.xcontest.xctrack.info.LocationInfoResult;
import org.xcontest.xctrack.paint.GeneralFont;
import org.xcontest.xctrack.util.Format;
import org.xcontest.xctrack.widget.settings.PrecisionSettings;

public class AltitudeWidget extends TextBoxWidget {

	private int _idxPrecision;
	
	public AltitudeWidget() {
		super("Altitude [m]", GeneralFont.NumberFonts, 1);
		_idxPrecision = addSettings(new PrecisionSettings(2,0));
	}

	protected void paint(Graphics g, Object[] settings) {
		PrecisionSettings.Data prec = (PrecisionSettings.Data)settings[_idxPrecision];
		LocationInfoResult loc = WidgetInfo.getLocation();
		String text;
		if (loc.hasAltitude && loc.age < 20)
			text = Format.number(loc.altitude, prec.precision);
		else
			text = Format.dashes(4, prec.precision);
		super.paint(g, settings, text, 0);
	}

	public String getName() {
		return "Altitude";
	}
}
