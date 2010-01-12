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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import org.xcontest.xctrack.gps.GpsMessage;
import org.xcontest.xctrack.util.Log;

public class DemoGps extends GpsDriver implements Runnable {
    
	class IGCPoint {
		double lon,lat;
		int alt;
		int t;
	}
	
	private Thread _thread;
	private double _lat,_lon;
	private double _dx,_dy;
    private Random _rnd;
    private String _address;
    private IGCPoint[] _igc;
    
    public DemoGps() {
    }
	
	public String getDriverId() {
		return "DEMO";
	}
	
	/** @return driver name */
	public String getName() {
		return "Demo GPS";
	}
    
    /** Connect the device */
    public synchronized void connect(String address) {
		_lat = 50;
		_lon = 14;
		_rnd = new Random();

		_dx = 0.0002*(_rnd.nextDouble()-0.5);
		_dy = 0.0001*(_rnd.nextDouble()-0.5);

		_address = address;
		
		_thread = new Thread(this);
		_thread.start();
	}

	/** Disconnect device */	
    public synchronized void disconnect() {
    	_thread.interrupt();
	}
    
    private String findIGC() {
		Enumeration roots = FileSystemRegistry.listRoots();
		Enumeration files;
		FileConnection fconn = null;
		
		Vector dirs = new Vector();
		while (roots.hasMoreElements()) {
			String root = (String)roots.nextElement();
			if (root.length() == 0 || root.charAt(root.length()-1) != '/')
				root += "/";
			dirs.addElement(root);
			dirs.addElement(root+"Document/");
			dirs.addElement(root+"Other/");
		}
		
		for (int i = 0; i < dirs.size(); i ++) {
			String dir=  (String)dirs.elementAt(i);
			Log.info("DemoIGC: searching "+dir);
			try {
				fconn = (FileConnection)Connector.open("file:///"+dir);
				if (fconn.exists()) {
					files = fconn.list();
					while (files.hasMoreElements()) {
						String fn = (String)files.nextElement();
						if (fn.toLowerCase().endsWith(".igc")) {
							Log.info("DemoIGC: found "+dir+fn);
							return "file:///"+dir+fn;
						}
					}
				}
				fconn.close();
			}
			catch (SecurityException e) {
				Log.error("DemoIGC: error searching for file",e);
				if (fconn != null) {
					try {
						fconn.close();
					}
					catch (IOException e1) {}
				}
			}
			catch (IOException e) {
				Log.error("DemoIGC: error searching for file",e);
				if (fconn != null) {
					try {
						fconn.close();
					}
					catch (IOException e1) {}
				}
			}
		}
		
		return null;
    }
    
    private int readLine(DataInputStream is, byte[] buf) {
    	int pos = 0;
    	int c;
    	
    	try {
	    	while (true) {
	    		c = is.read();
	    		if (c == -1) return pos == 0 ? -1 : pos;	// EOF
	    		if (c == 10 || c == 13) return pos;
	    		if (pos < buf.length) {
	    			buf[pos++] = (byte)c;
	    		}
	    	}
    	}
    	catch (IOException e) {
    		return pos == 0 ? -1 : pos;
    	}
    }
    
    private int getNum(byte[] buf, int start, int len) {
    	int n = 0;
    	for (int i = start; i < start+len; i ++)
    		n = n*10+buf[i]-'0';
    	return n;
    }
    
