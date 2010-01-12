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

import java.util.Vector;
import org.xcontest.live.GpsPoint;
import org.xcontest.live.json.*;

public class RequestTrackPart extends Request {
	private static String format2(int n) {
		if (n < 10)
			return "0"+n;
		else
			return ""+n;
	}
	
	private static String format6(int n) {
		if (n < 10)
			return "00000"+n;
		else if (n < 100)
			return "0000"+n;
		else if (n < 1000)
			return "000"+n;
		else if (n < 10000)
			return "00"+n;
		else if (n < 100000)
			return "0"+n;
		else
			return ""+n;
	}
	
	
	public RequestTrackPart(int seq, Vector points) {
		_seq = seq;
		_points = new GpsPoint[points.size()];
		points.copyInto(_points);
	}
	
	public int getPointCount() {
		return _points.length;
	}
	
	public void setKey(String key) {
		_key = key;
	}
	
	public int getSeq() {
		return _seq;
	}
	
	public JSONObject toJSON() {
		try {
			JSONArray jpoints = new JSONArray();
		
			for (int i = 0; i < _points.length; i ++) {
				GpsPoint p = _points[i];
				JSONObject jp = new JSONObject();
				jp.put("t",""+p.year+"-"+format2(p.month)+"-"+format2(p.day)+
								"T"+format2(p.hour)+":"+format2(p.min)+":"+format2(p.sec)+"."+format6(p.usec));
				jp.put("x",p.lon);
				jp.put("y",p.lat);
				jp.put("z",p.alt);
				jpoints.put(jp);
			}
		
			JSONObject j = new JSONObject();
			j.put("action","track-part");
			j.put("trackKey",_key);
			j.put("requestId",getRequestId());
			j.put("seq",_seq);
			j.put("points",jpoints);
			return j;
		}
		catch (JSONException e) {
			throw new Error("YOU SHOULD NEVER SEE THIS");
		}
	}
	
	int _seq;
	GpsPoint[] _points;
	String _key;
}


