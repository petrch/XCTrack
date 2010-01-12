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

import org.xcontest.live.LiveClient;
import org.xcontest.live.json.JSONException;
import org.xcontest.live.json.JSONObject;

public class ServerMessage {
	
	
	
	public static ServerMessage parse(LiveClient client, String json) {
		try {
			JSONObject j = new JSONObject(json);
			String responseFor = j.getString("responseFor");
			String status = j.getString("status");
			if (status.equals("ok")) {
				if (responseFor.equals("open-registered"))
					return ResponseOpenOk.forRegistered(j);
				else if (responseFor.equals("open-anonymous"))
					return ResponseOpenOk.forAnonymous(j);
				else if (responseFor.equals("open-continue"))
					return ResponseOpenOk.forContinue(j);
				else if (responseFor.equals("track-part")) {
//String s = "{\"status\":\"ok\",\"requestId\":35,\"responseFor\":\"track-part\",\"seqCount\":12,\"missing\":[0,1,2,3]}";
//j = new JSONObject(s);
					return new ResponseTrackPart(j);
				}
				else {
					client.logError("Received invalid message: status="+status+" responseFor="+responseFor);
					return null;
				}
			}
			else if (status.equals("bad-request")) {
				client.logError("Received bad-request message from server");
				return null;
			}
			else if (status.equals("login-failed")) {
				if (responseFor.equals("open-registered"))
					return new ResponseOpenLoginFailed(j);
				else {
					client.logError("Received invalid message: status="+status+" responseFor="+responseFor);
					return null;
				}
			}
			else {
				client.logError("Received invalid message: status="+status+" responseFor="+responseFor);
				return null;
			}
		}
		catch(JSONException e) {
			client.logError("Error parsing server message:",e);
			return null;
		}
	}
	
	
	String _json;
}