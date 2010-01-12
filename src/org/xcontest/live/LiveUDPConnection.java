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

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.UDPDatagramConnection;

public class LiveUDPConnection extends LiveConnection {
	
	// [ui]
	public LiveUDPConnection(String host, int port) {
		_host = host;
		_port = port;
		
		_recvBuf = new byte[1000];
	}
	
	// [receiver,sender]
	public boolean isOpen() {
		synchronized (this) {
			return _conn != null;
		}
	}
	
	// [receiver,sender]
	boolean open() {
		try {
			UDPDatagramConnection conn = (UDPDatagramConnection)Connector.open("datagram://"+_host+":"+_port);
			synchronized(this) {
				_conn = conn;
			}
			return true;
		}
		catch(IOException e) {
			_client.logError("Error opening UDP connection",e);
			return false;
		}
	}
	
	// [ui], [receiver]
	void close() {
		synchronized(this) {
			if (_conn != null) {
				try {
					_conn.close();
				}
				catch(IOException e) {
					_client.logError("Error closing UDP connection",e);
				}
				_conn = null;
			}
		}
	}
		
	// [receiver]
	public String getRemoteEnd() {
		return "UDP:"+_host+":"+_port;
	}
	
	// [sender]
	boolean startSendingMessages() {
		return true;
	}
	
	// [sender]
	public void finishedSendingMessages() {
	}
	
	// [sender]
	public boolean sendMessage(byte[] bytes, int len) {
		UDPDatagramConnection conn;
		synchronized(this) {
			conn = _conn;
		}
		if (conn == null)
			return false;
		
		try {
			Datagram dgm = conn.newDatagram(bytes,len);
			conn.send(dgm);
			increaseSentTraffic(20+len);
		}
		catch(IOException e) {
			_client.logError("Error creating UDP datagram",e);
			return false;
		}
		return true;
	}
	
	
	// [receiver]
	public boolean recvMessage() throws InterruptedIOException {
		UDPDatagramConnection conn;
		
		synchronized (this) {
			conn = _conn;
		}
		if (conn == null)
			return false;
		
		try {
			if (_recvdgram == null) {
				_recvdgram = conn.newDatagram(_recvBuf,_recvBuf.length);
			}
			_recvdgram.reset();
			_recvdgram.setLength(_recvBuf.length);
			conn.receive(_recvdgram);
			increaseReceivedTraffic(20+_recvdgram.getLength());
			return true;
		}
		catch (InterruptedIOException e) {
			throw e;
		}
		catch (IOException e) {
			_client.logError("Error receiving UDP datagram",e);
			return false;
		}
		catch (IllegalArgumentException e) {
			_client.logError("Error receiving UDP datagram",e);
			return false;
		}
	}

	// [receiver]
	protected byte[] getRecvData() {
		return _recvdgram.getData();
	}
	
	// [receiver]
	protected int getRecvLength() {
		return _recvdgram.getLength();
	}
	
	UDPDatagramConnection _conn;
	Datagram _recvdgram;
	byte[] _recvBuf;
	
	String _host;
	int _port;
}


