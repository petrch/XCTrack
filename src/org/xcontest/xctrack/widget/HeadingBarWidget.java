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

import java.io.IOException;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.xcontest.xctrack.util.Log;
import org.xcontest.xctrack.widget.settings.DataSourceSettings;

public class HeadingBarWidget extends Widget {

	private Image _imgNoSignal;
	private Image _imgHeading;
	private Image _imgPointer;
	int _iconSize;
	int _idxHeadingSource;
	
	public HeadingBarWidget() {
		_iconSize = 0;
		_idxHeadingSource = addSettings(new DataSourceSettings(DataSourceSettings.HEADING));
	}
	
	protected int getDefaultWidth() { return 2000; }
	protected int getDefaultHeight() { return 24; }

	private void LoadImages(int h) {
		int size;
		
		if (h >= 40) size = 40;
		else if (h >= 32) size = 32;
		else size = 24;
		
		if (size != _iconSize) {
			try {
				_imgNoSignal = Image.createImage("/img/heading/heading"+size+"_nosignal.png");
				_imgHeading = Image.createImage("/img/heading/heading"+size+".png");
				_imgPointer = Image.createImage("/img/heading/heading"+size+"_pointer.png");
				_iconSize = size;
			}
			catch (IOException e) {
				Log.error("HeadingWidget: Cannot load icons");
			}
		}
	}
	
	public void paint(Graphics g, Object[] objSettings) {
		DataSourceSettings.Data headingSource = (DataSourceSettings.Data)objSettings[_idxHeadingSource];
		double heading = WidgetInfo.getHeading(headingSource);

		int w = g.getClipWidth();
		int h = g.getClipHeight();
		
		LoadImages(h);

		int x = g.getClipX();
		int y = g.getClipY() + (g.getClipHeight()-_iconSize)/2;

		g.setColor(0);
		g.fillRect(x,y,w,h);
		
		if (!Double.isNaN(heading)) {
			int iw = _imgHeading.getWidth();
			int offset = (iw-(int)(iw*heading/360)+w/2)%iw - iw;
			for (int i = offset; i < w; i += iw)
				g.drawImage(_imgHeading, x+i, y, Graphics.LEFT | Graphics.TOP);
			g.drawImage(_imgPointer, x+(w-_imgPointer.getWidth())/2, y, Graphics.LEFT | Graphics.TOP);
		}
		else {
			int iw = _imgNoSignal.getWidth();
			for (int i = 0; i < w; i += iw)
				g.drawImage(_imgNoSignal, x+i, y, Graphics.LEFT | Graphics.TOP);
		}
	}

	public String getName() {
		return "HeadingBar";
	}
}
