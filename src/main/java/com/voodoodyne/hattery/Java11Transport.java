package com.voodoodyne.hattery;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map.Entry;

/**
 * <p>Transport impl that uses the Java11 HTTP Client.</p>
 *
 * <p>Holds two HttpClient instances, which are shared among all requests so that we get connection
 * pool caching. There are two instances because clients are built with follow/nofollow and this
 * can't be adjusted on a per-request basis (as Hattery allows).</p>
 *
 * <p>This is now the default transport.</p>
 */
@Slf4j
public class Java11Transport implements Transport {

	private final HttpClient normalFollow;
	private final HttpClient neverFollow;

	/** Constructs the transport with basic client configuration */
	public Java11Transport() {
		this(HttpClient.newBuilder());
	}

	/**
	 * Constructs this transport with a customized client configuration.
	 * The builder will be modified with followRedirects() and built() twice;
	 * once to generate a client that follows redirects, once to generate a client that does not follow redirects.
	 */
	public Java11Transport(final HttpClient.Builder clientBuilder) {
		this(
				clientBuilder.followRedirects(Redirect.ALWAYS).build(),
				clientBuilder.followRedirects(Redirect.NEVER).build()
		);
	}

	/**
	 * Construct this transport with a customized HttpClient. Note that we must require
	 * two client instances, one which follows redirects and one which does not follow
	 * redirects. That's because with Hattery, following is a per-request option and the
	 * transport must be able to handle both cases.
	 *
	 * @param normalFollow should be built with {@code followRedirects(Redirect.NORMAL)}
	 * @param neverFollow should be built with {@code followRedirects(Redirect.NEVER)}
	 */
	public Java11Transport(final HttpClient normalFollow, final HttpClient neverFollow) {
		this.normalFollow = normalFollow;
		this.neverFollow = neverFollow;
	}

	/** Override this to add any additional custom configuration for each request */
	protected void configure(final java.net.http.HttpRequest.Builder builder) {
		// default do nothing
	}

	@Override
	public TransportResponse fetch(final HttpRequest request) throws IOException {
		for (int i = 0; i <= request.getRetries(); i++) {
			try {
				return fetchOnce(request);
			} catch (IOException ex) {
				if (i < request.getRetries() && ex instanceof HttpTimeoutException) {
					log.warn("Timeout error, retrying");
				} else {
					throw ex;
				}
			}
		}

		// Logically unreachable code, but the compiler doesn't know that
		return null;
	}

	@SneakyThrows
	private TransportResponse fetchOnce(final HttpRequest request) throws IOException {
		final HttpClient client = request.isFollowRedirects() ? normalFollow : neverFollow;

		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
		request.writeBody(outputStream);
		final BodyPublisher bodyPublisher = (outputStream.size() > 0)
				? BodyPublishers.ofByteArray(outputStream.toByteArray())
				: BodyPublishers.noBody();

		final java.net.http.HttpRequest.Builder requestBuilder = java.net.http.HttpRequest.newBuilder()
				.uri(request.toUrl().toURI())
				.method(request.getMethod(), bodyPublisher);

		if (request.getTimeout() > 0)
			requestBuilder.timeout(Duration.ofMillis(request.getTimeout()));

		for (final Entry<String, String> header : request.getHeaders().entrySet()) {
			requestBuilder.header(header.getKey(), header.getValue());
		}

		if (request.getContentType() != null) {
			requestBuilder.header("Content-Type", request.getContentType());
		}

		configure(requestBuilder);
		final java.net.http.HttpRequest javaRequest = requestBuilder.build();

		final java.net.http.HttpResponse<byte[]> response = client.send(javaRequest, BodyHandlers.ofByteArray());

		return new TransportResponse() {
			@Override
			public int getResponseCode() throws IOException {
				return response.statusCode();
			}

			@Override
			public InputStream getContentStream() throws IOException {
				return new ByteArrayInputStream(response.body());
			}

			@Override
			public byte[] getContentBytes() throws IOException {
				return response.body();
			}

			@Override
			public ListMultimap<String, String> getHeaders() throws IOException {
				final ListMultimap<String, String> headers = ArrayListMultimap.create();

				for (final Entry<String, List<String>> header : response.headers().map().entrySet()) {
					headers.putAll(header.getKey(), header.getValue());
				}

				return headers;
			}
		};
	}
}
