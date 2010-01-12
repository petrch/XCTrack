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

import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Graphics;

import org.xcontest.xctrack.paint.GeneralFont;
import org.xcontest.xctrack.paint.TextPainter;
import org.xcontest.xctrack.widget.settings.DataSourceSettings;
import org.xcontest.xctrack.widget.settings.WidgetSettings;

public class VarioBarWidget extends Widget {

	private class Settings extends WidgetSettings {
		private class Data {
			int displayMode;
		}
		
//		private static final int NODECORATION = 0;
		private static final int ZERO_LINE = 1;
		private static final int LINES = 2;
		private static final int NUMBERS = 3;
		
		private ChoiceGroup _choiceShowNumbers;

		public Object load(String str) {
			Data d = new Data();
			if (str == null || str.length() != 1)
				d.displayMode = NUMBERS;
			else
				d.displayMode = str.charAt(0)-'0';
			return d;
		}

		public String save(Object obj) {
			Data d = (Data)obj;
			return ""+d.displayMode;
		}

		public void createForm(Vector items, Object obj) {
			_choiceShowNumbers = new ChoiceGroup("Display",ChoiceGroup.EXCLUSIVE);
			_choiceShowNumbers.append("No decoration", null);
			_choiceShowNumbers.append("Zero line", null);
			_choiceShowNumbers.append("All lines", null);
			_choiceShowNumbers.append("All lines and numbers", null);

			Data d = (Data)obj;
			_choiceShowNumbers.setSelectedIndex(d.displayMode, true);

			items.addElement(_choiceShowNumbers);
		}

		public void saveForm(Object obj) {
			Data d = (Data)obj;
			d.displayMode = _choiceShowNumbers.getSelectedIndex();
		}
		
	}
		
	private TextPainter _textPainter;
	private int _idxSettings;
	private int _idxVarioSource;
	
	
	public VarioBarWidget() {
		_textPainter = new TextPainter(GeneralFont.SystemFontsBold,1);
		_idxSettings = addSettings(new Settings());
		_idxVarioSource = addSettings(new DataSourceSettings(DataSourceSettings.VARIO));
	}
	
	public String getName() {
		return "VarioBar";
	}

	protected int getDefaultHeight() {
		return 2000;
	}

	protected int getDefaultWidth() {
		return 24;
	}
	
	private static final int getLiftColor(double lift) {
		lift /= 3;
		if (lift > 2)
			return 0xFF0000;
		else if (lift > 1)
			return 0xFFFF00 - ((int)(0xFF*(lift-1)))*0x100;
		else
			return 0x00FF00 + ((int)(0xFF*lift))*0x10000;
	}
	
	private static final int getSinkColor(double sink) {
		return 0x0000ff;
		/*
		sink /= 6;
		if (sink > 1)
			return 0x0000FF;
		else
			return 0x000060+(int)((0xFF-0x60)*sink);
		*/
	}
	
	protected void paint(Graphics g, Object[] objSettings) {
		Settings.Data s = (Settings.Data)objSettings[_idxSettings];
		DataSourceSettings.Data varioSource = (DataSourceSettings.Data)objSettings[_idxVarioSource];
		double vario = WidgetInfo.getVario(varioSource);
		int x = g.getClipX();
		int y = g.getClipY();
		int w = g.getClipWidth();
		int h = g.getClipHeight();
		int sinkplus = 0;
		int liftplus = 0;
		
		g.setColor(0);
		g.fillRect(x,y,w,h);

		if (!Double.isNaN(vario)) {
			int bartop;
			int barbottom;
			if (vario < -4) sinkplus = 4;
			if (vario > 4) liftplus = 4;
			if (vario >= 0) {
				g.setColor(getLiftColor(vario));
				if (vario >= 8) {
					g.fillTriangle(x+w/2, y, x, y+w/2, x+w, y+w/2);
				}
				else {
					if (vario < 4) {
						bartop = h/2-(int)((vario/8)*h);
						barbottom = h/2;
					}
					else {
						bartop = 0;
						barbottom = h/2-(int)(((vario-4)/8)*h);
					}
					g.fillRect(x, y+bartop, w, 1+barbottom-bartop);
				}
			}
			else {
				double sink = -vario;
				g.setColor(getSinkColor(sink));
				if (sink >= 8) {
					g.fillTriangle(x+w/2, y+h, x, y+h-w/2, x+w, y+h-w/2);
				}
				else {
					if (sink < 4) {
						bartop = h/2;
						barbottom = h/2+(int)((sink/8)*h);
					}
					else {
						bartop = h/2+(int)(((sink-4)/8)*h);
						barbottom = h-1;
					}
					g.fillRect(x, y+bartop, w, 1+barbottom-bartop);
				}
			}
		}	// hasVerticalSpeed		
		
		if (s.displayMode >= Settings.ZERO_LINE) {
			g.setColor(0xFFFFFF);
			g.drawLine(x, y+h/2, x+w, y+h/2);
			g.drawLine(x, y+h/2+1, x+w+1, y+h/2+1);
		
			if (s.displayMode >= Settings.LINES) {
				g.setColor(COLOR_STATIC_TEXT);
				for (int i = 1; i <= 3; i ++) {
					int cury = y+(4-i)*h/8;
					g.drawLine(x, cury, x+w, cury);
					if (s.displayMode >= Settings.NUMBERS)
						_textPainter.paint(g, "+"+(i+liftplus), 0, x, cury+1, w, h/8, Graphics.TOP | Graphics.HCENTER);
					cury = y+(4+i)*h/8;
					g.drawLine(x, cury, x+w, cury);
					if (s.displayMode >= Settings.NUMBERS)
						_textPainter.paint(g, "-"+(i+sinkplus), 0, x, cury-1-h/8, w, h/8, Graphics.BOTTOM | Graphics.HCENTER);
				}
				if (s.displayMode >= Settings.NUMBERS) {
					_textPainter.paint(g, "+"+(4+liftplus), 0, x, y+1, w, h/8, Graphics.TOP | Graphics.HCENTER);
					_textPainter.paint(g, "-"+(4+sinkplus), 0, x, y+h-1-h/8, w, h/8, Graphics.BOTTOM | Graphics.HCENTER);
				}
			}
		}
	}
	
}
