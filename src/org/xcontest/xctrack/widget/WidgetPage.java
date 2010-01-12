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

import java.io.IOException;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.xcontest.xctrack.App;
import org.xcontest.xctrack.ScreenListener;
import org.xcontest.xctrack.info.InfoCenter;
import org.xcontest.xctrack.Util;
import org.xcontest.xctrack.config.Config;
import org.xcontest.xctrack.config.WidgetPosition;
import org.xcontest.xctrack.paint.GeneralFont;
import org.xcontest.xctrack.paint.Menu;
import org.xcontest.xctrack.paint.MenuItem;
import org.xcontest.xctrack.paint.MenuResult;
import org.xcontest.xctrack.paint.TextPainter;
import org.xcontest.xctrack.util.Log;
import org.xcontest.xctrack.widget.settings.WidgetSettingsForm;

public class WidgetPage extends Canvas implements ScreenListener {
	
	private static final int STATE_SHOW = 1;
	private static final int STATE_MAIN_MENU = 2;
	private static final int STATE_WIDGET_MENU = 3;
	private static final int STATE_CHOOSE_WIDGET = 4;
	private static final int STATE_MOVE_WIDGET = 5;
	private static final int STATE_RESIZE_WIDGET = 6;
	
	// main menu
	private static final int ACTION_QUIT = 1;
	private static final int ACTION_ORGANIZE_WIDGETS = 2;
	private static final int ACTION_RESTORE_LAYOUT = 3;
	private static final int ACTION_ADD_WIDGET = 4;
	private static final int ACTION_BACKLIGHT = 5;
	
	// widget menu
	private static final int ACTION_MOVE_WIDGET = 1;
	private static final int ACTION_RESIZE_WIDGET = 2;
	private static final int ACTION_PULL_WIDGET = 3;
	private static final int ACTION_PUSH_WIDGET = 4;
	private static final int ACTION_DELETE_WIDGET = 5;
	private static final int ACTION_SELECT_WIDGET = 6;
	private static final int ACTION_WIDGET_SETTINGS = 7;
	
	public static final int NOKEY = 0;
	public static final int KEY_LEFT = 1;
	public static final int KEY_RIGHT = 2;
	public static final int KEY_UP = 3;
	public static final int KEY_DOWN = 4;
	public static final int KEY_FIRE = 5;
	public static final int KEY_BACK = 6;
	
	public WidgetPage() {
		setFullScreenMode(true);
		_ncols = getWidth()/8;
		_nrows = getHeight()/8;
		_state = STATE_SHOW;

		if (!loadWidgetLayout())
			restoreDefaultLayout();

		_menuResult = new MenuResult();
		_useSoftkeys = hasPointerEvents();
		_touchHintDisplayTimeout = -1;
		_touchHintMenuPainter = new TextPainter(GeneralFont.SystemFontsBold,1);

		try {
			_imageMove = Image.createImage("/img/icons/move.png");
			if (_useSoftkeys) {
				_imgSoftkeyUp = Image.createImage("/img/softkeys/up.png");
				_imgSoftkeyRight = Image.createImage("/img/softkeys/right.png");
				_imgSoftkeyDown = Image.createImage("/img/softkeys/down.png");
				_imgSoftkeyLeft = Image.createImage("/img/softkeys/left.png");
				_imgSoftkeyFire = Image.createImage("/img/softkeys/fire.png");
				_imgSoftkeyUpSel = Image.createImage("/img/softkeys/up-sel.png");
				_imgSoftkeyRightSel = Image.createImage("/img/softkeys/right-sel.png");
				_imgSoftkeyDownSel = Image.createImage("/img/softkeys/down-sel.png");
				_imgSoftkeyLeftSel = Image.createImage("/img/softkeys/left-sel.png");
				_imgSoftkeyFireSel = Image.createImage("/img/softkeys/fire-sel.png");
				
				if (getWidth() < getHeight()) {
					_softkeyW = getWidth() / 3;
					_softkeyH = _softkeyW;
				}
				else {
					_softkeyH = getHeight() / 3;
					_softkeyW = _softkeyH;
				}
				_softkeysPanelX = getWidth()-3*_softkeyW;
				_softkeysPanelY = getHeight()-3*_softkeyH;
				_selectedSoftkey = NOKEY;
			}
		}
		catch (IOException e) {
			Log.error("Cannot load image (move/softkey icons)",e);
			_imageMove = null;
		}
		
	}
	
