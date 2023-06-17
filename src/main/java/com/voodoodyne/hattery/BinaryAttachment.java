package com.voodoodyne.hattery;

import lombok.ToString;
import lombok.Value;

import java.io.InputStream;

/** Used as a param value when user submits binary attachments */
@Value
@ToString(exclude = "data")
public class BinaryAttachment {
	InputStream data;
	String contentType;
	String filename;
}
