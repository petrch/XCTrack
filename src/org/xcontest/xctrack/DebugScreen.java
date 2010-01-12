package org.xcontest.xctrack;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.xcontest.xctrack.config.Config;
import org.xcontest.xctrack.fs.OpenFileDialog;
import org.xcontest.xctrack.util.Log;

public class DebugScreen implements CommandListener {
	private List _list;
	private int _idxLog;
	private int _idxKeyTest;
	private int _idxClearConfig;
	private int _idxFileSystem;
	private int _idxSpeed;
	
	public DebugScreen() {
		_list = new List("XC Track",List.IMPLICIT);
		
		_idxLog = _list.append("View Log", null);
		_idxClearConfig = _list.append("Clear Configuration", null);
		_idxKeyTest = _list.append("Key Test",null);
		_idxFileSystem = _list.append("Filesystem Test",null);
		_idxSpeed = _list.append("JVM Benchmark",null);
		
//		_list.addCommand(new Command("Select", Command.SCREEN, 1));
		_list.addCommand(new Command("Back", Command.BACK, 1));
		_list.setCommandListener(this);
	}
	
	public void show() {
		App.showScreen(_list);
	}
	
	private void hide() {
		App.hideScreen(_list);
	}

	public void commandAction(Command cmd, Displayable disp) {
		int cmdType = cmd.getCommandType();
		if (cmdType == Command.BACK || cmdType == Command.CANCEL || cmdType == Command.EXIT || cmdType == Command.STOP) {
			hide();
		}
		if (cmdType == Command.OK || cmdType == Command.SCREEN) {
			int idx = _list.getSelectedIndex();
			if (idx == _idxLog) {
				try {
					new LogScreen().show();
				}
				catch (Error e) {
					Util.showError("??", e);
				}
			}
			else if (idx == _idxKeyTest) {
				new KeyTestScreen().show();
			}
			else if (idx == _idxClearConfig) {
				Config.clear();
				Log.clear();
				Util.showInfo("Restored default settings");
			}
			else if (idx == _idxFileSystem) {
				new OpenFileDialog().show();
			}
			else if (idx == _idxSpeed) {
				new BenchmarkScreen().show();
			}
		}
	}
}
