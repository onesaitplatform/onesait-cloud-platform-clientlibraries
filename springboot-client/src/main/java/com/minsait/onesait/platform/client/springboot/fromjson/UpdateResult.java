package com.minsait.onesait.platform.client.springboot.fromjson;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public class UpdateResult {

	@JsonProperty("nModified")
	@Getter
	private long nModified;

	@JsonProperty("modified")
	@Getter
	private List<String> modified;

}
