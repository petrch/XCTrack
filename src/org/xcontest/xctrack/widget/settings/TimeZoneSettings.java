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

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;

import org.xcontest.xctrack.util.Format;
import org.xcontest.xctrack.util.Sort;

public final class TimeZoneSettings extends WidgetSettings {
	
	public class Data {
		public TimeZone timezone;
	}

	private ChoiceGroup _choiceTimezone;
	private TimeZone[] _timezones;
	private Hashtable _timezonesOffsets;
	
	public TimeZoneSettings() {
		_timezones = null;
		_timezonesOffsets = null;
	}
	
	public Object load(String str) {
		Data d = new Data();
		if (str == null || str.length() == 0)
			d.timezone = TimeZone.getDefault();
		else
			d.timezone = TimeZone.getTimeZone(str);
		return d;
	}

	public String save(Object obj) {
		Data s = (Data)obj;
		return s.timezone.getID();
	}

	private void createTimezones() {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		long now = System.currentTimeMillis();
		cal.setTime(new Date(now));
		
		String[] ids = TimeZone.getAvailableIDs();
		_timezonesOffsets = new Hashtable();
		_timezones = new TimeZone[ids.length];
		for (int i = 0; i < ids.length; i ++) {
			_timezones[i] = TimeZone.getTimeZone(ids[i]);
			int offset = _timezones[i].getOffset(1, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_WEEK),
					(int)(now%(3600*24*1000)))/60000;
			_timezonesOffsets.put(_timezones[i], new Integer(offset));
		}
		Sort.sortWithIntHashtable(_timezones, _timezonesOffsets);
	}
	
	private String getTimeZoneName(TimeZone tz) {
		int offset = ((Integer)_timezonesOffsets.get(tz)).intValue();
		if (offset >= 0)
			return "(+"+Format.number2(offset/60)+":"+Format.number2(offset%60)+") " + tz.getID();
		else
			return "(-"+Format.number2(-offset/60)+":"+Format.number2((-offset)%60)+") " + tz.getID();
	}
	
	public void createForm(Vector items, Object obj) {
		Data d = (Data)obj;

		if (_timezones == null)
			createTimezones();

		_choiceTimezone = new ChoiceGroup("Time Zone", ChoiceGroup.EXCLUSIVE);
		for (int i = 0; i < _timezones.length; i ++)
			_choiceTimezone.append(getTimeZoneName(_timezones[i]), null);

		String id = d.timezone.getID();
		boolean found = false;
		for (int i = 0; i < _timezones.length; i ++) {
			if (_timezones[i].getID().equals(id)) {
				_choiceTimezone.setSelectedIndex(i, true);
				found = true;
				break;
			}
		}
		
		if (!found) {
			for (int i = 0; i < _timezones.length; i ++) {
				if (d.timezone.getRawOffset() == _timezones[i].getRawOffset() &&
							d.timezone.useDaylightTime() == _timezones[i].useDaylightTime()) {
					_choiceTimezone.setSelectedIndex(i, true);
					break;
				}
			}
		}
		
		items.addElement(_choiceTimezone);
	}

	public void saveForm(Object obj) {
		Data d = (Data)obj;
		d.timezone = _timezones[_choiceTimezone.getSelectedIndex()];
	}
}
