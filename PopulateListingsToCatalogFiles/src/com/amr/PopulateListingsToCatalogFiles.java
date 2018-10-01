package com.amr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class PopulateListingsToCatalogFiles {

	private static String sheetType="screen_guard,mobile_accessories_combo,cases_covers";
	
	public static void main(String[] args) throws IOException {
		String path = getPathToCatalogFolder("./catalogFolderPath.txt");
		System.out.println("Path to Catalog Folder : "+path);
		
		if(args.length > 0 && args[0] != null && !"".equals(args[0]) && sheetType.contains(args[0])){
			sheetType = args[0];
		}else{
			System.out.println("Error : Sheet Type is Missing. Please mention one of screen_guard//mobile_accessories_combo//cases_covers");
			return;
		}
		populateListings(path);
	}
	
	private static  List<Map<Integer,String>> readRandomFlipkartTextFile(String fileName) throws IOException{
		List<Map<Integer, String>> flipkartTemplateRows = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(new File(fileName)))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       // write the line
		    	Map<Integer, String> rowMap = new HashMap<>();
		    	int index = 6;
		    	for(String line1: line.split("\t")){
		    		if(line1 != null && !line1.isEmpty()){
		    			rowMap.put(index, line1);
		    		}
		    		index++;
		    	}
		    	flipkartTemplateRows.add(rowMap);
		    }
		}
		return (flipkartTemplateRows != null && flipkartTemplateRows.size() > 0) ? flipkartTemplateRows : null;
	}

	private static void populateListings(String listingFolder) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		List<String> fileNames = new ArrayList<>();
		
		for(File file : new File(listingFolder).listFiles()){
			if(!file.getName().contains("listings.txt")){
				fileNames.add(file.getAbsolutePath());
			}else{
				System.out.println(file.getAbsolutePath());
			}
		}
		
		System.out.println(fileNames.size());
		
		List<Map<Integer,String>> data = readRandomFlipkartTextFile(listingFolder+"/listings.txt");
		Collections.shuffle(data);
		int start = 0;
		int fileCount = 0;
		List<Map<Integer,String>> temp = new ArrayList<>();
		System.out.println(data.size());
		for(Map<Integer,String>data1: data){
			if(start < 301 && data.indexOf(data1) != data.size()-1){
				temp.add(data1);
			}else if(data.indexOf(data1) == data.size()-1){
				temp.add(data1);
				String writeFile = fileNames.get(fileCount);
				System.out.println(writeFile);
				writeRandomResponseToFlipkart(temp,writeFile);
			}else{
				String writeFile = fileNames.get(fileCount);
				System.out.println(writeFile);
				writeRandomResponseToFlipkart(temp,writeFile);
				fileCount++;
				start = 0;
				temp = new ArrayList<>();
				temp.add(data1);
				continue;
			}
			start++;
		}
		System.out.println("DONE!!!!");
	}
	
	private static boolean writeRandomResponseToFlipkart(List<Map<Integer, String>> flipkartTemplateRows,String fileName){
		FileInputStream file = null;
		FileOutputStream output_file = null;
		HSSFWorkbook workbook = null;
		try {
			File fileq = new File(fileName);//"D:\\AAA_WORK\\JARS\\POI Testing\\Flipkart Bulk Listing File"
				file = new FileInputStream(fileq);
			    workbook = new HSSFWorkbook(file);
			    HSSFSheet sheet = workbook.getSheet(sheetType);//screen_guard//mobile_accessories_combo//cases_covers
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
					    for(int cellPointer=6; cellPointer < 56; cellPointer++) {
					    	Cell cell = row.getCell(cellPointer);
					    	if(cell == null){
					    		cell = row.createCell(cellPointer);
					    	}
//					    	System.out.println(inputRowMap.get(cellPointer));
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

	private static String getPathToCatalogFolder(String fileName) throws IOException {
		// TODO Auto-generated method stub
		List<String> skuList = new ArrayList<>();
		String sCurrentLine;
		BufferedReader br1 = new BufferedReader(new FileReader(fileName));
		while ((sCurrentLine = br1.readLine()) != null) {
			if(!sCurrentLine.isEmpty()){
				return sCurrentLine;
			}
        }
		System.out.println("Error : Path to Catalog Folder is Empty");
		return "";
	}
}
