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

package org.xcontest.xctrack.widget;

import javax.microedition.lcdui.Graphics;

import org.xcontest.xctrack.info.InfoCenter;
import org.xcontest.xctrack.paint.GeneralFont;
import org.xcontest.xctrack.util.LogRecord;


public class LogWidget extends TextBoxWidget {
	public LogWidget() {		
		super("Log",GeneralFont.SystemFonts,1);
		setAlignment(Graphics.LEFT | Graphics.TOP);
		_records = new LogRecord[20];
	}
	
	protected int getDefaultWidth() {
		return 2000;
	}
		
	protected int getDefaultHeight() {
		return 120;
	}
	
	protected void paint(Graphics g, Object[] objSettings) {
		boolean first = true;
		StringBuffer sb = new StringBuffer();
		int n = InfoCenter.getInstance().getLogInfo().getRecords(_records);
		for (int i = 0; i < n; i ++) {
			LogRecord rec = _records[i];
			
			if (first)
				first = false;
			else
				sb.append('\n');

			
			if (rec.getMessageType() == LogRecord.DEBUG)
				sb.append("[D] ");
			else if (rec.getMessageType() == LogRecord.ERROR)
				sb.append("[E] ");
			
			sb.append(rec.getMessage());
		}
		super.paint(g, objSettings, sb.toString(), 0);
	}
	
	public String getName() {
		return "Log";
	}

	private LogRecord[] _records;
}


