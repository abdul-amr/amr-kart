package com.missing.single;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MissingSingleComboPresent {
	public static void main(String[] args) throws IOException {
		Map<String, Set<String>> single = new TreeMap<>();
		Map<String, Set<String>> combo = new TreeMap<>();
		MissingSingleComboPresent missingSingleComboPresent = new MissingSingleComboPresent();
		List<String> skus = missingSingleComboPresent.loadSkuList();
		Map<String, String> skuVsPhoneMap = missingSingleComboPresent.loadSkuVsPhoneMapFromCentralLocation("D:/Shezan Images1/Daily Images/MAR/SkuVsPhoneMapping");
		if(skus != null && skus.size() > 0){
			for(String sku : skus){
				String mobile = skuVsPhoneMap.get(sku);
				if(mobile == null){
					System.out.println("Mobile not found for Sku : "+sku);
					return;
				}
				System.out.println(mobile);
				mobile = mobile.trim().toLowerCase();
				String tokens[] = sku.split("-");
//				if(mobile.equals("mi 4") || mobile.equals("mi redmi 3s prime, mi redmi 3s prime") || mobile.equals("samsung galaxy a7-2017")){
//					System.out.println(sku);
//				}
				if(tokens.length == 3){
					if(single.containsKey(mobile)){
						single.get(mobile).add(tokens[1]);
					}else{
						Set<String> list = new TreeSet<>();
						list.add(tokens[1]);
						single.put(mobile, list);
					}
				}else if(tokens.length == 4){
					if(combo.containsKey(mobile)){
						combo.get(mobile).add(tokens[1]);
						combo.get(mobile).add(tokens[2]);
					}else{
						Set<String> list = new TreeSet<>();
						list.add(tokens[1]);
						list.add(tokens[2]);
						combo.put(mobile, list);
					}
				}
			}
			
			
			if(single.size() > 0 && combo.size() > 0){
				
				for(String cMobile : combo.keySet()){
					if(single.containsKey(cMobile)){
						String printS = cMobile+":";
						for(String cModel : combo.get(cMobile)){
							if(!single.get(cMobile).contains(cModel)){
								printS+= cModel+"\t";
							}
						}
						if(!printS.equals(cMobile+":")){
							System.out.println(printS);
						}
					}else{
						String printS = cMobile+":";
						for(String cModel : combo.get(cMobile)){
							printS+= cModel+"\t";
						}
						System.out.println(printS);
					}
				}
				
				
			}
		}else{
			System.out.println("Please list down all the skus of the account in skuList.txt file");
		}
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

	private Map<String, String> loadSkuVsPhoneMapFromCentralLocation(String folderName) throws IOException {
		
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
}
