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

import com.voodoodyne.hattery.test.Requests;
import com.voodoodyne.hattery.test.ValidateResponse;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static com.voodoodyne.hattery.HttpRequest.HTTP;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jeff Schnitzer
 */
class HttpRequestTest {

	/** */
	@Test
	@SuppressWarnings("unchecked")
	void extraHeadersAreSubmitted() throws Exception {
		final Map<String, String> headers = Requests.HEADERS_ENDPOINT
				.header("foo", "bar")
				.header("baz", "bat")
				.fetch().as(Map.class);

		assertThat(headers).containsEntry("foo", "bar");
		assertThat(headers).containsEntry("baz", "bat");
	}

	/** */
	@Test
	@SuppressWarnings("unchecked")
	void headersCanBeOverridden() throws Exception {
		final Map<String, String> headers = Requests.HEADERS_ENDPOINT
				.header("foo", "zzz").header("foo", "yyy")
				.fetch().as(Map.class);
		assertThat(headers).containsEntry("foo", "yyy");
	}

	/** */
	@Test
	@SuppressWarnings("unchecked")
	void contentTypeCanBeSpecified() throws Exception {
		final Map<String, String> headers = Requests.HEADERS_ENDPOINT
				.header("foo", "bar")
				.contentType("not/real")
				.fetch().as(Map.class);

		assertThat(headers).containsEntry("foo", "bar");
		assertThat(headers).containsEntry("Content-Type", "not/real");
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
		assertThat(response.getOriginal()).isEqualTo("example");
	}

	/** */
	@Test
	void paramObjectsAreSubmitted() throws Exception {
		final MD5Response response = Requests.MD5_ENDPOINT.param(new Param("text", "example")).fetch().as(MD5Response.class);
		assertThat(response.getOriginal()).isEqualTo("example");
	}

	/** */
	@Test
	void paramsCanBeOverridden() throws Exception {
		final MD5Response response = Requests.MD5_ENDPOINT
				.param("text", "notexample")
				.param("text", "example")
				.fetch().as(MD5Response.class);
		assertThat(response.getOriginal()).isEqualTo("example");
	}

	/** */
	@Test
	void paramsCanBeRemoved() throws Exception {
		final String url = Requests.MD5_ENDPOINT
				.param("text", "notexample")
				.param("text", null)
				.toUrlString();
		assertThat(url).isEqualTo("http://md5.jsontest.com");
	}

	/** */
	@SuppressWarnings("unchecked")
	@Test
	void pathsAreSubmitted() throws Exception {
		final Map<String, String> headers = Requests.ECHO_ENDPOINT.path("/one").path("/two").fetch().as(Map.class);
		assertThat(headers).containsEntry("one", "two");
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
	
	/** */
	@Test
	void basicAuthIsSubmitted() {
		final HttpRequest request = HTTP.basicAuth("test", "testing");
		assertThat(request.getHeaders()).containsEntry("Authorization", "Basic dGVzdDp0ZXN0aW5n");
	}
	
	/** */
	@Test
	void jacksonExceptionsProduceIORException() {
		assertThrows(IORuntimeException.class, () -> {
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

		assertThat(validate.isValidate()).isTrue();
	}

	/** */
	@Test
	void queryParamsAreForcedEvenWhenPostingFormData() {
		final HttpRequest request = HTTP.url("http://example.com").POST().param("foo", "bar").queryParam("foo2", "bar2");

		assertThat(request.toUrlString()).isEqualTo("http://example.com?foo2=bar2");
	}

	/** */
	@Test
	void multipleParamObjectsCanBePassed() {
		final HttpRequest request = HTTP.url("http://example.com").param(new Param("foo", "bar"), new Param("foo2", "bar2"));

		assertThat(request.toUrlString()).isEqualTo("http://example.com?foo=bar&foo2=bar2");
	}

	/** */
	@Test
	void listParamsBecomeMultipleEntries() {
		final List<String> list = Arrays.asList("foo", "bar");
		final HttpRequest request = HTTP.url("http://example.com").param("baz", list);

		assertThat(request.toUrlString()).isEqualTo("http://example.com?baz=foo&baz=bar");
	}
}