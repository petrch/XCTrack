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

package org.xcontest.live.message;

import java.util.Hashtable;

import org.xcontest.live.json.JSONArray;
import org.xcontest.live.json.JSONException;
import org.xcontest.live.json.JSONObject;

public class ResponseTrackPart extends ServerMessage {
	public ResponseTrackPart(JSONObject j) throws JSONException {
		_seqCount = j.getInt("seqCount");
		JSONArray jmissing = j.getJSONArray("missing");
		int len = jmissing.length();
		
		if (len > 0) {
			_isMissing = new Hashtable();
			for (int i = 0; i < len; i ++) {
				_isMissing.put(new Integer(jmissing.getInt(i)), this);
			}
		}
		else {
			_isMissing = null;
		}
	}
		
	public boolean isConfirmed(int seq) {
		return seq < _seqCount && (_isMissing == null || !_isMissing.containsKey(new Integer(seq)));
	}

	
	int _seqCount;
	Hashtable _isMissing;
}
