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

package org.xcontest.xctrack.gps;

import org.xcontest.live.UTF8;
import org.xcontest.xctrack.gps.GpsMessage;
import org.xcontest.xctrack.util.Log;


public class NMEAParser {

	class Field {
		final static int CHAR=1;
		final static int NUMBER=2;
		final static int UNKNOWN=3;

		int type;
		char ch;
		int num1;
		int num2;
		int scale2;
		
		int getNum2(int s) {
			int n = num2;
			for (; s < scale2; s ++) n /= 10;
			for (; s > scale2; s --) n *= 10;
			return n;
		}
		
		double getDouble() {
			double scale = 1;
			for (int i = 0; i < scale2; i ++) scale *= 10;
			return num1 + num2/scale;
		}
	}

	private final int MAXFIELDS = 10;
	private boolean _hasDate;
	private int _year,_month,_day;
	private Field[] _fields;
	private int _nFields;
	
	public NMEAParser() {
		_fields = new Field[MAXFIELDS];
		for (int i = 0; i < MAXFIELDS; i ++)
			_fields[i] = new Field();
	}

	public boolean parse(byte[] bytes, int length, GpsMessage msg) {
		boolean retval = false;
		

		// 1byte '$', 2 bytes for 'GP', 3 bytes message ID, 1 byte ','
		if (length < 7) return false;

		
		// every message starts with '$GPxxx,'
		if (bytes[0] != '$' || bytes[1] != 'G' || bytes[2] != 'P' || bytes[6] != ',') return false;
		
		// check checksum if present
		if (bytes[length-3] == '*') {
			int sum = checksum(bytes,1,length-4);
			if (sum != getHexNum(bytes[length-2],bytes[length-1])) {
				Log.info("NMEA GPS: Invalid checksum");
				Log.info("Message: "+UTF8.decode(bytes, 0, length));
				return false;
			}
			length -= 3;	// skip checksum for parsing
		}
		
		if (checkType(bytes,"RMC")) {
			msg.reset();
			
			parseFields(bytes,length);
			if (_nFields < 12 || _fields[11].type != Field.CHAR || _fields[11].ch == 'A' || _fields[11].ch == 'D') {
				// field 0 ... time
				// field 8 ... date
				if (_nFields >= 9 && _fields[0].type == Field.NUMBER && _fields[8].type == Field.NUMBER) {
					int nt = _fields[0].num1;
					int nd = _fields[8].num1;
					
					_hasDate = true;
					_year = 2000+nd%100;
					_month = (nd/100)%100;
					_day = (nd/10000)%100;
					msg.setTime(_year,_month,_day,
									((nt/10000)%24)*3600000 + (((nt/100)%100)%60)*60000 + ((nt%100)%60)*1000 + _fields[0].getNum2(6)/1000);
					retval = true;
				}
				if (_nFields >= 8 && _fields[6].type == Field.NUMBER && _fields[7].type == Field.NUMBER) {
					msg.setHeadingSpeed(_fields[7].getDouble(),_fields[6].getDouble()*0.514444);
					retval = true;
				}
				if (_nFields >= 6 && _fields[1].type == Field.CHAR && _fields[2].type == Field.NUMBER && _fields[3].type == Field.CHAR && _fields[4].type == Field.NUMBER && _fields[5].type == Field.CHAR) {
					if (_fields[1].ch == 'A' || _fields[1].ch == 'a') {		// valid
						double lat,lon;
						lat = (_fields[2].num1/100) + (_fields[2].num1%100)/60.0 + _fields[2].getNum2(8)/6000000000.0;
						if (_fields[3].ch != 'N' && _fields[3].ch != 'n') lat = -lat;
						lon = (_fields[4].num1/100) + (_fields[4].num1%100)/60.0 + _fields[4].getNum2(8)/6000000000.0;
						if (_fields[5].ch != 'E' && _fields[5].ch != 'e') lon = -lon;
						msg.setPosition(lon,lat);
						retval = true;
					}
				}
			}
//Log.info("Message: "+(msg.hasTime ? "T" : "") + (msg.hasPosition ? "P" : "") + (msg.hasAltitude ? "A" : "") + (msg.hasSatellites ? "S" : ""));
			return retval;
		}
		
		if (checkType(bytes,"GGA")) {
			parseFields(bytes,length);
			if (_hasDate && _nFields >= 1 && _fields[0].type == Field.NUMBER) {
				int nt = _fields[0].num1;
				msg.setTime(_year,_month,_day,
						((nt/10000)%24)*3600000 + (((nt/100)%100)%60)*60000 + ((nt%100)%60)*1000 + _fields[0].getNum2(6)/1000);
				retval = true;
			}
			
			if (_nFields >= 7 && _fields[1].type == Field.NUMBER && _fields[2].type == Field.CHAR && _fields[3].type == Field.NUMBER && _fields[4].type == Field.CHAR && _fields[5].type == Field.NUMBER) {
				if (_fields[5].num1 != 0 && _fields[5].num1 != 6) {
					double lat,lon;
					lat = (_fields[1].num1/100) + (_fields[1].num1%100)/60.0 + _fields[1].getNum2(8)/6000000000.0;
					if (_fields[2].ch != 'N' && _fields[2].ch != 'n') lat = -lat;
					lon = (_fields[3].num1/100) + (_fields[3].num1%100)/60.0 + _fields[3].getNum2(8)/6000000000.0;
					if (_fields[4].ch != 'E' && _fields[4].ch != 'e') lon = -lon;
				
					msg.setPosition(lon,lat);
					if (_nFields >= 9 && _fields[8].type == Field.NUMBER)
						msg.setAltitude(_fields[8].getDouble());
					retval = true;
				}
				if (_fields[6].type == Field.NUMBER) {
					msg.setSatellites(_fields[6].num1);
					retval = true;
				}
				if (_nFields >= 8 && _fields[7].type == Field.NUMBER) {
					msg.setPrecision(_fields[8].getDouble());
					retval = true;
				}
			}
			return retval;
		}

		return false;
	}
	
