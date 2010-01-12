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

package org.xcontest.xctrack.util;

import java.util.Hashtable;

public final class Sort {
	private static void sortWithIntHashtable(Object[] arr, Hashtable h, int ll, int rr) {
		
		while (true) {
			int l = ll;
			int r = rr;
			
			if (l >= r)
				return;
			
			if (l+1 == r) {
				if (((Integer)h.get(arr[l])).intValue() > ((Integer)h.get(arr[r])).intValue()) {
					Object tmp = arr[l];
					arr[l] = arr[r];
					arr[r] = tmp;
				}
				return;
			}
			
			int x = ((Integer)h.get(arr[(l+r)/2])).intValue();
			while (l <= r) {
				while (((Integer)h.get(arr[l])).intValue() < x) l ++;
				while (((Integer)h.get(arr[r])).intValue() > x) r --;
				if (l <= r) {
					Object tmp = arr[l];
					arr[l] = arr[r];
					arr[r] = tmp;
					l ++;
					r --;
				}
			}
			if (r-ll > rr-l) {
				sortWithIntHashtable(arr,h,l,rr);
				rr = r;
			}
			else {
				sortWithIntHashtable(arr,h,ll,r);
				ll = l;
			}
		}
	}
	
	public static void sortWithIntHashtable(Object[] arr, Hashtable h) {
		sortWithIntHashtable(arr,h,0,arr.length-1);
	}
}



