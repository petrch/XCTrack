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

import org.xcontest.live.Earth;

//BUGS: longitude crossing +-180 will mess up averaging

public class History {
	class AltitudeBufferPosition {
		double age;
		int cnt;
		double sumT;
		double sumT2;
		double sumAlt;
		double sumAltT;
	}
	
	class LocationBufferPosition {
		double age;
		int cnt;
		double sumT;
		double sumT2;
		double sumX;
		double sumXT;
		double sumY;
		double sumYT;
	}
	
	private static final double MININTERVAL = 0.09;

	private double _baseT;
	private double[] _location;	// { time, lon, lat }*
	private double[] _altitude; // { time, alt } *
	private int _locationCount;
	private int _altitudeCount;
	private double _locationTime;
	private double _altitudeTime;
	private LocationBufferPosition[] _locationPosition;
	private AltitudeBufferPosition[] _altitudePosition;
	
	public History() {
		reset();
	}
	
	public final void reset() {
		double[] arr = new double[0];
		reset(arr,arr);
	}
	
	public synchronized final void reset(double[] locationAges, double[] altitudeAges) {
		int len;
		double maxage;
		
		_baseT = System.currentTimeMillis()/1000;
		maxage = 0;
		_locationCount = 0;
		_locationTime = -1;
		_locationPosition = new LocationBufferPosition[locationAges.length];
		for (int i = 0; i < locationAges.length; i ++) {
			LocationBufferPosition pos = new LocationBufferPosition();
			pos.age = locationAges[i];
			pos.cnt = 0;
			pos.sumT = 0;
			pos.sumT2 = 0;
			pos.sumX = 0;
			pos.sumXT = 0;
			pos.sumY = 0;
			pos.sumYT = 0;
			if (maxage < pos.age) maxage = pos.age;
			_locationPosition[i] = pos;
		}
		len = (int)Math.ceil(maxage/MININTERVAL);
		_location = len == 0 ? null : new double[3*len];

		maxage = 0;
		_altitudeCount = 0;
		_altitudeTime = -1;
		_altitudePosition = new AltitudeBufferPosition[altitudeAges.length];
		for (int i = 0; i < altitudeAges.length; i ++) {
			AltitudeBufferPosition pos = new AltitudeBufferPosition();
			pos.age = altitudeAges[i];
			pos.cnt = 0;
			pos.sumT = 0;
			pos.sumT2 = 0;
			pos.sumAlt = 0;
			pos.sumAltT = 0;
			if (maxage < pos.age) maxage = pos.age;
			_altitudePosition[i] = pos;
		}
		len = (int)Math.ceil(maxage/MININTERVAL);
		_altitude = len == 0 ? null : new double[2*len];
	}
	
	public synchronized final void addAltitude(double t, double alt) {
		if (_altitude != null && t >= _altitudeTime+MININTERVAL) {
			t -= _baseT;
			for (int i = 0; i < _altitudePosition.length; i ++) {
				AltitudeBufferPosition pos = _altitudePosition[i];
				pos.sumAlt += alt;
				pos.sumAltT += alt*t;
				pos.sumT += t;
				pos.sumT2 += t*t;
				if (pos.cnt == _altitudeCount-_altitude.length) {
					int idx = _altitudeCount%_altitude.length;
					double oldt = _altitude[idx];
					double oldalt = _altitude[idx+1];
					pos.sumAlt -= oldalt;
					pos.sumAltT -= oldalt*oldt;
					pos.sumT -= oldt;
					pos.sumT2 -= oldt*oldt;
					pos.cnt += 2;
				}
			}
			
			int idx = _altitudeCount%_altitude.length;
			_altitude[idx] = t;
			_altitude[idx+1] = alt;
			_altitudeCount += 2;
			_altitudeTime = t;
		}
	}

	public synchronized final void addLocation(double t, double lon, double lat) {
		if (_location != null && t >= _locationTime+MININTERVAL) {
			t -= _baseT;
			double x = Earth.lon2gg(lon);
			double y = Earth.lat2gg(lat);
			
			for (int i = 0; i < _locationPosition.length; i ++) {
				LocationBufferPosition pos = _locationPosition[i];
				pos.sumX += x;
				pos.sumXT += x*t;
				pos.sumY += y;
				pos.sumYT += y*t;
				pos.sumT += t;
				pos.sumT2 += t*t;
				if (pos.cnt == _locationCount-_location.length) {
					int idx = _locationCount%_location.length;
					double oldt = _location[idx];
					double oldx = _location[idx+1];
					double oldy = _location[idx+2];
					pos.sumX -= oldx;
					pos.sumXT -= oldx*oldt;
					pos.sumY -= oldy;
					pos.sumYT -= oldy*oldt;
					pos.sumT -= oldt;
					pos.sumT2 -= oldt*oldt;
					pos.cnt += 3;
				}
			}

			int idx = _locationCount%_location.length;
			_location[idx] = t;
			_location[idx+1] = x;
			_location[idx+2] = y;
			_locationCount += 3;
			_locationTime = t;
		}
	}
	