	public void saveWidgetLayout() {
		Config.setWidgetLayout(_widgets);
		Config.writeAll();
	}
	
	private boolean loadWidgetLayout() {
		WidgetPosition[] wp = Config.getWidgetLayout();
		if (wp == null)
			return false;
		_widgets = wp;
		return true;
	}
	
	private void restoreDefaultLayout() {
		int top;
		int nrows;
		int y;
		
		_widgets = new WidgetPosition[6];
		_widgets[0] = new WidgetPosition();
		_widgets[0].col = 0;
		_widgets[0].row = 0;
		_widgets[0].ncols = _ncols;
		_widgets[0].nrows = 3;
		_widgets[0].widget = Widget.getWidgetByName("Status");
		top = _widgets[0].nrows;
		
		_widgets[1] = new WidgetPosition();
		_widgets[1].col = 0;
		_widgets[1].row = _nrows-4;
		_widgets[1].ncols = _ncols;
		_widgets[1].nrows = 4;
		_widgets[1].widget = Widget.getWidgetByName("HeadingBar");
		nrows = _nrows - top - _widgets[1].nrows;

		y = 0;
		_widgets[2] = new WidgetPosition();
		_widgets[2].col = 0;
		_widgets[2].row = top+y;
		_widgets[2].ncols = _ncols;
		_widgets[2].nrows = nrows/3;
		_widgets[2].widget = Widget.getWidgetByName("Time");
		y += _widgets[2].nrows;
		
		_widgets[3] = new WidgetPosition();
		_widgets[3].col = 2*_ncols/5;
		_widgets[3].row = top+y;
		_widgets[3].ncols = _ncols-2*_ncols/5;
		_widgets[3].nrows = nrows-y;
		_widgets[3].widget = Widget.getWidgetByName("Traffic");

		_widgets[4] = new WidgetPosition();
		_widgets[4].col = 0;
		_widgets[4].row = top+y;
		_widgets[4].ncols = 2*_ncols/5;
		_widgets[4].nrows = nrows/3;
		_widgets[4].widget = Widget.getWidgetByName("Speed");
		y += _widgets[4].nrows;

		_widgets[5] = new WidgetPosition();
		_widgets[5].col = 0;
		_widgets[5].row = top+y;
		_widgets[5].ncols = 2*_ncols/5;
		_widgets[5].nrows = nrows-y;
		_widgets[5].widget = Widget.getWidgetByName("Altitude");
		y += _widgets[5].nrows;
		
		for (int i = 0; i < _widgets.length; i ++)
			_widgets[i].settings = _widgets[i].widget.loadSettings(null);	// default settings
	}
	
	public void show() {
		_state = STATE_SHOW;
		App.showScreen(this);
		doFullRepaint();
	}

	public void screenShown(Displayable disp, boolean explicit) {
		if (!explicit)
			doFullRepaint();
	}
	
	//////////////////////////////////////////////////////
	//// soft keys stuff start

	private int getSoftkey(int x,int y) {
		x -= _softkeysPanelX;
		y -= _softkeysPanelY;
		int xtile = x/_softkeyW;
		int ytile = y/_softkeyH;
		x -= xtile*_softkeyW;
		y -= ytile*_softkeyH;
		
		if (xtile < 0 || xtile > 2 || ytile < 0 || ytile > 2)
			return NOKEY;
		
		if (xtile == 0) {
			if (ytile == 0)
				return x > y ? KEY_UP : KEY_LEFT;
			else if (ytile == 1)
				return KEY_LEFT;
			else
				return x > _softkeyH-y ? KEY_DOWN : KEY_LEFT;
		}
		else if (xtile == 1) {
			if (ytile == 0)
				return KEY_UP;
			else if (ytile == 1)
				return KEY_FIRE;
			else
				return KEY_DOWN;
		}
		else {
			if (ytile == 0)
				return x > _softkeyH-y ? KEY_RIGHT : KEY_UP;
			else if (ytile == 1)
				return KEY_RIGHT;
			else
				return x > y ? KEY_RIGHT : KEY_DOWN;
		}
	}
	
