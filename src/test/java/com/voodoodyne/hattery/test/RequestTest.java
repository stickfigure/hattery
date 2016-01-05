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

package com.voodoodyne.hattery.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;

import java.util.Map;

import lombok.Data;

import org.testng.annotations.Test;

import com.voodoodyne.hattery.HttpRequest;
import com.voodoodyne.hattery.IORException;
import com.voodoodyne.hattery.Param;
import com.voodoodyne.hattery.test.util.DefaultBase;

/**
 * @author Jeff Schnitzer
 */
public class RequestTest extends DefaultBase {

	/** */
	@SuppressWarnings("unchecked")
	@Test
	public void defaultHeaders() throws Exception {
		final Map<String, String> headers = headersEndpoint().fetch().as(Map.class);
		assertThat(headers, hasKey("Host"));
		assertThat(headers, hasKey("User-Agent"));
		assertThat(headers, hasKey("Accept"));
	}

	/** */
	@SuppressWarnings("unchecked")
	@Test
	public void extraHeaders() throws Exception {
		final Map<String, String> headers = headersEndpoint().header("foo", "bar").header("baz", "bat").fetch().as(Map.class);
		assertThat(headers, hasEntry("foo", "bar"));
		assertThat(headers, hasEntry("baz", "bat"));
	}

	@Data
	private static class MD5Response {
		private String md5;
		private String original;
	}

	/** */
	@Test
	public void params() throws Exception {
		final MD5Response response = md5Endpoint().param("text", "example").fetch().as(MD5Response.class);
		assertThat(response.getOriginal(), equalTo("example"));
	}

	/** */
	@Test
	public void paramObjects() throws Exception {
		final MD5Response response = md5Endpoint().param(new Param("text", "example")).fetch().as(MD5Response.class);
		assertThat(response.getOriginal(), equalTo("example"));
	}

	/** */
	@SuppressWarnings("unchecked")
	@Test
	public void path() throws Exception {
		final Map<String, String> headers = echoEndpoint().path("/one").path("/two").fetch().as(Map.class);
		assertThat(headers, hasEntry("one", "two"));
	}

	/** */
	@Test
	public void addsSlashWhenAppropriate() {
		HttpRequest request = transport.request();
		assertThat(request.url("http://example.com").path("foo").getUrl(), equalTo("http://example.com/foo"));
		assertThat(request.url("http://example.com/").path("foo").getUrl(), equalTo("http://example.com/foo"));
		assertThat(request.url("http://example.com").path("/foo").getUrl(), equalTo("http://example.com/foo"));
	}

	/** */
	@Test
	public void removesSlashWhenAppropriate() {
		HttpRequest request = transport.request();
		assertThat(request.url("http://example.com/").path("/foo").getUrl(), equalTo("http://example.com/foo"));
	}
	
	/** */
	@Test
	public void basicAuth() {
		HttpRequest request = transport.request().basicAuth("test", "testing");
		assertThat(request.getHeaders(), hasEntry("Authorization", "Basic dGVzdDp0ZXN0aW5n"));
	}
	
	/** */
	@Test
	public void GETExistentData() {
		HttpRequest request = transport.request("http://echo.jsontest.com/foo/bar");
		final Foo foo = request.GET().fetch().as(Foo.class);
		assertThat(foo.getFoo(), equalTo("bar"));
	}
	
	/** */
	@Test(expectedExceptions={IORException.class})
	public void GETNonExistentData() {
		HttpRequest request = echoEndpoint();
		request.GET().fetch().as(Foo.class);
		assertThat(true, equalTo(false)); //Should fail if gets here
	}
	
	/** */
	@Test
	public void POSTExistentData() {
		HttpRequest request = transport.request("http://validate.jsontest.com?json={foo:bar}");
		final Validate validate = request.POST().fetch().as(Validate.class);
		assertThat(validate.isValidate(), equalTo(true));
	}
	
	/** */
	@Test(expectedExceptions={IORException.class})
	public void POSTNonExistentData() {
		HttpRequest request = transport.request("http://validate.jsontest.com");
		request.GET().fetch().as(Validate.class);
		assertThat(true, equalTo(false)); //Should fail if gets here
	}
}