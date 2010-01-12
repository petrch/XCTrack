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

public class GpsDeviceInfo {

	GpsDriver _driver;
	String _name;
	String _address;

	public GpsDeviceInfo(GpsDriver driver, String name, String address) {
		_driver = driver;
		_name = name;
		_address = address;
	}
	
	public GpsDriver getDriver() {
		return _driver;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getAddress() {
		return _address;
	}
}

