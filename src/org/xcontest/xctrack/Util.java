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

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;

import org.xcontest.xctrack.config.Config;
import org.xcontest.xctrack.util.Log;

public class Util {
	
	public static void showError(String message, Throwable e) {
		String str,title;
		
		if (Config.isDebugMode()) {
			e.printStackTrace();
			title = "EXCEPTION";
			str = message + ": " + e.getMessage() + "\n" + "EXCEPTION " + e.toString();
		}
		else {
			title = "APPLICATION ERROR";
			str = message + ": " + e.getMessage();
		}
		
		showAlert(AlertType.ERROR,title,str);
	}
	
	public static void showError(String message) {
		showAlert(AlertType.ERROR,"XCTrack Error",message);
	}
	
	public static void showInfo(String message) {
		showAlert(AlertType.INFO,"XCTrack Info",message);
	}
	
	public static boolean expect(boolean trueExpression, String koMessage) {
		if (!trueExpression) {
			Log.error("Assertion failed: "+koMessage);
			showAlert(AlertType.ERROR, "Assertion failed", koMessage);
		}
		return trueExpression;
	}
	
	private static void showAlert(AlertType type, String title, String message) {
		// TODO - udelat form, udelat moznost "modal" displeju do App
		Display d = App.getDisplay();
		Alert a = new Alert(title,message,null,type);
		if (d.getCurrent() == null)
			d.setCurrent(a);
		else
			d.setCurrent(a,d.getCurrent());
	}
	
	/**
	 * 
	 * @param intensity 0-100
	 * @return
	 */
	public static boolean setBacklight(int intensity) {
		try {
			Class.forName("com.nokia.mid.ui.DeviceControl");
			com.nokia.mid.ui.DeviceControl.setLights(0, intensity);
			return true;
		}
		catch(ClassNotFoundException e){
		}
		return false;
	}

	public static boolean hasBacklightSetting() {
		try {
			Class.forName("com.nokia.mid.ui.DeviceControl");
			return true;
		}
		catch(ClassNotFoundException e){
		}
		return false;
	}
}
