package com.amr.main.pojo;

import java.io.Serializable;
import java.util.List;

public class BulkFlipkartListings implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<FlipkartListing> listings;
	
	public List<FlipkartListing> getListings() {
		return listings;
	}
	public void setListings(List<FlipkartListing> listings) {
		this.listings = listings;
	}
}
