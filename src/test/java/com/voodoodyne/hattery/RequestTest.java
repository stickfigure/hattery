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

import com.voodoodyne.hattery.test.ValidateResponse;
import com.voodoodyne.hattery.test.Requests;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jeff Schnitzer
 */
class RequestTest {

	/** */
	@Test
	@SuppressWarnings("unchecked")
	void extraHeadersAreSubmitted() throws Exception {
		final Map<String, String> headers = Requests.HEADERS_ENDPOINT
				.header("foo", "bar")
				.header("baz", "bat")
				.fetch().as(Map.class);

		assertThat(headers, hasEntry("foo", "bar"));
		assertThat(headers, hasEntry("baz", "bat"));
	}

	/** */
	@Test
	@SuppressWarnings("unchecked")
	void headersCanBeOverridden() throws Exception {
		final Map<String, String> headers = Requests.HEADERS_ENDPOINT
				.header("foo", "zzz").header("foo", "yyy")
				.fetch().as(Map.class);
		assertThat(headers, hasEntry("foo", "yyy"));
	}

	@Data
	private static class MD5Response {
		private final String md5;
		private final String original;
	}

	/** */
	@Test
	void paramStringsAreSubmitted() throws Exception {
		final MD5Response response = Requests.MD5_ENDPOINT.param("text", "example").fetch().as(MD5Response.class);
		assertThat(response.getOriginal(), equalTo("example"));
	}

	/** */
	@Test
	void paramObjectsAreSubmitted() throws Exception {
		final MD5Response response = Requests.MD5_ENDPOINT.param(new Param("text", "example")).fetch().as(MD5Response.class);
		assertThat(response.getOriginal(), equalTo("example"));
	}

	/** */
	@Test
	void paramsCanBeOverridden() throws Exception {
		final MD5Response response = Requests.MD5_ENDPOINT
				.param("text", "notexample")
				.param("text", "example")
				.fetch().as(MD5Response.class);
		assertThat(response.getOriginal(), equalTo("example"));
	}

	/** */
	@SuppressWarnings("unchecked")
	@Test
	void pathsAreSubmitted() throws Exception {
		final Map<String, String> headers = Requests.ECHO_ENDPOINT.path("/one").path("/two").fetch().as(Map.class);
		assertThat(headers, hasEntry("one", "two"));
	}

	/** */
	@Test
	void addsSlashToPathWhenAppropriate() {
		HttpRequest request = new DefaultTransport().request();
		assertThat(request.url("http://example.com").path("foo").getUrl(), equalTo("http://example.com/foo"));
		assertThat(request.url("http://example.com/").path("foo").getUrl(), equalTo("http://example.com/foo"));
		assertThat(request.url("http://example.com").path("/foo").getUrl(), equalTo("http://example.com/foo"));
	}

	/** */
	@Test
	void removesSlashFromPathWhenAppropriate() {
		HttpRequest request = new DefaultTransport().request();
		assertThat(request.url("http://example.com/").path("/foo").getUrl(), equalTo("http://example.com/foo"));
	}
	
	/** */
	@Test
	void basicAuthIsSubmitted() {
		HttpRequest request = new DefaultTransport().request().basicAuth("test", "testing");
		assertThat(request.getHeaders(), hasEntry("Authorization", "Basic dGVzdDp0ZXN0aW5n"));
	}
	
	/** */
	@Test
	void jacksonExceptionsProduceIORException() {
		assertThrows(IORException.class, () -> {
			Requests.ECHO_ENDPOINT.GET().fetch().as(MD5Response.class);	// wrong response
		});
	}
	
	/** This service doesn't actually take post bodies. Sigh. */
	//@Test
	void bodyIsSubmittedAsJSON() {
		final ValidateResponse validate = Requests.VALIDATE_ENDPOINT
				.POST()
				.body(new MD5Response("foo", "bar"))
				.fetch().as(ValidateResponse.class);

		assertThat(validate.isValidate(), equalTo(true));
	}
}