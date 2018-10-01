package com.retailers.rma;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FlipkartInventoryFile {

	public static final String TEMPLATE_FILE_URL = "D:\\AAA_WORK\\JARS\\POI Testing\\Template\\Sunglasses Template Information.xlsx";
	public static final String FLIPKART_BULK_LISTING_FILE_URL = "D:\\AAA_WORK\\JARS\\POI Testing\\Flipkart Bulk Listing File";
	
	
	public boolean start(){
		Map<Integer, String> staticMap = populateFlipkartStaticDataMap();
		if(staticMap == null || staticMap.size() == 0){
			System.out.println("Failed to read the static content");
			return false;
		}
		List<Map<Integer, String>> flipkartTemplateRows = readFlipkartTemplateFile();
		if(flipkartTemplateRows == null || flipkartTemplateRows.size() == 0){
			System.out.println("Failed to read the template content");
			return false;
		}
		System.out.println("Started Writing to Bulk Listing File");
		if(writeToFlipkartBulkListingFile(flipkartTemplateRows, staticMap)){
			System.out.println("Completed Writing to Bulk Listing File");
			return true;
		}else{
			return false;
		}
	}
	
	/*
	 * Populate Flipkart Static Data
	 * */
	private Map<Integer, String> populateFlipkartStaticDataMap() {
		// TODO Auto-generated method stub
		Map<Integer, String> staticMap = new HashMap<>();
		FileInputStream file = null;
		XSSFWorkbook workbook = null;
		try {
			file = new FileInputStream(new File(FlipkartInventoryFile.TEMPLATE_FILE_URL));
		    workbook = new XSSFWorkbook(file);
		    XSSFSheet sheet = workbook.getSheet("Flipkart Static Data");
		    Row header = sheet.getRow(1);
		    Iterator<Cell> iterator = header.cellIterator();
		    List<Integer> staticHeaders = new ArrayList<>();
		    int count = 0;
		    while(iterator.hasNext()) {
		    	Cell cell = iterator.next();
		    	if(cell == null){
		    		cell = header.createCell(count);
		    	}
		    	int cellValue = new Double(cell.getNumericCellValue()).intValue();
		    	if(cellValue != 0){
		    		staticHeaders.add(cellValue);
		    	}
		    }
		    
		    sheet = workbook.getSheet("Flipkart Static Data");
		    Row cellPosition = sheet.getRow(2);
		    Iterator<Cell> staticIterator = cellPosition.cellIterator();
		    count = 0;
		    while(staticIterator.hasNext()) {
		    	Cell cell = staticIterator.next();
		    	if(cell == null){
		    		cell = cellPosition.createCell(count);
		    	}
		    	String cellValue = cell.getStringCellValue();
		    	if(count >= staticHeaders.size()){
		    		break;
		    	}
		    	staticMap.put(staticHeaders.get(count++), cellValue);
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(file != null){
				try {
					file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(workbook != null){
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return (staticMap != null && staticMap.size() > 0) ? staticMap : null;
	}
	
	/*
	 * Updates the Flipkart Bulk Listing File
	 * 
	 * Input :: Flipkart dynamic data and static data
	 * 
	 * */
	public boolean writeRandomResponseToFlipkart(List<Map<Integer, String>> flipkartTemplateRows,String fileName){
		FileInputStream file = null;
		FileOutputStream output_file = null;
		HSSFWorkbook workbook = null;
		try {
			File fileq = new File(fileName);//"D:\\AAA_WORK\\JARS\\POI Testing\\Flipkart Bulk Listing File"
				file = new FileInputStream(fileq);
			    workbook = new HSSFWorkbook(file);
			    HSSFSheet sheet = workbook.getSheet("cases_covers");//screen_guard//mobile_accessories_combo//cases_covers
			    boolean flag = true;
			    int rowPOinter = 4;
			    while(flag){
			    	boolean isDone = (rowPOinter-4)<flipkartTemplateRows.size();
			    	if(!isDone){
			    		break;
			    	}
				    Map<Integer, String> inputRowMap = isDone ? flipkartTemplateRows.get(rowPOinter-4) : null;
				    Row row = sheet.getRow(rowPOinter);
				    if(row != null){
					    for(int cellPointer=3; cellPointer < 7; cellPointer++) {
					    	if(cellPointer ==3 || cellPointer ==6){
					    		Cell cell = row.getCell(cellPointer);
					    		if(cell == null){
					    			cell = row.createCell(cellPointer);
					    		}
//					    	System.out.println(inputRowMap.get(cellPointer));
					    		cell.setCellValue(inputRowMap.get(cellPointer));
					    	}
					    }
				    }
				    rowPOinter++;
			    }
			    file.close();
			    output_file =new FileOutputStream(fileq);
			    workbook.write(output_file);
			    return true;
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
		return false;
	}
	
	public boolean writeRandomResponseToPayTM(List<Map<Integer, String>> flipkartTemplateRows,String fileName){
		FileInputStream file = null;
		FileOutputStream output_file = null;
		XSSFWorkbook workbook = null;
		try {
			File fileq = new File(fileName);//"D:\\AAA_WORK\\JARS\\POI Testing\\Flipkart Bulk Listing File"
				file = new FileInputStream(fileq);
			    workbook = new XSSFWorkbook(file);
			    XSSFSheet sheet = workbook.getSheet("Product Create");
			    boolean flag = true;
			    int rowPOinter = 2;
			    while(flag){
			    	boolean isDone = (rowPOinter-2)<flipkartTemplateRows.size();
			    	if(!isDone){
			    		break;
			    	}
				    Map<Integer, String> inputRowMap = isDone ? flipkartTemplateRows.get(rowPOinter-2) : null;
				    Row row = sheet.getRow(rowPOinter);
				    if(row != null){
					    for(int cellPointer=0; cellPointer < 35; cellPointer++) {
					    	Cell cell = row.getCell(cellPointer);
					    	if(cell == null){
					    		cell = row.createCell(cellPointer);
					    	}
					    	System.out.println(inputRowMap.get(cellPointer));
					    	cell.setCellValue(inputRowMap.get(cellPointer));
					    }
				    }
				    rowPOinter++;
			    }
			    file.close();
			    output_file =new FileOutputStream(fileq);
			    workbook.write(output_file);
			    return true;
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
		return false;
	}
	
	public boolean writeComboRandomResponseToFlipkart(List<Map<Integer, String>> flipkartTemplateRows,String fileName){
		FileInputStream file = null;
		FileOutputStream output_file = null;
		HSSFWorkbook workbook = null;
		try {
			File fileq = new File(fileName);//"D:\\AAA_WORK\\JARS\\POI Testing\\Flipkart Bulk Listing File"
				file = new FileInputStream(fileq);
			    workbook = new HSSFWorkbook(file);
			    HSSFSheet sheet = workbook.getSheet("mobile_accessories_combo");
			    boolean flag = true;
			    int rowPOinter = 4;
			    while(flag){
			    	boolean isDone = (rowPOinter-4)<flipkartTemplateRows.size();
			    	if(!isDone){
			    		break;
			    	}
				    Map<Integer, String> inputRowMap = isDone ? flipkartTemplateRows.get(rowPOinter-4) : null;
				    Row row = sheet.getRow(rowPOinter);
				    if(row != null){
					    for(int cellPointer=6; cellPointer < 33; cellPointer++) {
					    	Cell cell = row.getCell(cellPointer);
					    	if(cell == null){
					    		cell = row.createCell(cellPointer);
					    	}
					    	System.out.println(inputRowMap.get(cellPointer));
					    	cell.setCellValue(inputRowMap.get(cellPointer));
					    }
				    }
				    rowPOinter++;
			    }
			    file.close();
			    output_file =new FileOutputStream(fileq);
			    workbook.write(output_file);
			    return true;
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
		return false;
	}

	/*
	 * Updates the Flipkart Bulk Listing File
	 * 
	 * Input :: Flipkart dynamic data and static data
	 * 
	 * */
	private boolean writeToFlipkartBulkListingFile(List<Map<Integer, String>> flipkartTemplateRows, Map<Integer, String> flipkartStaticMap){
		FileInputStream file = null;
		FileOutputStream output_file = null;
		HSSFWorkbook workbook = null;
		try {
			File directory = new File(FlipkartInventoryFile.FLIPKART_BULK_LISTING_FILE_URL);//"D:\\AAA_WORK\\JARS\\POI Testing\\Flipkart Bulk Listing File"
			File[] files = directory.listFiles();
			if(files!= null && files.length > 0 && files[0].getName().contains(".xls") && flipkartTemplateRows != null && flipkartStaticMap != null){
				file = new FileInputStream(files[0]);
			    workbook = new HSSFWorkbook(file);
			    HSSFSheet sheet = workbook.getSheet("sunglass");
			    boolean flag = true;
			    int rowPOinter = 4;
			    while(flag){
			    	boolean isDone = (rowPOinter-4)<flipkartTemplateRows.size();
			    	if(!isDone){
			    		break;
			    	}
				    Map<Integer, String> inputRowMap = isDone ? flipkartTemplateRows.get(rowPOinter-4) : null;
				    Row row = sheet.getRow(rowPOinter);
				    if(row != null){
					    for(int cellPointer=6; cellPointer < 67; cellPointer++) {
					    	Cell cell = row.getCell(cellPointer);
					    	if(cell == null){
					    		cell = row.createCell(cellPointer);
					    	}
					    	populateBulkListingRow(cellPointer, cell, inputRowMap, flipkartStaticMap);
					    }
				    }
				    rowPOinter++;
			    }
			    file.close();
			    output_file =new FileOutputStream(files[0]);
			    workbook.write(output_file);
			    return true;
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
		return false;
	}

	private void populateBulkListingRow(int cellPointer, Cell cell,
			Map<Integer, String> inputRowMap,
			Map<Integer, String> flipkartStaticMap) {
		if(inputRowMap.get(cellPointer) != null){
			cell.setCellValue(inputRowMap.get(cellPointer));
		}else if(flipkartStaticMap.get(cellPointer) != null){
			if(flipkartStaticMap.get(cellPointer).equals("sku")){
				cell.setCellValue(inputRowMap.get(6));
			}else{
				cell.setCellValue(flipkartStaticMap.get(cellPointer));
			}
		}else{
			cell.setCellValue("");
		}
	}
	
	public List<Map<Integer,String>> readRandomFlipkartFile(String fileName){
		List<Map<Integer, String>> flipkartTemplateRows = new ArrayList<>();
		List<Integer> headers = new ArrayList<>();
		FileInputStream file = null;
		HSSFWorkbook workbook = null;
		try {
		    file = new FileInputStream(new File(fileName));//"D:\\AAA_WORK\\JARS\\POI Testing\\Sunglasses Template Information.xlsx"
		    workbook = new HSSFWorkbook(file);
		    HSSFSheet sheet = workbook.getSheet("cases_covers");
		    
		    boolean flag = true;
		    int rowPOinter = 4;
		    while(flag){
		    	Row row = sheet.getRow(rowPOinter);
			    if(row != null){
			    	Map<Integer, String> rowMap = new HashMap<>();
				    for(int index = 6; index < 56;index++){
				    	Cell cell = row.getCell(index);
				    	if(index == 6 && cell == null){
				    		flag = false;
				    		break;
				    	}
				    	if(cell != null){
				    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    	String cellValue = cell.getStringCellValue();
					    	if(index == 6 && "".equals(cellValue)){
					    		flag = false;
					    		break;
					    	}else{
					    		System.out.print(index+":"+cellValue);
					    		rowMap.put(index, cellValue);
					    	}
				    	}
				    }
				    if(rowMap.size() > 0){
				    	flipkartTemplateRows.add(rowMap);
				    }
			    }else{
			    	break;
			    }
			    System.out.println("");
			    rowPOinter++;
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
			if(file != null){
				try {
					file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(workbook != null){
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return (flipkartTemplateRows != null && flipkartTemplateRows.size() > 0) ? flipkartTemplateRows : null;
	}
	
	public List<Map<Integer,String>> readRandomFlipkartTextFile(String fileName) throws IOException{
		List<Map<Integer, String>> flipkartTemplateRows = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(new File(fileName)))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       // write the line
		    	Map<Integer, String> rowMap = new HashMap<>();
		    	int index = 3;
		    	String[] line1=line.split("\t");
	    			rowMap.put(3, line1[0]);
	    			rowMap.put(6, line1[1]);
	    			if(line1[0].contains("accessory-combo") || line1[0].contains("screen-guard") || line1[0].contains("tempered-")){
	    				System.out.println(line1[0]+"\t"+line1[1]);
	    			}
//		    	for(String line1: line.split("\t")){
//		    		if(!StringUtils.isBlank(line1)){
//		    			rowMap.put(index, line1);
//		    		}
//		    		index++;
//		    	}
		    	flipkartTemplateRows.add(rowMap);
		    }
		}
		return (flipkartTemplateRows != null && flipkartTemplateRows.size() > 0) ? flipkartTemplateRows : null;
	}
	
	public List<Map<Integer,String>> readRandomPayTMFile(String fileName){
		List<Map<Integer, String>> flipkartTemplateRows = new ArrayList<>();
		List<Integer> headers = new ArrayList<>();
		FileInputStream file = null;
		XSSFWorkbook workbook = null;
		try {
		    file = new FileInputStream(new File(fileName));//"D:\\AAA_WORK\\JARS\\POI Testing\\Sunglasses Template Information.xlsx"
		    workbook = new XSSFWorkbook(file);
		    XSSFSheet sheet = workbook.getSheet("Product Create");
		    
		    boolean flag = true;
		    int rowPOinter = 2;
		    while(flag){
		    	Row row = sheet.getRow(rowPOinter);
			    if(row != null){
			    	Map<Integer, String> rowMap = new HashMap<>();
				    for(int index = 0; index < 35;index++){
				    	Cell cell = row.getCell(index);
				    	if(index == 0 && cell == null){
				    		flag = false;
				    		break;
				    	}
				    	if(cell != null){
				    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    	String cellValue = cell.getStringCellValue();
					    	if(index == 0 && "".equals(cellValue)){
					    		flag = false;
					    		break;
					    	}else{
					    		System.out.print(index+":"+cellValue);
					    		rowMap.put(index, cellValue);
					    	}
				    	}
				    }
				    if(rowMap.size() > 0){
				    	flipkartTemplateRows.add(rowMap);
				    }
			    }else{
			    	break;
			    }
			    System.out.println("");
			    rowPOinter++;
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
			if(file != null){
				try {
					file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(workbook != null){
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return (flipkartTemplateRows != null && flipkartTemplateRows.size() > 0) ? flipkartTemplateRows : null;
	}
	
	public List<Map<Integer,String>> readRandomComboFlipkartFile(String fileName){
		List<Map<Integer, String>> flipkartTemplateRows = new ArrayList<>();
		List<Integer> headers = new ArrayList<>();
		FileInputStream file = null;
		HSSFWorkbook workbook = null;
		try {
		    file = new FileInputStream(new File(fileName));//"D:\\AAA_WORK\\JARS\\POI Testing\\Sunglasses Template Information.xlsx"
		    workbook = new HSSFWorkbook(file);
		    HSSFSheet sheet = workbook.getSheet("mobile_accessories_combo");
		    
		    boolean flag = true;
		    int rowPOinter = 4;
		    while(flag){
		    	Row row = sheet.getRow(rowPOinter);
			    if(row != null){
			    	Map<Integer, String> rowMap = new HashMap<>();
				    for(int index = 6; index < 33;index++){
				    	Cell cell = row.getCell(index);
				    	if(index == 6 && cell == null){
				    		flag = false;
				    		break;
				    	}
				    	if(cell != null){
				    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    	String cellValue = cell.getStringCellValue();
					    	if(index == 6 && "".equals(cellValue)){
					    		flag = false;
					    		break;
					    	}else{
					    		System.out.print(index+":"+cellValue);
					    		rowMap.put(index, cellValue);
					    	}
				    	}
				    }
				    if(rowMap.size() > 0){
				    	flipkartTemplateRows.add(rowMap);
				    }
			    }else{
			    	break;
			    }
			    System.out.println("");
			    rowPOinter++;
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
			if(file != null){
				try {
					file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(workbook != null){
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return (flipkartTemplateRows != null && flipkartTemplateRows.size() > 0) ? flipkartTemplateRows : null;
	}

	private List<Map<Integer,String>> readFlipkartTemplateFile(){
		List<Map<Integer, String>> flipkartTemplateRows = new ArrayList<>();
		List<Integer> headers = new ArrayList<>();
		FileInputStream file = null;
		XSSFWorkbook workbook = null;
		try {
		    file = new FileInputStream(new File(FlipkartInventoryFile.TEMPLATE_FILE_URL));//"D:\\AAA_WORK\\JARS\\POI Testing\\Sunglasses Template Information.xlsx"
		    workbook = new XSSFWorkbook(file);
		    XSSFSheet sheet = workbook.getSheet("Flipkart and SnapDeal Template");
		    
		    boolean flag = true;
		    int rowPOinter = 0;
		    int imageCellPointer = -1;
		    while(flag){
		    	Row row = sheet.getRow(rowPOinter);
			    if(row != null){
			    	Map<Integer, String> rowMap = new HashMap<>();
				    Iterator<Cell> iterator = row.cellIterator();
				    int cellPointer = 0;
				    while(iterator.hasNext()) {
				    	Cell cell = iterator.next();
				    	if(cell == null){
				    		cell = row.createCell(cellPointer);
				    	}
				    	cell.setCellType(Cell.CELL_TYPE_STRING);
				    	String cellValue = cell.getStringCellValue();
				    	if(cellPointer==0 && (cellValue == null || cellValue.isEmpty())){
				    		flag = false;
				    		break;
				    	}else if(rowPOinter == 0 && cellValue.contains("-")){
				    		String[] temp = cellValue.split("-");
				    		if(temp.length > 1){
				    			headers.add(new Integer(temp[1]));
				    		}
				    		if(temp[0].startsWith("Image")){
				    			imageCellPointer = cellPointer;
				    		}
				    	}else if(cellPointer < headers.size()){
				    		if(imageCellPointer == cellPointer && cellValue.contains(",")){
				    			String[] urls = cellValue.split(",");
				    			for(int i =0; i < urls.length; i++){
				    				rowMap.put(headers.get(cellPointer)+i, urls[i]);
				    			}
				    		}else{
				    			rowMap.put(headers.get(cellPointer), cellValue);
				    		}
				    	}else if(cellPointer >= headers.size()){
				    		break;
				    	}
				    	cellPointer++;
				    }
				    if(rowMap.size() > 0){
				    	flipkartTemplateRows.add(rowMap);
				    }
			    }else{
			    	break;
			    }
			    rowPOinter++;
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
			if(file != null){
				try {
					file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(workbook != null){
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return (flipkartTemplateRows != null && flipkartTemplateRows.size() > 0) ? flipkartTemplateRows : null;
	}
}
