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
import com.voodoodyne.hattery.util.QueryBuilder;
import com.voodoodyne.hattery.util.TeeOutputStream;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * <p>Immutable definition of a request; methods return new immutable object with the data changed.</p>
 * 
 * @author Jeff Schnitzer
 */
@Data
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
@ToString(exclude = {"mapper", "preflight", "postflight", "body"})	// too noisy
public class HttpRequest {
	/** The immutable starting point for any http request chain */
	public static HttpRequest HTTP = new HttpRequest(new DefaultTransport());

	/** */
	public static final String APPLICATION_JSON = "application/json";
	public static final String APPLICATION_XML = "application/xml";
	public static final String TEXT_XML = "text/xml";
	public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded; charset=utf-8";

	/** Just the first part of it for matching */
	private static final String APPLICATION_X_WWW_FORM_URLENCODED_BEGINNING = APPLICATION_X_WWW_FORM_URLENCODED.split(";")[0];

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

	/** 0 for no explicit timeout (aka default), otherwise measured in millis */
	private final int timeout;

	/** 0 for no retries */
	private final int retries;

	/** */
	private final ObjectMapper mapper;

	/** */
	private final Function<HttpRequest, HttpRequest> preflight;

	/** */
	private final Function<HttpResponse, HttpResponse> postflight;

	/** Careful, defaults to true like most libraries */
	private final boolean followRedirects;

	/**
	 * Default values
	 */
	HttpRequest(final Transport transport) {
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
		this.postflight = Function.identity();
		this.followRedirects = true;
	}

