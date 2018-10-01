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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

public class CreateSingleListings {
	
	private Map<String, String> keywordsMap;
	private String[] brandColors;
	private Map<String, String> caseTypesMap;
	private Map<String, String> colorMap;
	private Map<String, String> skuStringMap;
	private int skuStart;
	private Map<String, String> modelMap;
	private Map<String, String> descriptionMap;
	private ConfigurationLoader configurationLoader;
	private String imageShackUsername;
	private String imageShackPassword;
	private String imageShackAPIKey;
	private Properties iSProp;
	private ImageShack imageShack;
	private int temperedSkuStart;
	List<String> filePaths;
	List<String> configFilePaths;
	List<String> listingInfoList;
	private EmailUtils emailUtils;
	private String dateFolderString = "";
	
	public CreateSingleListings() throws Exception {
		configurationLoader = new ConfigurationLoader();
		keywordsMap = configurationLoader.loadKeywords();
		caseTypesMap = configurationLoader.loadCaseTypes();
		colorMap = configurationLoader.loadColorMap();
		modelMap = configurationLoader.loadModelMap();
		skuStringMap = configurationLoader.loadSkuMap();
		descriptionMap = configurationLoader.loadDescriptionMap();
		brandColors = configurationLoader.loadbrandColors();
		iSProp = configurationLoader.loadImageShackProperties();
		imageShackUsername = iSProp.getProperty("username");
		imageShackPassword = iSProp.getProperty("password");
		imageShackAPIKey = iSProp.getProperty("apikey");
		this.skuStart= configurationLoader.getLastSkuCount();
		imageShack = new ImageShack(this.imageShackAPIKey, this.imageShackUsername, this.imageShackPassword);
		this.temperedSkuStart = configurationLoader.getLastTemperedSkuCount();
		filePaths = new ArrayList<>();
		listingInfoList = new ArrayList<>();
		emailUtils = new EmailUtils();
	}

	public void createFlipkartListingsForManyPhone(String[] multiplePhones) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		if(multiplePhones != null && multiplePhones.length > 0){
			for(String eachPhone : multiplePhones){
				createFlipkartListingsForOnePhone(eachPhone);
			}
		}
		System.out.println("Update the SKU in properties file : "+ this.skuStart+1);
	}

	private void createFlipkartListingsForOnePhone(String mobileFolder) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		//resize all images
		resizeImagesForOnePhone(mobileFolder);
		
		String listingsInfo = "";
		File parentFolder = new File(mobileFolder);
		String mobileName = parentFolder.getName();
		System.out.println("Creating Listings for Mobile : "+mobileName);
		File[] varieties = parentFolder.listFiles();
		if(varieties != null && varieties.length > 0){
			for(File eachType : varieties){
				if(eachType.getName().contains("Combo") || eachType.getName().contains("Glass")  || eachType.getName().contains("Screen Guard")
						|| eachType.getName().contains("Tempered") || eachType.getName().contains("Catalog") 
						|| eachType.getName().contains("#")){
					continue;
				}
				System.out.println("Starting to create listings of type : "+eachType.getName());
				int tempSkuCount = this.skuStart;
				createListngs(mobileName,eachType,mobileFolder);
				int finalSkuCount = this.skuStart;
				listingsInfo += eachType.getName()+"-"+(finalSkuCount-tempSkuCount)+"\t";
			}
		}
		
		File cat = new File(parentFolder.getAbsolutePath()+"/Catalog");
		File back = new File(parentFolder.getAbsolutePath()+"/###backup");
		
		if(!cat.exists()){
			FileUtils.forceMkdir(cat);
		}
		if(!back.exists()){
			FileUtils.forceMkdir(back);
		}
		
		FileUtils.copyDirectory(cat, back);

		listingInfoList.add("Mobile Name : "+mobileName+" Models Created : "+listingsInfo);
		//update the sku count in skuCount.txt
