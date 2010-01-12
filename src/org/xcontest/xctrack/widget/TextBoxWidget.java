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

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import org.xcontest.xctrack.paint.GeneralFont;
import org.xcontest.xctrack.paint.TextPainter;
import org.xcontest.xctrack.widget.settings.TextBoxSettings;


public abstract class TextBoxWidget extends Widget {
	private Font _font;
	private TextPainter _painter;
	private int _alignment;
	private int _idxSettings;
	
	public TextBoxWidget(String title, GeneralFont[] fonts, int nvariants) {
		_painter = new TextPainter(fonts,nvariants);
		_font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
		_alignment = Graphics.HCENTER | Graphics.VCENTER;
		
		_idxSettings = addSettings(new TextBoxSettings(title)); 
	}

	protected int getDefaultWidth() { return 136; }
	protected int getDefaultHeight() { return 80; }
	
	protected final int getWidth(Graphics g, Object[] settings) {
		return g.getClipWidth();
	}
	
	protected final int getHeight(Graphics g, Object[] settings) {
		TextBoxSettings.Data s = (TextBoxSettings.Data)settings[_idxSettings];
		if (s.showCaption)
			return g.getClipHeight()-_font.getHeight();
		else
			return g.getClipHeight();
	}
	
	protected final void paint(Graphics g, Object[] settings, String text, int variant) {
		TextBoxSettings.Data s = (TextBoxSettings.Data)settings[_idxSettings];
		int x = g.getClipX();
		int y = g.getClipY();
		int w = g.getClipWidth();
		int h = g.getClipHeight();
		int fonth = _font.getHeight();
		
		if (s.showCaption) {
			g.setColor(COLOR_STATIC_TEXT);
			g.setFont(_font);
			g.drawString(s.caption, x + w/2, y, Graphics.TOP|Graphics.HCENTER);

			g.setColor(0xFFFFFF);
			_painter.paint(g, text, variant, x, y+fonth, w, h-fonth, _alignment);
		}
		else {
			g.setColor(0xFFFFFF);
			_painter.paint(g, text, variant, x, y, w, h, _alignment);
		}
	}

	// ORed values of Graphics.xxx
	protected void setAlignment(int al) {
		_alignment = al;
	}
}
