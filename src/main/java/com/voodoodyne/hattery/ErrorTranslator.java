package com.voodoodyne.hattery;

/**
 * Offers a way of interpreting specific http errors as more application-specific
 * exceptions. This is invoked whenever an HttpException would have been thrown.
 */
public interface ErrorTranslator {
	RuntimeException translate(final HttpException e);
}
