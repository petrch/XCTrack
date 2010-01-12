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

import org.xcontest.xctrack.util.Log;
import org.xcontest.xctrack.util.LogListener;
import org.xcontest.xctrack.util.LogRecord;

public class LogInfo implements LogListener {
	private final int MAX_LOG_MESSAGES=20;
	private LogRecord[] _records;
	private int _count;

	LogInfo() {
		_count = 0;
		_records = new LogRecord[MAX_LOG_MESSAGES];
		Log.setListener(this);
	}
	
	// stores latest log messages into buf - returns number of messages stored 
	synchronized public int getRecords(LogRecord[] buf) {
		int n = buf.length;
		if (n > _records.length) n = _records.length;
		if (n > _count) n = _count;
		
		for (int i = 0; i < n; i ++)
			buf[i] = _records[(_count-i-1) % _records.length];
		
		return n;
	}
	
	synchronized public void newLogRecord(LogRecord rec) {
		_records[_count % _records.length] = rec;
		_count ++;
	}
}
