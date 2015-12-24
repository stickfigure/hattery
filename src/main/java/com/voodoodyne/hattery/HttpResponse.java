package com.voodoodyne.hattery;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ListMultimap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import java.io.IOException;
import java.io.InputStream;

/** Returned by request execution */
@RequiredArgsConstructor
@ToString(exclude = "mapper")
public class HttpResponse {
	private final TransportResponse response;

	@Getter
	private final ObjectMapper mapper;

	/** The http response code */
	public int getResponseCode() throws IORException {
		try {
			return response.getResponseCode();
		} catch (IOException e) {
			throw new IORException(e);
		}
	}

	/** Response headers */
	public ListMultimap<String, String> getHeaders() throws IORException {
		try {
			return response.getHeaders();
		} catch (IOException e) {
			throw new IORException(e);
		}
	}

	/** The body content of the response, whether it was success or error */
	public InputStream getContentStream() throws IORException {
		try {
			return response.getContentStream();
		} catch (IOException e) {
			throw new IORException(e);
		}
	}

	/** The body content of the response, whether it was success or error */
	public byte[] getContent() throws IORException {
		try {
			return response.getContent();
		} catch (IOException e) {
			throw new IORException(e);
		}
	}

	/** The body content of the response, throwing HttpException if response code is not successful */
	public InputStream getSuccessContentStream() throws HttpException, IORException {
		succeed();
		try {
			return response.getContentStream();
		} catch (IOException e) {
			throw new IORException(e);
		}
	}

	/** The body content of the response, throwing HttpException if response code is not successful */
	public byte[] getSuccessContent() throws HttpException, IORException {
		succeed();
		try {
			return response.getContent();
		} catch (IOException e) {
			throw new IORException(e);
		}
	}

	/**
	 * Call this if you don't care about the response body, you only want it to ensure that the request was successful
	 * @return this
	 * @throws HttpException if response code is not successful
	 */
	public HttpResponse succeed() throws HttpException {
		if (getResponseCode() < 200 || getResponseCode() >= 400)
			throw new HttpException(getResponseCode(), getContent());

		return this;
	}

	/**
	 * Convert the response to a JSON object using Jackson
	 * @throws HttpException if there was a nonsuccess error code
	 */
	public <T> T as(Class<T> type) throws HttpException, IORException {
		return succeed().contentAs(type);
	}

	/**`
	 * Convert the response to a JSON object using Jackson
	 * @throws HttpException if there was a nonsuccess error code
	 */
	public <T> T as(TypeReference<T> type) throws HttpException, IORException  {
		return succeed().contentAs(type);
	}

	/**
	 * Convert the response to a JSON object using Jackson
	 * @throws HttpException if there was a nonsuccess error code
	 */
	public <T> T as(JavaType type) throws HttpException, IORException  {
		return succeed().contentAs(type);
	}

	/**
	 * Convert the response (whether success or error) to a JSON object using Jackson.
	 */
	public <T> T contentAs(Class<T> type) throws IORException  {
		try {
			return mapper.readValue(getContentStream(), type);
		} catch (IOException e) {
			throw new IORException(e);
		}
	}

	/**`
	 * Convert the response  (whether success or error) to a JSON object using Jackson
	 */
	public <T> T contentAs(TypeReference<T> type) throws IORException  {
		try {
			return mapper.readValue(getContentStream(), type);
		} catch (IOException e) {
			throw new IORException(e);
		}
	}

	/**
	 * Convert the response  (whether success or error) to a JSON object using Jackson
	 */
	public <T> T contentAs(JavaType type) throws IORException  {
		try {
			return mapper.readValue(getContentStream(), type);
		} catch (IOException e) {
			throw new IORException(e);
		}
	}
}