	private boolean isSoftkeyVisibleState() {
		return _useSoftkeys && (_state == STATE_CHOOSE_WIDGET || _state == STATE_MOVE_WIDGET || _state == STATE_RESIZE_WIDGET);
	}
	
	protected void pointerPressed(int x, int y) {
		if (_state == STATE_MAIN_MENU) {
			_mainMenu.handlePointerPressed(x,y);
			repaint();
		}
		else if (_state == STATE_WIDGET_MENU) {
			_widgetMenu.handlePointerPressed(x,y);
			repaint();
		}
		else if (isSoftkeyVisibleState()) {
			_selectedSoftkey = getSoftkey(x,y);
			if (_selectedSoftkey != NOKEY)
				repaint();		// we use the fact that focused key image clears (covers) the not-focused key image
		}
	}
	
	protected void pointerDragged(int x, int y) {
		if (_state == STATE_MAIN_MENU) {
			_mainMenu.handlePointerDragged(x,y);
			repaint();
		}
		else if (_state == STATE_WIDGET_MENU) {
			_widgetMenu.handlePointerDragged(x,y);
			repaint();
		}
		else if (isSoftkeyVisibleState()) {
			int last = _selectedSoftkey;
			_selectedSoftkey = getSoftkey(x,y);
			if (_selectedSoftkey != last)
				doFullRepaint();	// we need to repaint the old focused key
		}
	}
	
	protected void pointerReleased(int x, int y) {
		if (_state == STATE_MAIN_MENU) {
			handleMainMenuAction(_mainMenu.handlePointerReleased(x,y,_menuResult));
		}
		else if (_state == STATE_WIDGET_MENU) {
			handleWidgetMenuAction(_widgetMenu.handlePointerReleased(x,y,_menuResult));
		}
		else if (_useSoftkeys && _state == STATE_SHOW) {
			int w = getWidth();
			int h = getHeight();
			int s = (w < h ? w : h)/3;
			if (x >= w-s && y < s) {
				handleKey(KEY_FIRE);
				_touchHintDisplayTimeout = -1;
			}
			else {
				_touchHintDisplayTimeout = System.currentTimeMillis()+3000;
				repaint();
			}
		}
		else if (isSoftkeyVisibleState()) {
			int key = getSoftkey(x,y);
			_selectedSoftkey = NOKEY;
			if (key != NOKEY) {
				handleKey(key);
				doFullRepaint();	// we need to repaint the focused key
			}
		}
	}
	
	
	//// soft keys stuff end
	//////////////////////////////////////////////////
	
	

	protected void keyPressed(int keyCode) {
		handleKeyCode(keyCode);
	}
	
	protected void keyRepeated(int keyCode) {
		handleKeyCode(keyCode);
	}
	
	private boolean WidgetIsFollower(WidgetPosition a, WidgetPosition b, int aIdx, int bIdx, int key) {
		int check1=0,check2=0;
		boolean idxCheck = false;
		if (key == KEY_LEFT) { check1 = a.row-b.row; check2 = a.col-b.col; idxCheck = bIdx < aIdx; }
		else if (key == KEY_RIGHT) { check1 = b.row-a.row; check2 = b.col-a.col; idxCheck = aIdx < bIdx; }
		else if (key == KEY_UP) { check1 = a.col-b.col; check2 = a.row-b.row; idxCheck = bIdx < aIdx; }
		else if (key == KEY_DOWN) { check1 = b.col-a.col; check2 = b.row-a.row; idxCheck = aIdx < bIdx; }
		if (check1 > 0) return true;
		if (check1 < 0) return false;
		if (check2 > 0) return true;
		if (check2 < 0) return false;
		return idxCheck;
	}
	
