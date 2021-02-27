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

import com.google.common.collect.ImmutableMap;
import com.voodoodyne.hattery.test.Snoop;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static com.voodoodyne.hattery.test.Snoop.SNOOP;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jeff Schnitzer
 */
class ParamsTest {

	/** */
	@Test
	void paramStringsAreSubmitted() throws Exception {
		final Snoop snoop = SNOOP
				.param("text", "example")
				.fetch().as(Snoop.class);

		assertThat(snoop.getQuery()).isEqualTo("text=example");
	}

	/** */
	@Test
	void paramJsonIsSubmitted() throws Exception {
		final Map<String, String> map = ImmutableMap.of("foo", "bar");
		final Snoop snoop = SNOOP
				.paramJson("text", map)
				.fetch().as(Snoop.class);

		assertThat(snoop.getQueryParams().get("text")).isEqualTo("{\"foo\":\"bar\"}");
	}

	/** */
	@Test
	void paramObjectsAreSubmitted() throws Exception {
		final Snoop snoop = SNOOP
				.param(new Param("text", "example"))
				.fetch().as(Snoop.class);

		assertThat(snoop.getQuery()).isEqualTo("text=example");
	}

	/** */
	@Test
	void paramsCanBeOverridden() throws Exception {
		final Snoop snoop = SNOOP
				.param("text", "notexample")
				.param("text", "example")
				.fetch().as(Snoop.class);

		assertThat(snoop.getQuery()).isEqualTo("text=example");
	}

	/** */
	@Test
	void paramsCanBeRemoved() throws Exception {
		final Snoop snoop = SNOOP
				.param("text", "notexample")
				.param("text", null)
				.fetch().as(Snoop.class);

		assertThat(snoop.getQuery()).isEqualTo("");
	}

	/** */
	@Test
	void queryParamsAreForcedEvenWhenPostingFormData() {
		final Snoop snoop = SNOOP
				.POST()
				.param("foo", "bar")
				.queryParam("foo2", "bar2")
				.fetch().as(Snoop.class);

		assertThat(snoop.getQuery()).isEqualTo("foo2=bar2");
		assertThat(snoop.getBody().asText()).isEqualTo("foo=bar");
		assertThat(snoop.getContentType()).startsWith("application/x-www-form-urlencoded");
	}

	/** */
	@Test
	void multipleParamObjectsCanBePassed() {
		final Snoop snoop = SNOOP
				.param(new Param("foo", "bar"), new Param("foo2", "bar2"))
				.fetch().as(Snoop.class);

		assertThat(snoop.getQuery()).isEqualTo("foo=bar&foo2=bar2");
	}

	/** */
	@Test
	void listParamsBecomeMultipleEntries() {
		final List<String> list = Arrays.asList("foo", "bar");
		final Snoop snoop = SNOOP
				.param("baz", list)
				.fetch().as(Snoop.class);

		assertThat(snoop.getQuery()).isEqualTo("baz=foo&baz=bar");
	}
}