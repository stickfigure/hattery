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

import com.voodoodyne.hattery.HttpRequest;


/**
 * Some constant requests - they're immutable!
 * 
 * @author Jeff Schnitzer
 */
public class Requests {

	/** */
	public static HttpRequest REQ = HttpRequest.HTTP;

	/** */
	public static HttpRequest HEADERS_ENDPOINT = REQ.url("http://headers.jsontest.com");

	/** */
	public static  HttpRequest MD5_ENDPOINT = REQ.url("http://md5.jsontest.com");

	/** */
	public static  HttpRequest ECHO_ENDPOINT = REQ.url("http://echo.jsontest.com");

	/** */
	public static  HttpRequest VALIDATE_ENDPOINT = REQ.url("http://validate.jsontest.com");
}