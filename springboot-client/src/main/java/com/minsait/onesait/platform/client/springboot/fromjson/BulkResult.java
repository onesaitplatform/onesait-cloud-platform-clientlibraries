package com.minsait.onesait.platform.client.springboot.fromjson;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public class BulkResult {

	@JsonProperty("nInserted")
	@Getter
	private long nInserted;

	@JsonProperty("inserted")
	@Getter
	private List<String> inserted;
}
