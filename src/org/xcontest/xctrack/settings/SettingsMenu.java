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

package org.xcontest.xctrack.settings;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.xcontest.xctrack.App;
import org.xcontest.xctrack.ScreenListener;
import org.xcontest.xctrack.config.Config;
import org.xcontest.xctrack.gps.GpsDeviceInfo;

/**
 *     MenuItem
 */
class MenuItem {
	MenuItem(String text, Menu submenu) {
		this(text,submenu,null);
	}
	MenuItem(String text, Menu submenu, Object data) {
		_text = text;
		_submenu = submenu;
		_data = data;
	}
	
	Menu getSubmenu() {
		return _submenu;
	}
	
	String getText() {
		String val = getCurrentValue();
		return val == null ? _text : (_text + " [" + val + "]");
	}
	
	boolean isSubmenu() {
		return _submenu != null;
	}
		
	protected String getCurrentValue() {
		return null;
	}
	
	Object getData() {
		return _data;
	}

	String _text;
	Menu _submenu;
	Object _data;
}

/**
 *     Menu
 */
abstract class Menu implements CommandListener, ScreenListener {	
	private final List createList(String parentCaption) {
//		int index;
		
		if (parentCaption != null) {
			if (parentCaption == "")
				_caption = getTitle();
			else
				_caption = parentCaption + " / " + getTitle();
		}
		
//nefunguje... proc??		index = _list == null ? 0 : _list.getSelectedIndex();
		_list = new List(_caption,List.IMPLICIT);
		if (_items == null || isDynamic())
			_items = createItems();
		for (int i = 0; i < _items.length; i ++)
			_list.append(_items[i].getText(), null);
//		if (index < _items.length)
//			_list.setSelectedIndex(index,true);
		
		
//		_cmdSelect = new Command("Select", Command.SCREEN, 1);
		_cmdBack = new Command("Back", Command.BACK, 1);
//		_list.addCommand(_cmdSelect);
		_list.addCommand(_cmdBack);
		if (hasDelete()) {
			_cmdDelete = new Command("Delete",Command.ITEM,2);
			_list.addCommand(_cmdDelete);
		}
		_list.setCommandListener(this);
		
		return _list;
	}
	
	void show(String title) {
		createList(title);
		App.showScreen(_list,this);
	}
	
	protected void hide() {
		App.hideScreen(_list);
	}

	private void rebuildMenu() {
		if (App.getCurrentDisplayable() == _list && isDynamic()) {
			App.hideScreen(_list);
			createList(null);
			App.showScreen(_list,this);
		}
	}

	public void commandAction(Command cmd, Displayable disp) {
		int cmdType = cmd.getCommandType();
		if (cmd == _cmdDelete) {
			MenuItem item = _items[_list.getSelectedIndex()];
			doDeleteAction(item);
			rebuildMenu();
		}
		else if (cmd == _cmdSelect || cmdType == Command.OK || cmdType == Command.SCREEN) {
			MenuItem item = _items[_list.getSelectedIndex()];
			if (item.isSubmenu()) {
				item.getSubmenu().show(_caption);
			}
			else {
				doAction(item);
				rebuildMenu();
			}
		}
		else if (cmd == _cmdBack || cmdType == Command.BACK || cmdType == Command.CANCEL || cmdType == Command.STOP || cmdType == Command.EXIT) {
			hide();
		}
	}	

	// menu visible (!explicit == again)
	public final void screenShown(Displayable disp, boolean explicit) {
		if (!explicit)
			rebuildMenu();
		shown(explicit);
	}

	protected void shown(boolean fromParent) {
	}
	
	protected boolean isDynamic() {
		return false;
	}
	
	protected boolean hasDelete() {
		return false;
	}
	
	protected void doDeleteAction(MenuItem it) {}
	protected void doAction(MenuItem it){}
	
	protected abstract MenuItem[] createItems();
	
