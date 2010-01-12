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

import org.xcontest.xctrack.info.LocationInfoResult;
import org.xcontest.xctrack.paint.GeneralFont;
import org.xcontest.xctrack.paint.TextPainter;
import org.xcontest.xctrack.util.Format;
import org.xcontest.xctrack.widget.settings.DataSourceSettings;
import org.xcontest.xctrack.widget.settings.FlyCompassSettings;


public class FlyCompassWidget extends Widget {

	private static final int COLOR_WIND=0xFFFF80;
	private static final int COLOR_HEADING=0x6080FF;
	private static final int COLOR_HEADING_POINT_OUTER=0xFFFFFF;
	private static final int COLOR_HEADING_POINT_INNER=0xC0D0FF;
	private static final int COLOR_LETTERS=0xFFFFFF;
	
	private Font _font;
	private TextPainter _textPainter;
	private int _idxSettings;
	private int _idxHeadingSource;
	
	public FlyCompassWidget() {
		_font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
		_textPainter = new TextPainter(GeneralFont.NumberFonts,1);
		_idxSettings = addSettings(new FlyCompassSettings());
		_idxHeadingSource = addSettings(new DataSourceSettings(DataSourceSettings.HEADING));
	}
	
	protected int getDefaultHeight() {
		return 300;
	}

	protected int getDefaultWidth() {
		return 300;
	}

	public String getName() {
		return "FlyCompass";
	}

	private static final int getX(int centerX, double angle, double radius) {
		return centerX+(int)Math.floor(radius*Math.sin(angle*Math.PI/180)); 
	}

	private static final int getY(int centerY, double angle, double radius) {
		return centerY-(int)Math.floor(radius*Math.cos(angle*Math.PI/180)); 
	}
	
	private static void paintArrow(Graphics g, int color, int centerx, int centery, double angle, double radius) {
		g.setColor(color);
		
		g.fillTriangle(getX(centerx,angle,radius), getY(centery,angle,radius),
				getX(centerx,angle+28,radius*0.72), getY(centery,angle+28,radius*0.72),
				getX(centerx,angle-28,radius*0.72), getY(centery,angle-28,radius*0.72));

		g.fillTriangle(getX(centerx,angle+172,radius), getY(centery,angle+172,radius),
				getX(centerx,angle-9,radius*0.72), getY(centery,angle-9,radius*0.72),
				getX(centerx,angle+9,radius*0.72), getY(centery,angle+9,radius*0.72));

		g.fillTriangle(getX(centerx,angle-9,radius*0.72), getY(centery,angle-9,radius*0.72),
				getX(centerx,angle+172,radius), getY(centery,angle+172,radius),
				getX(centerx,angle-172,radius), getY(centery,angle-172,radius));
	}
	
	protected void paint(Graphics g, Object[] obj) {
		FlyCompassSettings.Data s = (FlyCompassSettings.Data)obj[_idxSettings];
		DataSourceSettings.Data headingSource = (DataSourceSettings.Data)obj[_idxHeadingSource];
		LocationInfoResult loc = WidgetInfo.getLocation();
		double heading = WidgetInfo.getHeading(headingSource);
		int centerx,centery;
		int radius;
		int headingPointRadius=0;
		int x,y,w,h;
		int fonth;
		boolean showNESW;
		boolean hasRotation;
		double rotation;
		
		x = g.getClipX();
		y = g.getClipY();
		w = g.getClipWidth();
		h = g.getClipHeight();
		fonth = _font.getHeight();
		centerx = x + w/2;
		centery = y + h/2;
		radius = (w < h ? w : h)/2;
		if (s.showLetters == FlyCompassSettings.HIDE)
			showNESW = false;
		else if (s.showLetters == FlyCompassSettings.SHOW)
			showNESW = true;
		else
			showNESW = radius > 4*fonth;
		if (showNESW)
			radius -= fonth;
		if (s.displayHeadingPoint) {
			headingPointRadius = radius/7;
			if (headingPointRadius < 5) headingPointRadius = 5;
			if (!showNESW)
				radius -= headingPointRadius/2;
		}

		if (s.northAtTop) {
			hasRotation = true;
			rotation = 0;
		}
		else {
			hasRotation = !Double.isNaN(heading);
			rotation = heading;
		}

		g.setColor(0xffffff);
		g.drawArc(centerx-radius, centery-radius, 2*radius-1, 2*radius-1, 0, 360);
		if (hasRotation) {
			for (int i = 0; i < 360; i += 10) {
				int r = i % 30 == 0 ? radius*88/100 : radius*95/100;
				g.drawLine(getX(centerx,i-rotation,r), getY(centery,i-rotation,r), getX(centerx,i-rotation,radius), getY(centery,i-rotation,radius));
			}
			if (showNESW) {
				g.setFont(_font);
				g.setColor(COLOR_LETTERS);
				g.drawString("N",getX(centerx,0-rotation,radius+fonth/2),getY(centery,0-rotation,radius+fonth/2)-fonth/2,Graphics.HCENTER|Graphics.TOP);
				g.drawString("E",getX(centerx,90-rotation,radius+fonth/2),getY(centery,90-rotation,radius+fonth/2)-fonth/2,Graphics.HCENTER|Graphics.TOP);
				g.drawString("S",getX(centerx,180-rotation,radius+fonth/2),getY(centery,180-rotation,radius+fonth/2)-fonth/2,Graphics.HCENTER|Graphics.TOP);
				g.drawString("W",getX(centerx,270-rotation,radius+fonth/2),getY(centery,270-rotation,radius+fonth/2)-fonth/2,Graphics.HCENTER|Graphics.TOP);
			}
			
			if (s.displayWind && loc.hasWind) {
				paintArrow(g,COLOR_WIND,centerx,centery,loc.windAvgDirection + 180 - rotation,radius);
			}

			if (!Double.isNaN(heading) && s.displayHeading) {
				paintArrow(g,COLOR_HEADING,centerx,centery,heading-rotation,radius);
			}
			
			if (s.displayWind && loc.hasWind) {
				g.setColor(0);
				int r = radius*60/100;
				int size = (int)(r/1.3);
				g.fillArc(centerx-r, centery-r, 2*r, 2*r, 0, 360);

				g.setColor(COLOR_WIND);
				_textPainter.paint(g, Format.number(loc.windAvgSpeed, 1), 0, centerx-size, centery-size, 2*size, 2*size, Graphics.HCENTER | Graphics.VCENTER);
			}
			
			if (!Double.isNaN(heading) && s.displayHeadingPoint) {
				int r = headingPointRadius;
				g.setColor(COLOR_HEADING_POINT_OUTER);
				g.fillArc(getX(centerx-r,heading-rotation,radius-headingPointRadius/2),
						getY(centery-r,heading-rotation,radius-headingPointRadius/2),
						r*2, r*2, 0, 360);
				int border = r/5;
				if (border != 2) border = 2;
				r = headingPointRadius-border;
				g.setColor(COLOR_HEADING_POINT_INNER);
				g.fillArc(getX(centerx-r,heading-rotation,radius-headingPointRadius/2),
						getY(centery-r,heading-rotation,radius-headingPointRadius/2),
						r*2, r*2, 0, 360);
			}
			
		}
	}
}



