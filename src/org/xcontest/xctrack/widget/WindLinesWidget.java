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

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import org.xcontest.xctrack.info.InfoCenter;
import org.xcontest.xctrack.info.LocationInfo;
import org.xcontest.xctrack.info.LocationInfoResult;
import org.xcontest.xctrack.util.Format;

public class WindLinesWidget extends Widget {

	private int _x,_y,_w,_h;
	private double _px;
	
	public WindLinesWidget() {
	}
	
	protected int getDefaultHeight() {
		return 200;
	}

	protected int getDefaultWidth() {
		return 2000;
	}

	public String getName() {
		return "WindLines";
	}

	private int getX(double speedx) {
		return (int)Math.floor(0.5 + _x + _w/2.0 + _px*speedx); 
	}

	private int getY(double speedy) {
		return (int)Math.floor(0.5 + _y + _h/2.0 - _px*speedy);
	}
	
	protected void paint(Graphics g, Object[] objSettings) {
		LocationInfo li = InfoCenter.getInstance().getLocationInfo();
		LocationInfoResult loc = WidgetInfo.getLocation();
		double[] points = li.getWindPoints();
		double[] lines = li.getWindLines();
		double scale = 20;
		double xscale,yscale;
		
		_x = g.getClipX();
		_y = g.getClipY();
		_h = g.getClipHeight();
		_w = g.getClipWidth();
		_px = (_w < _h ? _w : _h)/(2*scale);
		xscale = _w/(2*_px);
		yscale = _h/(2*_px);
		
		g.setColor(0);
		g.fillRect(_x, _y, _w, _h);

		if (loc.hasWind) {
			g.setColor(0xC0C000);
			double windX = loc.windAvgSpeed*Math.sin(loc.windAvgDirection*Math.PI/180+Math.PI);
			double windY = loc.windAvgSpeed*Math.cos(loc.windAvgDirection*Math.PI/180+Math.PI);
			g.fillArc(getX(windX)-5, getY(windY)-5, 11, 11, 0, 360);
		}
		
		g.setColor(0xffffff);
		g.drawLine(getX(0), _y, getX(0), _y+_h);
		g.drawLine(_x, getY(0), _x+_w, getY(0));
		
		g.setColor(0xC0ffff);
		for (int i = 0; i < lines.length; i += 3) {
			double a = lines[i];
			double b = lines[i+1];
			double c = lines[i+2];
			if (Math.abs(a) > Math.abs(b))
				g.drawLine(getX((-b*(-yscale)-c)/a), getY(-yscale), getX((-b*yscale-c)/a), getY(yscale));
			else
				g.drawLine(getX(-xscale), getY((-a*(-xscale)-c)/b), getX(xscale), getY((-a*xscale-c)/b));
		}
		
		g.setColor(0xA060FF);
		for (int i = 0; i < points.length; i += 2) {
			g.fillArc(getX(points[i])-2, getY(points[i+1])-2, 5, 5, 0, 360);
		}
		
		if (loc.hasWind) {
			g.setColor(0xff0000);
			double windX = loc.windSpeed*Math.sin(loc.windDirection*Math.PI/180+Math.PI);
			double windY = loc.windSpeed*Math.cos(loc.windDirection*Math.PI/180+Math.PI);
			g.fillArc(getX(windX)-2, getY(windY)-2, 5, 5, 0, 360);
			g.setColor(0xFFFFFF);
			g.setFont(Font.getDefaultFont());
			g.drawString("Precision: "+Format.number(loc.windPrecision, 1),_x,_y,Graphics.LEFT|Graphics.TOP);
		}
	}
}



