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


package org.xcontest.xctrack.widget.settings;

import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.TextField;


public final class TextBoxSettings extends WidgetSettings {
	public class Data {
		public boolean showCaption;
		public String caption;
	}
	
	private ChoiceGroup _choiceDisplayCaption;
	private TextField _textCaption;
	private String _defaultCaption;
	
	public TextBoxSettings(String defaultCaption) {
		_defaultCaption = defaultCaption;
	}

	public Object load(String str) {
		Data d = new Data();
		if (str == null || str.length() == 0) {
			d.showCaption = true;
			d.caption = _defaultCaption;
		}
		else {
			d.showCaption = str.startsWith("S");
			d.caption = str.substring(1);
		}
		return d;
	}

	public String save(Object obj) {
		Data d = (Data)obj;
		return (d.showCaption ? "S" : "H") + d.caption;
	}

	public void createForm(Vector items, Object obj) {
		Data d = (Data)obj;
		
		_choiceDisplayCaption = new ChoiceGroup("General Options",ChoiceGroup.MULTIPLE);
		_choiceDisplayCaption.append("Display Title", null);
		_textCaption = new TextField("Title", "", 64, TextField.ANY);

		_choiceDisplayCaption.setSelectedIndex(0, d.showCaption);
		_textCaption.setString(d.caption);
		
		items.addElement(_choiceDisplayCaption);
		items.addElement(_textCaption);
	}
	
	public void saveForm(Object obj) {
		Data d = (Data)obj;

		d.showCaption = _choiceDisplayCaption.isSelected(0);
		d.caption = _textCaption.getString();
	}
	

}
