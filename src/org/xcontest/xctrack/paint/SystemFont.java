package org.xcontest.xctrack.paint;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class SystemFont extends GeneralFont {
	public SystemFont(Font f) {
		_font = f;
	}
	
	public int getHeight() {
		return _font.getHeight();
	}
	
	public int substringWidth(String str, int idx, int len) {
		return _font.substringWidth(str,idx,len);
	}
	
	public void drawString(Graphics g, String str, int x, int y, int align) {
		g.setFont(_font);
		g.drawString(str, x, y, align);
	}
	
	public void drawSubstring(Graphics g, String str, int pos, int len, int x, int y, int align) {
		g.setFont(_font);
		g.drawSubstring(str, pos, len, x, y, align);
	}
	
	Font _font;
}
