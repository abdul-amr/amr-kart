package com.amr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author MDSR
 *
 *This will extract the QC Failed Listings(for using it in new catalog) and put it in the below mentioned folder.
 *Also, it will extract QC Passed and creates the skuVsPhoneName mapping(this is used to get the phoneName if it is missing in listings file) and put it in the below folder
 *
 *QC Failed Folder : D:\AAA_WORK\JARS\POI Testing\Flipkart Bulk Listing File\AMR_QC_Rejected\listings_*.txt
 *QC Passed Folder : D:\AAA_WORK\JARS\POI Testing\Flipkart Bulk Listing File\AMR_Catalog_Files\QC Passed SKU_PhoneName\skuVsNameMapping_*.txt
 */
public class ExtractQCListings {
	public static void main(String[] args) throws IOException {
		String path = getPathToCatalogFolder("./AllCatalogFilesLocation.txt");
		System.out.println("Path to All Catalog Files Folder : "+path);
		
		
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
