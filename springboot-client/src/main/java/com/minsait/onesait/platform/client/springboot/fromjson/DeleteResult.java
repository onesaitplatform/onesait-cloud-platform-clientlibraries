package com.minsait.onesait.platform.client.springboot.fromjson;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public class DeleteResult {

	@JsonProperty("nDeleted")
	@Getter
	private long nDeleted;

	@JsonProperty("deleted")
	@Getter
	private List<String> deleted;
}
