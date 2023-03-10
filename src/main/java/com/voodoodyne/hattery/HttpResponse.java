package com.voodoodyne.hattery;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ListMultimap;
import com.voodoodyne.hattery.util.CaseInsensitiveListMultimap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Returned by request execution.
 *
 * Because of header caching, this object is not thread safe.
 */
@RequiredArgsConstructor
@ToString(exclude = "mapper")
public class HttpResponse {
	@Getter
	private final TransportResponse transportResponse;

	@Getter
	private final ObjectMapper mapper;

	private final ErrorTranslator errorTranslator;

	private CaseInsensitiveListMultimap<String> cachedHeaders;

	/** The http response code */
	public int getResponseCode() throws IORuntimeException {
		try {
			return transportResponse.getResponseCode();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * Response headers
	 * @return a collection that is case insensitive, case preserving, unmodifiable
	 */
	public ListMultimap<String, String> getHeaders() throws IORuntimeException {
		if (cachedHeaders == null) {
			cachedHeaders = new CaseInsensitiveListMultimap<>(getTransportHeaders());
		}

		return cachedHeaders;
	}

	private ListMultimap<String, String> getTransportHeaders() {
		try {
			return transportResponse.getHeaders();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * A convenience method for obtaining the Location header value
	 * @return the value of the Location header, if it exists
	 */
	public Optional<String> getLocation() throws IORuntimeException {
		final List<String> location = getHeaders().get("Location");
		return location.isEmpty() ? Optional.empty() : Optional.of(location.get(0));
	}

	/**
	 * A convenience method for obtaining the Content-Type header value
	 * @return the value of the Content-Type header, if it exists
	 */
	public Optional<String> getContentType() throws IORuntimeException {
		final List<String> location = getHeaders().get("Content-Type");
		return location.isEmpty() ? Optional.empty() : Optional.of(location.get(0));
	}

	/**
	 * Call this if you don't care about the response body, you only want it to ensure that the request was successful
	 * @return this
	 * @throws HttpException if response code is not successful
	 */
	public HttpResponse succeed() throws HttpException {
		if (getResponseCode() < 200 || getResponseCode() >= 400)
			throw errorTranslator.translate(new HttpException(getResponseCode(), getHeaders(), getContentBytes()));

		return this;
	}

	/**
	 * Convert the response to a JSON object using Jackson
	 * @throws HttpException if there was a nonsuccess error code
	 */
	public <T> T as(final Class<T> type) throws HttpException, IORuntimeException {
		return succeed().getContentAs(type);
	}

	/**
	 * Convert the response to a JSON object using Jackson
	 * @throws HttpException if there was a nonsuccess error code
	 */
	public <T> T as(final TypeReference<T> type) throws HttpException, IORuntimeException {
		return succeed().getContentAs(type);
	}

	/**
	 * Convert the response to a JSON object using Jackson
	 * @throws HttpException if there was a nonsuccess error code
	 */
	public <T> T as(final JavaType type) throws HttpException, IORuntimeException {
		return succeed().getContentAs(type);
	}

	/** The body content of the response, throwing HttpException if response code is not successful */
	public InputStream asStream() throws HttpException, IORuntimeException {
		return succeed().getContentStream();
	}

	/** The body content of the response, throwing HttpException if response code is not successful */
	public byte[] asBytes() throws HttpException, IORuntimeException {
		return succeed().getContentBytes();
	}

	/**
	 * Convert the response to a string.
	 * @throws HttpException if there was a nonsuccess error code
	 */
	public String asString(final Charset charset) throws HttpException, IORuntimeException {
		return new String(asBytes(), charset);
	}

	/**
	 * Convert the response to a string, assuming UTF-8 (because that's what you want 95% of the time)
	 * @throws HttpException if there was a nonsuccess error code
	 */
	public String asString() throws HttpException, IORuntimeException {
		return asString(StandardCharsets.UTF_8);
	}

	/** Shorthand for as(JsonNode.class) */
	public JsonNode asNode() throws HttpException, IORuntimeException {
		return as(JsonNode.class);
	}

	/** The body content of the response, whether it was success or error */
	public InputStream getContentStream() throws IORuntimeException {
		try {
			return transportResponse.getContentStream();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * The body content of the response, whether it was success or error
	 * Normally you should use {@code asBytes()} instead to check success.
	 */
	public byte[] getContentBytes() throws IORuntimeException {
		try {
			return transportResponse.getContentBytes();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * The body content of the response, whether it was success or error
	 * Normally you should use {@code asString()} instead to check success.
	 */
	public String getContentString(final Charset charset) throws IORuntimeException {
		return new String(getContentBytes(), charset);
	}

	/**
	 * The body content of the response, whether it was success or error. Assumes UTF-8.
	 * Normally you should use {@code asString()} instead to check success.
	 */
	public String getContentString() throws IORuntimeException {
		return getContentString(StandardCharsets.UTF_8);
	}

	/**
	 * Convert the response (whether success or error) to a JSON object using Jackson.
	 * Normally you should use {@code as()} instead to check success.
	 */
	public <T> T getContentAs(final Class<T> type) throws IORuntimeException {
		try {
			return mapper.readValue(getContentStream(), type);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * Convert the response  (whether success or error) to a JSON object using Jackson
	 * Normally you should use {@code as()} instead to check success.
	 */
	public <T> T getContentAs(final TypeReference<T> type) throws IORuntimeException {
		try {
			return mapper.readValue(getContentStream(), type);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	/**
	 * Convert the response  (whether success or error) to a JSON object using Jackson
	 * Normally you should use {@code as()} instead to check success.
	 */
	public <T> T getContentAs(final JavaType type) throws IORuntimeException {
		try {
			return mapper.readValue(getContentStream(), type);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}
}
