package amr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

import test.EmailUtils;

public class CreateCombosListings {
	

	private final static String  SKU_COUNT_PROPERTIES = "./code/skuCount.txt";
	private final static String  COMBO_COUNT_PROPERTIES = "./code/comboCount.txt";


	private Map<String, String> comboCaseTypes;
	private Map<String, String> comboSKUMap;
	private Map<String, String> caseTypesMap;
	private int comboCount;
	private Map<String, String> keywordsMap;
	private int comboSkuStart;
	private Map<String, String> colorMap;
	private int skuStart;
	private boolean plusImage;
	private int combosLimit=0;
	private int mACombosLimit=0;
	private int combosCreated = 0;
	private ConfigurationLoader configurationLoader;
	private Properties iSProp;
	private ImageShack imageShack;
	private String imageShackUsername;
	private String imageShackPassword;
	private String imageShackAPIKey;
	List<String> filePaths;
	private String dateFolderString = "";
	private boolean isCoversCombo;
	private boolean isMACombo;
	private List<String> coversComboList;
	private List<String> mAComboList;
	private EmailUtils emailUtils;
	List<String> listingInfoList;
	List<String> configFilePaths;
	
	public CreateCombosListings() throws FileNotFoundException, IOException{
		configurationLoader = new ConfigurationLoader();
		comboSKUMap = configurationLoader.loadComboSkuMap();
		comboCaseTypes = configurationLoader.loaComboCaseTypes();
		caseTypesMap = configurationLoader.loadCaseTypes();
		this.comboCount = 0;
		keywordsMap = configurationLoader.loadKeywords();
		colorMap = configurationLoader.loadColorMap();
		this.skuStart= configurationLoader.getLastSkuCount();
		this.plusImage = false;
		this.comboSkuStart = configurationLoader.getLastComboSkuCount();
		iSProp = configurationLoader.loadImageShackProperties();
		imageShackUsername = iSProp.getProperty("username");
		imageShackPassword = iSProp.getProperty("password");
		imageShackAPIKey = iSProp.getProperty("apikey");
		imageShack = new ImageShack(this.imageShackAPIKey, this.imageShackUsername, this.imageShackPassword);
		filePaths = new ArrayList<>();
		coversComboList = new ArrayList<>();
		mAComboList = new ArrayList<>();
		listingInfoList = new ArrayList<>();
		emailUtils = new EmailUtils();
	}
	
	public void createDummyComboForEachPhoneNew(String mobileFolder) {
		File test = new File(mobileFolder+"/Combos/Covers");
		if(test != null && test.exists() && test.isDirectory() && test.listFiles().length > 0){//Combos already created
			System.out.println("Combos Already created for Mobile : "+mobileFolder);
			System.out.println("Please delete the previous Combos Covers folder to proceed");
			System.exit(0);
		}
		String[] caseFolers = getComboCaseTypes().get("All").split(",");//Transparent,Defender,Cherry,Chrome,Flip Cover Gold,Flip Cover Black,Tempered Glass
		for(int i = 0; i < caseFolers.length; i++){	//Transparent,Defender,Cherry,Chrome
			File caseFolder1 = new File(mobileFolder+"/"+caseFolers[i]);
			if(caseFolder1.exists() && caseFolder1.isDirectory()){
//				createCombinationsOfOneType(mobileFolder,caseFolder1,eachCase);
				for(int j=i+1; j< caseFolers.length; j++){
					File caseFolder2 = new File(mobileFolder+"/"+caseFolers[j]);
					if(caseFolder2.exists() && caseFolder2.isDirectory()){
						//Check if Covers Combo or MA combo
						if(isCoversCombo() && getComboCaseTypes().get(caseFolers[i]).equalsIgnoreCase("Cover:Y") && getComboCaseTypes().get(caseFolers[j]).equalsIgnoreCase("Cover:Y")){
							createDummyCombinationsOfMixedTypes(mobileFolder,caseFolder1,caseFolder2);
						}else if(isMACombo() && !getComboCaseTypes().get(caseFolers[i]).equals(getComboCaseTypes().get(caseFolers[j]))){
							createDummyCombinationsOfMixedTypes(mobileFolder,caseFolder1,caseFolder2);
						}
					}
				}
			}
		}
	}

	public void createComboForOnePhoneNew(String mF) throws IOException {
		// TODO Auto-generated method stub
		String[] mobileFolders =mF.split(",");
		int combostart = this.comboCount;
		
		for(String mobileFolder: mobileFolders){
			int combostartEachMobile = this.comboCount;
			System.out.println("Resizing Images for : "+mobileFolder);
			resizeImagesForOnePhone(mobileFolder);
			this.coversComboList = new ArrayList<>();
			this.mAComboList = new ArrayList<>();
			createDummyComboForEachPhoneNew(mobileFolder);
			if(isCoversCombo() && this.coversComboList.size() == 0){
				System.out.println("Failed :: No Combo Created for : "+mobileFolder);
				System.exit(0);
			}else if(isMACombo() && this.mAComboList.size() == 0){
				System.out.println("Failed :: No Mobile Accessories Combo Created for : "+mobileFolder);
				System.exit(0);
			}
			if(isCoversCombo() && this.coversComboList.size() > 0){
				System.out.println("Dummy Combos Created : "+this.coversComboList.size());
				//shuffle dummy combos and extract random 2000
				int allMobilesLimit = this.getCombosLimit();
				allMobilesLimit = allMobilesLimit/mobileFolders.length;
//				allMobilesLimit = roundDown(allMobilesLimit, 10);
				System.out.println("Creating Combos for : "+mobileFolder);
				createCombos(allMobilesLimit, this.coversComboList);
				System.out.println("Total Combos Created : "+(this.comboCount - combostartEachMobile));
			}else if(isMACombo() && this.mAComboList.size() > 0){
				System.out.println("Dummy Mobile Accessories Combos Created : "+this.mAComboList.size());
				//shuffle dummy combos and extract random 2000
				int allMobilesLimit = this.getmACombosLimit();
				allMobilesLimit = allMobilesLimit/mobileFolders.length;
//				allMobilesLimit = roundDown(allMobilesLimit, 0);
				createCombos(allMobilesLimit, this.mAComboList);
				System.out.println("Total Combos Created : "+(this.comboCount - combostartEachMobile));
			}
		}
		System.out.println("Total combos for all mobiles created is : "+(this.comboCount - combostart));
	}
	
