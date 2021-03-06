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

package org.xcontest.xctrack;

import javax.microedition.lcdui.Displayable;

public interface ScreenListener {
	/**
	 * 
	 * @param disp displayable just shown
	 * @param explicit if true then the call is result from App.showScreen()
	 * 					if false, then the call is resulted from App.hide() at another displayable
	 */
	public void screenShown(Displayable disp, boolean explicit);
}
