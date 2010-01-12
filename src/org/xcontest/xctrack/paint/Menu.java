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

import java.io.IOException;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.xcontest.xctrack.util.Log;
import org.xcontest.xctrack.widget.WidgetPage;

public final class Menu {
	public static final int ACTION_SUBMENU_CHANGED = -2;
	public static final int ACTION_CLOSE_MENU = -1;
	public static final int NOACTION = 0;
	public static final int MENUITEM_MAX_HEIGHT = 96;

	public Menu(String caption, MenuItem[] items) {
		_caption = caption;
		
		// delete NULL items to make variable configuration easier
		int cnt = 0;
		for (int i = 0; i < items.length; i ++)
			if (items[i] != null)
				cnt += 1;
		if (cnt != items.length) {
			MenuItem[] newItems = new MenuItem[cnt];
			cnt = 0;
			for (int i = 0; i < items.length; i ++)
				if (items[i] != null)
					newItems[cnt++] = items[i];
			items = newItems;
		}
		
		
		_items = items;
		_openedFrom = null;

		_selIndex = 0;
		_activeSubmenu = null;

		_pointerPressed = false;
		_isTouchscreen = false;
		_font = Font.getFont(Font.FACE_PROPORTIONAL,Font.STYLE_BOLD,Font.SIZE_MEDIUM);
		_fontSmall = Font.getFont(Font.FACE_PROPORTIONAL,Font.STYLE_BOLD,Font.SIZE_SMALL);
		try {
			_imgCursor = Image.createImage("/img/menu/menu-cursor.png");
		}
		catch (IOException e) {
			Log.error("Cannot load menu cursor image");
		}
	}
	
	public MenuItem[] getItems() {
		return _items;
	}
	
	public void resetPosition(int w, int h, boolean touch) {
		int fonth = _font.getHeight();
		
		_isTouchscreen = touch;
		_screenW = w;
		_screenH = h;
		_menuX = w/10;
		_menuW = w-_menuX*2;
		_menuTitleH = fonth;
		
		_activeSubmenu = null;
		_selIndex = 0;
		for (int i = 0; i < _items.length; i ++) {
			if (_items[i].isDefault()) {
				_selIndex = i;
				break;
			}
		}
		
		if (_isTouchscreen)
			_menuItemH = MENUITEM_MAX_HEIGHT;
		else
			_menuItemH = fonth;
		
		if (_menuItemH*_items.length + _menuTitleH > h)
			_menuItemH = (h - _menuTitleH)/_items.length;
		_menuH = _menuItemH * _items.length;
		_menuY = (h-_menuH-_menuTitleH)/2 + _menuTitleH;
	}
	
	public void paint(Graphics g) {
		if (_activeSubmenu != null) {
			_activeSubmenu.paint(g);
		}
		else {
			int blp = _font.getBaselinePosition();
			int fonthDefault = _font.getHeight();
			int fonthSmall = _fontSmall.getHeight();
			int fonth;
			
			g.setColor(0xFFFFFF);
			g.drawRect(_menuX-1, _menuY-_menuTitleH-1, _menuW+1, _menuH+_menuTitleH+1);
			g.setColor(0x808080);
			g.drawRect(_menuX-2, _menuY-_menuTitleH-2, _menuW+3, _menuH+_menuTitleH+3);
			
			TransparentBox.paint(g,_menuX,_menuY-_menuTitleH,_menuW,_menuH,0xC0E0E0E0);
			g.setColor(0x000000);
			g.setFont(_font);
			g.drawString(_caption,_menuX+_menuW/2,_menuY-_menuTitleH+blp,Graphics.HCENTER|Graphics.BASELINE);
			
			for (int i = 0; i < _items.length; i ++) {
				int style = _items[i].getStyle(); 
				if (style == MenuItem.STYLE_SMALL) {
					g.setFont(_fontSmall);
					fonth = fonthSmall;
				}
				else {
					g.setFont(_font);
					fonth = fonthDefault;
				}
				if (i == _selIndex) {
					Painter.drawBackground(g, _imgCursor, _menuX, _menuY+i*_menuItemH, _menuW, _menuItemH, Graphics.LEFT | Graphics.VCENTER);
					g.setColor(0xffffff);
				}
				else {
					TransparentBox.paint(g,_menuX,_menuY+i*_menuItemH,_menuW,_menuItemH,TransparentBox.WHITE);
					g.setColor(style == MenuItem.STYLE_SMALL ? 0x808080 : 0x000000);
				}
				g.drawString(_items[i].getCaption(),_menuX+_menuW/16,_menuY+i*_menuItemH+(_menuItemH-fonth)/2,Graphics.LEFT|Graphics.TOP);
			}
		}
	}
	
