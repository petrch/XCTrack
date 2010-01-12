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

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.xcontest.live.GpsPoint;
import org.xcontest.live.LiveClient;
import org.xcontest.live.LiveClientListener;
import org.xcontest.live.LiveConnection;
import org.xcontest.live.LiveTCPConnection;
import org.xcontest.live.LiveUDPConnection;
import org.xcontest.live.Logger;
import org.xcontest.live.TrackType;
import org.xcontest.xctrack.App;
import org.xcontest.xctrack.Util;
import org.xcontest.xctrack.config.Config;
import org.xcontest.xctrack.gps.GpsDeviceInfo;
import org.xcontest.xctrack.gps.GpsDriver;
import org.xcontest.xctrack.gps.GpsListener;
import org.xcontest.xctrack.gps.GpsMessage;
import org.xcontest.xctrack.settings.Profile;
import org.xcontest.xctrack.util.Log;
import org.xcontest.xctrack.widget.WidgetPage;

class LiveLogger implements Logger {
	LiveLogger() {
	}

	public void info(String message) {
		Log.info(message);
	}
	
	public void error(String message) {
		Log.error(message);
	}
	
	public void error(String message, Throwable ex) {
		Log.error(message,ex);
	}
}


public class InfoCenter extends Thread
		implements LiveClientListener, GpsListener {

	private final int BACKLIGHT_REFRESH_INTERVAL=5000;	// ms
	
	private final int STATE_STOPPED=1;
	private final int STATE_LOGIN=2;
	private final int STATE_TRACKING=3;
	
	private final int ACTION_STOP_TRACKING=1;
	private final int ACTION_CONTINUE_TRACKING=2;
	private final int ACTION_START_TRACKING=3;
	private final int ACTION_LOGIN_OK = 4;
	private final int ACTION_LOGIN_FAILED = 5;
	private final int ACTION_UPDATE_LIVE_STATUS = 6;
	private final int ACTION_BACKLIGHT_ON = 7;
	private final int ACTION_BACKLIGHT_OFF = 8;
	private final int ACTION_EXIT = 9;
	
	
	private LocationInfo _locationInfo;
	private LiveInfo _liveInfo;
	private LogInfo _logInfo;
	
	private LiveClient _live;
	private WidgetPage _widgetPage;

	
	// modified in InfoCenter thread only
	private int _state;
	private LiveConnection _conn;
	private GpsDriver _gpsDriver;
	
	private boolean _hasBacklight;
	private boolean _keepsBacklight;
	private long _nextBacklightRefresh;
	private long _nextWidgetPageRepaint;

	
	// modified from more threads
	private Profile _activeProfile;
	private double _nextLivePointTime;
	
	// queues & info forwarding to thread
	private Vector _queueActions;
	private Vector _queueLivePoints;
	//////////////////////////
	
	

	private static InfoCenter _instance;
	
	public static InfoCenter getInstance() {
		return _instance;
	}
	
	public LocationInfo getLocationInfo() {
		return _locationInfo;
	}
	
	public LiveInfo getLiveInfo() {
		return _liveInfo;
	}
	
	public LogInfo getLogInfo() {
		return _logInfo;
	}
	
	
	/*******************************************************
	 * 
	 *               Commands
	 * 
	 */
	
	public synchronized boolean keepsBacklight() {
		return _keepsBacklight;
	}
	
	public synchronized void keepBacklight(boolean val) {
		if (val)
			_queueActions.addElement(new Integer(ACTION_BACKLIGHT_ON));
		else
			_queueActions.addElement(new Integer(ACTION_BACKLIGHT_OFF));
		notify();
	}
	
	public synchronized void exit() {
		if (_state != STATE_STOPPED) {	// if we are tracking stop it
			_queueActions.addElement(new Integer(ACTION_STOP_TRACKING));
		}
		_queueActions.addElement(new Integer(ACTION_EXIT));
		notify();
	}
	
	public synchronized void stopTracking() {
		if (_state != STATE_STOPPED) {
			_queueActions.addElement(new Integer(ACTION_STOP_TRACKING));
			notify();
		}
	}
	
	
	public synchronized void continueTracking() {
		if (_state == STATE_STOPPED) {
			_activeProfile = Config.getLastTrackProfile();
			Util.expect(_activeProfile != null, "continueTracking(): Last track profile not found!");
			_queueActions.addElement(new Integer(ACTION_CONTINUE_TRACKING));
			notify();
		}
	}

	public synchronized void startTracking(Profile profile) {
		if (_state == STATE_STOPPED) {
			_activeProfile = profile;
			_queueActions.addElement(new Integer(ACTION_START_TRACKING));
			notify();
		}	
	}
	
	
	/*******************************************************
	 * 
	 *               Event handling
	 *  
	 */


	// GpsListener
	public void deviceConnected() {
		_locationInfo.setGpsConnected(true);
	}

	// GpsListener
	public void deviceDisconnected() {
		_locationInfo.setGpsConnected(false);
	}

	// GpsListener
	public void signalLost() {
		_locationInfo.setHasGpsSignal(false);
	}

	// GpsListener
	public void signalReached() {
		_locationInfo.setHasGpsSignal(true);
	}

	// GpsListener
	public synchronized void gpsMessage(GpsMessage msg) {
		// pass message to locationInfo
		_locationInfo.update(msg);

		// process the message for passing the position to LiveClient
		if (msg.hasPosition) {
			long time;
			if (msg.hasTime)
				time = msg.time;
			else
				time = (long)(_locationInfo.computeTime()*1000);
			
			if (_nextLivePointTime*1000 <= time) {
				GpsPoint p = new GpsPoint();
				p.setTime(time);
				p.lat = msg.lat;
				p.lon = msg.lon;
				p.alt = msg.hasAltitude ? msg.altitude : 0;
				_queueLivePoints.addElement(p);
				_nextLivePointTime += _activeProfile.getTracklogInterval();
				if (_nextLivePointTime < p.time)
					_nextLivePointTime = p.time;
				notify();
			}
		}
	}
	
	
	// LiveClientListener
	public synchronized void connectionOpened(String remote) {
		if (_state != STATE_STOPPED) {
			_liveInfo.setConnected(true);
		}
	}

	// LiveClientListener
	public synchronized void openConnectionFailed(String remote) {
		if (_state != STATE_STOPPED) {
			_liveInfo.setConnected(false);
		}
	}

	// LiveClientListener - start tracking successful
	public synchronized void loginOK(String key, String domain, String username) {
		if (_state == STATE_LOGIN) {
			Config.setLastTrackKey(key);
			Config.setLastTrackProfile(_activeProfile);
			Config.writeAll();
			_queueActions.addElement(new Integer(ACTION_LOGIN_OK));
			notify();
		}
	}

	// LiveClientListener
	public synchronized void loginFailed(String domain, String username) {
		if (_state == STATE_LOGIN) {
			_queueActions.addElement(new Integer(ACTION_LOGIN_FAILED));
			notify();
		}
	}

	// LiveClientListener
	public synchronized void statusUpdate() {
		if (_state != STATE_STOPPED) {
			_queueActions.addElement(new Integer(ACTION_UPDATE_LIVE_STATUS));
			notify();
		}
	}

	
	
	
	
	public InfoCenter() {
		Util.expect(_instance == null, "Second instance of infocenter created!?!");
		_instance = this;
		
		_locationInfo = new LocationInfo();
		_liveInfo = new LiveInfo();
		_logInfo = new LogInfo();
		
		_queueActions = new Vector();
		_queueLivePoints = new Vector();
		
		_state = STATE_STOPPED;
		_nextWidgetPageRepaint = -1;
		_nextBacklightRefresh = -1;
		_hasBacklight = Util.hasBacklightSetting();
		
		_live = new LiveClient();
		_widgetPage = new WidgetPage();

		_live.addListener(this);
		_live.addLogger(new LiveLogger());
		
		_keepsBacklight = Config.getKeepBacklight();
	}
	
	/****************************************************************************************
	 * 
	 *                    Worker LOOP
	 * 
	 */

	private void createConnection() {
		if (Config.getProtocol() == Config.PROTOCOL_UDP)
			_conn = new LiveUDPConnection(Config.getUDPServerHost(),Config.getUDPServerPort());
		else if (Config.getProtocol() == Config.PROTOCOL_TCP)
			_conn = new LiveTCPConnection(Config.getTCPServerHost(),Config.getTCPServerPort());
		else
			Util.expect(false, "createConnection() invalid connection type");
		_conn.useZLib(Config.getUseZLib());
		_conn.setResendInterval(Config.getResendInterval());
		_conn.setReceiveReconnectInterval(Config.getReceiveReconnectInterval());
		_live.setConnection(_conn);
	}
	
	private void ping() {
		int pingMode = Config.getHTTPPingMode();
		if (pingMode == Config.HTTP_PING_ONCE) {
			new Thread(){
				public void run() {
					try {
						Log.debug("HTTP Ping: start");
						StreamConnection conn = (StreamConnection)Connector.open("http://live.xcontest.org");
						InputStream is = conn.openInputStream();
						int cnt = 0;
						while (is.read() != -1) cnt ++;
						Log.debug("HTTP Ping: request finished - received "+cnt+" bytes");
					}
					catch (IOException e) {
						Log.error("HTTP Ping failed",e);
						Util.showInfo("HTTP Ping failed: "+e.getMessage());
					}
				}
			}.start();
		}
	}
	
	
	public void run() {
		Log.info("InfoCenter: started");
		try {
			while (true) {
				// deliver new points to Live tracker
				GpsPoint livePoint;
				if (_state != STATE_STOPPED) {
					while(true) {
						synchronized(this) {
							if (_queueLivePoints.size() == 0) break;
	
							livePoint = (GpsPoint)_queueLivePoints.elementAt(0);
							_queueLivePoints.removeElementAt(0);
						}
						if (_state == STATE_LOGIN || _state == STATE_TRACKING) {
							_live.addPoint(livePoint);
						}
						_liveInfo.setPendingPoints(_live.getPendingPoints());
					}
					
					_live.checkSendPositionMessage();
				}
				
				// process ACTIONS
				Profile activeProfile;
				int action;
				while(true) {
					synchronized(this) {
						if (_queueActions.size() == 0) break;

						action = ((Integer)_queueActions.elementAt(0)).intValue();
						activeProfile = _activeProfile;
						_queueActions.removeElementAt(0);
					}

					// STOP TRACKING
					if (action == ACTION_STOP_TRACKING) {
						if (_state != STATE_STOPPED) {
							Log.info("STOP Tracking");
							_liveInfo.setClosedSession();
							_gpsDriver.disconnect();
							_gpsDriver.setListener(null);
							_live.closeSession();
							App.hideScreen(_widgetPage);
							synchronized(this) {
								_state = STATE_STOPPED;
							}
						}
					}
					
					// LOGIN OK
					else if (action == ACTION_LOGIN_OK) {
						synchronized(this) {
							if (_activeProfile.isAnonymous())
								_liveInfo.setAnonymousSession(_activeProfile.getNickname());
							else
								_liveInfo.setRegisteredSession(_activeProfile.getUsername(), _activeProfile.getDomain());
							_state = STATE_TRACKING;
						}
					}
					
					// LOGIN FAILED
					else if (action == ACTION_LOGIN_FAILED) {
						_liveInfo.setClosedSession();
						_gpsDriver.disconnect();
						_gpsDriver.setListener(null);
						_live.closeSession();
						App.hideScreen(_widgetPage);
						Util.showError("Login to server FAILED!\nPlease go to Settings -> Profiles and correct your username/password settings");
						synchronized(this) {
							_state = STATE_STOPPED;
						}
					}
					
					// START/CONTINUE TRACKING
					else if (action == ACTION_START_TRACKING || action == ACTION_CONTINUE_TRACKING) {
						if (_state == STATE_STOPPED) {
							ping();

							_locationInfo.reset();
							_liveInfo.reset();
							_liveInfo.startTracking();

							synchronized(this) {
								_nextLivePointTime = 0;
								_state = STATE_LOGIN;
								_queueLivePoints.removeAllElements();
							}
							_widgetPage.show();
							
							createConnection();

							GpsDeviceInfo dev = Config.getGpsDevice();
							_gpsDriver = dev.getDriver();
							_gpsDriver.setListener(this);
							_gpsDriver.connect(dev.getAddress());
							
							_live.setMessageInterval(activeProfile.getMessageInterval());
							
							if (action == ACTION_START_TRACKING) {
								_live.setPublic(true);
								_live.setTrackType(_gpsDriver.isForDebugModeOnly() ? TrackType.DEMO.getValue() : activeProfile.getTrackType());
								if (activeProfile.isAnonymous())
									_live.openAnonymousSession(activeProfile.getFirstname(), activeProfile.getSurname(), activeProfile.getNickname());
								else
									_live.openRegisteredSession(activeProfile.getDomain(), activeProfile.getUsername(), activeProfile.getPassword());
							}
							else {
								_live.continueSession(Config.getLastTrackKey());
							}
						}
					}
					
					// update LIVE STATUS
					else if (action == ACTION_UPDATE_LIVE_STATUS) {
						if (_state != STATE_STOPPED) {
							_liveInfo.setBytesReceived(_conn.getTotalReceivedBytes());
							_liveInfo.setBytesSent(_conn.getTotalSentBytes());
							_liveInfo.setPendingMessages(_live.getPendingMessages());
							_liveInfo.setConfirmedMessages(_live.getConfirmedMessages());
							_liveInfo.setPendingPoints(_live.getPendingPoints());
							_liveInfo.setConfirmedPoints(_live.getConfirmedPoints());
						}
					}
					
					else if (action == ACTION_BACKLIGHT_ON) {
						_keepsBacklight = true;
					}
					
					else if (action == ACTION_BACKLIGHT_OFF) {
						_keepsBacklight = false;
					}
					
					// EXIT
					else if (action == ACTION_EXIT) {
						break;
					}
				} // process actions - while

				long now = System.currentTimeMillis();
				long wakeUp=now+10000;
				if (_state != STATE_STOPPED) {
					if (_hasBacklight && _keepsBacklight && _nextBacklightRefresh <= now) {
						Util.setBacklight(Config.getBacklightLevel());
						_nextBacklightRefresh = now+BACKLIGHT_REFRESH_INTERVAL;
					}
					if (_nextWidgetPageRepaint <= now) {
						int repaint = (int)(1000*Config.getWidgetPageRepaintInterval());
						int minwait = repaint/10;
						_nextWidgetPageRepaint += repaint;
						if (_nextWidgetPageRepaint < now+minwait)
							_nextWidgetPageRepaint = now+minwait;
						_widgetPage.doFullRepaint();
					}
					if (wakeUp > _nextBacklightRefresh)
						wakeUp = _nextBacklightRefresh;
					if (wakeUp > _nextWidgetPageRepaint)
						wakeUp = _nextWidgetPageRepaint;
				}

				long delay = wakeUp - System.currentTimeMillis();
				if (delay > 0) {
					synchronized(this) {
						wait(delay);
					}
				}
				
			}
		}
		catch(InterruptedException e) {
			Log.error("InfoCenter: Thread interrupted - exiting");
		}
		catch(Throwable e) {
			Log.error("InfoCenter: FATAL",e);
		}
		Log.error("InfoCenter: Thread finished normally - exiting");
	}
}
