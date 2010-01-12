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


public class CustomNMEAGps extends NMEADriver {

    public CustomNMEAGps() {
    }
	
	public String getDriverId() {
		return "CUSTOM NMEA";
	}
	
	public String getName() {
		return "Custom GPS connection";
	}
	
	public GpsDeviceInfo[] scanDevices() {
		return new GpsDeviceInfo[]{
			new GpsDeviceInfo(this,"socket://127.0.0.1:20175","socket://127.0.0.1:20175")
		};
	}
	
}