	public void handlePointerPressed(int x, int y) {
		if (_isTouchscreen) {
			if (_activeSubmenu != null) {
				_activeSubmenu.handlePointerPressed(x, y);
			}
			else {
				_pointerPressed = true;
				
				int idx = y<_menuY ? -1 : (y-_menuY)/_menuItemH;
				if (idx >= 0 && idx < _items.length && idx != _selIndex)
					_selIndex = idx;
			}
		}
	}
	
	public void handlePointerDragged(int x, int y) {
		if (_isTouchscreen) {
			if (_activeSubmenu != null)
				_activeSubmenu.handlePointerDragged(x, y);
			else {
				int idx = y<_menuY ? -1 : (y-_menuY)/_menuItemH;
				if (idx >= 0 && idx < _items.length && idx != _selIndex)
					_selIndex = idx;
			}
		}
	}
	
	private int processMenuItemAction(MenuItem it, MenuResult res) {
		if (it.getSubmenu() != null) {
			_activeSubmenu = it.getSubmenu();
			_activeSubmenu._openedFrom = this;
			_activeSubmenu.resetPosition(_screenW,_screenH,_isTouchscreen);
			if (res != null) {
				res.customData = null;
			}
			return ACTION_SUBMENU_CHANGED;
		}
		int action = it.getAction();
		if (action == ACTION_CLOSE_MENU && _openedFrom != null) {
			_openedFrom._activeSubmenu = null;
			if (res != null) {
				res.customData = it.getCustomData();
			}
			return ACTION_SUBMENU_CHANGED;
		}
		else {
			if (res != null) {
				res.customData = it.getCustomData();
			}
			return action;
		}
	}
	
	public int handlePointerReleased(int x, int y, MenuResult res) {
		if (!_isTouchscreen) {
			if (res != null)
				res.customData = null;
			return NOACTION;
		}
		
		if (_activeSubmenu != null)
			return _activeSubmenu.handlePointerReleased(x, y, res);

		if (!_pointerPressed) { // pointer was pressed in different context, not this menu - so this is probably unwanted release
			if (res != null)
				res.customData = null;
			return NOACTION;
		}
		
		_pointerPressed = true;
		
		int idx = y<_menuY ? -1 : (y-_menuY)/_menuItemH;
		if (idx >= 0 && idx < _items.length) {
			_selIndex = idx;
			return processMenuItemAction(_items[idx],res);
		}
		
		if (res != null)
			res.customData = null;
		return NOACTION;
	}
	
	public int handleKey(int key, MenuResult res) {
		if (_activeSubmenu != null) {
			return _activeSubmenu.handleKey(key,res);
		}
		else {
			if (res != null)
				res.customData = null;

			if (key == WidgetPage.KEY_LEFT) {
				if (_openedFrom != null) {
					_openedFrom._activeSubmenu = null;
					return NOACTION;
				}
				else {
					return ACTION_CLOSE_MENU;
				}
			}
			else if (key == WidgetPage.KEY_UP) {
				if (_selIndex > 0)
					_selIndex --;
				else
					_selIndex = _items.length-1;
				return NOACTION;
			}
			else if (key == WidgetPage.KEY_DOWN) {
				if (_selIndex < _items.length-1)
					_selIndex ++;
				else
					_selIndex = 0;
				return NOACTION;
			}
			else if (key == WidgetPage.KEY_FIRE || key == WidgetPage.KEY_RIGHT) {
				return processMenuItemAction(_items[_selIndex],res);
			}
			else {
				return NOACTION;
			}
		}
	}
	
	public int getX() {
		return _menuX-2;
	}
	
	public int getY() {
		return _menuY-_menuTitleH-2;
	}
	
	public int getWidth() {
		return _menuW+4;
	}
	
	public int getHeight() {
		return _menuH+_menuTitleH+4;
	}
	
	private String _caption;
	private MenuItem[] _items;
	int _selIndex;
	private Menu _activeSubmenu;
	private Menu _openedFrom;
	private boolean _isTouchscreen;
	private boolean _pointerPressed;
	private Font _font;
	private Font _fontSmall;
	
	private Image _imgCursor;
	private int _menuTitleH,_menuItemH;
	private int _menuX,_menuY,_menuW,_menuH;
	private int _screenW,_screenH;
}
