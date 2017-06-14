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

package com.voodoodyne.hattery.util;

/**
 * Builds a properly encoded query string
 */
public final class QueryBuilder {
	private final StringBuilder bld = new StringBuilder();

	/**
	 * @param value can be iterable, which will add all the various components
	 */
	public void add(final String key, final Object value) {
		if (value instanceof Iterable) {
			for (final Object val : ((Iterable<?>)value)) {
				this.doAdd(key, val.toString());
			}
		} else {
			doAdd(key, value.toString());
		}
	}

	private void doAdd(final String key, final String value) {
		if (bld.length() > 0)
			bld.append('&');

		bld.append(UrlUtils.urlEncode(key));
		bld.append('=');
		bld.append(UrlUtils.urlEncode(value));
	}

	@Override
	public String toString() {
		return bld.toString();
	}
}