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
import org.xcontest.xctrack.gps.GpsMessage;

public class LocationInfo {
	private static final double EPSILON = 1e-12;
	private static final int NWINDLINES = 4;
//	private static final int NWINDAVGPOINTS = 10;
	
	class WindLine {
		double a,b,c;
	}
	
	class WindPoint {
		double x,y;
	}
	
	class WindAvgPoint {
		double x,y;
		double weight;
	}
	
	private History _history;
	
	private boolean _isGpsConnected;
	private boolean _hasGpsSignal;
	
	private boolean _hasTwoPoints;
	private double _speed;
	private double _lonSpeed;
	private double _latSpeed;
	private double _heading;

	private double _altitude;
	private double _altitudeTime;
	private boolean _hasVerticalSpeed;
	private double _verticalSpeed;
	
	private double _lon;
	private double _lat;
	private double _positionTime;
	
	// NaN if not available
	private double _gpsHeading;
	private double _gpsSpeed;
	
	// wind speed, direction and stuff
	private WindLine[] _windLines;
	private WindPoint[] _windPoints;
	private boolean _windHasLastSpeed;
	private double _windLastSpeedX;
	private double _windLastSpeedY;
	private double _windLastSpeedTime;
	private int _windLinesCount;
	private double _windAvgX;
	private double _windAvgY;
	private double _windAvgWeight;
//	private WindAvgPoint[] _windAvgPoints;
//	private int _windAvgPointsCount;
	
	private boolean _hasWind;
	private double _windDirection;
	private double _windSpeed;
	private double _windPrecision;
	private double _windAvgSpeed;
	private double _windAvgDirection;
	
	private double _time,_localTime;

	LocationInfo() {
		_history = new History();
		reset();
	}
	
	synchronized final void reset() {
		_time = System.currentTimeMillis()/1000.0;
		_localTime = _time;
		
		_hasTwoPoints = false;
		_altitudeTime = -1;
		_positionTime = -1;
		_hasVerticalSpeed = false;
		
		_gpsHeading = Double.NaN;
		_gpsSpeed = Double.NaN;
		
		_isGpsConnected = false;
		_hasGpsSignal = false;

		_hasWind = false;
		if (_windLines == null) {
			_windLines = new WindLine[NWINDLINES];
			for (int i = 0; i < NWINDLINES; i ++)
				_windLines[i] = new WindLine();
			_windPoints = new WindPoint[NWINDLINES+1];
			for (int i = 0; i <= NWINDLINES; i ++)
				_windPoints[i] = new WindPoint();
			/*
			_windAvgPoints = new WindAvgPoint[NWINDAVGPOINTS];
			for (int i = 0; i < NWINDAVGPOINTS; i ++)
				_windAvgPoints[i] = new WindAvgPoint();
			*/
		}
		_windLinesCount = 0;
//		_windAvgPointsCount = 0;
		_windAvgWeight = 0;
		_windHasLastSpeed = false;
	}
	
	public final History getHistory() {
		return _history;
	}
	
	public synchronized final void computeLocation(LocationInfoResult result) {
		result.time = computeTime();
		result.hasTwoPoints = _hasTwoPoints;
		result.hasPosition = _positionTime >= 0;
		result.hasAltitude = _altitudeTime >= 0;
		result.hasVerticalSpeed = _hasVerticalSpeed;
		if (_hasTwoPoints) {
			double dt = result.time - _positionTime;
			result.speed = _speed;
			result.lon = _lon + _lonSpeed*dt;
			result.lat = _lat + _latSpeed*dt;
			result.heading = _heading;
			result.age = result.time - _positionTime;
		}
		else if (_positionTime >= 0) {
			result.lon = _lon;
			result.lat = _lat;
			result.age = result.time - _positionTime;
		}
		
		if (_altitudeTime >= 0)
			result.altitude = _altitude;

		if (_hasVerticalSpeed)
			result.verticalSpeed = _verticalSpeed;
		
		result.hasWind = _hasWind;
		if (_hasWind) {
			result.windDirection = _windDirection;
			result.windSpeed = _windSpeed;
			result.windPrecision = _windPrecision;
			result.windAvgSpeed = _windAvgSpeed;
			result.windAvgDirection = _windAvgDirection;
		}
		
		result.gpsHeading = _gpsHeading;
		result.gpsSpeed = _gpsSpeed;
	}
	
	public synchronized final double[] getWindLines() {
		int n = _windLinesCount > _windLines.length ? _windLines.length : _windLinesCount;
		double[] out = new double[3*n];
		int idx = 0;
		for (int i = _windLinesCount-n; i < _windLinesCount; i ++) {
			WindLine line = _windLines[i%_windLines.length];
			out[idx++] = line.a;
			out[idx++] = line.b;
			out[idx++] = line.c;
		}
		return out;
	}

	public synchronized final double[] getWindPoints() {
		int n = _windLinesCount+1 > _windPoints.length ? _windPoints.length : _windLinesCount+1;
		double[] out = new double[2*n];
		int idx = 0;
		for (int i = _windLinesCount+1-n; i <= _windLinesCount; i ++) {
			WindPoint point = _windPoints[i%_windPoints.length];
			out[idx++] = point.x;
			out[idx++] = point.y;
		}
		return out;
	}
	
	public synchronized final double computeTime() {
		double now = ((double)System.currentTimeMillis())/1000;
		return _time+now-_localTime;
	}
	
	public synchronized final void setGpsConnected(boolean val) {
		_isGpsConnected = val;
	}
	
	public synchronized final boolean isGpsConnected() {
		return _isGpsConnected;
	}
	
	public synchronized final boolean hasGpsSignal() {
		return _hasGpsSignal;
	}
	
