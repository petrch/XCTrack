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

package org.xcontest.xctrack.settings;


public class Profile {
	final static double DEFAULT_TRACKLOG_INTERVAL = 2;
	final static double DEFAULT_MESSAGE_INTERVAL = 60;

	// setters and getters
	public void setUsername(String username) {
		_username = username;
	}
	public String getUsername() {
		return _username;
	}
	public void setDomain(String domain) {
		_domain = domain;
	}
	public String getDomain() {
		return _domain;
	}
	public void setPassword(String password) {
		_password = password;
	}
	public String getPassword() {
		return _password;
	}
	public void setFirstname(String firstname) {
		_firstname = firstname;
	}
	public String getFirstname() {
		return _firstname;
	}
	public void setSurname(String surname) {
		_surname = surname;
	}
	public String getSurname() {
		return _surname;
	}
	public void setNickname(String nickname) {
		_nickname = nickname;
	}
	public String getNickname() {
		return _nickname;
	}
	public void setAnonymous(boolean anonymous) {
		_anonymous = anonymous;
	}
	public boolean isAnonymous() {
		return _anonymous;
	}
	public void setTrackType(String trackType) {
		_trackType = trackType;
	}
	public String getTrackType() {
		return _trackType;
	}

	public void setProfileName(String profileName) {
		_profileName = profileName;
	}
	public String getProfileName() {
		return _profileName;
	}
	
	public double getTracklogInterval() {
		return _tracklogInterval;
	}
	
	public void setTracklogInterval(double val) {
		_tracklogInterval = val;
	}
	
	public double getMessageInterval() {
		return _messageInterval;
	}
	
	public void setMessageInterval(double val) {
		_messageInterval = val;
	}

	// serialization for config
	public static Profile fromStringArray(String[] arr) {
		if (arr.length < 6)
			return null;

		Profile p = new Profile();
		
		p._profileName = arr[0];
		p._anonymous = arr[1].equals("ANON");
		if (p._anonymous) {
			p._firstname = arr[2];
			p._surname = arr[3];
			p._nickname = arr[4];
		}
		else {
			p._username = arr[2];
			p._domain = arr[3];
			p._password = arr[4];
		}
		
		p._trackType = arr[5];
		
		if (arr.length >= 7) {
			try {
				p._tracklogInterval = Double.parseDouble(arr[6]);
			}
			catch (Exception e) {
				p._tracklogInterval = DEFAULT_TRACKLOG_INTERVAL;
			}
		}
		else {
			p._tracklogInterval = DEFAULT_TRACKLOG_INTERVAL;
		}

		if (arr.length >= 8) {
			try {
				p._messageInterval = Double.parseDouble(arr[7]);
			}
			catch (Exception e) {
				p._messageInterval = DEFAULT_MESSAGE_INTERVAL;
			}
		}
		else {
			p._messageInterval = DEFAULT_MESSAGE_INTERVAL;
		}
		
		return p;
	}
	
	public String[] toStringArray() {
		if (_anonymous) {
			return new String[]{_profileName,"ANON",_firstname,_surname,_nickname,_trackType,""+_tracklogInterval,""+_messageInterval};
		}
		else {
			return new String[]{_profileName,"REG",_username,_domain,_password,_trackType,""+_tracklogInterval,""+_messageInterval};
		}
	}
	
	private String _profileName;

	private boolean _anonymous;
	
	private String _username;
	private String _domain;
	private String _password;
	
	private String _firstname;
	private String _surname;
	private String _nickname;
	
	private String _trackType;
	private double _tracklogInterval;
	private double _messageInterval;
}
