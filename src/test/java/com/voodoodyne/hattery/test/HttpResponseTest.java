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
import static org.hamcrest.Matchers.hasKey;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.voodoodyne.hattery.HttpException;
import com.voodoodyne.hattery.HttpResponse;
import com.voodoodyne.hattery.TransportResponse;
import com.voodoodyne.hattery.test.util.DefaultBase;

/**
 * @author Jeff Schnitzer
 */
public class HttpResponseTest extends DefaultBase {

	private ListMultimap<String, String> fakeHeaders() {
		ListMultimap<String, String> headers = ArrayListMultimap.create();
		headers.put("Host", "host");
		headers.put("User-Agent", "user-agent");
		headers.put("Accept","accept");
		return headers;
	}
	
	/** */
	@Test
	public void getHeaders() throws Exception {
		TransportResponse transportResponseMock = Mockito.mock(TransportResponse.class);
		Mockito.when(transportResponseMock.getHeaders()).thenReturn(fakeHeaders());
		
		HttpResponse response = new HttpResponse(transportResponseMock, new ObjectMapper());
		
		final Map<String, Collection<String>> headers = response.getHeaders().asMap();
		assertThat(headers, hasKey("Host"));
		assertThat(headers, hasKey("User-Agent"));
		assertThat(headers, hasKey("Accept"));
	}
	
	/** */
	@Test
	public void getContent() throws Exception {
		TransportResponse transportResponseMock = Mockito.mock(TransportResponse.class);
		byte[] byteTest = "test".getBytes();
		Mockito.when(transportResponseMock.getContent()).thenReturn(byteTest);
		
		HttpResponse response = new HttpResponse(transportResponseMock, new ObjectMapper());
		
		assertThat(response.getContent(), equalTo(byteTest));
	}
	
	/** */
	@Test
	public void getContentStream() throws Exception {
		TransportResponse transportResponseMock = Mockito.mock(TransportResponse.class);
		InputStream byteArrayInputStream = new ByteArrayInputStream("test".getBytes());
		Mockito.when(transportResponseMock.getContentStream()).thenReturn(byteArrayInputStream);
		
		HttpResponse response = new HttpResponse(transportResponseMock, new ObjectMapper());
		
		assertThat(response.getContentStream(), equalTo(byteArrayInputStream));
	}
	
	/** */
	@Test
	public void getSuccessContentStream() throws Exception {
		TransportResponse transportResponseMock = Mockito.mock(TransportResponse.class);
		InputStream byteArrayInputStream = new ByteArrayInputStream("test".getBytes());
		Mockito.when(transportResponseMock.getContentStream()).thenReturn(byteArrayInputStream);
		
		HttpResponse response = Mockito.spy(new HttpResponse(transportResponseMock, new ObjectMapper()));
		Mockito.doReturn(response).when(response).succeed();
		
		assertThat(response.getSuccessContentStream(), equalTo(byteArrayInputStream));
	}
	
	/** */
	@Test
	public void getSuccessContent() throws Exception {
		TransportResponse transportResponseMock = Mockito.mock(TransportResponse.class);
		byte[] byteTest = "test".getBytes();
		Mockito.when(transportResponseMock.getContent()).thenReturn(byteTest);
		
		HttpResponse response = Mockito.spy(new HttpResponse(transportResponseMock, new ObjectMapper()));
		Mockito.doReturn(response).when(response).succeed();
		
		assertThat(response.getSuccessContent(), equalTo(byteTest));
	}
	
	/** */
	@Test
	public void succeedSuccessful() throws Exception {
		TransportResponse transportResponseMock = Mockito.mock(TransportResponse.class);
		Mockito.when(transportResponseMock.getResponseCode()).thenReturn(200);
		
		HttpResponse response = new HttpResponse(transportResponseMock, new ObjectMapper());
		assertThat(response.succeed(), equalTo(response));
		
		Mockito.when(transportResponseMock.getResponseCode()).thenReturn(304);
		assertThat(response.succeed(), equalTo(response));
	}
	
	/** */
	@Test(expectedExceptions={HttpException.class})
	public void succeedUnsuccessful() throws Exception {
		TransportResponse transportResponseMock = Mockito.mock(TransportResponse.class);
		Mockito.when(transportResponseMock.getResponseCode()).thenReturn(404);
		
		HttpResponse response = new HttpResponse(transportResponseMock, new ObjectMapper());
		response.succeed();
		assertThat(true, equalTo(false)); //Should fail if gets here
	}

}