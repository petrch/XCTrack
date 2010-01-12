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

import org.xcontest.xctrack.paint.GeneralFont;

public class MemoryWidget extends TextBoxWidget {

	public MemoryWidget() {
		super("Memory", GeneralFont.SystemFonts, 1);
	}

	protected int getDefaultHeight() {
		return 7*super.getDefaultHeight()/6;
	}
	
	protected int getDefaultWidth() {
		return 4*super.getDefaultWidth()/3;
	}
	
	private String formatBytes(long n) {
		if (n < 10*1024) {
			return ""+n/1024+"."+((10*n/1024)%10)+"Kb";
		}
		else if (n < 1024*1024) {
			return ""+n/1024+"Kb";
		}
		else if (n < 10*1024*1024) {
			return ""+n/(1024*1024)+"."+((10*n/(1024*1024))%10)+"Mb";
		}
		else {
			return ""+n/(1024*1024)+"Mb";
		}
	}
	
	protected void paint(Graphics g, Object[] objSettings) {
		Runtime r = Runtime.getRuntime();
		String text = "Free: " + formatBytes(r.freeMemory()) + "\nTotal:" + formatBytes(r.totalMemory());
		super.paint(g, objSettings, text, 0);
	}

	public String getName() {
		return "Memory";
	}
}
