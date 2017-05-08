/*
 * Copyright (c) 2010 Jeff Schnitzer.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.voodoodyne.hattery;

import lombok.Data;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * A single key/value pair
 * 
 * @author Jeff Schnitzer
 */
@Data
public class Param {
	/** Make a new array with the extra parameters tacked on */
	public static Param[] concat(final Param[] base, final Param... params) {
		final Param[] result = Arrays.copyOf(base, base.length + params.length);
		System.arraycopy(params, 0, result, result.length, params.length);
		return result;
	}

	/** */
	private final String name;

	/** */
	private final Object value;

	/**
	 */
	public Param(final String name, final Object value) {
		this.name = name;
		this.value = value;
	}

	/**
	 */
	public Param(final String name, final List<Object> value) {
		this.name = name;
		this.value = value;
	}

	/**
	 */
	public Param(final String name, final InputStream stream, final String contentType, final String filename) {
		this.name = name;
		this.value = new BinaryAttachment(stream, contentType, filename);
	}

	/**
	 * @return a version useful for debugging.
	 */
	@Override
	public String toString() {
		return "[" + name + "=" + value + "]";
	}
}