	private void handleKeyCode(int keyCode) {
		int gameAction = getGameAction(keyCode);
		int key;
		
		if (gameAction == LEFT) key = KEY_LEFT;
		else if (gameAction == RIGHT) key = KEY_RIGHT;
		else if (gameAction == UP) key = KEY_UP;
		else if (gameAction == DOWN) key = KEY_DOWN;
		else if (gameAction == FIRE) key = KEY_FIRE;
		else if (keyCode == -11) key = KEY_BACK;	// Sony-Erricsson BACK key
		else if (keyCode == -6 || keyCode == -7) key = KEY_FIRE;	// left and right choice keys placed under display
		else key = NOKEY;
		
		if (key != NOKEY)
			handleKey(key);
	}
	
	private void handleMainMenuAction(int action) {
		if (action == Menu.ACTION_CLOSE_MENU) {
			_state = STATE_SHOW;
			doFullRepaint();
		}
		else if (action == ACTION_ORGANIZE_WIDGETS && _widgets.length > 0) {
			_state = STATE_CHOOSE_WIDGET;
			_widgetIndex = 0;
			doFullRepaint();
		}
		else if (action == ACTION_RESTORE_LAYOUT) {
			restoreDefaultLayout();
			saveWidgetLayout();
			_state = STATE_SHOW;
			doFullRepaint();
		}
		else if (action == ACTION_ADD_WIDGET) {
			Widget widget = (Widget)_menuResult.customData;
			int w = getWidth();
			int h = getHeight();
			int ncols = (widget.getDefaultWidth()*_ncols+w/2)/w;
			int nrows = (widget.getDefaultHeight()*_nrows+h/2)/h;
			if (ncols < 1) ncols = 1;
			if (ncols > _ncols) ncols = _ncols;
			if (nrows < 1) nrows = 1;
			if (nrows > _nrows) nrows = _nrows;
			WidgetPosition[] newWidgets = new WidgetPosition[_widgets.length+1];
			System.arraycopy(_widgets, 0, newWidgets, 0, _widgets.length);
			newWidgets[_widgets.length] = new WidgetPosition();
			newWidgets[_widgets.length].col = (_ncols-ncols)/2;
			newWidgets[_widgets.length].row = (_nrows-nrows)/2;
			newWidgets[_widgets.length].ncols = ncols;
			newWidgets[_widgets.length].nrows = nrows;
			newWidgets[_widgets.length].widget = widget;
			newWidgets[_widgets.length].settings = widget.loadSettings(null);
			_widgets = newWidgets;
			
			// choose added widget
			_widgetIndex = _widgets.length-1;
			
			_state = STATE_MOVE_WIDGET;
			rebuildWidgetMenu();
			doFullRepaint();
		}
		else if (action == ACTION_QUIT) {
			InfoCenter.getInstance().stopTracking();
			doFullRepaint();
		}
		else if (action == Menu.ACTION_SUBMENU_CHANGED) {
			doFullRepaint();
		}
		else if (action == ACTION_BACKLIGHT) {
			InfoCenter ic = InfoCenter.getInstance();
			ic.keepBacklight(!ic.keepsBacklight());
			_state = STATE_SHOW;
			doFullRepaint();
		}
		else {
			repaint();
		}
	}
	
