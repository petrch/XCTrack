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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

public class LiveTCPConnection extends LiveConnection {

	private static final byte[] CRLF = new byte[] {'\r','\n'};
	
	private String _host;
	private int _port;

	private Vector _responses;
	byte[] _recv;
	private boolean _isOpen;

	SocketConnection _connection;
	InputStream _inputStream;
	OutputStream _outputStream;
	
	byte[] _recvBuffer;

	public LiveTCPConnection(String host, int port) {
		_host = host;
		_port = port;
		_responses = new Vector();
		_isOpen = false;
		_recvBuffer = new byte[4096];
	}
	
	public synchronized void close() {
		if (_connection != null) {
			try {
				_inputStream.close();
			}
			catch (IOException e) {}
			try {
				_outputStream.close();
			}
			catch (IOException e) {}
			try {
				_connection.close();
			}
			catch (IOException e) {}
			
			_connection = null;
			_inputStream = null;
			_outputStream = null;
		}
		_isOpen = false;
	}

	protected synchronized byte[] getRecvData() {
		return _recv;
	}

	protected synchronized int getRecvLength() {
		return _recv.length;
	}

	public String getRemoteEnd() {
		return "TCP:"+_host+":"+_port;
	}

	public boolean isOpen() {
		return _isOpen;
	}

	public boolean open() {
		_isOpen = true;
		return true;
	}

	protected synchronized boolean recvMessage() throws InterruptedException {
		while (_responses.size() == 0) {
			wait();
		}
		if (_responses.size() > 0) {
			_recv = (byte[])_responses.firstElement();
			_responses.removeElementAt(0);
			return true;
		}
		else {
			return false;
		}
	}
	
	protected synchronized boolean sendMessage(byte[] message, int len) {
		if (_connection == null) {
			return false;
		}
		else {
			try {
				byte[] header = UTF8.encode(""+len);
				_outputStream.write(header);
				_outputStream.write(CRLF);
				_outputStream.write(message,0,len);
				_outputStream.write(CRLF);
				_outputStream.flush();
				increaseSentTraffic(header.length+2+len+2);
				
				// read response length
				int n = 0;
				int recvCount = 0;
				boolean found = false;
				
				for (int i = 0; i < 10; i ++) {		// 10 bytu na dylku proste musi stacit
					int c = _inputStream.read();
					recvCount ++;
					if (c == '\r') {
						c = _inputStream.read();
						recvCount ++;
						if (c == '\n') {
							found = true;
							break;
						}
						close();		// invalid character - CRLF expected
						return false;
					}
					if (c < '0' || c > '9') {		// invalid char or end of stream
						close();
						return false;
					}
					
					n = n*10+c-'0';
				}
				if (!found || n <= 0 || n > MAX_MESSAGE_SIZE)	{		// < 0 just for the case of overflow. n==0 means server closes the connection
					close();
					return false;
				}
				increaseReceivedTraffic(recvCount);
				
				byte[] resp = new byte[n];
				_inputStream.read(resp);
				increaseReceivedTraffic(n);
				
				if (_inputStream.read() != '\r' || _inputStream.read() != '\n') {
					close();
					return false;
				}
				increaseReceivedTraffic(2);
				
				_responses.addElement(resp);
				notifyAll();
				return true;
			}
			catch (IOException e) {
				close();
				return false;
			}
		}
	}

	public void finishedSendingMessages() {
		close();
	}

	public synchronized boolean startSendingMessages() {
		try {
			if (_connection == null) {
				_connection = (SocketConnection)Connector.open("socket://"+_host+":"+_port);
				_inputStream = _connection.openDataInputStream();
				_outputStream = _connection.openDataOutputStream();
			}
		}
		catch (IOException e) {
			_connection = null;
			return false;
		}
		return true;
	}
	
}
