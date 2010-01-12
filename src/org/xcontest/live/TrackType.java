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

package org.xcontest.live;

public class TrackType {
	public static final TrackType DEFAULT = new TrackType("Unspecified","unknown");
	public static final TrackType DEMO = new TrackType("Demo GPS","demo");
	
	private static TrackType[] _all = new TrackType[] {
			new TrackType("Walk","ground/foot/walk"),
			new TrackType("Bicycle","ground/bicycle"),
			new TrackType("Car","ground/car"),
			new TrackType("Paragliding","air/glide/paraglider"),
			new TrackType("Hanggliding","air/glide/hangglider"),
			DEFAULT,
	};
	
	public static TrackType[] getAll() {
		return _all;
	}

	private String _name,_value;
	
	public TrackType(String name, String value) {
		_name = name;
		_value = value;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getValue() {
		return _value;
	}
}
