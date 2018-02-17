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

package com.voodoodyne.hattery.util;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 */
class UrlUtilsTest {
	
	private String encodedURL = "http%3A%2F%2Fvalidate.jsontest.com%3Fjson%3D%7Bfoo%3Abar%7D";
	private String decodedURL = "http://validate.jsontest.com?json={foo:bar}";
	
	/** */
	@Test
	void urlEncode() {
		assertThat(UrlUtils.urlEncode(decodedURL)).isEqualTo(encodedURL);
	}
	
	@Test
	void urlDecode() {
		assertThat(UrlUtils.urlDecode(encodedURL)).isEqualTo(decodedURL);
	}
}