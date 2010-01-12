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

import org.xcontest.live.json.JSONException;
import org.xcontest.live.json.JSONObject;

public class ResponseOpenOk extends ResponseOpen {
		
	private ResponseOpenOk() {
	}
	
	public static ResponseOpenOk forAnonymous(JSONObject j) throws JSONException {
		ResponseOpenOk r = new ResponseOpenOk();
		r._key = j.getString("key");
		r._seqCount = 0;
		return r;
	}
	
	public static ResponseOpenOk forRegistered(JSONObject j) throws JSONException {
		ResponseOpenOk r = new ResponseOpenOk();
		r._domain = j.getString("domain");
		r._username = j.getString("username");
		r._key = j.getString("key");
		r._seqCount = 0;
		return r;
	}
	
	public static ResponseOpenOk forContinue(JSONObject j) throws JSONException {
		ResponseOpenOk r = new ResponseOpenOk();
		r._key = null;
		r._domain = j.getString("domain");
		r._username = j.getString("username");
		r._seqCount = j.getInt("seqCount");
		return r;
	}
	
	public boolean isLoggedIn() {
		return _username != null && _domain != null;
	}

	public String getKey() {
		return _key;
	}
		
	public String getUsername() {
		return _username;
	}
	
	public String getDomain() {
		return _domain;
	}
	
	public int getSeqCount() {
		return _seqCount;
	}
	
	String _key,_domain,_username;
	int _seqCount;
}

