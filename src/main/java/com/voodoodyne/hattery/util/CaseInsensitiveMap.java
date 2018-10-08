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

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Unmodifiable; all write methods throw UnsupportedOperationException
 */
@ToString(of="base")
@RequiredArgsConstructor
public class CaseInsensitiveMap<V> implements Map<String, V> {
	final Map<String, V> base;
	final Map<String, V> normalized;

	public CaseInsensitiveMap(final Map<String, V> base) {
		this.base = Collections.unmodifiableMap(base);

		final Map<String, V> normalized = new LinkedHashMap<>();

		for (final Entry<String, V> entry : base.entrySet()) {
			normalized.put(entry.getKey().toLowerCase(), entry.getValue());
		}

		this.normalized = Collections.unmodifiableMap(normalized);
	}

	@Override
	public int size() {
		return base.size();
	}

	@Override
	public boolean isEmpty() {
		return base.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {
		return normalized.containsKey(((String)key).toLowerCase());
	}

	@Override
	public boolean containsValue(final Object value) {
		return base.containsValue(value);
	}

	@Override
	public V get(final Object key) {
		return normalized.get(((String)key).toLowerCase());
	}

	@Override
	public V put(final String key, final V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(final Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(final Map<? extends String, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> keySet() {
		return base.keySet();
	}

	@Override
	public Collection<V> values() {
		return base.values();
	}

	@Override
	public Set<Entry<String, V>> entrySet() {
		return base.entrySet();
	}
}