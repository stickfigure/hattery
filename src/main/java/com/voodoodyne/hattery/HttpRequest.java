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
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

	/** */
	public static final String APPLICATION_JSON = "application/json";
	public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded; charset=utf-8";

	/** Just the first part of it for matching */
	private static final String APPLICATION_X_WWW_FORM_URLENCODED_BEGINNING = APPLICATION_X_WWW_FORM_URLENCODED.split(" ")[0];

	/** */
	private final Transport transport;

	/** */
	private final String method;

	/** URL so far; can be extended with path() */
	private final String url;

	/** value will be either String, Collection<String>, or BinaryAttachment */
	private final Map<String, Object> params;

	/** */
	private final String contentType;

	/** Object to be jsonfied */
	private final Object body;

	/** */
	private final Map<String, String> headers;

	/** 0 for no explicit timeout (aka default) */
	private final int timeout;

	/** 0 for no retries */
	private final int retries;

	/** */
	private final ObjectMapper mapper;

	/** */
	private final Function<HttpRequest, HttpRequest> preflight;

	/**
	 * Default values
	 */
	public HttpRequest(final Transport transport) {
		this.transport = transport;
		this.method = HttpMethod.GET.name();
		this.url = null;
		this.params = Collections.emptyMap();
		this.headers = Collections.emptyMap();
		this.timeout = 0;
		this.retries = 0;
		this.mapper = new ObjectMapper();
		this.contentType = null;
		this.body = null;
		this.preflight = Function.identity();
	}

	/** */
	public HttpRequest method(final String method) {
		Preconditions.checkNotNull(method);
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight);
	}

	/** */
	public HttpRequest method(final HttpMethod method) {
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
	public HttpRequest url(final String url) {
		Preconditions.checkNotNull(url);
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight);
	}

	/**
	 * Appends path to the existing url. If no url is not set, this becomes the url.
	 * Ensures this is a separate path segment by adding or removing a leading '/' if necessary.
	 * @path is converted to a string via toString()
	 */
	public HttpRequest path(final Object path) {
		Preconditions.checkNotNull(path);
		String url2 = (url == null) ? path.toString() : concatPath(url, path.toString());
		return url(url2);
	}

	/** Check for slashes */
	private String concatPath(final String url, final String path) {
		if (url.endsWith("/")) {
			return path.startsWith("/") ? (url + path.substring(1)) : (url + path);
		} else {
			return path.startsWith("/") ? (url + path) : (url + '/' + path);
		}
	}

	/**
	 * Set/override the parameter with a single value
	 * @param value can be null to remove a parameter
	 * @return the updated, immutable request
	 */
	public HttpRequest param(final String name, final Object value) {
		return paramAnything(name, value);
	}

	/**
	 * Set/override the parameter with a list of values
	 * @param value can be empty or null to remove a parameter
	 * @return the updated, immutable request
	 */
	public HttpRequest param(final String name, final List<Object> value) {
		return paramAnything(name, value == null ? null : ImmutableList.copyOf(value));
	}

	/**
	 * Set/override the parameters. Values can be null to remove a parameter.
	 * @return the updated, immutable request
	 */
	public HttpRequest param(final Param... params) {
		HttpRequest here = this;
		for (Param param: params)
			here = paramAnything(param.getName(), param.getValue());

		return here;
	}

	/**
	 * Set/override the parameter with a binary attachment.
	 */
	public HttpRequest param(final String name, final InputStream stream, final String contentType, final String filename) {
		final BinaryAttachment attachment = new BinaryAttachment(stream, contentType, filename);
		return POST().paramAnything(name, attachment);
	}

	/** Private implementation lets us add anything, but don't expose that to the world */
	private HttpRequest paramAnything(final String name, final Object value) {
		final Map<String, Object> params = combine(this.params, name, value);
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight);
	}

	/**
	 * Provide a body that will be turned into JSON.
	 */
	public HttpRequest body(final Object body) {
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight);
	}
	
	/**
	 * Sets/overrides a header.  Value is not encoded in any particular way.
	 */
	public HttpRequest header(final String name, final String value) {
		final Map<String, String> headers = combine(this.headers, name, value);
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight);
	}

	/**
	 * Set a connection/read timeout in milliseconds, or 0 for no/default timeout.
	 */
	public HttpRequest timeout(final int timeout) {
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight);
	}

	/**
	 * Set a retry count, or 0 for no retries
	 */
	public HttpRequest retries(final int retries) {
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight);
	}

	/**
	 * Set the mapper. Be somewhat careful here, ObjectMappers are themselves not immutable (sigh).
	 */
	public HttpRequest mapper(final ObjectMapper mapper) {
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight);
	}

	/**
	 * Set the basic auth header
	 */
	public HttpRequest basicAuth(final String username, final String password) {
		final String basic = username + ':' + password;

		// There is no standard for charset, might as well use utf-8
		final byte[] bytes = basic.getBytes(StandardCharsets.UTF_8);

		return header("Authorization", "Basic " + BaseEncoding.base64().encode(bytes));
	}

	/**
	 * <p>Just before doing the fetch work, run this function on the http request and actually do the fetch work
	 * on the new value. This can be useful to (for example) sign requests.</p>
	 *
	 * <p>This method completely replaces the preflight function. The default preflight function is identity,
	 * so you can safely {@code request.preflight(request.getPreflight().andThen(yourfunction)}</p>
	 */
	public HttpRequest preflight(final Function<HttpRequest, HttpRequest> function) {
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, function);
	}

	/**
	 * A shortcut for {@code preflight(this.getPreflight().andThen(function)}. This method is probably what you
	 * typically want to use when building up a request.
	 */
	public HttpRequest preflightAndThen(final Function<HttpRequest, HttpRequest> function) {
		return preflight(preflight.andThen(function));
	}

	/**
	 * Execute the request, providing the result in the response object - which might be an async wrapper, depending
	 * on the transport.
	 */
	public HttpResponse fetch() {
		final HttpRequest preflighted = preflight.apply(this);
		return preflighted.doFetch();
	}

	/**
	 * Actually do the work post-preflight
	 */
	private HttpResponse doFetch() {
		Preconditions.checkState(url != null);

		log.debug("Fetching {}", this);
		log.debug("{} {}", getMethod(), toUrlString());

		try {
			return new HttpResponse(getTransport().fetch(this), getMapper());
		} catch (IOException e) {
			throw new IORException(e);
		}
	}

	/**
	 * @deprecated use toUrlString() instead
	 * @return the actual url for this request, with appropriate parameters
	 */
	@Deprecated
	public String getUrlComplete() {
		return toUrlString();
	}

	/**
	 * @return the full url for this request, with appropriate parameters
	 */
	public String toUrlString() {
		if (paramsAreInContent()) {
			return getUrl();
		} else {
			final String queryString = getQuery();
			return queryString.isEmpty() ? getUrl() : (getUrl() + "?" + queryString);
		}
	}

	/**
	 * @return the java url equivalent of this request
	 * @throws IORException (the runtime wrapper for IOException) if somehow the url is malformed
	 */
	public URL toUrl() throws IORException {
		try {
			return new URL(toUrlString());
		} catch (MalformedURLException e) {
			throw new IORException(e);
		}
	}

	/**
	 * @return the content type which should be submitted along with this data, of null if not present (ie a GET)
	 */
	public String getContentType() {
		if (contentType != null)
			return contentType;

		if (body != null)
			return APPLICATION_JSON;

		if (isPOST()) {
			if (hasBinaryAttachments()) {
				return MultipartWriter.CONTENT_TYPE;
			} else {
				return APPLICATION_X_WWW_FORM_URLENCODED;
			}
		} else {
			return null;
		}
	}

	/**
	 * Write any body content, if appropriate. Will debug log body if reasonable to do so.
	 */
	public void writeBody(OutputStream output) throws IOException {
		final String ctype = getContentType();

		if (MultipartWriter.CONTENT_TYPE.equals(ctype)) {
			output = tee(output);
			MultipartWriter writer = new MultipartWriter(output);
			writer.write(params);
		}
		else if (ctype != null && ctype.startsWith(APPLICATION_X_WWW_FORM_URLENCODED_BEGINNING)) {
			output = tee(output);
			final String queryString = getQuery();
			output.write(queryString.getBytes(StandardCharsets.UTF_8));
		}
		else if (body instanceof byte[]) {
			// Don't tee, probably binary
			output.write((byte[])body);
			log.debug("Wrote byte[] body of length {}", ((byte[])body).length);
		}
		else if (body instanceof InputStream) {
			// Don't tee, probably binary
			final long length = ByteStreams.copy((InputStream)body, output);
			log.debug("Wrote InputStream body of length {}", length);
		}
		else if (APPLICATION_JSON.equals(ctype)) {
			output = tee(output);
			mapper.writeValue(output, body);
		}

		if (output instanceof TeeOutputStream) {
			byte[] bytes = ((ByteArrayOutputStream)((TeeOutputStream)output).getTwo()).toByteArray();
			if (bytes.length > 0)
				log.debug("Wrote body: {}", new String(bytes, StandardCharsets.UTF_8));
		}
	}

	private OutputStream tee(final OutputStream output) {
		if (log.isDebugEnabled()) {
			return new TeeOutputStream(output, new ByteArrayOutputStream());
		} else {
			return output;
		}
	}

	/** POST has a lot of special cases, so this is convenient */
	public boolean isPOST() {
		return HttpMethod.POST.name().equals(getMethod());
	}

	/** For some types, params go in the body (not on the url) */
	private boolean paramsAreInContent() {
		final String ctype = getContentType();
		return ctype != null &&
				(ctype.startsWith(APPLICATION_X_WWW_FORM_URLENCODED_BEGINNING) || ctype.startsWith(MultipartWriter.CONTENT_TYPE));
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
	public String getQuery() {

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

	/**
	 * Make a new map that combines the old values with the new key/value. Overrides the key if already present.
	 * @return a new immutable map, preserving order
	 */
	private <T> Map<String, T> combine(final Map<String, T> old, final String newKey, final T newValue) {
		final Map<String, T> combined = new LinkedHashMap<>(old);

		if (newValue == null || (newValue instanceof Collection && ((Collection)newValue).isEmpty()))
			combined.remove(newKey);
		else
			combined.put(newKey, newValue);

		return Collections.unmodifiableMap(combined);
	}
}