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

import java.io.UnsupportedEncodingException;


public final class UTF8 {
	
	public static final byte[] encode(String str) {
		try {
			return str.getBytes("utf-8");
		}
		catch (UnsupportedEncodingException e) {
			return encodeSlow(str);
		}
	}
	
	public static final String decode(byte[] bytes) {
		return decode(bytes,0,bytes.length);
	}
	
	public static final String decode(byte[] bytes, int off, int len) {
		try {
			return new String(bytes,off,len,"utf-8");
		}
		catch (UnsupportedEncodingException e) {
			return decodeSlow(bytes, off, len);
		}
	}
	
	private static final byte[] encodeSlow(String str) {
		int len = 0;
		char[] chars = new char[str.length()];
		byte[] out;
		
		str.getChars(0, str.length(), chars, 0);
		
		for (int i = 0; i < chars.length; i ++) {
			if (chars[i] < 0x80)
				len ++;
			else if (chars[i] < 0x800)
				len += 2;
			else
				len += 3;
		}
		
		out = new byte[len];
		
		int pos = 0;
		for (int i = 0; i < chars.length; i ++) {
			int ch = (int)chars[i];
			if (ch < 0x80) {
				out[pos] = (byte)ch;
				pos ++;
			}
			else if (ch < 0x800) {
				out[pos] = (byte)(192 | (ch>>6));
				out[pos+1] = (byte)(128 | (ch&63));
				pos += 2;
			}
			else {
				out[pos] = (byte)(224 | (ch>>12));
				out[pos+1] = (byte)(128 | ((ch>>6)&63));
				out[pos+2] = (byte)(128 | (ch&63));
				pos += 3;
			}
		}
		
		return out;
	}
	
	private static final String decodeSlow(byte[] bytes, int off, int len) {
		int cnt = 0;
		int pos = 0;
		while (pos < len) {
			int b = (int)bytes[off+pos];
			if (b < 0) b += 256;
			if ((b & 0x80) == 0)
				pos ++;
			else if ((b >> 5) == 6)
				pos += 2;
			else if ((b >> 4) == 14)
				pos += 3;
			else
				pos ++;	// ?
			cnt ++;
		}
		
		char[] chars = new char[cnt];
		cnt = 0;
		pos = 0;
		while (pos < len) {
			int b = bytes[off+pos];
			if (b < 0) b += 256;
			if (b < 0x80) {
				chars[cnt] = (char)b;
				pos ++;
			}
			else if ((b >> 5) == 6) {
				int b2 = pos+1 < len ? bytes[off+pos+1] : 128;
				chars[cnt] = (char)(((b&31)<<6)|(b2&63));
				pos += 2;
			}
			else if ((b >> 4) == 14) {
				int b2 = pos+1 < len ? bytes[off+pos+1] : 128;
				int b3 = pos+2 < len ? bytes[off+pos+2] : 128;
				chars[cnt] = (char)(((b&15)<<12)|((b2&63)<<6)|(b3&63));
				pos += 3;
			}
			else {	// error or 4byte utf-8 sequence (not representable in UCS2
				chars[cnt] = '?';
				pos ++;
			}
			cnt ++;
		}
		
		return new String(chars);
	}
}




