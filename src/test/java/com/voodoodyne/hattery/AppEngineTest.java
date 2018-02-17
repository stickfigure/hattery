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

import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author Jeff Schnitzer
 */
class AppEngineTest {

	/** The timeout service returns this */
	@Data
	public static class Foo {
		private final String foo;
	}

	@Data
	private static class CallCounter implements InvocationHandler {
		private final URLFetchService service;
		private int count;

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			count++;
			return method.invoke(service, args);
		}
	}

	/** */
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalURLFetchServiceTestConfig());

	@BeforeEach
	void setUpHelper() {
		helper.setUp();
	}

	@AfterEach
	void tearDownHelper() {
		helper.tearDown();
	}

	/** */
	@Test
	void timesOutAndRetries() throws Exception {
		final CallCounter callCounter = new CallCounter(URLFetchServiceFactory.getURLFetchService());
		final URLFetchService fetchService = (URLFetchService)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{URLFetchService.class}, callCounter);

		try {
			new AppEngineTransport(fetchService).request()
					.url("http://voodoodyne1.appspot.com/timeout")
					.param("time", "4")
					//.retries(2)
					.timeout(1000)
					.fetch().as(Foo.class);
			assert false;
		} catch (IORException e) {
			assertThat(callCounter.getCount()).isEqualTo(1);
		}
	}

}