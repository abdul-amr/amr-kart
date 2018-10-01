package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.amr.main.ListingsStatus;
import com.google.gson.Gson;

import au.com.bytecode.opencsv.CSVReader;

public class SKUUpdate {
	
	private Properties configProp;
	private List<String> skuList;
	public BulkFlipkartListings bulkFlipkartListings;
	public List<String> failedObjects;
	public List<String> varieties;
	private int attempt = 0;
	private Map<String, String> priceMap;
	private Map<String, String> purchaseMap;
	public Map<String, String> skuVsPhoneMap;
	public Set<String> uniqueMobiles;
	private int totalCount=0;
	private int currentCount=0;
	private String sla;
	private String procurement;
	private String status;
	private boolean shippingUpdate;
	private boolean dimensionsUpdate;
	private boolean priceUpdate;
	private boolean statusUpdate;
	private boolean slaUpdate;
	private String token;
	private EmailUtils emailUtils;
	private List<String> filePaths;
	private List<String> possibleVarieties;
	
	
	public Properties getConfigProp() {
		return configProp;
	}

	public void setConfigProp(Properties configProp) {
		this.configProp = configProp;
	}

	public List<String> getSkuList() {
		return skuList;
	}

	public void setSkuList(List<String> skuList) {
		this.skuList = skuList;
	}

	public SKUUpdate() throws IOException{
		configProp = loadProperties("./code/config.properties");
		this.bulkFlipkartListings = new BulkFlipkartListings();
		this.bulkFlipkartListings.setListings(new ArrayList<>());
		failedObjects = new ArrayList<>();
		this.priceMap = loadPriceMap();
		this.purchaseMap = loadpurchaseMapMap();
		skuVsPhoneMap = new HashMap<>();
		uniqueMobiles = new TreeSet<>();
		emailUtils = new EmailUtils();
		filePaths = new ArrayList<>();
		filePaths.add("./skuList.txt");
		filePaths.add("./mobileSearchResult.txt");
		filePaths.add("./failedSkus.txt");
		filePaths.add("./skippedModelsOfCombos.txt");
		filePaths.add("./code/config.properties");
		possibleVarieties = loadVarieties();
	}

	private Map<String, String> loadPriceMap() {
		String pricingMap = this.getConfigProp().getProperty("price");
		if(pricingMap == null || "".equals(pricingMap)){
			return null;
		}
		String[] rates = pricingMap.split(",");
		Map<String, String> rateMap  = new HashMap<>();
		for(String rate : rates){
			rateMap.put(rate.split(":")[0], rate.split(":")[1]);
		}
		return rateMap;
	}
	
