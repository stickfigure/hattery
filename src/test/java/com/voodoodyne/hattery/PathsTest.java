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

import com.voodoodyne.hattery.test.Snoop;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.voodoodyne.hattery.HttpRequest.HTTP;
import static com.voodoodyne.hattery.test.Snoop.SNOOP;

/**
 * @author Jeff Schnitzer
 */
class PathsTest {

	/** */
	@SuppressWarnings("unchecked")
	@Test
	void pathsAreSubmitted() throws Exception {
		final Snoop snoop = SNOOP
				.path("/one")
				.path("/two")
				.fetch().as(Snoop.class);

		assertThat(snoop.getPath()).isEqualTo("/one/two");
	}

	/** */
	@Test
	void addsSlashToPathWhenAppropriate() {
		final HttpRequest request = HTTP;
		assertThat(request.url("http://example.com").path("foo").getUrl()).isEqualTo("http://example.com/foo");
		assertThat(request.url("http://example.com/").path("foo").getUrl()).isEqualTo("http://example.com/foo");
		assertThat(request.url("http://example.com").path("/foo").getUrl()).isEqualTo("http://example.com/foo");
	}

	/** */
	@Test
	void removesSlashFromPathWhenAppropriate() {
		final HttpRequest request = HTTP;
		assertThat(request.url("http://example.com/").path("/foo").getUrl()).isEqualTo("http://example.com/foo");
	}
}