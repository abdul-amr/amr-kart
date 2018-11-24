package com.amr.main;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

import com.google.gson.Gson;

import au.com.bytecode.opencsv.CSVReader;
import test.EmailUtils;

import com.amr.main.pojo.UpdateFlipkartListingResponse;
import com.amr.main.pojo.BulkFlipkartListings;
import com.amr.main.pojo.BulkUpdateFlipkartListingResponse;
import com.amr.main.pojo.FlipkartListing;

public class ListingsStatus {
	

	private Properties configProp;
	public static String healthCheck;
	public Map<String, HashMap<String, String>> masterMobileMap;
	public Map<String, TreeMap<String, String>> correctedMissingSingleButComboPresent;//If the single listing is missing and combo is present then this program was making those listings INACTIVE. To avoid that we have corrected status of these listings
	public List<String> oldSkus;//stores all old skus which do not have -tp -def etc... and prints them in the console to manually update
	public String extractProcurementType;
	public String extractSLA;
	public String extractStatus;
	public List<String> failedObjects;
//	public String verifiedCatalogFiles;
	public Map<String, String> skuVsPhoneMap;
	public Map<String, Set<String>> missingSingleButComboPresent;
	public BulkFlipkartListings bulkFlipkartListings;
	private int attempt = 0;
	private String token;
	private EmailUtils emailUtils;
	private List<String> filePaths;
	private String sellerName;
	private String allModelsUpdate;//If Y all models of any changedMobile will be updates, If N only changed modified models of changedMobile will be updated

	public ListingsStatus() throws IOException {
		configProp = loadProperties();
		oldSkus = new ArrayList<>();
		failedObjects = new ArrayList<>();
		skuVsPhoneMap = new HashMap<>();
		missingSingleButComboPresent = new HashMap<>();
		this.bulkFlipkartListings = new BulkFlipkartListings();
		this.bulkFlipkartListings.setListings(new ArrayList<>());
		this.allModelsUpdate = "N";
		emailUtils = new EmailUtils();
		filePaths = new ArrayList<>();
		filePaths.add("./uniqueMobileList.txt");
		filePaths.add("./code/config.properties");
		this.sellerName = this.getConfigProp().getProperty("seller");
	}
	
	public static void main(String[] args) throws Exception {
		ListingsStatus iU = new ListingsStatus();
		if(iU.getConfigProp() == null){
			System.out.println("Config Properties cannot be loaded");
			return;
		}
		ListingsStatus.healthCheck = iU.getConfigProp().getProperty("healthCheck");

		iU.extractProcurementType = iU.getConfigProp().getProperty("extractProcurementType");
		iU.extractSLA = iU.getConfigProp().getProperty("extractSLA");
		iU.extractStatus = iU.getConfigProp().getProperty("extractStatus");
//		iU.verifiedCatalogFiles = iU.getConfigProp().getProperty("verifiedCatalogFiles");
		String inventoryFileName = iU.getConfigProp().getProperty("inventoryfile");
		File invFile = new File(inventoryFileName);
		if("MobileList".equals(args[0]) && inventoryFileName.endsWith("xls")){
			System.out.println("Loading Unique Mobile List Status");
			//Loading the skuVsPhone Map
//			iU.skuVsPhoneMap = iU.loadSkuVsPhoneMap();
			iU.skuVsPhoneMap = iU.loadSkuVsPhoneMapFromCentralLocation();
			Map<String, TreeMap<String, String>> list = iU.getUniqueMobileNames(invFile);
			iU.writeToFile(list);
		}else if("Update".equals(args[0]) && inventoryFileName.endsWith("xls")){
			System.out.println("Loading Changed Mobile List Status");
			iU.token = iU.getFlipkartToken();
			iU.allModelsUpdate = iU.getConfigProp().getProperty("updateallmodels");
//			iU.skuVsPhoneMap = iU.loadSkuVsPhoneMap();
			iU.skuVsPhoneMap = iU.loadSkuVsPhoneMapFromCentralLocation();
			iU.correctedMissingSingleButComboPresent = iU.loadCorrectedMissingSingleButComboPresent();
			Map<String, HashMap<String, String>> changedMobiles = iU.loadUpdatedMobiles();//This will find the difference between updatedMobileList.txt and uniqueMobileList.txt. Just to update only those mobiles whose value is changed
			iU.updateListings(invFile,changedMobiles);
		}
		
//		else if("LoadSkuvsPhoneFromCatalogFiles".equals(args[0]) && "excel".equals(ListingsStatus.fileType)){
//			System.out.println("Loading Sku vs Phone From Catalog Files");
//			Map<String, String> skuVsPhone = iU.loadSkusFromCatalog(iU.verifiedCatalogFiles);
//			iU.writeSkusToFile(skuVsPhone);
//			
//			//cross check if all skus has phone name
//			System.out.println("\n\nCross Checking if any SKU is missing the mobile name ::");
//			iU.crossCheckIfAllSkusHasNameExcel(invFile);
//			
//		}
		
		
		
		else if("MobileList".equals(args[0]) && inventoryFileName.endsWith("csv")){
			System.out.println("Loading Unique Mobile List Status");
			//Loading the skuVsPhone Map
//			iU.skuVsPhoneMap = iU.loadSkuVsPhoneMap();
			iU.skuVsPhoneMap = iU.loadSkuVsPhoneMapFromCentralLocation();
			Map<String, TreeMap<String, String>> list = iU.getUniqueMobileNamesCSV(invFile);
			iU.writeToFile(list);
		}else if("Update".equals(args[0]) && inventoryFileName.endsWith("csv")){
			System.out.println("Loading Changed Mobile List Status");
			iU.token = iU.getFlipkartToken();
			iU.allModelsUpdate = iU.getConfigProp().getProperty("updateallmodels");
//			iU.skuVsPhoneMap = iU.loadSkuVsPhoneMap();
			iU.skuVsPhoneMap = iU.loadSkuVsPhoneMapFromCentralLocation();;
			iU.correctedMissingSingleButComboPresent = iU.loadCorrectedMissingSingleButComboPresent();
			Map<String, HashMap<String, String>> changedMobiles = iU.loadUpdatedMobilesCSV();//This will find the difference between updatedMobileList.txt and uniqueMobileList.txt. Just to update only those mobiles whose value is changed
			iU.updateListingsCSV(invFile,changedMobiles);
		}
		
//		else if("LoadSkuvsPhoneFromCatalogFiles".equals(args[0]) && "csv".equals(ListingsStatus.fileType)){
//			System.out.println("Loading Sku vs Phone From Catalog Files");
//			Map<String, String> skuVsPhone = iU.loadSkusFromCatalog(iU.verifiedCatalogFiles);
//			iU.writeSkusToFile(skuVsPhone);
//			
//			//cross check if all skus has phone name
//			System.out.println("\n\nCross Checking if any SKU is missing the mobile name ::");
//			iU.crossCheckIfAllSkusHasName(invFile);
//			
//		}
	}
	
	
	
//	private void crossCheckIfAllSkusHasNameExcel(File invFile) throws IOException {
//		// TODO Auto-generated method stub
//		this.skuVsPhoneMap = this.loadSkuVsPhoneMap();
//
//		List<String> list = new ArrayList<>();
//		FileInputStream file = null;
//		HSSFWorkbook workbook = null;
//		Map<String, HashMap<String, String>> map = new HashMap<>();
//		try {
//				file = new FileInputStream(invFile);
//			    workbook = new HSSFWorkbook(file);
//			    HSSFSheet sheet = workbook.getSheetAt(0);
//			    boolean flag = true;
//			    int rowPOinter = 2;
//			    while(flag){
//				    Row row = sheet.getRow(rowPOinter);
//				    if(row != null){
//				    	String mobileName = null;
//				    	String sku = null;
//				    	String mobile = null;
//					    for(int cellPointer=3; cellPointer < 6; cellPointer++) {
//					    	Cell cell = row.getCell(cellPointer);
//					    	if(cellPointer==3){
//					    		cell.setCellType(Cell.CELL_TYPE_STRING);
//					    		mobileName = cell.getStringCellValue();
//					    	}
//					    	if(cellPointer == 5){
////					    		if("".equals(mobileName)){
////					    			mobileName = null;
////					    			continue;
////					    		}
//					    		cell.setCellType(Cell.CELL_TYPE_STRING);
//					    		sku = cell.getStringCellValue();
//					    		if(sku != null && !"".equals(sku)){
//					    			if(!this.skuVsPhoneMap.containsKey(sku)){
//					    				System.out.println("Missing SKU : "+sku);
//					    				list.add(sku);
//					    			}
//					    		}else{
//					    			System.out.println("SKU itself in the Listings Sheet is empty");
//					    		}
//					    	}
//					    }
//				    }else{
//				    	break;
//				    }
//				    rowPOinter++;
//			    }
//				writeFailedToFile(list, "missingSkus");
//			    file.close();
//		} catch (FileNotFoundException e) {
//		    e.printStackTrace();
//		} catch (IOException e) {
//		    e.printStackTrace();
//		}finally {
//			if(file != null){
//				try {
//					file.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			if(workbook != null){
//				try {
//					workbook.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//	}

//	private void crossCheckIfAllSkusHasName(File invFile) throws IOException {
//		// TODO Auto-generated method stub
//		this.skuVsPhoneMap = this.loadSkuVsPhoneMap();
//		List<String> list = new ArrayList<>();
//		Map<String, HashMap<String, String>> healthCheck = new HashMap<>();//checks if 2 skus of any mobile model is having different status/sla/procurement
//		CSVReader reader = null;
//		try {
//
//			reader = new CSVReader(new FileReader(invFile), ',');
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		String[] nextLine;
//		try {
//			int rowPointer = 0;
//			while ((nextLine = reader.readNext()) != null) {
//				if(nextLine.length == 0 || "".equals(nextLine[0])){
//					break;
//				}
//				if (rowPointer == 0) {
//					rowPointer++;
//					continue;
//				}
//		    	String sku = null;
//		    	String mobile = null;
//			    for(int cellPointer=2; cellPointer < 15; cellPointer++) {
//			    	String cell = nextLine[cellPointer];
//					if (cell == null || cell.isEmpty()) {
//						continue;
//					}
//					cell = cell.trim();
//			    	if(cellPointer == 3){
//			    		sku = cell;
//			    		if(sku != null && !"".equals(sku)){
//			    			if(!this.skuVsPhoneMap.containsKey(sku)){
//			    				System.out.println("Missing SKU : "+sku);
//			    				list.add(sku);
//			    			}
//			    		}else{
//			    			System.out.println("SKU itself in the Listings Sheet is empty");
//			    		}
//			    	}
//			    	
//			    }
//			    rowPointer++;
//			}
//			writeFailedToFile(list, "missingSkus");
//		}  catch (FileNotFoundException e) {
//		    e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//			
//		}
//	}

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

//	private Map<String, String> loadSkuVsPhoneMap() throws IOException {
//		Map<String, String> skuVsPhoneMap = new HashMap<>();
//		BufferedReader br1 = null;
//        String sCurrentLine;
//        br1 = new BufferedReader(new FileReader("./skuVsPhoneMapping.txt"));
//        while ((sCurrentLine = br1.readLine()) != null) {
//            if(sCurrentLine != null && !"".equals(sCurrentLine)){
//            	skuVsPhoneMap.put(sCurrentLine.split("::")[0].trim(), sCurrentLine.split("::")[1].trim());
//            }
//        }
//        
//        BufferedReader br2 = null;
//        br2 = new BufferedReader(new FileReader("./missingSkuVsPhoneMapping.txt"));
//        while ((sCurrentLine = br2.readLine()) != null) {
//            if(sCurrentLine != null && !"".equals(sCurrentLine)){
//            	skuVsPhoneMap.put(sCurrentLine.split("::")[0].trim(), sCurrentLine.split("::")[1].trim());
//            }
//        }
//		return skuVsPhoneMap;
//	}

