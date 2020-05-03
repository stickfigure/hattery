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

import com.fasterxml.jackson.databind.JsonNode;
import com.voodoodyne.hattery.HttpRequest;
import lombok.Data;

import java.util.Map;


/**
 * The response that comes back from the snoop service
 *
 * @author Jeff Schnitzer
 */
@Data
public class Snoop {

	/** */
	public static HttpRequest SNOOP = HttpRequest.HTTP.url("https://hattery-snoop.appspot.com");

	private final String url;
	private final String method;
	private final String path;
	private final String query;
	private final Map<String, String[]> queryParams;
	private final Map<String, String[]> formParams;
	private final Map<String, String> headers;
	private final JsonNode body;

	public String getContentType() {
		return getHeaders().get("Content-Type");
	}
}