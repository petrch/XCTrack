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

package org.xcontest.xctrack.gps;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public final class GpsMessage {
	private static final TimeZone _utcTimezone = TimeZone.getTimeZone("GMT");
	private final Calendar _calDate;
	
	public boolean hasAltitude;
	public double altitude;		// altitude in meters
	
	public boolean hasPosition;
	public double lat;		// latitude in degrees
	public double lon;		// longitude in degrees
	
	public boolean hasPrecision;
	public double precision;
	
	public boolean hasTime;
	public long time;
	
	public boolean hasSatellites;
	public int nsatellites;
	
	public boolean hasHeadingSpeed;
	public double heading;
	public double speed;
	
	public GpsMessage() {
		_calDate = Calendar.getInstance(_utcTimezone);
		_calDate.setTime(new Date());
		_calDate.set(Calendar.HOUR_OF_DAY, 0);
		_calDate.set(Calendar.MINUTE, 0);
		_calDate.set(Calendar.SECOND, 0);
		_calDate.set(Calendar.MILLISECOND, 0);
	}
	
	public void reset() {
		hasTime = false;
		hasPosition = false;
		hasAltitude = false;
		hasSatellites = false;
		hasHeadingSpeed = false;
		hasPrecision = false;
	}
	
	public void setTime(int year, int month, int day, int millis) {
		_calDate.set(Calendar.YEAR, year);
		_calDate.set(Calendar.MONTH, month-1);
		_calDate.set(Calendar.DAY_OF_MONTH, day);
		hasTime = true;
		time = _calDate.getTime().getTime()+millis;
	}
	
	public void setTime(long millis) {
		hasTime = true;
		time = millis;
	}
	
	public void setPosition(double lon, double lat) {
		hasPosition = true;
		this.lon = lon;
		this.lat = lat;
	}
	
	public void setAltitude(double alt) {
		this.hasAltitude = true;
		this.altitude = alt;
	}
	
	public void setSatellites(int n) {
		this.hasSatellites = true;
		this.nsatellites = n;
	}
	
	public void setHeadingSpeed(double heading, double speed) {
		this.hasHeadingSpeed = true;
		this.heading = heading;
		this.speed = speed;
	}
	
	public void setPrecision(double prec) {
		this.hasPrecision = true;
		this.precision = prec;
	}
}

