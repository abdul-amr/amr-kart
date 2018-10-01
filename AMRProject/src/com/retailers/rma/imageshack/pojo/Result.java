package com.retailers.rma.imageshack.pojo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Result  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<Map<String, Object>> images;

	public List<Map<String, Object>> getImages() {
		return images;
	}

	public void setImages(List<Map<String, Object>> images) {
		this.images = images;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
