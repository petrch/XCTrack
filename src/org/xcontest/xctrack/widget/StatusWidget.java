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

package org.xcontest.xctrack.widget;

import java.io.IOException;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.xcontest.xctrack.info.InfoCenter;
import org.xcontest.xctrack.info.LiveInfo;
import org.xcontest.xctrack.info.LocationInfo;
import org.xcontest.xctrack.paint.GeneralFont;
import org.xcontest.xctrack.paint.TextPainter;
import org.xcontest.xctrack.util.Log;

public class StatusWidget extends Widget {
	TextPainter _textPainter;
	Image _imgGpsNotConnected;
	Image _imgGpsOk;
	Image _imgGpsNoSignal;
	Image _imgLiveNoConnection;
	Image _imgLiveNoResponse;
	Image _imgLivePending;
	Image _imgLiveOk;
	int _loadedSize;
	
	public StatusWidget() {
		_textPainter = new TextPainter(GeneralFont.SystemFontsBold,1);
		_loadedSize = 0;
	}
	
	private boolean loadIcons(int size) {
		if (_loadedSize != size) {
			try {
				String dir = "/img/status/"+size+"/";
				_imgGpsNotConnected = Image.createImage(dir+"gps_not_connected.png");
				_imgGpsOk = Image.createImage(dir+"gps_ok.png");
				_imgGpsNoSignal = Image.createImage(dir+"gps_no_signal.png");
				_imgLiveNoConnection = Image.createImage(dir+"live_no_connection.png");
				_imgLiveNoResponse = Image.createImage(dir+"live_no_response.png");
				_imgLivePending = Image.createImage(dir+"live_pending.png");
				_imgLiveOk = Image.createImage(dir+"live_ok.png");
			} catch (IOException e) {
				Log.error("StatusWidget: Cannot load images!",e);
			}
			_loadedSize = size;
			return true;
		}
		else {
			return false;
		}
	}
	
	protected int getDefaultWidth() { return 2000; }
	protected int getDefaultHeight() { return 24; }
	
	protected void paint(Graphics g, Object[] objSettings) {
		LocationInfo loc = InfoCenter.getInstance().getLocationInfo();
		LiveInfo live = InfoCenter.getInstance().getLiveInfo();
		int clipx = g.getClipX();
		int clipy = g.getClipY();
		int clipw = g.getClipWidth();
		int cliph = g.getClipHeight();
		
		int size = cliph >= 23 && clipw >= 47 ? 24 : 16;
		int y = clipy+(cliph-size)/2;
		int iconwidth = 9*size/4;
		
		loadIcons(size);		// (re-)load icons

		g.setColor(0x404040);
		g.fillRect(clipx, clipy, clipw, cliph);

		g.setColor(0xFFFFFF);
		String text = "Data: " + formatBytes(live.getBytesReceived()+live.getBytesSent());
		_textPainter.paint(g, text, 0, clipx+iconwidth+4, clipy, clipw-2*iconwidth-8, cliph, Graphics.HCENTER|Graphics.VCENTER);
		
		int x = clipx+clipw-iconwidth;
		// live doprava
		if (live.isConnected()) {
			if (live.getConfirmedMessages() == 0)
				g.drawImage(_imgLiveNoResponse, x, y, Graphics.LEFT | Graphics.TOP);
			else if (live.getPendingMessages() > 0)
				g.drawImage(_imgLivePending, x, y, Graphics.LEFT | Graphics.TOP);
			else
				g.drawImage(_imgLiveOk, x, y, Graphics.LEFT | Graphics.TOP);
		}
		else {
			g.drawImage(_imgLiveNoConnection, x, y, Graphics.LEFT | Graphics.TOP);
		}
		
		x = clipx;
		
		// gps doleva
		if (loc.isGpsConnected()) {
			if (loc.hasGpsSignal())
				g.drawImage(_imgGpsOk, x, y, Graphics.LEFT | Graphics.TOP);
			else
				g.drawImage(_imgGpsNoSignal, x, y, Graphics.LEFT | Graphics.TOP);
		}
		else {
			g.drawImage(_imgGpsNotConnected, x, y, Graphics.LEFT | Graphics.TOP);
		}
	}
	
	public String getName() {
		return "Status";
	}
	
	private String formatBytes(int n) {
		if (n < 10*1024) {
			return ""+n/1024+"."+((10*n/1024)%10)+"Kb";
		}
		else if (n < 1024*1024) {
			return ""+n/1024+"Kb";
		}
		else if (n < 10*1024*1024) {
			return ""+n/(1024*1024)+"."+((10*n/(1024*1024))%10)+"Mb";
		}
		else {
			return ""+n/(1024*1024)+"Mb";
		}
	}
}