	protected abstract String getTitle();
	
	protected String _caption;
	protected MenuItem[] _items;
	protected List _list;
	private Command _cmdSelect,_cmdBack,_cmdDelete;
}


/**
 * Menu definition
 */

// settings / display
class DisplayMenu extends Menu {

	protected String getTitle() {
		return "Display";
	}
	
	protected MenuItem[] createItems() {
		return new MenuItem[] {
				new MenuItem("Basics",null,"basics"),
		};
	}

	protected void doAction(MenuItem it) {
		if (it.getData().equals("basics")) {
			new DisplayBasicsPage().show();
		}
	}
}


// settings
class MainMenu extends Menu {
	protected String getTitle() {
		return "Settings";
	}
	
	protected MenuItem[] createItems() {
		MenuItem[] items = new MenuItem[] {
				new MenuItem("GPS",null,"gps") {
					protected String getCurrentValue() {
						GpsDeviceInfo dev = Config.getGpsDevice();
						if (dev != null) {
							if (dev.getName() != null && dev.getName() != "")
								return dev.getName();
							else if (dev.getAddress() != null && dev.getAddress() != "")
								return dev.getDriver().getName() + ":" + dev.getAddress();
							else
								return dev.getDriver().getName();
						}
						else {
							return "no device selected";
						}
					}
				},
				new MenuItem("Profiles",new ProfilesMenu()),
				new MenuItem("Display",new DisplayMenu()),
				new MenuItem("Internet Connection",null,"connection"),
				new MenuItem(Config.isDebugMode() ? "(disable Debug Mode)" : "(enable Debug Mode)",null,"debug"),
//				new MenuItem("Tracks",null),
		};
		
		return items;
	}
	
	protected void doAction(MenuItem it) {
		if (it.getData().equals("connection")) {
			new ConnectionPage().show();
		}
		else if (it.getData().equals("gps")) {
			new GpsPage().show();
		}
		else if (it.getData().equals("debug")) {
			Config.setDebugMode(!Config.isDebugMode());
			Config.writeAll();
		}
	}

	protected boolean isDynamic() {
		return true;
	}
	
}

//settings/profiles
class ProfilesMenu extends Menu {
	protected String getTitle() {
		return "Profiles";
	}

	protected MenuItem[] createItems() {
		Profile[] profiles = Config.getProfiles();
		
		MenuItem[] items = new MenuItem[profiles.length+1];
		for (int i = 0; i < profiles.length; i++) {
			items[i] = new MenuItem(profiles[i].getProfileName(),null,new Integer(i));
		}
		items[items.length-1] = new MenuItem("<create new profile>",null,null);
		return items;
	}
	
	protected boolean isDynamic() {
		return true;
	}
	
	protected void doAction(MenuItem it) {
		Integer idx = (Integer)it.getData();
		if (idx == null) {
			new ProfilePage(Config.getProfiles(),-1).show();
		}
		else {
			new ProfilePage(Config.getProfiles(),idx.intValue()).show();
		}
	}
	
	protected void doDeleteAction(MenuItem it) {
		Profile[] profiles = Config.getProfiles();
		Integer oidx = (Integer)it.getData();
		if (oidx != null) {
			int idx = oidx.intValue();
			Profile[] newProfiles = new Profile[profiles.length-1];
			for (int i = 0; i < idx; i ++)
				newProfiles[i] = profiles[i];
			for (int i = idx+1; i < profiles.length; i ++)
				newProfiles[i-1] = profiles[i];
			Config.setProfiles(newProfiles);
			Config.writeAll();
		}
	}
	
	protected boolean hasDelete() {
		return true;
	}
}


/**
 * SettingsMenu ... moc tu toho z chudacka nezbylo...
 */
public class SettingsMenu {
	private Menu _menu;
	
	public SettingsMenu() {
		_menu = new MainMenu();
	}
	
	public void show() {
		_menu.show("");
	}
}
