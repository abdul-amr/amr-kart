package com.amr.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import au.com.bytecode.opencsv.CSVReader;

public class ListingsStatus {

	public Map<String, String> skuVsPhoneMap;
	private String sellerName;
	
	public ListingsStatus(){
		skuVsPhoneMap = new HashMap<>();
	}
	


	public Map<String, TreeMap<String, String>> getUniqueMobileNamesCSV(File invFile) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Reading the Live Listings File");
		Map<String, TreeMap<String, String>> list = new TreeMap<>();
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
//			    			System.out.println(mobile);
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
						populateUniqueModelsMap(list, mobile, model, eachRowtring,healthCheck );
					}
			    }
		    
			    rowPointer++;
			}
			System.out.println("Total Rows Scanned : "+rowPointer);
			return list;
		}  catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
		}
		return null;
	}


	public void populateUniqueModelsMap(Map<String, TreeMap<String, String>> list, String mobile, String model, String eachRowtring, Map<String, TreeMap<String, String>> healthCheck) {
		// TODO Auto-generated method stub
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
//					if(healthCheck.get(mobile) != null){
//						TreeMap<String, String> oldValue = healthCheck.get(mobile);
//						if(oldValue.get(model) != null){
//							String oldValueS = oldValue.get(model);
//							if(oldValueS.indexOf(finalString) == -1){
//								oldValue.put(model, oldValueS+"/"+finalString);
//								healthCheck.put(mobile,oldValue);
//							}
//							if(oldValueS.indexOf(modelValue) == -1){
//								oldValue.put(model, oldValueS+"/"+finalString);
//								healthCheck.put(mobile,oldValue);
//							}
//						}else{
//							//new model
//							oldValue.put(model, modelValue+"/"+finalString);
//							healthCheck.put(mobile,oldValue);
//						}
//					}else{
//						//new mobile
//						TreeMap<String, String> newModel = new TreeMap<>();
//						newModel.put(model, modelValue+"/"+finalString);
//						healthCheck.put(mobile,newModel);
//					}
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
	}



	public Map<String, TreeMap<String, String>> getUniqueMobileNamesExcel(File fileq) {
		// TODO Auto-generated method stub
		System.out.println("Reading the Live Listings File");
		FileInputStream file = null;
		Map<String, TreeMap<String, String>> healthCheck = new TreeMap<>();//checks if 2 skus of any mobile model is having different status/sla/procurement
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
								populateUniqueModelsMap(list, mobile, model, eachRowtring, healthCheck);
//								if(list.get(mobile) != null){
//									TreeMap<String, String> existingMap = list.get(mobile);
//									if(existingMap.get(model) != null && !"".equals(model)){
//										//check if the there are 2 models having different status
//										String modelValue = existingMap.get(model);
//										if(!modelValue.equals(eachRowtring)){
//											String[] values = modelValue.split(",");
//											String newmodelValue = eachRowtring;
//											String[] newValues = newmodelValue.split(",");
//											String finalString = "";
//											if(new Integer(values[0]) > new Integer(newValues[0])){
//												finalString = values[2].equals("e") ? newValues[0] : values[0];
//											}else{
//												finalString = newValues[2].equals("e") ? values[0] : newValues[0];
//											}
//											
//											if("i".equals(values[1]) || "i".equals(newValues[1])){
//												finalString+= ",i";
//											}else {
//												finalString+=",a";
//											}
//											
//											if("d".equals(values[2]) || "d".equals(newValues[2])){
//												finalString+= ",d";
//											}else if("i".equals(values[2]) || "i".equals(newValues[2])){
//												finalString+=",i";
//											}else if("e".equals(values[2]) || "e".equals(newValues[2])){
//												finalString+=",e";
//											}
//									
//											if(procurementtypeCondition(finalString) && slaCondition(finalString) && listingStatusCondition(finalString)){
//												existingMap.put(model, finalString);
//												list.put(mobile, existingMap);
//											}
//									
//											//health check
//											if(healthCheck.get(mobile) != null){
//												TreeMap<String, String> oldValue = healthCheck.get(mobile);
//												if(oldValue.get(model) != null){
//													String oldValueS = oldValue.get(model);
//													if(oldValueS.indexOf(finalString) == -1){
//														oldValue.put(model, oldValueS+"/"+finalString);
//														healthCheck.put(mobile,oldValue);
//													}else if(oldValueS.indexOf(modelValue) == -1){
//														oldValue.put(model, oldValueS+"/"+finalString);
//														healthCheck.put(mobile,oldValue);
//													}
//												}else{
//													//new model
//													oldValue.put(model, modelValue+"/"+finalString);
//													healthCheck.put(mobile,oldValue);
//												}
//											}else{
//												//new mobile
//												TreeMap<String, String> newModel = new TreeMap<>();
//												newModel.put(model, modelValue+"/"+finalString);
//												healthCheck.put(mobile,newModel);
//											}
//										}
//									}else{
//										if(procurementtypeCondition(eachRowtring) && slaCondition(eachRowtring) && listingStatusCondition(eachRowtring)){
//											existingMap.put(model, eachRowtring);
//											list.put(mobile, existingMap);
//										}
//									}
//								}else{
//									if(procurementtypeCondition(eachRowtring) && slaCondition(eachRowtring) && listingStatusCondition(eachRowtring)){
//										TreeMap<String, String> modelMap = new TreeMap<>();
//										modelMap.put(model, eachRowtring);
//										list.put(mobile, modelMap);
//									}
//								}
							}
					    }
				    }else{
				    	break;
				    }
				    rowPOinter++;
			    }
				System.out.println("Total Rows Scanned : "+rowPOinter);
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

	private boolean listingStatusCondition(String eachRowtring) {
		return true;
	}

	private boolean procurementtypeCondition(String eachRowtring) {
		return true;
		
	}

	private boolean slaCondition(String eachRowtring) {
		return true;
	}


	public Map<String, String> getSkuVsPhoneMap() {
		return skuVsPhoneMap;
	}


	public void setSkuVsPhoneMap(Map<String, String> skuVsPhoneMap) {
		this.skuVsPhoneMap = skuVsPhoneMap;
	}



	public String getSellerName() {
		return sellerName;
	}



	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}

}
