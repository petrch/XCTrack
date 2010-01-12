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

public class LocationWidget extends TextBoxWidget {

	public LocationWidget() {
		super("Location",GeneralFont.SystemFonts,1);
	}
	
	
	protected int getDefaultHeight() {
		return 7*super.getDefaultHeight()/6;
	}
	
	protected int getDefaultWidth() {
		return 4*super.getDefaultWidth()/3;
	}
	
	protected void paint(Graphics g, Object[] objSettings) {
		LocationInfoResult loc = WidgetInfo.getLocation();
		String text;
		if (loc.hasPosition)
			text = "Lat: "+formatLonLat(loc.lat,"S","N")+"\nLon: "+formatLonLat(loc.lon,"W","E");
		else
			text = "Lat:\nLon:";
		super.paint(g, objSettings, text, 0);
	}
	
	public String getName() {
		return "Location";
	}

	private static String formatLonLat(double x, String neg, String pos) {
		int sec = (int)Math.floor(x*60*60);
		String sign;
		
		if (sec < 0) {
			sign = neg;
			sec = -sec;
		}
		else
			sign = pos;
		
		return sign+(sec/3600)+"°"+Format.number2((sec/60)%60)+"'"+Format.number2(sec%60)+"''";
	}

}