	public List<String> loadVarieties() throws IOException {
		List<String> varieties = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new FileReader("./code/varieties.txt"))) {
		    String line = br.readLine();
		    if(line != null && line.contains("varieties=")){
		    	line = line.replace("varieties=", "");
		    	if(!line.isEmpty()){
		    		String[] arr = line.split(",");
		    		for(String each : arr){
		    			if(each != null && !"".equals(each)){
		    				varieties.add(each);
		    			}
		    		}
		    	}
		    }
		}
		return varieties;
	}

	private Map<String, String> loadpurchaseMapMap() {
		String pricingMap = this.getConfigProp().getProperty("purchase");
		String[] rates = pricingMap.split(",");
		Map<String, String> rateMap  = new HashMap<>();
		for(String rate : rates){
			rateMap.put(rate.split(":")[0], rate.split(":")[1]);
		}
		return rateMap;
	}

	public static void main(String[] args) throws Exception {
		SKUUpdate skuUpdate = new SKUUpdate();
		skuUpdate.sla = skuUpdate.getConfigProp().getProperty("sla");
		String sellerName = skuUpdate.getConfigProp().getProperty("seller");
		skuUpdate.procurement = skuUpdate.getConfigProp().getProperty("procurement");
		skuUpdate.status = skuUpdate.getConfigProp().getProperty("status");
		String varieties = skuUpdate.getConfigProp().getProperty("variety");
		skuUpdate.varieties = Arrays.asList(varieties.split(","));
		String mobileNamesCommaSeparated = skuUpdate.getConfigProp().getProperty("mobiles");
		boolean doProceed = skuUpdate.validateInputData();
		if(!doProceed){
			return;
		}
		
		if(args[0].equals("Update")){
			skuUpdate.skuList = skuUpdate.loadSkuList();
			System.out.println("Updating the SKUs for models : "+varieties);
			if(varieties == null || "".equals(varieties)){
				System.out.println("Please select a variety tp,fcg,fcb,def,sg,crm,chr");
				return;
			}
			String variety[] = varieties.split(",");
			if(skuUpdate.skuList.size() > 0){
				skuUpdate.token = skuUpdate.getFlipkartToken();
				if(StringUtils.isBlank(skuUpdate.token)){
					System.out.println("Error while getting Flipkart token");
					return;
				}else{
					System.out.println("Token generated successfully");
				}
				skuUpdate.totalCount = skuUpdate.skuList.size();
				for(String sku : skuUpdate.skuList){
					sku = sku.split("::")[0];
//					for(String varty : variety){
//						if(sku.trim().contains("-"+varty)){
							skuUpdate.updateInventory(sku.trim());
//						}
//					}
				}
			}
			skuUpdate.saveThefailedObjects();
			skuUpdate.emailUtils.sendEmail(sellerName, "ExtractAndUpdateSKUs", "3_UpdateSKUs", "For Mobiles : "+mobileNamesCommaSeparated+"\nFor Models : "+varieties+"\nUpdated SKUs : "+skuUpdate.skuList.size()+"\n"+"Failed SKUs : "+skuUpdate.failedObjects.size(), skuUpdate.filePaths,"");
			System.out.println("SUCCESS !!!");
		}else if(args[0].equals("Extract")){
			System.out.println("Extracting Skus for the selected Phones");
			skuUpdate.skuVsPhoneMap = skuUpdate.loadSkuVsPhoneMapFromCentralLocation();
			String inventoryFileName = skuUpdate.getConfigProp().getProperty("inventoryfile");
			String inactiveFileName = skuUpdate.getConfigProp().getProperty("inactiveinventoryfile");
			List<String> mobileList = new ArrayList<>();
			for(String eMobile : mobileNamesCommaSeparated.split(",")){
				mobileList.add(eMobile.trim().toLowerCase());
			}
			List<String> skuList = null;
			ListingsStatus listingsStatus = new ListingsStatus();
			listingsStatus.skuVsPhoneMap = skuUpdate.skuVsPhoneMap;
			listingsStatus.extractProcurementType = "ALL";
			listingsStatus.extractSLA = "ALL";
			listingsStatus.extractStatus = "ALL";
			if(inventoryFileName.endsWith(".csv")){//CSV File
				Map<String, TreeMap<String, String>> list = listingsStatus.getUniqueMobileNamesCSV(new File(inventoryFileName));
				skuList = skuUpdate.extractSKUsFromCSVFile(inventoryFileName,mobileList,list);
			}else if(inventoryFileName.endsWith(".xls")){//Excel File
				Map<String, TreeMap<String, String>> list = listingsStatus.getUniqueMobileNames(new File(inventoryFileName));
				skuList = skuUpdate.extractSKUsFromExcelFile(inventoryFileName,mobileList,list);
			}
			if(inactiveFileName != null && !inactiveFileName.equals("") && inactiveFileName.endsWith(".csv")){//CSV File
				Map<String, TreeMap<String, String>> list = listingsStatus.getUniqueMobileNamesCSV(new File(inactiveFileName));
				List<String> inactiveSkuList = skuUpdate.extractSKUsFromCSVFile(inactiveFileName,mobileList,list);
				skuList.addAll(inactiveSkuList);
			}else if(inactiveFileName != null && !inactiveFileName.equals("") && inactiveFileName.endsWith(".xls")){//Excel File
				Map<String, TreeMap<String, String>> list = listingsStatus.getUniqueMobileNames(new File(inactiveFileName));
				List<String> inactiveSkuList = skuUpdate.extractSKUsFromExcelFile(inactiveFileName,mobileList,list);
				skuList.addAll(inactiveSkuList);
			}
			if(skuList != null && skuList.size() > 0){
				writeToFile(skuList);
				System.out.println("Total SKUs found for the selected Phones : "+skuList.size());
				skuUpdate.emailUtils.sendEmail(sellerName, "ExtractAndUpdateSKUs", "2_ExtractSKUsOfMobiles", "SKUs Found : "+skuList.size(), skuUpdate.filePaths,"");
				System.out.println("SUCCESS !!!!");
			}else {
				writeToFile(skuList);
				skuUpdate.emailUtils.sendEmail(sellerName, "ExtractAndUpdateSKUs", "2_ExtractSKUsOfMobiles", "No Skus Found", skuUpdate.filePaths,"");
				System.out.println("No Macthes Found for the selected mobile in config file : "+mobileNamesCommaSeparated);
			}
		}else if(args[0].equals("SearchMobiles")){
			List<String> resultMobiles = new ArrayList<>();
			skuUpdate.uniqueMobiles = skuUpdate.loadUniqueMobilesFromCentralLocation();
			if(skuUpdate.uniqueMobiles.size() == 0){
				System.out.println("Issue loading Unique Mobile Names from location : "+skuUpdate.getConfigProp().getProperty("uniquemobiles"));
				return;
			}
			String inventoryFileName = skuUpdate.getConfigProp().getProperty("mobiles");
			if(inventoryFileName != null || !"".equals(inventoryFileName)){
				for(String eachUniqueMobile : skuUpdate.uniqueMobiles){
					for(String eachName : inventoryFileName.split(",")){
						if(eachUniqueMobile.trim().toLowerCase().contains(eachName.trim().toLowerCase())){
							resultMobiles.add(eachUniqueMobile);
						}
					}
				}
			}else{
				System.out.println("Incorrect or Empty value of mobiles in config file");
			}
			if(resultMobiles.size() == 0){
				skuUpdate.emailUtils.sendEmail(sellerName, "ExtractAndUpdateSKUs","1_SearchMobileNames","No Mobiles Found",skuUpdate.filePaths,"");
				System.out.println("No Match Found !!!!");
			}else {
				skuUpdate.emailUtils.sendEmail(sellerName, "ExtractAndUpdateSKUs","1_SearchMobileNames",resultMobiles.toString(),skuUpdate.filePaths,"");
				System.out.println("SUCCESS !!! Total Matches Found : "+resultMobiles);
				System.out.println("Please check the results in mobileSearchResult.txt File");
				writeToFileResult(resultMobiles);
			}
		}
	}

	private String getFlipkartToken() {
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

	private boolean validateInputData() {
		// TODO Auto-generated method stub
		String mobiles = configProp.getProperty("mobiles");
		String shippinglocal = configProp.getProperty("shippinglocal");
		String variety = configProp.getProperty("variety");
		String shippingzonal = configProp.getProperty("shippingzonal");
		String shippingnational = configProp.getProperty("shippingnational");
		String length = configProp.getProperty("length");
		String breadth = configProp.getProperty("breadth");
		String height = configProp.getProperty("height");
		String weight = configProp.getProperty("weight");
		String sellingprice = configProp.getProperty("sellingprice");
		String status = configProp.getProperty("status");
		String procurement = configProp.getProperty("procurement");
		String sla = configProp.getProperty("sla");
		String inventoryFileName = configProp.getProperty("inventoryfile");

		
		if(inventoryFileName == null || "".equals(inventoryFileName) || !(inventoryFileName.endsWith(".csv") || inventoryFileName.endsWith(".xls"))){
			System.out.println("Incorrect Inventory/Listings File Name ");
			return false;
		}else{
			File listingsFle = new File(inventoryFileName);
			if(!listingsFle.exists()){
				System.out.println("Inventory/Listings File does not exists");
				return false;
			}
		}
		this.shippingUpdate = !"".equals(shippinglocal) || 
				!"".equals(shippingzonal) || 
				!"".equals(shippingnational);
		
		this.dimensionsUpdate = !"".equals(length) || 
				!"".equals(breadth) || 
				!"".equals(height) || 
						!"".equals(weight);
		
		this.priceUpdate = "Y".equals(sellingprice);
		this.statusUpdate = !"".equals(status);
		this.slaUpdate = !"".equals(procurement) || !"".equals(sla);
		
		//No Two conditions must be true
		String isTwoUpdates = this.shippingUpdate+"-"+this.dimensionsUpdate+"-"+this.priceUpdate+"-"+this.statusUpdate+"-"+this.slaUpdate;
		if(isTwoUpdates.indexOf("true") != isTwoUpdates.lastIndexOf("true")){
			System.out.println("Please update either Status or SLA or Price or Shipping or Dimensions");
			return false;
		}
		
		if(this.slaUpdate && ("".equals(procurement) || "".equals(sla))){
			System.out.println("Procurement and SLA should be changed together. One among them cannot be empty");
			return false;
		}
		
		//
		if(this.dimensionsUpdate && (!StringUtils.isNumeric(length) || !StringUtils.isNumeric(breadth) || !StringUtils.isNumeric(height) || !StringUtils.isNumeric(weight)) ){
			System.out.println("Dimensions are not Numeric");
			return false;
		}
		if(this.shippingUpdate && (!StringUtils.isNumeric(shippinglocal) || !StringUtils.isNumeric(shippingnational) || !StringUtils.isNumeric(shippingzonal)) ){
			System.out.println("Shipping details are not Numeric");
			return false;
		}
		
		//Validate input Data
		if(this.statusUpdate && !status.equals("ACTIVE") && !status.equals("INACTIVE")){
			System.out.println("Status can only be ACTIVE or INACTIVE");
			return false;
		}
		if(this.slaUpdate && !procurement.equals("EXPRESS") && !procurement.equals("REGULAR") && !procurement.equals("DOMESTIC")){
			System.out.println("procurement can only be EXPRESS or REGULAR or DOMESTIC");
			return false;
		}
		if(this.priceUpdate && !sellingprice.equals("Y")){
			System.out.println("sellingprice can only be Y");
			return false;
		}
		if(this.dimensionsUpdate && new Integer(length) != 25 && new Integer(breadth) != 14 && new Integer(height) != 3 && new Integer(weight) != 0.1){
			System.out.println("Dimensions can only be of Length : 25, Breadth : 14, Height : 3, Weight : 0.1");
			return false;
		}
		if(this.shippingUpdate && new Integer(shippinglocal) > 35 && new Integer(shippingnational) > 65 && new Integer(shippingzonal) > 45){
			System.out.println("Shipping charges cannot exceed National : 65, Zonal : 45, Local : 35");
			return false;
		}
		
		if(mobiles == null || mobiles.equals("")){
			System.out.println("mobiles cannot be empty");
			return false;
		}
		
		//checking correct varieties or models
		if(variety == null || variety.equals("")){
			System.out.println("Variety cannot be empty");
			return false;
		}else if(mobiles.equals("all") && variety.equals("all")){
			System.out.println("You cannot fetch all varieties of all mobiles");
			return false;
		}else if("all".equals(mobiles) && variety.indexOf(",") != -1){
			System.out.println("You cannot fetch multiple varieties for all mobiles");
			return false;
		}else if("all".equals(variety) && mobiles.indexOf(",") != -1){
			System.out.println("You cannot fetch all varieties for multiple mobiles");
			return false;
		}else if(variety.indexOf(",") == -1 && (!possibleVarieties.contains(variety.trim()) && !variety.trim().equals("all"))){
			System.out.println("Variety cannot be empty");
			return false;
		}else if(variety.indexOf(",") != -1){
			for(String varty : variety.split(",")){
				if(!possibleVarieties.contains(varty.trim())){
					System.out.println(varty+" variety is incorrect");
					return false;
				}
				
			}
		}
		return true;
	}

	private Set<String> loadUniqueMobilesFromCentralLocation() throws IOException {
		String folderName = this.getConfigProp().getProperty("uniquemobiles");
		
		Set<String> uniqueMobileNames = new TreeSet<>();
		File folder = new File(folderName);
		if(!folder.exists() || !folder.isDirectory()){
			System.out.println("Incorrect folder name mentioned in config file \"uniquemobiles\" .... Exiting ");
			System.exit(0);
		}
		BufferedReader br1 = null;
        String sCurrentLine;
		
		File[] files = folder.listFiles();
		for(File file : files){
			if(file.getName().endsWith(".txt")){
				br1 = new BufferedReader(new FileReader(file));
		        while ((sCurrentLine = br1.readLine()) != null) {
		            if(sCurrentLine != null && !"".equals(sCurrentLine)){
		            	uniqueMobileNames.add(sCurrentLine.trim());
		            }
		        }
			}
		}
		return uniqueMobileNames;
	}

	private List<String> extractSKUsFromExcelFile(String inventoryFileName, List<String> mobileList, Map<String, TreeMap<String, String>> list2) {
		// TODO Auto-generated method stub
		FileInputStream file = null;
		List<String> skuList = new ArrayList<>();
		HSSFWorkbook workbook = null;
		try {
			Map<String, TreeMap<String, String>> list = new TreeMap<>();
				file = new FileInputStream(new File(inventoryFileName));
			    workbook = new HSSFWorkbook(file);
			    HSSFSheet sheet = workbook.getSheetAt(0);
			    boolean flag = true;
			    int rowPOinter = 2;
			    while(flag){
				    Row row = sheet.getRow(rowPOinter);
				    if(row != null){
				    	String sku = null;
				    	String mobile = null;
				    	String listingsId = null;
					    for(int cellPointer=1 ; cellPointer < 25; cellPointer++) {
					    	Cell cell = row.getCell(cellPointer);
							if(cellPointer == 1){
					    		cell.setCellType(Cell.CELL_TYPE_STRING);
								listingsId = !"".equals(cell.getStringCellValue()) ? cell.getStringCellValue() : "";
							}
					    	if(cellPointer == 5){
//					    		if("".equals(mobileName)){
//					    			mobileName = null;
//					    			continue;
//					    		}
					    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    		sku = cell.getStringCellValue();
					    		if(sku != null && !"".equals(sku)){
					    			mobile = this.skuVsPhoneMap.get(sku);
					    		}
					    		if(mobile == null){
					    			System.out.println("Missing Mobile Name for Sku : \t"+sku);
					    			System.exit(0);
	//						    	if(sku.indexOf("-sg") != -1 && sku.split("-").length == 3){
	//						    		mobile = mobileName.substring(mobileName.indexOf("for ")+4,mobileName.length()).trim();
	//						    	}else{
	//						    		mobile = mobileName.substring(mobileName.indexOf("for ")+4,mobileName.length()).trim();
	//						    		mobile = mobile.substring(0,mobile.indexOf("(")-1).trim();
	//						    	}
					    		}else if(mobileList.contains(mobile.trim().toLowerCase()) || (mobileList.size() == 1 && mobileList.contains("all"))){
					    			String skuToken[] = sku.split("-");
					    			
					    			if(skuToken.length == 3 && (this.varieties.contains(skuToken[1]) || (this.varieties.size() == 1 && this.varieties.contains("all")))){
					    				//if it is single and exists in models then add in skuList
					    				skuList.add(sku+"::"+mobile+"::"+listingsId);
					    			}else if(skuToken.length == 4 && StringUtils.isBlank(this.status) && StringUtils.isBlank(this.sla) && (this.varieties.contains(skuToken[1]) || this.varieties.contains(skuToken[2]) || (this.varieties.size() == 1 && this.varieties.contains("all")))){
					    				skuList.add(sku+"::"+mobile+"::"+listingsId);
					    			}else if(skuToken.length == 4 && (StringUtils.isNotBlank(this.status) || StringUtils.isNotBlank(this.sla)) && (this.varieties.contains(skuToken[1]) || this.varieties.contains(skuToken[2]) || (this.varieties.size() == 1 && this.varieties.contains("all")))){
					    				//if it is combo , if both exists in models then add in skuList
					    				if((this.varieties.contains(skuToken[1]) && this.varieties.contains(skuToken[2])) || (this.varieties.size() == 1 && this.varieties.contains("all"))){
					    					skuList.add(sku+"::"+mobile+"::"+listingsId);
					    				}else{
					    					//if any one among combo exists then 
					    					String otherModel = "";
					    					if(this.varieties.contains(skuToken[1])){
					    						otherModel = skuToken[2];
					    					}else{
					    						otherModel = skuToken[1];
					    					}
					    					//Check the state from existing listings.
					    					String modelState = list.get(mobile.trim().toLowerCase()) != null && list.get(mobile.trim().toLowerCase()).get(otherModel) != null ? list.get(mobile.trim().toLowerCase()).get(otherModel) : null;
					    					if(otherModel.equals("ear")){
				    							modelState = "2,a,e";
				    						}
					    					if(StringUtils.isNotBlank(this.status)){
				    							if(this.status.equals("ACTIVE") && modelState != null && modelState.split(",")[1].equals("a")){
				    								skuList.add(sku+"::"+mobile+"::"+listingsId);
				    							}else if(this.status.equals("INACTIVE")){
				    								skuList.add(sku+"::"+mobile+"::"+listingsId);
				    							}
				    						}else if(StringUtils.isNotBlank(this.sla)){
				    							if(this.procurement.equals("EXPRESS") && modelState != null && modelState.split(",")[2].equals("e")){
				    								skuList.add(sku+"::"+mobile+"::"+listingsId);
				    							}else if(this.procurement.equals("REGULAR")){
				    								if(modelState != null && (modelState.split(",")[2].equals("e") || modelState.split(",")[2].equals("i"))){
				    									skuList.add(sku+"::"+mobile+"::"+listingsId);
				    								}
				    							}else if(this.procurement.equals("DOMESTIC")){
				    								skuList.add(sku+"::"+mobile+"::"+listingsId);
				    							}
				    						}
					    				}
					    				
					    			}
					    		}
					    		continue;
					    	}
					    	
					    }
				    }else{
				    	break;
				    }
				    rowPOinter++;
			    }
				System.out.println("Total Rows Scanned : "+rowPOinter);
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}finally {
			if(file != null){
				try {
					file.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(workbook != null){
				try {
					workbook.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return skuList;
	}

	private static void writeToFileResult(List<String> skuList) throws IOException {
		
		File fout = new File("./mobileSearchResult.txt");
		if(!fout.exists()){
			fout.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		
		String mobileNames = "";
		for(String entry : skuList){
			mobileNames += entry+",";
		}
		bw.write(mobileNames.substring(0, mobileNames.length()-1));
		bw.newLine();
		bw.close();
	}

	private static void writeToFile(List<String> skuList) throws IOException {
		
		File fout = new File("./skuList.txt");
		if(!fout.exists()){
			fout.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	 
		if(skuList == null || skuList.size() == 0){
			bw.write("");
		}else{
			for(String entry : skuList){
				bw.write(entry);
				bw.newLine();
			}
		}
		bw.close();
	}

	private Map<String, String> loadSkuVsPhoneMap() throws IOException {
		Map<String, String> skuVsPhoneMap = new HashMap<>();
		BufferedReader br1 = null;
        String sCurrentLine;
        br1 = new BufferedReader(new FileReader("./skuVsPhoneMapping.txt"));
        while ((sCurrentLine = br1.readLine()) != null) {
            if(sCurrentLine != null && !"".equals(sCurrentLine)){
            	skuVsPhoneMap.put(sCurrentLine.split("::")[0], sCurrentLine.split("::")[1]);
            }
        }
        
        BufferedReader br2 = null;
        br2 = new BufferedReader(new FileReader("./missingSkuVsPhoneMapping.txt"));
        while ((sCurrentLine = br2.readLine()) != null) {
            if(sCurrentLine != null && !"".equals(sCurrentLine)){
            	skuVsPhoneMap.put(sCurrentLine.split("::")[0], sCurrentLine.split("::")[1]);
            }
        }
		return skuVsPhoneMap;
	}

	private Map<String, String> loadSkuVsPhoneMapFromCentralLocation() throws IOException {
		String folderName = this.getConfigProp().getProperty("skuvsphonemapping");
		
		Map<String, String> skuVsPhoneMap = new HashMap<>();
		File folder = new File(folderName);
		if(!folder.exists() || !folder.isDirectory()){
			System.out.println("Incorrect folder name mentioned in config file \"skuvsphonemappin\".... Exiting ");
			System.exit(0);
		}
		BufferedReader br1 = null;
        String sCurrentLine;
		
		File[] files = folder.listFiles();
		for(File file : files){
			if(file.getName().endsWith(".txt")){
				br1 = new BufferedReader(new FileReader(file));
		        while ((sCurrentLine = br1.readLine()) != null) {
		            if(sCurrentLine != null && !"".equals(sCurrentLine)){
		            	skuVsPhoneMap.put(sCurrentLine.split("::")[0], sCurrentLine.split("::")[1]);
		            }
		        }
			}
		}
		return skuVsPhoneMap;
	}
	
	
	private List<String> extractSKUsFromCSVFile(String inventoryFileName, List<String> mobileList, Map<String, TreeMap<String, String>> list) {
		// TODO Auto-generated method stub
		List<String> skuList = new ArrayList<>();
		CSVReader reader = null;
		try {

			reader = new CSVReader(new FileReader(inventoryFileName), ',');
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String[] nextLine;
		try {
			int rowPointer = 0;
			while ((nextLine = reader.readNext()) != null) {
				if(nextLine.length == 0 || "".equals(nextLine[0])){
					break;
				}
				if (rowPointer == 0) {
					rowPointer++;
					continue;
				}
		    	String sku = null;
		    	String mobile = null;
		    	String listingsId = null;
			    for(int cellPointer=1; cellPointer < 15; cellPointer++) {
			    	String cell = nextLine[cellPointer];
					if (cell == null || cell.isEmpty()) {
						continue;
					}
					cell = cell.trim();
					if(cellPointer == 1){
						listingsId = !"".equals(cell) ? cell : "";
					}
			    	if(cellPointer == 3){
			    		sku = cell;
			    		if(sku != null && !"".equals(sku)){
			    			mobile = this.skuVsPhoneMap.get(sku);
			    		}
			    		if(mobile == null){
			    			System.out.println("Missing Mobile Name for Sku : \t"+sku);
			    			System.exit(0);
//		    				if(sku.indexOf("-sg") != -1 && sku.split("-").length == 3){
//		    					mobile = mobileName.substring(mobileName.indexOf("for ")+4,mobileName.length()).trim();
//		    				}else{
//		    					mobile = mobileName.substring(mobileName.indexOf("for ")+4,mobileName.length()).trim();
//		    					mobile = mobile.substring(0,mobile.indexOf("(")-1).trim();
//		    				}
			    		}else if(mobileList.contains(mobile.trim().toLowerCase()) || (mobileList.size() == 1 && mobileList.contains("all"))){
			    			String skuToken[] = sku.split("-");
			    			
			    			if(skuToken.length == 3 && (this.varieties.contains(skuToken[1]) || (this.varieties.size() == 1 && this.varieties.contains("all")))){
			    				//if it is single and exists in models then add in skuList
			    				skuList.add(sku+"::"+mobile+"::"+listingsId);
			    			}else if(skuToken.length == 4 && StringUtils.isBlank(this.status) && StringUtils.isBlank(this.sla) && (this.varieties.contains(skuToken[1]) || this.varieties.contains(skuToken[2]) || (this.varieties.size() == 1 && this.varieties.contains("all")))){
			    				skuList.add(sku+"::"+mobile+"::"+listingsId);
			    			}else if(skuToken.length == 4 && (StringUtils.isNotBlank(this.status) || StringUtils.isNotBlank(this.sla)) && (this.varieties.contains(skuToken[1]) || this.varieties.contains(skuToken[2]) || (this.varieties.size() == 1 && this.varieties.contains("all")))){
			    				//if it is combo , if both exists in models then add in skuList
			    				if((this.varieties.contains(skuToken[1]) && this.varieties.contains(skuToken[2])) || (this.varieties.size() == 1 && this.varieties.contains("all"))){
			    					skuList.add(sku+"::"+mobile+"::"+listingsId);
			    				}else{
			    					//if any one among combo exists then 
			    					String otherModel = "";
			    					if(this.varieties.contains(skuToken[1])){
			    						otherModel = skuToken[2];
			    					}else{
			    						otherModel = skuToken[1];
			    					}
			    					//Check the state from existing listings.
			    					String modelState = list.get(mobile.trim().toLowerCase()) != null && list.get(mobile.trim().toLowerCase()).get(otherModel) != null ? list.get(mobile.trim().toLowerCase()).get(otherModel) : null;
		    						if(otherModel.equals("ear")){
		    							modelState = "2,a,e";
		    						}
			    					if(StringUtils.isNotBlank(this.status)){
		    							if(this.status.equals("ACTIVE") && modelState != null && modelState.split(",")[1].equals("a")){
		    								skuList.add(sku+"::"+mobile+"::"+listingsId);
		    							}else if(this.status.equals("INACTIVE")){
		    								skuList.add(sku+"::"+mobile+"::"+listingsId);
		    							}
		    						}else if(StringUtils.isNotBlank(this.sla)){
		    							if(this.procurement.equals("EXPRESS") && modelState != null && modelState.split(",")[2].equals("e")){
		    								skuList.add(sku+"::"+mobile+"::"+listingsId);
		    							}else if(this.procurement.equals("REGULAR")){
		    								if(modelState != null && (modelState.split(",")[2].equals("e") || modelState.split(",")[2].equals("i"))){
		    									skuList.add(sku+"::"+mobile+"::"+listingsId);
		    								}
		    							}else if(this.procurement.equals("DOMESTIC")){
		    								skuList.add(sku+"::"+mobile+"::"+listingsId);
		    							}
		    						}
			    				}
			    				
			    			}
			    		}
			    	}
			    	
			    }
			    rowPointer++;
			}
			System.out.println("Total Rows Scanned : "+rowPointer);
		}  catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
		}
		return skuList;
	}

	private void updateInventory(String sku) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		Gson gson = new Gson();
		String printTask = sku+"\t";
//		FlipkartListing flipkartListing = getFlipkartListings(sku);
		FlipkartListing flipkartListing = getFlipkartListings(sku);
		if(flipkartListing!= null){
			Map<String, String> attributeMap = new HashMap<>();
			if(this.getConfigProp().getProperty("shippingzonal") != null && 
					!"NA".equals(this.getConfigProp().getProperty("shippingzonal")) && 
					!"".equals(this.getConfigProp().getProperty("shippingzonal"))){
				attributeMap.put("zonal_shipping_charge", this.getConfigProp().getProperty("shippingzonal"));
				printTask += "Zonal-"+this.getConfigProp().getProperty("shippingzonal")+"\t";
			}
			
			if(this.getConfigProp().getProperty("shippingnational") != null && 
					!"NA".equals(this.getConfigProp().getProperty("shippingnational")) && 
					!"".equals(this.getConfigProp().getProperty("shippingnational"))){
				attributeMap.put("national_shipping_charge", this.getConfigProp().getProperty("shippingnational"));
				printTask += "National-"+this.getConfigProp().getProperty("shippingnational")+"\t";
			}
			
			if(this.getConfigProp().getProperty("shippinglocal") != null && 
					!"NA".equals(this.getConfigProp().getProperty("shippinglocal")) && 
					!"".equals(this.getConfigProp().getProperty("shippinglocal"))){
				attributeMap.put("local_shipping_charge", this.getConfigProp().getProperty("shippinglocal"));
				printTask += "Local-"+this.getConfigProp().getProperty("shippinglocal")+"\t";
			}
			
			if(this.getConfigProp().getProperty("sellingprice") != null && 
					!"NA".equals(this.getConfigProp().getProperty("sellingprice")) && 
					!"".equals(this.getConfigProp().getProperty("sellingprice")) && "Y".equals(this.getConfigProp().getProperty("sellingprice"))){
				Integer maxShipping = new Integer(this.getConfigProp().getProperty("maxshipping"));
				Integer singleProfit = new Integer(this.getConfigProp().getProperty("singleprofit"));
				Integer comboProfit = new Integer(this.getConfigProp().getProperty("comboprofit"));
				String sellingPrice = null;
				if(!sku.contains("-")){
					sellingPrice = null;
				}else if(sku.split("-").length == 3){
					String purchase = this.purchaseMap.get(sku.split("-")[1]);
					sellingPrice = getSellingPrice(new Integer(purchase),singleProfit,maxShipping);
				}else if(sku.split("-").length == 4){
					Integer purchase1 = new Integer(this.purchaseMap.get(sku.split("-")[1]));
					Integer purchase2 = new Integer(this.purchaseMap.get(sku.split("-")[2]));
					sellingPrice = getSellingPrice(purchase1+purchase2,comboProfit,maxShipping);
				}
				if(sellingPrice == null || "".equals(sellingPrice)){
					System.out.println("Cannot calculate selling price for "+sku);
					System.exit(0);
				}
				//get values from price property
				attributeMap.put("selling_price", sellingPrice);
				printTask += "SellingPrice-"+sellingPrice+"\t";
			}
			if(this.getConfigProp().getProperty("stockcount") != null && 
					!"NA".equals(this.getConfigProp().getProperty("stockcount")) && 
					!"".equals(this.getConfigProp().getProperty("stockcount"))){
				attributeMap.put("stock_count", this.getConfigProp().getProperty("stockcount").trim());
				printTask += "StockCount-"+this.getConfigProp().getProperty("stockcount")+"\t";
			}
			
			if(this.getConfigProp().getProperty("status") != null && 
					!"NA".equals(this.getConfigProp().getProperty("status")) && 
					!"".equals(this.getConfigProp().getProperty("status"))){
				attributeMap.put("listing_status", this.getConfigProp().getProperty("status").trim());
				printTask += "Status-"+this.getConfigProp().getProperty("status")+"\t";
			}
			
			if(this.getConfigProp().getProperty("sla") != null && 
					!"NA".equals(this.getConfigProp().getProperty("sla")) && 
					!"".equals(this.getConfigProp().getProperty("sla"))){
				attributeMap.put("procurement_sla", this.getConfigProp().getProperty("sla").trim());
				printTask += "SLA-"+this.getConfigProp().getProperty("sla")+"\t";
			}
			
			if(this.getConfigProp().getProperty("procurement") != null && 
					!"NA".equals(this.getConfigProp().getProperty("procurement")) && 
					!"".equals(this.getConfigProp().getProperty("procurement"))){
				if(this.getConfigProp().getProperty("procurement").trim().equals("EXPRESS")){
					attributeMap.put("procurement_sla", "2");
					printTask += "SLA-"+"2"+"\t";
				}
				attributeMap.put("procurement_type", this.getConfigProp().getProperty("procurement").trim());
				printTask += "Procurement-"+this.getConfigProp().getProperty("procurement")+"\t";
			}
			
			
			if(this.getConfigProp().getProperty("length") != null && 
					!"NA".equals(this.getConfigProp().getProperty("length")) && 
					!"".equals(this.getConfigProp().getProperty("length"))){
				attributeMap.put("package_length", this.getConfigProp().getProperty("length"));
				printTask += "Length-"+this.getConfigProp().getProperty("length")+"\t";
			}
			if(this.getConfigProp().getProperty("breadth") != null && 
					!"NA".equals(this.getConfigProp().getProperty("breadth")) && 
					!"".equals(this.getConfigProp().getProperty("breadth"))){
				attributeMap.put("package_breadth", this.getConfigProp().getProperty("breadth"));
				printTask += "Breadth-"+this.getConfigProp().getProperty("breadth")+"\t";
			}
			if(this.getConfigProp().getProperty("height") != null && 
					!"NA".equals(this.getConfigProp().getProperty("height")) && 
					!"".equals(this.getConfigProp().getProperty("height"))){
				attributeMap.put("package_height", this.getConfigProp().getProperty("height"));
				printTask += "Height-"+this.getConfigProp().getProperty("height")+"\t";
			}
			if(this.getConfigProp().getProperty("weight") != null && 
					!"NA".equals(this.getConfigProp().getProperty("weight")) && 
					!"".equals(this.getConfigProp().getProperty("weight"))){
				attributeMap.put("package_weight", this.getConfigProp().getProperty("weight"));
				printTask += "Weight-"+this.getConfigProp().getProperty("weight")+"\t";
			}
			
			System.out.println(printTask);
			flipkartListing.setAttributeValues(attributeMap);
			
			if((this.bulkFlipkartListings.getListings() != null && this.bulkFlipkartListings.getListings().size() == 9) || (this.totalCount == this.currentCount+1)){
				//push, save and clear
				this.bulkFlipkartListings.getListings().add(flipkartListing);
				this.currentCount++;
				String json = gson.toJson(this.bulkFlipkartListings);
				BulkUpdateFlipkartListingResponse uflrFlipkartListingResponse = bulkUpdateFlipkartListings(json);
				if(uflrFlipkartListingResponse.getStatus() != null && uflrFlipkartListingResponse.getStatus().equals("success")){
//					System.out.println("Successfully Updated the Inventory");
//					this.currentCount += 10;
					System.out.println("Total : "+this.totalCount+" - Completed : "+this.currentCount);
				}else{
					//write failed skus in another file
					for(Map<String, Object> each : uflrFlipkartListingResponse.getResponse()){
						if(!each.get("status").equals("updated")){
							this.failedObjects.add(sku+"\t"+uflrFlipkartListingResponse.getStatus()+"\t"+uflrFlipkartListingResponse.getResponse()+"\tJSON : "+json);
							System.out.println("Update Failed : "+sku+"\t"+uflrFlipkartListingResponse.getStatus()+"\t"+uflrFlipkartListingResponse.getResponse()+"\tJSON : "+json);
						}
					}
				}
				this.bulkFlipkartListings.getListings().clear();
			}else{
				//only push
				this.bulkFlipkartListings.getListings().add(flipkartListing);
				this.currentCount++;
			}
			
//			String json = gson.toJson(flipkartListing);
//			System.out.println(json);
//			UpdateFlipkartListingResponse uflrFlipkartListingResponse = updateFlipkartListings(json,flipkartListing.getSkuId());
//			if(uflrFlipkartListingResponse.getStatus() != null && uflrFlipkartListingResponse.getStatus().equals("success")){
//				System.out.println("Successfully Updated the Inventory");
//			}else{
				//write failed skus in another file
//				this.failedObjects.add(mobile+"\t"+sku+"\t"+uflrFlipkartListingResponse.getStatus()+"\t"+uflrFlipkartListingResponse.getResponse()+"\tJSON : "+json);
//				System.out.println("Update Failed : "+sku+"\t"+mobile+"\t"+uflrFlipkartListingResponse.getStatus()+"\t"+uflrFlipkartListingResponse.getResponse()+"\tJSON : "+json);
//			}
		}
//		else{
//			System.out.println("Cannot get the listings from Flipkart for SKU : "+sku);
//			System.exit(0);
//		}
	}
	
	private String getSellingPrice(String sku) {
		String[] tokens = sku.split("-");
		
		if(tokens.length > 3){
			if(this.priceMap.containsKey(tokens[1]+"+"+tokens[2])){
				return this.priceMap.get(tokens[1]+"+"+tokens[2]);
			}else if(this.priceMap.containsKey(tokens[2]+"+"+tokens[1])){
				return this.priceMap.get(tokens[2]+"+"+tokens[1]);
			}else{
				System.out.println("Price missed");
			}
		}else{
			if(this.priceMap.containsKey(tokens[1])){
				return this.priceMap.get(tokens[1]);
			}else{
				System.out.println("missed");
				return null;
			}
		}
		return null;
	}
	
	public String getSellingPrice(int purchase,int profit, int shipping){
		
		double gstAmt = 0.18;
		int fixedFee = 5;
		int collectionFee = 15;
		double commissionAmt = 0.14;
		
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
		int finalPrice = new Double(Math.floor(sellingPrice)).intValue();
		finalPrice = round(finalPrice);
		return finalPrice+"";
	}

	private int round(int n)
    {
        // Smaller multiple
        int a = (n / 10) * 10;
          
        // Larger multiple
        int b = a + 10;
      
        // Return of closest of two
        return (n - a > b - n)? b : a;
    }
	

	private void writeFailedToFile(List<String> list, String objectType) throws IOException {
		// TODO Auto-generated method stub
		File fout = null;
		if(objectType != null && objectType.equals("failed")){
			fout = new File("./failedSkus.txt");
		}else if(objectType != null && objectType.equals("oldsku")){
			fout = new File("./oldSkus.txt");
		}else if(objectType != null && objectType.equals("missingSkus")){
			fout = new File("./missingSkusFromCurrentInventoryFile.txt");
		}else if(objectType != null && objectType.equals("missingSingleButComboPresent")){
			fout = new File("./missingSingleButComboPresent.txt");
		}
		if(!fout.exists()){
			fout.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		
		if(list.size() > 0){
			for(String entry : list){
				bw.write(entry);
				bw.newLine();
			}
		}else{
			bw.write("");
		}
		
		bw.close();
	}

	private void saveThefailedObjects() throws Exception{
		if(this.failedObjects != null){
			System.out.println("Total SKUs Failed to Update : "+this.failedObjects.size());
			this.writeFailedToFile(this.failedObjects,"failed");
			System.out.println("\nPlease check the list of failed SKUs in failedSKUs.txt file");
		}
	}
	
	public BulkUpdateFlipkartListingResponse bulkUpdateFlipkartListings(String flipkartListingJson) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		
		try {
			String url = "https://api.flipkart.net/sellers/skus/listings/bulk";
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(url);
			request.addHeader("Authorization", "Bearer "+this.token);
			request.addHeader("Content-Type", "application/json");
			StringEntity params =new StringEntity(flipkartListingJson);
			request.setEntity(params);
			HttpResponse response = client.execute(request);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
//				System.out.println(line);
			}
			Gson gson = new Gson();
			BulkUpdateFlipkartListingResponse bulkUpdateFlipkartListingResponse = gson.fromJson(result.toString(), BulkUpdateFlipkartListingResponse.class);
			this.attempt = 0;
			return bulkUpdateFlipkartListingResponse;
		} catch (Exception e) {
			// TODO: handle exception
			if(this.attempt < 3){
				this.attempt++;
				bulkUpdateFlipkartListings(flipkartListingJson);
			}else{
				throw e;
			}
		}
		return null;
		
		
	}
	
	private FlipkartListing getFlipkartListings(String sku) throws ClientProtocolException, IOException {
		FlipkartListing flipkartListing = null;
		String url = "https://api.flipkart.net/sellers/skus/"+sku+"/listings";
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
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
		if(result.indexOf("listingId") != -1){
			String res = result.toString().replace("[\"seller\"]","\"seller\"");
			Gson gson = new Gson();
			flipkartListing = gson.fromJson(res, FlipkartListing.class);
//			System.out.println(flipkartListing.getSkuId());
		}else{
			return null;
		}
		return flipkartListing;
	}
	
	private List<String> loadSkuList() throws IOException {
		// TODO Auto-generated method stub
		List<String> skuList = new ArrayList<>();
		BufferedReader br1 = new BufferedReader(new FileReader("./skuList.txt"));
		String sCurrentLine;
        while ((sCurrentLine = br1.readLine()) != null) {
            if(sCurrentLine != null && !"".equals(sCurrentLine)){
            	skuList.add(sCurrentLine);
            }
        }
        
        return skuList;
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
}