	private void handleWidgetMenuAction(int action) {
		if (action == ACTION_MOVE_WIDGET) {
			_state = STATE_MOVE_WIDGET;
			doFullRepaint();
		}
		else if (action == ACTION_RESIZE_WIDGET) {
			_state = STATE_RESIZE_WIDGET;
			doFullRepaint();
		}
		else if (action == ACTION_PULL_WIDGET) {
			WidgetPosition wp = _widgets[_widgetIndex];
			for (int i = _widgetIndex; i < _widgets.length-1; i ++)
				_widgets[i] = _widgets[i+1];
			_widgets[_widgets.length-1] = wp;
			_widgetIndex = _widgets.length-1;
			saveWidgetLayout();
			doFullRepaint();
		}
		else if (action == ACTION_PUSH_WIDGET) {
			WidgetPosition wp = _widgets[_widgetIndex];
			for (int i = _widgetIndex; i > 0; i --)
				_widgets[i] = _widgets[i-1];
			_widgets[0] = wp;
			_widgetIndex = 0;
			saveWidgetLayout();
			doFullRepaint();
		}
		else if (action == ACTION_DELETE_WIDGET) {
			WidgetPosition[] newWidgets = new WidgetPosition[_widgets.length-1];
			for (int i = 0; i < _widgetIndex; i ++)
				newWidgets[i] = _widgets[i];
			for (int i = _widgetIndex+1; i < _widgets.length; i ++)
				newWidgets[i-1] = _widgets[i];
			_widgets = newWidgets;
			_state = STATE_MAIN_MENU;
			_mainMenu.resetPosition(getWidth(),getHeight(),_useSoftkeys);
			saveWidgetLayout();
			doFullRepaint();
		}
		else if (action == ACTION_SELECT_WIDGET) {
			_state = STATE_CHOOSE_WIDGET;
			doFullRepaint();
		}
		else if (action == ACTION_WIDGET_SETTINGS) {
			WidgetPosition wp = _widgets[_widgetIndex];
			new WidgetSettingsForm(this,wp).show();
			doFullRepaint();
		}
		else if (action == Menu.ACTION_CLOSE_MENU) {
			_state = STATE_SHOW;
//			_mainMenu.resetPosition(getWidth(),getHeight(),_useSoftkeys);
			doFullRepaint();
		}
		else if (action == Menu.ACTION_SUBMENU_CHANGED) {
			doFullRepaint();
		}
		else {
			repaint();
		}
	}
	
	private void rebuildWidgetMenu() {
		WidgetPosition wp = _widgets[_widgetIndex];
		if (wp.widget.hasSettings())
			_widgetMenuItems[0] = new MenuItem("Customize",ACTION_WIDGET_SETTINGS);
		else
			_widgetMenuItems[0] = null;
		_widgetMenu = new Menu(wp.widget.getName(),_widgetMenuItems);
		_widgetMenu.resetPosition(getWidth(),getHeight(),_useSoftkeys);
	}
	
