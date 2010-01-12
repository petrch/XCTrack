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

package org.xcontest.xctrack.util;

import org.xcontest.live.UTF8;


public class LogRecord {
	public final static int DEBUG=1;
	public final static int INFO=2;
	public final static int ERROR=3;
	
	public LogRecord(int rsId, byte[] data) {
		_recordStoreId = rsId;
		_data = data;
	}
	
	public LogRecord(int seq, long time, int type, String msg) {
		if (msg.length() > 160)
			msg = msg.substring(0,160);
		byte[] msgBytes = UTF8.encode(msg);
		// should be invalid id
		_recordStoreId = -1;
		
		// seq(4) time(8) type(1)
		_data = new byte[4+8+1+msgBytes.length];
		for (int i = 0; i < 4; i ++) {
			_data[3-i] = (byte)(seq%256);
			seq /= 256;
		}
		for (int i = 0; i < 8; i ++) {
			_data[11-i] = (byte)(time%256);
			time /= 256;
		}
		_data[12] = (byte)type;
		
		for (int i = 0; i < msgBytes.length; i ++)
			_data[13+i] = msgBytes[i];
	}
	
	
	public int getSeq() {
		int n = 0;
		for (int i = 0; i < 4; i ++) {
			n = n*256+(((int)_data[i]+256)%256);
		}
		return n;
	}
	
	public long getTime() {
		long n = 0;
		for (int i = 4; i < 12; i ++) {
			n = n*256+(((long)_data[i]+256)%256);
		}
		return n;
	}
	
	public String getMessage() {
		return UTF8.decode(_data, 13, _data.length-13);
	}
	
	public int getMessageType() {
		return _data[12];
	}
	
	int getRecordStoreId() {
		return _recordStoreId;
	}
	
	void setRecordStoreId(int id) {
		_recordStoreId = id;
	}
	
	byte[] getData() {
		return _data;
	}
	
	private int _recordStoreId;
	private byte[] _data;
}
