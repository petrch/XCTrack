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
import java.util.Vector;

import org.xcontest.xctrack.gps.GpsMessage;
import org.xcontest.xctrack.util.Log;
import org.xcontest.xctrack.util.ValueForm;

public abstract class GpsDriver {	
	/// in seconds
	protected int GPS_POSITION_MAXAGE = 5;
	
	private static GpsDriver[] _allDrivers = null;

	private GpsListener _listener; 
	private long _lastPositionTime;
	private boolean _hasSignal;
    
    public GpsDriver() {
		_listener = null;
		_lastPositionTime = -1;
		_hasSignal = false;
    }
    
	public static GpsDriver[] getAllDrivers() {
		if (_allDrivers == null) {
			Vector all = new Vector();
			
			try {
			    Class.forName("javax.microedition.location.LocationProvider");
				all.addElement(new LocationApiGps());
			}
			catch(Exception ex){
				Log.info("Location API not supported");
			}

			try {
			    Class.forName("javax.bluetooth.LocalDevice");
				all.addElement(new BluetoothGps());
			}
			catch(Exception ex){
				Log.info("Bluetooth not supported");
			}

			all.addElement(new SerialPortGps());
			all.addElement(new CustomNMEAGps());
			all.addElement(new DemoGps());
			
			int ndrivers  = all.size();
			_allDrivers = new GpsDriver[ndrivers];
			for (int i = 0; i < ndrivers; i ++) {
				_allDrivers[i] = (GpsDriver)all.elementAt(i);
			}			
		}
		
		return _allDrivers;
	}
	
	public static GpsDeviceInfo[] scanAllDrivers() throws InterruptedException {
		GpsDriver[] drivers = getAllDrivers();
		GpsDeviceInfo[][] dev = new GpsDeviceInfo[drivers.length][];
		int cnt;
		
		cnt = 0;
		for (int i = 0; i < drivers.length; i ++) {
			dev[i] = drivers[i].scanDevices();
			cnt += dev[i].length;
		}
		
		GpsDeviceInfo[] all = new GpsDeviceInfo[cnt];
		
		cnt = 0;
		for (int i = 0; i < drivers.length; i ++)
			for (int j = 0; j < dev[i].length; j ++)
				all[cnt++] = dev[i][j];

		return all;
	}
	
	/**
	 * checks the age of last received position, causing calls to signalReached() or signalLost()
	 * @param msg just received message or null
	 */
	protected void checkGpsPositionAge(GpsMessage msg) {
		long now = Calendar.getInstance().getTime().getTime();
		if (msg != null && msg.hasPosition) {
			_lastPositionTime = now;
			if (!_hasSignal) {
				signalReached();
				_hasSignal = true;
			}
		}
		else if (_lastPositionTime >= 0 && _lastPositionTime + GPS_POSITION_MAXAGE*1000 < now) {
			if (_hasSignal) {
				signalLost();
				_hasSignal = false;
			}
		}
	}
	
    public abstract String getDriverId();

	/** returns driver name */
	public abstract String getName();
    
    /** Setup the communication thread and try to connect the requested device. Keep retrying on errors,
		report connection state trough deviceConnected(), deviceDisconnected(), error() */
    public abstract void connect(String address);

	/** Disconnect device */	
    public abstract void disconnect();
	
	/** returns the list of visible devices */
	public abstract GpsDeviceInfo[] scanDevices() throws InterruptedException;
	
	
	public synchronized void setListener(GpsListener l) {
		_listener = l;
	}



	/** Pass the GpsMessage to listeners */
	protected synchronized void notifyListeners(GpsMessage msg) {
		if (_listener != null)
			_listener.gpsMessage(msg);
	}
	
	/** The gps device is connected */
	protected synchronized void deviceConnected() {
		if (_listener != null)
			_listener.deviceConnected();
	}
	
	/** The gps device was disconnected */
	protected synchronized void deviceDisconnected() {
		if (_hasSignal) {
			_hasSignal = false;
			signalLost();
		}
		if (_listener != null)
			_listener.deviceDisconnected();
	}
	
	/** GPS signal lost */
	protected synchronized void signalLost() {
		if (_listener != null)
			_listener.signalLost();
	}
	
	/** GPS on air :-) */
	protected synchronized void signalReached() {
		if (_listener != null)
			_listener.signalReached();
	}
	
	
	/**
	 * 
	 * @return true if this driver provides only one (single) device. In such case, function scanDevices() MUST be fast
	 */
	public boolean hasSingleDevice() {
		return false;
	}
	
	/**
	 * 
	 * @return true iff the scan for devices is fast at all occasions. false for bluetooth :)
	 */
	public boolean hasFastScan() {
		return true;
	}
	
	public boolean isForDebugModeOnly() {
		return false;
	}
	
	/**
	 * to be overriden by drivers, which have custom user interface to ask for device
	 * 
	 * @param old last chosen device from the same driver or null
	 *  
	 * @return form for asking user gps, or null for standard [scan]->[device menu] process
	 * 
	 * @see NMEAGps
	 */
	public ValueForm askDevice(GpsDeviceInfo old) {
		return null;
	}
}



