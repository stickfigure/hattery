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

	public TeeOutputStream(final OutputStream one, final OutputStream two) {
		this.one = one;
		this.two = two;
	}

	@Override
	public void write(final int b) throws IOException {
		one.write(b);
		two.write(b);
	}

	@Override
	public void write(final byte[] b) throws IOException {
		one.write(b);
		two.write(b);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
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
