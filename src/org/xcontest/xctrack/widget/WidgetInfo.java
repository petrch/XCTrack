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


package org.xcontest.xctrack.widget;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.xcontest.live.Earth;
import org.xcontest.xctrack.info.InfoCenter;
import org.xcontest.xctrack.info.LocationInfo;
import org.xcontest.xctrack.info.LocationInfoResult;
import org.xcontest.xctrack.widget.settings.DataSourceSettings;

class AveragingItem {
	AveragingItem(double age, int idx) {
		this.age = age;
		this.idx = idx;
	}
	double age;
	int idx;
}

public final class WidgetInfo {
	
	private static LocationInfoResult _loc = new LocationInfoResult();
	
	private static boolean _averagingChanged = false;
	private static Hashtable _locationAveraging = new Hashtable();
	private static Hashtable _altitudeAveraging = new Hashtable();
	private static double[] _locationAges;
	private static double[] _historyLocations;
	private static double[] _altitudeAges;
	private static double[] _historyAltitudes;
	private static double[] _averageSpeed;
	private static double[] _averageHeading;
	private static double[] _averageVario;
	
	public static void update() {
		LocationInfo li = InfoCenter.getInstance().getLocationInfo();
		
		if (_averagingChanged) {
			Vector ages = new Vector();
			Enumeration els;
			int nages;
			
			els = _locationAveraging.elements();
			while (els.hasMoreElements()) {
				AveragingItem it = (AveragingItem)els.nextElement();
				Double ot = new Double(it.age);
				it.idx = ages.indexOf(ot);
				if (it.idx < 0) {
					it.idx = ages.size();
					ages.addElement(ot);
				}
			}
			nages = ages.size();
			_locationAges = new double[nages];
			for (int i = 0; i < nages; i ++)
				_locationAges[i] = ((Double)ages.elementAt(i)).doubleValue();
			_averageSpeed = new double[nages];
			_averageHeading = new double[nages];
			_historyLocations = new double[4*nages];

			ages.removeAllElements();
			els = _altitudeAveraging.elements();
			while (els.hasMoreElements()) {
				AveragingItem it = (AveragingItem)els.nextElement();
				Double ot = new Double(it.age);
				it.idx = ages.indexOf(ot);
				if (it.idx < 0) {
					it.idx = ages.size();
					ages.addElement(ot);
				}
			}
			nages = ages.size();
			_altitudeAges = new double[nages];
			for (int i = 0; i < nages; i ++)
				_altitudeAges[i] = ((Double)ages.elementAt(i)).doubleValue();
			_averageVario = new double[nages];
			_historyAltitudes = new double[2*nages];
			
			li.getHistory().reset(_locationAges,_altitudeAges);
			_averagingChanged = false;
		}
		
		li.computeLocation(_loc);
		
		// == null for the first run - the history requests has to be setup first at the first-time paint() of widgets
		double currentSpeed = (_loc.hasTwoPoints && _loc.age < 20) ? _loc.speed : Double.NaN;
		double currentHeading = (_loc.hasTwoPoints && _loc.age < 20) ? _loc.heading : Double.NaN;
		double currentVario = (_loc.hasVerticalSpeed && _loc.age < 20) ? _loc.verticalSpeed : Double.NaN;

		if (_locationAges != null) {
			li.getHistory().get(_loc.time, _historyLocations, _historyAltitudes);
			
			int n = _locationAges.length;
			for (int i = 0; i < n; i ++) {
				if (Double.isNaN(_historyLocations[4*i])) {
					_averageHeading[i] = currentHeading;
					_averageSpeed[i] = currentSpeed;
				}
				else {
					_averageSpeed[i] = Earth.getDistance(_historyLocations[4*i], _historyLocations[4*i+1],
															_historyLocations[4*i+2], _historyLocations[4*i+3])/_locationAges[i];
					_averageHeading[i] = Earth.getAngle(_historyLocations[4*i], _historyLocations[4*i+1],
														_historyLocations[4*i+2], _historyLocations[4*i+3]);
				}
			}
	
			n = _altitudeAges.length;
			for (int i = 0; i < n; i ++) {
				if (Double.isNaN(_historyAltitudes[2*i])) {
					_averageVario[i] = currentVario;
				}
				else {
					_averageVario[i] = (_historyAltitudes[2*i+1]-_historyAltitudes[2*i])/_altitudeAges[i];
				}
			}
		}
	}
	
	public static LocationInfoResult getLocation() {
		return _loc;
	}
	
	// synchronize? ... all methods here are called from one thread (WidgetPage.paint), no serialization needed (yet)
	public static double getAverageVario(Object settings, double age) {
		if (age > 0) {
			if (_altitudeAveraging.containsKey(settings)) {
				AveragingItem it = (AveragingItem)_altitudeAveraging.get(settings);
				if (it.age == age) {
					return _averageVario[it.idx];
				}
				else {
					it.age = age;
					_averagingChanged = true;
					return Double.NaN;
				}
			}
			else {
				_altitudeAveraging.put(settings, new AveragingItem(age,0));
				_averagingChanged = true;
				return Double.NaN;
			}
		}
		else {
			if (_altitudeAveraging.containsKey(settings)) {
				_altitudeAveraging.remove(settings);
				_averagingChanged = true;
			}
			return (_loc.hasVerticalSpeed && _loc.age < 20) ? _loc.verticalSpeed : Double.NaN;
		}
	}

	// synchronize? ... all methods here are called from one thread (WidgetPage.paint), no serialization needed (yet)
	private static double getAverageSpeedHeading(Object settings, double age, boolean isSpeed) {
		if (age > 0) {
			if (_locationAveraging.containsKey(settings)) {
				AveragingItem it = (AveragingItem)_locationAveraging.get(settings);
				if (it.age == age) {
					return isSpeed ? _averageSpeed[it.idx] : _averageHeading[it.idx];
				}
				else {
					it.age = age;
					_averagingChanged = true;
					return Double.NaN;
				}
			}
			else {
				_locationAveraging.put(settings, new AveragingItem(age,0));
				_averagingChanged = true;
				return Double.NaN;
			}
		}
		else {
			if (_locationAveraging.containsKey(settings)) {
				_locationAveraging.remove(settings);
				_averagingChanged = true;
			}
			return (_loc.hasTwoPoints && _loc.age < 20) ? (isSpeed ? _loc.speed : _loc.heading) : Double.NaN;
		}
	}

	public static double getAverageSpeed(Object settings, double age) {
		return getAverageSpeedHeading(settings,age,true);
	}

	public static double getAverageHeading(Object settings, double age) {
		return getAverageSpeedHeading(settings,age,false);
	}
	
	public static double getSpeed(DataSourceSettings.Data d) {
		if (d.gpsSource)
			return _loc.gpsSpeed;
		else 
			return getAverageSpeed(d,d.averaging);
	}

	public static double getHeading(DataSourceSettings.Data d) {
		if (d.gpsSource)
			return _loc.gpsHeading;
		else
			return getAverageHeading(d,d.averaging);
	}

	public static double getVario(DataSourceSettings.Data d) {
		return getAverageVario(d,d.averaging);
	}
}
