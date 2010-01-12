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

public class LoginDomain {
	private LoginDomain(String visibleName, String protocolName) {
		_visibleName = visibleName;
		_protocolName = protocolName;
	}
	
	public static LoginDomain[] getAll() {
		if (_all == null) {
			_all = new LoginDomain[]{
//					new LoginDomain("Live-Tracking.org","LIVETRACKING"),
					new LoginDomain("XContest","XCONTEST"),
			};
		}
		return _all;
	}
	
	public String getName() {
		return _visibleName;
	}
	
	public String getValue() {
		return _protocolName;
	}

	private static LoginDomain[] _all;
	private String _visibleName;
	private String _protocolName;
}