	int roundDown(int n, int m) {
	    return n >= 0 ? (n / m) * m : ((n - m + 1) / m) * m;
	}

	private void createCombos(int limit, List<String> comboList) {
		// TODO Auto-generated method stub
		//Generating Unique Random Nummbers
		List<Integer> randomIntegers = new ArrayList<>();
		for(int m =0; m<comboList.size(); m++){
			randomIntegers.add(m);
		}
		Collections.shuffle(randomIntegers);
		
		if(comboList.size() < limit){
			limit = comboList.size();
		}
		for(int i = 0; i < limit; i++){
			String comboString = comboList.get(randomIntegers.get(i));
			String[] comboStringArr = comboString.split("::");
//			image1.getAbsolutePath()+"::"+image2.getAbsolutePath()+"::"+mobileFolder+"::"+caseFolder1.getName()+"::"+caseFolder2.getName()
			boolean isSuccess = createCombo(comboStringArr[0], comboStringArr[1], comboStringArr[2]+"/Combos/Covers/"+comboStringArr[3]+"_"+comboStringArr[4]+"_"+this.comboCount+".jpeg",this.isPlusImage());
			if(isSuccess){
//				System.out.println("success");				
				this.comboCount++;
			}
		}
	}

	private void createDummyCombinationsOfMixedTypes(String mobileFolder, File caseFolder1, File caseFolder2) {
		// TODO Auto-generated method stub
		File newDir = new File(mobileFolder+"/Combos/Covers");
		if(!newDir.exists()){
			newDir.mkdirs();
		}
		File[] imageList1 = caseFolder1.listFiles();
		File[] imageList2 = caseFolder2.listFiles();
		if(imageList1 != null && imageList1.length > 0 && imageList2 != null && imageList2.length > 0){
			for(File image1 : imageList1){
				if(image1.isDirectory()){
					continue;
				}
				for(File image2 : imageList2){
					if(image2.isDirectory()){
						continue;
					}
					String dummyImageFile = image1.getAbsolutePath()+"::"+image2.getAbsolutePath()+"::"+mobileFolder+"::"+caseFolder1.getName()+"::"+caseFolder2.getName();
					if(isCoversCombo()){
						coversComboList.add(dummyImageFile);
					}else if(isMACombo()){
						mAComboList.add(dummyImageFile);
					}
//					createCombo(image1.getAbsolutePath(), image2.getAbsolutePath(), mobileFolder+"/Combos/Covers/"+caseFolder1.getName()+"_"+caseFolder2.getName()+"_"+this.comboCount+".jpeg",this.isPlusImage());
//					this.comboCount++;
				}
			}
		}
	}

	private void createCombinationsOfMixedTypes(String mobileFolder, File caseFolder1, File caseFolder2) {
		// TODO Auto-generated method stub
		File newDir = new File(mobileFolder+"/Combos/Covers");
		if(!newDir.exists()){
			newDir.mkdirs();
		}
		File[] imageList1 = caseFolder1.listFiles();
		File[] imageList2 = caseFolder2.listFiles();
		if(imageList1 != null && imageList1.length > 0 && imageList2 != null && imageList2.length > 0){
			for(File image1 : imageList1){
				for(File image2 : imageList2){
					createCombo(image1.getAbsolutePath(), image2.getAbsolutePath(), mobileFolder+"/Combos/Covers/"+caseFolder1.getName()+"_"+caseFolder2.getName()+"_"+this.comboCount+".jpeg",this.isPlusImage());
					this.comboCount++;
				}
			}
		}
	}

