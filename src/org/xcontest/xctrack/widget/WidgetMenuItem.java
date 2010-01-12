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

import java.util.Hashtable;

import org.xcontest.xctrack.paint.Menu;
import org.xcontest.xctrack.paint.MenuItem;

public class WidgetMenuItem {
	WidgetMenuItem(Widget widget, String caption, WidgetMenuItem[] submenu) {
		_widget = widget;
		_caption = caption;
		_submenu = submenu;
	}
	
	MenuItem createMenuItem(String menuCaption, int action) {
		MenuItem mi = new MenuItem(_caption,
				_widget == null ? Menu.NOACTION : action,
				_submenu == null ? null : createMenu(menuCaption, _caption, action, _submenu));
		mi.setCustomData(_widget);
		return mi;
	}

	static Menu createMenu(String captionPrevious, String caption, int action, WidgetMenuItem[] items) {
		MenuItem[] mitems = new MenuItem[items.length+1];
		for (int i = 0; i < items.length; i ++)
			mitems[i] = items[i].createMenuItem(caption, action);
		mitems[items.length] = new MenuItem("<< " + captionPrevious,Menu.ACTION_CLOSE_MENU,false,MenuItem.STYLE_SMALL,null);
		return new Menu(caption,mitems);
	}
	
	static void fillByNameHashtable(WidgetMenuItem[] items, Hashtable byName) {
		for (int i = 0; i < items.length; i ++) {
			WidgetMenuItem it = items[i];
			if (it._widget != null)
				byName.put(it._widget.getName(),it._widget);
			if (it._submenu != null)
				fillByNameHashtable(it._submenu, byName);
		}
	}
	
	Widget _widget;
	String _caption;
	WidgetMenuItem[] _submenu;
}