	private void handleKey(int key) {
		if (key == KEY_BACK && _state != STATE_SHOW) {
			int w = getWidth();
			int h = getHeight();
			_widgetMenu.resetPosition(w,h,_useSoftkeys);
			_mainMenu.resetPosition(w,h,_useSoftkeys);
			_state = STATE_SHOW;
			doFullRepaint();
		}
		else if (_state == STATE_SHOW) {
			if (key == KEY_FIRE) {
				int w = getWidth();
				int h = getHeight();
				_state = STATE_MAIN_MENU;
				_mainMenu.resetPosition(w,h,_useSoftkeys);
				repaint();
			}
		}
		else if (_state == STATE_MAIN_MENU) {
			handleMainMenuAction(_mainMenu.handleKey(key,_menuResult));
		}
		else if (_state == STATE_WIDGET_MENU) {
			handleWidgetMenuAction(_widgetMenu.handleKey(key,_menuResult));
		}
		else if (_state == STATE_CHOOSE_WIDGET) {
			if (key == KEY_UP || key == KEY_LEFT || key == KEY_DOWN || key == KEY_RIGHT) {
				WidgetPosition cur = _widgets[_widgetIndex];
				WidgetPosition next = null;
				int nextIdx = -1;
				for (int i = 0; i < _widgets.length; i ++) {
					if (i != _widgetIndex) {
						WidgetPosition w = _widgets[i];
						// if w is between current and next
						if (WidgetIsFollower(cur,w,_widgetIndex,i,key) &&
										(next == null || WidgetIsFollower(w,next,i,nextIdx,key))) {
							next = w;
							nextIdx = i; 
						}
					}
				}
				if (next == null) {	// select the "first" one
					next = _widgets[0];
					nextIdx = 0;
					for (int i = 1; i < _widgets.length; i ++) {
						if (WidgetIsFollower(_widgets[i],next,i,nextIdx,key)) {
							next = _widgets[i];
							nextIdx = i; 
						}
					}
				}
				_widgetIndex = nextIdx;
				repaint();
			}
			else if (key == KEY_FIRE) {
				_state = STATE_WIDGET_MENU;
				rebuildWidgetMenu();
				doFullRepaint();
			}
		}
		else if (_state == STATE_MOVE_WIDGET) {
			WidgetPosition wp = _widgets[_widgetIndex];
			if (key == KEY_UP) { if (wp.row > 0) {wp.row --; doFullRepaint(); }}
			else if (key == KEY_DOWN) { if (wp.row+wp.nrows < _nrows) { wp.row ++;doFullRepaint(); }}
			else if (key == KEY_LEFT) { if (wp.col > 0){wp.col --; doFullRepaint(); }}
			else if (key == KEY_RIGHT) { if (wp.col+wp.ncols < _ncols) { wp.col ++;doFullRepaint();}}
			else if (key == KEY_FIRE) {
				_state = STATE_WIDGET_MENU;
				saveWidgetLayout();
				doFullRepaint();
			}
		}
		else if (_state == STATE_RESIZE_WIDGET) {
			WidgetPosition wp = _widgets[_widgetIndex];
			if (key == KEY_UP) { if (wp.nrows > 1) {wp.nrows --; doFullRepaint(); }}
			else if (key == KEY_DOWN) { if (wp.row+wp.nrows < _nrows) { wp.nrows ++;doFullRepaint(); }}
			else if (key == KEY_LEFT) { if (wp.ncols > 1){wp.ncols --; doFullRepaint(); }}
			else if (key == KEY_RIGHT) { if (wp.col+wp.ncols < _ncols) { wp.ncols ++;doFullRepaint();}}
			else if (key == KEY_FIRE) {
				_state = STATE_WIDGET_MENU;
				saveWidgetLayout();
				doFullRepaint();
			}
		}
	}
	
	public void doFullRepaint() {
		_fullRepaintPending = true;
		repaint();
	}
	
