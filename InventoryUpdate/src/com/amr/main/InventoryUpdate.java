package com.amr.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import test.EmailUtils;

public class InventoryUpdate {

	private Map<String, String> pricingMap;
	private Properties configProp;
	public Map<String, String> skuVsPhoneMap;
	private Map<String, String> purchaseMap;
	private EmailUtils emailUtils;
	private List<String> filePaths;
	private String sellerName;

	public InventoryUpdate() throws IOException {
		pricingMap = loadPricingMap();
		configProp = loadProperties();
		skuVsPhoneMap = new HashMap<>();
		this.purchaseMap = loadpurchaseMapMap();
		emailUtils = new EmailUtils();
		filePaths = new ArrayList<>();
		filePaths.add("./uniqueMobileList.txt");
		filePaths.add("./code/config.properties");
	}

	public static void main(String[] args) throws Exception {
		InventoryUpdate iU = new InventoryUpdate();
		if(iU.getConfigProp() == null){
			System.out.println("Config Properties cannot be loaded");
			return;
		}
		boolean isValid = iU.validateConfigFile();
		if(!isValid){
			System.out.println("Validation Failed");
			return;
		}
		iU.sellerName = iU.getConfigProp().getProperty("seller");
		File qcFile = new File(iU.getConfigProp().getProperty("qcapprovedfile"));
		File invFile = new File(iU.getConfigProp().getProperty("listingsfile"));
		File inactiveInvFile = new File(iU.getConfigProp().getProperty("inactivelistingsfile"));
		//Loading the skuVsPhone Map
		System.out.println("Loading the skuVsPhone Map");
		iU.skuVsPhoneMap = iU.loadSkuVsPhoneMapFromCentralLocation();
		System.out.println("Loaded SkuVsPhoneMapping Size : "+iU.skuVsPhoneMap.size());
		
		boolean isInvCSV = invFile.getName().endsWith(".csv");
		boolean isInvExcel = invFile.getName().endsWith(".xls");
		boolean isInactiveInvCSV = inactiveInvFile.getName().endsWith(".csv");
		boolean isInactiveInvExcel = inactiveInvFile.getName().endsWith(".xls");
		boolean isQcCSV = qcFile.getName().endsWith(".csv");
		boolean isQcExcel = qcFile.getName().endsWith(".xls");
		
		ListingsStatus listingsStatus = new ListingsStatus();
		listingsStatus.setSellerName(iU.sellerName);
		listingsStatus.setSkuVsPhoneMap(iU.skuVsPhoneMap);
		Map<String, TreeMap<String, String>> currentList = new TreeMap<>();
		Map<String, TreeMap<String, String>> list = new TreeMap<>();
		Map<String, TreeMap<String, String>> inactiveCurrentList = new TreeMap<>();
		
		if("MobileList".equals(args[0])){
			//Get existing status
			if(isInvExcel){
				currentList = listingsStatus.getUniqueMobileNamesExcel(invFile);
			}else if(isInvCSV){
				currentList = listingsStatus.getUniqueMobileNamesCSV(invFile);
			}
			
			if(isInactiveInvExcel){
				inactiveCurrentList = listingsStatus.getUniqueMobileNamesExcel(inactiveInvFile);
			}else if(isInactiveInvCSV){
				inactiveCurrentList = listingsStatus.getUniqueMobileNamesCSV(inactiveInvFile);
			}
			if(inactiveCurrentList.size() > 0){
				for(String mobile : inactiveCurrentList.keySet()){
					TreeMap<String, String> models = inactiveCurrentList.get(mobile);
					for(String model : models.keySet()){
						String eachRowtring = models.get(model);
						listingsStatus.populateUniqueModelsMap(currentList, mobile, model, eachRowtring, null);
					}
				}
			}
			
			//Get the non-live status
			if(isQcExcel){
				list = iU.getUniqueMobileNames(qcFile);
			}else if(isQcCSV){
				list = iU.getUniqueMobileNamesCSV(qcFile);
			}

			//merge status
			iU.getMergedStatus(currentList,list);
			iU.emailUtils.sendEmail(iU.sellerName, "Non-LIVE to LIVE", "1_UniqueModels", "", iU.filePaths,"");
		}else if("Update".equals(args[0]) && isQcExcel){
			iU.makeListingsLive(qcFile);
			iU.emailUtils.sendEmail(iU.sellerName, "Non-LIVE to LIVE", "2_UpdateInventory", "", iU.filePaths,"");
		}else if("Update".equals(args[0]) && isQcCSV){
			iU.makeListingsLiveCSV(qcFile);
			iU.emailUtils.sendEmail(iU.sellerName, "Non-LIVE to LIVE", "2_UpdateInventory", "", iU.filePaths,"");
		}
	}

	private boolean validateConfigFile() {
		// TODO Auto-generated method stub
		String qcApprvd = getConfigProp().getProperty("qcapprovedfile");
		String sellerName = getConfigProp().getProperty("seller");
		String listingsfile = getConfigProp().getProperty("listingsfile");
		String shippinglocal = getConfigProp().getProperty("shippinglocal");
		String shippingzonal = getConfigProp().getProperty("shippingzonal");
		String shippingnational = getConfigProp().getProperty("shippingnational");
		String mrp = getConfigProp().getProperty("mrp");
		String count = getConfigProp().getProperty("count");
		String length = getConfigProp().getProperty("length");
		String breadth = getConfigProp().getProperty("breadth");
		String height = getConfigProp().getProperty("height");
		String weight = getConfigProp().getProperty("weight");
		String purchase = getConfigProp().getProperty("purchase");
		String maxshipping = getConfigProp().getProperty("maxshipping");
		String singleprofit = getConfigProp().getProperty("singleprofit");
		String comboprofit = getConfigProp().getProperty("comboprofit");
		String hsn = getConfigProp().getProperty("hsn");
		String tax = getConfigProp().getProperty("tax");
		String skuvsphonemapping = getConfigProp().getProperty("skuvsphonemapping");
		
		if(qcApprvd == null || "".equals(qcApprvd) || !(qcApprvd.endsWith(".csv") || qcApprvd.endsWith(".xls"))){
			System.out.println("Incorrect QCApproved File Name ");
			return false;
		}else{
			File qcFile = new File(qcApprvd);
			if(!qcFile.exists()){
				System.out.println("QCApproved File does not exists");
				return false;
			}
		}
		
		if(listingsfile == null || "".equals(listingsfile) || !(listingsfile.endsWith(".csv") || listingsfile.endsWith(".xls"))){
			System.out.println("Incorrect Inventory/Listings File Name ");
			return false;
		}else{
			File listingsFle = new File(listingsfile);
			if(!listingsFle.exists()){
				System.out.println("Inventory/Listings File does not exists");
				return false;
			}
		}
		
		if(shippinglocal == null || "".equals(shippinglocal) || ((!sellerName.equalsIgnoreCase("amr") && !shippinglocal.equals("30")) || (sellerName.equalsIgnoreCase("amr") && !shippinglocal.equals("27")))){
			System.out.println("Incorrect Shipping Local");
			return false;
		}
		
		if(shippingzonal == null || "".equals(shippingzonal) || ((!sellerName.equalsIgnoreCase("amr") && !shippingzonal.equals("45")) || (sellerName.equalsIgnoreCase("amr") && !shippingzonal.equals("40")))){
			System.out.println("Incorrect Shipping Zonal");
			return false;
		}
		
		if(shippingnational == null || "".equals(shippingnational) || ((!sellerName.equalsIgnoreCase("amr") && !shippingnational.equals("65")) || (sellerName.equalsIgnoreCase("amr") && !shippingnational.equals("58")))){
			System.out.println("Incorrect Shipping National");
			return false;
		}
		
		if(mrp == null || "".equals(mrp) || !mrp.equals("999")){
			System.out.println("Incorrect MRP");
			return false;
		}
		
		if(count == null || "".equals(count) || !count.equals("25")){
			System.out.println("Incorrect Count");
			return false;
		}
		
		if(length == null || "".equals(length) || !length.equals("25")){
			System.out.println("Incorrect Length");
			return false;
		}
		
		if(breadth == null || "".equals(breadth) || !breadth.equals("14")){
			System.out.println("Incorrect Breadth");
			return false;
		}
		
		if(height == null || "".equals(height) || !height.equals("3")){
			System.out.println("Incorrect Height");
			return false;
		}
		
		if(weight == null || "".equals(weight) || !weight.equals("0.1")){
			System.out.println("Incorrect Weight");
			return false;
		}
		
		if(maxshipping == null || "".equals(maxshipping) || ((!sellerName.equalsIgnoreCase("amr") && !maxshipping.equals("65")) || (sellerName.equalsIgnoreCase("amr") && !maxshipping.equals("58")))){
			System.out.println("Incorrect Max Shipping");
			return false;
		}
		
		if(singleprofit == null || "".equals(singleprofit) || !(new Integer(singleprofit) >= 40)){
			System.out.println("Incorrect Single Profit or less than 40");
			return false;
		}
		
		if(comboprofit == null || "".equals(comboprofit) || !(new Integer(comboprofit) >= 50)){
			System.out.println("Incorrect Combo Profit or less than 50");
			return false;
		}
		
		if(hsn == null || "".equals(hsn) || !hsn.equals("39269099")){
			System.out.println("Incorrect HSN");
			return false;
		}
		
		if(tax == null || "".equals(tax) || !tax.equals("GST_18")){
			System.out.println("Incorrect Tax Code");
			return false;
		}
		
		if(skuvsphonemapping == null || "".equals(skuvsphonemapping)){
			System.out.println("Incorrect SKUVsPhoneMapping URL");
			return false;
		}else{
			File skuVsPhone = new File(skuvsphonemapping);
			if(!skuVsPhone.exists() || !skuVsPhone.isDirectory()){
				System.out.println("SkuVsPhoneMapping Folder does not exists or not a directory");
				return false;
			}
		}
		
		String[] varieties = "tp,btp,ear,crm,chr,hyb,gkred,gkblu,bli,dot,def,daz,robo,fcb,fcg,rbblk,rbbrw,rbblu,rbred,liscrm,lisbrn,lisblk,lisblu,sg,blkg,golg,whtg,nang,tg,gglass,nglass,bglass,wglass".split(",");
		List<String> vars = Arrays.asList(varieties);
		for(String each : purchase.split(",")){
			if(!vars.contains(each.split(":")[0])){
				System.out.println("New model Found... Please update the validation test case : "+each.split(":")[0]);
				return false;
			}
		}
		return true;
	}