	private void writeSkusToFile(Map<String, String> skuVsPhone) throws IOException {
		// TODO Auto-generated method stub
		File fout = new File("./skuVsPhoneMapping.txt");
		if(!fout.exists()){
			fout.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	 
		for(String sku : skuVsPhone.keySet()){
	    	String phone = skuVsPhone.get(sku);
	    	if(sku != null && !sku.isEmpty() && phone != null && !phone.isEmpty()){
	    		bw.write(sku+"::"+phone);
				bw.newLine();
	    	}
	    }
		bw.close();
	}

	private Map<String, String> loadSkusFromCatalog(String verifiedCatalogFiles2) throws Exception {
		Map<String, String> skuVsPhone = new HashMap<>();
		System.out.println(verifiedCatalogFiles2);
		File folder = new File(verifiedCatalogFiles2);
		if(folder.exists() && folder.isDirectory()){
			File[] files = folder.listFiles();
			if(files.length > 0){
				for(File eachFile : files){
					Map<String, String> skuVsPhoneForEachFile = extractPassedListings(eachFile);
					skuVsPhone.putAll(skuVsPhoneForEachFile);
				}
			}else {
				throw new Exception("Empty Verified Catalog Files");
			}
		}else {
			throw new Exception("Empty Verified Catalog Files");
		}
		return skuVsPhone.size() > 0 ? skuVsPhone : null;
	}

	private Map<String, String> extractPassedListings(File fileq) throws Exception {
		// TODO Auto-generated method stub
		Map<String, String> skuVsPhone = new HashMap<>();
		FileInputStream file = null;
		HSSFWorkbook workbook = null;
		try {
				file = new FileInputStream(fileq);
			    workbook = new HSSFWorkbook(file);
			    HSSFSheet sheet1 = null;
			    boolean extraSheet = false;
			    HSSFSheet sheet = workbook.getSheet("cases_covers");
			    String skuHeaderName = "Seller SKU ID";
			    String phoneHeader = "Designed For";
			    if(sheet != null){
			    	sheet1 = workbook.getSheet("mobile_accessories_combo");
			    	if(sheet1 != null){
			    		extraSheet = true;
			    	}
			    }
			    if(sheet == null){
			    	sheet = workbook.getSheet("mobile_accessories_combo");
				    skuHeaderName = "Seller SKU ID";
				    phoneHeader = "Suitable For";
			    }
			    if(sheet == null){
			    	sheet = workbook.getSheet("screen_guard");
				    skuHeaderName = "Seller SKU ID";
				    phoneHeader = "Designed For";
			    }
			    boolean flag = true;
			    int rowPointer = 0;
			    int skuCellPos = -1;
			    int phoneCellPos = -1;
			    int qcStatus = -1;
			    while(flag){
				    Row row = sheet.getRow(rowPointer);
				    if(row != null){
				    	String sku = "";
				    	String phone = "";
					    for(int cellPointer=1; cellPointer < 20; cellPointer++) {
					    	Cell cell = row.getCell(cellPointer);
					    	if(rowPointer == 0 && cell != null && skuHeaderName.equals(cell.getStringCellValue())){
					    		skuCellPos = cellPointer;
					    	}else if(rowPointer == 0 && cell != null && phoneHeader.equals(cell.getStringCellValue())){
					    		phoneCellPos = cellPointer;
					    		rowPointer = 3;
					    		break;
					    	}else if(rowPointer == 0 && cell != null && "QC Status".equals(cell.getStringCellValue())){
					    		qcStatus = cellPointer;
					    	}
					    	if(rowPointer > 3 && cell != null && cellPointer == qcStatus && ("Passed".equals(cell.getStringCellValue()) || "".equals(cell.getStringCellValue()))){//( || "AVPassed".equals(cell.getStringCellValue())
					    		if(skuCellPos != -1 && phoneCellPos != -1){
					    			Cell cell1 = row.getCell(skuCellPos);
					    			Cell cell2 = row.getCell(phoneCellPos);
					    			if(cell1 != null && cell2 != null){
					    				sku = cell1.getStringCellValue();
					    				phone = cell2.getStringCellValue();
//					    				System.out.println(sku+"\t"+phone);
					    				skuVsPhone.put(sku, phone);
					    				break;
					    			}else{
						    			throw new Exception("SKU ID and Desined For cells are null");
						    		}
					    		}else{
					    			throw new Exception("SKU ID and Desined For positions not found");
					    		}
					    	}
					    }
				    }else{
				    	if(extraSheet){
				    		sheet = sheet1;
				    		extraSheet = false;
				    		skuHeaderName = "Seller SKU ID";
						    phoneHeader = "Suitable For";
				    	}else{
				    		break;
				    	}
				    }
				    rowPointer++;
			    }
			    file.close();
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
		return skuVsPhone;
	}

	private void updateListingsCSV(File invFile, Map<String, HashMap<String, String>> changedMobiles) {
		// TODO Auto-generated method stub
		FileInputStream file = null;
		HSSFWorkbook workbook = null;
		Map<String, HashMap<String, String>> map = new HashMap<>();
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
					rowPointer++;
					continue;
				}
				String mobileName = null;
				String fsn = null;
				String listingId = null;
		    	String sku = null;
		    	String mobile = null;
			    for(int cellPointer=0; cellPointer < 4; cellPointer++) {
			    	String cell = nextLine[cellPointer];
			    	if (cell == null || cell.isEmpty()) {
						continue;
					}
					cell = cell.trim();
					if(cellPointer==0){
						fsn = cell;
					}
					if(cellPointer==1){
						listingId = cell;
					}
					if(cellPointer==2){
						mobileName = cell;
					}
			    	if(cellPointer == 3){
//			    		if("".equals(mobileName)){
//			    			mobileName = null;
//			    			continue;
//			    		}
			    		sku = cell;
			    		if(sku != null && !"".equals(sku)){
			    			mobile = this.skuVsPhoneMap.get(sku);
			    			mobile = mobile != null ? mobile.toLowerCase().trim() : null;
			    		}
			    		if(mobile == null){
			    			System.out.println("Missing Sku : \t"+sku);
			    			System.exit(0);
//		    				if(sku.indexOf("-sg") != -1 && sku.split("-").length == 3){
//		    					mobile = mobileName.substring(mobileName.indexOf("for ")+4,mobileName.length()).trim();
//		    				}else{
//		    					mobile = mobileName.substring(mobileName.indexOf("for ")+4,mobileName.length()).trim();
//		    					mobile = mobile.substring(0,mobile.indexOf("(")-1).trim();
//		    				}
			    		}
			    		mobileName = mobile;
			    		
			    		for(String changedMobile : changedMobiles.keySet()){
			    			if(changedMobile.contains(";;")){
			    				 String[] mobileArr = changedMobile.split(";;");
			    				 if(mobileName.equals(mobileArr[0])){
			    					 if(mobileArr[1].contains(",")){
			    						 String[] notContainingMobiles = mobileArr[1].split(",");
			    						 boolean notContains = false;
			    						 for(String notMobile : notContainingMobiles){
			    							 if(mobileName.equals(notMobile)){
			    								 notContains = true;
			    							 }
			    						 }
			    						 if(!notContains){
			    							 map.put(sku+";;"+mobileName+";;"+fsn+";;"+listingId, changedMobiles.get(changedMobile));
			    						 }
			    					 }else if(!mobileArr[1].isEmpty() && mobileName.equals(mobileArr[1])){
			    						 map.put(sku+";;"+mobileName+";;"+fsn+";;"+listingId, changedMobiles.get(changedMobile));
			    					 }
			    				 }
			    			}else if(mobileName.equals(changedMobile)){
			    				map.put(sku+";;"+mobileName+";;"+fsn+";;"+listingId, changedMobiles.get(changedMobile));
			    			}
			    		}
				    	
//				    	if(changedMobiles.keySet().contains(mobile)){
//				    		map.put(sku+";;"+mobile, changedMobiles.get(mobile));//all skus of that mobile
//				    		break;
//				    	}
			    	}
			    }
			    rowPointer++;
			}
			updateFlipkartInventory(map);
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
	}

