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

package org.xcontest.xctrack.config;

import java.util.Vector;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

import org.xcontest.live.UTF8;
import org.xcontest.xctrack.Util;
import org.xcontest.xctrack.gps.GpsDeviceInfo;
import org.xcontest.xctrack.gps.GpsDriver;
import org.xcontest.xctrack.settings.Profile;



final class Record {
	int recordStoreId;
	boolean isModified;
	byte[] data;
}

public final class Config {
	
	final static String VERSION = "0.2";
	
	public static void init() {
		readAll();
		_lastRunVersion = getConfigVersion();
		if (_lastRunVersion == null || !_lastRunVersion.equals(VERSION)) {
			setConfigVersion(VERSION);
			writeAll();
		}
	}
	
	/**
	 * debug only method ... clears both cached settings and associated record store
	 */
	public static void clear() {
		try {
			RecordStore.deleteRecordStore("config");
		}
		catch (RecordStoreNotFoundException e) {
		}
		catch (RecordStoreException e) {
		}
		
		_records = new Record[KEY_MAX+1];
	}
	
	/**
	 * 
	 * @return string with information of all the records in the config record store
	 */
	public static String inspectRecordStore() {
		String out = "";
		RecordStore rs = null;
		try {
			rs = RecordStore.openRecordStore("config",true);
			RecordEnumeration re = rs.enumerateRecords(null, null, false);
			while (re.hasNextElement()) {
				int id = re.nextRecordId();
				byte[] data = rs.getRecord(id);
				out += "" + id + ": key=" + data[0] + " data=" + new String(data,1,data.length-1) + "\n";
			}
			rs.closeRecordStore();
		}
		catch (Exception e) {
			out += "EXCEPTION: " + e.toString();
			try {
				out += " num=" + rs.getNumRecords();
			}
			catch (RecordStoreNotOpenException e1) {}
		}
		return out;
	}

	public static String getConfigVersion() {
		return getString(KEY_CONFIG_VERSION);
	}
	
	private static void setConfigVersion(String version) {
		setString(KEY_CONFIG_VERSION,version);
	}
	
	public static boolean isFirstRun() {
		return _lastRunVersion == null;
	}
	
	public static boolean isDebugMode() {
		return Config.getInt(KEY_DEBUG_MODE, 0) == 1;
	}
	
	public static void setDebugMode(boolean val) {
		Config.setInt(KEY_DEBUG_MODE, val ? 1 : 0);
	}
	
	public static int getProtocol() {
		return getInt(KEY_PROTOCOL,DEFAULT_PROTOCOL);
	}
	
	public static void setProtocol(int value) {
		setInt(KEY_PROTOCOL, value);
	}
	
	public static int getHTTPPingMode() {
		return getInt(KEY_HTTP_PING_MODE,DEFAULT_HTTP_PING_MODE);
	}
	
	public static void setHTTPPingMode(int value) {
		setInt(KEY_HTTP_PING_MODE,value);
	}

	/*
	public static int getHTTPPingInterval() {
		return getInt(KEY_HTTP_PING_INTERVAL,DEFAULT_HTTP_PING_INTERVAL);
	}
	
	public static void setHTTPPingInterval(int value) {
		setInt(KEY_HTTP_PING_INTERVAL,value);
	}
	*/
	
	public static String getUDPServerHost() {
		String s = getString(KEY_UDPSERVER_HOST);
		if (s == null || s.equals(""))
			return DEFAULT_UDP_HOST;
		else
			return s;
	}
	
	public static int getUDPServerPort() {
		String s = getString(KEY_UDPSERVER_PORT);
		if (s == null || s.equals(""))
			return DEFAULT_UDP_PORT;
		else
			return Integer.parseInt(s);
	}
	
	public static String getWSServerUrl() {
		return DEFAULT_WSSERVER_URL;
	}
	
	public static String getTCPServerHost() {
		String s = getString(KEY_TCPSERVER_HOST);
		if (s == null || s.equals(""))
			return DEFAULT_TCP_HOST;
		else
			return s;
	}
	
	public static int getTCPServerPort() {
		String s = getString(KEY_TCPSERVER_PORT);
		if (s == null || s.equals(""))
			return DEFAULT_TCP_PORT;
		else
			return Integer.parseInt(s);
	}

	public static boolean getUseZLib() {
		return Config.getInt(KEY_ZLIB, 1) == 1;
	}
	
	public static void setUseZLib(boolean val) {
		Config.setInt(KEY_ZLIB, val ? 1 : 0);
	}
	
	/*
	public static int getMinTracklogInterval() {
		return getInt(KEY_TRACKLOG_MIN_INTERVAL,DEFAULT_TRACKLOG_MIN_INTERVAL);
	}
	
	public static void setMinTracklogInterval(int val) {
		setInt(KEY_TRACKLOG_MIN_INTERVAL,val);
	}
	*/
	