	private Map<String, String> loadSkuVsPhoneMapFromCentralLocation() throws IOException {
		String folderName = this.getConfigProp().getProperty("skuvsphonemapping");
		
		Map<String, String> skuVsPhoneMap = new HashMap<>();
		File folder = new File(folderName);
		if(!folder.exists() || !folder.isDirectory()){
			System.out.println("Incorrect folder name mentioned in config file.... Exiting ");
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
	
	private void getMergedStatus(Map<String, TreeMap<String, String>> existingmap, Map<String, TreeMap<String, String>> nonLiveList) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Merging the Live and Non-Live Listings Status");
		Map<String, TreeMap<String, String>> livemap = new TreeMap<>();
		Map<String, TreeMap<String, String>> nonLivemap = new TreeMap<>();
        
        for(String mobileName : nonLiveList.keySet()){
        	if(!existingmap.containsKey(mobileName)){//If it is not present in existing map
        		TreeMap<String, String> temp = new TreeMap<>();
        		for(String key : nonLiveList.get(mobileName).keySet()){
        			temp.put(key, "1,i,i");
        		}
        		nonLivemap.put(mobileName,temp);
        	}else{
//        		TreeMap<String, String> map1 = new TreeMap<>();
        		for(String var1 : nonLiveList.get(mobileName).keySet()){
        			if(existingmap.get(mobileName).containsKey(var1)){
//        				nonLivemap.put(mobileName, map1);
        				if(nonLivemap.get(mobileName) != null){
        					nonLivemap.get(mobileName).put(var1, existingmap.get(mobileName).get(var1));
        				}else{
            				TreeMap<String, String> map1 = new TreeMap<>();
            				map1.put(var1, existingmap.get(mobileName).get(var1));
        					nonLivemap.put(mobileName, map1);
        				}
        			}else{// If the model is not there in existing map then put it in nonLive
        				if(nonLivemap.get(mobileName) != null){
        					nonLivemap.get(mobileName).put(var1, "1,i,i");
        				}else{
        					TreeMap<String, String> map2 = new TreeMap<>();
        					map2.put(var1, "1,i,i");
        					nonLivemap.put(mobileName, map2);
        				}
        			}
        		}
//        		livemap.put(mobileName, map1);
        	}
        }
        
        if(nonLivemap.size() > 0 || livemap.size() > 0 ){
        	writeToFile(livemap,nonLivemap);
        }else{
        	System.out.println("No Data");
        }
	}
	
	private void copyExistingStatusToNonLive() throws IOException {
		// TODO Auto-generated method stub
		Map<String, TreeMap<String, String>> existingmap = new TreeMap<>();
		Map<String, TreeMap<String, String>> livemap = new TreeMap<>();
		Map<String, TreeMap<String, String>> nonLivemap = new TreeMap<>();
		BufferedReader br1 = null;
        String sCurrentLine;
        br1 = new BufferedReader(new FileReader("./currentLIVEListingsStatus.txt"));
        while ((sCurrentLine = br1.readLine()) != null) {
            if(sCurrentLine != null && !"".equals(sCurrentLine)){
            	String mobileName = sCurrentLine.split(":")[0];
            	String models = sCurrentLine.split(":")[1];
            	String[] variety = models.split("\t");
            	TreeMap<String, String> map1 = new TreeMap<>();
            	for(String var1 : variety){
            		map1.put(var1.split("-")[0], var1.split("-")[1]);
            	}
            	existingmap.put(mobileName, map1);
            }
        }
        
        
        BufferedReader br2 = null;
        br2 = new BufferedReader(new FileReader("./uniqueMobileList.txt"));
        while ((sCurrentLine = br2.readLine()) != null) {
            if(sCurrentLine != null && !"".equals(sCurrentLine)){
            	String mobileName = sCurrentLine.split(":")[0];
            	String models = sCurrentLine.split(":")[1];
            	
            	if(!existingmap.containsKey(mobileName)){
            		String[] variety = models.split("\t");
            		TreeMap<String, String> map1 = new TreeMap<>();
            		for(String var1 : variety){
            			map1.put(var1.split("-")[0], var1.split("-")[1]);
            		}
            		nonLivemap.put(mobileName,map1);
            	}else{
            		String[] variety = models.split("\t");
            		TreeMap<String, String> map1 = new TreeMap<>();
            		for(String var1 : variety){
            			if(existingmap.get(mobileName).containsKey(var1.split("-")[0])){
            				map1.put(var1.split("-")[0], existingmap.get(mobileName).get(var1.split("-")[0]));
            			}else{
            				map1.put(var1.split("-")[0], var1.split("-")[1]);
            			}
            		}
            		livemap.put(mobileName, map1);
            	}
            	
            }
        }
        
        if(nonLivemap.size() > 0 || livemap.size() > 0 ){
        	writeToFile(livemap,nonLivemap);
        }else{
        	System.out.println("No Data");
        }
	}

	private void makeListingsLiveCSV(File invFile) throws IOException {
		// TODO Auto-generated method stub
		List<String[]> listR = new ArrayList<>();
		Map<String, HashMap<String, String>> map1 = loadLiveProducts();
		if(map1 == null){
			System.out.println("Empty uniqueMobileList.txt");
			return;
		}
		CSVReader reader = null;
		try {

			reader = new CSVReader(new FileReader(invFile), ',');
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
					listR.add(nextLine);
					rowPointer++;
					continue;
				}
				String mobileName = null;
		    	String sku = null;
			    for(int cellPointer=2; cellPointer <= 28; cellPointer++) {
			    	String cell = nextLine[cellPointer];
			    	if(cell == null){
			    		continue;
			    	}
			    	cell = cell.trim();
			    	if(cellPointer == 2){
			    		mobileName = cell;
			    	}
			    	if(cellPointer == 3){
			    		sku = cell;
			    	}

			    	if(cellPointer == 4){
			    		nextLine[cellPointer]=this.getConfigProp().getProperty("mrp");
			    	}
			    	if(cellPointer == 6){
			    		nextLine[cellPointer]="YES";
			    	}
			    	if(cellPointer == 7){
			    		nextLine[cellPointer]=this.getConfigProp().getProperty("shippinglocal");
			    	}
			    	if(cellPointer == 8){
			    		nextLine[cellPointer]=this.getConfigProp().getProperty("shippingzonal");
			    	}
			    	if(cellPointer == 9){
			    		nextLine[cellPointer]=this.getConfigProp().getProperty("shippingnational");
			    	}
			    	if(cellPointer == 12){
			    		nextLine[cellPointer]=this.getConfigProp().getProperty("count");
			    	}
			    	if(cellPointer == 25){
			    		nextLine[cellPointer]=this.getConfigProp().getProperty("length");
			    	}
			    	if(cellPointer == 26){
			    		nextLine[cellPointer]=this.getConfigProp().getProperty("breadth");
			    	}
			    	if(cellPointer == 27){
			    		nextLine[cellPointer]=this.getConfigProp().getProperty("height");
			    	}
			    	if(cellPointer == 28){
			    		nextLine[cellPointer]=this.getConfigProp().getProperty("weight");
			    	}
			    	if(cellPointer == 16){
			    		nextLine[cellPointer]=this.getConfigProp().getProperty("hsn");
			    	}
			    	if(cellPointer == 17){
			    		nextLine[cellPointer]=this.getConfigProp().getProperty("tax");
			    	}
			    	
			    	String mobile = null;
		    		if(sku != null && !"".equals(sku)){
		    			mobile = this.skuVsPhoneMap.get(sku);
		    			if(mobile == null){
		    				System.out.println("Missing Mobile Name for Sku : \t"+sku);
		    				System.exit(0);
//		    			if(sku != null && sku.indexOf("-sg") != -1 && sku.split("-").length == 3){
//		    				mobile = mobileName.substring(mobileName.indexOf("for ")+4,mobileName.length()).trim();
//		    			}else if(sku != null){
//		    				mobile = mobileName.substring(mobileName.indexOf("for ")+4,mobileName.length()).trim();
//		    				mobile = mobile.substring(0,mobile.indexOf("(")-1).trim();
//		    			}
		    			}
		    		}
			    	//populate price
			    	if(cellPointer == 5 && mobile != null && sku != null){
//			    		String price = getPriceList(mobile+":"+sku);
			    		String price = "";
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
			    		if(StringUtils.isNoneBlank(sellingPrice)){
			    			nextLine[cellPointer]=sellingPrice;
			    		}
			    	}
			    	if(map1.get(mobile) != null && (cellPointer == 13 || cellPointer == 10 || cellPointer == 14)){
			    		String[] tokens = sku.split("-");
						List<String> lst = new ArrayList<>();
						if(tokens.length > 3){
							String res1 = tokens[1].equals("ear") ? "2,a,e" : map1.get(mobile).get(tokens[1]);
							String res2 = tokens[2].equals("ear") ? "2,a,e" : map1.get(mobile).get(tokens[2]);
							String[] red1 = res1.split(",");
							String[] red2 = res2.split(",");
							String[] final1 = new String[3];
							if(new Integer(red1[0]) > new Integer(red2[0])){
								final1[0] = red1[0];
							}else{
								final1[0] = red2[0];
							}
							
							if(red1[1].equals("i") || red2[1].equals("i")){
								final1[1] = "INACTIVE";
							}else{
								final1[1] = "ACTIVE";
							}
							
							if(red1[2].equals("d") || red2[2].equals("d")){
								final1[2] = "domestic procurement";
							}else if(red1[2].equals("i") || red2[2].equals("i")){
								final1[2] = "instock";
							}else if(red1[2].equals("e") && red2[2].equals("e")){
								final1[2] = "express";
							}
							
							
							if(cellPointer == 13){
								nextLine[cellPointer]=final1[0];
				    		}else if(cellPointer == 14){
				    			nextLine[cellPointer]=final1[1];
				    		}else if(cellPointer == 10){
				    			nextLine[cellPointer]=final1[2];
				    		}
							
						}else{
							String res1 = map1.get(mobile).get(tokens[1]);
							String[] final1 = new String[3];
							String[] red1 = res1.split(",");
							final1[0] = red1[0];
							if(red1[1].equals("a")){
								final1[1] = "ACTIVE";
							}else{
								final1[1] = "INACTIVE";
							}
							if(red1[2].equals("d")){
								final1[2] = "domestic procurement";
							}else if(red1[2].equals("i")){
								final1[2] = "instock";
							}else if(red1[2].equals("e")){
								final1[2] = "express";
							}
							
							if(cellPointer == 13){
								nextLine[cellPointer]=final1[0];
				    		}else if(cellPointer == 14){
				    			nextLine[cellPointer]=final1[1];
				    		}else if(cellPointer == 10){
				    			nextLine[cellPointer]=final1[2];
				    		}
						}
			    	}else{
			    		if(cellPointer == 13){
			    			nextLine[cellPointer]="1";
			    		}else if(cellPointer == 14){
			    			nextLine[cellPointer]="INACTIVE";
			    		}else if(cellPointer == 10){
			    			nextLine[cellPointer]="instock";
			    		}
			    	}
			    }
			    listR.add(nextLine);
			    System.out.println("Row complete : "+rowPointer+", SKU : "+sku);
			    rowPointer++;
			}
		    reader.close();
			System.out.println(listR.size());
			
			CSVWriter writer = new CSVWriter(new FileWriter(invFile));
			writer.writeAll(listR);
			writer.close();
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
//			try {
//				writer.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}

	private Map<String, TreeMap<String, String>> getUniqueMobileNamesCSV(File invFile)
			throws FileNotFoundException, IOException {
		System.out.println("Reading the Non-Live Listings File");
		Map<String, TreeMap<String, String>> list = new TreeMap<>();
		CSVReader reader = null;
		try {

			reader = new CSVReader(new FileReader(invFile), ',');
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String[] nextLine;
		try {
			int rowPointer = 0;
			while ((nextLine = reader.readNext()) != null) {
				if(nextLine.length == 0){
					break;
				}
				if (rowPointer == 0) {
					rowPointer++;
					continue;
				}
				String mobileName = null;
				String sku = null;
				String mobile = null;
				String eachRowtring = "";
				if (nextLine.length > 2) {
					for (int cellPointer = 2; cellPointer < 25; cellPointer++) {
						String cell = nextLine[cellPointer];
						if (cell == null || cell.isEmpty()) {
							continue;
						}
						cell = cell.trim();
						if (cellPointer == 2) {
							mobileName = cell;
						}
						if (cellPointer == 3) {
							if ("".equals(mobileName)) {
								mobileName = null;
								continue;
							}
							sku = cell;
							if(sku != null && !"".equals(sku)){
				    			mobile = this.skuVsPhoneMap.get(sku);
				    			if(mobile.equals("Cover")){
				    				System.out.println(sku);
				    			}
				    		}
				    		if(mobile == null){
				    			System.out.println("Missing Mobile Name for Sku : \t"+sku);
				    			System.exit(0);
//				    			if (sku.indexOf("-sg") != -1 && sku.split("-").length == 3) {
//				    				mobile = mobileName.substring(mobileName.indexOf("for ") + 4, mobileName.length())
//				    						.trim();
//				    			} else {
//				    				System.out.println(sku);
//				    				mobile = mobileName.substring(mobileName.indexOf("for ") + 4, mobileName.length())
//				    						.trim();
//				    				mobile = mobile.substring(0, mobile.indexOf("(") - 1).trim();
//				    			}
				    		}
						}

					}
					if (mobile != null && sku != null) {

						String[] tokens = sku.split("-");
						if (tokens.length == 3) {// Do not extract combos
													// for model status.
													// there will be
													// conflicts if we
													// consider combos.
							updateMapForSingleListing(list, mobile, eachRowtring, tokens[1]);
						}else if(tokens.length > 3){
							updateMapForSingleListing(list, mobile, eachRowtring, tokens[1]);
							updateMapForSingleListing(list, mobile, eachRowtring, tokens[2]);
						}
					}

				}
				rowPointer++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

	private void updateMapForSingleListing(Map<String, TreeMap<String, String>> list, String mobile,
			String eachRowtring, String model) {
//		String model = tokens[1];
		if (list.get(mobile) != null) {
			TreeMap<String, String> existingMap = list.get(mobile);
			if (existingMap.get(model) != null && !"".equals(model)) {
				// check if the there are 2 models
				// having different status
				String modelValue = existingMap.get(model);
				if (!modelValue.equals(eachRowtring)) {
					String[] values = modelValue.split(",");
					String newmodelValue = eachRowtring;
					String[] newValues = newmodelValue.split(",");
					String finalString = "";
					if (new Integer(values[0]) > new Integer(newValues[0])) {
						finalString += values[0];
					} else {
						finalString += newValues[0];
					}

					if ("i".equals(values[1]) || "i".equals(newValues[1])) {
						finalString += ",i";
					} else {
						finalString += ",a";
					}

					if ("d".equals(values[2]) || "d".equals(newValues[2])) {
						finalString += ",d";
					} else if ("i".equals(values[2]) || "i".equals(newValues[2])) {
						finalString += ",i";
					} else if ("e".equals(values[2]) || "e".equals(newValues[2])) {
						finalString += ",e";
					}

					existingMap.put(modelValue, finalString);
					list.put(mobile, existingMap);
				}
			} else {
				existingMap.put(model, eachRowtring);
				list.put(mobile, existingMap);
			}
		} else {
			TreeMap<String, String> modelMap = new TreeMap<>();
			modelMap.put(model, eachRowtring);
			list.put(mobile, modelMap);
		}
	}

	private Properties loadProperties() throws IOException{

	    String versionString = null;

	    //to load application's properties, we use this class
	    Properties mainProperties = new Properties();

	    FileInputStream file;

	    //the base folder is ./, the root of the main.properties file  
	    String path = "./code/config.properties";//"D:/AAA_WORK/Important Batch Files/Flipkart/Non-LIVE to LIVE/code/config.properties

	    //load the file handle for main.properties
	    file = new FileInputStream(path);

	    //load all the properties from this file
	    mainProperties.load(file);

	    //we have loaded the properties, so close the file handle
	    file.close();

	    //retrieve the property we are intrested, the app.version
	    versionString = mainProperties.getProperty("app.version");

	    return mainProperties;
	}

	private Map<String, String> loadPricingMap() {
		Map<String, String> comboCaseTypes = new HashMap<>();
//		comboCaseTypes.put("compete","tp+chr:319,tp+def:379,tp+fcb:359,tp+fcg:359,chr+def:389,chr+fcb:369,chr+fcg:369,def+fcb:429,def+fcg:429,fcb+fcg:399,tp+tp:289,chr+chr:339,def+def:459,fcb+fcb:399,fcg+fcg:399,tp+crm:319,crm+crm:339,chr+crm:339,def+crm:389,fcb+crm:369,fcg+crm:369,tp+sg:289,chr+sg:319,def+sg:379,crm+sg:319,fcb+sg:359,fcg+sg:359,tp:189,crm:199,chr:209,def:269,fcb:239,fcg:239,sg:189");
		comboCaseTypes.put("compete","tp+chr:340,tp+def:399,tp+fcb:359,tp+fcg:379,chr+def:410,chr+fcb:389,chr+fcg:389,def+fcb:449,def+fcg:449,fcb+fcg:419,tp+tp:310,chr+chr:359,def+def:479,fcb+fcb:419,fcg+fcg:419,tp+crm:339,crm+crm:359,chr+crm:359,def+crm:410,fcb+crm:389,fcg+crm:389,tp+sg:310,chr+sg:339,def+sg:399,crm+sg:339,fcb+sg:379,fcg+sg:379,tp:210,crm:210,chr:239,def:289,fcb:259,fcg:259,sg:210");
		comboCaseTypes.put("level-1","tp+chr:319,tp+def:379,tp+fcb:359,tp+fcg:359,chr+def:389,chr+fcb:369,chr+fcg:369,def+fcb:429,def+fcg:429,fcb+fcg:399,tp+tp:289,chr+chr:339,def+def:459,fcb+fcb:399,fcg+fcg:399,tp+crm:319,crm+crm:339,chr+crm:339,def+crm:389,fcb+crm:369,fcg+crm:369,tp:199,crm:229,chr:219,def:299,fcb:259,fcg:259,sg:199");
		comboCaseTypes.put("level-2","tp+chr:389,tp+def:449,tp+fcb:429,tp+fcg:429,chr+def:459,chr+fcb:439,chr+fcg:439,def+fcb:499,def+fcg:499,fcb+fcg:469,tp+tp:359,chr+chr:409,def+def:529,fcb+fcb:469,fcg+fcg:469,tp+crm:389,crm+crm:409,chr+crm:409,def+crm:459,fcb+crm:439,fcg+crm:439,tp:289,crm:329,chr:319,def:389,fcb:349,fcg:349");
		comboCaseTypes.put("level-3","tp+chr:319,tp+def:379,tp+fcb:359,tp+fcg:359,chr+def:389,chr+fcb:369,chr+fcg:369,def+fcb:429,def+fcg:429,fcb+fcg:399,tp+tp:289,chr+chr:339,def+def:459,fcb+fcb:399,fcg+fcg:399,tp+crm:319,crm+crm:339,chr+crm:339,def+crm:389,fcb+crm:369,fcg+crm:369,tp:219,crm:259,chr:249,def:319,fcb:279,fcg:279");
		comboCaseTypes.put("level-4","tp+chr:499,tp+def:599,tp+fcb:499,tp+fcg:499,chr+def:599,chr+fcb:599,chr+fcg:599,def+fcb:699,def+fcg:699,fcb+fcg:599,tp+tp:450,chr+chr:599,def+def:459,fcb+fcb:599,fcg+fcg:599,tp+crm:499,crm+crm:599,chr+crm:599,def+crm:599,fcb+crm:599,fcg+crm:599,tp:399,crm:399,chr:399,def:499,fcb:499,fcg:499");
		return comboCaseTypes;
	}

	private void writeToFile(Map<String, TreeMap<String, String>> list) throws IOException {
		// TODO Auto-generated method stub
		File fout = new File("./uniqueMobileList.txt");
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	 
		for(String entry : list.keySet()){
	    	String printS = entry+":";
	    	TreeMap<String, String> modelMap = list.get(entry);
	    	for(String variety:  modelMap.keySet()){
	    		String values = modelMap.get(variety);
	    		if(values == null || values.isEmpty()){
	    			printS += variety+"-1,i,i\t";
	    		}else{
	    			printS += variety+"-"+values+"\t";
	    		}
	    	}
	    	bw.write(printS);
			bw.newLine();
	    	
	    }
		bw.close();
	}

	private void writeToFile(Map<String, TreeMap<String, String>> list1, Map<String, TreeMap<String, String>> list2) throws IOException {
		// TODO Auto-generated method stub
		File fout = new File("./uniqueMobileList.txt");
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	 
//		for(String entry : list1.keySet()){
//	    	String printS = entry+":";
//	    	TreeMap<String, String> modelMap = list1.get(entry);
//	    	for(String variety:  modelMap.keySet()){
//	    		String values = modelMap.get(variety);
//	    		if(values == null || values.isEmpty()){
//	    			printS += variety+"-1,i,i\t";
//	    		}else{
//	    			printS += variety+"-"+values+"\t";
//	    		}
//	    	}
//	    	bw.write(printS);
//			bw.newLine();
//	    	
//	    }
		
		for(String entry : list2.keySet()){
	    	String printS = entry+":";
	    	TreeMap<String, String> modelMap = list2.get(entry);
	    	for(String variety:  modelMap.keySet()){
	    		String values = modelMap.get(variety);
	    		if(values == null || values.isEmpty()){
	    			printS += variety+"-1,i,i\t";
	    		}else{
	    			printS += variety+"-"+values+"\t";
	    		}
	    	}
	    	bw.write(printS);
			bw.newLine();
	    	
	    }
		bw.close();
	}

	private Map<String, TreeMap<String, String>> getUniqueMobileNames(File fileq) {
		// TODO Auto-generated method stub
		System.out.println("Reading the Non-Live Inventory File");
		FileInputStream file = null;
		HSSFWorkbook workbook = null;
		try {
			Map<String, TreeMap<String, String>> list = new TreeMap<>();
				file = new FileInputStream(fileq);
			    workbook = new HSSFWorkbook(file);
			    HSSFSheet sheet = workbook.getSheetAt(0);
			    boolean flag = true;
			    int rowPOinter = 2;
			    while(flag){
				    Row row = sheet.getRow(rowPOinter);
				    if(row != null){
				    	String mobileName = null;
				    	String sku = null;
				    	String mobile = null;
				    	String eachRowtring = "";
				    	boolean nonLiveListing = false;
					    for(int cellPointer=3; cellPointer < 28; cellPointer++) {
					    	Cell cell = row.getCell(cellPointer);
					    	if(cellPointer==3){
					    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    		mobileName = cell.getStringCellValue();
					    	}
					    	if(cellPointer == 5){
					    		if("".equals(mobileName)){
					    			mobileName = null;
					    			continue;
					    		}
					    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    		sku = cell.getStringCellValue();
					    		if(sku != null && !"".equals(sku)){
					    			if("mot-def-sg-a325715,mot-tp-tg-a326434,mot-chr-tg-a325497,mot-tp-tg-a326338,mot-fcb-ear-a325734,mot-def-tg-a326499,mot-chr-sg-a325475,mot-tp-tg-a326263,mot-rbbrw-ear-a325904,mot-liscrm-ear-a325736,mot-tp-tg-a326186,mot-chr-tg-a325853,mot-tp-tg-a326143,mot-tp-tg-a326314,mot-tp-tg-a326237,mot-tp-tg-a326383,mot-tp-tg-a326155,mot-tp-tg-a326390,mot-chr-tg-a325492,mot-tp-tg-a326150,mot-chr-tg-a326515,mot-rbblk-ear-a326053,mot-tp-tg-a326299,mot-tp-ear-a326376,mot-tp-tg-a325905,mot-chr-tg-a325834,mot-fcb-ear-a325518,mot-chr-tg-a326060,mot-tp-tg-a326302,mot-tp-tg-a326307,mot-tp-sg-a325630,mot-tp-tg-a325474,mot-chr-tg-a326604,mot-tp-tg-a325842,mot-tp-tg-a326352,mot-chr-tg-a325480,mot-tp-tg-a326011,mot-tp-tg-a326446,mot-tp-tg-a326422,mot-tp-tg-a326059,mot-tp-sg-a325402,mot-tp-tg-a325753,mot-tp-tg-a325983,mot-tp-tg-a326289,mot-tp-sg-a325625,mot-chr-tg-a325519,mot-chr-tg-a325507,mot-chr-sg-a325693,mot-tp-tg-a326234,mot-tp-tg-a326474,mot-chr-tg-a325773,mot-chr-tg-a326581,mot-tp-tg-a326594,mot-tp-tg-a326203,mot-tp-tg-a326347,mot-chr-tg-a326081,mot-rbbrw-ear-a325781,mot-chr-tg-a325901,mot-tp-sg-a325668,mot-tp-tg-a326568,mot-tp-tg-a326493,mot-tp-tg-a326258,mot-chr-tg-a325785,mot-chr-tg-a325499,mot-tp-tg-a325988,mot-tp-tg-a326316,mot-tp-tg-a325515,mot-rbblu-ear-a325847,mot-tp-tg-a326380,mot-tp-tg-a326215,mot-tp-sg-a325613,mot-tp-tg-a326171,mot-chr-tg-a325879,mot-tp-ear-a326448,mot-tp-tg-a326481,mot-tp-tg-a325558,mot-fcg-ear-a325417,mot-chr-tg-a325468,mot-chr-tg-a326000,mot-tp-tg-a325490,mot-tp-tg-a325750,mot-tp-ear-a326195,mot-tp-tg-a326277,mot-chr-tg-a325975,mot-chr-tg-a325850,mot-tp-tg-a326119,mot-tp-tg-a326359,mot-tp-tg-a326309,mot-chr-sg-a325597,mot-tp-tg-a325971,mot-chr-tg-a325937,mot-tp-tg-a325894,mot-tp-tg-a326354,mot-tp-tg-a326392,mot-tp-tg-a326272,mot-tp-tg-a325969,mot-tp-tg-a326443,mot-tp-tg-a325565,mot-chr-tg-a326101,mot-tp-ear-a325375,mot-def-tg-a326528,mot-tp-tg-a325957,mot-tp-tg-a325398,mot-tp-tg-a325812,mot-tp-sg-a325477,mot-tp-tg-a325817,mot-chr-tg-a326542,mot-chr-ear-a326509,mot-tp-tg-a326156,mot-tp-tg-a326271,mot-tp-tg-a325766,mot-chr-tg-a325900,mot-tp-tg-a326360,mot-tp-tg-a326288,mot-tp-tg-a326252,mot-chr-sg-a325553,mot-tp-tg-a325545,mot-chr-tg-a325758,mot-tp-tg-a326219,mot-tp-tg-a326353,mot-chr-tg-a325607,mot-chr-sg-a325642,mot-tp-tg-a325925,mot-tp-tg-a326315,mot-tp-tg-a326245,mot-tp-tg-a326238,mot-tp-tg-a326339,mot-tp-tg-a326168,mot-tp-ear-a326276,mot-chr-sg-a325635,mot-rbbrw-ear-a326110,mot-chr-tg-a326561,mot-tp-tg-a326226,mot-tp-tg-a325881,mot-chr-ear-a325993,mot-tp-tg-a326396,mot-def-ear-a326479,mot-chr-tg-a325854,mot-chr-tg-a326042,mot-tp-tg-a326478,mot-tp-tg-a325987,mot-tp-tg-a326327,mot-chr-tg-a325486,mot-tp-tg-a325610,mot-chr-tg-a325789,mot-rbblk-ear-a326108,mot-tp-tg-a326473,mot-tp-tg-a325963,mot-tp-tg-a326295,mot-chr-sg-a325546,mot-chr-tg-a326080,mot-tp-tg-a326182,mot-chr-tg-a325784,mot-tp-tg-a325898,mot-tp-tg-a326221,mot-tp-tg-a326298,mot-def-tg-a325623,mot-tp-tg-a326349,mot-tp-tg-a325966,mot-tp-tg-a326185,mot-chr-tg-a326588,mot-tp-tg-a326243,mot-tp-tg-a326231,mot-tp-sg-a325540,mot-chr-tg-a326514,mot-tp-tg-a326212,mot-chr-tg-a325782,mot-tp-tg-a326320,mot-tp-tg-a326426,mot-tp-tg-a325815,mot-tp-tg-a325430,mot-chr-tg-a326071,mot-tp-tg-a325846,mot-chr-tg-a326014,mot-tp-tg-a326255,mot-tp-tg-a326332,mot-chr-tg-a326115,mot-tp-tg-a326250,mot-def-tg-a325503,mot-chr-sg-a325722,mot-tp-tg-a326438,mot-tp-tg-a325644,mot-chr-tg-a325852,mot-tp-ear-a326452,mot-tp-tg-a326015,mot-tp-ear-a325961,mot-tp-tg-a325810,mot-chr-tg-a326552,mot-tp-tg-a326572,mot-tp-tg-a326421,mot-tp-ear-a326123,mot-tp-ear-a326248,mot-tp-tg-a326522,mot-tp-tg-a326306,mot-tp-tg-a326495,mot-tp-tg-a326128,mot-tp-tg-a326058,mot-tp-tg-a325745,mot-tp-tg-a326490,mot-chr-tg-a325744,mot-rbblk-ear-a325633,mot-tp-tg-a326344,mot-chr-tg-a326033,mot-chr-sg-a325703,mot-tp-tg-a326197,mot-tp-tg-a325752,mot-chr-sg-a325455,mot-fcg-ear-a325461,mot-tp-tg-a326122,mot-tp-tg-a326165,mot-def-tg-a326606,mot-tp-tg-a326141,mot-tp-tg-a326235,mot-tp-tg-a326456,mot-chr-tg-a325666,mot-chr-sg-a325586,mot-tp-tg-a326228,mot-rbblk-ear-a326043,mot-chr-tg-a325875,mot-def-tg-a326531,mot-tp-tg-a326139,mot-tp-tg-a326103,mot-tp-tg-a325922,mot-def-tg-a325425,mot-rbblk-ear-a325855,mot-chr-tg-a326099,mot-rbblu-ear-a326116,mot-chr-ear-a326533,mot-tp-tg-a326172,mot-chr-tg-a325508,mot-tp-tg-a326350,mot-tp-ear-a326538,mot-chr-tg-a326025,mot-tp-tg-a326216,mot-tp-tg-a326451,mot-chr-tg-a326602,mot-chr-sg-a325726,mot-def-sg-a325453,mot-tp-tg-a325890,mot-tp-tg-a326278,mot-chr-tg-a326563,mot-tp-tg-a326146,mot-tp-tg-a326158,mot-tp-tg-a326557,mot-chr-sg-a325447,mot-chr-tg-a325743,mot-tp-tg-a326386,mot-chr-ear-a325903,mot-chr-tg-a325849,mot-rbbrw-ear-a326024,mot-fcb-ear-a326590,mot-tp-tg-a326127,mot-chr-tg-a326607,mot-chr-tg-a325762,mot-tp-tg-a326204,mot-tp-tg-a325845,mot-tp-tg-a326324,mot-tp-tg-a326292,mot-tp-tg-a326223,mot-tp-tg-a325888,mot-chr-tg-a325543,mot-chr-tg-a325776,mot-tp-tg-a326559,mot-tp-tg-a326162,mot-tp-tg-a326181,mot-chr-tg-a326096,mot-def-sg-a325602,mot-tp-tg-a325967,mot-tp-tg-a326206,mot-chr-tg-a325839,mot-tp-tg-a326388,mot-tp-tg-a325955,mot-tp-tg-a325544,mot-tp-tg-a325979,mot-tp-tg-a325811,mot-tp-tg-a325481,mot-tp-tg-a326458,mot-tp-tg-a325652,mot-tp-tg-a326201,mot-tp-ear-a326319,mot-tp-tg-a326131,mot-tp-tg-a326220,mot-tp-tg-a326460,mot-chr-tg-a325911,mot-tp-tg-a326439,mot-chr-tg-a326560,mot-chr-tg-a326048,mot-chr-tg-a325982,mot-tp-tg-a326164,mot-tp-tg-a326222,mot-tp-ear-a325399,mot-rbbrw-ear-a325678,mot-fcg-ear-a325431,mot-tp-sg-a325404,mot-chr-tg-a325797,mot-chr-tg-a325805,mot-tp-tg-a326549,mot-tp-tg-a325882,mot-tp-sg-a325548,mot-tp-ear-a325440,mot-tp-tg-a326328,mot-chr-tg-a326536,mot-tp-tg-a326296,mot-tp-tg-a326169,mot-chr-tg-a325920,mot-tp-tg-a325793,mot-def-tg-a325436,mot-tp-ear-a326551,mot-tp-tg-a326450,mot-tp-sg-a325517,mot-chr-tg-a325944,mot-tp-tg-a325510,mot-tp-tg-a325387,mot-tp-tg-a326126,mot-chr-sg-a325713,mot-tp-tg-a326361,mot-tp-tg-a326253,mot-chr-tg-a326036,mot-tp-tg-a326176,mot-rbblk-ear-a326068,mot-tp-tg-a326157,mot-tp-tg-a326311,mot-chr-tg-a325829,mot-tp-tg-a326121,mot-tp-tg-a325712,mot-tp-tg-a326239,mot-tp-tg-a326145,mot-tp-tg-a326190,mot-tp-tg-a326429,mot-chr-tg-a325836,mot-tp-tg-a326140,mot-tp-tg-a326455,mot-chr-tg-a325886,mot-chr-tg-a325800,mot-tp-ear-a326260,mot-chr-ear-a325891,mot-tp-tg-a325767,mot-chr-sg-a325643,mot-tp-tg-a325870,mot-chr-tg-a326093,mot-chr-tg-a325386,mot-tp-tg-a326246,mot-chr-tg-a325867,mot-tp-tg-a326384,mot-rbblu-ear-a325747,mot-chr-tg-a326535,mot-tp-tg-a326290,mot-tp-tg-a326269,mot-tp-tg-a326346,mot-tp-tg-a326459,mot-tp-tg-a326435,mot-chr-ear-a326516,mot-chr-sg-a325370,mot-tp-ear-a326377,mot-def-ear-a325488,mot-tp-tg-a325893,mot-tp-tg-a326574,mot-chr-tg-a325828,mot-chr-tg-a326600,mot-tp-tg-a326480,mot-tp-ear-a326430,mot-tp-tg-a326175,mot-rbblu-ear-a326029,mot-tp-tg-a326144,mot-tp-ear-a325761,mot-tp-tg-a326120,mot-tp-sg-a325446,mot-chr-tg-a326592,mot-tp-tg-a326372,mot-tp-tg-a325862,mot-def-sg-a325456,mot-tp-tg-a325994,mot-tp-tg-a326322,mot-tp-tg-a326485,mot-tp-tg-a326199,mot-tp-tg-a326308,mot-tp-tg-a326086,mot-tp-tg-a325374,mot-chr-tg-a326477,mot-chr-tg-a325400,mot-chr-tg-a326597,mot-chr-tg-a325929,mot-chr-tg-a325861,mot-tp-tg-a326149,mot-tp-tg-a325569,mot-def-tg-a326510,mot-tp-tg-a326202,mot-chr-tg-a326100,mot-chr-tg-a325791,mot-chr-ear-a326028,mot-chr-tg-a325936,mot-tp-sg-a325395,mot-chr-tg-a325669,mot-chr-ear-a325664,mot-chr-sg-a325609,mot-tp-tg-a326423,mot-tp-sg-a325629,mot-tp-tg-a326404,mot-tp-tg-a326375,mot-chr-tg-a325908,mot-chr-tg-a325592,mot-chr-tg-a326571,mot-tp-tg-a326003,mot-chr-tg-a326088,mot-tp-tg-a325928,mot-chr-ear-a325774,mot-chr-tg-a326045,mot-tp-tg-a326433,mot-rbbrw-ear-a325804,mot-tp-tg-a325930,mot-tp-tg-a326565,mot-tp-tg-a325783,mot-chr-tg-a325787,mot-chr-tg-a325768,mot-chr-tg-a325934,mot-chr-tg-a325953,mot-tp-sg-a325432,mot-tp-tg-a326135,mot-tp-tg-a325790,mot-chr-tg-a325989,mot-tp-tg-a326325,mot-tp-sg-a325487,mot-tp-ear-a326173,mot-tp-tg-a326267,mot-fcb-ear-a325485,mot-tp-tg-a326407,mot-tp-ear-a326488,mot-fcg-ear-a325701,mot-rbbrw-ear-a325405,mot-chr-tg-a325775,mot-chr-tg-a325794,mot-rbblu-ear-a325807,mot-tp-sg-a325557,mot-tp-tg-a326200,mot-def-tg-a326513,mot-chr-tg-a326083,mot-chr-tg-a326038,mot-fcg-ear-a325371,mot-chr-tg-a325941,mot-tp-tg-a325795,mot-tp-tg-a326286,mot-tp-tg-a326130,mot-chr-sg-a325551,mot-def-tg-a325697,mot-tp-tg-a326192,mot-tp-tg-a326205,mot-chr-tg-a326540,mot-tp-tg-a326370,mot-chr-tg-a326095,mot-tp-tg-a325757,mot-tp-tg-a326382,mot-tp-tg-a325973,mot-chr-tg-a325484,mot-tp-tg-a326166,mot-tp-tg-a326274,mot-tp-ear-a326072,mot-fcg-ear-a325383,mot-chr-tg-a325833,mot-tp-tg-a326402,mot-chr-sg-a325670,mot-tp-tg-a325648,mot-tp-tg-a326242,mot-tp-sg-a325479,mot-rbblu-ear-a325803,mot-chr-sg-a325555,mot-tp-tg-a325821,mot-chr-tg-a325469,mot-tp-tg-a326425,mot-tp-tg-a325763,mot-tp-tg-a326280,mot-tp-tg-a325927,mot-tp-tg-a326064,mot-tp-tg-a326209,mot-chr-sg-a325733,mot-chr-sg-a325531,mot-tp-tg-a325910,mot-tp-tg-a326160,mot-tp-tg-a326336,mot-chr-tg-a326056,mot-tp-tg-a326312,mot-chr-sg-a325505,mot-tp-tg-a325667,mot-chr-tg-a325940,mot-tp-tg-a326482,mot-tp-tg-a326230,mot-tp-tg-a326261,mot-chr-tg-a326082,mot-chr-tg-a325837,mot-chr-tg-a325801,mot-tp-tg-a325403,mot-tp-tg-a326191,mot-tp-tg-a326285,mot-def-tg-a325603,mot-tp-tg-a326468,mot-chr-tg-a325464,mot-tp-tg-a326211,mot-chr-tg-a326587,mot-tp-tg-a326413,mot-chr-tg-a325856,mot-chr-tg-a326044,mot-chr-sg-a325409,mot-chr-tg-a326537,mot-tp-tg-a325802,mot-tp-tg-a326297,mot-chr-tg-a326032,mot-chr-tg-a325748,mot-chr-ear-a326021,mot-tp-tg-a325465,mot-tp-tg-a326196,mot-tp-tg-a325991,mot-tp-tg-a325996,mot-tp-tg-a326177,mot-tp-tg-a325799,mot-chr-tg-a325919,mot-tp-sg-a325710,mot-chr-tg-a325907,mot-tp-tg-a326355,mot-chr-tg-a326075,mot-tp-tg-a326401,mot-tp-tg-a326004,mot-chr-sg-a325470,mot-tp-tg-a326294,mot-tp-tg-a326054,mot-tp-tg-a326213,mot-chr-tg-a326041,mot-tp-tg-a326218,mot-chr-tg-a325942,mot-chr-sg-a325502,mot-chr-tg-a325872,mot-tp-tg-a326117,mot-tp-tg-a325917,mot-tp-sg-a325604,mot-def-tg-a326564,mot-tp-tg-a326244,mot-chr-tg-a325567,mot-chr-tg-a325909,mot-def-tg-a325434,mot-tp-tg-a326105,mot-tp-tg-a325986,mot-chr-tg-a325980,mot-chr-sg-a325451,mot-tp-tg-a326333,mot-chr-ear-a325918,mot-tp-tg-a326233,mot-tp-tg-a326264,mot-chr-ear-a325851,mot-tp-sg-a325393,mot-chr-tg-a326057,mot-chr-tg-a325838,mot-tp-tg-a325751,mot-tp-tg-a325857,mot-tp-tg-a326398,mot-chr-sg-a325581,mot-chr-tg-a326518,mot-tp-tg-a326259,mot-tp-tg-a325972,mot-chr-tg-a326575,mot-tp-tg-a326184,mot-tp-tg-a326463,mot-tp-sg-a325452,mot-chr-sg-a325482,mot-chr-tg-a326109,mot-tp-ear-a325950,mot-chr-tg-a325396,mot-chr-sg-a325634,mot-tp-tg-a326251,mot-tp-tg-a326112,mot-tp-ear-a326287,mot-tp-tg-a326580,mot-tp-tg-a325931,mot-tp-tg-a326326,mot-chr-tg-a325985,mot-rbblu-ear-a325899,mot-tp-tg-a326523,mot-chr-tg-a325719,mot-tp-tg-a326441,mot-tp-tg-a326321,mot-tp-tg-a326061,mot-chr-tg-a325459,mot-rbblu-ear-a325779,".contains(sku+",")){
					    				System.out.println(sku+"::"+mobileName);
					    			}
					    			mobile = this.skuVsPhoneMap.get(sku);
					    		}
					    		if(mobile == null){
					    			System.out.println("Missing Mobile Name for Sku : \t"+sku);
					    			System.exit(0);

//					    			if(sku.indexOf("-sg") != -1 && sku.split("-").length == 3){
//					    				mobile = mobileName.substring(mobileName.indexOf("for ")+4,mobileName.length()).trim();
//					    			}else{
//					    				mobile = mobileName.substring(mobileName.indexOf("for ")+4,mobileName.length()).trim();
//					    				mobile = mobile.substring(0,mobile.indexOf("(")-1).trim();
//					    			}
					    		}
					    	}
					    	if(cellPointer == 6){
					    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    		if(cell.getStringCellValue() == null || "".equals(cell.getStringCellValue())){
					    			nonLiveListing = true;
					    			break;
					    		}
					    	}
					    	if(cell == null){
					    		cell = row.createCell(cellPointer);
					    	}
					    	
					    	if(cellPointer == 15){
					    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    		String sla = cell.getStringCellValue();
					    		eachRowtring += sla;
					    	}
					    	
					    	if(cellPointer == 16){
					    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    		String status = cell.getStringCellValue();
					    		if("INACTIVE".equals(status)){
					    			status="i";
					    		}else if("ACTIVE".equals(status)){
					    			status="a";
					    		}
					    		eachRowtring += ","+status;
					    	}
					    	
					    	if(cellPointer == 18){
					    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    		String procurement = cell.getStringCellValue();
					    		if("domestic procurement".equals(procurement)){
					    			procurement="d";
					    		}else if("instock".equals(procurement)){
					    			procurement="i";
					    		}else if("express".equals(procurement)){
					    			procurement="e";
					    		}
					    		eachRowtring += ","+procurement;
					    	}
					    	
					    }
					    if(!nonLiveListing && (eachRowtring.isEmpty() || eachRowtring.indexOf(",") == -1 || eachRowtring.split(",").length < 3)){
					    	System.out.println("No SLA or No Status or No Procurement present for SKU : "+sku);
					    	System.exit(0);
					    }
					    if(mobile != null && sku != null){
					    	
					    	String[] tokens = sku.split("-");
							if(tokens.length == 3){//Do not extract combos for model status. there will be conflicts if we consider combos.
								updateMapForSingleListing(list, mobile, eachRowtring, tokens[1]);
							}else if(tokens.length > 3){
								updateMapForSingleListing(list, mobile, eachRowtring, tokens[1]);
								updateMapForSingleListing(list, mobile, eachRowtring, tokens[2]);
							}
					    }
				    }else{
				    	break;
				    }
				    rowPOinter++;
			    }
			    file.close();
			    return list;
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
		return null;
	}

	private void makeListingsLive(File fileq) throws IOException {
		// TODO Auto-generated method stub
		FileInputStream file = null;
		FileOutputStream output_file = null;
		HSSFWorkbook workbook = null;
		Map<String, HashMap<String, String>> map1 = loadLiveProducts();
		if(map1 == null){
			System.out.println("Empty uniqueMobileList.txt");
			return;
		}
		try {
				file = new FileInputStream(fileq);
			    workbook = new HSSFWorkbook(file);
			    HSSFSheet sheet = workbook.getSheetAt(0);
			    boolean flag = true;
			    int rowPOinter = 2;
			    while(flag){
				    Row row = sheet.getRow(rowPOinter);
				    if(row != null){
				    	String mobileName = null;
				    	String sku = null;
					    for(int cellPointer=3; cellPointer < 27; cellPointer++) {
					    	Cell cell = row.getCell(cellPointer);
					    	if(cell == null){
					    		continue;
					    	}
					    	if(cellPointer==3){
					    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    		mobileName = cell.getStringCellValue();
//					    		if(cell == null || "".equals(cell.getStringCellValue())){
//					    			System.out.println("exiting row : "+rowPOinter);
//					    			break;
//					    		}
					    	}
					    	if(cellPointer == 5){
					    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    		sku = cell.getStringCellValue();
					    	}
//					    	if(cell == null){
//					    		cell = row.createCell(cellPointer);
//					    	}

					    	if(cellPointer == 6){
					    		cell.setCellValue(this.getConfigProp().getProperty("mrp"));
					    	}
					    	if(cellPointer == 8){
					    		cell.setCellValue("YES");
					    	}
					    	if(cellPointer == 9){
					    		cell.setCellValue(this.getConfigProp().getProperty("shippinglocal"));
					    	}
					    	if(cellPointer == 10){
					    		cell.setCellValue(this.getConfigProp().getProperty("shippingzonal"));
					    	}
					    	if(cellPointer == 11){
					    		cell.setCellValue(this.getConfigProp().getProperty("shippingnational"));
					    	}
					    	if(cellPointer == 14){
					    		cell.setCellValue(this.getConfigProp().getProperty("count"));
					    	}
					    	if(cellPointer == 18){
					    		cell.setCellValue("seller");
					    	}
					    	if(cellPointer == 19){
					    		cell.setCellValue(this.getConfigProp().getProperty("length"));
					    	}
					    	if(cellPointer == 20){
					    		cell.setCellValue(this.getConfigProp().getProperty("breadth"));
					    	}
					    	if(cellPointer == 21){
					    		cell.setCellValue(this.getConfigProp().getProperty("height"));
					    	}
					    	if(cellPointer == 22){
					    		cell.setCellValue(this.getConfigProp().getProperty("weight"));
					    	}
					    	if(cellPointer == 24){
					    		cell.setCellValue(this.getConfigProp().getProperty("hsn"));
					    	}
					    	if(cellPointer == 25){
					    		cell.setCellValue(this.getConfigProp().getProperty("tax"));
					    	}
					    	
					    	String mobile = null;
				    		if(sku != null && !"".equals(sku)){
				    			mobile = this.skuVsPhoneMap.get(sku);
				    		}
				    		if(sku != null && mobile == null){
				    			System.out.println("Missing Mobile Name for Sku : \t"+sku);
				    			System.exit(0);

//				    			if(sku != null && sku.indexOf("-sg") != -1 && sku.split("-").length == 3){
//				    				mobile = mobileName.substring(mobileName.indexOf("for ")+4,mobileName.length()).trim();
//				    			}else if(sku != null){
//				    				mobile = mobileName.substring(mobileName.indexOf("for ")+4,mobileName.length()).trim();
//				    				mobile = mobile.substring(0,mobile.indexOf("(")-1).trim();
//				    			}
				    		}
					    	//populate price
					    	if(cellPointer == 7 && mobile != null && sku != null){
//					    		String price = getPriceList(mobile+":"+sku);
					    		String price = "";
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
					    		if(StringUtils.isNoneBlank(sellingPrice)){
					    			cell.setCellValue(sellingPrice);
					    		}
					    	}
					    	boolean forAmr = cellPointer == 15 || cellPointer == 16 || cellPointer == 23;
					    	if(map1.get(mobile) != null && forAmr){
					    		String[] tokens = sku.split("-");
								List<String> lst = new ArrayList<>();
								if(tokens.length > 3){
									String res1 = tokens[1].equals("ear") ? "2,a,e" : map1.get(mobile).get(tokens[1]);
									String res2 = tokens[2].equals("ear") ? "2,a,e" : map1.get(mobile).get(tokens[2]);
									String[] red1 = res1.split(",");
									String[] red2 = res2.split(",");
									String[] final1 =  new String[3];
									if(new Integer(red1[0]) > new Integer(red2[0])){
										final1[0] = red1[0];
									}else{
										final1[0] = red2[0];
									}
									
									if(red1[1].equals("i") || red2[1].equals("i")){
										final1[1] = "INACTIVE";
									}else{
										final1[1] = "ACTIVE";
									}
									
									if(red1[2].equals("d") || red2[2].equals("d")){
										final1[2] = "domestic procurement";
									}else if(red1[2].equals("i") || red2[2].equals("i")){
										final1[2] = "instock";
									}else if(red1[2].equals("e") && red2[2].equals("e")){
										final1[2] = "express";
									}
									
									
									if(cellPointer == 15){
						    			cell.setCellValue(final1[0]);
						    		}else if(cellPointer == 16){
						    			cell.setCellValue(final1[1]);
						    		}else if(cellPointer == 23){
						    			cell.setCellValue(final1[2]);
						    		}
									
								}else{
									String res1 = map1.get(mobile).get(tokens[1]);
									String[] final1 = new String[3];
									String[] red1 = res1.split(",");
									final1[0] = red1[0];
									if(red1[1].equals("a")){
										final1[1] = "ACTIVE";
									}else{
										final1[1] = "INACTIVE";
									}
									if(red1[2].equals("d")){
										final1[2] = "domestic procurement";
									}else if(red1[2].equals("i")){
										final1[2] = "instock";
									}else if(red1[2].equals("e")){
										final1[2] = "express";
									}
									
									if(cellPointer == 15){
						    			cell.setCellValue(final1[0]);
						    		}else if(cellPointer == 16){
						    			cell.setCellValue(final1[1]);
						    		}else if(cellPointer == 23){
						    			cell.setCellValue(final1[2]);
						    		}
								}
					    	}else{
					    		if(cellPointer == 15){
					    			cell.setCellValue("1");
					    		}else if(cellPointer == 16){
					    			cell.setCellValue("INACTIVE");
					    		}else if(cellPointer == 23){
					    			cell.setCellValue("instock");
					    		}
					    	}
					    }
					    System.out.println("Row complete : "+rowPOinter+", SKU : "+sku);
				    }else{
				    	System.out.println("exiting row : "+rowPOinter);
				    	break;
				    }
				    rowPOinter++;
			    }
			    file.close();
			    output_file =new FileOutputStream(fileq);
			    workbook.write(output_file);
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
			if(output_file != null){
				try {
					output_file.close();
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
	}
	

	private Map<String, HashMap<String, String>> loadLiveProducts() throws IOException {
		// TODO Auto-generated method stub
		Map<String, HashMap<String, String>> map1 = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader("./uniqueMobileList.txt"))) {
			String line = "";
			while ((line = br.readLine()) != null) {
				if (!"".equals(line.trim())) {
					String[] tokens = line.split(":");
					String[] var1 = tokens[1].split("\t");
					HashMap<String, String> map2 = new HashMap<>();
					for(String var2 : var1){
						map2.put(var2.split("-")[0], var2.split("-")[1]);
					}
					if(map1.get(tokens[0]) != null){
						map1.get(tokens[0]).putAll(map2);
					}else{
						map1.put(tokens[0], map2);
					}
				}
			}
		}
		return map1.size() > 0 ? map1 : null;
	}
	
	private String getPriceList(String nameVsSkus) {
		
		String priceList = "";
		for(String sku : nameVsSkus.split(";")){
			if(sku.indexOf(":") == -1){
				System.out.println("ERROR IN INPUT MISSING :");
				return null;
			}
			
			String name = sku.split(":")[0];
//			if(name.indexOf("Mozette") != -1){
//				name = name.substring(name.indexOf("Mozette ")+8, name.indexOf(" for"));
//			}else if(name.indexOf("XOLDA") != -1){
//				name = name.substring(name.indexOf("XOLDA ")+6, name.indexOf(" for"));
//			}else if(name.indexOf("Motaz") != -1){
//				name = name.substring(name.indexOf("Motaz ")+6, name.indexOf(" for"));
//			}else if(name.indexOf("ZOTIKOS") != -1){
//				name = name.substring(name.indexOf("ZOTIKOS ")+8, name.indexOf(" for"));
//			}
			String skus = sku.split(":")[1];
			
			if(StringUtils.isBlank(name)){
				System.out.println("ERROR IN INPUT MISSING CASE TYPE");
				return null;
			}
			String pricingMap = this.getConfigProp().getProperty("price");
//			String[] rates = getPricingMap().get(getPricingLevelMap().get(name)).split(",");
			String[] rates = pricingMap.split(",");
			
			Map<String, String> rateMap  = new HashMap<>();
			
			for(String rate : rates){
				rateMap.put(rate.split(":")[0], rate.split(":")[1]);
			}
			for(String sku1 :skus.split(",")){
				String[] tokens = sku1.split("-");
				if(tokens.length > 3){
					if(rateMap.containsKey(tokens[1]+"+"+tokens[2])){
						priceList += rateMap.get(tokens[1]+"+"+tokens[2])+",";
					}else if(rateMap.containsKey(tokens[2]+"+"+tokens[1])){
						priceList += rateMap.get(tokens[2]+"+"+tokens[1])+",";
					}else{
						System.out.println("Price missed");
					}
				}else{
					if(rateMap.containsKey(tokens[1])){
						priceList += rateMap.get(tokens[1])+",";
					}else{
						System.out.println("missed");
						return null;
					}
				}
			}
		}
			
		return priceList.length() > 0 ? priceList.substring(0, priceList.length()-1): "";
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

	private Map<String, String> loadpurchaseMapMap() {
		String pricingMap = this.getConfigProp().getProperty("purchase");
		String[] rates = pricingMap.split(",");
		Map<String, String> rateMap  = new HashMap<>();
		for(String rate : rates){
			rateMap.put(rate.split(":")[0], rate.split(":")[1]);
		}
		return rateMap;
	}

	public Map<String, String> getPricingMap() {
		return pricingMap;
	}

	public void setPricingMap(Map<String, String> pricingMap) {
		this.pricingMap = pricingMap;
	}

	public Properties getConfigProp() {
		return configProp;
	}

	public void setConfigProp(Properties configProp) {
		this.configProp = configProp;
	}
}
