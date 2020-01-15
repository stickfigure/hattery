package com.voodoodyne.hattery;

import com.google.common.collect.ListMultimap;
import java.io.IOException;
import java.io.InputStream;

/**
 * Returned by transports when requests are executed
 */
public interface TransportResponse {
	/** The http response code */
	int getResponseCode() throws IOException;

	/** The body content of the response. Might be more efficient, might not */
	InputStream getContentStream() throws IOException;

	/** Raw bytes of response. Might be more efficient, might not */
	byte[] getContentBytes() throws IOException;

	/** Response headers */
	ListMultimap<String, String> getHeaders() throws IOException;
}