	protected void paint(Graphics g) {
		int w = getWidth();
		int h = getHeight();
		
	
		if (_fullRepaintPending) {
			boolean design = _state == STATE_CHOOSE_WIDGET || _state == STATE_MOVE_WIDGET || _state == STATE_RESIZE_WIDGET;
			if (design)
				g.setColor(0x202060);
			else
				g.setColor(0);
			g.fillRect(0, 0, w, h);

//long start = System.currentTimeMillis();
			// paint all widgets
			WidgetInfo.update();
			for (int i = 0; i < _widgets.length; i ++) {
				WidgetPosition wp = _widgets[i];
				g.setClip(w*wp.col/_ncols,h*wp.row/_nrows,
							w*(wp.col+wp.ncols)/_ncols - w*wp.col/_ncols,
							h*(wp.row+wp.nrows)/_nrows - h*wp.row/_nrows);
				
				if (design) {
					g.setColor(0);
					g.fillRect(g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight());
				}
				
				try {
					wp.widget.paint(g,wp.settings);
				}
				catch (Exception e) {
					Log.error("Widget paint failed! widget="+wp.widget.getName(),e);
				}
			}
//Log.info("Widgets repaint: "+(System.currentTimeMillis()-start)+"ms");
			g.setClip(0, 0, getWidth(), getHeight());
			_fullRepaintPending = false;
		}
				
		if (_state == STATE_WIDGET_MENU || _state == STATE_CHOOSE_WIDGET || _state == STATE_MOVE_WIDGET || _state == STATE_RESIZE_WIDGET) {
			int x1,y1,x2,y2;
			WidgetPosition wp;
			// paint rectangle around all widgets
			for (int i = 0; i < _widgets.length; i ++) {
				if (i != _widgetIndex) {
					wp = _widgets[i];
					x1 = wp.col*w/_ncols;
					y1 = wp.row*h/_nrows;
					x2 = (wp.col+wp.ncols)*w/_ncols-1;
					y2 = (wp.row+wp.nrows)*h/_nrows-1;
					g.setColor(0x808080);
					g.drawRect(x1, y1, x2-x1, y2-y1);
					g.drawRect(x1-1, y1-1, x2-x1+2, y2-y1+2);
				}
			}

			// paint rectangle around selected widget
			wp = _widgets[_widgetIndex];
			x1 = wp.col*w/_ncols;
			y1 = wp.row*h/_nrows;
			x2 = (wp.col+wp.ncols)*w/_ncols-1;
			y2 = (wp.row+wp.nrows)*h/_nrows-1;
			g.setColor(0xff0000);
			g.drawRect(x1, y1, x2-x1, y2-y1);
			g.drawRect(x1-1, y1-1, x2-x1+2, y2-y1+2);
			
			// paint move/resize icon
			if (_imageMove != null) {
				if (_state == STATE_MOVE_WIDGET)
					g.drawImage(_imageMove, (x1+x2)/2, (y1+y2)/2, Graphics.HCENTER | Graphics.VCENTER);
				else if (_state == STATE_RESIZE_WIDGET)
					g.drawImage(_imageMove, x2, y2, Graphics.HCENTER | Graphics.VCENTER);
			}
		}

		// paint menu
		if (_state == STATE_MAIN_MENU) {
			_mainMenu.paint(g);
		}
		
		if (_state == STATE_WIDGET_MENU) {
			_widgetMenu.paint(g);
		}

		if (isSoftkeyVisibleState()) {
			g.drawImage(_selectedSoftkey == KEY_LEFT ? _imgSoftkeyLeftSel : _imgSoftkeyLeft,_softkeysPanelX+_softkeyW/2,_softkeysPanelY+_softkeyH*3/2,Graphics.VCENTER|Graphics.HCENTER);
			g.drawImage(_selectedSoftkey == KEY_UP ? _imgSoftkeyUpSel : _imgSoftkeyUp,_softkeysPanelX+_softkeyW*3/2,_softkeysPanelY+_softkeyH/2,Graphics.VCENTER|Graphics.HCENTER);
			g.drawImage(_selectedSoftkey == KEY_FIRE ? _imgSoftkeyFireSel : _imgSoftkeyFire,_softkeysPanelX+_softkeyW*3/2,_softkeysPanelY+_softkeyH*3/2,Graphics.VCENTER|Graphics.HCENTER);
			g.drawImage(_selectedSoftkey == KEY_DOWN ? _imgSoftkeyDownSel : _imgSoftkeyDown,_softkeysPanelX+_softkeyW*3/2,_softkeysPanelY+_softkeyH*5/2,Graphics.VCENTER|Graphics.HCENTER);
			g.drawImage(_selectedSoftkey == KEY_RIGHT ? _imgSoftkeyRightSel : _imgSoftkeyRight,_softkeysPanelX+_softkeyW*5/2,_softkeysPanelY+_softkeyH*3/2,Graphics.VCENTER|Graphics.HCENTER);
		}
		
		if (_state == STATE_SHOW && _touchHintDisplayTimeout > System.currentTimeMillis()) {
			int s = (w < h ? w : h)/3;
			g.setColor(0xFF0000);
			g.drawRect(w-s, 0, s-1, s-1);
			g.drawRect(w-s+1, 1, s-3, s-3);
			_touchHintMenuPainter.paint(g, "MENU", 0, w-s, 0, s, s, Graphics.HCENTER|Graphics.VCENTER);
		}
	}
	/*
	private static Menu createWidgetMenu() {
		Widget[] widgets = Widget.getAllWidgets();
		MenuItem[] items = new MenuItem[widgets.length+1];
		for (int i = 0; i < widgets.length; i ++)
			items[i] = new MenuItem(widgets[i].getName(),ACTION_ADD_WIDGET);
		items[widgets.length]= new MenuItem("<< back",Menu.ACTION_CLOSE_MENU); 
		return new Menu("Choose widget",items);
	}
	*/
	
		
	private static Menu _mainMenu = new Menu("Page Menu",new MenuItem[]{
				Util.hasBacklightSetting() ?
						new MenuItem("",ACTION_BACKLIGHT) {
							public String getCaption() {
								return InfoCenter.getInstance().keepsBacklight() ? "Backlight is ON" : "Backlight is OFF";
							}
						}
						: null,
				
				new MenuItem("Configure current page",Menu.NOACTION, new Menu("Configure Page",new MenuItem[]{
					new MenuItem("Add new widget",Menu.NOACTION, Widget.createMenu("configure page", "Add new widget", ACTION_ADD_WIDGET)),
					new MenuItem("Organize widgets",ACTION_ORGANIZE_WIDGETS),
					new MenuItem("Restore default layout",Menu.NOACTION,new Menu("Restore default layout?",new MenuItem[]{
							new MenuItem("OK",ACTION_RESTORE_LAYOUT, true),
							new MenuItem("Cancel",Menu.ACTION_CLOSE_MENU),
					})),
					new MenuItem("<< page menu",Menu.ACTION_CLOSE_MENU,false,MenuItem.STYLE_SMALL,null),
				})),
				new MenuItem("STOP tracking",Menu.NOACTION, new Menu("Really STOP tracking?",new MenuItem[]{
					new MenuItem("OK",ACTION_QUIT, true),
					new MenuItem("Cancel",Menu.ACTION_CLOSE_MENU),
				})),
				new MenuItem("<< close menu",Menu.ACTION_CLOSE_MENU,false,MenuItem.STYLE_SMALL,null),
			});
	
