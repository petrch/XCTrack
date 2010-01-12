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


public class WindWidget extends TextBoxWidget {
	
	int _idxPrecision;
	
	public WindWidget() {
		super("Wind",GeneralFont.SystemFonts,1);
		setAlignment(Graphics.LEFT | Graphics.VCENTER);
		_idxPrecision = addSettings(new PrecisionSettings(3,1));
	}
	
	
	protected int getDefaultHeight() {
		return 7*super.getDefaultHeight()/6;
	}
	
	protected int getDefaultWidth() {
		return 4*super.getDefaultWidth()/3;
	}
	
	protected void paint(Graphics g, Object[] settings) {
		PrecisionSettings.Data prec = (PrecisionSettings.Data)settings[_idxPrecision];
		LocationInfoResult loc = WidgetInfo.getLocation();
		String text;
		if (loc.hasWind)
			text = "Direction: "+Format.number(loc.windAvgDirection,0)+"\nSpeed [m/s]: "+Format.number(loc.windAvgSpeed,prec.precision);
		else
			text = "Direction: ---\nSpeed: "+Format.dashes(2, prec.precision);
		super.paint(g, settings, text, 0);
	}
	
	public String getName() {
		return "Wind";
	}

}
