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

package com.voodoodyne.hattery.test.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;

import org.testng.annotations.Test;

import com.voodoodyne.hattery.util.LineWriter;

/**
 * @author Jeff Schnitzer
 */
public class LineWriterTest extends DefaultBase {
	
	private static final String CRLF = "\r\n";
	
	/**
	 * @throws IOException  */
	@Test
	public void print() throws IOException {
		configSystemOutForTest();
		LineWriter lineWriter = new LineWriter(outContent);
		lineWriter.print("f");
		lineWriter.print("oo");
		lineWriter.close();
		assertThat(outContent.toString(), equalTo("foo"));
		resetSystemOutTest();
	}
	
	@Test
	public void printlnWithParameters() throws IOException {
		configSystemOutForTest();
		LineWriter lineWriter = new LineWriter(outContent);
		lineWriter.println("f");
		lineWriter.println("oo");
		lineWriter.close();
		assertThat(outContent.toString(), equalTo("f" + CRLF + "oo" + CRLF));
		resetSystemOutTest();
	}
	
	@Test
	public void printlnWithoutParameters() throws IOException {
		configSystemOutForTest();
		LineWriter lineWriter = new LineWriter(outContent);
		lineWriter.println();
		lineWriter.close();
		assertThat(outContent.toString(), equalTo(CRLF));
		resetSystemOutTest();
	}
}