	private static Menu _widgetMenu;
	private static MenuItem[] _widgetMenuItems = new MenuItem[]{
			null,	// place holder for widget settings menuitem
			new MenuItem("Move Widget",ACTION_MOVE_WIDGET,null),
			new MenuItem("Resize Widget",ACTION_RESIZE_WIDGET,null),
			new MenuItem("Bring to Front",ACTION_PULL_WIDGET,null),
			new MenuItem("Send to Back",ACTION_PUSH_WIDGET,null),
			new MenuItem("Delete",Menu.NOACTION,new Menu("Delete selected widget?",new MenuItem[]{
					new MenuItem("OK",ACTION_DELETE_WIDGET, true),
					new MenuItem("Cancel",Menu.ACTION_CLOSE_MENU),
			})),
			new MenuItem("Select Another",ACTION_SELECT_WIDGET,null),
			new MenuItem("<< close menu",Menu.ACTION_CLOSE_MENU,false,MenuItem.STYLE_SMALL,null),
	};
			
	private WidgetPosition[] _widgets;
	private int _ncols;
	private int _nrows;
	private int _state;

	private boolean _fullRepaintPending;
	
	private Image _imageMove;
	
	private boolean _useSoftkeys;
	private Image _imgSoftkeyUp;
	private Image _imgSoftkeyRight;
	private Image _imgSoftkeyDown;
	private Image _imgSoftkeyLeft;
	private Image _imgSoftkeyFire;
	private Image _imgSoftkeyUpSel;
	private Image _imgSoftkeyRightSel;
	private Image _imgSoftkeyDownSel;
	private Image _imgSoftkeyLeftSel;
	private Image _imgSoftkeyFireSel;
	private int _softkeyW;
	private int _softkeyH;
	private int _softkeysPanelX;
	private int _softkeysPanelY;
	private int _selectedSoftkey;
	private MenuResult _menuResult;
	
	// STATE_CHOOSE_WIDGET
	private int _widgetIndex;
	
	// STATE_SHOW
	private long _touchHintDisplayTimeout;
	private TextPainter _touchHintMenuPainter;
}
