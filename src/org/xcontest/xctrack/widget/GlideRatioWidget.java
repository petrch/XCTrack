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

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Graphics;

import org.xcontest.xctrack.paint.GeneralFont;
import org.xcontest.xctrack.util.Format;
import org.xcontest.xctrack.widget.settings.DataSourceSettings;
import org.xcontest.xctrack.widget.settings.PrecisionSettings;
import org.xcontest.xctrack.widget.settings.WidgetSettings;

public class GlideRatioWidget extends TextBoxWidget {

	private class Settings extends WidgetSettings {
		private class Data {
			private boolean show1;
			private boolean showVario;
		}
		
		private ChoiceGroup _choice;

		public Object load(String str) {
			Data d = new Data();
			if (str == null || str.length() != 2) {
				d.show1 = false;
				d.showVario = false;
			}
			else {
				d.show1 = str.charAt(0) == '1';
				d.showVario = str.charAt(1) == 'V';
			}
			return d;
		}

		public String save(Object obj) {
			Data d = (Data)obj;
			return "" + (d.show1 ? '1' : ' ') + (d.showVario ? 'V' : ' ');
		}

		public void createForm(Vector items, Object obj) {
			Data d = (Data)obj;
			_choice = new ChoiceGroup("Display Options",Choice.MULTIPLE);
			_choice.append("Show \"1:\" prefix", null);
			_choice.append("Show vario in lift", null);
			_choice.setSelectedIndex(0, d.show1);
			_choice.setSelectedIndex(1, d.showVario);
			items.addElement(_choice);
		}

		public void saveForm(Object obj) {
			Data d = (Data)obj;
			d.show1 = _choice.isSelected(0);
			d.showVario = _choice.isSelected(1);
		}
	}
	
	int _idxPrecisionVario,_idxPrecisionGlide,_idxSettings,_idxVarioSource,_idxSpeedSource;
	
	public GlideRatioWidget() {
		super("Glide", GeneralFont.NumberFonts, 2);
		_idxSettings = addSettings(new Settings());
		_idxPrecisionGlide = addSettings(new PrecisionSettings("Decimal precision",3,1));
		_idxPrecisionVario = addSettings(new PrecisionSettings("Decimal precision (vario)",3,1));
		_idxSpeedSource = addSettings(new DataSourceSettings(DataSourceSettings.SPEED));
		_idxVarioSource = addSettings(new DataSourceSettings(DataSourceSettings.VARIO));
	}

	protected void paint(Graphics g, Object[] settings) {
		PrecisionSettings.Data precVario = (PrecisionSettings.Data)settings[_idxPrecisionVario];
		PrecisionSettings.Data precGlide = (PrecisionSettings.Data)settings[_idxPrecisionGlide];
		DataSourceSettings.Data srcSpeed = (DataSourceSettings.Data)settings[_idxSpeedSource];
		DataSourceSettings.Data srcVario = (DataSourceSettings.Data)settings[_idxVarioSource];
		Settings.Data s = (Settings.Data)settings[_idxSettings];

		double vario = WidgetInfo.getVario(srcVario);
		double speed = WidgetInfo.getSpeed(srcSpeed);
		
		String text;
		
		
		if (!Double.isNaN(vario) && !Double.isNaN(speed)) {
			if (vario < 0 && speed/vario < 1000) {
				text = s.show1 ? "1:" : "";
				text += Format.number(-speed/vario,precGlide.precision);
				super.paint(g, settings, text, 0);
			}
			else {
				if (s.showVario) {
					double v = vario<0 ? 0 : vario;
					super.paint(g, settings, "+"+Format.number(v,precVario.precision), 0);
				}
				else {
					super.paint(g, settings, (s.show1 ? "1:" : "")+Format.dashes(2,precGlide.precision), 0);
				}
			}
		}
		else
			super.paint(g, settings, (s.show1 ? "1:" : "")+Format.dashes(2,precGlide.precision), 0);
	}

	public String getName() {
		return "GlideRatio";
	}
}
