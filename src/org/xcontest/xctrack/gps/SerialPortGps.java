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

public class SerialPortGps extends NMEADriver {

    public SerialPortGps() {
    }
	
	public String getDriverId() {
		return "SERIAL";
	}
	
	public String getName() {
		return "Serial GPS";
	}
	
	public GpsDeviceInfo[] scanDevices() {
		return new GpsDeviceInfo[] {
			new GpsDeviceInfo(this,"AT5: Ericsson HGE-100","comm:AT5;baudrate=9600"),
			new GpsDeviceInfo(this,"COM1","comm:COM1;baudrate=9600"),
			new GpsDeviceInfo(this,"COM2","comm:COM2;baudrate=9600"),
			new GpsDeviceInfo(this,"COM3","comm:COM3;baudrate=9600"),
			new GpsDeviceInfo(this,"COM4","comm:COM4;baudrate=9600"),
			new GpsDeviceInfo(this,"COM5","comm:COM5;baudrate=9600"),
			new GpsDeviceInfo(this,"COM6","comm:COM6;baudrate=9600"),
			new GpsDeviceInfo(this,"COM7","comm:COM7;baudrate=9600"),
			new GpsDeviceInfo(this,"COM8","comm:COM8;baudrate=9600"),
			new GpsDeviceInfo(this,"COM9","comm:COM9;baudrate=9600"),
			new GpsDeviceInfo(this,"COM10","comm:COM10;baudrate=9600"),
		};
	}
}

