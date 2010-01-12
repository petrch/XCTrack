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


package org.xcontest.xctrack.paint;

import java.io.IOException;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

import org.xcontest.xctrack.util.Log;

public class ImageFont extends GeneralFont {
	
	public ImageFont(String path, String chars, int height, int baseline, int[] widths) {
		_offsets = new int[widths.length+1];
		int o = 0;
		for (int i = 0; i < widths.length; i ++) {
			_offsets[i] = o;
			o += widths[i];
		}
		_offsets[widths.length] = o;
		
		int len = chars.length();
		int maxChar;
		_minChar = maxChar = chars.charAt(0);
		for (int i = 0; i < len; i ++) {
			int c = chars.charAt(i);
			if (c < _minChar) _minChar = c;
			if (c > maxChar) maxChar = c;
		}
		_charIndex = new int[maxChar-_minChar+1];
		for (int i = 0; i < _charIndex.length; i ++)
			_charIndex[i] = -1;
		for (int i = 0; i < len; i ++) {
			int c = chars.charAt(i);
			_charIndex[c-_minChar] = i;
		}
		
		_path = path;
		_height = height;
		_baseline = baseline;
		_img = null;
	}
	
	public int getHeight() {
		return _height;
	}
	
	public int substringWidth(String str, int start, int len) {
		int width = 0;
		for (int i = 0; i < len; i ++) {
			int c = str.charAt(start+i);
			if (c >= _minChar && c < _minChar+_charIndex.length) {
				int idx = _charIndex[c - _minChar];
				if (idx >= 0)
					width += _offsets[idx+1]-_offsets[idx];
			}
		}
		return width;
	}
	
	public void drawString(Graphics g, String str, int x, int y, int align) {
		drawSubstring(g,str,0,str.length(),x,y,align);
	}
	
	public void drawSubstring(Graphics g, String str, int pos, int len, int x, int y, int align) {
		try {
			if (_img == null)
				_img = Image.createImage(_path);
			
			if ((align & Graphics.BASELINE) != 0)
				y -= _baseline;
			else if ((align & Graphics.VCENTER) != 0)
				y -= _height/2;
			else if ((align & Graphics.BOTTOM) != 0)
				y -= _height;
			
			if ((align & Graphics.RIGHT) != 0)
				x -= substringWidth(str,pos,len);
			else if ((align & Graphics.HCENTER) != 0)
				x -= substringWidth(str,pos,len)/2;
			
			for (int i = 0; i < len; i ++) {
				int c = str.charAt(pos+i);
				if (c >= _minChar && c < _minChar+_charIndex.length) {
					int idx = _charIndex[c - _minChar];
					if (idx >= 0) {
						int width = _offsets[idx+1]-_offsets[idx];
						g.drawRegion(_img,_offsets[idx],0,width,_height,
											Sprite.TRANS_NONE,x,y,Graphics.TOP|Graphics.LEFT);
						x += width;
					}
				}
			}
		}
		catch (IOException e) {
			Log.error("Cannot load font "+_path);
		}
	}
	
	private String _path;
	private Image _img;
	private int _baseline;
	private int _height;
	private int[] _offsets;
	private int _minChar;
	private int[] _charIndex; 
}


