package com.selling.price;

public class SellingPriceCalculator {
	private static int tpPurchase = 25;
	private int defPurchase = 90;
	private int chrPurchase = 45;
	private int crmPurchase = 45;
	private int fcbPurchase = 55;
	private int fcgPurchase = 55;
	private int sgPurchase = 25;
	private static int shipping = 65;
	private static int fixedFee = 5;
	private static int collectionFee = 15;
	private static double gstAmt = 0.18;
	private static double commissionAmt = 0.14;
	
	public static void main(String[] args){
		SellingPriceCalculator.getSellingPrice(25, 40);
	}
	
	public static double getSellingPrice(int purchase,int profit){

		double shippingTax  =  shipping * gstAmt;
		double totalShipping = shipping + shippingTax;

		double fixedFeeTax = fixedFee * gstAmt;
		double fixedFeeTotal = fixedFee + fixedFeeTax;

		double collectionTax = collectionFee * gstAmt;
		double collectionTotal = collectionFee + collectionTax;
		
//		double sellingPrice = profit + purchase + totalGst + totalFixed + totalCollection + totalShipping + totalCommission - shipping;
		//Below formula is derived from above equation
		
		double sellingPrice=  (profit + purchase + ((shipping*gstAmt) - collectionTax - fixedFeeTax - shippingTax - (shipping*commissionAmt*gstAmt)) + fixedFeeTotal + collectionTotal + totalShipping + ((shipping*commissionAmt) + (shipping*commissionAmt*gstAmt)) - shipping) / (1 - (1*gstAmt) + (1*commissionAmt*gstAmt) - (1*commissionAmt) - (1*commissionAmt*gstAmt));

		if((sellingPrice + shipping) > 300){//24% commission is charged for order item value more than 300
			commissionAmt = 0.24;
			sellingPrice=  (profit + purchase + ((shipping*gstAmt) - collectionTax - fixedFeeTax - shippingTax - (shipping*commissionAmt*gstAmt)) + fixedFeeTotal + collectionTotal + totalShipping + ((shipping*commissionAmt) + (shipping*commissionAmt*gstAmt)) - shipping) / (1 - (1*gstAmt) + (1*commissionAmt*gstAmt) - (1*commissionAmt) - (1*commissionAmt*gstAmt));
		}
		if((sellingPrice + shipping) > 500){//fixed fee is 20 for order item value more than 500
			commissionAmt = 0.24;
			fixedFee = 20;
			fixedFeeTax = fixedFee * gstAmt;
			fixedFeeTotal = fixedFee + fixedFeeTax;
			sellingPrice=  (profit + purchase + ((shipping*gstAmt) - collectionTax - fixedFeeTax - shippingTax - (shipping*commissionAmt*gstAmt)) + fixedFeeTotal + collectionTotal + totalShipping + ((shipping*commissionAmt) + (shipping*commissionAmt*gstAmt)) - shipping) / (1 - (1*gstAmt) + (1*commissionAmt*gstAmt) - (1*commissionAmt) - (1*commissionAmt*gstAmt));
		}
		
		
		int finalVal = new Double(Math.floor(sellingPrice)).intValue();
		finalVal = round(finalVal);
		System.out.println(finalVal);
		
		return finalVal;
	}
	
