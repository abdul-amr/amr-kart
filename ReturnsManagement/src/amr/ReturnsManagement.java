package amr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;

import test.EmailUtils;


public class ReturnsManagement {

	private Properties configProp;
	private String token;
	private Integer days;
	private List<FlipkartReturn> flipkartReturns;
	private EmailUtils emailUtils;
	public ReturnsManagement() throws IOException {
		configProp = loadProperties("./code/config.properties");
		flipkartReturns = new ArrayList<>();
		emailUtils = new EmailUtils();
	}
	
	public static void main(String[] args) throws Exception {
		String eamilBody = "";
		ReturnsManagement rM = new ReturnsManagement();
		String sellername = rM.getConfigProp().getProperty("seller");
		String days = rM.getConfigProp().getProperty("days");
		String email = rM.getConfigProp().getProperty("email");
		if(days != null && !"".equals(days)){
			rM.days = new Integer("-"+days);
		}
		String token = rM.getFlipkartToken();
		if(token == null || "".equals(token)){
			System.out.println("Error while getting Flipkart token");
			return;
		}
		rM.token = token;
		System.out.println("Fetching Flipkart Returns");
		List<FlipkartReturn> flipkartReturns = rM.getFlipkartReturns("");
		System.out.println("Fetching Product Details");
		for(FlipkartReturn flipkartReturn : flipkartReturns){
			eamilBody += flipkartReturn.getSku()+" | "+flipkartReturn.getTitle()+" | "+flipkartReturn.getReason()+" | "+flipkartReturn.getComments()+" | "+flipkartReturn.getSubReason()+" | "+flipkartReturn.getOrderId()+"::"+flipkartReturn.getOrderDate()+"\n";
		}
		rM.writeToFile(eamilBody);
		rM.emailUtils.sendEmail(sellername, "Returns", "", eamilBody, null, email);
		System.out.println("Success");
	}
	