//		updateSkuCount();
		configurationLoader.updateSkuCount(this.skuStart);
	}
	
	private void createListngs(String mobileName, File eachType, String mobileFolder) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		
		String keywords = getKeywordsMap().get(eachType.getName());
		String brandColors[] = getBrandColors();
		String caseTypes = getCaseTypesMap().get(eachType.getName());
		
		File[] imageList = eachType.listFiles();
		if(imageList != null && imageList.length > 0){
			List<String> imageUrls = imageShack.processImageUploadNew(eachType.getAbsolutePath(), mobileName);
			List<String> commonImageUrls = imageShack.processImageUploadNew(eachType.getAbsolutePath()+"/common", mobileName);
			
			if(imageUrls != null && imageUrls.size() > 0 && caseTypes != null && caseTypes.length() > 0){
				for(String caseType : caseTypes.split(",")){
					String[] caseTypeArr = caseType.split(":");
					if(caseTypeArr != null && caseTypeArr.length > 0){
						List<Map<Integer, String>> completedListings = popuateEachRow(imageUrls,caseTypeArr[0],mobileName,commonImageUrls,keywords,brandColors,false,eachType);
						writeToFile(completedListings,caseTypeArr[0],mobileFolder,eachType.getName(),mobileName,false);

						if(caseTypeArr.length > 1 && caseTypeArr[1].equals("Y")){
							completedListings = popuateEachRow(imageUrls,caseTypeArr[0],mobileName,commonImageUrls,keywords,brandColors,true,eachType);
							writeToFile(completedListings,caseTypeArr[0],mobileFolder,eachType.getName(),mobileName,true);
						}
					}
				}
			}
		}

	}

	public void createTemperedGlassListings(String mobileFolders) throws IOException {
		// TODO Auto-generated method stub
		String folders[] = mobileFolders.split(",");
		for(String eachFolder : folders){
			String listingsInfo = "";
			File mobile = new File(eachFolder);
			String mobileName = mobile.getName();
			
			File[] varieties = mobile.listFiles();
			if(varieties != null && varieties.length > 0){
				for(File eachType : varieties){
					if((eachType.getName().contains("Glass")  || eachType.getName().contains("Screen Guard")
							|| eachType.getName().contains("Tempered") )
							&& (!eachType.getName().contains("Combo") && !eachType.getName().contains("Catalog") && !eachType.getName().contains("#")) ){
						resizeImagesForOnePhone(eachType.getAbsolutePath());
						int tempSkuCount = this.temperedSkuStart;
						createTemperedGlassListngs(mobileName,eachType,eachFolder);
						int finalSkuCount = this.temperedSkuStart;
						File abc = new File(eachFolder+"/###backup/"+eachType.getName());
						FileUtils.forceMkdir(abc);
						File catFile = new File(eachFolder+"/Catalog/"+eachType.getName());
						FileUtils.copyDirectory(catFile, abc);
						listingsInfo += eachType.getName()+"-"+(finalSkuCount-tempSkuCount)+"\t";
					}
				}
			}
			listingInfoList.add("Mobile Name : "+mobileName+" Models Created : "+listingsInfo);
		}

//		updateTemperedSkuCount();
		configurationLoader.updateTemperedSkuCount(temperedSkuStart);
	}

	private void createTemperedGlassListngs(String mobileName, File eachType, String mobileFolder) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		
		String keywords = getKeywordsMap().get(eachType.getName());
		String caseTypes = getCaseTypesMap().get(eachType.getName());
		
		File[] imageList = eachType.listFiles();
		if(imageList != null && imageList.length > 0){
			
			//create image combinations
//			createComboImagesForTemperedGlasses(eachType);
			
			List<String> imageUrls = imageShack.processImageUploadNew(eachType.getAbsolutePath(), mobileName);
			List<String> commonImageUrls = imageShack.processImageUploadNew(eachType.getAbsolutePath()+"/common", mobileName);
			
			if(imageUrls != null && imageUrls.size() > 0 && caseTypes != null && caseTypes.length() > 0){
				for(String caseType : caseTypes.split(",")){
					String[] caseTypeArr = caseType.split(":");
					if(caseTypeArr != null && caseTypeArr.length > 0){
						List<Map<Integer, String>> completedListings = popuateEachTemperedGlassRow(imageUrls,caseTypeArr[0],mobileName,commonImageUrls,keywords,brandColors,false,eachType);
						writeToFile(completedListings,caseTypeArr[0],mobileFolder,eachType.getName(),mobileName,false);

//						if(caseTypeArr.length > 1 && caseTypeArr[1].equals("Y")){
//							completedListings = popuateEachTemperedGlassRow(imageUrls,caseTypeArr[0],mobileName,commonImageUrls,keywords,brandColors,true,eachType);
//							writeToFile(completedListings,caseTypeArr[0],mobileFolder,eachType.getName(),mobileName,true);
//						}
					}
				}
			}
		}

	}
	
	private List<Map<Integer, String>> popuateEachTemperedGlassRow(List<String> imageUrls, String caseType, String mobileName, List<String> commonImageUrls,
			String keywords, String[] brandColors, boolean diffBrandColors, File eachType) {
		// TODO Auto-generated method stub
		List<Map<Integer, String>> completeListings = new ArrayList<>();
		int index = 0;
		for(String imageUrl : imageUrls){
			Map<Integer, String> eachRow = new HashMap<>();
			this.temperedSkuStart++;
			String color = this.getColorMap().get(eachType.getName());
			eachRow.put(6, this.getSkuStringMap().get(eachType.getName())+""+this.temperedSkuStart);
			eachRow.put(7, "Mozette");
			eachRow.put(8, this.getModelMap().get(eachType.getName())+""+this.temperedSkuStart);
			eachRow.put(9, color);
			eachRow.put(10, mobileName);
			eachRow.put(11, caseType);
			eachRow.put(12, "Scratch Resistant::UV Protection::Anti Glare");
			eachRow.put(13, "1 "+caseType);
			eachRow.put(14, "Mobile");
			
			eachRow.put(15, imageUrl);
			if(commonImageUrls != null && commonImageUrls.size() > 0){
				int imageIndex = 16;
				for(String commonImages : commonImageUrls){
					eachRow.put(imageIndex++, commonImages);
				}
			}
			
			String description = getDescriptionMap().get(eachType.getName());
			if(!StringUtils.isBlank(description)){
				description = description.replaceAll("<mobilename>", mobileName.toUpperCase());
				eachRow.put(22, description);
			}
			
			String keywords1 = keywords.replace("mobilename", mobileName.toLowerCase());
			if(keywords1.length() >= 1000){
				keywords1 = keywords1.substring(0,999);
				keywords1 = keywords1.substring(0,keywords1.lastIndexOf(":"));
			}
			
			eachRow.put(23,keywords1);
			eachRow.put(24, "Fully Protective::More Reliable");
			eachRow.put(28, "Yes");
			eachRow.put(30, "1");
			completeListings.add(eachRow);
		}
		return completeListings;
	}
	
	private static void writeToFile(List<Map<Integer, String>> completeListings, String caseType,String mobileFolder, String folderName, String mobileName, boolean diffBrandColors) throws IOException {
		
		String filePath = mobileFolder+"/Catalog/"+folderName;
		String fileName = "";
		if(diffBrandColors){
			fileName = filePath+"/"+caseType+"_"+"Differnet"+" Brand Color.txt";
		}else{
			fileName = filePath+"/"+caseType+".txt";
		}
		
		File file = new File(fileName);
		if(!file.getParentFile().exists()){
			FileUtils.forceMkdir(file.getParentFile());
		}
		file.getParentFile().mkdirs();
		try{
		    PrintWriter writer = new PrintWriter(file);
		    
			if(completeListings.size() > 0){
				for(Map<Integer, String> row : completeListings){
					for(int cellPointer=6; cellPointer < 31; cellPointer++) {
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
	
	private List<Map<Integer, String>> popuateEachRow(List<String> imageUrls, String caseType, String mobileName, List<String> commonImageUrls,
			String keywords, String[] brandColors, boolean diffBrandColors, File eachType) {
		// TODO Auto-generated method stub
		List<Map<Integer, String>> completeListings = new ArrayList<>();
		int index = 0;
		for(String imageUrl : imageUrls){
			Map<Integer, String> eachRow = new HashMap<>();
			this.skuStart++;
			String color = this.getColorMap().get(eachType.getName());
			eachRow.put(6, this.getSkuStringMap().get(eachType.getName())+""+this.skuStart);
			eachRow.put(7, "Mozette");
			eachRow.put(8, this.getModelMap().get(eachType.getName())+""+this.skuStart);
			eachRow.put(9, caseType);
			eachRow.put(10, color);
			eachRow.put(11, mobileName);
			eachRow.put(12, "Rubber::Plastic");
			eachRow.put(13, "Mobile");
			eachRow.put(14, caseType);
			eachRow.put(15, "No Theme");
			if(diffBrandColors){
				if(index < brandColors.length){
					eachRow.put(16, brandColors[index]+" "+color);
					index++;
				}else{
					index = 0;
					eachRow.put(16, brandColors[index]+" "+color);
				}
			}else{
				eachRow.put(16, color);
			}
			eachRow.put(17, imageUrl);
			if(commonImageUrls != null && commonImageUrls.size() > 0){
				int imageIndex = 18;
				for(String commonImages : commonImageUrls){
					eachRow.put(imageIndex++, commonImages);
				}
			}
			eachRow.put(25, "1");
			String description = getDescriptionMap().get(eachType.getName());
			if(!StringUtils.isBlank(description)){
				description = description.replaceAll("<mobilename>", mobileName.toLowerCase());
				eachRow.put(26, description);
			}
			String keywords1 = keywords.replace("mobilename", mobileName.toLowerCase());
			if(keywords1.length() >= 1000){
				keywords1 = keywords1.substring(0,999);
				keywords1 = keywords1.substring(0,keywords1.lastIndexOf(":"));
			}
//			eachRow.put(27,keywords1);
			eachRow.put(27, "Fully Protective");
			eachRow.put(33, "Reliable, Durable, Shock-proof, Drop-protection, Luxurious, Slim-design");
			completeListings.add(eachRow);
		}
		return completeListings;
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
		int[] minMaxY1 = getMinMaxY(bufferredImage1);
		int[] minMaxX1 = getMinMaxX(bufferredImage1);
		
		int origWidth = bufferredImage1.getWidth();
		int origHeight = bufferredImage1.getHeight();
		
		int wDiff = 0;
		int hDiff = 0;
		
		if(origWidth > 500 && origHeight > 500){
//			System.out.println("skipping : "+image1);
			return;
		}
		
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

	private List<String> extractSpecificCatalog(String mobiles, String types) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		List<String> finalListings = new ArrayList<>();
		String[] mobilesF = mobiles.split(",");
		String[] typesF = types.split(",");
		for(String each : mobilesF){
			File eachMobile = new File(each);
			File catalog = new File(eachMobile.getAbsolutePath()+"/Catalog");
			if(catalog != null && catalog.exists() && catalog.isDirectory()){
				File[] types1 = catalog.listFiles();
				for(File eachType : types1){
					if(eachType.exists() && eachType.isDirectory()){
						File[] catFiles = eachType.listFiles();
						if(catFiles != null && catFiles.length > 0){
							for(File eachCatFile : catFiles){
//								System.out.println(eachCatFile.getName());
								for(String types2 : typesF){
									if(types2.trim().equals(eachCatFile.getName().trim().replace(".txt",""))){
										if(!eachCatFile.exists()){
											continue;
										}
										try (BufferedReader br = new BufferedReader(new FileReader(eachCatFile))) {
										    String line;
										    while ((line = br.readLine()) != null) {
//										    	System.out.println(line);
										    	finalListings.add(line);
										    }
										}
										//move file to completed folder
										FileUtils.copyFile(eachCatFile, new File(eachCatFile.getAbsolutePath().replace("Catalog", "###Completed")));
									}
								}
							}
						}
					}
				}
			}
		}
		return finalListings;
	}

	private List<String> extractSpecificCatalogForOtherSellers(String sellerName, String mobiles, String types) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		List<String> listings = new ArrayList<>();
		String[] mobilesF = mobiles.split(",");
		String[] typesF = types.split(",");
		for(String each : mobilesF){
			File eachMobile = new File(each);
			File catalog = new File(eachMobile.getAbsolutePath()+"/###backup");
			if(catalog != null && catalog.exists() && catalog.isDirectory()){
				File[] types1 = catalog.listFiles();
				for(File eachType : types1){
					if(eachType.exists() && eachType.isDirectory()){
						File[] catFiles = eachType.listFiles();
						if(catFiles != null && catFiles.length > 0){
							for(File eachCatFile : catFiles){
//								System.out.println(eachCatFile.getName());
								for(String types2 : typesF){
									if(types2.trim().equals(eachCatFile.getName().trim().replace(".txt",""))){
										if(!eachCatFile.exists()){
											continue;
										}
										try (BufferedReader br = new BufferedReader(new FileReader(eachCatFile))) {
										    String line;
										    while ((line = br.readLine()) != null) {
										    	if(sellerName.equals("MAR")){
										    		line = line.replaceAll("mzt-", "mot-");
										    		line = line.replaceAll("Mozette\tM-", "Motaz\tMt-");
										    		line = line.replaceAll("mozette", "motaz");
										    		line = line.replaceAll("Mozette", "Motaz");
										    	}if(sellerName.equals("MAR_Mozette")){
										    		line = line.replaceAll("mzt-", "mzm-");
										    		line = line.replaceAll("Mozette\tM-", "Mozette\tMt-");
										    	}if(sellerName.equals("MAR_XOLDA")){
										    		line = line.replaceAll("mzt-", "xom-");
										    		line = line.replaceAll("Mozette\tM-", "XOLDA\tXm-");
										    		line = line.replaceAll("mozette", "xolda");
										    		line = line.replaceAll("Mozette", "XOLDA");
										    	}else if(sellerName.equals("AMR")){
										    		line = line.replaceAll("mzt-", "xol-");
										    		line = line.replaceAll("Mozette\tM-", "XOLDA\tX-");
										    		line = line.replaceAll("mozette", "xolda");
										    		line = line.replaceAll("Mozette", "XOLDA");
										    	}else if(sellerName.equals("TRAM")){
										    		line = line.replaceAll("mzt-", "zot-");
										    		line = line.replaceAll("Mozette\tM-", "ZOTIKOS\tZt-");
										    		line = line.replaceAll("mozette", "zotikos");
										    		line = line.replaceAll("Mozette", "ZOTIKOS");
										    	}
//										    	System.out.println(line);
										    	listings.add(line);
										    }
										}
										//move file to completed folder
										FileUtils.copyFile(eachCatFile, new File(eachCatFile.getAbsolutePath().replace("###backup", "###Completed_"+sellerName)));
									}
								}
							}
						}
					}
				}
			}else{
				System.out.println("\n\n\n\n\n\n###backup does not exists for : "+each);
			}
		}
		return listings;
	}

	public void extractListingsForAllSellers(String mobileForlderNames) throws Exception {
		System.out.println("Extracting Catalog Files for All Sellers");
		List<String> amrSingleListingsCasesNCovers = extractSpecificCatalog(mobileForlderNames, "Back Cover,Back Replacement Cover_Differnet Brand Color,Bumper Case_Differnet Brand Color,Flip Cover,Front & Back Case_Differnet Brand Color,Book Cover_Differnet Brand Color");
		List<String> amrTemperedGlass = extractSpecificCatalog(mobileForlderNames, "Tempered Glass,Screen Guard,Nano Glass");
		
		List<String> amrXoldaSingleListingsCasesNCovers = extractSpecificCatalogForOtherSellers("AMR", mobileForlderNames, "Back Cover,Back Replacement Cover_Differnet Brand Color,Bumper Case_Differnet Brand Color,Flip Cover,Front & Back Case_Differnet Brand Color,Book Cover_Differnet Brand Color");
		List<String> amrXoldaTemperedGlass = extractSpecificCatalogForOtherSellers("AMR", mobileForlderNames, "Tempered Glass,Screen Guard,Nano Glass");
		
		amrSingleListingsCasesNCovers.addAll(amrXoldaSingleListingsCasesNCovers);
		amrTemperedGlass.addAll(amrXoldaTemperedGlass);
		
		List<String> tramSingleListingsCasesNCovers = extractSpecificCatalogForOtherSellers("TRAM", mobileForlderNames, "Back Cover,Back Replacement Cover_Differnet Brand Color,Bumper Case_Differnet Brand Color,Flip Cover,Front & Back Case_Differnet Brand Color,Book Cover_Differnet Brand Color");
		List<String> tramTemperedGlass = extractSpecificCatalogForOtherSellers("TRAM", mobileForlderNames, "Tempered Glass,Screen Guard,Nano Glass");

		List<String> marSingleListingsCasesNCovers = extractSpecificCatalogForOtherSellers("MAR", mobileForlderNames, "Back Cover,Back Replacement Cover_Differnet Brand Color,Bumper Case_Differnet Brand Color,Flip Cover,Front & Back Case_Differnet Brand Color,Book Cover_Differnet Brand Color");
		List<String> marTemperedGlass = extractSpecificCatalogForOtherSellers("MAR", mobileForlderNames, "Tempered Glass,Screen Guard,Nano Glass");
		
		List<String> marXOLSingleListingsCasesNCovers = extractSpecificCatalogForOtherSellers("MAR_XOLDA", mobileForlderNames, "Back Cover,Back Replacement Cover_Differnet Brand Color,Bumper Case_Differnet Brand Color,Flip Cover,Front & Back Case_Differnet Brand Color,Book Cover_Differnet Brand Color");
		List<String> marXOLTemperedGlass = extractSpecificCatalogForOtherSellers("MAR_XOLDA", mobileForlderNames, "Tempered Glass,Screen Guard,Nano Glass");
		
		List<String> marMozetteSingleListingsCasesNCovers = extractSpecificCatalogForOtherSellers("MAR_Mozette", mobileForlderNames, "Back Cover,Back Replacement Cover_Differnet Brand Color,Bumper Case_Differnet Brand Color,Flip Cover,Front & Back Case_Differnet Brand Color,Book Cover_Differnet Brand Color");
		List<String> marMozetteTemperedGlass = extractSpecificCatalogForOtherSellers("MAR_Mozette", mobileForlderNames, "Tempered Glass,Screen Guard,Nano Glass");
		
		
		marSingleListingsCasesNCovers.addAll(marXOLSingleListingsCasesNCovers);
		marSingleListingsCasesNCovers.addAll(marMozetteSingleListingsCasesNCovers);
		
		marTemperedGlass.addAll(marXOLTemperedGlass);
		marTemperedGlass.addAll(marMozetteTemperedGlass);
		
		
		//Writing Listings to File
		if(amrSingleListingsCasesNCovers.size() > 0)
			writeToFile(amrSingleListingsCasesNCovers, "AMR", "cases_covers",0);
		if(amrTemperedGlass.size() > 0)
			writeToFile(amrTemperedGlass, "AMR", "screen_guards",0);

		if(tramSingleListingsCasesNCovers.size() > 0)
			writeToFile(tramSingleListingsCasesNCovers, "TRAM", "cases_covers",0);
		if(tramTemperedGlass.size() > 0)
			writeToFile(tramTemperedGlass, "TRAM", "screen_guards",0);

		if(marSingleListingsCasesNCovers.size() > 0)
			writeToFile(marSingleListingsCasesNCovers, "MAR", "cases_covers",0);
		if(marTemperedGlass.size() > 0)
			writeToFile(marTemperedGlass, "MAR", "screen_guards",0);
		
		//Create SKU vs Phone Mapping
		//AMR
		System.out.println("Loading SkuvsPhone Mappings");
		List<String> amrSkuVsPhoneMapping = new ArrayList<>();
		loadSkuVsPhoneMapping(amrSkuVsPhoneMapping, amrSingleListingsCasesNCovers,"cases_covers");
		loadSkuVsPhoneMapping(amrSkuVsPhoneMapping, amrTemperedGlass,"screen_guards");
		writeToFileMapping(amrSkuVsPhoneMapping, "AMR");
		
		//TRAM
		List<String> tramSkuVsPhoneMapping = new ArrayList<>();
		loadSkuVsPhoneMapping(tramSkuVsPhoneMapping, tramSingleListingsCasesNCovers,"cases_covers");
		loadSkuVsPhoneMapping(tramSkuVsPhoneMapping, tramTemperedGlass,"screen_guards");
		writeToFileMapping(tramSkuVsPhoneMapping, "TRAM");
		
		//MAR
		List<String> marSkuVsPhoneMapping = new ArrayList<>();
		loadSkuVsPhoneMapping(marSkuVsPhoneMapping, marSingleListingsCasesNCovers,"cases_covers");
		loadSkuVsPhoneMapping(marSkuVsPhoneMapping, marTemperedGlass,"screen_guards");
		writeToFileMapping(marSkuVsPhoneMapping, "MAR");
		
		//Unique Mobile Names
		List<String> mobileNames = new ArrayList<>();
		for(String echFoilder : mobileForlderNames.split(",")){
			File file = new File(echFoilder);
			mobileNames.add(file.getName());
		}
		writeToFile(mobileNames, "AMR", "UniqueMobileNames",0);
		writeToFile(mobileNames, "TRAM", "UniqueMobileNames",0);
		writeToFile(mobileNames, "MAR", "UniqueMobileNames",0);
		
		
		emailUtils.sendEmail("Listings","","","PFA",filePaths,"shezan.listings@gmail.com");
		
		if(listingInfoList.size() > 0){
			String mailText = "";
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
			emailUtils.sendEmail("Listings Status","","",mailText,configFilePaths,"abdul.mudassir5086@gmail.com");
		}
		System.out.println("SUCCESS !!!");
	}

	public Map<String, String> getKeywordsMap() {
		return keywordsMap;
	}

	public void setKeywordsMap(Map<String, String> keywordsMap) {
		this.keywordsMap = keywordsMap;
	}

	public String[] getBrandColors() {
		return brandColors;
	}

	public void setBrandColors(String[] brandColors) {
		this.brandColors = brandColors;
	}

	public Map<String, String> getCaseTypesMap() {
		return caseTypesMap;
	}

	public void setCaseTypesMap(Map<String, String> caseTypesMap) {
		this.caseTypesMap = caseTypesMap;
	}

	public Map<String, String> getColorMap() {
		return colorMap;
	}

	public void setColorMap(Map<String, String> colorMap) {
		this.colorMap = colorMap;
	}

	public Map<String, String> getSkuStringMap() {
		return skuStringMap;
	}

	public void setSkuStringMap(Map<String, String> skuStringMap) {
		this.skuStringMap = skuStringMap;
	}

	public int getSkuStart() {
		return skuStart;
	}

	public void setSkuStart(int skuStart) {
		this.skuStart = skuStart;
	}

	public Map<String, String> getModelMap() {
		return modelMap;
	}

	public void setModelMap(Map<String, String> modelMap) {
		this.modelMap = modelMap;
	}

	public Map<String, String> getDescriptionMap() {
		return descriptionMap;
	}

	public void setDescriptionMap(Map<String, String> descriptionMap) {
		this.descriptionMap = descriptionMap;
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
				skuVsPhoneMapping.add(tokens[0]+"::"+tokens[5]);
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
}