	public static int getResendInterval() {
		return getInt(KEY_RESEND_INTERVAL,DEFAULT_RESEND_INTERVAL);
	}
	
	public static void setResendInterval(int val) {
		setInt(KEY_RESEND_INTERVAL,val);
	}

	public static int getReceiveReconnectInterval() {
		return getInt(KEY_RECEIVE_RECONNECT_INTERVAL,DEFAULT_RECEIVE_RECONNECT_INTERVAL);
	}
	
	public static void setReceiveReconnectInterval(int val) {
		setInt(KEY_RECEIVE_RECONNECT_INTERVAL,val);
	}
	
	/*
	public static int getSegmentNPoints() {
		return getInt(KEY_SEGMENT_NPOINTS,DEFAULT_SEGMENT_NPOINTS);
	}
	
	public static void setSegmentNPoints(int val) {
		setInt(KEY_SEGMENT_NPOINTS,val);
	}
	*/
	
	public static GpsDriver getGpsDriver() {
		GpsDriver[] all = GpsDriver.getAllDrivers();
		String ident = getString(KEY_GPS_DRIVER);
		if (ident != null) {
			for (int i = 0; i < all.length; i ++)
				if (all[i].getDriverId().compareTo(ident) == 0)
					return all[i];
		}
		return null;
	}
	
	public static void setGpsDriver(GpsDriver driver) {
		GpsDriver old = getGpsDriver();
		if (old != driver) {
			setGpsDevice(null);
		}
		setString(KEY_GPS_DRIVER,driver.getDriverId());
	}
	
	/**
	 * 
	 * @return GpsDeviceInfo struct containing selected Driver & Device. Returns null if no device is selected
	 */
	public static GpsDeviceInfo getGpsDevice() {
		GpsDriver driver = getGpsDriver();
		String name = getString(KEY_GPS_DEVNAME);
		String address = getString(KEY_GPS_DEVADDRESS);
		if (address == null || address == "")
			return null;
		else
			return new GpsDeviceInfo(driver,name == null ? "" : name,address);
	}
	
	public static void setGpsDevice(GpsDeviceInfo dev) {
		if (dev == null) {
			setString(KEY_GPS_DEVADDRESS,"");
			setString(KEY_GPS_DEVNAME,"");
		}
		else {
			setGpsDriver(dev.getDriver());
			setString(KEY_GPS_DEVADDRESS,dev.getAddress());
			setString(KEY_GPS_DEVNAME,dev.getName());
		}
	}

	public static Profile[] getProfiles() {
		if (_cacheProfiles != null) {
			return _cacheProfiles;
		}
		else {
			String strall = getString(KEY_PROFILES);
			if (strall == null)
				return _cacheProfiles = new Profile[0];
			String[] all = unpackStrings(strall);
			if (all == null)
				return _cacheProfiles = new Profile[0];
			
			Profile[] out = new Profile[all.length];
			int j = 0;
			for (int i = 0; i < out.length; i ++) {
				String[] arr = unpackStrings(all[i]);
				if (arr != null) {
					Profile p = Profile.fromStringArray(arr);
					if (p != null) {
						out[j++] = p;
					}
				}
			}
			
			if (j <  out.length) {
				Profile[] out2 = new Profile[j];
				for (int i = 0; i < j; i ++)
					out2[i] = out[i];
				return _cacheProfiles = out2;
			}
			else {
				return _cacheProfiles = out;
			}
		}
	}

	public static void setProfiles(Profile[] profiles) {
		_cacheProfiles = profiles;
		String[] arr = new String[profiles.length];
		for (int i = 0; i < arr.length; i ++) {
			arr[i] = packStrings(profiles[i].toStringArray());
		}
		setString(KEY_PROFILES,packStrings(arr));
	}

	public static Profile getLastTrackProfile() {
		String str = getString(KEY_LASTTRACK_PROFILE);
		if (str == null)
			return null;
		String[] arr = unpackStrings(str);
		if (arr == null)
			return null;
		return Profile.fromStringArray(arr);
	}
	
	public static void setLastTrackProfile(Profile p) {
		setString(KEY_LASTTRACK_PROFILE,packStrings(p.toStringArray()));
	}
	
	public static void setLastTrackKey(String key) {
		setString(KEY_LASTTRACK_KEY,key);
	}
	
	public static String getLastTrackKey() {
		String val = getString(KEY_LASTTRACK_KEY);
		if (val == "")
			return null;
		else
			return val;
	}
	
	public static double getWidgetPageRepaintInterval() {
		return getDouble(KEY_WIDGETPAGE_REPAINT_INTERVAL,DEFAULT_WIDGETPAGE_REPAINT_INTERVAL);
	}
	
