package com.voodoodyne.hattery.test;

import lombok.Data;

/**
 * Response from the validate.jsontest.com service
 */
@Data
public class ValidateResponse {
	private final String object_or_array;
	private final boolean empty;
	private final long parse_time_nanoseconds;
	private final boolean validate;
	private final int size;
}
