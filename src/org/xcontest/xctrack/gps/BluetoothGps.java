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

import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import org.xcontest.xctrack.util.Log;



class Scanner implements DiscoveryListener {
	private GpsDriver _driver;
	private Vector _addresses;
	private Vector _devices;
	private boolean _scanning;

	public Scanner(GpsDriver driver) {
		_driver = driver;
		_addresses = new Vector();
		_devices = new Vector();
	}
	
	public GpsDeviceInfo[] getAllDevices() {
		GpsDeviceInfo[] arr = new GpsDeviceInfo[_devices.size()];
		for (int i = 0; i < _devices.size(); i ++)
			arr[i] = (GpsDeviceInfo)_devices.elementAt(i);
		return arr;
	}
	
	public synchronized void scan() throws InterruptedException,BluetoothStateException {
		LocalDevice dev = LocalDevice.getLocalDevice();
		
		DiscoveryAgent agent = dev.getDiscoveryAgent();
		
		/*
		// append cached&preknown devices
		RemoteDevice[] devs;
		devs = agent.retrieveDevices(DiscoveryAgent.CACHED);
		if (devs != null)
			for (int i = 0; i < devs.length; i ++)
				addDevice(devs[i]);
		devs = agent.retrieveDevices(DiscoveryAgent.PREKNOWN);
		if (devs != null)
			for (int i = 0; i < devs.length; i ++)
				addDevice(devs[i]);
		*/
		
		Log.debug("BluetoothGps: scanning...");
		_scanning = true;
		agent.startInquiry(DiscoveryAgent.GIAC, this);
		
		while (_scanning) {
			wait();
		}

		Log.debug("BluetoothGps: scan finished: found "+_devices.size()+" devices");
	}
	
	public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
		Log.debug("BluetoothGps::deviceDiscovered()");
		addDevice(remoteDevice);
	}
	
	private void addDevice(RemoteDevice remoteDevice) {
		String address = remoteDevice.getBluetoothAddress();
		
		// same device may be found several times during single search
		if (_addresses.indexOf(address) == -1) {
			String name = null;
			try {
				name = remoteDevice.getFriendlyName(false);
			}
			catch (IOException e) {
				name = address;
			}
			
			if (name != null)
				name = name.trim();
			
			if (name == null || name.length() == 0)
				name = address;

			_addresses.addElement(address);
			_devices.addElement(new GpsDeviceInfo(_driver,name,"btspp://"+address+":1"));
		}
	}

	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
	}

	public synchronized void serviceSearchCompleted(int transID, int respCode) {
	}

	public synchronized void inquiryCompleted(int discType) {
		/*
		if (discType == INQUIRY_COMPLETED) {
			Util.showInfo("Inquiry completed");
		}
		else if (discType == INQUIRY_ERROR) {
			Util.showInfo("Inquiry error!");
		}
		else if (discType == INQUIRY_TERMINATED) {
			Util.showInfo("Inquiry terminated");
		}
		else {
			Util.showInfo("Inquiry completed: "+discType);
		}
		*/
		_scanning = false;
		notifyAll();
	}
}



public class BluetoothGps extends NMEADriver {

    public BluetoothGps() {
	}
    
    /*
    public static boolean x() {
		return System.getProperty("bluetooth.api.version") != null;
    }
    */
	
	public String getDriverId() {
		return "BLUETOOTH";
	}
	
	public String getName() {
		return "Bluetooth GPS";
	}
	
	public GpsDeviceInfo[] scanDevices() throws InterruptedException {
		Scanner s = new Scanner(this);
		
		try {
			s.scan();
		}
		catch (BluetoothStateException e) {
			Log.error("Error searching for Bluetooth devices",e);
		}
		
		return s.getAllDevices();
	}
    
	public boolean hasFastScan() {
		return false;
	}
}

