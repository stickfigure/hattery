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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

/**
 * <p>Uses the default HttpURLConnection in the JDK.  Does not support parallel fetching.</p>
 * 
 * @author Jeff Schnitzer
 */
@Slf4j
public class DefaultTransport extends Transport {

	@Override
	public TransportResponse fetch(HttpRequest request) throws IOException {
		for (int i = 0; i <= request.getRetries(); i++) {
			try {
				return executeOnce(request);
			} catch (IOException ex) {
				// This should just be a check for SocketTimeoutException, but GAE is not
				// throwing the right exception - it's just IOException with "Timeout while fetching..."
				if (i < request.getRetries() && (ex instanceof SocketTimeoutException || ex.getMessage().startsWith("Timeout"))) {
					log.warn("Timeout error, retrying");
				} else {
					throw ex;
				}
			}
		}

		// Logically unreachable code, but the compiler doesn't know that
		return null;
	}

	/** Override this if you want special behavior */
	protected void prepareConnection(final HttpURLConnection conn) {
		// default do nothing
	}

	/** */
	private TransportResponse executeOnce(HttpRequest request) throws IOException {

		final HttpURLConnection conn = (HttpURLConnection)request.toUrl().openConnection();
		conn.setRequestMethod(request.getMethod());
		conn.setConnectTimeout(request.getTimeout());
		conn.setReadTimeout(request.getTimeout());

		for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
			conn.setRequestProperty(header.getKey(), header.getValue());
		}

		if (request.getContentType() != null) {
			conn.setRequestProperty("Content-Type", request.getContentType());
		}

		prepareConnection(conn);

		// This whole setDoOutput() thing is retarded
		request.writeBody(new OutputStream() {
			private OutputStream real;
			private OutputStream output() throws IOException {
				if (real == null) {
					conn.setDoOutput(true);
					real = conn.getOutputStream();
				}
				return real;
			}

			@Override
			public void write(int b) throws IOException {
				output().write(b);
			}
			@Override
			public void write(byte[] b) throws IOException {
				output().write(b);
			}
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				output().write(b, off, len);
			}
			@Override
			public void flush() throws IOException {
				output().flush();
			}
			@Override
			public void close() throws IOException {
				output().close();
			}
		});

		final int responseCode = conn.getResponseCode();
		final InputStream content = conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream();

		final ListMultimap<String, String> headers = ArrayListMultimap.create();
		for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
			headers.putAll(entry.getKey(), entry.getValue());
		}

		return new TransportResponse() {
			@Override
			public int getResponseCode() throws IOException {
				return responseCode;
			}

			@Override
			public InputStream getContentStream() throws IOException {
				return content;
			}

			@Override
			public byte[] getContent() throws IOException {
				return ByteStreams.toByteArray(getContentStream());
			}

			@Override
			public ListMultimap<String, String> getHeaders() throws IOException {
				return headers;
			}
		};
	}

}