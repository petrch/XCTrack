package org.xcontest.live;

import org.xcontest.live.zlib.JZlib;
import org.xcontest.live.zlib.ZStream;

public class ZLib {
		
	public static synchronized int zip(byte[] in, int inpos, int inlen, byte[] out, int outpos, int outlen) {
		ZStream z = new ZStream();
		z.next_in = in;
		z.next_in_index = inpos;
		z.avail_in = inlen;
		z.next_out = out;
		z.next_out_index = outpos;
		z.avail_out = outlen;
		
		z.deflateInit(JZlib.Z_DEFAULT_COMPRESSION);
		if (z.deflate(JZlib.Z_FINISH) != JZlib.Z_STREAM_END) {
			z.deflateEnd();      
			return -1;
		}
		z.deflateEnd(); 
		return (int)z.total_out;
	}
	
	public static int unzip(byte[] in, int inpos, int inlen, byte[] out, int outpos, int outlen) {
		ZStream z = new ZStream();
		z.next_in = in;
		z.next_in_index = inpos;
		z.avail_in = in.length;
		z.next_out = out;
		z.next_out_index = outpos;
		z.avail_out = outlen;

		z.inflateInit();
		if (z.inflate(JZlib.Z_FINISH) != JZlib.Z_STREAM_END) {
			z.inflateEnd();
			return -1;
		}		
		z.inflateEnd();      
		
		return (int)z.total_out;
	}
}
