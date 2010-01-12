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



package org.xcontest.xctrack.paint;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Painter {
	public static void drawBackground(Graphics g, Image img, int x, int y, int w, int h, int anchor) {
		int xoff,yoff;
		int imgw = img.getWidth();
		int imgh = img.getHeight();
		int clipX = g.getClipX();
		int clipY = g.getClipY();
		int clipW = g.getClipWidth();
		int clipH = g.getClipHeight();
		
		if ((anchor & Graphics.RIGHT) != 0)
			xoff = w-imgw;
		else if ((anchor & Graphics.HCENTER) != 0)
			xoff = w-imgw/2;
		else
			xoff = 0;
		
		if ((anchor & Graphics.TOP) != 0)
			yoff = h-imgh;
		else if ((anchor & Graphics.VCENTER) != 0)
			yoff = (h-imgh)/2;
		else
			yoff = 0;
		
		if (xoff > 0) xoff = (xoff%imgw)-imgw;
		if (yoff > 0) yoff = (yoff%imgh)-imgh;
		if (xoff <= -imgw) xoff = -((-xoff)%imgw);			// stupid % implementation... ruby rules
		if (yoff <= -imgh) yoff = -((-yoff)%imgh);			// stupid % implementation... ruby rules
		
		g.setClip(x, y, w, h);
		
		for (int i = yoff; i < h; i += imgh) {
			for (int j = xoff; j < w; j += imgw) {
				g.drawImage(img, x+j, y+i, Graphics.LEFT|Graphics.TOP);
			}
		}
		
		g.setClip(clipX, clipY, clipW, clipH);
	}
}




