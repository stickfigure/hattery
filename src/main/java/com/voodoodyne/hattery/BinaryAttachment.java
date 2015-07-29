package com.voodoodyne.hattery;

import lombok.Data;
import java.io.InputStream;

/** Used as a param value when user submits binary attachments */
@Data
public class BinaryAttachment {
	private final InputStream data;
	private final String contentType;
	private final String filename;
}
