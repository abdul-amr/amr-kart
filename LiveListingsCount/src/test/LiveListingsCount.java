package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;


import au.com.bytecode.opencsv.CSVReader;


public class LiveListingsCount {
	
	private Properties configProp;
	public Map<String, String> skuVsPhoneMap;
	private EmailUtils emailUtils;
	private List<String> filePaths;
	
	public LiveListingsCount() throws IOException{
		configProp = loadProperties();
		skuVsPhoneMap = new HashMap<>();
		emailUtils = new EmailUtils();
		filePaths = new ArrayList<>();
		filePaths.add("./singleListingsCount.txt");
		filePaths.add("./comboListingsCount.txt");
		filePaths.add("./code/config.properties");
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		LiveListingsCount liveListingsCount = new LiveListingsCount();
		String inventoryFile = liveListingsCount.configProp.getProperty("listingsfile");
		0
		boolean isValid = liveListingsCount.validateConfig();
		if(!isValid){
			System.out.println("Validation Failed");
			return;
		}
		
		if(inventoryFile.endsWith("xls")){
			liveListingsCount.skuVsPhoneMap = liveListingsCount.loadSkuVsPhoneMapFromCentralLocation();
			liveListingsCount.getUniqueMobileNames(new File(inventoryFile));
		}else if(inventoryFile.endsWith("csv")){
			liveListingsCount.skuVsPhoneMap = liveListingsCount.loadSkuVsPhoneMapFromCentralLocation();
			liveListingsCount.getUniqueMobileNamesCSV(new File(inventoryFile));
		}
	}

