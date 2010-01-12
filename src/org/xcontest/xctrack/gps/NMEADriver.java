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
import java.io.InterruptedIOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.xcontest.xctrack.gps.GpsMessage;
import org.xcontest.xctrack.util.Log;


class LineReader {
	DataInputStream _is;
	private byte[] _buffer;
	int _newLineIndex;
	int _bufpos;
	
	LineReader(DataInputStream is, int bufLength) {
		_is = is;
		_buffer = new byte[bufLength];
		_newLineIndex = -1;
	}
	
	byte[] getBuffer() {
		return _buffer;
	}
	
	int getNewLineIndex() {
		return _newLineIndex;
	}
	
	/**
	 * @param buf
	 * @return false on EOF
	 */
    boolean readNextLine() throws IOException {
    	DataInputStream is = _is;
    	byte[] buffer = _buffer;
    	int len = buffer.length;
    	
    	// drop old line from buffer
    	if (_newLineIndex >= 0) {
    		int idx = _newLineIndex;
    		// drop also the trailing '\r', '\n'
	    	while (idx < _bufpos && (buffer[idx] == 10 || buffer[idx] == 13))
	    		idx ++;
	    	if (idx < _bufpos)
	    		System.arraycopy(buffer, idx, buffer, 0, _bufpos-idx);
	    	_bufpos -= idx;
    	}
    	
    	// check whether we already have new line in buffer
    	int bufpos = _bufpos;	// cache bufpos
    	for (int i = 0; i < bufpos; i ++) {
    		if (buffer[i] == 10 || buffer[i] == 13) {
    			_newLineIndex = i;
    			return true;
    		}
    	}

    	while (bufpos < len) {
	    	int navail = is.available();
			if (navail > 0) {
				if (navail > len-bufpos)
					navail = len-bufpos;
				is.read(buffer,bufpos,navail);
				
				for (int i = 0; i < navail; i ++) {
					if (buffer[bufpos+i] == 10 || buffer[bufpos+i] == 13) { // we have another line?
						_newLineIndex = bufpos+i;
						_bufpos = bufpos+navail;
						return true;
					}
				}
				bufpos += navail;
			}
			else {
				int c = is.read();	//block
	
				if (c == -1) {	// EOF
					if (bufpos > 0) {
						_bufpos = bufpos;
						_newLineIndex = bufpos;
						return true;
					}
					else {
						return false;
					}
				}
	
				if (c == 10 || c == 13) {
					if (bufpos > 0) {
						buffer[bufpos++] = (byte)c;
						_newLineIndex = bufpos;
						_bufpos = bufpos;
						return true;
					}
				}
				else {
					buffer[bufpos++] = (byte)c;
				}
			}
    	}
    	
    	// buffer full, no CR/LF found
    	_bufpos = bufpos;	// == _buffer.length
    	_newLineIndex = bufpos;
    	return true;
    }
}



public abstract class NMEADriver extends GpsDriver implements Runnable {

	private static final int RECONNECT_TIMEOUT=5000;	//ms
    
	private String _deviceAddress;
	private StreamConnection _connection;
	private DataInputStream _inputStream;
	private Thread _thread;

    public NMEADriver() {
		_deviceAddress = null;
		_connection = null;
		_inputStream = null;
		_thread = null;
	}
    
    protected void setDeviceAddress(String addr) {
    	_deviceAddress = addr;
    }
	    
    /** Start reader thread */
    public synchronized void connect(String address) {
		// close previous connection
		cleanup();
		
		_deviceAddress = address;
		
		if (_thread == null) {
			_thread = new Thread(this);
			_thread.start();
		}
    }
	
    public synchronized void disconnect() {
		if (_thread != null) {
			_thread.interrupt();
			_thread = null;
		}
    }
    
	
    public void run() {
//int cnt = 0;
		NMEAParser parser = new NMEAParser();
		StreamConnection conn;
		DataInputStream inputStream;
		LineReader reader = null;
		GpsMessage msg = new GpsMessage();
		
		Log.info("NMEA thread started");
		
		try {
	        while (_thread != null) {
	        	synchronized(this) {
	        		inputStream = _inputStream;
	        		conn = _connection;
	        	}
				// try to (re)connect
				if (inputStream == null) {
					try {
						conn = (StreamConnection)Connector.open(_deviceAddress);
						Log.info("NMEA Connected OK");
						try {
							conn.openOutputStream().write(new byte[]{36,83,84,65,13,10});	// "$STA\r\n" : start transmitting
							Log.info("NMEA Sent $STA message");
						}
						catch(InterruptedIOException e) {
							throw e;
						}
						catch(IOException e) {}
						inputStream = conn.openDataInputStream();
						reader = new LineReader(inputStream,1000);
						
						synchronized(this) {
							_connection = conn;
							_inputStream = inputStream;
							deviceConnected();
						}
					}
					catch(InterruptedIOException e) {
						throw e;
					}
					catch(IOException e) {
						cleanup();
						synchronized(this) {
							wait(RECONNECT_TIMEOUT);
						}
					}
					catch(SecurityException e) {
						cleanup();
						synchronized(this) {
							wait(RECONNECT_TIMEOUT);
						}
					}
				}
				else  { // reader != null
		            try {
		            	if (reader.readNextLine()) {
							if (parser.parse(reader.getBuffer(),reader.getNewLineIndex(),msg)) {
								checkGpsPositionAge(msg);
				                notifyListeners(msg);
							}
							else {
								checkGpsPositionAge(null);
							}
		            	}
		            	else {		// EOF
							Log.info("NMEA read EOF - device disconnected");
							deviceDisconnected();
							cleanup();
							reader = null;
		            	}
					}
		            catch (InterruptedIOException e) {
		            	throw e;
		            }
					catch (IOException e) {
						Log.error("NMEA error reading from device",e);
						deviceDisconnected();
						cleanup();
						reader = null;
					}
				}

	        }	// while(true)
		}
		catch(InterruptedException e){}
		catch(InterruptedIOException e){}
		Log.info("NMEA Driver EXIT");
		cleanup();
    }
	
	protected synchronized void cleanup() {
		if (_inputStream != null) {
			try {
				_inputStream.close();
			}
			catch (IOException e) {
			}
			_inputStream = null;
		}
		if (_connection != null) {
			try {
				_connection.close();
			}
			catch (IOException e) {
			}
			_connection = null;
		}
	}
}

