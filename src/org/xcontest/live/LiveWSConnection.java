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

import org.xcontest.xctrack.util.Log;

public class LiveWSConnection extends LiveConnection {

	private String _path;
	private String _host;
	private int _port;

	private Vector _responses;
	byte[] _recv;
	private boolean _isOpen;

	SocketConnection _connection;
	InputStream _inputStream;
	OutputStream _outputStream;
	
	byte[] _recvBuffer;

	public LiveWSConnection(String url) {
		if (!url.startsWith("ws://")) {
			throw new ArgumentError("LiveWSConnection: expected ws:// url scheme");
		}
		url = url.substring(4);
		int idx = url.indexOf('/');
		if (idx >= 0) {
			_path = url.substring(idx);
			url = url.substring(0,idx);
		}
		else {
			_path = "/";
		}
		
		idx = url.indexOf(':');
		if (idx >= 0) {
			_host = url.substring(0,idx);
			_port = Integer.parseInt(_host.substring(idx+1));
		}
		else {
			_host = url;
			_port = 80;
		}
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
		return "ws://"+_host+":"+_port+_path;
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
				_outputStream.write(0);
				_outputStream.write(message,0,len);
				_outputStream.write(255);
				_outputStream.flush();
				increaseSentTraffic(len+2);
				
				if (_inputStream.read() != 0) {
					_client.logError("TCP: Invalid data received - expected byte 0 (start of WebSockets message)");
					close();
					return false;
				}
				int pos = 0;
				// java nema unsigned byte chjo :( ... (byte)0xff == -1
				while (pos == 0 || (pos < _recvBuffer.length && _recvBuffer[pos-1] != -1)) {
					int n = _inputStream.available();
					if (n > 0)
						pos += _inputStream.read(_recvBuffer,pos,_recvBuffer.length-pos);
					else {
						int c = _inputStream.read();
						if (c < 0) {
							close();
							_client.logError("TCP: EOF while receiving WS response!");
							return false;
						}
						_recvBuffer[pos++] = (byte)c;
					}
				}
				increaseReceivedTraffic(1+pos);
				if (pos == _recvBuffer.length) {
					close();
					_client.logError("TCP: Buffer not large enough to store WS response!");
					return false;
				}
				
				byte[] resp = new byte[pos-1];
				System.arraycopy(_recvBuffer, 0, resp, 0, pos-1);
				
				_responses.addElement(resp);
				notifyAll();
				return true;
			}
			catch (IOException e) {
				_client.logError("TCP: Error receiving data",e);
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
				
		        byte[] header = UTF8.encode("GET "+_path+" HTTP/1.1\r\nUpgrade: WebSocket\r\nConnection: Upgrade\r\nHost: "+_host+"\r\nOrigin: XCTrack\r\nWebSocket-Protocol: sample\r\n\r\n");
				_outputStream.write(header);

				increaseSentTraffic(header.length);
				
				int idx = 0;
				int cnt = 0;
				// skip http headers
				while(idx != 4) {
					int c = _inputStream.read();
					cnt ++;
					if (c == -1) {
						Log.error("TCP: EOF received while WebSockets handshake: cnt="+cnt);
						_connection.close();
						_connection = null;
						increaseReceivedTraffic(cnt);
						return false;
					}
					if (c == '\r' && idx%2 == 0)
						idx ++;
					else if (c == '\n' && idx%2 == 1)
						idx ++;
					else
						idx = 0;
				}
				increaseReceivedTraffic(cnt);
			}
		}
		catch (IOException e) {
			_client.logError("TCP: Error opening WebSocket connection", e);
			_connection = null;
			return false;
		}
		return true;
	}
	
}
