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

import java.util.Calendar;
import java.util.Vector;

import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

class LogRecordComparator implements RecordComparator {

	public int compare(byte[] a, byte[] b) {
		// the first 4 bytes are (int)sequence
		if (a.length < 4 || b.length < 4) {
			return EQUIVALENT;
		}
		for (int i = 0; i < 4; i ++) {
			int ai = ((int)a[i]+256)%256;
			int bi = ((int)b[i]+256)%256;
			if (ai < bi) return PRECEDES;
			if (ai > bi) return FOLLOWS;
		}
		return EQUIVALENT;
	}
}


public final class Log {
	private static LogListener _listener = null;
	private static Vector _rsRecords = null;
	private static int _rsNextSeq = 0;
	private static final int LOG_SIZE = 50; // number of messages
	
	public static void info(String msg) {
		append(LogRecord.INFO,msg);
	}
	
	public static void debug(String msg) {
		append(LogRecord.DEBUG,msg);
	}
	
	public static void error(String msg) {
		append(LogRecord.ERROR,msg);
	}
	
	public static void error(String msg, Throwable e) {
		append(LogRecord.ERROR,msg + "[EX:"+e.getClass().getName()+":"+e.getMessage()+"]");
	}
	
	public static synchronized void setListener(LogListener l) {
		_listener = l;
	}
	
	public synchronized static LogRecord[] readAll() {
		
		if (_rsRecords == null) {
			try {
				RecordStore rs = openRS();
				rs.closeRecordStore();
			}
			catch (RecordStoreException e) {
//				Util.showError("Cannot read log",e);
			}
		}
		LogRecord[] out = new LogRecord[_rsRecords.size()];

		for (int i = 0; i < out.length; i ++) {
			out[i] = (LogRecord)_rsRecords.elementAt(i);
		}
		return out;
	}
	
	public synchronized static void clear() {
		try {
			RecordStore.deleteRecordStore("log");
		}
		catch (Exception e) {
		}
		_rsRecords = null;
	}
	
	private synchronized static RecordStore openRS() throws RecordStoreException {
		RecordStore rs = RecordStore.openRecordStore("log", true);

		// read _rsSeqToRecord hash and initialize the _rsNextSeq
		if (_rsRecords == null) {
			RecordEnumeration re;
			_rsNextSeq = 1;
			try {
				re = rs.enumerateRecords(null, new LogRecordComparator(), false);
			}
			catch (Exception e) {
				rs.closeRecordStore();
				RecordStore.deleteRecordStore("log");
				rs = RecordStore.openRecordStore("log", true);
				re = rs.enumerateRecords(null, new LogRecordComparator(), false);
			}
			_rsRecords = new Vector();
			while (re.hasNextElement()) {
				int id = re.nextRecordId();
				LogRecord rec = new LogRecord(id,rs.getRecord(id));
				int seq = rec.getSeq();
				if (_rsNextSeq <= seq)
					_rsNextSeq = seq + 1;
				_rsRecords.addElement(rec);
			}
		}
		
		return rs;
	}
		
	private synchronized static void append(int type, String msg) {
		LogRecord rec = new LogRecord(_rsNextSeq,Calendar.getInstance().getTime().getTime(),type,msg);
		
		if (_listener != null)
			_listener.newLogRecord(rec);
		
		RecordStore rs = null;
		try {
			rs = openRS();
			
			// delete the oldest record if LOG_SIZE is reached
			while (_rsRecords.size() > LOG_SIZE) {
				rs.deleteRecord(((LogRecord)_rsRecords.elementAt(0)).getRecordStoreId());
				_rsRecords.removeElementAt(0);
			}
			
			// push new record to the record store and cache
			int id = rs.addRecord(rec.getData(),0,rec.getData().length);
			rec.setRecordStoreId(id);
			_rsRecords.addElement(rec);
			_rsNextSeq ++;
			
		}
		catch (RecordStoreException e) {
		}
		finally {
			try {
				if (rs != null)
					rs.closeRecordStore();
			}
			catch(RecordStoreException e) {
			}
		}
	}
	
}
