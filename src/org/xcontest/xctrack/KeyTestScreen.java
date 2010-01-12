package org.xcontest.xctrack;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class KeyTestScreen extends Canvas{

	int _keyCode = 0;
	String _keyName = "";
	int _x=-1,_y=-1;
	int _dx=-1,_dy=-1;
	boolean _dragging = false;
	
	int _exitX,_exitY,_exitW,_exitH;
	
	public KeyTestScreen() {
		setFullScreenMode(true);
	}
	
	protected void keyPressed(int keyCode) {
		_keyCode = keyCode;
		_keyName = getKeyName(keyCode);
		if (getGameAction(keyCode) == LEFT)
			hide();
		repaint();
	}
	
	protected void paint(Graphics g) {
		int w = getWidth();
		int h = getHeight();
		Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
		int fonth = f.getHeight();
		
		_exitW = f.stringWidth("EXIT");
		_exitH = f.getHeight();
		_exitX = (w-_exitW)/2;
		_exitY = (h-_exitH)/2;
		
		g.setColor(0);
		g.fillRect(0, 0, w, h);

		g.setFont(f);
		g.setColor(0xFFFFFF);
		g.drawString("Press LEFT arrow or click the EXIT to cancel",0,0,Graphics.TOP|Graphics.LEFT);
		g.drawString("KeyCode: "+_keyCode, 0, fonth, Graphics.TOP|Graphics.LEFT);
		g.drawString("KeyName: "+_keyName, 0, 2*fonth, Graphics.TOP|Graphics.LEFT);
		
		g.setColor(0xFFC0C0);
		g.drawString("EXIT", _exitX, _exitY, Graphics.TOP | Graphics.LEFT);
		
		g.setColor(_dragging ? 0xFFFF00 : 0xFFFFFF);
		g.drawLine(_x, _y, _dx, _dy);
	}
	
	protected void pointerPressed(int x, int y) {
		if (x >= _exitX && x <= _exitX+_exitW && y >= _exitY && y <= _exitY + _exitH)
			hide();

		_x = x;
		_y = y;
		_dx = x;
		_dy = y;
		_dragging = true;
		repaint();
	}
	
	protected void pointerDragged(int x, int y) {
		_dx = x;
		_dy = y;
		repaint();
	}
	
	protected void pointerReleased(int x, int y) {
		_dragging = false;
		repaint();
	}
	
	public void show() {
		App.showScreen(this);
	}
	
	public void hide() {
		App.hideScreen(this);
	}
}
