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

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;

import org.xcontest.xctrack.Util;
import org.xcontest.xctrack.gps.GpsMessage;
import org.xcontest.xctrack.util.Log;


public class LocationApiGps extends GpsDriver implements LocationListener {

    private static final String NAME = "Built-in GPS";
    private LocationProvider _lp;
    private boolean _haveSignal;
    private GpsMessage _msg;

    public LocationApiGps() {
        Criteria criteria = new Criteria();
        try {
            _lp = LocationProvider.getInstance(criteria);
        }
		catch (LocationException e) {
			Log.error("Cannot get LocationProvider instance", e);
			Util.showError("Cannot get LocationProvider instance", e);
        }
		_msg = new GpsMessage();
    }

	public String getDriverId() {
		return "LOCATION-API";
	}
	
    public String getName() {
        return NAME;
    }

    public void connect(String address) {
//        _lp.setLocationListener(this, 2, -1, -1);
    	if (_lp != null) {
    		try {
    			_lp.setLocationListener(this, -1, -1, -1);
    	        _haveSignal = false;
    	        deviceConnected();
    		}
    		catch (SecurityException e) {
    			Log.error("LocationApiGps: Security Exception");
    		}
    	}
    }

    public void disconnect() {
    	if (_lp != null) {
	        _lp.setLocationListener(null, 0, 0, 0);
	        _lp.reset();
	        if (_haveSignal) {
	        	signalLost();
	        	_haveSignal = false;
	        }
	        deviceDisconnected();
    	}
    }

    public GpsDeviceInfo[] scanDevices() {
		GpsDeviceInfo[] arr = new GpsDeviceInfo[1];
		arr[0] = new GpsDeviceInfo(this,"Built-in GPS","locationapi");
		return arr;
    }
    
    public boolean hasSingleDevice() {
    	return true;
    }

    public void locationUpdated(final LocationProvider provider, final Location location) {
        if (_haveSignal) {
        	if (!location.isValid()) {
        		signalLost();
        		_haveSignal = false;
        	}
        }
        else {
        	if (location.isValid()) {
        		signalReached();
        		_haveSignal = true;
        	}
        }
        
        if (location.isValid()) {
            QualifiedCoordinates coords = location.getQualifiedCoordinates();
            _msg.reset();
	        _msg.setPosition(coords.getLongitude(), coords.getLatitude());
            _msg.setHeadingSpeed(location.getCourse(),location.getSpeed());
            _msg.setPrecision(coords.getHorizontalAccuracy());
	        _msg.setAltitude(coords.getAltitude());
	        _msg.setTime(location.getTimestamp());
	        checkGpsPositionAge(_msg);
	        notifyListeners(_msg);
        }
        else {
        	checkGpsPositionAge(null);
        }
    }

    public void providerStateChanged(LocationProvider provider, int arg1) {
    }
}

