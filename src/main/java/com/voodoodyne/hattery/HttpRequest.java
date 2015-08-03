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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;
import com.voodoodyne.hattery.util.MultipartWriter;
import com.voodoodyne.hattery.util.TeeOutputStream;
import com.voodoodyne.hattery.util.UrlUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>Immutable definition of a request; methods return new immutable object with the data changed.</p>
 * 
 * @author Jeff Schnitzer
 */
@Data
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
@ToString(exclude = "mapper")	// string version is useless and noisy
public class HttpRequest {

	private final Transport transport;

	/** */
	private final String method;

	/** URL so far; can be extended with path() */
	private final String url;

	/** value will be either String, Collection<String>, or BinaryAttachment */
	private final Map<String, Object> params;

	/** */
	private final Map<String, String> headers;

	/** 0 for no explicit timeout (aka default) */
	private final int timeout;

	/** 0 for no retries */
	private final int retries;

	/** */
	private final ObjectMapper mapper;

	/**
	 * Default values
	 */
	HttpRequest(Transport transport) {
		this.transport = transport;
		this.method = HttpMethod.GET.name();
		this.url = null;
		this.params = Collections.emptyMap();
		this.headers = Collections.emptyMap();
		this.timeout = 0;
		this.retries = 0;
		this.mapper = new ObjectMapper();
	}

	/** */
	public HttpRequest method(String method) {
		Preconditions.checkNotNull(method);
		return new HttpRequest(transport, method, url, params, headers, timeout, retries, mapper);
	}

	/** */
	public HttpRequest method(HttpMethod method) {
		return method(method.name());
	}

	/** Shortcut for method(HttpMethod.GET) */
	public HttpRequest GET() {
		return method(HttpMethod.GET);
	}

	/** Shortcut for method(HttpMethod.POST) */
	public HttpRequest POST() {
		return method(HttpMethod.POST);
	}

	/** Shortcut for method(HttpMethod.PUT) */
	public HttpRequest PUT() {
		return method(HttpMethod.PUT);
	}

	/** Shortcut for method(HttpMethod.DELETE) */
	public HttpRequest DELETE() {
		return method(HttpMethod.DELETE);
	}

	/**
	 * Replaces the existing url wholesale
	 */
	public HttpRequest url(String url) {
		Preconditions.checkNotNull(url);
		return new HttpRequest(transport, method, url, params, headers, timeout, retries, mapper);
	}

	/**
	 * Appends path to the existing url. If no url is not set, this becomes the url.
	 */
	public HttpRequest path(String path) {
		Preconditions.checkNotNull(path);
		String url2 = (url == null) ? path : (url + path);
		return url(url2);
	}

	/**
	 * Set/override the parameter with a single value
	 * @return the updated, immutable request
	 */
	public HttpRequest param(String name, String value) {
		return paramAnything(name, value);
	}

	/**
	 * Set/override the parameter with a list of values
	 * @return the updated, immutable request
	 */
	public HttpRequest param(String name, List<String> value) {
		return paramAnything(name, ImmutableList.copyOf(value));
	}

	/**
	 * Set/override the parameters
	 * @return the updated, immutable request
	 */
	public HttpRequest param(Param... params) {
		HttpRequest here = this;
		for (Param param: params)
			here = paramAnything(param.getName(), param.getValue());

		return here;
	}

	/**
	 * Set/override the parameter with a binary attachment.
	 */
	public HttpRequest param(String name, InputStream stream, String contentType, String filename) {
		final BinaryAttachment attachment = new BinaryAttachment(stream, contentType, filename);
		return POST().paramAnything(name, attachment);
	}

	/** Private implementation lets us add anything, but don't expose that to the world */
	private HttpRequest paramAnything(String name, Object value) {
		final ImmutableMap<String, Object> params = new ImmutableMap.Builder<String, Object>().putAll(this.params).put(name, value).build();
		return new HttpRequest(transport, method, url, params, headers, timeout, retries, mapper);
	}
	
	/**
	 * Sets/overrides a header.  Value is not encoded in any particular way.
	 */
	public HttpRequest header(String name, String value) {
		final ImmutableMap<String, String> headers = new ImmutableMap.Builder<String, String>().putAll(this.headers).put(name, value).build();
		return new HttpRequest(transport, method, url, params, headers, timeout, retries, mapper);
	}
	
