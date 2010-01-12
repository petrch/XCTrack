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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.UDPDatagramConnection;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.xcontest.xctrack.config.Config;
import org.xcontest.xctrack.info.InfoCenter;
import org.xcontest.xctrack.util.Log;

class ScreenInfo {
	ScreenInfo(Displayable d, ScreenListener l) {
		disp = d;
		listener = l;
	}
	
	Displayable disp;
	ScreenListener listener;
}

public class App extends MIDlet {
	private static App _instance;
	
	public static String getMidletVersion() {
		String ver = _instance.getAppProperty("MIDlet-Version");
		return ver == null ? "" : ver;
	}
	
	public App() {
		_instance = this;

		_display = Display.getDisplay(this);
		_displayables = new Vector();

Log.clear();
		Log.info("------- started");

		Config.init();

		_infoCenter = new InfoCenter();
		_infoCenter.start();
		(new MainScreen()).show();
		
//		if (Config.isFirstRun())
//			Util.showInfo("Hello for the first time!");
	}
	
	public static Display getDisplay() {
		return _instance._display;
	}
	
	public void testUDP() {
		try {
			UDPDatagramConnection conn = (UDPDatagramConnection)Connector.open("datagram://"+Config.getUDPServerHost()+":"+Config.getUDPServerPort());
			byte[] bytes = new byte[]{97,98,99};
			Datagram dgm = conn.newDatagram(bytes,bytes.length);
//			dgm.reset();
//			dgm.setAddress("datagram://"+Config.getUDPServerHost()+":"+Config.getUDPServerPort());
//			dgm.setData(bytes, 0, bytes.length);
			conn.send(dgm);
			conn.close();
			Util.showInfo(dgm.getAddress());
		}
		catch (IOException e) {
			Util.showInfo(e.getMessage());
		}
	}

	public void testTCP() {
		try {
			SocketConnection conn = (SocketConnection)Connector.open("socket://"+Config.getUDPServerHost()+":"+Config.getUDPServerPort());
			InputStream is = conn.openInputStream();
			OutputStream os = conn.openOutputStream();

			os.write(new byte[]{'4','\r','\n','a','h','o','j','\r','\n'});
			
			os.close();
			is.close();
			conn.close();
		}
		catch (IOException e) {
			Util.showInfo(e.getMessage());
		}
	}
	
	public void testHTTP() {
		try {
			StreamConnection conn = (StreamConnection)Connector.open("http://live.xcontest.org");
			InputStream is = conn.openInputStream();
			int cnt = 0;
			while (is.read() != -1) cnt ++;
			Util.showInfo(""+cnt);
		}
		catch (IOException e) {
			Util.showInfo(e.getMessage());
		}
	}
	
	public static Displayable getCurrentDisplayable() {
		synchronized(_instance._displayables) {
			int n = _instance._displayables.size();
			Displayable d = n >= 1 ? ((ScreenInfo)_instance._displayables.elementAt(n-1)).disp : null;
			return d;
		}
	}
	
	public static void showScreen(Displayable disp, ScreenListener listener) {
		showScreen(disp,listener,false);
	}
	
	public static void showScreen(Displayable disp) {
		showScreen(disp,null,false);
	}
	
	public static void showScreen(Displayable disp, ScreenListener listener, boolean isModal) {	
		synchronized(_instance._displayables) {
			_instance._displayables.addElement(new ScreenInfo(disp,listener));
			_instance._display.setCurrent(disp);
		}
		
		if (listener != null)
			listener.screenShown(disp,true);
	}
	
	public static void hideScreen(Displayable disp) {
		ScreenInfo info = null;
		synchronized(_instance._displayables) {
			if (Util.expect(!_instance._displayables.isEmpty(),"App.hideScreen(): no screen to hide!")) {
				int size = _instance._displayables.size();
				while (size > 0 && ((ScreenInfo)_instance._displayables.elementAt(size-1)).disp != disp)
					_instance._displayables.removeElementAt(--size);
				if (Util.expect(size > 0,"App.hideScreen(): specified screen not found!")) {
					_instance._displayables.removeElementAt(--size);
					if (size > 0) {
						info = (ScreenInfo)_instance._displayables.elementAt(size-1);
						_instance._display.setCurrent(info.disp);
					}
				}
			}
		}

		if (info.listener != null)
			info.listener.screenShown(info.disp,false);
	}
	
	public static void exit() {
		_instance.cleanup();
		_instance.notifyDestroyed();
	}

	public boolean isPaused() {
		return _isPaused;
	}

	public void startApp() {
		_isPaused = false;
		Log.info("startApp()");
	}

	public void pauseApp() {
		_isPaused = true;
		
		Log.info("pauseApp()");
	}
	
	public void destroyApp(boolean unconditional) throws MIDletStateChangeException {
		if (!unconditional) {
			throw new MIDletStateChangeException();
		}
		else {
			cleanup();
		}
	}
	
	private void cleanup() {
		_infoCenter.exit();
	}

	
	// Live client listener implementation
	public void connectionOpened(String remote) {}
	public void loginFailed(String domain, String username) {}
	public void openConnectionFailed(String remote) {}
	public void statusUpdate() {}

	private boolean _isPaused;
	private Vector _displayables;
	private Display _display;
	
	private static InfoCenter _infoCenter;

}




