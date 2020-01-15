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

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * <p>Uses GAE's URLFetch service.  Supports parallel fetching!</p>
 * 
 * @author Jeff Schnitzer
 */
@ToString
@Slf4j
public class AppEngineTransport implements Transport {

	private final URLFetchService fetchService;

	public AppEngineTransport() {
		this(URLFetchServiceFactory.getURLFetchService());
	}

	/** Handy if you want to pass in a mock */
	public AppEngineTransport(final URLFetchService fetchService) {
		this.fetchService = fetchService;
	}

	@Override
	public TransportResponse fetch(final HttpRequest request) throws IOException {
		final HTTPRequest gaeRequest = new HTTPRequest(request.toUrl(), HTTPMethod.valueOf(request.getMethod()), defaultOptions());

		if (request.getTimeout() > 0)
			gaeRequest.getFetchOptions().setDeadline(request.getTimeout() / 1000.0);

		for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
			gaeRequest.setHeader(new HTTPHeader(header.getKey(), header.getValue()));
		}

		if (request.getContentType() != null) {
			gaeRequest.setHeader(new HTTPHeader("Content-Type", request.getContentType()));
		}

		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
		request.writeBody(outputStream);
		if (outputStream.size() > 0) {
			gaeRequest.setPayload(outputStream.toByteArray());
		}

		if (request.isFollowRedirects())
			gaeRequest.getFetchOptions().followRedirects();
		else
			gaeRequest.getFetchOptions().doNotFollowRedirects();

		return new Response(request.getRetries(), gaeRequest);
	}

	/**
	 * Provide a hook so that default options like following redirects, validating ssl, etc can be overridden by
	 * subclassing the transport.
	 */
	protected FetchOptions defaultOptions() {
		return FetchOptions.Builder.withDefaults();
	}

	/**
	 * The appengine version of an HttpResponse, which hides the asynchrony and the retry mechanism.
	 */
	private class Response implements TransportResponse {
		
		/** Number of retries to execute */
		private final int retries;
		
		/** */
		private final HTTPRequest request;
		
		/** */
		private final Future<HTTPResponse> futureResponse;
		
		/** */
		public Response(int retries, HTTPRequest req) {
			this.retries = retries;
			this.request = req;
			this.futureResponse = fetchService.fetchAsync(this.request);
		}

		@Override
		public int getResponseCode() throws IOException {
			return this.getResponse().getResponseCode();
		}

		@Override
		public InputStream getContentStream() throws IOException {
			return new ByteArrayInputStream(this.getResponse().getContent());
		}

		@Override
		public byte[] getContentBytes() throws IOException {
			return this.getResponse().getContent();
		}

		@Override
		public ListMultimap<String, String> getHeaders() throws IOException {
			final ListMultimap<String, String> headers = ArrayListMultimap.create();

			for (HTTPHeader header : getResponse().getHeadersUncombined()) {
				headers.put(header.getName(), header.getValue());
			}

			return headers;
		}

		/** */
		private HTTPResponse getResponse() throws IOException {
			try {
				return this.futureResponse.get();
			}
			catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
			catch (ExecutionException ex) {
				if (ex.getCause() instanceof IOException) {
					if (this.retries == 0) {
						throw (IOException)ex.getCause();
					} else {
						log.warn("URLFetch timed out, retrying: " + ex.getCause().toString());
						return new Response(this.retries-1, this.request).getResponse();
					}
				}
				else if (ex.getCause() instanceof RuntimeException) {
					throw (RuntimeException)ex.getCause();
				}
				else
					throw new UndeclaredThrowableException(ex);
			}
		}

	}
}