	private void parseFields(byte[] bytes, int length) {
		int pos = 7;
		_nFields = 0;
		while (pos < length && _nFields < MAXFIELDS) {
			int i;
			for (i = pos; i < length && bytes[i] != ','; i ++);
			parseField(bytes,pos,i-pos);
			pos = i+1;
		}
	}
	
	private void parseField(byte[] bytes, int offset, int length) {
		if (length == 1 && (bytes[offset] < '0' || bytes[offset] > '9')) {
			_fields[_nFields].type = Field.CHAR;
			_fields[_nFields].ch = (char)bytes[offset];
		}
		else {
			int i,num1=0,num2=0,scale2=0;
			boolean hasDot = false;
			boolean valid = true;
			for (i = 0; valid && i < length; i ++) {
				int ch = bytes[offset+i];
				if (ch == '.' && !hasDot) {
					hasDot = true;
				}
				else if (ch >= '0' && ch <= '9') {
					if (hasDot) {
						num2 = num2*10+ch-'0';
						scale2 ++;
					}
					else
						num1 = num1*10+ch-'0';
				}
				else
					valid = false;
			}
			if (valid && length >= 1) {
				_fields[_nFields].type = Field.NUMBER;
				_fields[_nFields].num1 = num1;
				_fields[_nFields].num2 = num2;
				_fields[_nFields].scale2 = scale2;
			}
			else {
				_fields[_nFields].type = Field.UNKNOWN;
			}
		}
		_nFields ++;
	}
	
	private boolean checkType(byte[] bytes, String type) {
		return bytes[3] == type.charAt(0) && bytes[4] == type.charAt(1) && bytes[5] == type.charAt(2);
	}
	
	private int checksum(byte[] bytes, int offset, int length) {
		int sum = 0;
		for (int i = 0; i < length; i ++)
			sum ^= bytes[offset+i];
		return sum;
	}
	
	private int getHexNum(int a, int b) {
		if (a >= '0' && a <= '9') a -= '0';
		else if (a >= 'a' && a <= 'f') a += 10-'a';
		else if (a >= 'A' && a <= 'F') a += 10-'A';
		else a = 0;
		
		if (b >= '0' && b <= '9') b -= '0';
		else if (b >= 'a' && b <= 'f') b += 10-'a';
		else if (b >= 'A' && b <= 'F') b += 10-'A';
		else b = 0;
		
		return a*16+b;
	}
}