	public synchronized final void setHasGpsSignal(boolean val) {
		_hasGpsSignal = val;
	}
	
	synchronized final void update(GpsMessage msg) {
		double t = -1;

		if (msg.hasTime) {
			_time = ((double)msg.time)/1000;
			_localTime = ((double)System.currentTimeMillis())/1000;
			t = _time;
		}
		
		if (msg.hasPosition) {
			if (t < 0) t = computeTime();
			
			_history.addLocation(t, msg.lon, msg.lat);
			
			if (_positionTime >= 0) {
				double lat = msg.lat;
				double lon = msg.lon;
				double dist = Earth.getDistance(lon, lat, _lon, _lat);
				if (_positionTime < t) {
					_speed = dist/(t-_positionTime);
					_lonSpeed = (lon-_lon)/(t-_positionTime);
					_latSpeed = (lat-_lat)/(t-_positionTime);
					_heading = Earth.getAngle(_lon, _lat, lon, lat);
					_hasTwoPoints = true;

					double speedX = _speed*Math.sin(_heading*Math.PI/180);
					double speedY = _speed*Math.cos(_heading*Math.PI/180);
					if (_windHasLastSpeed) {
						double a = speedX-_windLastSpeedX;
						double b = speedY-_windLastSpeedY;
						double len = Math.sqrt(a*a+b*b);
						if (len > 4) {	// at least 4m/s difference in speed
							a /= len;
							b /= len;
							double c = -a*(speedX+_windLastSpeedX)/2-b*(speedY+_windLastSpeedY)/2;
							WindLine lastLine = _windLinesCount == 0 ? null : _windLines[(_windLinesCount-1)%_windLines.length];
							// 0.866 == cos(30)
							if (lastLine == null || Math.abs(lastLine.a*a+lastLine.b*b) < 0.866 || t-_windLastSpeedTime > 30) {
								WindLine line = _windLines[_windLinesCount%_windLines.length];
								line.a = a;
								line.b = b;
								line.c = c;
								_windLinesCount ++;
								_windLastSpeedX = speedX;
								_windLastSpeedY = speedY;
								_windLastSpeedTime = t;
								WindPoint point = _windPoints[_windLinesCount%_windPoints.length];
								point.x = speedX;
								point.y = speedY;
								
								if (_windLinesCount >= _windLines.length) { 
									double ab=0,bc=0,ac=0,a2=0,b2=0;
									for (int i = _windLinesCount-_windLines.length; i < _windLinesCount; i ++) {
										line = _windLines[i%_windLines.length];
										ab += line.a*line.b;
										bc += line.b*line.c;
										ac += line.a*line.c;
										a2 += line.a*line.a;
										b2 += line.b*line.b;
									}
									double d = a2*b2-ab*ab;
									if (d > EPSILON) {
										double x = (ab*bc-b2*ac)/d;
										double y = (ab*ac-a2*bc)/d;
										_windDirection = (Earth.atan2(x,y)+Math.PI)*180/Math.PI;
										_windSpeed = Math.sqrt(x*x+y*y);
										_windPrecision = 0;
										for (int i = _windLinesCount-_windLines.length; i < _windLinesCount; i ++) {
											line = _windLines[i%_windLines.length];
											_windPrecision += Math.abs(line.a*x+line.b*y+line.c);
										}
										_windPrecision /= _windLines.length;
										
										// center of the points
										double centerX = 0;
										double centerY = 0;
										for (int i = 0; i < _windPoints.length; i ++) {
											centerX += _windPoints[i].x;
											centerY += _windPoints[i].y;
										}
										centerX /= _windPoints.length;
										centerY /= _windPoints.length;
										double maxdist2 = 0;
										for (int i = 0; i < _windPoints.length; i ++) {
											WindPoint p = _windPoints[i];
											double d2 = (p.x-centerX)*(p.x-centerX)+(p.y-centerY)*(p.y-centerY);
											if (maxdist2 < d2)
												maxdist2 = d2;
										}
										
										// at most 1m/s precision
										// at least 5m/s radius (10m/s diameter) of the circle containing points
										if (_windPrecision < 1 && maxdist2 > 25) {
											_hasWind = true;
											double weight = 1/(1+100*_windPrecision*_windPrecision);
											_windAvgX = (_windAvgX*_windAvgWeight+weight*x)/(_windAvgWeight+weight);
											_windAvgY = (_windAvgY*_windAvgWeight+weight*y)/(_windAvgWeight+weight);
											_windAvgWeight = (_windAvgWeight+weight)*0.9;
											_windAvgDirection = (Earth.atan2(_windAvgX,_windAvgY)+Math.PI)*180/Math.PI;
											_windAvgSpeed = Math.sqrt(_windAvgX*_windAvgX+_windAvgY*_windAvgY);
										}
									}
								}
							}
						}						
					}
					else {	// _windHasLastSpeed == false
						_windHasLastSpeed = true;
						_windLastSpeedX = speedX;
						_windLastSpeedY = speedY;
						_windPoints[0].x = speedX;
						_windPoints[0].y = speedY;
					}
				}
			}
			
			_lat = msg.lat;
			_lon = msg.lon;
			_positionTime = t;
		}

		if (msg.hasAltitude) {
			if (t < 0) t = computeTime();
			
			_history.addAltitude(t, msg.altitude);
			
			if (_altitudeTime >= 0 && _altitudeTime < t) {
				_hasVerticalSpeed = true;
				_verticalSpeed = (msg.altitude - _altitude)/(t - _altitudeTime);
			}
			
			_altitude = msg.altitude;
			_altitudeTime = t;
		}
		
		if (msg.hasHeadingSpeed) {
			_gpsSpeed = msg.speed;
			_gpsHeading = msg.heading;
		}
	}

}
