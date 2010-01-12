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

package org.xcontest.live;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class GpsPoint {
	private static TimeZone _utcTimeZone = TimeZone.getTimeZone("GMT");
	
	public double lon,lat,alt;
	public int year,month,day,hour,min,sec,usec;
	public double time;
	
	public void setTime(long millis) {
		Calendar cal = Calendar.getInstance(_utcTimeZone);
		cal.setTime(new Date(millis));
		year = cal.get(Calendar.YEAR);
		month = 1+cal.get(Calendar.MONTH);
		day = cal.get(Calendar.DAY_OF_MONTH);
		hour = cal.get(Calendar.HOUR_OF_DAY);
		min = cal.get(Calendar.MINUTE);
		sec = cal.get(Calendar.SECOND);
		usec = 1000*cal.get(Calendar.MILLISECOND);
		time = millis/1000.0;
	}
}

