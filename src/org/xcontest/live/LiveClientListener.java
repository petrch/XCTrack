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

public interface LiveClientListener {
	/// Notification from RECEIVER thread
	public void openConnectionFailed(String remote);

	/// Notification from RECEIVER thread
	public void connectionOpened(String remote);
	
	/**
	 * Called after the session is opened
	 * @param key session key
	 * @param domain login domain or null for anonymous session
	 * @param username login username or null for anonymous session
	 * 
	 * Notification from RECEIVER thread
	 */
	public void loginOK(String key, String domain, String username);
	
	/// Notification from RECEIVER thread
	public void loginFailed(String domain, String username);
	
	// Notification from RECEIVER thread
	public void statusUpdate();
}

