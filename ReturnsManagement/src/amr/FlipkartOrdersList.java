package amr;

import java.io.Serializable;
import java.util.List;

public class FlipkartOrdersList implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<FlipkartOrder> orderItems;

	public List<FlipkartOrder> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(List<FlipkartOrder> orderItems) {
		this.orderItems = orderItems;
	}
}
