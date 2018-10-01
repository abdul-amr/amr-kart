package amr;

import java.io.Serializable;
import java.util.List;

public class ImageShackUploadResponse implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String success;
	String process_time;
	Result result;
	List<String> directLink;
	
	public String getSuccess() {
		return success;
	}
	public void setSuccess(String success) {
		this.success = success;
	}
	public String getProcess_time() {
		return process_time;
	}
	public void setProcess_time(String process_time) {
		this.process_time = process_time;
	}
	public Result getResult() {
		return result;
	}
	public void setResult(Result result) {
		this.result = result;
	}
	public List<String> getDirectLink() {
		return directLink;
	}
	public void setDirectLink(List<String> directLink) {
		this.directLink = directLink;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
