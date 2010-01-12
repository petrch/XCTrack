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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;

import org.xcontest.xctrack.App;
import org.xcontest.xctrack.Util;
import org.xcontest.xctrack.config.WidgetPosition;
import org.xcontest.xctrack.widget.WidgetPage;

public class WidgetSettingsForm implements CommandListener {
	
	private WidgetPage _page;
	private WidgetPosition _wp;
	private Command _cmdSave;
	private Form _form;
	
	public WidgetSettingsForm(WidgetPage page,WidgetPosition wp) {
		_page = page;
		_wp = wp;
		
		Vector items = new Vector();
		wp.widget.createSettingsForm(items, wp.settings);
		
		_form = new Form("Widget Settings");
		for (int i = 0; i < items.size(); i ++)
			_form.append((Item)items.elementAt(i));
		_cmdSave = new Command("OK",Command.OK,1);
		_form.addCommand(_cmdSave);
		_form.setCommandListener(this);
	}
	
	public void show() {
		App.showScreen(_form);
	}
	
	private void hide() {
		_form.deleteAll();
		App.hideScreen(_form);
	}

	public void commandAction(Command cmd, Displayable disp) {
		if (cmd == _cmdSave) {
			String error = _wp.widget.validateSettingsForm();
			if (error == null) {
				_wp.widget.saveSettingsForm(_wp.settings);
				_page.saveWidgetLayout();
				hide();
			}
			else {
				Util.showError(error);
			}
		}
	}
}


