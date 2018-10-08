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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 */
class HttpResponseTest {

	@Mock
	private TransportResponse transportResponse;

	@BeforeEach
	void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	/** */
	@Test
	void getHeaders() throws Exception {
		when(transportResponse.getHeaders()).thenReturn(fakeHeaders());

		final HttpResponse response = new HttpResponse(transportResponse, new ObjectMapper());
		
		final Map<String, Collection<String>> headers = response.getHeaders().asMap();
		assertThat(headers).containsKey("Host");
		assertThat(headers).containsKey("User-Agent");
		assertThat(headers).containsKey("Accept");
	}

	/** */
	@Test
	void headersAreCaseInsensitive() throws Exception {
		when(transportResponse.getHeaders()).thenReturn(fakeHeaders());

		final HttpResponse response = new HttpResponse(transportResponse, new ObjectMapper());

		final Map<String, Collection<String>> headers = response.getHeaders().asMap();
		assertThat(headers.get("Host")).isEqualTo(headers.get("hOsT"));
	}

	/** */
	@Test
	void getContent() throws Exception {
		final byte[] byteTest = "test".getBytes();
		when(transportResponse.getContent()).thenReturn(byteTest);
		when(transportResponse.getHeaders()).thenReturn(ArrayListMultimap.create());

		final HttpResponse response = new HttpResponse(transportResponse, new ObjectMapper());
		assertThat(response.getContent()).isEqualTo(byteTest);
	}
	
	/** */
	@Test
	void getContentStream() throws Exception {
		final InputStream byteArrayInputStream = new ByteArrayInputStream("test".getBytes());
		when(transportResponse.getContentStream()).thenReturn(byteArrayInputStream);
		when(transportResponse.getHeaders()).thenReturn(ArrayListMultimap.create());

		final HttpResponse response = new HttpResponse(transportResponse, new ObjectMapper());
		assertThat(response.getContentStream()).isEqualTo(byteArrayInputStream);
	}
	
	/** */
	@Test
	void getSuccessContentStream() throws Exception {
		final InputStream byteArrayInputStream = new ByteArrayInputStream("test".getBytes());
		when(transportResponse.getContentStream()).thenReturn(byteArrayInputStream);
		when(transportResponse.getHeaders()).thenReturn(ArrayListMultimap.create());

		final HttpResponse response = spy(new HttpResponse(transportResponse, new ObjectMapper()));
		doReturn(response).when(response).succeed();
		
		assertThat(response.getSuccessContentStream()).isEqualTo(byteArrayInputStream);
	}
	
	/** */
	@Test
	void getSuccessContent() throws Exception {
		final byte[] byteTest = "test".getBytes();
		when(transportResponse.getContent()).thenReturn(byteTest);
		when(transportResponse.getHeaders()).thenReturn(ArrayListMultimap.create());

		final HttpResponse response = spy(new HttpResponse(transportResponse, new ObjectMapper()));
		doReturn(response).when(response).succeed();
		
		assertThat(response.getSuccessContent()).isEqualTo(byteTest);
	}
	
	/** */
	@Test
	void succeedSuccessful() throws Exception {
		when(transportResponse.getResponseCode()).thenReturn(200);
		when(transportResponse.getHeaders()).thenReturn(ArrayListMultimap.create());

		final HttpResponse response = new HttpResponse(transportResponse, new ObjectMapper());
		assertThat(response.succeed()).isEqualTo(response);
		
		when(transportResponse.getResponseCode()).thenReturn(304);
		assertThat(response.succeed()).isEqualTo(response);
	}
	
	/** */
	@Test
	void succeedUnsuccessful() throws Exception {
		when(transportResponse.getResponseCode()).thenReturn(404);
		when(transportResponse.getHeaders()).thenReturn(ArrayListMultimap.create());
		
		final HttpResponse response = new HttpResponse(transportResponse, new ObjectMapper());
		
		assertThrows(HttpException.class, () -> {
			response.succeed();
		});
	}

	private ListMultimap<String, String> fakeHeaders() {
		ListMultimap<String, String> headers = ArrayListMultimap.create();
		headers.put("Host", "host");
		headers.put("User-Agent", "user-agent");
		headers.put("Accept","accept");
		return headers;
	}
}