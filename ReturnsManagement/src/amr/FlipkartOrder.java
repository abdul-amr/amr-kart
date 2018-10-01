package amr;

import java.io.Serializable;

public class FlipkartOrder implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String orderItemId;
	private String orderId;
	private String hsn;
	private String status;
	private boolean hold;
	private String orderDate;
	private String dispatchAfterDate;
	private String dispatchByDate;
	private String deliverByDate;
	private String updatedAt;
	private Integer sla;
	private Integer quantity;
	private String title;
	private String listingId;
	private String fsn;
	private String sku;
	private String shippingPincode;
	private String dispatchServiceTier;//need
	private String paymentType;//need
	private String shipmentId;
	private String shipmentType;
	private String serviceProfile;
	public String getOrderItemId() {
		return orderItemId;
	}
	public void setOrderItemId(String orderItemId) {
		this.orderItemId = orderItemId;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getHsn() {
		return hsn;
	}
	public void setHsn(String hsn) {
		this.hsn = hsn;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public boolean isHold() {
		return hold;
	}
	public void setHold(boolean hold) {
		this.hold = hold;
	}
	public String getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}
	public String getDispatchAfterDate() {
		return dispatchAfterDate;
	}
	public void setDispatchAfterDate(String dispatchAfterDate) {
		this.dispatchAfterDate = dispatchAfterDate;
	}
	public String getDispatchByDate() {
		return dispatchByDate;
	}
	public void setDispatchByDate(String dispatchByDate) {
		this.dispatchByDate = dispatchByDate;
	}
	public String getDeliverByDate() {
		return deliverByDate;
	}
	public void setDeliverByDate(String deliverByDate) {
		this.deliverByDate = deliverByDate;
	}
	public String getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}
	public Integer getSla() {
		return sla;
	}
	public void setSla(Integer sla) {
		this.sla = sla;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getListingId() {
		return listingId;
	}
	public void setListingId(String listingId) {
		this.listingId = listingId;
	}
	public String getFsn() {
		return fsn;
	}
	public void setFsn(String fsn) {
		this.fsn = fsn;
	}
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	public String getShippingPincode() {
		return shippingPincode;
	}
	public void setShippingPincode(String shippingPincode) {
		this.shippingPincode = shippingPincode;
	}
	public String getDispatchServiceTier() {
		return dispatchServiceTier;
	}
	public void setDispatchServiceTier(String dispatchServiceTier) {
		this.dispatchServiceTier = dispatchServiceTier;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getShipmentId() {
		return shipmentId;
	}
	public void setShipmentId(String shipmentId) {
		this.shipmentId = shipmentId;
	}
	public String getShipmentType() {
		return shipmentType;
	}
	public void setShipmentType(String shipmentType) {
		this.shipmentType = shipmentType;
	}
	public String getServiceProfile() {
		return serviceProfile;
	}
	public void setServiceProfile(String serviceProfile) {
		this.serviceProfile = serviceProfile;
	}
}
