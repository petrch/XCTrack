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

package org.xcontest.xctrack.util;

public class Format {

	public static String dashes(int left, int right) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < left; i ++)
			sb.append('-');
		if (right > 0) {
			sb.append('.');
			for (int i = 0; i < right; i ++)
				sb.append('-');
		}
		return sb.toString();
	}
	
	
	public static String number(double d, int ndecimal) {
		String s;
		if (d < 0) {
			d = -d;
			s = "-";
		}
		else
			s = "";
		
		double fd = Math.floor(d);
		s += (int)fd;
		if (ndecimal > 0) {
			d -= fd;
			s += '.';
			while (ndecimal > 0) {
				d *= 10;
				int x = ((int)d)%10;
				s += x;
				d -= x;
				ndecimal --;
			}
		}
		return s;
	}
	
	public static String number2(int n) {
		if (n < 10)
			return "0"+n;
		else
			return ""+n;
	}
}