	public List<String> createMobileAccessoriesCombo(String sellerName, String filePath) throws IOException {
		// TODO Auto-generated method stub
		List<String> alllines = new ArrayList<>();
		String[] paths = filePath.split(",");
		for(String mobileFolder : paths){
			File catalog = new File(mobileFolder+"/Combos/Catalog");
			if(catalog != null && catalog.exists() && catalog.isDirectory()){
				File[] files = catalog.listFiles();
				for(File file : files){
					if(file.getName().startsWith("Final")){
						List<String> lines = new ArrayList<>();
//						File writeFile = new File(mobileFolder+"/###MobileAccessoriesCombo_"+sellerName+"/"+file.getName());
//						writeFile.getParentFile().mkdirs();
						try (BufferedReader br = new BufferedReader(new FileReader(file))) {
							String line;
							while ((line = br.readLine()) != null) {
								String[] tokens = line.split("\t");
								String[] skuToken = tokens[0].split("-");
								if(skuToken.length == 4){
									String sku = tokens[0];
//									String sku = skuToken[0]+"-"+skuToken[1]+"-"+skuToken[2]+"-a"+this.comboSkuStart++;
//									String type1 = skuToken[1].equals("fcb") || skuToken[1].equals("fcg") ? "Flip Cover" : skuToken[1].equals("sg") ? "Screen Guard" : "Back Cover";
//									String type2 = skuToken[2].equals("fcb") || skuToken[2].equals("fcg") ? "Flip Cover" : skuToken[2].equals("sg") ? "Screen Guard" : "Back Cover";
//									String type3 = skuToken[2].equals("sg") ? "Screen Protector" : "Cover";
									line = tokens[0]+"\t"+
											tokens[1]+"\t"+
											tokens[2]+"\t"+
											tokens[8]+"\t"+//change
											tokens[4]+"\t"+
											tokens[8].split("::")[0]+"\t"+
											tokens[5]+"\t"+
											tokens[8].split("::")[1]+"\t"+//Cover or Screen Protector
											tokens[19]+"\t"+
											tokens[11]+"\t"+
											"\t"+
											"\t"+
											"\t"+
											"\t"+
											"\t"+
											"\t"+
											tokens[21]+"\t"+
											tokens[20]+"\t"+
											"\t"+
											tokens[27]+"\t";
//									if(sellerName.equals("MAR")){
//							    		line = line.replaceAll("mzt-", "mot-");
//							    		line = line.replaceAll("Mozette\tM-", "Motaz\tMt-");
//							    		line = line.replaceAll("mozette", "motaz");
//							    		line = line.replaceAll("Mozette", "Motaz");
//							    		line = line.replaceAll("MOZETTE", "MOTAZ");
//							    	}else if(sellerName.equals("AMR")){
//							    		line = line.replaceAll("mzt-", "xol-");
//							    		line = line.replaceAll("Mozette\tM-", "XOLDA\tX-");
//							    		line = line.replaceAll("mozette", "xolda");
//							    		line = line.replaceAll("Mozette", "XOLDA");
//							    		line = line.replaceAll("MOZETTE", "XOLDA");
//							    	}else if(sellerName.equals("TRAM")){
//							    		line = line.replaceAll("mzt-", "zot-");
//							    		line = line.replaceAll("Mozette\tM-", "ZOTIKOS\tZt-");
//							    		line = line.replaceAll("mozette", "zotikos");
//							    		line = line.replaceAll("Mozette", "ZOTIKOS");
//							    		line = line.replaceAll("MOZETTE", "ZOTIKOS");
//							    	}
									if(sellerName.equals("MAR")){
							    		line = line.replaceAll("mzt-", "mot-");
							    		line = line.replaceAll("Mozette\tM-", "Motaz\tMt-");
							    		line = line.replaceAll("mozette", "motaz");
							    		line = line.replaceAll("Mozette", "Motaz");
							    		line = line.replaceAll("MOZETTE", "MOTAZ");
							    	}else if(sellerName.equals("AMR_XOLDA")){
							    		line = line.replaceAll("mzt-", "xol-");
							    		line = line.replaceAll("Mozette\tM-", "XOLDA\tX-");
							    		line = line.replaceAll("mozette", "xolda");
							    		line = line.replaceAll("Mozette", "XOLDA");
							    		line = line.replaceAll("MOZETTE", "XOLDA");
							    	}else if(sellerName.equals("TRAM")){
							    		line = line.replaceAll("mzt-", "zot-");
							    		line = line.replaceAll("Mozette\tM-", "ZOTIKOS\tZt-");
							    		line = line.replaceAll("mozette", "zotikos");
							    		line = line.replaceAll("Mozette", "ZOTIKOS");
							    		line = line.replaceAll("MOZETTE", "ZOTIKOS");
							    	}else if(sellerName.equals("AMR_MOZETTE")){
//							    		line = line;
							    	}if(sellerName.equals("MAR_Mozette")){
							    		line = line.replaceAll("mzt-", "mzm-");
							    		line = line.replaceAll("Mozette\tM-", "Mozette\tMt-");
							    	}
									lines.add(line);
//									System.out.println(line);
								}
							}
						}
//						FileUtils.writeLines(writeFile, lines);
						alllines.addAll(lines);
					}
				}
			}
		}
		//update combo count in comboCount.txt file
		updateComboSkuCount();
		return alllines;
	}

	public void createCombosCatalog(String combosFolders) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		String[] cF = combosFolders.split(",");
		
		for(String combosFoldr : cF){
			List<File> tempFiles = new ArrayList<>();
			File test = new File(combosFoldr+"/Combos/Catalog");
			if(test!=null && test.isDirectory() && test.exists() && test.listFiles().length > 0){//Combos already created
				continue;
			}
			
			String mobileName = combosFoldr.substring(combosFoldr.lastIndexOf("\\")+1);
			System.out.println("Creating Combo Listings for : "+mobileName);
			File combosFolder = new File(combosFoldr+"/Combos");
			if(combosFolder != null && combosFolder.exists() && combosFolder.isDirectory()){
				File[] caseTypes = combosFolder.listFiles();
				if(caseTypes != null && caseTypes.length > 0){
					for(File caseType : caseTypes){
						if(caseType.isDirectory() && !caseType.getName().contains("#") && !caseType.getName().contains("Catalog")){
//							String caseName = caseType.getName();
							
							//move all pending files to Covers
							File pending = new File(caseType.getAbsolutePath()+"/Pending");
							if(pending.exists()){
								File[] pendingFiles = pending.listFiles();
								for(File file : pendingFiles){
									FileUtils.moveFileToDirectory(file, caseType, false);
								}
								FileUtils.deleteDirectory(pending);
							}

							File[] imageList = caseType.listFiles();
							tempFiles = Arrays.asList(imageList);
							
							//Generating Unique Random Nummbers
							List<Integer> randomIntegers = new ArrayList<>();
							for(int m =0; m<imageList.length; m++){
								randomIntegers.add(m);
							}
							Collections.shuffle(randomIntegers);
							
							int comboLimit = getCombosLimit();
							
							if(getCombosLimit() == 0 || getCombosLimit() > imageList.length){
								comboLimit = imageList.length;//If there is no limit on combos
							}
							
							List<File> fileList = new ArrayList<>();
							if(imageList != null && imageList.length > 0){
								int limit = comboLimit;
								for(int i = 0; i < limit; i++){
									if(fileList.size() <= 9){
//										System.out.println(imageList[randomIntegers.get(i)].getName());
										if(imageList[randomIntegers.get(i)].isDirectory()){
											continue;
										}
										fileList.add(imageList[randomIntegers.get(i)]);
										tempFiles.remove(randomIntegers.get(i));
									}else{
										createComboListingsNew(fileList, combosFoldr, mobileName);
										fileList.removeAll(fileList);
										fileList = new ArrayList<>();
										if(imageList[randomIntegers.get(i)].isDirectory()){
											continue;
										}
										fileList.add(imageList[randomIntegers.get(i)]);
										tempFiles.remove(randomIntegers.get(i));
									}
									if((i+1) == (limit)){//last file
										createComboListingsNew(fileList, combosFoldr, mobileName);
									}
								}
							}
						}
					}
				}
			}
			//Move All the pending Images to pending folder
			if(tempFiles.size() > 0){
				File destFile = new File(tempFiles.get(0).getParentFile().getAbsolutePath()+"/Pending");
				FileUtils.forceMkdir(destFile);
				for(File file : tempFiles){
					FileUtils.moveFileToDirectory(file, destFile, false);
				}
			}
		}
		//Create Final Listings files
		createFinalComboListing(combosFolders);
		
