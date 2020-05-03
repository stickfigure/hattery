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

import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;
import static com.voodoodyne.hattery.HttpRequest.HTTP;
import static com.voodoodyne.hattery.test.Snoop.SNOOP;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 */
class ResponseTest {

	/** */
	@Test
	void getHeaders() throws Exception {
		final HttpResponse response = SNOOP.fetch().succeed();

		assertThat(response.getHeaders()).containsEntry("Server", "Google Frontend");
	}

	/** */
	@Test
	void headersAreCaseInsensitive() throws Exception {
		final HttpResponse response = SNOOP.fetch().succeed();

		assertThat(response.getHeaders().get("server")).containsExactly("Google Frontend");
	}

	/** */
	@Test
	void getContentBytesForSuccess() throws Exception {
		final HttpResponse response = SNOOP.fetch().succeed();
		assertThat(new String(response.getContentBytes(), StandardCharsets.UTF_8)).startsWith("{\"body\":");
	}

	/** */
	@Test
	void getContentBytesForError() throws Exception {
		final HttpResponse response = HTTP.url("https://www.google.com/doesnotexist").fetch();
		assertThat(new String(response.getContentBytes(), StandardCharsets.UTF_8)).startsWith("<!DOCTYPE html>");
	}

	/** */
	@Test
	void getContentStreamForSuccess() throws Exception {
		final HttpResponse response = SNOOP.fetch().succeed();
		assertThat(new String(ByteStreams.toByteArray(response.getContentStream()), StandardCharsets.UTF_8)).startsWith("{\"body\":");
	}

	/** */
	@Test
	void asStream() throws Exception {
		final HttpResponse response = SNOOP.fetch().succeed();
		assertThat(new String(ByteStreams.toByteArray(response.asStream()), StandardCharsets.UTF_8)).startsWith("{\"body\":");
	}
	
	/** */
	@Test
	void asBytes() throws Exception {
		final HttpResponse response = SNOOP.fetch().succeed();
		assertThat(new String(response.asBytes(), StandardCharsets.UTF_8)).startsWith("{\"body\":");
	}
	
	/** */
	@Test
	void succeedCanBeUnsuccessful() throws Exception {
		try {
			HTTP.url("https://www.google.com/doesnotexist").fetch().succeed();
			fail();
		} catch (final HttpException e) {
			assertThat(e.getCode()).isEqualTo(404);
			assertThat(e.getMessage()).startsWith("404: <!DOCTYPE html>");
		}
	}
}