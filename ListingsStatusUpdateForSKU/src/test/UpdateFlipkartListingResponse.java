package test;

import java.util.Map;

public class UpdateFlipkartListingResponse {

	private String status;
	private Map<String, Object> response;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public  Map<String, Object> getResponse() {
		return response;
	}
	public void setResponse( Map<String, Object> response) {
		this.response = response;
	}
	
}
