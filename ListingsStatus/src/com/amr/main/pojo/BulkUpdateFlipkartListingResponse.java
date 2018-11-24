package com.amr.main.pojo;

import java.util.List;
import java.util.Map;

public class BulkUpdateFlipkartListingResponse {

	private String status;
	private List<Map<String, Object>> response;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public  List<Map<String, Object>> getResponse() {
		return response;
	}
	public void setResponse( List<Map<String, Object>> response) {
		this.response = response;
	}
	
}
