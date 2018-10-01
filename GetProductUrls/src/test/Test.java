package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

public class Test {
	
	private static int i =0;

	public static void main(String[] args) throws UnsupportedOperationException, IOException {
		// TODO Auto-generated method stub
		if("GetUrls".equals(args[0])){
			List<String> newUrls = new ArrayList<>();
			int i=1;
			
			try (BufferedReader br = new BufferedReader(new FileReader("./fsnlist.txt"))) {
				String line = "";
				while ((line = br.readLine()) != null) {
					if (!"".equals(line.trim())) {
//						System.out.println(line);
						String[] arr = line.split("::");
						String fsn = arr[0];
						String sku = arr[1];
						sku = sku.replace("xld-", "xlm-").replace("xol-", "xom-").replace("mzt-", "mzm-");
						String newUrl = extractProductImages(fsn);
						System.out.println("status\t"+i++);
						newUrls.add(newUrl+"\t"+sku);
						if(newUrls.size() == 25){
							writeToFile(newUrls, true, "./newUrls.txt");
							newUrls = new ArrayList<>();
						}
					}
				}
			}
			
			writeToFile(newUrls,true, "./newUrls.txt");
		}else if("Split".equals(args[0])){
			List<String> missed = new ArrayList<>();
			List<String> screenGuards = new ArrayList<>();
			List<String> combos = new ArrayList<>();
			List<String> covers = new ArrayList<>();
			try (BufferedReader br = new BufferedReader(new FileReader("./newUrls.txt"))) {
				String line = "";
				while ((line = br.readLine()) != null) {
					if (!"".equals(line.trim())) {
						String[] tokens = line.split("\t");
						if(tokens[1].equals("NA")){
							missed.add(tokens[0]+"::"+tokens[2]);
						}else if(tokens[1].contains("screen-guard")){
							screenGuards.add(line);
						}else if(tokens[1].contains("tempered")){
							screenGuards.add(line);
						}else if(tokens[1].contains("accessory-combo")){
							combos.add(line);
						}else{
							covers.add(line);
						}
					}
				}
			}
			if(missed.size() > 0){
				writeToFile(missed,true, "./missed.txt");
			}
			if(screenGuards.size() > 0){
				writeToFile(screenGuards,true, "./screenguard.txt");
			}
			if(combos.size() > 0){
				writeToFile(combos,true, "./combos.txt");
			}
			if(covers.size() > 0){
				writeToFile(covers,true, "./cases_covers.txt");
			}
		}
		
	}
	
	private static void writeToFile(List<String> list, boolean append, String fileName) throws IOException {
		// TODO Auto-generated method stub
		File fout = new File(fileName);
		if(!fout.exists()){
			fout.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(fout, append);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	 
		for(String entry : list){
	    	bw.write(entry);
			bw.newLine();
	    }
		bw.close();
	}
	
	private static String extractProductImages(String fsn) throws UnsupportedOperationException, IOException {
		// TODO Auto-generated method stub
		
		try {
			String url = "https://www.flipkart.com/mozette-back-cover-oneplus-3t/p/itmeqq85dkzhpvq8?pid="+fsn;
	
			HttpClient client = HttpClients.custom()
			        .setDefaultRequestConfig(RequestConfig.custom()
			                .setCookieSpec(CookieSpecs.STANDARD).build())
			            .build();
			HttpPost request = new HttpPost(url);
	
			// add request header
			request.setHeader("Host", "www.flipkart.com");
			request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");
			request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
			request.setHeader("Accept-Language", "en-GB,en-US;q=0.8,en;q=0.6");
			request.setHeader("Accept-Encoding", "gzip, deflate, br");
			request.setHeader("Upgrade-Insecure-Requests", "1");
			request.setHeader("Cookie", "s_nr=1509898620743-Repeat; _ga=GA1.2.993031245.1509881646; AMCV_55CFEDA0570C3FA17F000101%40AdobeOrg=-227196251%7CMCIDTS%7C17476%7CMCMID%7C83970010071232145939210776659443284150%7CMCOPTOUT-1509905820s%7CNONE%7CMCAID%7CNONE; _mkto_trk=id:021-QVV-957&token:_mch-flipkart.com-1509881646225-72134; AMCVS_17EB401053DAF4840A490D4C%40AdobeOrg=1; AMCV_17EB401053DAF4840A490D4C%40AdobeOrg=-227196251%7CMCIDTS%7C17482%7CMCMID%7C41877339198696453240709315239410794609%7CMCAAMLH-1511036920%7C3%7CMCAAMB-1511036920%7CRKhpRz8krg2tLO6pguXWp5olkAcUniQYPHaMWWgdJ3xzPWQmdj0y%7CMCOPTOUT-1510439321s%7CNONE%7CMCAID%7CNONE; T=SD.47717178-ef00-4b5c-aac8-14af9df5b6a0.1509881636994; SN=2.VI817BAE406D9341139BD28C5E9349EE4D.SIBD04436492254818B102BA4860F38C28.VS31502C7F2B59418CA7D4D59C736E5A0B.1510432199; gpv_pn=MobileAccessory%3AXOLDA%20Back%20Cover%20for%20Xiaomi%20Redmi%20Note%203; gpv_pn_t=Product%20Page; s_cc=true");
			request.setHeader("Connection", "keep-alive");
			
			
			HttpResponse response = null;
				response = client.execute(request);
	
	
			BufferedReader rd = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent()));
	
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
				if(line.contains("<link rel=\"canonical\"")){
					String printS = line.replace("<link rel=\"canonical\" href=\"", "").replace("\"/>","").trim();
					printS = printS+"?pid="+fsn;
					System.out.println(printS);
					return fsn+"\t"+printS;
				}
			}
			i=0;
			return fsn+"\tNA";
		} catch (Exception e) {
			i++;
			if(i < 4){
				return extractProductImages(fsn);
			}
		}
		return null;
		
	}
}
