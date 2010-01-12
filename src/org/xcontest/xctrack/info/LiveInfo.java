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


public class LiveInfo {
	
	public class SessionInfo {
		String _domain;
		String _username;
		String _nickname;
		boolean _isAnonymous;
		
		public boolean isAnonymous() {
			return _isAnonymous;
		}
		
		public String getDomain() {
			return _domain;
		}
		
		public String getUsername() {
			return _username;
		}
		
		public String getNickname() {
			return _nickname;
		}
	}
	
	private int _confirmedMessages;
	private int _pendingMessages;
	private int _confirmedPoints;
	private int _pendingPoints;
	private int _bytesSent;
	private int _bytesReceived;
	private boolean _isConnected;
	private long _trackingStartTime;
	
	private SessionInfo _sessionInfo;
	
	LiveInfo() {
		reset();
	}
	
	synchronized void reset() {
		_confirmedPoints = 0;
		_pendingPoints = 0;
		_confirmedMessages = 0;
		_pendingMessages = 0;
		_bytesSent = 0;
		_bytesReceived = 0;
		_isConnected = false;
		_sessionInfo = null;
		_trackingStartTime = -1;
	}

	synchronized void startTracking() {
		_trackingStartTime = System.currentTimeMillis();
	}
	
	public double getTrackingTime() {
		if (_trackingStartTime >= 0)
			return (System.currentTimeMillis()-_trackingStartTime)/1000.0;
		else
			return 0;
	}
	
	synchronized void setConnected(boolean val) {
		_isConnected = val;
	}
	
	public synchronized boolean isConnected() {
		return _isConnected;
	}
	
	synchronized void setAnonymousSession(String nickname) {
		_sessionInfo = new SessionInfo();
		_sessionInfo._isAnonymous = true;
		_sessionInfo._nickname = nickname;
	}
	
	synchronized void setRegisteredSession(String username, String domain) {
		_sessionInfo = new SessionInfo();
		_sessionInfo._isAnonymous = false;
		_sessionInfo._username = username;
		_sessionInfo._domain = domain;
	}
	
	public synchronized SessionInfo getSessionInfo() {
		return _sessionInfo;
	}
	
	synchronized void setClosedSession() {
		_sessionInfo = null;
		_trackingStartTime = -1;
	}
	
	synchronized void setConfirmedMessages(int val) {
		_confirmedMessages = val;
	}
	
	public synchronized int getConfirmedMessages() {
		return _confirmedMessages;
	}
	
	synchronized void setPendingMessages(int val) {
		_pendingMessages = val;
	}
	
	public synchronized int getPendingMessages() {
		return _pendingMessages;
	}
	
	synchronized void setConfirmedPoints(int val) {
		_confirmedPoints = val;
	}
	
	public synchronized int getConfirmedPoints() {
		return _confirmedPoints;
	}
	
	synchronized void setPendingPoints(int val) {
		_pendingPoints = val;
	}
	
	public synchronized int getPendingPoints() {
		return _pendingPoints;
	}
	
	synchronized void setBytesSent(int val) {
		_bytesSent = val;
	}
	
	public synchronized int getBytesSent() {
		return _bytesSent;
	}
	
	synchronized void setBytesReceived(int val) {
		_bytesReceived = val;
	}
	
	public synchronized int getBytesReceived() {
		return _bytesReceived;
	}
}
