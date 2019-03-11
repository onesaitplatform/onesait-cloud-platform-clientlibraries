package com.minsait.onesait.platform.client.springboot.fromjson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import jline.internal.Log;

public class DateDeserializer extends StdDeserializer<Date> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String[] DATE_FORMATS = new String[] {
			"yyyy-MM-dd'T'HH:mm:ss'Z'",
	        "yyyy-MM-dd'T'HH:mm:ss",
	        "yyyy-MM-dd'T'HH:mm'Z'",
	        "yyyy-MM-dd'T'HH:mm",
	        "yyyy-MM-dd"
	    };

	public DateDeserializer() {
        this(null);
    }

    public DateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        final String date = node.textValue();

        for (String DATE_FORMAT : DATE_FORMATS) {
            try {
                return new SimpleDateFormat(DATE_FORMAT).parse(date);
            } catch (ParseException e) {
            	Log.error("Error parsing dates: ", e.getMessage());
            }
        }
        throw new JsonParseException(jp, "Unparseable date: \"" + date + "\". Supported formats: " + Arrays.toString(DATE_FORMATS));
    }
}
