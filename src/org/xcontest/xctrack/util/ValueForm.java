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

import javax.microedition.lcdui.Form;

/**
 * Used to encapsulate the form for asking user a question - the answer gets retrieved by getValue, after the form is closed
 */
public abstract class ValueForm
{
	private Form _form;
	
	protected ValueForm() {
	}
	
	protected void setForm(Form f) {
		_form = f;
	}
	
	public Form getForm() {
		return _form;
	}

	public abstract Object getValue();
}

