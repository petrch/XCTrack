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

import java.io.InterruptedIOException;


public abstract class LiveConnection {
	protected final int MAX_MESSAGE_SIZE = 65536;
	
	protected LiveClient _client;
	private boolean _useZLib = false;
	private byte[] _zipped = null;			// buffer for ZLib deflate
	private byte[] _unzipped = null;		// buffer for ZLib inflate
	int _sentBytes = 0;
	int _receivedBytes = 0;
	int _receiveReconnectInterval = LiveClient.DEFAULT_RECEIVE_RECONNECT_INTERVAL;
	int _sendInterval = LiveClient.DEFAULT_RESEND_INTERVAL;
	
	/** should reopen the connection if already open */
	abstract boolean open();
	public abstract boolean isOpen();
	
	protected abstract boolean sendMessage(byte[] message, int len);
	protected abstract boolean recvMessage() throws InterruptedException,InterruptedIOException;
	protected abstract byte[] getRecvData();
	protected abstract int getRecvLength();
	
	abstract void close();
	public abstract String getRemoteEnd();
	
	public void useZLib(boolean val) {
		_useZLib = val;
	}
	
	public void setReceiveReconnectInterval(int val) {
		_receiveReconnectInterval = val;
	}
	
	protected int getReceiveReconnectInterval() {
		return _receiveReconnectInterval;
	}
	
	public void setResendInterval(int val) {
		_sendInterval = val;
	}
	
	protected int getResendInterval() {
		return _sendInterval;
	}
	
	protected synchronized boolean reconnect() {
		if (isOpen()) {
			return true;
		}
		else {
			boolean success;
			try {
				success = open();
			}
			catch (SecurityException e) {
				success = false;
			}
			if (success)
				_client.connectionOpened(getRemoteEnd());
			else
				_client.connectionOpenFailed(getRemoteEnd());
			return success;
		}
	}
	
	// returns false if the sending cannot start
	abstract boolean startSendingMessages();
	
	// [sender]
	public abstract void finishedSendingMessages();
	
	// [sender]
	public final void sendStringMessage(String message) {
		byte[] bytes = UTF8.encode(message);
		if (_useZLib) {
			if (_zipped == null)
				_zipped = new byte[65536];
			_zipped[0] = 'Z';
			int len = ZLib.zip(bytes,0,bytes.length,_zipped,1,_zipped.length-1);
			if (len >= 0) {
				sendMessage(_zipped,1+len);
			}
			else {	// deflate failed (small buffer)
				_client.logError("ZLib compression failed (small buffer?). Sending plain text");
				sendMessage(bytes,bytes.length);
			}
		}
		else {
			sendMessage(bytes,bytes.length);
		}
	}
		
	// [receiver]
	public final String recvStringMessage() throws InterruptedException,InterruptedIOException {
		if (recvMessage()) {
			byte[] bytes = getRecvData();
			int len = getRecvLength();
			if (bytes[0] == 'Z') {
				if (_unzipped == null)
					_unzipped = new byte[4096];
				int ulen = ZLib.unzip(bytes, 1, len-1, _unzipped, 0, _unzipped.length);
				if (ulen >= 0) {
					return UTF8.decode(_unzipped,0,ulen);
				}
				else {
					_client.logError("ZLib uncompress failed (small buffer or bad data?): message dropped");
					return null;
				}
			}
			return UTF8.decode(bytes,0,len);
		}
		else
			return null;
	}
	
	public final void setClient(LiveClient client) {
		_client = client;
	}
	
	protected void increaseReceivedTraffic(int bytes) {
		_receivedBytes += bytes;
	}
	
	protected void increaseSentTraffic(int bytes) {
		_sentBytes += bytes;
	}
	
	public int getTotalReceivedBytes() {
		return _receivedBytes;
	}
	
	public int getTotalSentBytes() {
		return _sentBytes;
	}
}

