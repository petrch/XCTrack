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

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;

import org.xcontest.xctrack.Util;
import org.xcontest.xctrack.config.Config;
import org.xcontest.xctrack.paint.Menu;
import org.xcontest.xctrack.widget.settings.WidgetSettings;

public abstract class Widget {
	
	protected static final int COLOR_STATIC_TEXT = 0xA0A0A0;
	
	private static WidgetMenuItem[] _all = new WidgetMenuItem[]{
			new WidgetMenuItem(null,"GPS",new WidgetMenuItem[]{
					new WidgetMenuItem(new AltitudeWidget(),"Altitude",null),
					new WidgetMenuItem(new HeadingBarWidget(),"Heading Bar",null),
					new WidgetMenuItem(new HeadingWidget(),"Heading",null),
					new WidgetMenuItem(new LocationWidget(),"Location",null),
					new WidgetMenuItem(new SpeedWidget(),"Speed",null),
					new WidgetMenuItem(new TimeWidget(),"Time",null),
			}),
			new WidgetMenuItem(null,"Flying",new WidgetMenuItem[]{
					new WidgetMenuItem(new FlyCompassWidget(),"Compass",null),
					new WidgetMenuItem(new GlideRatioWidget(),"Glide Ratio",null),
					new WidgetMenuItem(new VerticalSpeedWidget(),"Vario",null),
					new WidgetMenuItem(new VarioBarWidget(),"Vario Bar",null),
					new WidgetMenuItem(new WindWidget(),"Wind",null),
			}),
			new WidgetMenuItem(null,"Tracking",new WidgetMenuItem[]{
					new WidgetMenuItem(new StatusWidget(),"Status",null),
					new WidgetMenuItem(new TrackingTimeWidget(),"Tracking Time",null),
					new WidgetMenuItem(new TrafficWidget(),"Traffic",null),
			}),
			new WidgetMenuItem(null,"DEBUG",new WidgetMenuItem[]{
					new WidgetMenuItem(new LogWidget(),"Log",null),
					new WidgetMenuItem(new MemoryWidget(),"Memory usage",null),
					new WidgetMenuItem(new WindLinesWidget(),"Wind lines",null),
			}),
		};
	
	
	private static Hashtable _byName = null;
	private Vector _settings;

	// basics
	public abstract String getName();
	protected abstract void paint(Graphics g, Object[] objSettings);
	protected abstract int getDefaultWidth();
	protected abstract int getDefaultHeight();

//	protected void addedFirst() {}
//	protected void removedLast() {}

	// settings stuff

	public Widget() {
		_settings = new Vector();
	}
	
	public static Widget getWidgetByName(String name) {
		if (_byName == null) {
			_byName = new Hashtable();
			WidgetMenuItem.fillByNameHashtable(_all, _byName);
		}
		
		if (_byName.containsKey(name)) {
			return (Widget)_byName.get(name);
		}
		else {
			Util.showError("Invalid widget name: "+name);
			return null;
		}
	}
	
	protected final int addSettings(WidgetSettings settings) {
		int idx = _settings.size();
		_settings.addElement(settings);
		return idx;
	}
	
	public final boolean hasSettings() {
		return _settings.size() > 0;
	}
	
	public final Object[] loadSettings(String str) {
		int len = _settings.size();
		if (len == 0)
			return null;
		else if (len == 1)
			return new Object[] { ((WidgetSettings)_settings.elementAt(0)).load(str) };
		else {
			Object[] out = new Object[len];
			if (str == null) {
				for (int i = 0; i < len; i ++)
					out[i] = ((WidgetSettings)_settings.elementAt(i)).load(null);
			}
			else {
				String[] arr = Config.unpackStrings(str);
				
				if (arr == null || arr.length != len)
					return loadSettings(null);
				
				for (int i = 0; i < len; i ++)
					out[i] = ((WidgetSettings)_settings.elementAt(i)).load(arr[i]);
			}
			return out;
		}
	}
	
	public final String saveSettings(Object[] obj) {
		int len = _settings.size();
		if (len == 0)
			return null;
		else if (len == 1)
			return ((WidgetSettings)_settings.elementAt(0)).save(obj[0]);
		else {
			String[] arr = new String[obj.length];
			for (int i = 0; i < len; i ++)
				arr[i] = ((WidgetSettings)_settings.elementAt(i)).save(obj[i]);
			return Config.packStrings(arr);
		}
	}
	
	public final void createSettingsForm(Vector items, Object[] settings) {
		int len = _settings.size();
		for (int i = 0; i < len; i ++)
			((WidgetSettings)_settings.elementAt(i)).createForm(items,settings[i]);
	}
	
	public final String validateSettingsForm() {
		int len = _settings.size();
		for (int i = 0; i < len; i ++) {
			String err = ((WidgetSettings)_settings.elementAt(i)).validateForm();
			if (err != null)
				return err;
		}
		return null;
	}
	
	public final void saveSettingsForm(Object[] obj) {
		int len = _settings.size();
		for (int i = 0; i < len; i ++)
			((WidgetSettings)_settings.elementAt(i)).saveForm(obj[i]);
	}
	
	public static Menu createMenu(String captionPrevious, String caption, int action) {
		return WidgetMenuItem.createMenu(captionPrevious, caption, action, _all);
	}
	
}

