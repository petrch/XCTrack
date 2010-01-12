package org.xcontest.live;

public interface Logger {
	void info(String message);
	void error(String message);
	void error(String message, Throwable e);
}
