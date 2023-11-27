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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Indicates an error condition from the remote side. Includes the full content of the result.
 * 
 * @author Jeff Schnitzer
 */
public class HttpException extends IORuntimeException {
	private static final long serialVersionUID = -4976836886636358176L;

	/** Longest text/* message we allow (we don't restrict json) */
	private static final int MAX_TEXT_MSG_LENGTH = 500;

	@Getter
	private final int code;

	@Getter
	private final Multimap<String, String> headers;

	@Getter
	private final byte[] content;

	public HttpException(final int code, final byte[] content) {
		this(code, ArrayListMultimap.create(), content);
	}

	public HttpException(final int code, final ListMultimap<String, String> headers, final byte[] content) {
		super(code + ": " + makeMessageOutOf(headers, content));

		this.code = code;
		this.headers = headers;
		this.content = content;
	}

	/**
	 * Get the content as a string with the specified charset.
	 */
	public String getContentString(final Charset charset) {
		return new String(getContent(), charset);
	}

	/**
	 * Alias for {@code getContentString(StandardCharsets.UTF_8)}
	 */
	public String getContentString() {
		return getContentString(StandardCharsets.UTF_8);
	}


	private static String makeMessageOutOf(final ListMultimap<String, String> headers, final byte[] content) {
		final String contentType = getContentType(headers);

		final byte[] notNullContent = content == null ? new byte[0] : content;

		if (contentType == null) {
			if (isValidUTF8(notNullContent)) {
				return chopTo(new String(notNullContent, StandardCharsets.UTF_8), MAX_TEXT_MSG_LENGTH);
			} else {
				return "Body of " + notNullContent.length + " bytes, not utf-8";
			}
		} else {
			final String lowercase = contentType.toLowerCase();
			if (lowercase.startsWith("text")) {
				return chopTo(new String(notNullContent, StandardCharsets.UTF_8), MAX_TEXT_MSG_LENGTH);
			} else if (lowercase.startsWith("application/json") || lowercase.startsWith("application/xml")) {
				return new String(notNullContent, StandardCharsets.UTF_8);
			} else {
				return "Error body of type " + contentType + ", " + notNullContent.length + " bytes";
			}
		}
	}

	/** Cut string down to a maximum length */
	private static String chopTo(final String original, final int length) {
		if (original.length() > length) {
			return original.substring(0, length - 1) + '\u2026';		// elipsis
		} else {
			return original;
		}
	}

	private static boolean isValidUTF8(final byte[] input) {
		final CharsetDecoder cs = StandardCharsets.UTF_8.newDecoder();

		try {
			cs.decode(ByteBuffer.wrap(input));
			return true;
		}
		catch (CharacterCodingException e) {
			return false;
		}
	}

	/** @return null if no content type can be found */
	private static String getContentType(final ListMultimap<String, String> headers) {
		if (headers == null)
			return null;

		final List<String> list = headers.get("Content-Type");
		if (!list.isEmpty())
			return list.get(0);

		final List<String> list2 = headers.get("content-type");
		if (!list2.isEmpty())
			return list2.get(0);

		return null;
	}
}