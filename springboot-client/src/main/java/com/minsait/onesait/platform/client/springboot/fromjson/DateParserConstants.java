package com.minsait.onesait.platform.client.springboot.fromjson;

public interface DateParserConstants {

	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	public static final String[] DATE_FORMATS = new String[] { "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
			"yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm'Z'", "yyyy-MM-dd'T'HH:mm",
			"yyyy-MM-dd" };
}