    private void loadIGC() {
    	byte[] buf = new byte[1+6+8+9+1+10];	// 'B'+HHMMSS+lat+lon+'A'+alt1,alt2
    	String fn = findIGC();
    	int len;
    	Vector points;
    	FileConnection fconn = null;
    	
    	_igc = null;
    	
    	if (fn != null) {
    		Log.info("DemoGps: Loading IGC "+fn);
        	try {
	    		points = new Vector();
	        	fconn = (FileConnection)Connector.open(fn);
	        	DataInputStream is = fconn.openDataInputStream();
	        	while ((len=readLine(is,buf))>=0) {
	        		if (len == buf.length && buf[0] == 'B') {
	        			IGCPoint p = new IGCPoint();
	        			p.t = (getNum(buf,1,2)*3600+getNum(buf,3,2)*60+getNum(buf,5,2))*1000;
	        			p.lat = getNum(buf,7,2)+getNum(buf,9,5)/60000.0;
	        			if (buf[14] == 'S' || buf[14] == 's') p.lat = -p.lat;
	        			p.lon = getNum(buf,15,3)+getNum(buf,18,5)/60000.0;
	        			if (buf[23] == 'W' || buf[23] == 'w') p.lon = -p.lon;
	        			p.alt = getNum(buf,25,5);
	        			if (p.alt == 0) p.alt = getNum(buf,30,5);
	        			points.addElement(p);
	        		}
	        	}
	        	
	        	_igc = new IGCPoint[points.size()];
	        	for (int i = 0; i < points.size(); i ++)
	        		_igc[i] = (IGCPoint)points.elementAt(i);
	    	}
        	catch(IOException e) {
        		Log.error("DemoGps: Cannot read IGC: "+fn,e);
        	}
        	finally {
        		if (fconn != null) {
        			try {
        				fconn.close();
        			}
        			catch(IOException e){}
        		}
        	}
    	}
    }
    
    public void runIGC() throws InterruptedException {
    	loadIGC();
    	int igcPos;
    	long igcTimeOffset = 0;
    	long now = System.currentTimeMillis();
    	boolean isConnected = false;
		GpsMessage msg = new GpsMessage();
    	
    	if (_igc != null && _igc.length > 0) {
    		igcTimeOffset = now-_igc[0].t;
    		deviceConnected();
    		signalReached();
    		isConnected = true;
    	}
    	
    	igcPos = 0;
    	while(true) {
    		if (!isConnected) {
    			Thread.sleep(10000);
    		}
    		else if (igcPos >= _igc.length) {
    			signalLost();
    			isConnected = false;
    		}
    		else {
    			IGCPoint p = _igc[igcPos];
    	    	now = System.currentTimeMillis();
    	    	
    	    	if (now < igcTimeOffset+p.t)
    	    		Thread.sleep(igcTimeOffset+p.t-now);

				msg.reset();
				msg.setPosition(p.lon, p.lat);
				msg.setAltitude(p.alt);
				msg.setTime(igcTimeOffset+p.t);
				notifyListeners(msg);
    			igcPos ++;
    		}
    	}
    }
    
    public void runDefault() throws InterruptedException {
		deviceConnected();
		signalReached();
		GpsMessage msg = new GpsMessage();

		while (true) {
			synchronized(this) {
				_lat += _dy;
				_lon += _dx;
				
				if (_rnd.nextInt()%40 == 0) {
					_dx = 0.0002*(_rnd.nextDouble()-0.5);
					_dy = 0.0001*(_rnd.nextDouble()-0.5);
				}
				else if (_rnd.nextInt()%3 == 0) {
					_dx += 0.00002*(_rnd.nextDouble()-0.5);
					_dy += 0.00001*(_rnd.nextDouble()-0.5);
				}
				
				msg.reset();
				msg.setPosition(_lon, _lat);
				msg.setAltitude(123);
			}			
			msg.setTime(System.currentTimeMillis());
			notifyListeners(msg);
			
			Thread.sleep(_address.equals("ffdemo") ? 5 : 500);
		}
    }
    
	public void run() {
		Log.info("GPS demo driver started");
		try {
			if (_address.equals("igc"))
				runIGC();
			else
				runDefault();
		}
		catch (InterruptedException e) {
		}
		catch (Throwable e) {
			Log.error("DemoGps: FATAL",e);
		}
		
		deviceDisconnected();

		Log.info("GPS demo driver stopped");
	}
	
	/** @return the list of visible devices. return value is Vector of GpsDeviceInfo objects */
	public GpsDeviceInfo[] scanDevices() {
		return new GpsDeviceInfo[] {
				new GpsDeviceInfo(this,"Demo GPS Nizbor","demo"),
				new GpsDeviceInfo(this,"FAST Demo GPS","ffdemo"),
				new GpsDeviceInfo(this,"IGC Replay","igc")
		};
	}
	
	public boolean hasSingleDevice() {
		return false;
	}
	
	public boolean isForDebugModeOnly() {
		return true;
	}
}



