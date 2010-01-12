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

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public abstract class GeneralFont {
//	public static final Syste

	public static final GeneralFont[] SystemFonts = new GeneralFont[] {
		new SystemFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL)),
		new SystemFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM)),
		new SystemFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE)),
	};
	
	public static final GeneralFont[] SystemFontsBold = new GeneralFont[] {
		new SystemFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL)),
		new SystemFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM)),
		new SystemFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE)),
	};
	
	// TODO doplnit baseline! ... je to pocitane odshora
	// SansBold sizes 20,30,40,50,60,70,80
	public static final GeneralFont[] NumberFonts = new GeneralFont[] {
		new ImageFont("/fonts/sans20.png","0123456789.:-+°'NESW",24,0,new int[]{14,14,14,14,14,14,14,14,14,14,8,8,8,17,10,6,17,14,14,22}),
		new ImageFont("/fonts/sans30.png","0123456789.:-+°'NESW",36,0,new int[]{21,21,21,21,21,21,21,21,21,21,11,12,12,25,15,9,25,21,22,33}),
		new ImageFont("/fonts/sans40.png","0123456789.:-+°'NESW",48,0,new int[]{28,28,28,28,28,28,28,28,28,28,15,16,17,34,20,12,33,27,29,44}),
		new ImageFont("/fonts/sans50.png","0123456789.:-+°'NESW",59,0,new int[]{35,35,35,35,35,35,35,35,35,35,19,20,21,42,25,15,42,34,36,55}),
		new ImageFont("/fonts/sans60.png","0123456789.:-+°'NESW",71,0,new int[]{42,42,42,42,42,42,42,42,42,42,23,24,25,50,30,18,50,41,43,66}),
		new ImageFont("/fonts/sans70.png","0123456789.:-+°'NESW",82,0,new int[]{49,49,49,49,49,49,49,49,49,49,27,28,29,59,35,21,59,48,50,77}),
		new ImageFont("/fonts/sans80.png","0123456789.:-+°'NESW",94,0,new int[]{56,56,56,56,56,56,56,56,56,56,30,32,33,67,40,25,67,55,58,88}),
	};

	public abstract int getHeight();
	public abstract int substringWidth(String str, int start, int len);
	public abstract void drawString(Graphics g, String str, int x, int y, int align);
	public abstract void drawSubstring(Graphics g, String str, int pos, int len, int x, int y, int align);
}
