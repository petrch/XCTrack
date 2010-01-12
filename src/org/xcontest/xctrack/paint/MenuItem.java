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

public class MenuItem {
	
	public static final int STYLE_DEFAULT = 0;
	public static final int STYLE_SMALL = 1;
	
	public MenuItem(String caption, int action) {
		this(caption,action,false,STYLE_DEFAULT,null);
	}
	
	public MenuItem(String caption, int action, boolean isDefault) {
		this(caption,action,isDefault,STYLE_DEFAULT,null);
	}
	
	public MenuItem(String caption, int action, Menu submenu) {
		this(caption,action,false,STYLE_DEFAULT,submenu);
	}
	
	public MenuItem(String caption, int action, boolean isDefault, int style, Menu submenu) {
		_caption = caption;
		_action = action;
		_submenu = submenu;
		_isDefault = isDefault;
		_style = style;
		_customData = null;
	}
	
	public final Object getCustomData() {
		return _customData;
	}
	
	public final void setCustomData(Object o) {
		_customData = o;
	}
	
	public String getCaption() {
		return _caption;
	}
	
	public void setCaption(String caption) {
		_caption = caption;
	}
	
	public final int getAction() {
		return _action;
	}
	
	public final Menu getSubmenu() {
		return _submenu;
	}
	
	public final boolean isDefault() {
		return _isDefault;
	}
	
	public final int getStyle() {
		return _style;
	}
	
	Menu _submenu;
	String _caption;
	int _action;
	int _style;
	boolean _isDefault;
	Object _customData;
}