	/** Replace the existing transport */
	public HttpRequest transport(final Transport transport) {
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight, postflight, followRedirects);
	}

	/** */
	public HttpRequest method(final String method) {
		Preconditions.checkNotNull(method);
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight, postflight, followRedirects);
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

	/** Shortcut for method(HttpMethod.PATCH) */
	public HttpRequest PATCH() {
		return method(HttpMethod.PATCH);
	}

	/**
	 * Replaces the existing url wholesale
	 */
	public HttpRequest url(final String url) {
		Preconditions.checkNotNull(url);
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight, postflight, followRedirects);
	}

	/**
	 * Appends path to the existing url. If no url is not set, this becomes the url.
	 * Ensures this is a separate path segment by adding or removing a leading '/' if necessary.
	 * @param path is converted to a string via toString()
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
	 * Set/override the parameter. If the value is iterable, this will create multiple parameter entries in the query.
	 * @param value can be null to remove a parameter, or Iterable to create multiple values
	 * @return the updated, immutable request
	 */
	public HttpRequest param(final String name, Object value) {
		if (value instanceof Iterable)
			value = ImmutableList.copyOf((Iterable<?>)value);

		return paramAnything(name, value);
	}

	/**
	 * Set/override the parameter, converting the value to JSON using the current mapper.
	 * @param value can be null to remove a parameter, or any object that will be mapped to JSON
	 * @return the updated, immutable request
	 */
	@SneakyThrows
	public HttpRequest paramJson(final String name, final Object value) {
		final String json = mapper.writeValueAsString(value);
		return paramAnything(name, json);
	}

	/**
	 * Set/override the parameters. Values can be null to remove a parameter.
	 * @return the updated, immutable request
	 */
	public HttpRequest param(final Param... params) {
		HttpRequest here = this;
		for (Param param: params)
			here = here.param(param.getName(), param.getValue());

		return here;
	}

	/**
	 * Set/override the parameters. Values can be null to remove a parameter.
	 * JSON encodes the value.
	 * @return the updated, immutable request
	 */
	public HttpRequest paramJson(final Param... params) {
		HttpRequest here = this;
		for (Param param: params)
			here = here.paramJson(param.getName(), param.getValue());

		return here;
	}

	/**
	 * Set/override the parameter with a binary attachment.
	 */
	public HttpRequest param(final String name, final InputStream stream, final String contentType, final String filename) {
		final BinaryAttachment attachment = new BinaryAttachment(stream, contentType, filename);
		return POST().paramAnything(name, attachment);
	}

	/**
	 * Replace all the params with the specified values.
	 */
	public HttpRequest params(final Map<String, Object> params) {
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight, postflight, followRedirects);
	}

	/**
	 * Set/override the parameter with a value, forcing the parameter to be part of the query string
	 * even if POSTing form data or multipart form data. Normally you just use param(), which automatically
	 * does the right thing.
	 * @param value can be null to remove a parameter, or an Iterable to provide multiple values with the same key
	 * @return the updated, immutable request
	 */
	public HttpRequest queryParam(final String name, Object value) {
		if (value instanceof Iterable)
			value = ImmutableList.copyOf((Iterable<?>)value);

		return paramAnything(name, QueryParamValue.of(value));
	}

	/**
	 * <p>Set/override the parameter with a value, forcing the parameter to be part of the query string
	 * even if POSTing form data or multipart form data. Normally you just use param(), which automatically
	 * does the right thing.</p>
	 * <p>This version always JSON encodes the value and passes it as text</p>
	 * @param value can be null to remove a parameter, or an Iterable to provide multiple values with the same key
	 * @return the updated, immutable request
	 */
	@SneakyThrows
	public HttpRequest queryParamJson(final String name, final Object value) {
		final String json = mapper.writeValueAsString(value);
		return paramAnything(name, QueryParamValue.of(json));
	}

	/**
	 * Set/override the parameters, forcing the parameter to be part of the query string
	 * even if POSTing form data or multipart form data. Normally you just use param(), which automatically
	 * does the right thing.
	 * @param params can have null values to remove a parameter.
	 * @return the updated, immutable request
	 */
	public HttpRequest queryParam(final Param... params) {
		HttpRequest here = this;
		for (Param param: params)
			here = here.queryParam(param.getName(), param.getValue());

		return here;
	}

	/**
	 * <p>Set/override the parameters, forcing the parameter to be part of the query string
	 * even if POSTing form data or multipart form data. Normally you just use param(), which automatically
	 * does the right thing.</p>
	 * <p>This version always JSON encodes the value and passes it as text</p>
	 * @param params can have null values to remove a parameter.
	 * @return the updated, immutable request
	 */
	public HttpRequest queryParamJson(final Param... params) {
		HttpRequest here = this;
		for (Param param: params)
			here = here.queryParamJson(param.getName(), param.getValue());

		return here;
	}

	/** Private implementation lets us add anything, but don't expose that to the world */
	private HttpRequest paramAnything(final String name, final Object value) {
		final Map<String, Object> params = combine(this.params, name, value);
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight, postflight, followRedirects);
	}

	/**
	 * Provide a body that will be turned into JSON.
	 */
	public HttpRequest body(final Object body) {
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight, postflight, followRedirects);
	}

	/**
	 * Provide an explicit Content-Type. Otherwise content type will be crudely inferred (typically as
	 * json, form encoded, or multipart). If you're doing anything unusual, set an explicit content type.
	 */
	public HttpRequest contentType(final String value) {
		return new HttpRequest(transport, method, url, params, value, body, headers, timeout, retries, mapper, preflight, postflight, followRedirects);
	}

	/**
	 * Sets/overrides a header.  Value is not encoded in any particular way.
	 * Setting Content-Type is the same as calling contentType().
	 * @param value can be null to remove a header
	 */
	public HttpRequest header(final String name, final String value) {
		if (name.toLowerCase().equals("content-type"))
			return contentType(value);

		final Map<String, String> headers = combine(this.headers, name, value);
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight, postflight, followRedirects);
	}

	/**
	 * Replace all the headers with the specified values. Handles content-type as header() normally does;
	 * the contentType field is set and excluded from the actual headers.
	 */
	public HttpRequest headers(final Map<String, String> headers) {
		final Map<String, String> copiedHeaders = new LinkedHashMap<>();

		String contentType = this.contentType;

		for (final Entry<String, String> header : headers.entrySet()) {
			if (header.getKey().toLowerCase().equals("content-type")) {
				contentType = header.getValue();	// don't include it
			} else {
				copiedHeaders.put(header.getKey(), header.getValue());
			}
		}

		return new HttpRequest(transport, method, url, params, contentType, body, Collections.unmodifiableMap(copiedHeaders), timeout, retries, mapper, preflight, postflight, followRedirects);
	}

	/**
	 * Set a connection/read timeout in milliseconds, or 0 for no/default timeout.
	 */
	public HttpRequest timeout(final int millis) {
		return new HttpRequest(transport, method, url, params, contentType, body, headers, millis, retries, mapper, preflight, postflight, followRedirects);
	}

	/**
	 * Set a retry count, or 0 for no retries
	 */
	public HttpRequest retries(final int retries) {
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight, postflight, followRedirects);
	}

	/**
	 * Set the mapper. Be somewhat careful here, ObjectMappers are themselves not immutable (sigh).
	 */
	public HttpRequest mapper(final ObjectMapper mapper) {
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight, postflight, followRedirects);
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
	public HttpRequest preflight(final Function<HttpRequest, HttpRequest> preflight) {
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight, postflight, followRedirects);
	}

	/**
	 * A shortcut for {@code preflight(this.getPreflight().andThen(function)}. This method is probably what you
	 * typically want to use when building up a request.
	 */
	public HttpRequest preflightAndThen(final Function<HttpRequest, HttpRequest> preflight) {
		return preflight(this.preflight.andThen(preflight));
	}

	/**
	 * <p>Just after doing the fetch work, run this function on the http response before handing it back.</p>
	 *
	 * <p>This method completely replaces the postflight function. The default function function is identity,
	 * so you can safely {@code request.postflight(request.getPostflight().andThen(yourfunction)}</p>
	 */
	public HttpRequest postflight(final Function<HttpResponse, HttpResponse> postflight) {
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight, postflight, followRedirects);
	}

	/**
	 * A shortcut for {@code postflight(this.getPostflight().andThen(function)}. This method is probably what you
	 * typically want to use when building up a request.
	 */
	public HttpRequest postflightAndThen(final Function<HttpResponse, HttpResponse> postflight) {
		return postflight(this.postflight.andThen(postflight));
	}

	/**
	 * <p>Controls whether the transport should follow 301 and 302 redirects. To avoid surprises, the default is true
	 * - the same behavior as most http libraries.</p>
	 */
	public HttpRequest followRedirects(final boolean followRedirects) {
		return new HttpRequest(transport, method, url, params, contentType, body, headers, timeout, retries, mapper, preflight, postflight, followRedirects);
	}

	/**
	 * Execute the request, providing the result in the response object - which might be an async wrapper, depending
	 * on the transport.
	 */
	public HttpResponse fetch() {
		final HttpRequest preflighted = preflight.apply(this);
		final HttpResponse response = preflighted.doFetch();
		return postflight.apply(response);
	}

	/**
	 * Actually do the work after preflight and before postflight
	 */
	private HttpResponse doFetch() {
		Preconditions.checkState(url != null);

		log.info("Fetching {}", this);
		log.debug("{} {}", getMethod(), toUrlString());

		try {
			return new HttpResponse(getTransport().fetch(this), getMapper());
		} catch (IOException e) {
			throw new IORuntimeException(e);
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
		final String queryString = getQuery();
		return queryString.isEmpty() ? getUrl() : (getUrl() + "?" + queryString);
	}

	/**
	 * @return the java url equivalent of this request
	 * @throws IORuntimeException (the runtime wrapper for IOException) if somehow the url is malformed
	 */
	public URL toUrl() throws IORuntimeException {
		try {
			return new URL(toUrlString());
		} catch (MalformedURLException e) {
			throw new IORuntimeException(e);
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
			log.debug("Writing multipart body");
			final MultipartWriter writer = new MultipartWriter(output);
			writer.write(QueryParamValue.filterOut(params));
			Preconditions.checkState(body == null, "Cannot specify body() for type %s", ctype);
		}
		else if (ctype != null && ctype.startsWith(APPLICATION_X_WWW_FORM_URLENCODED_BEGINNING)) {
			output = tee(output);
			final String queryString = getQuery(QueryParamValue.filterOut(params));
			output.write(queryString.getBytes(StandardCharsets.UTF_8));
			Preconditions.checkState(body == null, "Cannot specify body() for type %s", ctype);
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
		else if (APPLICATION_JSON.equals(ctype) || APPLICATION_XML.equals(ctype) || TEXT_XML.equals(ctype)) {
			output = tee(output);
			mapper.writeValue(output, body);
		}
		else if (body instanceof String) {
			// Assume it is something like application/graphql... write it out in best guess about charset
			final Charset charset = guessCharset(ctype);
			output = tee(output);
			output.write(((String)body).getBytes(charset));
		}
		else if (body != null) {
			throw new UnsupportedOperationException(String.format("Not sure what to do with %s body for content-type %s", body.getClass(), ctype));
		}

		if (output instanceof TeeOutputStream) {
			final byte[] bytes = ((ByteArrayOutputStream)((TeeOutputStream)output).getTwo()).toByteArray();
			if (bytes.length > 0) {
				if (log.isTraceEnabled()) {
					log.debug("Wrote body, {} bytes: {}", bytes.length, new String(bytes, StandardCharsets.UTF_8));
				} else if (log.isDebugEnabled()) {
					// Put a reasonable cap on how long this can be, otherwise we might generate excessive logging
					final int length = Math.min(1000, bytes.length);
					log.debug("Wrote body, {} bytes: {}", bytes.length, new String(bytes, 0, length, StandardCharsets.UTF_8));
				}
			}
		}
	}

	private Charset guessCharset(final String ctype) {
		if (ctype == null)
			return StandardCharsets.UTF_8;

		final int ind = ctype.indexOf("charset=");
		if (ind >= 0) {
			final String charset = ctype.substring(ind + "charset=".length());
			return Charset.forName(charset);
		}

		return StandardCharsets.UTF_8;
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
	 * Creates a string representing what would be submitted as a query string for the current request, or an empty
	 * string if there are no parameters. A '?' will not be included. POST parameters are typically sent as part
	 * of the body and will therefore not be included here.
	 */
	public String getQuery() {
		if (paramsAreInContent())
			return getQuery(QueryParamValue.filterIn(this.params));
		else
			return getQuery(this.params);
	}

	/**
	 * Convert params to a query string no matter what
	 */
	private String getQuery(final Map<String, Object> params) {
		if (params.isEmpty())
			return "";

		final QueryBuilder bld = new QueryBuilder();

		for (Map.Entry<String, Object> param: params.entrySet()) {
			bld.add(param.getKey(), QueryParamValue.strip(param.getValue()));
		}

		return bld.toString();
	}

	/**
	 * Make a new map that combines the old values with the new key/value. Overrides the key if already present.
	 * @return a new immutable map, preserving order
	 */
	private <T> Map<String, T> combine(final Map<String, T> old, final String newKey, final T newValue) {
		final Map<String, T> combined = new LinkedHashMap<>(old);

		if (newValue == null || (newValue instanceof Iterable && !((Iterable)newValue).iterator().hasNext()))
			combined.remove(newKey);
		else
			combined.put(newKey, newValue);

		return Collections.unmodifiableMap(combined);
	}
}