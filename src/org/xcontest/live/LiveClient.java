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

package org.xcontest.live;

import java.util.Vector;

import org.xcontest.live.message.RequestOpen;
import org.xcontest.live.message.RequestOpenAnonymous;
import org.xcontest.live.message.RequestOpenContinue;
import org.xcontest.live.message.RequestOpenRegistered;
import org.xcontest.live.message.RequestTrackPart;
import org.xcontest.live.message.ResponseOpenLoginFailed;
import org.xcontest.live.message.ResponseOpenOk;
import org.xcontest.live.message.ResponseTrackPart;
import org.xcontest.live.message.ServerMessage;


public class LiveClient {
	
	public final static int DEFAULT_RESEND_INTERVAL = 60*1000;
	public final static int DEFAULT_RECEIVE_RECONNECT_INTERVAL = 30*1000;
	public final static double DEFAULT_MESSAGE_INTERVAL = 60;

	/**
	 * Neither reader nor writer thread is running, session is closed
	 */
	public static final int STATE_CLOSED = 1;
	
	/**
	 * Both threads are running, not yet received ResponseOpenOk
	 */
	public static final int STATE_OPENING = 2;
	
	/**
	 * Both threads are running, session is opened (received ResponseOpenOk)
	 */
	public static final int STATE_OPEN = 3;
	
	/**
	 * Both threads are running, trying to send out remaining messages
	 */
	public static final int STATE_CLOSING = 4; 
	
	public LiveClient() {
		_conn = null;
		_listeners = new Vector();
		_pointBuffer = new Vector();
		_loggers = new Vector();
		
		_state = STATE_CLOSED;
		
		_trackType = TrackType.DEFAULT.getValue();
		_competitionKey = null;
		_isPublic = true;

		_messageInterval = (long)(1000*DEFAULT_MESSAGE_INTERVAL);
		_nextMessageTime = -1;
	}
	
	/**
	 * 
	 * @param npoints number of tracklog points to form one message
	 */
	public synchronized void setMessageInterval(double val) {
		_messageInterval = (long)(val*1000);
		_nextMessageTime = -1;
	}
	
	public synchronized void setConnection(LiveConnection conn) {
		if (_state != STATE_CLOSED)
			throw new ArgumentError("Cannot setConnection() - tracking already started");
		_conn = conn;
		_conn.setClient(this);
	}
	
	public void addListener(LiveClientListener l) {
		synchronized(this) {
			_listeners.addElement(l);
		}
	}
	
	public void removeListener(LiveClientListener l) {
		synchronized(this) {
			_listeners.removeElement(l);
		}
	}
	
	public void addLogger(Logger l) {
		synchronized(_loggers) {
			_loggers.addElement(l);
		}
	}
	
	public void logInfo(String msg) {
		synchronized(_loggers) {
			for (int i = 0; i < _loggers.size(); i ++)
				((Logger)_loggers.elementAt(i)).info(msg);
		}
	}
	
	public void logError(String msg) {
		synchronized(_loggers) {
			for (int i = 0; i < _loggers.size(); i ++)
				((Logger)_loggers.elementAt(i)).error(msg);
		}
	}

	public void logError(String msg,Throwable e) {
		synchronized(_loggers) {
			for (int i = 0; i < _loggers.size(); i ++)
				((Logger)_loggers.elementAt(i)).error(msg,e);
		}
	}
	
	public synchronized void setTrackType(String trackType) {
		_trackType = trackType;
	}
	
	public synchronized void setCompetitionKey(String competitionKey) {
		_competitionKey = competitionKey;
	}
	
	public synchronized void setPublic(boolean isPublic) {
		_isPublic = isPublic;
	}
	
	private synchronized void startTracking() {
		if (_conn == null) {
			throw new Error("Cannot start tracking - set LiveConnection first!");
		}
		
		if (_state != STATE_CLOSED)
			closeSession();
		_state = STATE_OPENING;

		_pointBuffer.removeAllElements();

		_sender = new LiveSender(this,_conn,_openRequest);
		_sender.start();
		
		_receiver = new LiveReceiver(this,_conn);
		_receiver.start();
	}
	
	public synchronized void openRegisteredSession(String domain, String username, String password) {
		_openRequest = new RequestOpenRegistered(domain,username,password,_trackType,_competitionKey,_isPublic);
		startTracking();
	}
	
	public synchronized void openAnonymousSession(String firstname, String surname, String nickname) {
		_openRequest = new RequestOpenAnonymous(firstname,surname,nickname,_trackType,_competitionKey,_isPublic);
		startTracking();
	}
	
	public synchronized void continueSession(String key) {
		_key = key;
		_openRequest = new RequestOpenContinue(key);
		startTracking();
	}
	
	public synchronized void closeSession() {
		if (_state != STATE_CLOSED) {
			_state = STATE_CLOSED;
			_sender.stop();
			_receiver.stop();
			_conn.close();
			_nextMessageTime = -1;
		}
	}
	
