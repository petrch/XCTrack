package org.xcontest.xctrack;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.xcontest.live.Earth;
import org.xcontest.xctrack.paint.GeneralFont;

public class BenchmarkScreen extends Canvas{

	private int _phase;
	private String[] _tests = new String[]{
		"Lines 150px",
		"Lines display width",
		"Fill rect 150x150",
		"Fill display",
		"Image 150x150",
		"System fonts",
		"PNG fonts",
		"Simple math",
		"Custom math",
	};
	
	private long[] _times = new long[_tests.length];
	
	public BenchmarkScreen() {
		setFullScreenMode(true);
	}
	
	protected void paint(Graphics g) {
		int w = getWidth();
		int h = getHeight();
		if (_phase == 0) {
			long t = System.currentTimeMillis();
			for (int i = 0; i < 10000; i ++)
				g.drawLine(0, i%150, 149, 149-i%150);
			_times[_phase++] = System.currentTimeMillis()-t;
		}
		else if (_phase == 1) {
			long t = System.currentTimeMillis();
			for (int i = 0; i < 10000; i ++)
				g.drawLine(0, i%h, w-1, h-1-i%h);
			_times[_phase++] = System.currentTimeMillis()-t;
		}
		else if (_phase == 2) {
			long t = System.currentTimeMillis();
			for (int i = 0; i < 1000; i ++)
				g.fillRect(0, 0, 150, 150);
			_times[_phase++] = System.currentTimeMillis()-t;
		}
		else if (_phase == 3) {
			long t = System.currentTimeMillis();
			for (int i = 0; i < 1000; i ++)
				g.fillRect(0, 0, w, h);
			_times[_phase++] = System.currentTimeMillis()-t;
		}
		else if (_phase == 4) {
			Image img = Image.createImage(150, 150);
			img.getGraphics().setColor(0x456789);
			img.getGraphics().setFont(Font.getDefaultFont());
			img.getGraphics().drawString("ahoj",0,0,Graphics.LEFT|Graphics.TOP);
			long t = System.currentTimeMillis();
			for (int i = 0; i < 1000; i ++)
				g.drawImage(img, 0, 0, Graphics.LEFT | Graphics.TOP);
			_times[_phase++] = System.currentTimeMillis()-t;
		}
		else if (_phase == 5) {
			long t = System.currentTimeMillis();
			GeneralFont f = GeneralFont.SystemFontsBold[0];
			for (int i = 0; i < 1000; i ++)
				f.drawString(g, "0123", 0, 0, Graphics.TOP | Graphics.LEFT);
			_times[_phase++] = System.currentTimeMillis()-t;
		}
		else if (_phase == 6) {
			long t = System.currentTimeMillis();
			GeneralFont f = GeneralFont.NumberFonts[0];
			for (int i = 0; i < 1000; i ++)
				f.drawString(g, "0123", 0, 0, Graphics.TOP | Graphics.LEFT);
			_times[_phase++] = System.currentTimeMillis()-t;
		}
		else if (_phase == 7) {
			long t = System.currentTimeMillis();
			double a,b,c;
			for (int i = 0; i < 10000; i ++) {
				a = Math.cos(Math.sin(Math.floor(1.1+i)));
				b = Math.sqrt(a);
				c = (a*b+a*b+a*b)%(1+b*b);
				_times[_phase] = (long)c;
			}
			_times[_phase++] = System.currentTimeMillis()-t;
		}
		else if (_phase == 8) {
			long t = System.currentTimeMillis();
			for (int i = 0; i < 10000; i ++) {
//				Earth.atan2(i*0.001, (i+1)*0.001);
				Earth.gg2lat(Earth.lat2gg(50));
			}
			_times[_phase++] = System.currentTimeMillis()-t;
		}
		
		Font f = Font.getDefaultFont();
		int y = 0;
		g.setColor(0);
		g.fillRect(0,0,w,h);
		g.setColor(0xffffff);
		g.setFont(f);
		for (int i = 0; i < _phase; i ++) {
			g.drawString(_tests[i]+": "+_times[i]+"ms", 0, y, Graphics.LEFT | Graphics.TOP);
			y += f.getHeight();
		}
		if (_phase < _tests.length) {
			g.drawString("<testing...>", 0, y, Graphics.LEFT | Graphics.TOP);
			new Thread() {
				public void run() {
					try {
						Thread.sleep(100);
					}
					catch(InterruptedException e){}
					repaint();
				}
			}.start();
		}
		else
			g.drawString("<done>", 0, y, Graphics.LEFT | Graphics.TOP);
	}
	
	protected void pointerReleased(int x, int y) {
		hide();
	}
	
	protected void keyPressed(int keyCode) {
		hide();
	}

	public void show() {
		App.showScreen(this);
	}
	
	public void hide() {
		App.hideScreen(this);
	}
}
