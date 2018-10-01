package com.amr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


/*
 * This will filter the catalog file before uploading. 
 * It will remove the existing SKUs from catalog file so that you don't get any error while uploading.
 * */
public class RemoveExistingsSkuFromCatalog {
	
	public static void main(String[] args) throws IOException {
		
		List<String> currentSkus = getCurrentSkuList("./currentSkuList.txt");
		List<String> updatedCatalog = new ArrayList<>();

		String sCurrentLine;
		BufferedReader br1 = new BufferedReader(new FileReader("./catalogFile.txt"));
		while ((sCurrentLine = br1.readLine()) != null) {
			if(!sCurrentLine.isEmpty() && sCurrentLine.indexOf("\t") != -1){
				String sku = sCurrentLine.split("\t")[0];
				if(!currentSkus.contains(sku)){
					updatedCatalog.add(sCurrentLine);
				}
			}
        }
		
		writeToFile(updatedCatalog);
		
		
	}

	private static void writeToFile(List<String> list) throws IOException {
		// TODO Auto-generated method stub
		File fout = new File("./updatedCatalogFile.txt");
		if(!fout.exists()){
			fout.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	 
		for(String entry : list){
	    	bw.write(entry);
			bw.newLine();
	    	
	    }
		bw.close();
	}

	private static List<String> getCurrentSkuList(String fileName) throws IOException {
		// TODO Auto-generated method stub
		List<String> skuList = new ArrayList<>();
		String sCurrentLine;
		BufferedReader br1 = new BufferedReader(new FileReader(fileName));
		while ((sCurrentLine = br1.readLine()) != null) {
			if(!sCurrentLine.isEmpty()){
				skuList.add(sCurrentLine.trim());
			}
        }
		return skuList;
	}
}
