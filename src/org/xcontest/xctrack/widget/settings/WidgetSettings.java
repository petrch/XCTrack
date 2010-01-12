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

package org.xcontest.xctrack.widget.settings;

import java.util.Vector;

public abstract class WidgetSettings {
	
	// load settings from string
	public abstract Object load(String str);
	
	// save settings to string
	public abstract String save(Object obj);

	// create and append javax.microedition.lcdui.Item(s) of settings form
	// load all control values from obj
	public abstract void createForm(Vector items, Object obj);
	
	// in case of error returns message for the user. returns null if the user input is valid
	public String validateForm() { return null; }

	public abstract void saveForm(Object obj);
}
