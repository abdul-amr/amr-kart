package amr;

import java.io.Serializable;
import java.util.List;

public class FlipkartReturnsList implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean hasMore;
	private String nextUrl;
	private List<FlipkartReturn> returnItems;
	public boolean isHasMore() {
		return hasMore;
	}
	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}
	public List<FlipkartReturn> getReturnItems() {
		return returnItems;
	}
	public void setReturnItems(List<FlipkartReturn> returnItems) {
		this.returnItems = returnItems;
	}
	public String getNextUrl() {
		return nextUrl;
	}
	public void setNextUrl(String nextUrl) {
		this.nextUrl = nextUrl;
	}
}