	private List<FlipkartReturn> getFlipkartReturns(String newUrl) throws Exception {
		FlipkartReturnsList flipkartReturn = null;
		String url = "https://api.flipkart.net/sellers/v2";
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = null;
		if(newUrl == null || "".equals(newUrl)){
			//1st request
			Date dt = new Date();
			Calendar c = Calendar.getInstance(); 
			c.setTime(dt); 
			c.add(Calendar.DATE, this.days);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			dt = c.getTime();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
			String fromDate = df.format(dt);
			URIBuilder builder = new URIBuilder(url+"/returns");
			builder.setParameter("source", "customer_return").setParameter("createdAfter", fromDate);
			request = new HttpGet(builder.build());
		}else {
			//2nd request
			request = new HttpGet(url+newUrl);
		}
		request.addHeader("Authorization", "Bearer "+this.token);
		request.addHeader("Content-Type", "application/json");
		HttpResponse response = client.execute(request);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		Gson gson = new Gson();
		flipkartReturn = gson.fromJson(result.toString(), FlipkartReturnsList.class);
		if(flipkartReturn != null && flipkartReturn.isHasMore()){
			flipkartReturn = updateReturnedSkus(flipkartReturn);
			flipkartReturns.addAll(flipkartReturn.getReturnItems());
			getFlipkartReturns(flipkartReturn.getNextUrl());
		}else if(flipkartReturn != null && !flipkartReturn.isHasMore()){
			flipkartReturn = updateReturnedSkus(flipkartReturn);
			flipkartReturns.addAll(flipkartReturn.getReturnItems());
			return flipkartReturns;
		}
		return flipkartReturns;
	}
	
	
	private FlipkartReturnsList updateReturnedSkus(FlipkartReturnsList flipkartReturn) throws Exception {
		String orderItemIds = getCommaSeparatedOrderItemIds(flipkartReturn.getReturnItems());
		String url = "https://api.flipkart.net/sellers/v2/orders";
		HttpClient client = HttpClientBuilder.create().build();
		URIBuilder builder = new URIBuilder(url);
		builder.setParameter("orderItemIds", orderItemIds);
		HttpGet request = new HttpGet(builder.build());
		request.addHeader("Authorization", "Bearer "+this.token);
		request.addHeader("Content-Type", "application/json");
		HttpResponse response = client.execute(request);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
			if(line.contains("Content not found")){
//				System.out.println(sku);
			}
//			System.out.println(line);
		}
		Gson gson = new Gson();
		FlipkartOrdersList flipkartOrdersList = gson.fromJson(result.toString(), FlipkartOrdersList.class);
		if(flipkartOrdersList != null){
			for(int i=0; i < flipkartOrdersList.getOrderItems().size(); i++){
				flipkartReturn.getReturnItems().get(i).setSku(flipkartOrdersList.getOrderItems().get(i).getSku());
				flipkartReturn.getReturnItems().get(i).setTitle(flipkartOrdersList.getOrderItems().get(i).getTitle());
				flipkartReturn.getReturnItems().get(i).setPaymentType(flipkartOrdersList.getOrderItems().get(i).getPaymentType());
			}
		}else{
			System.out.println("Cannot Fetch Returned Order Details");
			System.exit(0);
		}
		return flipkartReturn;
	}

	private void writeToFile(String returns) throws IOException {
		
		File fout = new File("./returns.txt");
		if(!fout.exists()){
			fout.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	 
		if(returns != null && !"".equals(returns)){
			bw.write(returns);
		}
		bw.close();
	}

	private String getCommaSeparatedOrderItemIds(List<FlipkartReturn> returnItems) {
		// TODO Auto-generated method stub
		String orderItemIds = "";
		for(FlipkartReturn flipkartReturn : returnItems){
			orderItemIds += flipkartReturn.getOrderItemId()+",";
		}
		return orderItemIds.indexOf(",") != -1 ? orderItemIds.substring(0, orderItemIds.length() -1) : orderItemIds;
	}

	private String getFlipkartToken(){
		try {

			String url = "https://api.flipkart.net/oauth-service/oauth/token?grant_type=client_credentials&scope=Seller_Api";

			URL obj = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);

			conn.setRequestMethod("PUT");

			String userpass = "";
			if(this.getConfigProp().getProperty("seller").equals("tram")){
				userpass = "130940112811a2344041921046577a37565b1" + ":" + "2bb6ac0480e31fada1f0f3703c3b8085b";
			}else if(this.getConfigProp().getProperty("seller").equals("mar")){
				userpass = "156303375008a35918562233b360b3714325a" + ":" + "fb3bde3b1045df21d8d50a3e3dd02117";
			}else if(this.getConfigProp().getProperty("seller").equals("amr")){
				userpass = "13518b3a60b44522b2546a72b4a71b68218bb" + ":" + "33a224e762e3d8c6a63c3d433d963cb48";
			}
			String basicAuth = "Basic "
					+ javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes("UTF-8"));
			conn.setRequestProperty("Authorization", basicAuth);

			String data = "{\"format\":\"json\",\"pattern\":\"#\"}";
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			out.write(data);
			out.close();

			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
//				System.out.println(line);
			}
			Gson gson = new Gson();
			FlipkartToken flipkartToken = gson.fromJson(result.toString(), FlipkartToken.class);
			return flipkartToken.getAccess_token();
		} catch (Exception e) {
			System.out.println("Error while getting Flipkart Token in getFlipkartToken() method");
			e.printStackTrace();
		}
		return null;
	}

	private Properties loadProperties(String fileName) throws IOException {
		// TODO Auto-generated method stub
		//to load application's properties, we use this class
	    Properties mainProperties = new Properties();

	    FileInputStream file;

	    //the base folder is ./, the root of the main.properties file  
//	    String path = "./code/config.properties";//D:/AAA_WORK/Important Batch Files/Flipkart/Listings Status(Get INACTIVE and HIGHER SLA Listings)/code/config.properties

	    //load the file handle for main.properties
	    file = new FileInputStream(fileName);

	    //load all the properties from this file
	    mainProperties.load(file);

	    //we have loaded the properties, so close the file handle
	    file.close();
	    return mainProperties;
	}
	
	public Properties getConfigProp() {
		return configProp;
	}

	public void setConfigProp(Properties configProp) {
		this.configProp = configProp;
	}
}
