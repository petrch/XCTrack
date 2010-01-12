package org.xcontest.xctrack.config;

import org.xcontest.xctrack.config.Config;
import org.xcontest.xctrack.util.Log;
import org.xcontest.xctrack.widget.Widget;

public class WidgetPosition {
	public int col,row,ncols,nrows;
	public Widget widget;
	public Object[] settings;
	
	public String save() {
		String str = settings == null ? null : widget.saveSettings(settings);
		if (str != null)
			return Config.packStrings(new String[]{widget.getName(),""+col,""+row,""+ncols,""+nrows,str});
		else
			return Config.packStrings(new String[]{widget.getName(),""+col,""+row,""+ncols,""+nrows});
	}
	
	public static WidgetPosition load(String str) {
		String[] arr = Config.unpackStrings(str);
		if (arr == null || (arr.length != 5 && arr.length != 6)) {
			Log.error("WidgetPosition.load() - unpackStrings failed");
			return null;
		}
		
		WidgetPosition wp = new WidgetPosition();
		wp.widget = Widget.getWidgetByName(arr[0]);
		if (wp.widget == null) {
			Log.error("WidgetPosition.load() - cannot find widget "+arr[0]);
			return null;
		}
		wp.col = Integer.parseInt(arr[1]);
		wp.row = Integer.parseInt(arr[2]);
		wp.ncols = Integer.parseInt(arr[3]);
		wp.nrows = Integer.parseInt(arr[4]);
		
		if (wp.ncols == 0 || wp.nrows == 0) {
			Log.error("WidgetPosition.load() - invalid widget size");
			return null;
		}
		
		wp.settings = wp.widget.loadSettings(arr.length == 6 ? arr[5] : null);
		
		return wp;
	}
}
