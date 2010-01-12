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

package org.xcontest.xctrack.fs;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.xcontest.xctrack.App;
import org.xcontest.xctrack.Util;
import org.xcontest.xctrack.util.Log;

public class OpenFileDialog implements CommandListener {
	List _list;
	String _path;
	Vector _entries;
	Command _cmdOk,_cmdCancel,_cmdUp;
	
	public void rebuildEntries() {
		FileConnection fconn = null;
		_list.deleteAll();
		_entries.removeAllElements();

		Enumeration entries;
		try {
			if (_path.equals(""))
				entries = FileSystemRegistry.listRoots();
			else {
				fconn = (FileConnection)Connector.open("file://"+_path+"/");
				entries = fconn.list();
			}
			while(entries.hasMoreElements()) {
				String fn = (String)entries.nextElement();
				int idx = fn.indexOf('/');
				if (idx >= 0) fn = fn.substring(0,idx);
				_list.append(fn, null);
				_entries.addElement(fn);
			}
		}
		catch (IOException e) {
			Log.error("Cannot read directory: "+_path);
			Util.showError("Cannot read directory: "+_path);
		}
		catch (Throwable t) {
			Util.showError("BUBU",t);
		}
		finally {
			if (fconn != null) {
				try {
					fconn.close();
				}
				catch (IOException e) {}
			}
		}
		
	}
	
	public OpenFileDialog() {
		_cmdOk = new Command("Ok",Command.OK,1);
		_cmdUp = new Command("One level UP",Command.ITEM,1);
		_cmdCancel = new Command("Cancel",Command.ITEM,1);
		
		_list = new List("/",List.IMPLICIT);
		_list.addCommand(_cmdOk);
		_list.addCommand(_cmdUp);
		_list.addCommand(_cmdCancel);
		_list.setCommandListener(this);
		_path = "";
		_entries = new Vector();
		
		rebuildEntries();
	}
	
	public void show() {
		App.showScreen(_list);
	}
	
	public void hide() {
		App.hideScreen(_list);
	}

	public void commandAction(Command cmd, Displayable disp) {
		int cmdType = cmd.getCommandType();
		if (cmdType == Command.SCREEN || cmd == _cmdOk) {
			String fn = (String)_entries.elementAt(_list.getSelectedIndex());
			_path += "/"+fn;
			rebuildEntries();
		}
		else if (cmd == _cmdUp) {
			int idx = _path.lastIndexOf('/');
			if (idx >= 0) {
				_path = _path.substring(0,idx);
				rebuildEntries();
			}
		}
		else if (cmd == _cmdCancel) {
			hide();
		}
	}
}
