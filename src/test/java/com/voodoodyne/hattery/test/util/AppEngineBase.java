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

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.voodoodyne.hattery.DefaultTransport;
import com.voodoodyne.hattery.Transport;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;


/**
 * Some simple tools common to all tests
 * 
 * @author Jeff Schnitzer
 */
public class AppEngineBase extends AbstractTest {

	/** */
	private final LocalServiceTestHelper helper =
			new LocalServiceTestHelper(
					// Our tests assume strong consistency
					new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy(),
					new LocalMemcacheServiceTestConfig());

	/** */
	protected Transport transport;

	@BeforeMethod
	public void setUp() throws Exception {
		this.helper.setUp();
		this.transport = new DefaultTransport();
	}

	@AfterMethod
	public void tearDown() throws Exception {
		this.transport = null;
		this.helper.tearDown();
	}
}