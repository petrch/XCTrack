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

public class MapWidget extends Widget {
	int _zoom;
	double _latitude;
	double _longitude;
	
	public String getName() {
		return "Map";
	}
	
	public String getTitle() {
		return "Map";
	}
	
	public int getZoom() {
		return _zoom;
	}
	
	protected int getDefaultWidth() {
		return 100;
	}
	
	protected int getDefaultHeight() {
		return 100;
	}
	
	private static double modulo(double a, double b) {
		return a-b*Math.floor(a/b);
	}
		
	public static double convX2Lon(int zoom, double x) {
		int n = 1 << zoom;
		return modulo(x*2*Math.PI/n,2*Math.PI)-Math.PI;
	}
	
	public static double convLon2X(int zoom, double lon) {
		int n = 1 << zoom;
		return modulo((lon+Math.PI)*n/(2*Math.PI),n);
	}
	
	public static double convY2Lat(int zoom, double y) {
		// TODO
		return 0.5;
	}
	
	public static double conLat2Y(int zoom, double lat) {
		// TODO
		return 123;
	}

	
	
	protected void paint(Graphics g, Object[] objSettings) {
		
	}
}