	public void getUniqueMobileNamesCSV(File invFile) throws Exception {
		// TODO Auto-generated method stub
		Map<String, TreeMap<String, Integer>> singleList = new TreeMap<>();
		Map<String, TreeMap<String, Integer>> comboList = new TreeMap<>();
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
		    	String sku = null;
		    	String mobile = null;
			    for(int cellPointer=2; cellPointer < 15; cellPointer++) {
			    	String cell = nextLine[cellPointer];
					if (cell == null || cell.isEmpty()) {
						continue;
					}
					cell = cell.trim();
			    	if(cellPointer == 3){
			    		sku = cell;
			    		if(sku != null && !"".equals(sku)){
			    			mobile = this.skuVsPhoneMap.get(sku);
			    			mobile = mobile.toLowerCase().trim();
			    		}
			    		if(mobile == null){
			    			System.out.println("Missing Mobile Name for Sku : \t"+sku);
			    			System.exit(0);
			    		}
			    	}
			    }
			    if(sku == null || mobile == null || "".equals(sku) || "".equals(mobile)){
			    	System.out.println("Mobile/SKU is missing - cannot proceed");
			    	System.exit(0);
			    }
		    	String[] tokens = sku.split("-");
				if(tokens.length == 3){//Do not extract combos for model status. there will be conflicts if we consider combos.
					String model = tokens[1];
					if(singleList.get(mobile) != null){
						TreeMap<String, Integer> existingMap = singleList.get(mobile);
						if(existingMap.get(model) != null && !"".equals(model)){
							Integer value = existingMap.get(model);
							existingMap.put(model, value+1);
						}else{
							existingMap.put(model, 1);
						}
						singleList.put(mobile, existingMap);
					}else{
						TreeMap<String, Integer> modelMap = new TreeMap<>();
						modelMap.put(model, 1);
						singleList.put(mobile, modelMap);
					}
				}else if(tokens.length == 4){
					String model1 = tokens[1];
					String model2 = tokens[2];
					if(comboList.get(mobile) != null){
						TreeMap<String, Integer> existingMap = comboList.get(mobile);
						if(existingMap.get(model1) != null && !"".equals(model1)){
							Integer value = existingMap.get(model1);
							existingMap.put(model1, value+1);
						}else{
							existingMap.put(model1, 1);
						}
						if(existingMap.get(model2) != null && !"".equals(model2)){
							Integer value = existingMap.get(model2);
							existingMap.put(model2, value+1);
						}else{
							existingMap.put(model2, 1);
						}
						comboList.put(mobile, existingMap);
					}else{
						TreeMap<String, Integer> modelMap = new TreeMap<>();
						modelMap.put(model1, 1);
						modelMap.put(model2, 1);
						comboList.put(mobile, modelMap);
					}
				}
			    rowPointer++;
			}
			System.out.println("Total Rows Scanned : "+rowPointer);
//			comboList = filterMissingSingleFromCombo(list, comboList);
			writeToFile(singleList, "./singleListingsCount.txt");
			writeToFile(comboList, "./comboListingsCount.txt");
		}  catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
		}
	}

	public void getUniqueMobileNames(File fileq) {
		// TODO Auto-generated method stub
		FileInputStream file = null;
		Map<String, TreeMap<String, String>> healthCheck = new TreeMap<>();//checks if 2 skus of any mobile model is having different status/sla/procurement
		HSSFWorkbook workbook = null;
		try {
			Map<String, TreeMap<String, Integer>> singleList = new TreeMap<>();
			Map<String, TreeMap<String, Integer>> comboList = new TreeMap<>();
				file = new FileInputStream(fileq);
			    workbook = new HSSFWorkbook(file);
			    HSSFSheet sheet = workbook.getSheetAt(0);
			    boolean flag = true;
			    int rowPOinter = 2;
			    while(flag){
				    Row row = sheet.getRow(rowPOinter);
				    if(row != null){
				    	String sku = null;
				    	String mobile = null;
				    	boolean nonLiveListing = false;
					    for(int cellPointer=3 ; cellPointer < 25; cellPointer++) {
					    	Cell cell = row.getCell(cellPointer);
					    	if(cellPointer == 5){
					    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    		sku = cell.getStringCellValue();
					    		if(sku != null && !"".equals(sku)){
					    			mobile = this.skuVsPhoneMap.get(sku);
					    			mobile = mobile.toLowerCase().trim();
					    		}
					    		if(mobile == null){
					    			System.out.println("Missing Mobile Name for Sku : \t"+sku);
					    			System.exit(0);
					    		}
					    	}
					    	if(cell == null){
					    		cell = row.createCell(cellPointer);
					    	}
					    }
					    if(sku == null || mobile == null || "".equals(sku) || "".equals(mobile)){
					    	System.out.println("Mobile/SKU is missing - cannot proceed");
					    	System.exit(0);
					    }
				    	String[] tokens = sku.split("-");
						if(tokens.length == 3){//Do not extract combos for model status. there will be conflicts if we consider combos.
							String model = tokens[1];
							if(singleList.get(mobile) != null){
								TreeMap<String, Integer> existingMap = singleList.get(mobile);
								if(existingMap.get(model) != null && !"".equals(model)){
									Integer value = existingMap.get(model);
									existingMap.put(model, value+1);
								}else{
									existingMap.put(model, 1);
								}
								singleList.put(mobile, existingMap);
							}else{
								TreeMap<String, Integer> modelMap = new TreeMap<>();
								modelMap.put(model, 1);
								singleList.put(mobile, modelMap);
							}
						}else if(tokens.length == 4){
							String model1 = tokens[1];
							String model2 = tokens[2];
							if(comboList.get(mobile) != null){
								TreeMap<String, Integer> existingMap = comboList.get(mobile);
								if(existingMap.get(model1) != null && !"".equals(model1)){
									Integer value = existingMap.get(model1);
									existingMap.put(model1, value+1);
								}else{
									existingMap.put(model1, 1);
								}
								if(existingMap.get(model2) != null && !"".equals(model2)){
									Integer value = existingMap.get(model2);
									existingMap.put(model2, value+1);
								}else{
									existingMap.put(model2, 1);
								}
								comboList.put(mobile, existingMap);
							}else{
								TreeMap<String, Integer> modelMap = new TreeMap<>();
								modelMap.put(model1, 1);
								modelMap.put(model2, 1);
								comboList.put(mobile, modelMap);
							}
						}
				    }else{
				    	break;
				    }
				    rowPOinter++;
			    }
				System.out.println("Total Rows Scanned : "+rowPOinter);
				
//				comboList = filterMissingSingleFromCombo(singleList, comboList);
				writeToFile(singleList, "./singleListingsCount.txt");
				writeToFile(comboList, "./comboListingsCount.txt");
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

	private void writeToFile(Map<String, TreeMap<String, Integer>> list, String fileName) throws IOException {
		// TODO Auto-generated method stub
		String sellerName = getConfigProp().getProperty("seller");
		StringBuilder strB = new StringBuilder();
		File fout = new File(fileName);
		if(!fout.exists()){
			fout.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	 
		for(String entry : list.keySet()){
	    	String printS = entry+":";
	    	TreeMap<String, Integer> modelMap = list.get(entry);
	    	for(String variety:  modelMap.keySet()){
	    		Integer values = modelMap.get(variety);
	    		if(values == null){
	    			printS += variety+"-0\t";
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
		emailUtils.sendEmail(sellerName, "LiveListingsCount", "1_GetCount", strB.toString(), filePaths,"");
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

	private boolean validateConfig() {
		// TODO Auto-generated method stub
		String listingsfile = configProp.getProperty("listingsfile");
		String sellerName = configProp.getProperty("seller");
		String skuvsphonemapping = configProp.getProperty("skuvsphonemapping");
		
		if(listingsfile == null || "".equals(listingsfile) || !(listingsfile.endsWith(".csv") || listingsfile.endsWith(".xls"))){
			System.out.println("Incorrect/Missing Inventory/Listings File Name ");
			return false;
		}else{
			File listingsFle = new File(listingsfile);
			if(!listingsFle.exists()){
				System.out.println("Inventory/Listings File does not exists");
				return false;
			}
		}
		
		if(sellerName == null || "".equals(sellerName)){
			System.out.println("Incorrect/Missing Seller Name");
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
		
		return true;
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

	public Properties getConfigProp() {
		return configProp;
	}

	public void setConfigProp(Properties configProp) {
		this.configProp = configProp;
	}

}
