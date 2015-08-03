package com.voodoodyne.hattery.util;

import lombok.Getter;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Sends output to two outputstreams
 */
public class TeeOutputStream extends OutputStream {
	@Getter
	private final OutputStream one;
	@Getter
	private final OutputStream two;

	public TeeOutputStream(OutputStream one, OutputStream two) {
		this.one = one;
		this.two = two;
	}

	@Override
	public void write(int b) throws IOException {
		one.write(b);
		two.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		one.write(b);
		two.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		one.write(b, off, len);
		two.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		one.flush();
		two.flush();
	}

	@Override
	public void close() throws IOException {
		one.close();
		two.close();
	}
}
