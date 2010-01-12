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

import java.io.InterruptedIOException;

import org.xcontest.live.message.ServerMessage;

class LiveReceiver extends Thread {
	private LiveClient _client;
	private LiveConnection _conn;
	
	public LiveReceiver(LiveClient client, LiveConnection conn) {
		super("LiveReceiver");
		_client = client;
		_conn = conn;
	}
	
	public void stop() {
		interrupt();
	}
	
	public void run() {
		_client.logInfo("LiveReceiver: Started");

		try {
			while (true) {
				boolean doWait = !_conn.reconnect();
				

				String str = _conn.recvStringMessage();
					
				if (str != null) {
					_client.logInfo("RECV: "+str);
					ServerMessage msg = ServerMessage.parse(_client,str);
					if (msg != null) {
						_client.receivedMessage(msg);
					}
				}
				
				if (doWait) {
					Thread.sleep(_conn.getReceiveReconnectInterval());
				}
			}
		}
		catch (InterruptedIOException e) {}
		catch (InterruptedException e) {}
		catch (Throwable e) {
			_client.logError("LiveReceiver: FATAL",e);
		}
		
		_client.logInfo("LiveReceiver: finished");
	}
}
