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

import com.voodoodyne.hattery.DefaultTransport;
import com.voodoodyne.hattery.HttpRequest;
import com.voodoodyne.hattery.Transport;


/**
 * Some constant requests - they're immutable!
 * 
 * @author Jeff Schnitzer
 */
public class Requests {

	/** */
	public static Transport TRANSPORT = new DefaultTransport();

	/** */
	public static HttpRequest HEADERS_ENDPOINT = TRANSPORT.request("http://headers.jsontest.com");

	/** */
	public static  HttpRequest MD5_ENDPOINT = TRANSPORT.request("http://md5.jsontest.com");

	/** */
	public static  HttpRequest ECHO_ENDPOINT = TRANSPORT.request("http://echo.jsontest.com");

	/** */
	public static  HttpRequest VALIDATE_ENDPOINT = TRANSPORT.request("http://validate.jsontest.com");
}