	public static void setWidgetPageRepaintInterval(double val) {
		setDouble(KEY_WIDGETPAGE_REPAINT_INTERVAL,val);
	}
	
	public static WidgetPosition[] getWidgetLayout() {
		String str = getString(KEY_WIDGET_LAYOUT);
		if (str == null)
			return null;
		String[] arr = Config.unpackStrings(str);
		if (arr == null)
			return null;
		
		Vector vect = new Vector();
		for (int i = 0; i < arr.length; i ++) {
			WidgetPosition wp = WidgetPosition.load(arr[i]);
			if (wp != null)
				vect.addElement(wp);
		}
		
		if (vect.size() == 0)
			return null;
		
		WidgetPosition[] wp = new WidgetPosition[vect.size()];
		for (int i = 0; i < wp.length; i ++)
			wp[i] = (WidgetPosition)vect.elementAt(i);
		return wp;
	}
	
	public static void setWidgetLayout(WidgetPosition[] wp) {
		String[] arr = new String[wp.length];
		for (int i = 0; i < arr.length; i ++)
			arr[i] = wp[i].save();
		setString(KEY_WIDGET_LAYOUT,packStrings(arr));
	}
	
	
	public static boolean getKeepBacklight() {
		return getBoolean(KEY_KEEP_BACKLIGHT,false);
	}
	
	public static void setKeepBacklight(boolean val) {
		setBoolean(KEY_KEEP_BACKLIGHT,val);
	}
	
	public static int getBacklightLevel() {
		return getInt(KEY_BACKLIGHT_LEVEL,DEFAULT_BACKLIGHT_LEVEL);
	}
	
	public static void setBacklightLevel(int level) {
		setInt(KEY_BACKLIGHT_LEVEL,level);
	}
		
	public static final int PROTOCOL_UDP = 0;
	public static final int PROTOCOL_TCP = 1;
//	public static final int PROTOCOL_HTTP = 2;
	
	public static final int HTTP_PING_NEVER = 0;
	public static final int HTTP_PING_ONCE = 1;
	
	private static final String DEFAULT_UDP_HOST = "81.0.213.99"; //"update.live-tracking.org";
	private static final int DEFAULT_UDP_PORT = 5483;
	private static final String DEFAULT_TCP_HOST = "81.0.213.99"; //"update.live-tracking.org";
	private static final int DEFAULT_TCP_PORT = 5483;
	private static final int DEFAULT_PROTOCOL = PROTOCOL_UDP;
	private static final String DEFAULT_WSSERVER_URL = "ws://update.live-tracking.org:80/tracker";
	
	private static final int DEFAULT_HTTP_PING_MODE = HTTP_PING_NEVER;
	private static final int DEFAULT_RESEND_INTERVAL = 60*1000;		// milliseconds
	private static final int DEFAULT_RECEIVE_RECONNECT_INTERVAL = 30*1000;	// milliseconds
	private static final int DEFAULT_BACKLIGHT_LEVEL = 50;
	private static final double DEFAULT_WIDGETPAGE_REPAINT_INTERVAL = 0.5;
	
	private static final byte KEY_GPS_DRIVER = 0;
	private static final byte KEY_GPS_DEVADDRESS = 1;
	private static final byte KEY_GPS_DEVNAME = 2;
	private static final byte KEY_UDPSERVER_HOST = 3;
	private static final byte KEY_UDPSERVER_PORT = 4;
	private static final byte KEY_WIDGETPAGE_REPAINT_INTERVAL = 5;
	private static final byte KEY_PROFILES = 6;
	private static final byte KEY_LASTTRACK_KEY = 7;
	private static final byte KEY_PROTOCOL = 8;
	private static final byte KEY_CONFIG_VERSION = 9;
	private static final byte KEY_HTTP_PING_MODE = 10;
	private static final byte KEY_TCPSERVER_HOST = 11;
	private static final byte KEY_TCPSERVER_PORT = 12;
	private static final byte KEY_BACKLIGHT_LEVEL = 13;
	private static final byte KEY_DEBUG_MODE = 14;
	private static final byte KEY_ZLIB = 15;
	private static final byte KEY_RESEND_INTERVAL = 16;
	private static final byte KEY_RECEIVE_RECONNECT_INTERVAL = 17;
	private static final byte KEY_WIDGET_LAYOUT = 18;
	private static final byte KEY_LASTTRACK_PROFILE = 19;
	private static final byte KEY_KEEP_BACKLIGHT = 20;
	
	private static final byte KEY_MAX = 20;
	
	
	private static final byte BYTE_ENCODING_DEFAULT = 1;
	private static final byte BYTE_ENCODING_UTF8 = 2;
	
	private static Profile[] _cacheProfiles;