	/***
	 * 
	 * @param t current time
	 * @param locout output of location history: array of repeating {lon_history,lat_history,lon_t0,lat_t0}*
	 * 				- sets NaN,NaN,NaN,NaN if the history item is not available
	 * @param altout output of altitude history: array of {alt_history,alt_t0}*
	 * 				- sets NaN,NaN if the history item is not available
	 */
	public synchronized final void get(double t, double[] locout, double[] altout) {
		int locCnt = _locationCount;
		int altCnt = _altitudeCount;
		int locLen = _location == null ? 0 : _location.length;
		int altLen = _altitude == null ? 0 : _altitude.length;
		double[] loc = _location;
		double[] alt = _altitude;
		t -= _baseT;
		
		for (int i = 0; i < _locationPosition.length; i ++) {
			LocationBufferPosition pos = _locationPosition[i];
//			if (pos.cnt < locCnt-locLen) pos.cnt = locCnt-locLen;
			while (pos.cnt < locCnt && loc[pos.cnt%locLen] < t-pos.age) {
				int idx = pos.cnt%locLen;
				double oldt = loc[idx];
				double oldx = loc[idx+1];
				double oldy = loc[idx+2];
				pos.sumX -= oldx;
				pos.sumXT -= oldx*oldt;
				pos.sumY -= oldy;
				pos.sumYT -= oldy*oldt;
				pos.sumT -= oldt;
				pos.sumT2 -= oldt*oldt;
				pos.cnt += 3;
			}
			if (pos.cnt < locCnt-3) {
				int n = (locCnt-pos.cnt)/3;
				double d = n*pos.sumT2 - pos.sumT*pos.sumT;
				double a = n*pos.sumXT - pos.sumX*pos.sumT;
				double b = pos.sumT2*pos.sumX - pos.sumT*pos.sumXT;
				locout[4*i] = Earth.gg2lon((a*(t-pos.age)+b)/d);
				locout[4*i+2] = Earth.gg2lon((a*t+b)/d);

				a = n*pos.sumYT - pos.sumY*pos.sumT;
				b = pos.sumT2*pos.sumY - pos.sumT*pos.sumYT;
				locout[4*i+1] = Earth.gg2lat((a*(t-pos.age)+b)/d);
				locout[4*i+3] = Earth.gg2lat((a*t+b)/d);
			}
			else {
				locout[4*i] = Double.NaN;
				locout[4*i+1] = Double.NaN;
				locout[4*i+2] = Double.NaN;
				locout[4*i+3] = Double.NaN;
			}
		}

		for (int i = 0; i < _altitudePosition.length; i ++) {
			AltitudeBufferPosition pos = _altitudePosition[i];
//			if (pos.cnt < altCnt-altLen) pos.cnt = altCnt-altLen;
			while (pos.cnt < altCnt && alt[pos.cnt%altLen] < t-pos.age) {
				int idx = pos.cnt%altLen;
				double oldt = alt[idx];
				double oldalt = alt[idx+1];
				pos.sumAlt -= oldalt;
				pos.sumAltT -= oldalt*oldt;
				pos.sumT -= oldt;
				pos.sumT2 -= oldt*oldt;
				pos.cnt += 2;
			}
			if (pos.cnt < altCnt-2) {
				int n = (altCnt-pos.cnt)/2;
				double d = n*pos.sumT2 - pos.sumT*pos.sumT;
				double a = n*pos.sumAltT - pos.sumAlt*pos.sumT;
				double b = pos.sumT2*pos.sumAlt - pos.sumT*pos.sumAltT;
				altout[2*i] = (a*(t-pos.age)+b)/d;
				altout[2*i+1] = (a*t+b)/d;
			}
			else {
				altout[2*i] = Double.NaN;
				altout[2*i+1] = Double.NaN;
			}
		}
	}
}


