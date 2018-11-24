package test;

import java.io.Serializable;
import java.util.Map;

public class FlipkartListing implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String listingId;
	private String skuId;
	private String fsn;
	/*
	  	"zonal_shipping_charge":"40"
		"actual_stock_count":"0"
		"seller_listing_state":"current"
		"mrp":"900"
		"listing_status":"ACTIVE"
		"fk_release_date":"2016-01-26 19:11:51"
		"fulfilled_by":"seller"
		"selling_price":"199"
		"sku_id":"scoobyDu24"
		"local_shipping_charge":"20"
		"national_shipping_charge":"70"
		"stock_count":"1"
		"procurement_sla":"1"
		"inventory_count":"0"	
	*/
	private Map<String, String> attributeValues;
	private String listingValidations;
	
	
	public FlipkartListing(String sku, String fsn, String listingId){
		this.skuId = sku;
		this.fsn = fsn;
		this.listingId = listingId;
	}
	public FlipkartListing(String sku){
		this.skuId = sku;
	}
	
	
	public String getListingId() {
		return listingId;
	}
	public void setListingId(String listingId) {
		this.listingId = listingId;
	}
	public String getSkuId() {
		return skuId;
	}
	public void setSkuId(String skuId) {
		this.skuId = skuId;
	}
	public String getFsn() {
		return fsn;
	}
	public void setFsn(String fsn) {
		this.fsn = fsn;
	}
	public Map<String, String> getAttributeValues() {
		return attributeValues;
	}
	public void setAttributeValues(Map<String, String> attributeValues) {
		this.attributeValues = attributeValues;
	}
	public String getListingValidations() {
		return listingValidations;
	}
	public void setListingValidations(String listingValidations) {
		this.listingValidations = listingValidations;
	}
	
	
}