	private static boolean getBoolean(int key, boolean defval) {
		String s = getString(key);
		if (s == null)
			return defval;
		else if (s.equals("0"))
			return false;
		else if (s.equals("1"))
			return true;
		else
			return defval;
	}
	
	private static void setBoolean(int key, boolean val) {
		setString(key,val ? "1" : "0");
	}

	private static void setInt(int key, int value) {
		setString(key,""+value);
	}
	
	private static int getInt(int key, int defval) {
		String s = getString(key);
		if (s == null || s.equals("")) {
			return defval;
		}
		else {
			return Integer.parseInt(s);
		}
	}
	
	private static void setDouble(int key, double value) {
		setString(key,""+value);
	}
	
	private static double getDouble(int key, double defval) {
		String s = getString(key);
		if (s == null || s.equals("")) {
			return defval;
		}
		else {
			return Double.parseDouble(s);
		}
	}
	
	private static String getString(int key) {
		Record r = _records[key];
		if (r != null && r.data.length >= 2) {
			if (r.data.length >= 2 && r.data[1] == BYTE_ENCODING_UTF8)
				return UTF8.decode(r.data,2,r.data.length-2);
			else if (r.data.length >= 2 && r.data[1] == BYTE_ENCODING_DEFAULT)
				return new String(r.data,2,r.data.length-2);
			else
				return new String(r.data,1,r.data.length-1);	// fallback for prehistoric version
		}
		else
			return null;
	}
		
	private static void setString(int key, String val) {
		Record r;
		if (_records[key] == null) {
			r = new Record();
			r.recordStoreId = -1;
			_records[key] = r;
		}
		else {
			r = _records[key];
		}
		
		if (val == null) val = "";
	
		byte[] newdata;
		newdata = UTF8.encode(val);
		
		r.data = new byte[2+newdata.length];
		r.data[0] = (byte)key;
		r.data[1] = BYTE_ENCODING_UTF8;
		for (int i = 0; i < newdata.length; i ++)
			r.data[2+i] = newdata[i];
		r.isModified = true;
	}
		
	public static String packStrings(String[] arr) {
		StringBuffer sb = new StringBuffer();

		sb.append(arr.length);
		sb.append(',');
		
		for (int i = 0; i < arr.length; i ++) {
			sb.append(arr[i].length());
			sb.append(',');
		}
		
		for (int i = 0; i < arr.length; i ++)
			sb.append(arr[i]);
		
		return sb.toString();
	}

	public static String[] unpackStrings(String str) {
		String[] out;
		int[] lens;
		int startPos;
		int lastPos;
		int count;
		
		if (str == null)
			return null;
		
		startPos = str.indexOf(',');
		if (startPos < 0)
			return null;
		
		count = Integer.parseInt(str.substring(0, startPos));
		
		lens = new int[count];
		lastPos = startPos;
		for (int i = 0; i < count; i ++) {
			int pos = str.indexOf(',',lastPos+1);
			if (pos < 0) return null;
			lens[i] = Integer.parseInt(str.substring(lastPos+1, pos));
			lastPos = pos;
		}
		
		lastPos ++;
		out = new String[count];
		for (int i = 0; i < count; i ++) {
			if (lastPos+lens[i] > str.length())
				return null;
			out[i] = str.substring(lastPos, lastPos+lens[i]);
			lastPos += lens[i];
		}
		
		if (lastPos != str.length())
			return null;
		
		return out;
	}
	
	
	private static void readAll() {
		_records = new Record[KEY_MAX+1];
		
		RecordStore rs;
		try {
			rs = RecordStore.openRecordStore("config",true);
			RecordEnumeration re = rs.enumerateRecords(null, null, false);
			while (re.hasNextElement()) {
				Record r = new Record();
				r.recordStoreId = re.nextRecordId();
				r.data = rs.getRecord(r.recordStoreId);
				r.isModified = false;
				if (r.data.length >= 1 && r.data[0] <= KEY_MAX) {
					_records[r.data[0]] = r;
				}
			}
			rs.closeRecordStore();
		}
		catch (Exception e) {
			Util.showError("Cannot read user settings!", e);
		}
	}
	
	public static void writeAll() {
		RecordStore rs;
		
		try {
			rs = RecordStore.openRecordStore("config", true);
			for (int i = 0; i <= KEY_MAX; i ++) {
				Record r = _records[i];
				if (r != null && r.isModified) {
					if (r.recordStoreId < 0) {
						r.recordStoreId = rs.addRecord(r.data, 0, r.data.length);
					}
					else {
						rs.setRecord(r.recordStoreId, r.data, 0, r.data.length);
					}
					r.isModified = false;
				}
			}
			rs.closeRecordStore();
		}
		catch (Exception e) {
			Util.showError("Cannot save user settings!", e);
		}
	}
	
	
	private static Record[] _records;
	private static String _lastRunVersion;
}
