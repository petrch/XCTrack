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

import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;

public final class TransparentBox {
	
	public static final int BLACK = 0xC0000000;
	public static final int BLUE = 0xC0000080;
	public static final int WHITE = 0xC0FFFFFF;
	public static final int GRAY = 0xC0808080;
	public static Hashtable _colorRows = new Hashtable();
	
	public static void paint(Graphics g, int x, int y, int w, int h, int argb) {
		/*
		if (App.getDisplay().numAlphaLevels() > 2) {
			int[] arr;
			Integer oargb = new Integer(argb);
			if (!_colorRows.containsKey(oargb) || ((int[])_colorRows.get(oargb)).length < w) {
				arr = new int[w];
				for (int i = 0; i < w; i ++)
					arr[i] = argb;
				_colorRows.put(oargb, arr);
			}
			else {
				arr = (int[])_colorRows.get(oargb);
			}
			
			g.drawRGB(arr, 0, 0, x, y, w, h, true);
		}
		else {
		*/
			g.setColor(argb & 0xffffff);
			g.fillRect(x, y, w, h);
//		}
	}
}