		//update combo count in comboCount.txt file
		updateComboSkuCount();
	}
	
	public void createComboListingsNew(List<File> fileList, String combosFoldr, String mobileName) throws ClientProtocolException, IOException{
		List<String> imageUrls = imageShack.processImageUploadForCombo(fileList, "combos",combosFoldr+"/Combos");
		if(imageUrls.size() == fileList.size()){
			List<Map<Integer, String>> completedListings = popuateEachRowForCombo(fileList,imageUrls,mobileName);
			writeCombosToFile(completedListings,combosFoldr+"/Combos");
			combosCreated += 10;
			System.out.println("Combos Created : "+combosCreated);
		}
	}

	public void createFinalComboListing(String mobileFolders) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		String[] mF = mobileFolders.split(",");
		for(String comboCatalogFolder : mF){
			comboCatalogFolder += "/Combos/Catalog";
			File mainFolder = new File(comboCatalogFolder);
			int fileCount = 1;
			if(mainFolder != null && mainFolder.exists()){
				File[] fileList = mainFolder.listFiles();
				List<String> list = new ArrayList<>();
				for(File eachFile : fileList){
					try (BufferedReader br = new BufferedReader(new FileReader(eachFile))) {
						String line;
						while ((line = br.readLine()) != null) {
							if(list.size() == 300){
								writeEachFinalFile(list, fileCount, comboCatalogFolder);
								fileCount++;
								list = new ArrayList<>();
							}
//							System.out.println(line);
							list.add(line);
						}
					}
				}
				if(list.size() > 0){
					writeEachFinalFile(list, fileCount, comboCatalogFolder);
					listingInfoList.add("Mobile : "+comboCatalogFolder.substring(comboCatalogFolder.lastIndexOf("\\")+1,comboCatalogFolder.length())+" Listings Created : "+list.size());
					System.out.println("Mobile : "+comboCatalogFolder.substring(comboCatalogFolder.lastIndexOf("\\")+1,comboCatalogFolder.length())+" Listings Created : "+list.size());
				}
			}
		}
	}
	
	public void writeEachFinalFile(List<String> list, int fileCount, String comboCatalogFolder) throws IOException{
		FileWriter fw = new FileWriter(comboCatalogFolder+"/Final"+fileCount+".txt");
		for(String eachLine : list){
			fw.write(eachLine);
			fw.write("\n");
		}
		fw.close();
		
	}
	
	public List<String> extractCombosCatalogForOtherSellers(String sellerName, String mobiles) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		String[] mobilesF = mobiles.split(",");
		List<String> listings = new ArrayList<>();
		for(String each : mobilesF){
			File eachMobile = new File(each);
			File catalog = new File(eachMobile.getAbsolutePath()+"/Combos/Catalog");
			if(catalog != null && catalog.exists() && catalog.isDirectory()){
				File[] finals = catalog.listFiles();
				for(File eachFinal : finals){
					if(!eachFinal.getName().contains("Final")){
						continue;
					}
//					File newFinal = new File(eachMobile.getAbsolutePath()+"/###Combos_Completed_"+sellerName+"/"+eachFinal.getName());
//					newFinal.getParentFile().mkdirs();
//					PrintWriter writer = new PrintWriter(newFinal);
					try (BufferedReader br = new BufferedReader(new FileReader(eachFinal))) {
					    String line;
					    while ((line = br.readLine()) != null) {
					    	if(sellerName.equals("MAR")){
					    		line = line.replaceAll("mzt-", "mot-");
					    		line = line.replaceAll("Mozette\tM-", "Motaz\tMt-");
					    		line = line.replaceAll("mozette", "motaz");
					    		line = line.replaceAll("Mozette", "Motaz");
					    		line = line.replaceAll("MOZETTE", "MOTAZ");
					    	}else if(sellerName.equals("AMR_XOLDA")){
					    		line = line.replaceAll("mzt-", "xol-");
					    		line = line.replaceAll("Mozette\tM-", "XOLDA\tX-");
					    		line = line.replaceAll("mozette", "xolda");
					    		line = line.replaceAll("Mozette", "XOLDA");
					    		line = line.replaceAll("MOZETTE", "XOLDA");
					    	}else if(sellerName.equals("TRAM")){
					    		line = line.replaceAll("mzt-", "zot-");
					    		line = line.replaceAll("Mozette\tM-", "ZOTIKOS\tZt-");
					    		line = line.replaceAll("mozette", "zotikos");
					    		line = line.replaceAll("Mozette", "ZOTIKOS");
					    		line = line.replaceAll("MOZETTE", "ZOTIKOS");
					    	}else if(sellerName.equals("AMR_MOZETTE")){
//					    		line = line;
					    	}if(sellerName.equals("MAR_Mozette")){
					    		line = line.replaceAll("mzt-", "mzm-");
					    		line = line.replaceAll("Mozette\tM-", "Mozette\tMt-");
					    	}
//					    	System.out.println(line);
//					    	writer.print(line+"\n");
					    	listings.add(line);
					    }
					}
//					writer.close();
				}
			}else{
				System.out.println("\n\n\n\n\n\n###backup does not exists for : "+each);
			}
		}
		return listings;
	}
	
	private void writeCombosToFile(List<Map<Integer, String>> completeListings,String mobileFolder) {
		
		String fileName = mobileFolder+"/Catalog/Covers_"+this.comboSkuStart+".txt";
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		try{
		    PrintWriter writer = new PrintWriter(file);
		    
			if(completeListings.size() > 0){
				for(Map<Integer, String> row : completeListings){
					for(int cellPointer=6; cellPointer < 48; cellPointer++) {
				    	String cell = row.get(cellPointer);
				    	if(cell != null && !cell.isEmpty()){
				    		writer.print(cell+"\t");
//				    		System.out.print(cell+"\t");
				    	}else{
				    		writer.print("\t");
//				    		System.out.print("\t");
				    	}
				    }
					writer.print("\n");
//					System.out.print("\n");
				}
				
			}
		    writer.close();
		} catch (IOException e) {
		   // do something
			System.out.println(e);
		}
	}
	
	private List<Map<Integer, String>> popuateEachRowForCombo(List<File> fileList, List<String> imageUrls, String mobileName) {
		// TODO Auto-generated method stub
		List<Map<Integer, String>> completeListings = new ArrayList<>();
		for(int i=0; i<imageUrls.size(); i++){
			String fullName = fileList.get(i).getName();
			if(!fullName.contains("_")){
				continue;
			}
			String imageName1 = fullName.split("_")[0];
			String imageName2 = fullName.split("_")[1];
			
			String[] comboSetName = {"",""};
//			if(imageName1.contains("Flip")){
//				comboSetName[0] = "FLIP COVER";
//			}else if(imageName1.contains("Glass")){
//				comboSetName[0] = "SCREEN PROTECTOR(TEMPERED GLASS)";
//			}else{
//				comboSetName[0] = "BACK COVER";
//			}
//			if(imageName2.contains("Flip")){
//				comboSetName[1] = "FLIP COVER";
//			}else if(imageName2.contains("Glass")){
//				comboSetName[1] = "SCREEN PROTECTOR(TEMPERED GLASS)";
//			}else{
//				comboSetName[1] = "BACK COVER";
//			}

			String type1 = this.isCoversCombo ? getCaseTypesMap().get(imageName1) : getComboCaseTypes().get(imageName1);
			if(type1.indexOf(",") != -1){
				type1 = type1.split(",")[0].split(":")[0];
			}else{
				type1 = type1.split(":")[0];
			}
			String type2 = this.isCoversCombo ? getCaseTypesMap().get(imageName2) : getComboCaseTypes().get(imageName2);
			if(type2.indexOf(",") != -1){
				type2 = type2.split(",")[0].split(":")[0];
			}else{
				type2 = type2.split(":")[0];
			}
			Map<Integer, String> eachRow = new HashMap<>();
			this.skuStart++;
			String color1 = this.getColorMap().get(imageName1);
			String color2 = this.getColorMap().get(imageName2);
			eachRow.put(6, "mzt"+getComboSKUMap().get(imageName1)+""+getComboSKUMap().get(imageName2)+"-a"+this.comboSkuStart);
			eachRow.put(7, "Mozette");
			eachRow.put(8, "M"+getComboSKUMap().get(imageName1)+""+getComboSKUMap().get(imageName2)+"-a"+this.comboSkuStart);
			eachRow.put(9, type1);
			eachRow.put(10, color1+"::"+color2);
			eachRow.put(11, mobileName);
			eachRow.put(12, "Rubber::Plastic");
			eachRow.put(13, "Mobile");
			eachRow.put(14, type1+"::"+type2);//use ::*************
			eachRow.put(15, "No Theme");
			eachRow.put(16, color1+"::"+color2);
			eachRow.put(17, imageUrls.get(i));
			eachRow.put(25, "2");
			String desc = "";
			
			
			if(type1.equals(type2) && color1.equals(color2)){
				desc = "BEST QUALITY COMBO SET OF 2 "+color1+" "+type1.toUpperCase()+". MOZETTE "+type1.toUpperCase()+"S ARE DESIGNED SPECIALLY TO FULLY PROTECT YOUR "+mobileName.toUpperCase()+" FROM GETTING DAMAGED. THEY ARE IMPACT RESISTANT AND HIGHLY DURABLE. MOREOVER, ITS EXTREME SLIM PROFLE AND LIGHT WEIGHT ADDS NO ADDITIONAL BULK TO YOUR PHONE. GIVES COMPLETE ACCESS TO ALL THE BUTTONS, PORTS AND SENSORS. THE MOZETTE CASE COVERS 100% OF THE OUTER SURFACE OF THE PHONE AND PRECISION MOLDED WITH NO SEAMS OR SHARP EDGES. THE "+mobileName.toUpperCase()+" "+type1.toUpperCase()+" AND "+type2.toUpperCase()+" PREMIUM QUALITY COMBO GIVES MAXIMUM PROTECTION.";;
			}else{
				desc = "BEST QUALITY COMBO SET OF 1 "+color1+" "+type1.toUpperCase()+" AND 1 "+color2+" "+type2.toUpperCase()+". MOZETTE "+type1.toUpperCase()+" AND "+type2.toUpperCase()+" ARE DESIGNED SPECIALLY TO FULLY PROTECT YOUR "+mobileName.toUpperCase()+" FROM GETTING DAMAGED. THEY ARE IMPACT RESISTANT AND HIGHLY DURABLE. MOREOVER, ITS EXTREME SLIM PROFLE AND LIGHT WEIGHT ADDS NO ADDITIONAL BULK TO YOUR PHONE. GIVES COMPLETE ACCESS TO ALL THE BUTTONS, PORTS AND SENSORS. THE MOZETTE CASE COVERS 100% OF THE OUTER SURFACE OF THE PHONE AND PRECISION MOLDED WITH NO SEAMS OR SHARP EDGES. THE "+mobileName.toUpperCase()+" "+type1.toUpperCase()+" AND "+type2.toUpperCase()+" PREMIUM QUALITY COMBO GIVES MAXIMUM PROTECTION.";;
			}
			
			eachRow.put(26, desc.toUpperCase());
			String keywords = getKeywordsMap().get(imageName1);
			String keywords1 = keywords.replace("mobilename", mobileName.toLowerCase());
			if(keywords1.length() >= 1000){
				keywords1 = keywords1.substring(0,999);
				keywords1 = keywords1.substring(0,keywords1.lastIndexOf(":"));
			}
//			eachRow.put(27,keywords1);
			eachRow.put(27, "FULLY PROTECTIVE::MORE RELIABLE");
			String keyFeatures = "";
			
			if(comboSetName[0].equals(comboSetName[1]) && color1.equals(color2)){
				keyFeatures = "Combo Offer in relatively low price::"+"COMBO SET OF 2 "+color1+" "+comboSetName[0];
			}else{
				keyFeatures = "Combo Offer in relatively low price::"+"COMBO SET OF 1 "+color1+" "+comboSetName[0]+" AND 1 "+color2+" "+comboSetName[1];
			}
			eachRow.put(33, keyFeatures.toUpperCase());
			completeListings.add(eachRow);
			this.comboSkuStart++;
		}
		return completeListings;
	}

	private boolean createCombo(String image1, String image2, String finalImage, boolean addPlusImage){
		boolean success = false;
		BufferedImage bufferredImage1,bufferredImage2,tempImage,plusImage;
		Graphics2D g2;
		try{
			plusImage = ImageIO.read(new File("D:/Mobile Cases/###combo_plus_image/p2.jpg"));
			bufferredImage1 = ImageIO.read(new File(image1));
			int firstImageWidth = bufferredImage1.getWidth();
			int[] minMaxY1 = this.getMinMaxY(bufferredImage1);
			int[] minMaxX1 = this.getMinMaxX(bufferredImage1);
			bufferredImage1 = bufferredImage1.getSubimage(minMaxX1[0],minMaxY1[0],minMaxX1[1]-minMaxX1[0],minMaxY1[1]-minMaxY1[0]);
			int firstImageHeight = minMaxY1[1]-minMaxY1[0];
//			System.out.println("First Image Height : "+firstImageHeight);
			
			bufferredImage2 = ImageIO.read(new File(image2));
			int secondImageWidth = bufferredImage2.getWidth();
			int[] minMaxY2 = this.getMinMaxY(bufferredImage2);
			int[] minMaxX2 = this.getMinMaxX(bufferredImage2);
			bufferredImage2 = bufferredImage2.getSubimage(minMaxX2[0],minMaxY2[0],minMaxX2[1]-minMaxX2[0],minMaxY2[1]-minMaxY2[0]);
			int secondImageHeight = minMaxY2[1]-minMaxY2[0];
//			System.out.println("Second Image Height : "+secondImageHeight);
			
			int offset = 100;
			int estimatedWidth = firstImageWidth+secondImageWidth+offset;
			int estimatedHeight = firstImageHeight > secondImageHeight ? firstImageHeight+offset : secondImageHeight+offset;
			
			//create a blank image with the required dimensions
			tempImage = new BufferedImage(addPlusImage ? estimatedWidth+144 : estimatedWidth, estimatedHeight, BufferedImage.TYPE_INT_RGB);
			
			g2 = tempImage.createGraphics();
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, tempImage.getWidth(), tempImage.getHeight());
			// draw other things on g
			g2.drawImage(bufferredImage1, null, offset/2, offset/2);
			if(addPlusImage){
				g2.drawImage(plusImage, null, bufferredImage1.getWidth()+(offset/2), offset/2);
			}
			g2.drawImage(bufferredImage2, null, addPlusImage ? bufferredImage1.getWidth()+(offset/2)+144 : bufferredImage1.getWidth()+(offset/2), offset/2);
			g2.dispose();
			
			File comboImage = new File(finalImage);
			comboImage.createNewFile();
			
			success = ImageIO.write(tempImage, "jpg", comboImage);
			
		} catch (Exception e) {
//			e.printStackTrace();
			return false;
		} finally {
			tempImage = null;
			bufferredImage2 = null;
			bufferredImage1 = null;
			g2 = null;
		}
        return success;
	}

	private void resizeImagesForOnePhone(String mobileFolder) throws IOException {
		File folder = new File(mobileFolder);
		if(folder != null){
			File[] files = folder.listFiles();
			if(files != null && files.length > 0){
				for(File file : files){
					if(file.isDirectory() && !file.getName().contains("#") && !file.getName().contains("Catalog") && !file.getName().contains("Combo")){
						resizeImagesForOnePhone(file.getAbsolutePath());
					}else if(!file.isDirectory()) {
						resizeImageForFlipkart(file.getAbsolutePath());
					}
				}
			}
		}
	}
	
	private void resizeImageForFlipkart(String image1) throws IOException {
		// TODO Auto-generated method stub
		BufferedImage bufferredImage1 = ImageIO.read(new File(image1));
		if(bufferredImage1 == null){
			return;
		}
		int origWidth = bufferredImage1.getWidth();
		int origHeight = bufferredImage1.getHeight();
		if(origWidth > 500 && origHeight > 500){
//			System.out.println("skipping : "+image1);
			return;
		}
		int[] minMaxY1 = getMinMaxY(bufferredImage1);
		int[] minMaxX1 = getMinMaxX(bufferredImage1);
		
		
		int wDiff = 0;
		int hDiff = 0;
		
		
//		System.out.println("Resizing : "+image1);
		if(origWidth <= 500){
			wDiff = 550 - origWidth;
			origWidth = 550;
		}
		if(origHeight <= 500){
			hDiff = 550 - origHeight;
			origHeight = 550;
		}
		
		int x1 = minMaxX1[0];
		int y1 = minMaxY1[0];
		int width = (minMaxX1[1]-minMaxX1[0]);
		int height = (minMaxY1[1]-minMaxY1[0]);
		
//		System.out.println(bufferredImage1.getWidth()+"-"+bufferredImage1.getHeight()+"-"+x1+"-"+y1+"-"+width+"-"+height);
		bufferredImage1 = bufferredImage1.getSubimage(x1,y1,width,height);

		//create a blank image with the required dimensions
		BufferedImage tempImage = new BufferedImage(origWidth, origHeight, BufferedImage.TYPE_INT_RGB);
		
		x1 = wDiff != 0 ? wDiff/2: x1;
		y1 = hDiff != 0 ? hDiff/2: y1;
		Graphics2D g2 = tempImage.createGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, tempImage.getWidth(), tempImage.getHeight());
		g2.drawImage(bufferredImage1, null, x1, y1);
		g2.dispose();
		
		ImageIO.write(tempImage, "jpg", new File(image1));
	}
	
	private int[] getMinMaxY(BufferedImage image) {
		int minY = image.getHeight();
		int maxY = 0;
		int color = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
            	color = image.getRGB(x, y);
            	if((color & 0x00ff0000) != 16711680){
            		if(minY > y){
            			minY = y;
            		}
            		if(maxY < y){
            			maxY = y;
            		}
            	}
            }
        }
        int[] minMaxY = {minY,maxY};
		return minMaxY;
	}
	
	private int[] getMinMaxX(BufferedImage image) {
		int minX = image.getWidth();
		int maxX = 0;
		int color = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
            	color = image.getRGB(x, y);
            	if((color & 0x00ff0000) != 16711680){
            		if(minX > x){
            			minX = x;
            		}
            		if(maxX < x){
            			maxX = x;
            		}
            	}
            }
        }
        int[] minMaxY = {minX,maxX};
		return minMaxY;
	}

	private int getLastSkuCount() throws FileNotFoundException, IOException {
		try(BufferedReader br = new BufferedReader(new FileReader(SKU_COUNT_PROPERTIES))) {
		    String line = br.readLine();
		    if(line != null && line.contains("skuCount=")){
		    	line = line.replace("skuCount=", "");
		    	if(!line.isEmpty() && StringUtils.isNumeric(line)){
//		    		System.out.println("Last SKU created was : "+line);
		    		return new Integer(line).intValue() + 1;
		    	}
		    }
		}
		return -1;
	}

	private void updateComboSkuCount() throws IOException {
		// TODO Auto-generated method stub
//		System.out.println("Updating the Combo SKU count"+comboSkuStart);
		FileUtils.writeStringToFile(new File(COMBO_COUNT_PROPERTIES), "comboSkuCount="+this.comboSkuStart);
	}
	
	private void extractCombosCasesNCovers(String mobileForlderNames) throws Exception{
		List<String> amrListings = extractCombosCatalogForOtherSellers("AMR_MOZETTE",mobileForlderNames);
//		List<String> amrXolda = extractCombosCatalogForOtherSellers("AMR_XOLDA",mobileForlderNames);
//		amrListings.addAll(amrXolda);
		
		List<String> tramListings = extractCombosCatalogForOtherSellers("TRAM",mobileForlderNames);
		
		List<String> marListings = extractCombosCatalogForOtherSellers("MAR",mobileForlderNames);
		//Commenting this out as huge listings is getting uploaded
//		List<String> marMozette = extractCombosCatalogForOtherSellers("MAR_Mozette",mobileForlderNames);
//		marListings.addAll(marMozette);
		
		if(amrListings.size() > 0)
			writeToFile(amrListings, "AMR", "cases_covers",0);

		System.out.println("AMR Combos Created : "+amrListings.size());
		if(tramListings.size() > 0)
			writeToFile(tramListings, "TRAM", "cases_covers",0);

		System.out.println("TRAM Combos Created : "+tramListings.size());
		if(marListings.size() > 0)
			writeToFile(marListings, "MAR", "cases_covers",0);
		
		System.out.println("MAR Combos Created : "+marListings.size());
		//Create SKU vs Phone Mapping
		//AMR
		System.out.println("Loading SkuvsPhone Mappings");
		List<String> amrSkuVsPhoneMapping = new ArrayList<>();
		loadSkuVsPhoneMapping(amrSkuVsPhoneMapping, amrListings,"cases_covers");
		writeToFileMapping(amrSkuVsPhoneMapping, "AMR");
		
		//TRAM
		List<String> tramSkuVsPhoneMapping = new ArrayList<>();
		loadSkuVsPhoneMapping(tramSkuVsPhoneMapping, tramListings,"cases_covers");
		writeToFileMapping(tramSkuVsPhoneMapping, "TRAM");
		
		//MAR
		List<String> marSkuVsPhoneMapping = new ArrayList<>();
		loadSkuVsPhoneMapping(marSkuVsPhoneMapping, marListings,"cases_covers");
		writeToFileMapping(marSkuVsPhoneMapping, "MAR");
	}
	
	private void extractCombosMACombos(String mobileForlderNames) throws Exception{
		List<String> amrMAListings = createMobileAccessoriesCombo("AMR_MOZETTE",mobileForlderNames);
//		amrMAListings.addAll(amrMAXolda);
		
		List<String> tramMAListings = createMobileAccessoriesCombo("TRAM",mobileForlderNames);
		
		List<String> marMAListings = createMobileAccessoriesCombo("MAR",mobileForlderNames);
//		List<String> marMAMozette = createMobileAccessoriesCombo("MAR_Mozette",mobileForlderNames);
//		marMAListings.addAll(marMAMozette);
		
		if(amrMAListings.size() > 0)
			writeToFile(amrMAListings, "AMR", "mobileaccessories_combo",0);

		System.out.println("AMR Mobile Accessories Combos Created : "+amrMAListings.size());
		if(tramMAListings.size() > 0)
			writeToFile(tramMAListings, "TRAM", "mobileaccessories_combo",0);

		System.out.println("TRAM Mobile Accessories Combos Created : "+tramMAListings.size());
		if(marMAListings.size() > 0)
			writeToFile(marMAListings, "MAR", "mobileaccessories_combo",0);

		System.out.println("MAR Mobile Accessories Combos Created : "+marMAListings.size());
		//Create SKU vs Phone Mapping
		//AMR
		System.out.println("Loading SkuvsPhone Mappings");
		List<String> amrMASkuVsPhoneMapping = new ArrayList<>();
		loadSkuVsPhoneMapping(amrMASkuVsPhoneMapping, amrMAListings,"mACombo");
		writeToFileMapping(amrMASkuVsPhoneMapping, "AMR");
		
		//TRAM
		List<String> tramMASkuVsPhoneMapping = new ArrayList<>();
		loadSkuVsPhoneMapping(tramMASkuVsPhoneMapping, tramMAListings,"mACombo");
		writeToFileMapping(tramMASkuVsPhoneMapping, "TRAM");
		
		//MAR
		List<String> marMASkuVsPhoneMapping = new ArrayList<>();
		loadSkuVsPhoneMapping(marMASkuVsPhoneMapping, marMAListings,"mACombo");
		writeToFileMapping(marMASkuVsPhoneMapping, "MAR");
		
		//Unique Mobile Names
		List<String> mobileNames = new ArrayList<>();
		for(String echFoilder : mobileForlderNames.split(",")){
			File file = new File(echFoilder);
			mobileNames.add(file.getName());
		}
		writeToFile(mobileNames, "AMR", "UniqueMobileNames",0);
		writeToFile(mobileNames, "TRAM", "UniqueMobileNames",0);
		writeToFile(mobileNames, "MAR", "UniqueMobileNames",0);
	}

	public void extractListingsForAllSellers(String mobileForlderNames) throws Exception {
		// TODO Auto-generated method stub
		if(this.isCoversCombo){
			extractCombosCasesNCovers(mobileForlderNames);
		}else if(this.isMACombo){
			extractCombosMACombos(mobileForlderNames);
		}
		
		//Archive completed Folders
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
	    Date date = new Date();  
	    String dateString = formatter.format(date);
		for(String eachPhone : mobileForlderNames.split(",")){
			String newName = eachPhone + "/Combos/#Catalog_"+dateString;
			eachPhone += "/Combos/Catalog";
			File catFolder = new File(eachPhone);
			if(catFolder.exists()){
				catFolder.renameTo(new File(newName));
			}
		}
		
		//Unique Mobile Names
		List<String> mobileNames = new ArrayList<>();
		for(String echFoilder : mobileForlderNames.split(",")){
			File file = new File(echFoilder);
			mobileNames.add(file.getName());
		}
		writeToFile(mobileNames, "AMR", "UniqueMobileNames",0);
		writeToFile(mobileNames, "TRAM", "UniqueMobileNames",0);
		writeToFile(mobileNames, "MAR", "UniqueMobileNames",0);
		
		
		String mailText = "";
		if(listingInfoList.size() > 0){
			for(String eachText : listingInfoList){
				mailText += eachText+"\n";
			}

			configFilePaths = new ArrayList<>();
			File zcore = new File("./code/zcore");
			if(zcore != null && zcore.exists() && zcore.isDirectory() && zcore.listFiles().length > 0){
				for(File eachFile : zcore.listFiles()){
					configFilePaths.add("./code/zcore/"+eachFile.getName());
				}
			}
			configFilePaths.add("./code/config.properties");
			String subject = this.isCoversCombo ? "CasesNCovers Combo Listings" : "Mobile Accessories Combo Listings";
			emailUtils.sendEmail(subject,"","",mailText,configFilePaths,"abdul.mudassir5086@gmail.com");
		}
		String mailBody = mailText.equals("") ? "PFA" : mailText;
		String subject = this.isCoversCombo ? "CasesNCovers Combo Listings" : "Mobile Accessories Combo Listings";
		emailUtils.sendEmail(subject,"","",mailBody,filePaths,"shezan.listings@gmail.com");
		System.out.println("SUCCESS !!!");
	}

	private static void loadSkuVsPhoneMapping(List<String> skuVsPhoneMapping,
			List<String> listings, String caseType) {
		// TODO Auto-generated method stub
		
		for(String eachLine : listings){
			String[] tokens = eachLine.split("\t");
			if(caseType.equals("cases_covers")){
				skuVsPhoneMapping.add(tokens[0]+"::"+tokens[5]);
			}else if(caseType.equals("screen_guards")){
				skuVsPhoneMapping.add(tokens[0]+"::"+tokens[4]);
			}else if(caseType.equals("mACombo")){
				skuVsPhoneMapping.add(tokens[0]+"::"+tokens[6]);
			}
		}
		
		
	}

	private void writeToFileMapping(List<String> list, String sellerName) throws IOException {
		// TODO Auto-generated method stub
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");  
	    Date date = new Date();  
	    String dateString = formatter.format(date);
		String fileName = sellerName+"_"+"skuvsPhone_Mapping"+"_"+dateString+"_listings";//AMR_Cases_Covers_listings.txt
		File fout = new File("./"+dateFolderString+"/"+fileName+".txt");
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
		filePaths.add(fout.getAbsolutePath());
	}

	private void writeToFile(List<String> list, String sellerName, String type, int dayPlus) throws IOException {
		// TODO Auto-generated method stub
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss"); 
		SimpleDateFormat dateFolderformatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");  
	    Date date = new Date();  
	    
	    Calendar c = Calendar.getInstance(); 
		c.setTime(date); 
		c.add(Calendar.DATE, dayPlus);
		date = c.getTime();
		
	    String dateString = formatter.format(date);
	    if("".equals(dateFolderString)){
	    	dateFolderString = dateFolderformatter.format(date);
	    }
	    if(dayPlus > 0){
	    	dateFolderString+= "_Pending";
	    }
		String fileName = sellerName+"_"+type+"_"+dateString+"_listings";//AMR_Cases_Covers_listings.txt
		File fout = new File("./"+dateFolderString+"/"+fileName+".txt");
		if(fout != null && !fout.getParentFile().exists()){
			FileUtils.forceMkdir(fout.getParentFile());
		}
		if(!fout.exists()){
			fout.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	 
		for(int i=0; i< list.size(); i++){
			if(i != 0 && i % 2000 == 0){//create a new file for nextDay
				list = list.subList(i, list.size());
				writeToFile(list, sellerName, type, ++dayPlus);
				break;
			}
	    	bw.write(list.get(i));
			bw.newLine();
	    }
		bw.close();
		filePaths.add(fout.getAbsolutePath());
	}

	public Map<String, String> getComboCaseTypes() {
		return comboCaseTypes;
	}

	public void setComboCaseTypes(Map<String, String> comboCaseTypes) {
		this.comboCaseTypes = comboCaseTypes;
	}

	public Map<String, String> getKeywordsMap() {
		return keywordsMap;
	}

	public void setKeywordsMap(Map<String, String> keywordsMap) {
		this.keywordsMap = keywordsMap;
	}

	public Map<String, String> getComboSKUMap() {
		return comboSKUMap;
	}

	public void setComboSKUMap(Map<String, String> comboSKUMap) {
		this.comboSKUMap = comboSKUMap;
	}

	public void setColorMap(Map<String, String> colorMap) {
		this.colorMap = colorMap;
	}

	public Map<String, String> getColorMap() {
		return colorMap;
	}



	public boolean isPlusImage() {
		return plusImage;
	}



	public void setPlusImage(boolean plusImage) {
		this.plusImage = plusImage;
	}



	public int getCombosLimit() {
		return combosLimit;
	}



	public void setCombosLimit(int combosLimit) {
		this.combosLimit = combosLimit;
	}



	public int getmACombosLimit() {
		return mACombosLimit;
	}



	public void setmACombosLimit(int mACombosLimit) {
		this.mACombosLimit = mACombosLimit;
	}



	public boolean isCoversCombo() {
		return isCoversCombo;
	}



	public void setCoversCombo(boolean isCoversCombo) {
		this.isCoversCombo = isCoversCombo;
	}



	public boolean isMACombo() {
		return isMACombo;
	}



	public void setMACombo(boolean isMACombo) {
		this.isMACombo = isMACombo;
	}

	public Map<String, String> getCaseTypesMap() {
		return caseTypesMap;
	}

	public void setCaseTypesMap(Map<String, String> caseTypesMap) {
		this.caseTypesMap = caseTypesMap;
	}
}
