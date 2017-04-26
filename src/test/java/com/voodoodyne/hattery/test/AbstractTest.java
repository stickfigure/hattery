package com.voodoodyne.hattery.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractTest {

	protected ByteArrayOutputStream outContent = null;
	protected ByteArrayOutputStream errContent = null;
	
	protected void configSystemOutForTest() {
		outContent = new ByteArrayOutputStream();
		errContent = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outContent));
	    System.setErr(new PrintStream(errContent));
	}
	
	protected void resetSystemOutTest() {
		System.setOut(null);
	    System.setErr(null);
	}
	
	protected static class Foo {
		@Getter @Setter private String foo;
	}
	
	protected static class Validate {
		@Getter @Setter private String object_or_array;
		@Getter @Setter private boolean empty;
		@Getter @Setter private long parse_time_nanoseconds;
		@Getter @Setter private boolean validate;
		@Getter @Setter private int size;
	}
}
