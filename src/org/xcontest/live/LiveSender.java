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

import java.util.Timer;
import java.util.Vector;

import org.xcontest.live.message.RequestOpen;
import org.xcontest.live.message.RequestOpenContinue;
import org.xcontest.live.message.RequestTrackPart;
import org.xcontest.live.message.ResponseOpenOk;
import org.xcontest.live.message.ResponseTrackPart;

class LiveSender extends Thread {
	public LiveSender(LiveClient live, LiveConnection conn, RequestOpen open) {
		super("LiveSender");
		_conn = conn;
		_live = live;
		_trackMessages = new Vector();
		_openRequest = open;
		_timer = new Timer();
		_weHaveSignal = false;
		
		if (open instanceof RequestOpenContinue) {
			_key = ((RequestOpenContinue)open).getKey();
		}
	}
	
	public synchronized void stop() {
		interrupt();
	}

	public void run() {
		long tLastTry = 0;
		
		_live.logInfo("LiveSender: Started");

		try {
			while (true) {
				boolean msgSent = false;
				boolean weHadSignal = false;
				long now;

				_conn.reconnect();
				
				synchronized(this) {
					weHadSignal = _weHaveSignal;
					_weHaveSignal = false;	// well we still have probably, but it means something like "SEND ALL"
				}
				
				now = System.currentTimeMillis();
				if (weHadSignal) {
					tLastTry = now;
					msgSent = sendAllMessages();
				}
				else if (tLastTry+_conn.getResendInterval() <= now) {
					tLastTry = now;
					msgSent = sendFirstMessage();
				}

				if (msgSent) {
					_live.messageSent();
				}
				
				synchronized(this) {
					if (getPendingCounter() == 0) {
						_weHaveSignal = true;
						wait();
					}
					else {
						long delay = tLastTry + _conn.getResendInterval() - System.currentTimeMillis();
						if (delay > 0)
							wait(delay);
					}
				}
			}
		}
		catch (InterruptedException e) {}
		catch (Throwable e) {
			_live.logError("LiveSender: FATAL",e);
		}
		
		_live.logInfo("LiveSender: Stopped");
	}
	
	// [gps]
	public synchronized void addTrackMessage(RequestTrackPart msg) {
		_trackMessages.addElement(msg);
		_pendingPoints += msg.getPointCount();
		notify();
	}
	
	public synchronized int getPendingCounter() {
		return _trackMessages.size() + (_openRequest != null ? 1 : 0);
	}
	
	public synchronized int getOKCounter() {
		return _nOK;
	}
	
	public synchronized int getPendingPoints() {
		return _pendingPoints;
	}
	
	public synchronized int getConfirmedPoints() {
		return _confirmedPoints;
	}
	
	// [receiver]
	public synchronized void receivedOpenResponseOk(ResponseOpenOk resp) {
		_openRequest = null;	// no longer try to send auth
		if (resp.getKey() != null)	// key is not set for ResponseOpenContinue
			_key = resp.getKey();
		_nOK ++;
		_weHaveSignal = true;	// we are on the signal now!
		notify();				// which is a good reason to wake up!
	}
	
	// [receiver]
	public synchronized void receivedTrackPartResponse(ResponseTrackPart resp) {
		int i = 0;
		while (i < _trackMessages.size()) {
			RequestTrackPart msg = (RequestTrackPart)_trackMessages.elementAt(i);

			if (resp.isConfirmed(msg.getSeq())) {
				// server claims it has already received this message - no need to send it again
				_trackMessages.removeElementAt(i);
				int npts = msg.getPointCount();
				_confirmedPoints += npts;
				_pendingPoints -= npts;
				_nOK ++;
			}
			else {
				i ++;
			}
		}
		
		_weHaveSignal = true;	// we are on the signal now!
		notify();				// which is a good reason to wake up!
	}
	
	private synchronized String getMessageString(int idx) {
		if (_openRequest != null) {
			if (idx == 0)
				return _openRequest.toJSON().toString();
			else
				idx --;
		}
		
		if (_key != null && idx < _trackMessages.size()) {
			RequestTrackPart msg = (RequestTrackPart)_trackMessages.elementAt(idx);
			msg.setKey(_key);
			return msg.toJSON().toString();
		}
		
		return null;
	}
	
	// [sender]
	// returns true if message was sent
	private boolean sendFirstMessage() {
		String msg = null;
		
		if (_conn.startSendingMessages()) {
			msg = getMessageString(0);
			if (msg != null) {
				_live.logInfo("SEND: "+msg);
				_conn.sendStringMessage(msg);
			}
			_conn.finishedSendingMessages();
		}

		return msg != null;
	}
	
	// [sender]
	// returns true if message was sent
	private boolean sendAllMessages() {
		boolean isSent = false;

		if (_conn.startSendingMessages()) {
			for (int i = 0; i < getPendingCounter(); i ++) {
				String msg = getMessageString(i);
				_live.logInfo("SEND: "+msg);
				_conn.sendStringMessage(msg);
				isSent = true;
			}
			_conn.finishedSendingMessages();
		}
		
		return isSent;
	}
	
	int _nOK;
	String _key;
	boolean _weHaveSignal;
	
	int _pendingPoints;
	int _confirmedPoints;

	// messages to send
	RequestOpen _openRequest;
	Vector _trackMessages;
	
	LiveConnection _conn;
	LiveClient _live;
	Timer _timer;
}
