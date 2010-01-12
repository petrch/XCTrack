package org.xcontest.live.json;

import java.io.Writer;

public class StringWriter extends Writer {
	
	public void write(char[] cbuf, int off, int len) {
		_buf.append(cbuf,off,len);
	}
	
	public void close() {
	}
	
	public void flush() {
	}
	
	private StringBuffer _buf;
}