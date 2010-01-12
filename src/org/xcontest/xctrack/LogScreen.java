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

package org.xcontest.xctrack;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;

import org.xcontest.xctrack.util.Log;
import org.xcontest.xctrack.util.LogRecord;

class LogDetail extends Form implements CommandListener {
	
	public LogDetail(LogRecord rec) {
		super(formatTime(rec.getTime()));
		append("SEQ: "+rec.getSeq()+"\n");
		append("TYPE: " + (rec.getMessageType() == LogRecord.ERROR ? "ERROR" : rec.getMessageType() == LogRecord.INFO ? "INFO" : "DEBUG")+"\n");
		append(rec.getMessage());
		addCommand(new Command("Back",Command.BACK,1));
		setCommandListener(this);
	}
	
	private static String format2(int n) {
		if (n < 10)
			return "0"+n;
		else
			return ""+n;
	}

	private static String formatTime(long t) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTime(new Date(t));
		return ""+cal.get(Calendar.YEAR)+"-"+format2(1+cal.get(Calendar.MONTH))+"-"+
					cal.get(Calendar.DAY_OF_MONTH)+" "+format2(cal.get(Calendar.HOUR_OF_DAY))+":"+
					format2(cal.get(Calendar.MINUTE))+":"+format2(cal.get(Calendar.SECOND));
	}

	public void show() {
		App.showScreen(this);
	}

	public void commandAction(Command cmd, Displayable disp) {
		App.hideScreen(this);
	}
}



public class LogScreen implements CommandListener {

	private List _list;
	private Command _cmdClear,_cmdBack;
	private LogRecord[] _records;
	
	public LogScreen() {
		_list = new List("Log entries",List.IMPLICIT);
		_list.addCommand(_cmdBack = new Command("Back", Command.SCREEN, 1));
		_list.addCommand(_cmdClear = new Command("Clear Log", Command.ITEM, 2));
		_list.setCommandListener(this);
		readLog();
	}
	
	private void readLog() {
		int max = 100;
		_records = Log.readAll();
		
		_list.deleteAll();
		for (int i = _records.length > max ? _records.length-max : 0; i < _records.length; i ++) {
			String msg = _records[i].getMessage();
			if (msg.length() > 30)
				msg = msg.substring(0,30)+"...";
			_list.append("["+_records[i].getSeq()+"] "+msg, null);
		}
	}
	
	public void show() {
		App.showScreen(_list);
	}
	
	public void commandAction(Command cmd, Displayable disp) {
		if (cmd == _cmdClear) {
			Log.clear();
			readLog();
		}
		else if (cmd == _cmdBack) {
			App.hideScreen(_list);
		}
		else if (cmd.getCommandType() == Command.SCREEN){
			new LogDetail(_records[_list.getSelectedIndex()]).show();
		}
	}
}
