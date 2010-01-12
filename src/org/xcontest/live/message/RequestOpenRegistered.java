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


public class RequestOpenRegistered extends RequestOpen {
	/**
	 * 
	 * @param domain can be null
	 * @param username
	 * @param password
	 * @param trackType
	 * @param competitionKey can be null
	 * @param isPublic
	 */
	public RequestOpenRegistered(String domain, String username, String password, String trackType, String competitionKey, boolean isPublic) {
		_domain = domain;
		_username = username;
		_password = password;
		_trackType = trackType;
		_competitionKey = competitionKey;
		_isPublic = isPublic;
	}
	
	public JSONObject toJSON() {
		try {
			JSONObject j = new JSONObject();
			j.put("action","open-registered");
			j.put("requestId", getRequestId());
			j.put("domain",_domain);
			j.put("username",_username);
			j.put("password",_password);
			j.put("trackType",_trackType);
			j.put("competitionKey",_competitionKey);
			j.put("isPublic",_isPublic);
			return j;
		}
		catch (JSONException e) {
			throw new Error("YOU SHOULD NEVER SEE THIS");
		}
	}
	
	String _domain;
	String _username;
	String _password;
	String _trackType;
	String _competitionKey;
	boolean _isPublic;
}