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
import static com.voodoodyne.hattery.test.Snoop.SNOOP;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jeff Schnitzer
 */
class HeadersTest {

	/** */
	@Test
	void extraHeadersAreSubmitted() throws Exception {
		final Snoop snoop = SNOOP
				.header("Foo", "bar")
				.header("Baz", "bat")
				.fetch().as(Snoop.class);

		assertThat(snoop.getHeaders()).containsEntry("Foo", "bar");
		assertThat(snoop.getHeaders()).containsEntry("Baz", "bat");
	}

	/** */
	@Test
	void headersCanBeOverridden() throws Exception {
		final Snoop snoop = SNOOP
				.header("Foo", "zzz")
				.header("Foo", "yyy")
				.fetch().as(Snoop.class);

		assertThat(snoop.getHeaders()).containsEntry("Foo", "yyy");
	}

	/** */
	@Test
	void contentTypeCanBeSpecified() throws Exception {
		final Snoop snoop = SNOOP
				.header("Foo", "bar")
				.contentType("not/real")
				.fetch().as(Snoop.class);

		assertThat(snoop.getHeaders()).containsEntry("Foo", "bar");
		assertThat(snoop.getContentType()).isEqualTo("not/real");
	}

	/** */
	@Test
	void basicAuthIsSubmitted() {
		final Snoop snoop = SNOOP
				.basicAuth("test", "testing")
				.fetch().as(Snoop.class);

		assertThat(snoop.getHeaders()).containsEntry("Authorization", "Basic dGVzdDp0ZXN0aW5n");
	}
}