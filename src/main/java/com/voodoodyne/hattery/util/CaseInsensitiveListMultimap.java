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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import lombok.ToString;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Unmodifiable; all write methods throw UnsupportedOperationException
 */
@ToString(of="base")
public class CaseInsensitiveListMultimap<V> implements ListMultimap<String, V> {
	final ListMultimap<String, V> base;
	final ListMultimap<String, V> normalized;

	public CaseInsensitiveListMultimap(final ListMultimap<String, V> base) {
		this.base = Multimaps.unmodifiableListMultimap(base);

		final ArrayListMultimap<String, V> normalized = ArrayListMultimap.create();

		for (final Entry<String, Collection<V>> entry : base.asMap().entrySet()) {
			normalized.putAll(entry.getKey().toLowerCase(), entry.getValue());
		}

		this.normalized = Multimaps.unmodifiableListMultimap(normalized);
	}

	@Override
	public List<V> get(final String s) {
		return normalized.get(s.toLowerCase());
	}

	@Override
	public Set<String> keySet() {
		return base.keySet();
	}

	@Override
	public Multiset<String> keys() {
		return base.keys();
	}

	@Override
	public Collection<V> values() {
		return base.values();
	}

	@Override
	public Collection<Entry<String, V>> entries() {
		return base.entries();
	}

	@Override
	public List<V> removeAll(final Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
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
	public boolean containsKey(final Object o) {
		return normalized.containsKey(((String)o).toLowerCase());
	}

	@Override
	public boolean containsValue(final Object o) {
		return base.containsValue(o);
	}

	@Override
	public boolean containsEntry(final Object o, final Object o1) {
		return normalized.containsEntry(((String)o).toLowerCase(), o1);
	}

	@Override
	public boolean put(final String s, final V v) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(final Object o, final Object o1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean putAll(final String s, final Iterable<? extends V> iterable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean putAll(final Multimap<? extends String, ? extends V> multimap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<V> replaceValues(final String s, final Iterable<? extends V> iterable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Collection<V>> asMap() {
		return new CaseInsensitiveMap<>(base.asMap(), normalized.asMap());
	}
}