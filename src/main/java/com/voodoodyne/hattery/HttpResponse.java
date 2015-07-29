package com.voodoodyne.hattery;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Returned by request execution */
@RequiredArgsConstructor
@ToString
public class HttpResponse {
	private final TransportResponse response;
	private final ObjectMapper objectMapper;

	/** The http response code */
	public int getResponseCode() throws IORException {
		try {
			return response.getResponseCode();
		} catch (IOException e) {
			throw new IORException(e);
		}
	}

	/** The body content of the response */
	public InputStream getContentStream() throws IORException {
		try {
			return response.getContentStream();
		} catch (IOException e) {
			throw new IORException(e);
		}
	}

	public byte[] getContent() throws IORException {
		try {
			return response.getContent();
		} catch (IOException e) {
			throw new IORException(e);
		}
	}

	/**
	 * Convert the response to a JSON object using Jackson
	 * @throws HttpException if there was a nonsuccess error code
	 */
	public <T> T as(Class<T> type) throws HttpException, IORException  {
		if (getResponseCode() < 200 || getResponseCode() >= 300)
			throw new HttpException(getResponseCode(), new String(getContent(), StandardCharsets.UTF_8));

		try {
			return objectMapper.readValue(getContentStream(), type);
		} catch (IOException e) {
			throw new IORException(e);
		}
	}
}
