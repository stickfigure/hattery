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

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

/**
 * Wrapper for a param value that forces the value to be treated as a query param no matter what kind
 * of http method and content type we are using. This is not exposed outside hattery.
 * 
 * @author Jeff Schnitzer
 */
@Data
class QueryParamValue {
	/** */
	private final Object value;

	/** Null-safe */
	public static QueryParamValue of(final Object val) {
		return val == null ? null : new QueryParamValue(val);
	}

	/** Strips the wrapper off (if present) */
	public static Object strip(final Object maybeQueryParamValue) {
		return maybeQueryParamValue instanceof QueryParamValue ? ((QueryParamValue)maybeQueryParamValue).getValue() : maybeQueryParamValue;
	}

	/** @return a map view that has only values of QueryParamValue type */
	public static Map<String, Object> filterIn(final Map<String, Object> input) {
		return Maps.filterValues(input, val -> val instanceof QueryParamValue);
	}

	/** @return a map view that excludes values of QueryParamValue type */
	public static Map<String, Object> filterOut(final Map<String, Object> input) {
		return Maps.filterValues(input, val -> !(val instanceof QueryParamValue));
	}
}