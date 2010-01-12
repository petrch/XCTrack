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

package org.xcontest.xctrack.info;

public class LocationInfoResult {
	public double time;

	public boolean hasTwoPoints;
	public double speed;
	public double heading;
	
	public double gpsSpeed;	// NaN if not available
	public double gpsHeading;	// NaN if not available
	
	public boolean hasAltitude;
	public double altitude;
	
	public boolean hasVerticalSpeed;
	public double verticalSpeed;
	
	public boolean hasPosition;
	public double lon;
	public double lat;
	public double age;
	
	public boolean hasWind;
	public double windDirection;
	public double windSpeed;
	public double windPrecision;
	public double windAvgSpeed;
	public double windAvgDirection;
}