	/**
	 * Set a connection/read timeout in milliseconds, or 0 for no/default timeout.
	 */
	public HttpRequest timeout(int timeout) {
		return new HttpRequest(transport, method, url, params, headers, timeout, retries, mapper);
	}

	/**
	 * Set a retry count, or 0 for no retries
	 */
	public HttpRequest retries(int retries) {
		return new HttpRequest(transport, method, url, params, headers, timeout, retries, mapper);
	}

	/**
	 * Set the mapper. Be somewhat careful here, ObjectMappers are themselves not immutable (sigh).
	 */
	public HttpRequest mapper(ObjectMapper mapper) {
		return new HttpRequest(transport, method, url, params, headers, timeout, retries, mapper);
	}

	/**
	 * Set the basic auth header
	 */
	public HttpRequest basicAuth(String username, String password) {
		final String basic = username + ':' + password;

		// There is no standard for charset, might as well use utf-8
		final byte[] bytes = basic.getBytes(StandardCharsets.UTF_8);

		return header("Authorization", "Basic " + BaseEncoding.base64().encode(bytes));
	}

	/**
	 * Execute the request, providing the result in the response object - which might be an async wrapper, depending
	 * on the transport.
	 */
	public HttpResponse fetch() {
		Preconditions.checkState(url != null);

		log.debug("Fetching {}", this);
		log.debug("{} {}", getMethod(), getUrlComplete());

		try {
			return new HttpResponse(getTransport().fetch(this), getMapper());
		} catch (IOException e) {
			throw new IORException(e);
		}
	}

	/**
	 * @return the actual url for this request, with appropriate parameters
	 */
	public String getUrlComplete() {
		if (isPOST()) {
			return getUrl();
		} else {
			final String queryString = createQueryString();
			return queryString.isEmpty() ? getUrl() : (getUrl() + "?" + queryString);
		}
	}

	/**
	 * @return the content type which should be submitted along with this data, of null if not present (ie a GET)
	 */
	public String getContentType() {
		if (isPOST()) {
			if (hasBinaryAttachments())
				return MultipartWriter.CONTENT_TYPE;
			else
				return "application/x-www-form-urlencoded; charset=utf-8";
		} else {
			return null;
		}
	}

	/**
	 * Write any body content, if appropriate
	 */
	public void writeBody(OutputStream output) throws IOException {
		if (log.isDebugEnabled()) {
			output = new TeeOutputStream(output, new ByteArrayOutputStream());
		}

		if (isPOST()) {
			if (hasBinaryAttachments()) {
				MultipartWriter writer = new MultipartWriter(output);
				writer.write(params);
			} else {
				final String queryString = createQueryString();
				output.write(queryString.getBytes(StandardCharsets.UTF_8));
			}
		}

		if (log.isDebugEnabled()) {
			byte[] bytes = ((ByteArrayOutputStream)((TeeOutputStream)output).getTwo()).toByteArray();
			if (bytes.length > 0)
				log.debug("Wrote body: {}", new String(bytes, StandardCharsets.UTF_8));	// not necessarily utf8 but best choice available
		}
	}

	/** POST has a lot of special cases, so this is convenient */
	public boolean isPOST() {
		return HttpMethod.POST.name().equals(getMethod());
	}

	/** @return true if there are any binary attachments in the parameters */
	private boolean hasBinaryAttachments() {
		for (Object value: getParams().values())
			if (value instanceof BinaryAttachment)
				return true;

		return false;
	}

	/**
	 * Creates a string representing the current query string, or an empty string if there are no parameters.
	 * Will not work if there are binary attachments!
	 */
	private String createQueryString() {

		if (this.getParams().isEmpty())
			return "";
		
		StringBuilder bld = null;
		
		for (Map.Entry<String, Object> param: this.params.entrySet()) {
			if (bld == null)
				bld = new StringBuilder();
			else
				bld.append('&');
			
			bld.append(UrlUtils.urlEncode(param.getKey()));
			bld.append('=');
			bld.append(UrlUtils.urlEncode(param.getValue().toString()));
		}
		
		return bld.toString();
	}
}