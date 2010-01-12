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

import javax.microedition.lcdui.Gauge;

public final class PrecisionSettings extends WidgetSettings {
	public class Data {
		public int precision;
	}
	
	private Gauge _gauge;
	private int _max;
	private int _defaultValue;
	private String _title;
	
	public PrecisionSettings(String title, int max, int defaultValue) {
		_max = max;
		_defaultValue = defaultValue;
		_title = title;
	}
	
	public PrecisionSettings(int max, int defaultValue) {
		this("Decimal precision",max,defaultValue);
	}
	
	public Object load(String str) {
		Data d = new Data();
		if (str == null) {
			d.precision = _defaultValue;
		}
		else {
			int i = Integer.parseInt(str);
			if (i < 0) i = 0;
			if (i > _max) i = _max;
			d.precision = i;
		}
		return d;
	}

	public String save(Object obj) {
		Data d = (Data)obj;
		return "" + d.precision;
	}

	public void createForm(Vector items, Object obj) {
		Data d = (Data)obj;
		_gauge = new Gauge(_title,true,_max,0);
		_gauge.setValue(d.precision);
		items.addElement(_gauge);
	}

	public void saveForm(Object obj) {
		Data d = (Data)obj;
		d.precision = _gauge.getValue();
	}
}


