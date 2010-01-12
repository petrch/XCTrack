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

import org.xcontest.xctrack.paint.GeneralFont;


public class TextPainter {
	
	private GeneralFont[] _fonts;
	private int[][] _maxWidths;
	private int[][] _maxHeights;
	
	public TextPainter(GeneralFont[] fonts, int nvariants) {
		_fonts = fonts;
		_maxWidths = new int[nvariants][];
		_maxHeights = new int[nvariants][];
		for (int i = 0; i < nvariants; i ++) {
			_maxWidths[i] = new int[fonts.length];
			_maxHeights[i] = new int[fonts.length];
			for (int j = 0; j < fonts.length; j ++) {
				_maxWidths[i][j] = 0;
				_maxHeights[i][j] = 0;
			}
		}
	}
	
	private static final int getLinesCount(String text) {
		int lastpos = -1;
		int newpos;
		int cnt = 1;
		while ((newpos=text.indexOf('\n',lastpos+1))>=0) {
			cnt += 1;
			lastpos = newpos;
		}
		return cnt;
	}
	
	private final int getFontIndex(String text, int variant, int winw, int winh) {
		int[] maxw = _maxWidths[variant];
		int[] maxh = _maxHeights[variant];
		
		// start from the biggest font and search for the first one small enough
		for (int i = _fonts.length-1; i >= 0; i --) {
			if (maxw[i] <= winw && maxh[i] <= winh) {
				GeneralFont f = _fonts[i];
				int lastpos = -1;
				int newpos;
				int width = 0;
				int height = f.getHeight();
				while ((newpos=text.indexOf('\n',lastpos+1))>=0) {
					int linew = f.substringWidth(text, lastpos+1,newpos-lastpos-1);
					if (linew > width)
						width = linew;
					height += f.getHeight();
					lastpos = newpos;
				}
				int linew = f.substringWidth(text, lastpos+1,text.length()-lastpos-1);
				if (linew > width)
					width = linew;
				
				if (width > maxw[i]) maxw[i] = width;
				if (height > maxh[i]) maxh[i] = height;
				
				if (width <= winw && height <= winh)	// vejdeme se
					return i;
			}
		}
		
		return 0;	// all the fonts are too large, use the smallest one
	}
	
	public final void paint(Graphics g, String text, int variant, int x, int y, int w, int h, int align) {
		GeneralFont f = _fonts[getFontIndex(text,variant,w,h)];
		
		int lastpos = -1;
		int newpos;
		int alOffsetX,alOffsetY;
		int nlines = getLinesCount(text);
		int drawStringAlignment = Graphics.TOP;
		
		if ((align & Graphics.HCENTER) != 0) { alOffsetX = w/2; drawStringAlignment |= Graphics.HCENTER; }
		else if ((align & Graphics.RIGHT) != 0) { alOffsetX = w;  drawStringAlignment |= Graphics.RIGHT; }
		else { alOffsetX = 0; drawStringAlignment |= Graphics.LEFT; }
		
		if ((align & Graphics.VCENTER) != 0) alOffsetY = (h-f.getHeight()*nlines)/2;
		else if ((align & Graphics.BOTTOM) != 0) alOffsetY = h-f.getHeight()*nlines;
		else alOffsetY = 0;
		
		y += alOffsetY;
		x += alOffsetX;
		while ((newpos=text.indexOf('\n',lastpos+1))>=0) {
			f.drawSubstring(g, text, lastpos+1,newpos-lastpos-1, x, y, drawStringAlignment);
			y += f.getHeight();
			lastpos = newpos;
		}
		f.drawSubstring(g, text, lastpos+1, text.length()-lastpos-1, x, y, drawStringAlignment);
	}
}