	private Map<String, HashMap<String, String>> loadUpdatedMobilesCSV() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br1 = null;
        BufferedReader br2 = null;
        String sCurrentLine;
        List<String> list1 = new ArrayList<String>();
        List<String> list2 = new ArrayList<String>();
        br1 = new BufferedReader(new FileReader("./uniqueMobileList.txt"));
        br2 = new BufferedReader(new FileReader("./updatedMobileList.txt"));
        while ((sCurrentLine = br1.readLine()) != null) {
            list1.add(sCurrentLine);
        }
        while ((sCurrentLine = br2.readLine()) != null) {
            list2.add(sCurrentLine);
        }
        
        br1.close();
        br2.close();
        
        //load list1
        Map<String, HashMap<String, String>> map2 = new HashMap<>();
        for(int i=0;i<list1.size();i++){
            String[] mobileEntry = list1.get(i).split(":");
            String[] models = mobileEntry[1].split("\t");
            HashMap<String, String> map1 = new HashMap<>();
            for(String eachModel : models){
            	String[] modelInfo = eachModel.trim().split("-");
            	map1.put(modelInfo[0], modelInfo[1]);
            }
            map2.put(mobileEntry[0].trim(), map1);
        }
        
        this.masterMobileMap = map2;
        
        List<String> tmpList = new ArrayList<String>(list2);
        tmpList.removeAll(list1);
        Map<String, HashMap<String, String>> map = new HashMap<>();
        for(int i=0;i<tmpList.size();i++){
            System.out.println(tmpList.get(i)); //content from test2.txt which is not there in test.txt
            String[] mobileEntry = tmpList.get(i).split(":");
            String[] models = mobileEntry[1].split("\t");
            HashMap<String, String> map1 = new HashMap<>();
            for(String eachModel : models){
            	String[] modelInfo = eachModel.trim().split("-");
            	if(!"Y".equals(this.allModelsUpdate) && map2.get(mobileEntry[0].trim()) != null && map2.get(mobileEntry[0].trim()).get(modelInfo[0]) != null && map2.get(mobileEntry[0].trim()).get(modelInfo[0]).equals(modelInfo[1])){
            		continue;
            	}
            	map1.put(modelInfo[0], modelInfo[1]);
            }
            map.put(mobileEntry[0].trim(), map1);
        }
        System.out.println("Total Number of Objects to be Updated : "+map.size());
		return map;
	}

	public Map<String, TreeMap<String, String>> getUniqueMobileNamesCSV(File invFile) throws Exception {
		// TODO Auto-generated method stub
		Map<String, TreeMap<String, String>> list = new TreeMap<>();
		Map<String, Set<String>> comboList = new TreeMap<>();
		Map<String, TreeMap<String, String>> healthCheck = new TreeMap<>();//checks if 2 skus of any mobile model is having different status/sla/procurement
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
					rowPointer++;
					continue;
				}
		    	String mobileName = null;
		    	String sku = null;
		    	String mobile = null;
		    	String eachRowtring = "";
		    	boolean nonLiveListing = false;
		    	String sla="";
		    	String procurement="";
		    	String status="";
			    for(int cellPointer=2; cellPointer < 15; cellPointer++) {
			    	String cell = nextLine[cellPointer];
					if (cell == null || cell.isEmpty()) {
						continue;
					}
					cell = cell.trim();
			    	if(cellPointer==2){
			    		mobileName = cell;
			    	}
			    	if(cellPointer == 3){
//			    		if("".equals(mobileName)){
//			    			mobileName = null;
//			    			continue;
//			    		}
			    		sku = cell;
			    		if(sku != null && !"".equals(sku)){
			    			mobile = this.skuVsPhoneMap.get(sku);
			    			mobile = mobile != null ? mobile.toLowerCase().trim() : null;
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
			    		}
			    	}
			    	if(cellPointer == 4){
			    		if(cell== null || "".equals(cell)){
			    			nonLiveListing = true;
			    			break;
			    		}
			    	}
			    	
			    	if(cellPointer == 13){
			    		sla = cell;
			    	}
			    	
			    	if(cellPointer == 14){
			    		status = cell;
			    		if("INACTIVE".equals(status)){
			    			status="i";
			    		}else if("ACTIVE".equals(status)){
			    			status="a";
			    		}
			    	}
			    	
			    	if(cellPointer == 10){
			    		procurement = cell;
			    		if("domestic procurement".equals(procurement)){
			    			procurement="d";
			    		}else if("instock".equals(procurement)){
			    			procurement="i";
			    		}else if("express".equals(procurement)){
			    			procurement="e";
			    		}
			    	}
			    	
			    }
			    eachRowtring = sla+","+status+","+procurement;
			    if(!nonLiveListing && (eachRowtring.isEmpty() || eachRowtring.indexOf(",") == -1 || eachRowtring.split(",").length < 3)){
			    	System.out.println("No SLA or No Status or No Procurement present for SKU : "+sku);
			    	System.exit(0);
			    }
			    if(mobile != null && sku != null){
			    	
			    	
			    	String[] tokens = sku.split("-");
					if(tokens.length == 3){//Do not extract combos for model status. there will be conflicts if we consider combos.
						String model = tokens[1];
						if(list.get(mobile) != null){
							TreeMap<String, String> existingMap = list.get(mobile);
							if(existingMap.get(model) != null && !"".equals(model)){
								//check if the there are 2 models having different status
								String modelValue = existingMap.get(model);
								if(!modelValue.equals(eachRowtring)){
									String[] values = modelValue.split(",");
									String newmodelValue = eachRowtring;
									String[] newValues = newmodelValue.split(",");
									String finalString = "";
									if(new Integer(values[0]) > new Integer(newValues[0])){
										finalString = values[2].equals("e") ? newValues[0] : values[0];
									}else{
										finalString = newValues[2].equals("e") ? values[0] : newValues[0];
									}
									
									if("i".equals(values[1]) || "i".equals(newValues[1])){
										finalString+= ",i";
									}else {
										finalString+=",a";
									}
									
									if("d".equals(values[2]) || "d".equals(newValues[2])){
										finalString+= ",d";
									}else if("i".equals(values[2]) || "i".equals(newValues[2])){
										finalString+=",i";
									}else if("e".equals(values[2]) || "e".equals(newValues[2])){
										finalString+=",e";
									}
									
									if(procurementtypeCondition(finalString) && slaCondition(finalString) && listingStatusCondition(finalString)){
										existingMap.put(model, finalString);
										list.put(mobile, existingMap);
									}
									
									//health check
									if("Y".equals(ListingsStatus.healthCheck) && healthCheck.get(mobile) != null){
										TreeMap<String, String> oldValue = healthCheck.get(mobile);
										if(oldValue.get(model) != null){
											String oldValueS = oldValue.get(model);
											if(oldValueS.indexOf(finalString) == -1){
												oldValue.put(model, oldValueS+"/"+finalString);
												healthCheck.put(mobile,oldValue);
											}
											if(oldValueS.indexOf(modelValue) == -1){
												oldValue.put(model, oldValueS+"/"+finalString);
												healthCheck.put(mobile,oldValue);
											}
										}else{
											//new model
											oldValue.put(model, modelValue+"/"+finalString);
											healthCheck.put(mobile,oldValue);
										}
									}else if("Y".equals(ListingsStatus.healthCheck)){
										//new mobile
										TreeMap<String, String> newModel = new TreeMap<>();
										newModel.put(model, modelValue+"/"+finalString);
										healthCheck.put(mobile,newModel);
									}
								}
							}else{
								if(procurementtypeCondition(eachRowtring) && slaCondition(eachRowtring) && listingStatusCondition(eachRowtring)){
									existingMap.put(model, eachRowtring);
									list.put(mobile, existingMap);
								}
							}
						}else{
							if(procurementtypeCondition(eachRowtring) && slaCondition(eachRowtring) && listingStatusCondition(eachRowtring)){
								TreeMap<String, String> modelMap = new TreeMap<>();
								modelMap.put(model, eachRowtring);
								list.put(mobile, modelMap);
							}
						}
					}else if(tokens.length == 4){
						if(comboList.get(mobile) != null){
							comboList.get(mobile).add(tokens[1]);
							comboList.get(mobile).add(tokens[2]);
						}else{
							Set<String> set = new TreeSet<>();
							set.add(tokens[1]);
							set.add(tokens[2]);
							comboList.put(mobile, set);
						}
					}
			    }
		    
			    rowPointer++;
			}
			System.out.println("Total Rows Scanned : "+rowPointer);
			if("Y".equals(ListingsStatus.healthCheck)){
				return healthCheck;
			}else{
				//find the missing single but combo present
				comboList = filterMissingSingleFromCombo(list, comboList);
				writeToMissingFile(comboList);
				return list;
			}
		}  catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
		}
		return null;
	}

	private boolean listingStatusCondition(String eachRowtring) {
		if(this.extractStatus.equals("ALL")){
			return true;
		}else if(this.extractStatus.indexOf(eachRowtring.split(",")[1]) != -1){
			return true;
		}else{
			return false;
		}
	}

	private boolean procurementtypeCondition(String eachRowtring) {
		if(this.extractProcurementType.equals("ALL")){
			return true;
		}else if(this.extractProcurementType.indexOf(eachRowtring.split(",")[2]) != -1){
			return true;
		}else{
			return false;
		}
		
	}

	private boolean slaCondition(String eachRowtring) {
		if(this.extractSLA.equals("ALL")){
			return true;
		}
		String newSla = eachRowtring.split(",")[0];
		String[] conditions = this.extractSLA.split(",");
		boolean finalValue = true;
		for(String eachCondition : conditions){
			if(!eachCondition.isEmpty() && eachCondition.contains("<")){
				if(!(new Integer(newSla) < new Integer(this.extractSLA.replace("<", "")))){
					finalValue = false;
					break;
				}
			}else if(!this.extractSLA.isEmpty() && this.extractSLA.contains(">")){
				if(!(new Integer(newSla) > new Integer(this.extractSLA.replace(">", "")))){
					finalValue = false;
					break;
				}
			}else if(!this.extractSLA.isEmpty()){
				if(!(new Integer(newSla) == new Integer(this.extractSLA))){
					finalValue = false;
					break;
				}
			}
		}
		return finalValue;
	}

	private Map<String, TreeMap<String, String>> loadCorrectedMissingSingleButComboPresent() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br1 = null;
        String sCurrentLine;
        Map<String, TreeMap<String, String>> map2 = new HashMap<>();
        br1 = new BufferedReader(new FileReader("./missingSingleButComboPresentCorrected.txt"));
        while ((sCurrentLine = br1.readLine()) != null) {
        	String[] mobileEntry = sCurrentLine.split(":");
            String[] models = mobileEntry[1].split("\t");
            TreeMap<String, String> map1 = new TreeMap<>();
            for(String eachModel : models){
            	String[] modelInfo = eachModel.trim().split("-");
            	map1.put(modelInfo[0], modelInfo[1]);
            }
            map2.put(mobileEntry[0].trim(), map1);
        }
        
        br1.close();
        
        return  map2;
	}

	private Map<String, HashMap<String, String>> loadUpdatedMobiles() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br1 = null;
        BufferedReader br2 = null;
        String sCurrentLine;
        List<String> list1 = new ArrayList<String>();
        List<String> list2 = new ArrayList<String>();
        br1 = new BufferedReader(new FileReader("./uniqueMobileList.txt"));
        br2 = new BufferedReader(new FileReader("./updatedMobileList.txt"));
        while ((sCurrentLine = br1.readLine()) != null) {
            list1.add(sCurrentLine);
        }
        while ((sCurrentLine = br2.readLine()) != null) {
            list2.add(sCurrentLine);
        }
        
        br1.close();
        br2.close();
        
        //load list1
        Map<String, HashMap<String, String>> map2 = new HashMap<>();
        for(int i=0;i<list1.size();i++){
            String[] mobileEntry = list1.get(i).split(":");
            String[] models = mobileEntry[1].split("\t");
            HashMap<String, String> map1 = new HashMap<>();
            for(String eachModel : models){
            	String[] modelInfo = eachModel.trim().split("-");
            	map1.put(modelInfo[0], modelInfo[1]);
            }
            map2.put(mobileEntry[0].trim(), map1);
        }
        
        this.masterMobileMap = map2;
        
        List<String> tmpList = new ArrayList<String>(list2);
        tmpList.removeAll(list1);
        Map<String, HashMap<String, String>> map = new HashMap<>();
        for(int i=0;i<tmpList.size();i++){
            System.out.println(tmpList.get(i)); //content from test2.txt which is not there in test.txt
            String[] mobileEntry = tmpList.get(i).split(":");
            String[] models = mobileEntry[1].split("\t");
            HashMap<String, String> map1 = new HashMap<>();
            for(String eachModel : models){
            	String[] modelInfo = eachModel.trim().split("-");
            	if(!"Y".equals(this.allModelsUpdate) && map2.get(mobileEntry[0].trim()) != null && map2.get(mobileEntry[0].trim()).get(modelInfo[0]) != null && map2.get(mobileEntry[0].trim()).get(modelInfo[0]).equals(modelInfo[1])){
            		continue;
            	}
            	map1.put(modelInfo[0], modelInfo[1]);
            }
            map.put(mobileEntry[0].trim(), map1);
        }
        System.out.println("Total Number of Objects to be Updated : "+map.size());
		return map;
	}

	private void updateListings(File fileq, Map<String, HashMap<String, String>> changedMobiles) {
		// TODO Auto-generated method stub
		FileInputStream file = null;
		HSSFWorkbook workbook = null;
		Map<String, HashMap<String, String>> map = new HashMap<>();
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
				    	String mobile = null;
				    	String fsn = null;
				    	String listingId = null;
					    for(int cellPointer=0; cellPointer < 6; cellPointer++) {
					    	Cell cell = row.getCell(cellPointer);
					    	if(cellPointer==0){
					    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    		fsn = cell.getStringCellValue();
					    	}
					    	if(cellPointer==1){
					    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    		listingId = cell.getStringCellValue();
					    	}
					    	if(cellPointer==3){
					    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    		mobileName = cell.getStringCellValue();
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
					    			mobile = mobile != null ? mobile.toLowerCase().trim() : null;
					    		}
					    		if(mobile == null){
					    			System.out.println("Missing Sku : \t"+sku);
					    			System.exit(0);
//					    			if(sku.indexOf("-sg") != -1 && sku.split("-").length == 3){
//					    				mobile = mobileName.substring(mobileName.indexOf("for ")+4,mobileName.length()).trim();
//					    			}else{
//					    				mobile = mobileName.substring(mobileName.indexOf("for ")+4,mobileName.length()).trim();
//					    				mobile = mobile.substring(0,mobile.indexOf("(")-1).trim();
//					    			}
					    		}
					    		
					    		mobileName = mobile;
						    	
						    	for(String changedMobile : changedMobiles.keySet()){
					    			if(changedMobile.contains(";;")){
					    				 String[] mobileArr = changedMobile.split(";;");
					    				 if(mobileName.equals(mobileArr[0])){
					    					 if(mobileArr[1].contains(",")){
					    						 String[] notContainingMobiles = mobileArr[1].split(",");
					    						 boolean notContains = false;
					    						 for(String notMobile : notContainingMobiles){
					    							 if(mobileName.equals(notMobile)){
					    								 notContains = true;
					    							 }
					    						 }
					    						 if(!notContains){
					    							 map.put(sku+";;"+mobileName+";;"+fsn+";;"+listingId, changedMobiles.get(changedMobile));
					    						 }
					    					 }else if(!mobileArr[1].isEmpty() && mobileName.equals(mobileArr[1])){
					    						 map.put(sku+";;"+mobileName+";;"+fsn+";;"+listingId, changedMobiles.get(changedMobile));
					    					 }
					    				 }
					    			}else if(mobileName.equals(changedMobile)){
					    				map.put(sku+";;"+mobileName+";;"+fsn+";;"+listingId, changedMobiles.get(changedMobile));
					    			}
					    		}
						    	
//						    	if(changedMobiles.keySet().contains(mobile)){
//						    		map.put(sku+";;"+mobile, changedMobiles.get(mobile));
//						    		break;
//						    	}
					    	}
					    }
				    }else{
				    	break;
				    }
				    rowPOinter++;
			    }
			    file.close();
			    updateFlipkartInventory(map);
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
	}

	private void updateFlipkartInventory(Map<String, HashMap<String, String>> map) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Started updating SKUS. Total SKUs to be updated : "+map.size());
		int count = 1;
		try{
			for(String sku : map.keySet()){
//				String mobile = sku.split(";;")[1];
				String skuValue = sku.split(";;")[0];
				String mobileName = sku.split(";;")[1];
				String fsn = sku.split(";;")[2];
				String listingId = sku.split(";;")[3];
				String[] tokens = skuValue.split("-");
				HashMap<String, String> models = map.get(sku);
				if(tokens.length == 3){
					if(models.containsKey(tokens[1])){
						String[] values = models.get(tokens[1]).split(",");
						if(values.length == 3 && !"".equals(values[0]) && !"".equals(values[1]) && !"".equals(values[2])){
							System.out.println(skuValue+"\t"+values[0]+","+values[1]+","+values[2]+"\t"+mobileName);
							updateInventory(skuValue, values,mobileName,false,fsn,listingId);
							System.out.println("Total : "+map.size()+" - Completed : "+count++);
						}else if(values.length == 1 && !"".equals(values[0])){//for price update
							System.out.println(skuValue+"\t"+values[0]+","+values[1]+","+values[2]+"\t"+mobileName);
							updateInventory(skuValue, values,mobileName,true,fsn,listingId);
							System.out.println("Total : "+map.size()+" - Completed : "+count++);
						}
					}
				}else if(tokens.length == 4){
					//first case : 1 model is changed, 2 model is not changed - compare the 2nd model from master
					//second case : 1 model is not changed, 2 model is changed - compare the 1st model from master
					//third case : 1 model is changed, 2 model is changed - compare from changed models
					//First see if the model is available in changed and then see in master
//					if(models.containsKey(tokens[1]) || models.containsKey(tokens[2])){
						String[] values1 = null;
						if(models.containsKey(tokens[1])){//checks from the new model state
							values1 = models.get(tokens[1]).split(",");
						}else if(this.masterMobileMap.get(mobileName).get(tokens[1]) != null){//checks from the old model state
							values1 = this.masterMobileMap.get(mobileName).get(tokens[1]).split(",");
						}else if(this.correctedMissingSingleButComboPresent.get(mobileName) != null && this.correctedMissingSingleButComboPresent.get(mobileName).get(tokens[1]) != null){
							values1 = this.correctedMissingSingleButComboPresent.get(mobileName).get(tokens[1]).split(",");
						}else{
							System.out.println("Error : "+skuValue);
							values1 = "1,i,i".split(",");//make the combo inactive if any one of the 2 model is missing in single listings
							if(missingSingleButComboPresent.get(mobileName) != null){
								Set<String> missingSingleModels = missingSingleButComboPresent.get(mobileName);
								missingSingleModels.add(tokens[1]);
								missingSingleButComboPresent.put(mobileName, missingSingleModels);
							}else{
								Set<String> missingSingleModels = new TreeSet();
								missingSingleModels.add(tokens[1]);
								missingSingleButComboPresent.put(mobileName, missingSingleModels);
							}
//							System.exit(0);
//							continue;
						}
						String[] values2 = null;
						if(models.containsKey(tokens[2])){
							values2 = models.get(tokens[2]).split(",");
						}else if(this.masterMobileMap.get(mobileName).get(tokens[2]) != null){
							values2 = this.masterMobileMap.get(mobileName).get(tokens[2]).split(",");
						}else if(this.correctedMissingSingleButComboPresent.get(mobileName) != null && this.correctedMissingSingleButComboPresent.get(mobileName).get(tokens[2]) != null){
							values2 = this.correctedMissingSingleButComboPresent.get(mobileName).get(tokens[2]).split(",");
						}else{
							System.out.println("Error : "+skuValue);
							values2 = "1,i,i".split(",");//make the combo inactive if any one of the 2 model is missing in single listings
							if(missingSingleButComboPresent.get(mobileName) != null){
								Set<String> missingSingleModels = missingSingleButComboPresent.get(mobileName);
								missingSingleModels.add(tokens[2]);
								missingSingleButComboPresent.put(mobileName, missingSingleModels);
							}else{
								Set<String> missingSingleModels = new TreeSet();
								missingSingleModels.add(tokens[2]);
								missingSingleButComboPresent.put(mobileName, missingSingleModels);
							}
//							System.exit(0);
//							continue;
						}
						String[] finalValues=new String[3];
						if(new Integer(values1[0]) > new Integer(values2[0])){
							finalValues[0] = values1[0];
						}else{
							finalValues[0] = values2[0];
						}
						
						if("i".equals(values1[1]) || "i".equals(values2[1])){
							finalValues[1] = "i";
						}else{
							finalValues[1] = "a";
						}
						
						if("d".equals(values1[2]) || "d".equals(values2[2])){
							finalValues[2] = "d";
						}else if("i".equals(values1[2]) || "i".equals(values2[2])){
							finalValues[2] = "i";
						}else if("e".equals(values1[2]) || "e".equals(values2[2])){
							finalValues[2] = "e";
						}
						
						
						if(finalValues.length == 3 && !"".equals(finalValues[0]) && !"".equals(finalValues[1]) && !"".equals(finalValues[2])){
							System.out.println(skuValue+"\t"+finalValues[0]+","+finalValues[1]+","+finalValues[2]+"\t"+mobileName);
							updateInventory(skuValue, finalValues,mobileName,false,fsn,listingId);
							System.out.println("Total : "+map.size()+" - Completed : "+count++);
						}else if(finalValues.length == 1 && !"".equals(finalValues[0])){//for price update
							System.out.println(skuValue+"\t"+finalValues.toString()+"\t"+mobileName);
							updateInventory(skuValue, finalValues,mobileName,true,fsn,listingId);
							System.out.println("Total : "+map.size()+" - Completed : "+count++);
						}
//					}
				}else if(sku.indexOf("-") == -1){
					this.oldSkus.add(sku+"\t"+mobileName);
					continue;
				}
			}
			System.out.println("Completed Updating the Listings !!! - "+map.size());
			System.out.println("Total Failed Listings : "+this.failedObjects.size());
			System.out.println("Missing Single But Combo Present : "+this.missingSingleButComboPresent.size());
			saveThefailedObjects();
			missingSingleButComboPresent();
		}catch(Exception e){
			try {
				saveThefailedObjects();
				missingSingleButComboPresent();
				e.printStackTrace();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	private void missingSingleButComboPresent() throws IOException {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<>();
		if(this.missingSingleButComboPresent != null && this.missingSingleButComboPresent.size() > 0){
			for(String key : this.missingSingleButComboPresent.keySet()){
				String mobileName = key;
				String models = "";
				for(String model : this.missingSingleButComboPresent.get(key)){
					models+=model+",";
				}
				list.add(mobileName+" : "+models);
			}
			if(list.size() > 0){
				this.writeFailedToFile(list, "missingSingleButComboPresent");
			}
		}
	}

	private void saveThefailedObjects() throws Exception{
		if(this.oldSkus != null){
			System.out.println("OLD SKUS : \n\n");
			for(String sku: this.oldSkus){
				System.out.println(sku);
			}
			this.writeFailedToFile(this.oldSkus,"oldsku");
		}
		if(this.failedObjects != null){
			this.writeFailedToFile(this.failedObjects,"failed");
		}
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
	
	private void updateInventory(String sku, String[] values, String mobile, boolean isPriceOrCountChange, String fsn, String listingId) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		Gson gson = new Gson();
//		FlipkartListing flipkartListing = getFlipkartListings(sku);
		FlipkartListing flipkartListing = new FlipkartListing(sku, fsn, listingId);
		
//		Map<String, String> existingAttributeMap = flipkartListing.getAttributeValues();
//		String sla = existingAttributeMap.get("procurement_sla");
//		String status = existingAttributeMap.get("listing_status");
//		String proc = existingAttributeMap.get("procurement_type");
//		
//		if(status.equalsIgnoreCase("ACTIVE")){
//			status = "a";
//		}else if(status.equalsIgnoreCase("INACTIVE")){
//			status = "i";
//		}
//		
//		if("EXPRESS".equals(proc)){
//			proc = "e";
//		}else if("REGULAR".equals(proc)){
//			proc = "i";
//		}else if("DOMESTIC".equals(proc)){
//			proc = "d";
//		}
//		String existingValues = sla+","+status+","+proc;
//		if(existingValues.equals(values)){
//			System.out.println("No need to update as the SKU is already updates SKU : "+sku+" Values : "+existingValues);
//			return;
//		}
		if(flipkartListing!= null){
			Map<String, String> attributeMap = new HashMap<>();
			if(this.getConfigProp().getProperty("shippingzonal") != null && 
					!"NA".equals(this.getConfigProp().getProperty("shippingzonal")) && 
					!"".equals(this.getConfigProp().getProperty("shippingzonal"))){
				attributeMap.put("zonal_shipping_charge", this.getConfigProp().getProperty("shippingzonal"));
			}
			
			if(this.getConfigProp().getProperty("shippingnational") != null && 
					!"NA".equals(this.getConfigProp().getProperty("shippingnational")) && 
					!"".equals(this.getConfigProp().getProperty("shippingnational"))){
				attributeMap.put("national_shipping_charge", this.getConfigProp().getProperty("shippingnational"));
			}
			
			if(this.getConfigProp().getProperty("shippinglocal") != null && 
					!"NA".equals(this.getConfigProp().getProperty("shippinglocal")) && 
					!"".equals(this.getConfigProp().getProperty("shippinglocal"))){
				attributeMap.put("local_shipping_charge", this.getConfigProp().getProperty("shippinglocal"));
			}
			
			if(isPriceOrCountChange){
				if(values[0].contains("price=")){
					attributeMap.put("selling_price", values[0].replace("price=", ""));
					System.out.println("Updating selling price for sku : "+sku+" to "+values[0].replace("price=", ""));
				}else if(values[0].contains("count=")){
					attributeMap.put("stock_count", values[0].replace("count=", ""));
					System.out.println("Updating stock count for sku : "+sku+" to "+values[0].replace("count=", ""));
				}
			}else{
				attributeMap.put("procurement_sla", values[0]);
				if("a".equals(values[1])){
					attributeMap.put("listing_status", "ACTIVE");
				}else if("i".equals(values[1])){
					attributeMap.put("listing_status", "INACTIVE");
				}
				if("e".equals(values[2])){
					attributeMap.put("procurement_type",  "EXPRESS");
					attributeMap.put("procurement_sla", "2");
				}else if("i".equals(values[2])){
					attributeMap.put("procurement_type",  "REGULAR" );
				}else if("d".equals(values[2])){
					attributeMap.put("procurement_type",  "DOMESTIC");
					if(new Integer(values[0]) < 3){
						attributeMap.put("procurement_sla", "3");
					}else if(new Integer(values[0]) > 5){
						attributeMap.put("procurement_sla", "5");
					}
				}
				
				if(this.getConfigProp().getProperty("length") != null && 
						!"NA".equals(this.getConfigProp().getProperty("length")) && 
						!"".equals(this.getConfigProp().getProperty("length"))){
					attributeMap.put("package_length", this.getConfigProp().getProperty("length"));
				}
				if(this.getConfigProp().getProperty("breadth") != null && 
						!"NA".equals(this.getConfigProp().getProperty("breadth")) && 
						!"".equals(this.getConfigProp().getProperty("breadth"))){
					attributeMap.put("package_breadth", this.getConfigProp().getProperty("breadth"));
				}
				if(this.getConfigProp().getProperty("height") != null && 
						!"NA".equals(this.getConfigProp().getProperty("height")) && 
						!"".equals(this.getConfigProp().getProperty("height"))){
					attributeMap.put("package_height", this.getConfigProp().getProperty("height"));
				}
				if(this.getConfigProp().getProperty("weight") != null && 
						!"NA".equals(this.getConfigProp().getProperty("weight")) && 
						!"".equals(this.getConfigProp().getProperty("weight"))){
					attributeMap.put("package_weight", this.getConfigProp().getProperty("weight"));
				}
			}
			
			flipkartListing.setAttributeValues(attributeMap);
			
			if(this.bulkFlipkartListings.getListings() != null && this.bulkFlipkartListings.getListings().size() == 9){
				//push, save and clear
				this.bulkFlipkartListings.getListings().add(flipkartListing);
				String json = gson.toJson(this.bulkFlipkartListings);
				BulkUpdateFlipkartListingResponse uflrFlipkartListingResponse = bulkUpdateFlipkartListings(json);
				try {
					
					if(uflrFlipkartListingResponse.getStatus() != null && uflrFlipkartListingResponse.getStatus().equals("success")){
//					System.out.println("Successfully Updated the Inventory");
					}else{
						//write failed skus in another file
						for(Map<String, Object> each : uflrFlipkartListingResponse.getResponse()){
							if(!each.get("status").equals("updated")){
								this.failedObjects.add(mobile+"\t"+sku+"\t"+uflrFlipkartListingResponse.getStatus()+"\t"+uflrFlipkartListingResponse.getResponse()+"\tJSON : "+json);
								System.out.println("Update Failed : "+sku+"\t"+mobile+"\t"+uflrFlipkartListingResponse.getStatus()+"\t"+uflrFlipkartListingResponse.getResponse()+"\tJSON : "+json);
							}
						}
					}
					this.bulkFlipkartListings.getListings().clear();
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println(e);
				}
			}else{
				//only push
				this.bulkFlipkartListings.getListings().add(flipkartListing);
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
	
	public UpdateFlipkartListingResponse updateFlipkartListings(String flipkartListingJson, String sku) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		String url = "https://api.flipkart.net/sellers/skus/"+sku+"/listings";
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
//			System.out.println(line);
		}
		Gson gson = new Gson();
		return gson.fromJson(result.toString(), UpdateFlipkartListingResponse.class);
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
				System.out.println(line);
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
				System.out.println(sku);
			}
//			System.out.println(line);
		}
		if(result.indexOf("listingId") != -1){
			Gson gson = new Gson();
			flipkartListing = gson.fromJson(result.toString(), FlipkartListing.class);
//			System.out.println(flipkartListing.getSkuId());
		}else{
			return null;
		}
		return flipkartListing;
	}

	private Properties loadProperties() throws IOException{

	    //to load application's properties, we use this class
	    Properties mainProperties = new Properties();

	    FileInputStream file;

	    //the base folder is ./, the root of the main.properties file  
	    String path = "./code/config.properties";//D:/AAA_WORK/Important Batch Files/Flipkart/Listings Status(Get INACTIVE and HIGHER SLA Listings)/code/config.properties

	    //load the file handle for main.properties
	    file = new FileInputStream(path);

	    //load all the properties from this file
	    mainProperties.load(file);

	    //we have loaded the properties, so close the file handle
	    file.close();


	    return mainProperties;
	}

	public Map<String, TreeMap<String, String>> getUniqueMobileNames(File fileq) {
		// TODO Auto-generated method stub
		FileInputStream file = null;
		Map<String, TreeMap<String, String>> healthCheck = new TreeMap<>();//checks if 2 skus of any mobile model is having different status/sla/procurement
		HSSFWorkbook workbook = null;
		try {
			Map<String, TreeMap<String, String>> list = new TreeMap<>();
			Map<String, Set<String>> comboList = new TreeMap<>();
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
					    for(int cellPointer=3 ; cellPointer < 25; cellPointer++) {
					    	Cell cell = row.getCell(cellPointer);
					    	if(cellPointer==3){
					    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    		mobileName = cell.getStringCellValue();
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
					    			mobile = mobile != null ? mobile.toLowerCase().trim() : "";
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
					    	
					    	if(this.sellerName != null && ((this.sellerName.equalsIgnoreCase("amr") && cellPointer == 23)
					    			|| (this.sellerName.equalsIgnoreCase("tram") && cellPointer == 22)
					    			|| (this.sellerName.equalsIgnoreCase("mar") && cellPointer == 22))){
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
								String model = tokens[1];
								if(list.get(mobile) != null){
									TreeMap<String, String> existingMap = list.get(mobile);
									if(existingMap.get(model) != null && !"".equals(model)){
										//check if the there are 2 models having different status
										String modelValue = existingMap.get(model);
										if(!modelValue.equals(eachRowtring)){
											String[] values = modelValue.split(",");
											String newmodelValue = eachRowtring;
											String[] newValues = newmodelValue.split(",");
											String finalString = "";
											if(new Integer(values[0]) > new Integer(newValues[0])){
												finalString = values[2].equals("e") ? newValues[0] : values[0];
											}else{
												finalString = newValues[2].equals("e") ? values[0] : newValues[0];
											}
											
											if("i".equals(values[1]) || "i".equals(newValues[1])){
												finalString+= ",i";
											}else {
												finalString+=",a";
											}
											
											if("d".equals(values[2]) || "d".equals(newValues[2])){
												finalString+= ",d";
											}else if("i".equals(values[2]) || "i".equals(newValues[2])){
												finalString+=",i";
											}else if("e".equals(values[2]) || "e".equals(newValues[2])){
												finalString+=",e";
											}
									
											if(procurementtypeCondition(finalString) && slaCondition(finalString) && listingStatusCondition(finalString)){
												existingMap.put(model, finalString);
												list.put(mobile, existingMap);
											}
									
											//health check
											if("Y".equals(ListingsStatus.healthCheck) && healthCheck.get(mobile) != null){
												TreeMap<String, String> oldValue = healthCheck.get(mobile);
												if(oldValue.get(model) != null){
													String oldValueS = oldValue.get(model);
													if(oldValueS.indexOf(finalString) == -1){
														oldValue.put(model, oldValueS+"/"+finalString);
														healthCheck.put(mobile,oldValue);
													}else if(oldValueS.indexOf(modelValue) == -1){
														oldValue.put(model, oldValueS+"/"+finalString);
														healthCheck.put(mobile,oldValue);
													}
												}else{
													//new model
													oldValue.put(model, modelValue+"/"+finalString);
													healthCheck.put(mobile,oldValue);
												}
											}else if ("Y".equals(ListingsStatus.healthCheck)){
												//new mobile
												TreeMap<String, String> newModel = new TreeMap<>();
												newModel.put(model, modelValue+"/"+finalString);
												healthCheck.put(mobile,newModel);
											}
										}
									}else{
										if(procurementtypeCondition(eachRowtring) && slaCondition(eachRowtring) && listingStatusCondition(eachRowtring)){
											existingMap.put(model, eachRowtring);
											list.put(mobile, existingMap);
										}
									}
								}else{
									if(procurementtypeCondition(eachRowtring) && slaCondition(eachRowtring) && listingStatusCondition(eachRowtring)){
										TreeMap<String, String> modelMap = new TreeMap<>();
										modelMap.put(model, eachRowtring);
										list.put(mobile, modelMap);
									}
								}
							}else if(tokens.length == 4){
								if(comboList.get(mobile) != null){
									comboList.get(mobile).add(tokens[1]);
									comboList.get(mobile).add(tokens[2]);
								}else{
									Set<String> set = new TreeSet<>();
									set.add(tokens[1]);
									set.add(tokens[2]);
									comboList.put(mobile, set);
								}
							}
					    }
				    }else{
				    	break;
				    }
				    rowPOinter++;
			    }
				System.out.println("Total Rows Scanned : "+rowPOinter);
				if("Y".equals(ListingsStatus.healthCheck)){
					return healthCheck;
				}else{
					//find the missing single but combo present
					comboList = filterMissingSingleFromCombo(list, comboList);
					writeToMissingFile(comboList);
					return list;
				}
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

	private void writeToMissingFile(Map<String, Set<String>> list) throws IOException {
		// TODO Auto-generated method stub
		File fout = new File("./skippedModelsOfCombos.txt");
		if(!fout.exists()){
			fout.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	 
		for(String entry : list.keySet()){
	    	String printS = entry+":";
	    	Set<String> modelMap = list.get(entry);
	    	if(modelMap.size() == 0){
	    		continue;
	    	}
	    	for(String variety:  modelMap){
	    		printS += variety+"\t";
	    	}
	    	bw.write(printS);
			bw.newLine();
	    	
	    }
		bw.close();
	}

	private Map<String, Set<String>> filterMissingSingleFromCombo(Map<String, TreeMap<String, String>> list,
			Map<String, Set<String>> comboList) {
		// TODO Auto-generated method stub
		for(String mobileName : list.keySet()){
			if(comboList.containsKey(mobileName)){
				for(String eachModel : list.get(mobileName).keySet()){
					if(comboList.get(mobileName).contains(eachModel)){
						comboList.get(mobileName).remove(eachModel);
					}
				}
			}
		}
		return comboList;
	}

	private void writeToFile(Map<String, TreeMap<String, String>> list) throws IOException {
		// TODO Auto-generated method stub
		String sellerName = getConfigProp().getProperty("seller");
		StringBuilder strB = new StringBuilder();
		File fout = new File("./uniqueMobileList.txt");
		if(!fout.exists()){
			fout.createNewFile();
		}
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
	    	strB.append(printS);
	    	strB.append("\n");
			bw.newLine();
	    	
	    }
		bw.close();
		emailUtils.sendEmail(sellerName, "ListingsStatus", "1_UniqueModels", strB.toString(), filePaths,"rma.retailers@gmail.com");
	}

	public Properties getConfigProp() {
		return configProp;
	}

	public void setConfigProp(Properties configProp) {
		this.configProp = configProp;
	}
}