	private static int round(int n)
    {
        // Smaller multiple
        int a = (n / 10) * 10;
          
        // Larger multiple
        int b = a + 10;
      
        // Return of closest of two
        return (n - a > b - n)? b : a;
    }
	
	
//	public static void main(String[] args) throws IOException {
//		CloseableHttpClient client = HttpClients.createDefault();
//		HttpPost httpPost = new HttpPost("https://seller.flipkart.com/napi/listing/commission-calc-mps?sellerId=3ceda4a4ae884f3b");
//	
//		String json = "{\"selling_price\":\"219\",\"fsn\":\"ACCF2JYVYT8ZEZPY\",\"vertical\":\"cases_covers\",\"service_profile\":\"NON_FBF\",\"drop_shipped\":false,\"isFassured\":false,\"packages\":[{\"length\":25,\"breadth\":14,\"height\":3,\"weight\":0.1}],\"shipping_charge_local\":\"30\",\"shipping_charge_zonal\":\"45\",\"shipping_charge_national\":\"65\",\"darwin_tier\":\"bronze\",\"payment_type\":\"prepaid\",\"hsn\":\"39269099\",\"date\":\"2018-05-21\",\"sellerId\":\"3ceda4a4ae884f3b\"}";
//		StringEntity entity = new StringEntity(json);
//		httpPost.setEntity(entity);
//		httpPost.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
//		httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
//		httpPost.setHeader("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8");
//		httpPost.setHeader("Cache-Control", "no-cache");
//		httpPost.setHeader("Connection", "keep-alive");
//		httpPost.setHeader("Content-Type", "application/json");	
//		httpPost.setHeader("fk-csrf-token", "SIGGdTZa-ymKj6aYN2PSB3n17VgfV-f1j6cg");
//		httpPost.setHeader("Host", "seller.flipkart.com");
//		httpPost.setHeader("Origin", "https://seller.flipkart.com");
//		httpPost.setHeader("Pragma", "no-cache");
//		httpPost.setHeader("Referer", "https://seller.flipkart.com/index.html");
//		httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36");
//		httpPost.setHeader("X-LOCATION-ID", "LOC4d6db3af5b954f9d847211717762a9c6");
//		httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
////		httpPost.setHeader("Cookie", "T=TI151724074000009580341414915447675159954912981689358261708001133275; __utmz=143439159.1517286652.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); _mkto_trk=id:021-QVV-957&token:_mch-flipkart.com-1517286652890-38222; _ga=GA1.2.1026410343.1517286652; __utmz=19769839.1518737358.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); __utma=19769839.1026410343.1517286652.1518737358.1518785365.2; aid=Jw9abtMSkkwIU25RWghuNlQXNC9YF8FqyH0VPrRQPAhhsEltf%2F%2FGyg0sYLo8okzl; AMCVS_55CFEDA0570C3FA17F000101%40AdobeOrg=1; s_cc=true; __utmc=143439159; _gid=GA1.2.829933350.1526765877; AMCVS_17EB401053DAF4840A490D4C%40AdobeOrg=1; AMCV_17EB401053DAF4840A490D4C%40AdobeOrg=-227196251%7CMCIDTS%7C17672%7CMCMID%7C83844476049612418904169062746935377798%7CMCAAMLH-1527268183%7C3%7CMCAAMB-1527429892%7C6G1ynYcLPuiQxYZrsz_pkqfLG9yMXBpb2zX5dvJdYQJzPXImdj0y%7CMCOPTOUT-1526832292s%7CNONE%7CMCAID%7CNONE; VID=2.VIB01A276B26DF40A3ABEF4F528829EE0C.1526850073.VS152685007288187421089; NSID=2.SI688F271458314E829BA6F24BD11144F3.1526850073.VIB01A276B26DF40A3ABEF4F528829EE0C; SN=2.VIB01A276B26DF40A3ABEF4F528829EE0C.SI688F271458314E829BA6F24BD11144F3.VS152685007288187421089.1526850077; RT=\"sl=1&ss=1526850068711&tt=11098&obo=0&sh=1526850079824%3D1%3A0%3A11098&dm=flipkart.com&si=undefined&ld=1526850079825&nu=https%3A%2F%2Fwww.flipkart.com%2Fsearch%3F2aa36a31b5d38315c67617c61d5509b3&cl=1526850189513\"; S=d1t16Gz8pBz8/Pz8/GD8/DXA/TrdzuIc+uhppzrEJZ2yGlr4Rrze9SB0qzRpBNNxa6q5EkW6cCPIq+Lrs/EZAjgMHiQ==; connect.sid=s%3A6Pzc-TiV_mSdZJQp0KHAEUG7cXiopeUi.vKJfdWP2Z8fxne0XwH%2FNIgBTV18jI4pDq0LfH2fWfa8; __utma=143439159.1026410343.1517286652.1526849581.1526852398.80; sellerId=3ceda4a4ae884f3b; is_login=true; s_sq=%5B%5BB%5D%5D; AMCV_55CFEDA0570C3FA17F000101%40AdobeOrg=-227196251%7CMCIDTS%7C17673%7CMCMID%7C58244062559055880209143646046636749403%7CMCOPTOUT-1526936180s%7CNONE%7CMCAID%7CNONE; _gat=1; s_ppn=seller%3A%20active%20orders%20%7C%20new; s_nr=1526929277111-Repeat; s_ppvl=seller%253A%2520active%2520orders%2520%257C%2520new%2C100%2C100%2C588%2C1366%2C588%2C1366%2C768%2C1%2CP; s_ppv=https%253A%2F%2Fseller.flipkart.com%2FappV2%253FsellerId%253D3ceda4a4ae884f3b%2523%2Flisting%2FmyListing%2Flive%253FcategoryName%253D%2526superCategoryName%253D%2526verticalName%253D%2526state%253DLIVE%2526internal_state%253D%2526internal_state%253DINACTIVE%2526actual_stock_size%253D%2526from%253D%2526to%253D%2526service_profile%253D%2C27%2C27%2C508%2C1366%2C508%2C1366%2C768%2C1%2CP");
//		
//		CloseableHttpResponse response = client.execute(httpPost);
//		System.out.println(response.toString());
//		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//		StringBuffer result = new StringBuffer();
//		String line = "";
//		while ((line = rd.readLine()) != null) {
//			result.append(line);
//			System.out.println(line);
//		}
//		client.close();
//	}
}