	public synchronized void addPoint(GpsPoint p) {
		_pointBuffer.addElement(p);
		
		if (_nextMessageTime < 0) {
			_nextMessageTime = System.currentTimeMillis() + _messageInterval;
		}
	}
	
	public synchronized void checkSendPositionMessage() {
		int n = _pointBuffer.size();
		
		if (_sender != null && n > 0) {
			long now = System.currentTimeMillis();
			if (_nextMessageTime <= now) {	// time for next message
				RequestTrackPart msg = new RequestTrackPart(_segmentSeq,_pointBuffer);
				_pointBuffer.removeAllElements();
				_segmentSeq ++;
				_sender.addTrackMessage(msg);
				
				_nextMessageTime += _messageInterval;
				if (_nextMessageTime < now)
					_nextMessageTime = now;
				
				for (int i = 0; i < _listeners.size(); i ++)
					((LiveClientListener)_listeners.elementAt(i)).statusUpdate();
			}
		}
	}
	
	public synchronized int getConfirmedMessages() {
		if (_sender != null)
			return _sender.getOKCounter();
		else
			return 0;
	}
	
	public synchronized int getPendingMessages() {
		if (_sender != null)
			return _sender.getPendingCounter();
		else
			return 0;
	}
	
	public synchronized int getConfirmedPoints() {
		if (_sender != null)
			return _sender.getConfirmedPoints();
		else
			return 0;
	}
	
	public synchronized int getPendingPoints() {
		if (_sender != null)
			return _sender.getPendingPoints() + _pointBuffer.size();
		else
			return _pointBuffer.size();
	}
	
	// Notification from RECEIVER thread
	synchronized void receivedMessage(ServerMessage msg) {
		if (msg instanceof ResponseOpenOk) {
			ResponseOpenOk resp = (ResponseOpenOk)msg;
			boolean doNotify = false;
			String recvDomain = null;
			String recvUsername = null;
			String key = null;
			
			if (_state == STATE_OPENING) {
				recvDomain = resp.getDomain();
				recvUsername = resp.getUsername();
				doNotify = true;

				if (resp.getKey() != null)
					_key = key = resp.getKey();
				else
					key = _key;
				
				_segmentSeq = resp.getSeqCount();
				
				_state = STATE_OPEN;
			}

			if (doNotify) {
				_sender.receivedOpenResponseOk(resp);
				synchronized(this) {
					for (int i = 0; i < _listeners.size(); i ++)
						((LiveClientListener)_listeners.elementAt(i)).loginOK(key, recvDomain, recvUsername);
				}
			}
		}
		else if (msg instanceof ResponseOpenLoginFailed) {
			ResponseOpenLoginFailed resp = (ResponseOpenLoginFailed)msg;

			// notify listeners
			synchronized(this) {
				for (int i = 0; i < _listeners.size(); i ++)
					((LiveClientListener)_listeners.elementAt(i)).loginFailed(resp.getDomain(),resp.getUsername());
			}
/* this is done from listeners through closeSession()
			_state = STATE_CLOSED;
			_conn.close();
			_sender.stop();
			_receiver.stop();	// interrupt meeee
*/
		}
		else if (msg instanceof ResponseTrackPart) {
			ResponseTrackPart track = (ResponseTrackPart)msg;
			_sender.receivedTrackPartResponse(track);
		}

		synchronized(this) {
			for (int i = 0; i < _listeners.size(); i ++)
				((LiveClientListener)_listeners.elementAt(i)).statusUpdate();
		}
	}

	// Notification from RECEIVER thread
	void connectionOpened(String remoteEnd) {
		synchronized(this) {
			for (int i = 0; i < _listeners.size(); i ++)
				((LiveClientListener)_listeners.elementAt(i)).connectionOpened(remoteEnd);
		}
	}

	// Notification from RECEIVER thread
	void connectionOpenFailed(String remoteEnd) {
		synchronized(this) {
			for (int i = 0; i < _listeners.size(); i ++)
				((LiveClientListener)_listeners.elementAt(i)).openConnectionFailed(remoteEnd);
		}
	}
	
	// Notification from SENDER thread
	void messageSent() {
		synchronized(this) {
			for (int i = 0; i < _listeners.size(); i ++)
				((LiveClientListener)_listeners.elementAt(i)).statusUpdate();
		}
	}
	
	private Vector _loggers;			// LOCK
	private LiveConnection _conn;
	private Vector _listeners;			// LOCK   this < _listeners
	private LiveSender _sender;
	private LiveReceiver _receiver;
	private RequestOpen _openRequest;
	private int _state;
	
	private Vector _pointBuffer;
	private int _segmentSeq;
	
	private String _key;
	
	private String _trackType;
	private String _competitionKey;
	private boolean _isPublic;

	private long _messageInterval;
	private long _nextMessageTime;
}

