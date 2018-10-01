package amr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

import test.EmailUtils;

/*
 * Pending - 
 * 
 *1. Full Dynamic Listing Creation
 *	a. put all column indexes in config file so that if flipkart changes the catalog we can just change the config
 *	b. For SkuvsPhoneMapping i should take index of sku and phone from config
 *2. All possible case types should be mentioned in config file. If flipkart adds or removes case types then i should just change config
 *3. brand colors, description, should be configurable
 * */
public class CreateListings {
	

	private Map<String, String> keywordsMap;
	private String[] brandColors;
	private Map<String, String> caseTypesMap;
	private Map<String, String> colorMap;
	private Map<String, String> skuStringMap;
	private int skuStart;
	private Map<String, String> modelMap;
	private Map<String, String> descriptionMap;
	private Properties configProp;
	private int temperedSkuStart;
	private EmailUtils emailUtils;
	List<String> filePaths;
	
	private final static String  SKU_COUNT_PROPERTIES = "./code/skuCount.txt";
	private final static String  COMBO_COUNT_PROPERTIES = "./code/comboCount.txt";
	private final static String  TEMPERED_COUNT_PROPERTIES = "./code/temperedSkuCount.txt";
	
	public CreateListings() throws Exception {
		keywordsMap = loadKeywords();
		caseTypesMap = loadCaseTypes();
		colorMap = loadColorMap();
		modelMap = loadModelMap();
		skuStringMap = loadSkuMap();
		descriptionMap = loadDescriptionMap();
		brandColors = "Shady,Arc,Zync,Shiny,Smooth,Soft,Pure,Crystal,Brash,Ablaze,Dusty,Flamboyant,Gaily,Iridescent,Mellow,Pastel,Sepia,Splashy,Vivid".split(",");
		this.skuStart= getLastSkuCount();
		emailUtils = new EmailUtils();
		configProp = loadProperties();
		this.temperedSkuStart = getLastTemperedSkuCount();
		filePaths = new ArrayList<>();
	}
	
	public static void main(String[] args) throws Exception {
		CreateListings createListings = new CreateListings();
		String mobileForlderNames = (String) createListings.configProp.get("mobilePhoneFolders");
		if(mobileForlderNames == null || "".equals(mobileForlderNames)){
			System.out.println("mobilePhoneFolders is missing in config.properties");
			return;
		}
		File dateFolder = new File(mobileForlderNames);
		System.out.println(mobileForlderNames);
		if(dateFolder == null || !dateFolder.isDirectory()){
			System.out.println("mobilePhoneFolders in config.properties is not a directory or missing");
		}
		mobileForlderNames = "";
		for(File eachMobileFolder : dateFolder.listFiles()){
			mobileForlderNames+= eachMobileFolder+",";
		}
		mobileForlderNames = mobileForlderNames.substring(0, mobileForlderNames.length()-1);
		System.out.println("Creating Listings for "+mobileForlderNames);
		if(args[0].equals("SingleListing")){
			//Creating Single Listings
			createListings.createFlipkartListingsForManyPhone(mobileForlderNames.split(","));
			//Creating screen guards
			System.out.println("Creating Listings for Screen Guards");
			createListings.createTemperedGlassListings(mobileForlderNames);
			//Extracting
			System.out.println("Extracting Catalog Files for All Sellers");
			List<String> amrSingleListingsCasesNCovers = createListings.extractSpecificCatalog(mobileForlderNames, "Back Cover,Back Replacement Cover_Differnet Brand Color,Bumper Case_Differnet Brand Color,Flip Cover,Front & Back Case_Differnet Brand Color,Book Cover_Differnet Brand Color");
			List<String> amrTemperedGlass = createListings.extractSpecificCatalog(mobileForlderNames, "Tempered Glass,Screen Guard");
			
			List<String> amrXoldaSingleListingsCasesNCovers = createListings.extractSpecificCatalogForOtherSellers("AMR", mobileForlderNames, "Back Cover,Back Replacement Cover_Differnet Brand Color,Bumper Case_Differnet Brand Color,Flip Cover,Front & Back Case_Differnet Brand Color,Book Cover_Differnet Brand Color");
			List<String> amrXoldaTemperedGlass = createListings.extractSpecificCatalogForOtherSellers("AMR", mobileForlderNames, "Tempered Glass,Screen Guard");
			
			amrSingleListingsCasesNCovers.addAll(amrXoldaSingleListingsCasesNCovers);
			amrTemperedGlass.addAll(amrXoldaTemperedGlass);
			
			List<String> tramSingleListingsCasesNCovers = createListings.extractSpecificCatalogForOtherSellers("TRAM", mobileForlderNames, "Back Cover,Back Replacement Cover_Differnet Brand Color,Bumper Case_Differnet Brand Color,Flip Cover,Front & Back Case_Differnet Brand Color,Book Cover_Differnet Brand Color");
			List<String> tramTemperedGlass = createListings.extractSpecificCatalogForOtherSellers("TRAM", mobileForlderNames, "Tempered Glass,Screen Guard");

			List<String> marSingleListingsCasesNCovers = createListings.extractSpecificCatalogForOtherSellers("MAR", mobileForlderNames, "Back Cover,Back Replacement Cover_Differnet Brand Color,Bumper Case_Differnet Brand Color,Flip Cover,Front & Back Case_Differnet Brand Color,Book Cover_Differnet Brand Color");
			List<String> marTemperedGlass = createListings.extractSpecificCatalogForOtherSellers("MAR", mobileForlderNames, "Tempered Glass,Screen Guard");
			
			List<String> marXOLSingleListingsCasesNCovers = createListings.extractSpecificCatalogForOtherSellers("MAR_XOLDA", mobileForlderNames, "Back Cover,Back Replacement Cover_Differnet Brand Color,Bumper Case_Differnet Brand Color,Flip Cover,Front & Back Case_Differnet Brand Color,Book Cover_Differnet Brand Color");
			List<String> marXOLTemperedGlass = createListings.extractSpecificCatalogForOtherSellers("MAR_XOLDA", mobileForlderNames, "Tempered Glass,Screen Guard");
			
			List<String> marMozetteSingleListingsCasesNCovers = createListings.extractSpecificCatalogForOtherSellers("MAR_Mozette", mobileForlderNames, "Back Cover,Back Replacement Cover_Differnet Brand Color,Bumper Case_Differnet Brand Color,Flip Cover,Front & Back Case_Differnet Brand Color,Book Cover_Differnet Brand Color");
			List<String> marMozetteTemperedGlass = createListings.extractSpecificCatalogForOtherSellers("MAR_Mozette", mobileForlderNames, "Tempered Glass,Screen Guard");
			
			
			marSingleListingsCasesNCovers.addAll(marXOLSingleListingsCasesNCovers);
			marSingleListingsCasesNCovers.addAll(marMozetteSingleListingsCasesNCovers);
			
			marTemperedGlass.addAll(marXOLTemperedGlass);
			marTemperedGlass.addAll(marMozetteTemperedGlass);
			
			
			//Writing Listings to File
			if(amrSingleListingsCasesNCovers.size() > 0)
				createListings.writeToFile(amrSingleListingsCasesNCovers, "AMR", "cases_covers",0);
			if(amrTemperedGlass.size() > 0)
				createListings.writeToFile(amrTemperedGlass, "AMR", "screen_guards",0);

			if(tramSingleListingsCasesNCovers.size() > 0)
				createListings.writeToFile(tramSingleListingsCasesNCovers, "TRAM", "cases_covers",0);
			if(tramTemperedGlass.size() > 0)
				createListings.writeToFile(tramTemperedGlass, "TRAM", "screen_guards",0);

			if(marSingleListingsCasesNCovers.size() > 0)
				createListings.writeToFile(marSingleListingsCasesNCovers, "MAR", "cases_covers",0);
			if(marTemperedGlass.size() > 0)
				createListings.writeToFile(marTemperedGlass, "MAR", "screen_guards",0);
			
			//Create SKU vs Phone Mapping
			//AMR
			System.out.println("Loading SkuvsPhone Mappings");
			List<String> amrSkuVsPhoneMapping = new ArrayList<>();
			loadSkuVsPhoneMapping(amrSkuVsPhoneMapping, amrSingleListingsCasesNCovers,"cases_covers");
			loadSkuVsPhoneMapping(amrSkuVsPhoneMapping, amrTemperedGlass,"screen_guards");
			createListings.writeToFileMapping(amrSkuVsPhoneMapping, "AMR");
			
			//TRAM
			List<String> tramSkuVsPhoneMapping = new ArrayList<>();
			loadSkuVsPhoneMapping(tramSkuVsPhoneMapping, tramSingleListingsCasesNCovers,"cases_covers");
			loadSkuVsPhoneMapping(tramSkuVsPhoneMapping, tramTemperedGlass,"screen_guards");
			createListings.writeToFileMapping(tramSkuVsPhoneMapping, "TRAM");
			
			//MAR
			List<String> marSkuVsPhoneMapping = new ArrayList<>();
			loadSkuVsPhoneMapping(marSkuVsPhoneMapping, marSingleListingsCasesNCovers,"cases_covers");
			loadSkuVsPhoneMapping(marSkuVsPhoneMapping, marTemperedGlass,"screen_guards");
			createListings.writeToFileMapping(marSkuVsPhoneMapping, "MAR");
			
			//Unique Mobile Names
			List<String> mobileNames = new ArrayList<>();
			for(String echFoilder : mobileForlderNames.split(",")){
				File file = new File(echFoilder);
				mobileNames.add(file.getName());
			}
			createListings.writeToFile(mobileNames, "AMR", "UniqueMobileNames",0);
			createListings.writeToFile(mobileNames, "TRAM", "UniqueMobileNames",0);
			createListings.writeToFile(mobileNames, "MAR", "UniqueMobileNames",0);
			
		}else if(args[0].equals("CreateComboImages")){
			String plusImage = (String) createListings.configProp.get("plusimage");
			CreateCombosListings CreateCombosListings = new CreateCombosListings();
			CreateCombosListings.setPlusImage("Y".equals(plusImage) ? true : false);
			CreateCombosListings.createComboForOnePhoneNew(mobileForlderNames);
		}else if(args[0].equals("CreateCombosListing")){
			String plusImage = (String) createListings.configProp.get("plusimage");
			String combosLimit = (String) createListings.configProp.get("comboslimit");
			CreateCombosListings CreateCombosListings = new CreateCombosListings();
			CreateCombosListings.setPlusImage("Y".equals(plusImage) ? true : false);
			if(!"".equals(combosLimit)){
				CreateCombosListings.setCombosLimit(new Integer(combosLimit));
			}
			CreateCombosListings.createCombosCatalog(mobileForlderNames);
		}else if(args[0].equals("ExtractCombos")){
			CreateCombosListings createCombosListings = new CreateCombosListings();
			
			List<String> amrListings = createCombosListings.extractCombosCatalogForOtherSellers("AMR_MOZETTE",mobileForlderNames);
			List<String> amrXolda = createCombosListings.extractCombosCatalogForOtherSellers("AMR_XOLDA",mobileForlderNames);
			amrListings.addAll(amrXolda);
			
			List<String> tramListings = createCombosListings.extractCombosCatalogForOtherSellers("TRAM",mobileForlderNames);
			
			List<String> marListings = createCombosListings.extractCombosCatalogForOtherSellers("MAR",mobileForlderNames);
			//Commenting this out as huge listings is getting uploaded
//			List<String> marMozette = createCombosListings.extractCombosCatalogForOtherSellers("MAR_Mozette",mobileForlderNames);
//			marListings.addAll(marMozette);
			
			if(amrListings.size() > 0)
				createListings.writeToFile(amrListings, "AMR", "cases_covers",0);

			System.out.println("AMR Combos Created : "+amrListings.size());
			if(tramListings.size() > 0)
				createListings.writeToFile(tramListings, "TRAM", "cases_covers",0);

			System.out.println("TRAM Combos Created : "+tramListings.size());
			if(marListings.size() > 0)
				createListings.writeToFile(marListings, "MAR", "cases_covers",0);
			
			System.out.println("MAR Combos Created : "+marListings.size());
			//Create SKU vs Phone Mapping
			//AMR
			System.out.println("Loading SkuvsPhone Mappings");
			List<String> amrSkuVsPhoneMapping = new ArrayList<>();
			loadSkuVsPhoneMapping(amrSkuVsPhoneMapping, amrListings,"cases_covers");
			createListings.writeToFileMapping(amrSkuVsPhoneMapping, "AMR");
			
			//TRAM
			List<String> tramSkuVsPhoneMapping = new ArrayList<>();
			loadSkuVsPhoneMapping(tramSkuVsPhoneMapping, tramListings,"cases_covers");
			createListings.writeToFileMapping(tramSkuVsPhoneMapping, "TRAM");
			
			//MAR
			List<String> marSkuVsPhoneMapping = new ArrayList<>();
			loadSkuVsPhoneMapping(marSkuVsPhoneMapping, marListings,"cases_covers");
			createListings.writeToFileMapping(marSkuVsPhoneMapping, "MAR");
			
			
			List<String> amrMAListings = createCombosListings.createMobileAccessoriesCombo("AMR_MOZETTE",mobileForlderNames);
			List<String> amrMAXolda = createCombosListings.createMobileAccessoriesCombo("AMR_XOLDA",mobileForlderNames);
			amrMAListings.addAll(amrMAXolda);
			
			List<String> tramMAListings = createCombosListings.createMobileAccessoriesCombo("TRAM",mobileForlderNames);
			
			List<String> marMAListings = createCombosListings.createMobileAccessoriesCombo("MAR",mobileForlderNames);
			List<String> marMAMozette = createCombosListings.createMobileAccessoriesCombo("MAR_Mozette",mobileForlderNames);
			marMAListings.addAll(marMAMozette);
			
			if(amrMAListings.size() > 0)
				createListings.writeToFile(amrMAListings, "AMR", "mobileaccessories_combo",0);

			System.out.println("AMR Mobile Accessories Combos Created : "+amrMAListings.size());
			if(tramMAListings.size() > 0)
				createListings.writeToFile(tramMAListings, "TRAM", "mobileaccessories_combo",0);

			System.out.println("TRAM Mobile Accessories Combos Created : "+tramMAListings.size());
			if(marMAListings.size() > 0)
				createListings.writeToFile(marMAListings, "MAR", "mobileaccessories_combo",0);

			System.out.println("MAR Mobile Accessories Combos Created : "+marMAListings.size());
			//Create SKU vs Phone Mapping
			//AMR
			System.out.println("Loading SkuvsPhone Mappings");
			List<String> amrMASkuVsPhoneMapping = new ArrayList<>();
			loadSkuVsPhoneMapping(amrMASkuVsPhoneMapping, amrMAListings,"mACombo");
			createListings.writeToFileMapping(amrMASkuVsPhoneMapping, "AMR");
			
			//TRAM
			List<String> tramMASkuVsPhoneMapping = new ArrayList<>();
			loadSkuVsPhoneMapping(tramMASkuVsPhoneMapping, tramMAListings,"mACombo");
			createListings.writeToFileMapping(tramMASkuVsPhoneMapping, "TRAM");
			
			//MAR
			List<String> marMASkuVsPhoneMapping = new ArrayList<>();
			loadSkuVsPhoneMapping(marMASkuVsPhoneMapping, marMAListings,"mACombo");
			createListings.writeToFileMapping(marMASkuVsPhoneMapping, "MAR");
			
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
		}
		


		createListings.emailUtils.sendEmail("Listings","","","PFA",createListings.filePaths,"shezan.listings@gmail.com");
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
		File fout = new File("./"+fileName+".txt");
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
		SimpleDateFormat dateFolderformatter = new SimpleDateFormat("yyyy-MM-dd");  
	    Date date = new Date();  
	    
	    Calendar c = Calendar.getInstance(); 
		c.setTime(date); 
		c.add(Calendar.DATE, dayPlus);
		date = c.getTime();
		
	    String dateString = formatter.format(date);
	    String dateFolderString = dateFolderformatter.format(date);
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
	
	private String getFileNameFromType(String sellerName, String type) {
		// TODO Auto-generated method stub
		if(type.equals("cases_covers")){
			return sellerName+"_"+type+"_listings";
		}
		if(sellerName.equals("AMR")){
			
		}
		return null;
	}

	private void createFlipkartListingsForManyPhone(String[] multiplePhones) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		if(multiplePhones != null && multiplePhones.length > 0){
			for(String eachPhone : multiplePhones){
				createFlipkartListingsForOnePhone(eachPhone);
			}
		}
		
		System.out.println("Update the SKU in properties file : "+ this.skuStart+1);
	}

	private void createTemperedGlassListings(String mobileFolders) throws IOException {
		// TODO Auto-generated method stub
		String folders[] = mobileFolders.split(",");
		for(String eachFolder : folders){
			File mobile = new File(eachFolder);
			String mobileName = mobile.getName();
			File temperedGlasses = new File(eachFolder+"/Tempered Glass");
			if(temperedGlasses != null && temperedGlasses.exists() && temperedGlasses.isDirectory() && temperedGlasses.listFiles().length > 0){
				resizeImagesForOnePhone(eachFolder+"/Tempered Glass");
				createTemperedGlassListngs(mobileName,temperedGlasses,eachFolder);
				File abc = new File(eachFolder+"/###backup/Tempered Glass");
				FileUtils.forceMkdir(abc);
				File catFile = new File(eachFolder+"/Catalog/Tempered Glass");
				FileUtils.copyDirectory(catFile, abc);
			}
			
		}
		
		updateTemperedSkuCount();
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

	private void createTemperedGlassListngs(String mobileName, File eachType, String mobileFolder) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		
		String keywords = getKeywordsMap().get(eachType.getName());
		String caseTypes = getCaseTypesMap().get(eachType.getName());
		
		File[] imageList = eachType.listFiles();
		if(imageList != null && imageList.length > 0){
			
			//create image combinations
//			createComboImagesForTemperedGlasses(eachType);
			
			List<String> imageUrls = new ImageShack("").processImageUploadNew(eachType.getAbsolutePath(), mobileName);
			List<String> commonImageUrls = new ImageShack("").processImageUploadNew(eachType.getAbsolutePath()+"/common", mobileName);
			
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
	
	private void updateTemperedSkuCount() throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Updating the Tempered SKU count"+temperedSkuStart);
		FileUtils.writeStringToFile(new File(TEMPERED_COUNT_PROPERTIES), "temperedSkuCount="+this.temperedSkuStart);
	}

	private void createFlipkartListingsForOnePhone(String mobileFolder) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		//resize all images
		resizeImagesForOnePhone(mobileFolder);
		
		File parentFolder = new File(mobileFolder);
		String mobileName = parentFolder.getName();
		System.out.println("Creating Listings for Mobile : "+mobileName);
		File[] varieties = parentFolder.listFiles();
		if(varieties != null && varieties.length > 0){
			for(File eachType : varieties){
				if(eachType.getName().contains("Combo") || eachType.getName().contains("Glass") 
						|| eachType.getName().contains("Tempered") || eachType.getName().contains("Catalog") 
						|| eachType.getName().contains("#")){
					continue;
				}
				System.out.println("Starting to create listings of type : "+eachType.getName());
				createListngs(mobileName,eachType,mobileFolder);
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
		//update the sku count in skuCount.txt
		updateSkuCount();
	}
	
	private void createListngs(String mobileName, File eachType, String mobileFolder) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		
		String keywords = getKeywordsMap().get(eachType.getName());
		String brandColors[] = getBrandColors();
		String caseTypes = getCaseTypesMap().get(eachType.getName());
		
		File[] imageList = eachType.listFiles();
		if(imageList != null && imageList.length > 0){
			List<String> imageUrls = new ImageShack("").processImageUploadNew(eachType.getAbsolutePath(), mobileName);
			List<String> commonImageUrls = new ImageShack("").processImageUploadNew(eachType.getAbsolutePath()+"/common", mobileName);
			
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

	private void updateSkuCount() throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Updating the SKU count"+skuStart);
		FileUtils.writeStringToFile(new File(SKU_COUNT_PROPERTIES), "skuCount="+this.skuStart);
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

	private int getLastTemperedSkuCount() throws FileNotFoundException, IOException {
		try(BufferedReader br = new BufferedReader(new FileReader(TEMPERED_COUNT_PROPERTIES))) {
		    String line = br.readLine();
		    if(line != null && line.contains("temperedSkuCount=")){
		    	line = line.replace("temperedSkuCount=", "");
		    	if(!line.isEmpty() && StringUtils.isNumeric(line)){
//		    		System.out.println("Last Tempered SKU created was : "+line);
		    		return new Integer(line).intValue() + 1;
		    	}
		    }
		}
		return -1;
	}

	private Map<String, String> loadKeywords() {
		Map<String, String> keywordsMap = new HashMap<>();
		keywordsMap.put("Transparent", "mobilename transparent back cover in plain cases & covers::mobilename back cover in plain cases & covers::mobilename back cover::mobilename back covers::mobilename transparent cover::mobilename transparent back cover::mobilename back cover transparent::mobilename light weight transparent covers::mobilename light weight transparent back covers::mobilename light weight transparent cover::mobilename light weight transparent back cover::mobilename soft back cover::mobilename soft covers::mobilename soft back cover::mobilename flexible transparent cover::mobilename flexible transparent back cover::mobilename plain back cover");
		keywordsMap.put("Flip Cover Black", "mobilename flip cover in plain cases & covers::mobilename flip cover::mobilename flip covers::mobilename black cover::mobilename black flip cover::mobilename flip cover black::mobilename light weight black covers::mobilename light weight black flip covers::mobilename light weight black cover::mobilename light weight black flip cover::mobilename soft flip cover::mobilename soft covers::mobilename soft flip cover::mobilename flexible black cover::mobilename flexible black flip cover::mobilename plain flip cover::mobilename flip cover in plain cases & covers::mobilename flip cover::mobilename flip covers::mobilename transparent cover::mobilename transparent flip cover::mobilename flip cover transparent::mobilename light weight transparent covers::mobilename light weight transparent flip covers::mobilename light weight transparent cover::mobilename light weight transparent flip cover::mobilename soft flip cover::mobilename soft covers");
		keywordsMap.put("Flip Cover Gold", "mobilename flip cover in plain cases & covers::mobilename flip cover::mobilename flip covers::mobilename gold cover::mobilename gold flip cover::mobilename flip cover gold::mobilename light weight gold covers::mobilename light weight gold flip covers::mobilename light weight gold cover::mobilename light weight gold flip cover::mobilename soft flip cover::mobilename soft covers::mobilename soft flip cover::mobilename flexible gold cover::mobilename flexible gold flip cover::mobilename plain flip cover::mobilename flip cover in plain cases & covers::mobilename flip cover::mobilename flip covers::mobilename transparent cover::mobilename transparent flip cover::mobilename flip cover transparent::mobilename light weight transparent covers::mobilename light weight transparent flip covers::mobilename light weight transparent cover::mobilename light weight transparent flip cover::mobilename soft flip cover::mobilename soft covers");
		keywordsMap.put("Defender", "mobilename back cover in plain cases & covers::mobilename back cover ::mobilename back covers::mobilename black cover::mobilename black back cover::mobilename back cover black ::mobilename light weight black covers::mobilename light weight black back covers::mobilename light weight black cover::mobilename light weight black back cover::mobilename soft back cover::mobilename soft covers::mobilename soft back cover::mobilename flexible black cover::mobilename flexible black back cover::mobilename plain back cover::mobilename back cover in plain cases & covers::mobilename back cover ::mobilename back covers::mobilename transparent cover::mobilename transparent back cover::mobilename back cover transparent ::mobilename light weight transparent covers::mobilename light weight transparent back covers::mobilename light weight transparent cover::mobilename light weight transparent back cover::mobilename soft back cover::mobilename soft covers");
		keywordsMap.put("Cherry", "mobilename back cover in plain cases & covers::mobilename back cover ::mobilename back covers::mobilename black cover::mobilename black back cover::mobilename back cover black ::mobilename light weight black covers::mobilename light weight black back covers::mobilename light weight black cover::mobilename light weight black back cover::mobilename soft back cover::mobilename soft covers::mobilename soft back cover::mobilename flexible black cover::mobilename flexible black back cover::mobilename plain back cover::mobilename back cover in plain cases & covers::mobilename back cover ::mobilename back covers::mobilename transparent cover::mobilename transparent back cover::mobilename back cover transparent ::mobilename light weight transparent covers::mobilename light weight transparent back covers::mobilename light weight transparent cover::mobilename light weight transparent back cover::mobilename soft back cover::mobilename soft covers");
		keywordsMap.put("Chrome", "mobilename back cover in plain cases & covers::mobilename back cover ::mobilename back covers::mobilename gold cover::mobilename gold back cover::mobilename back cover gold ::mobilename light weight gold covers::mobilename light weight gold back covers::mobilename light weight gold cover::mobilename light weight gold back cover::mobilename soft back cover::mobilename soft covers::mobilename soft back cover::mobilename flexible gold cover::mobilename flexible gold back cover::mobilename plain back cover::mobilename back cover in plain cases & covers::mobilename back cover ::mobilename back covers::mobilename transparent cover::mobilename transparent back cover::mobilename back cover transparent ::mobilename light weight transparent covers::mobilename light weight transparent back covers::mobilename light weight transparent cover::mobilename light weight transparent back cover::mobilename soft back cover::mobilename soft covers");
		keywordsMap.put("Tempered Glass", "mobilename tempered glasses in mobile screen guards::mobilename tempered glass in mobile screen guards::mobilename tempered glass::mobilename screen protector::mobilename screen guard::mobilename glass protector::mobilename clear tempered glass::mobilename transparent tempered glass::mobilename tempered glass transparent::mobilename thin screen guards::mobilename sleek screen guards::mobilename thin tempered glasss::mobilename light weight transparent tempered glass");
		keywordsMap.put("Screen Guard", "mobilename tempered glasses in mobile screen guards::mobilename tempered glass in mobile screen guards::mobilename tempered glass::mobilename screen protector::mobilename screen guard::mobilename glass protector::mobilename clear tempered glass::mobilename transparent tempered glass::mobilename tempered glass transparent::mobilename thin screen guards::mobilename sleek screen guards::mobilename thin tempered glasss::mobilename light weight transparent tempered glass");
		return keywordsMap;
	}
	
	private Map<String, String> loadCaseTypes() {
		// TODO Auto-generated method stub
		Map<String, String> caseTypes = new HashMap<>();
		//TP
		caseTypes.put("Transparent", "Back Cover:Y,Back Replacement Cover:Y");
//		caseTypes.put("Boom Transparent", "Back Cover:Y,Back Replacement Cover:Y");
		
		//Defenders
		caseTypes.put("Defender", "Back Cover:Y,Back Replacement Cover:Y,Bumper Case:Y");
//		caseTypes.put("Dazzle Defender", "Back Cover:Y,Back Replacement Cover:Y,Bumper Case:Y");
		
		//Cherries
		caseTypes.put("Cherry", "Back Cover:Y,Back Replacement Cover:Y");
		caseTypes.put("Chrome", "Back Cover:Y,Back Replacement Cover:Y");
//		caseTypes.put("Hybrid", "Back Cover:Y,Back Replacement Cover:Y");
//		caseTypes.put("Black Line", "Back Cover:Y,Back Replacement Cover:Y");
//		caseTypes.put("Dotted Cherry", "Back Cover:Y,Back Replacement Cover:Y");
//		caseTypes.put("Gkk Red", "Back Cover:Y,Back Replacement Cover:Y");
//		caseTypes.put("Gkk Blue", "Back Cover:Y,Back Replacement Cover:Y");
//		caseTypes.put("Moshi", "Back Cover:Y,Back Replacement Cover:Y");
		
		//Glasses
		caseTypes.put("Tempered Glass", "Tempered Glass:Y");
//		caseTypes.put("Screen Guard", "Screen Guard:Y");
//		caseTypes.put("Black Glass", "Tempered Glass:Y");
//		caseTypes.put("White Glass", "Tempered Glass:Y");
//		caseTypes.put("Nano Glass", "Tempered Glass:Y");

		//Flips
		caseTypes.put("Flip Cover Gold", "Flip Cover:Y,Front & Back Case:Y,Book Cover:Y");
		caseTypes.put("Flip Cover Black", "Flip Cover:Y,Front & Back Case:Y,Book Cover:Y");
//		caseTypes.put("Rich Boss Black", "Flip Cover:Y,Front & Back Case:Y,Book Cover:Y");
//		caseTypes.put("Rich Boss Brown", "Flip Cover:Y,Front & Back Case:Y,Book Cover:Y");
//		caseTypes.put("Rich Boss Blue", "Flip Cover:Y,Front & Back Case:Y,Book Cover:Y");
//		caseTypes.put("Rich Boss Red", "Flip Cover:Y,Front & Back Case:Y,Book Cover:Y");
//		caseTypes.put("Lishen Cream", "Flip Cover:Y,Front & Back Case:Y,Book Cover:Y");
//		caseTypes.put("Lishen Brown", "Flip Cover:Y,Front & Back Case:Y,Book Cover:Y");
//		caseTypes.put("Lishen Black", "Flip Cover:Y,Front & Back Case:Y,Book Cover:Y");
//		caseTypes.put("Lishen Blue", "Flip Cover:Y,Front & Back Case:Y,Book Cover:Y");
		
		return caseTypes;
	}

	private Map<String, String> loadColorMap() {
		Map<String, String> colorMap = new HashMap<>();
		colorMap.put("Transparent", "Transparent");
//		colorMap.put("Boom Transparent", "Transparent");
		
		//Cherry
		colorMap.put("Cherry", "Black");
		colorMap.put("Chrome", "Gold");
//		colorMap.put("Hybrid", "Black");
//		colorMap.put("Gkk Red", "Red");
//		colorMap.put("Gkk Blue", "Blue");
//		colorMap.put("Moshi", "Black");
		
		colorMap.put("Flip Cover Gold", "Gold");
		colorMap.put("Flip Cover Black", "Black");
		colorMap.put("Defender", "Black");
		colorMap.put("Tempered Glass", "Transparent");
		colorMap.put("Screen Guard", "Transparent");

//		colorMap.put("Rich Boss Black", "Black");
//		colorMap.put("Rich Boss Brown", "Brown");
//		colorMap.put("Rich Boss Blue", "Blue");
//		colorMap.put("Rich Boss Red", "Red");
//		colorMap.put("Lishen Cream", "Beige");
//		colorMap.put("Lishen Brown", "Brown");
//		colorMap.put("Lishen Black", "Black");
//		colorMap.put("Lishen Blue", "Blue");

//		colorMap.put("Screen Guard", "Transparent");
//		colorMap.put("Full Glass", "Transparent");
//		colorMap.put("Nano Glass", "Transparent");


//		colorMap.put("Dazzle Defender", "Black");

		return colorMap;
	}
	
	private Map<String, String> loadSkuMap() {
		Map<String, String> caseTypes = new HashMap<>();
		caseTypes.put("Transparent", "mzt-tp-a");
		caseTypes.put("Flip Cover Gold", "mzt-fcg-a");
		caseTypes.put("Flip Cover Black", "mzt-fcb-a");
		caseTypes.put("Defender", "mzt-def-a");
		caseTypes.put("Cherry", "mzt-chr-a");
		caseTypes.put("Chrome", "mzt-crm-a");
		caseTypes.put("Tempered Glass", "mzt-sg-a");
		caseTypes.put("Screen Guard", "mzt-sg-a");
		
//		caseTypes.put("Boom Transparent", "mzt-btp-a");
//		caseTypes.put("Hybrid", "mzt-hyb-a");
//		caseTypes.put("Gkk Red", "mzt-gkkr-a");
//		caseTypes.put("Moshi", "mzt-mos-a");
//		caseTypes.put("Full Glass", "mzt-fg-a");
//		caseTypes.put("Nano Glass", "mzt-ng-a");
//		caseTypes.put("Dazzle Defender", "mzt-ddef-a");
		
		//Flips
//		caseTypes.put("Rich Boss Black", "mzt-rbblck-a");
//		caseTypes.put("Rich Boss Brown", "mzt-rbbrwn-a");
//		caseTypes.put("Rich Boss Blue", "mzt-rbblue-a");
//		caseTypes.put("Rich Boss Red", "mzt-rbred-a");
//		caseTypes.put("Lishen Cream", "mzt-liscrem-a");
//		caseTypes.put("Lishen Brown", "mzt-lisbrwn-a");
//		caseTypes.put("Lishen Black", "mzt-lisblck-a");
//		caseTypes.put("Lishen Blue", "mzt-lisblue-a");
		return caseTypes;
	}
	
	private Map<String, String> loadModelMap() {
		Map<String, String> caseTypes = new HashMap<>();
		caseTypes.put("Transparent", "M-tp-a");
//		caseTypes.put("Boom Transparent", "M-btp-a");
		caseTypes.put("Defender", "M-def-a");
		caseTypes.put("Cherry", "M-chr-a");
		caseTypes.put("Chrome", "M-crm-a");
		caseTypes.put("Tempered Glass", "M-tg-a");
		caseTypes.put("Screen Guard", "M-sg-a");
//		caseTypes.put("Hybrid", "M-hyb-a");
//		caseTypes.put("Gkk Red", "M-gkkr-a");
//		caseTypes.put("Moshi", "M-mos-a");
//		caseTypes.put("Full Glass", "M-fg-a");
//		caseTypes.put("Nano Glass", "M-ng-a");
//		caseTypes.put("Dazzle Defender", "M-ddef-a");
		
		//Flips
		caseTypes.put("Flip Cover Gold", "M-fcg-a");
		caseTypes.put("Flip Cover Black", "M-fcb-a");
//		caseTypes.put("Rich Boss Black", "M-rbblck-a");
//		caseTypes.put("Rich Boss Brown", "M-rbbrwn-a");
//		caseTypes.put("Rich Boss Blue", "M-rbblue-a");
//		caseTypes.put("Rich Boss Red", "M-rbred-a");
//		caseTypes.put("Lishen Cream", "M-liscrem-a");
//		caseTypes.put("Lishen Brown", "M-lisbrwn-a");
//		caseTypes.put("Lishen Black", "M-lisblck-a");
//		caseTypes.put("Lishen Blue", "M-lisblue-a");
		return caseTypes;
	}

	private Map<String, String> loadDescriptionMap() {
		Map<String, String> colorMap = new HashMap<>();
		colorMap.put("Transparent", "Mozette Crystal Clear and High Quality Transparent cover best fits and compatible to your <mobilename>. Its sleek and ultra thin design is flexible and reliable. Gives comfortable access to ports, buttons, camera, sensors and all other features. It's sleek body minimizes the bulk and gives the original feel of the phone. It's rounded corners and raised edges protects your phone completely from damaging your phone. Polished and laser cut texture makes it scratch proof. Designed to fit <mobilename> perfectly. Overall it is best in material and durability.All buttons and jacks are accessible through the cutouts, making it comfortable.");
		colorMap.put("Flip Cover Gold", "Mozette Gold Flip Cover fits perfectly and compatible to your <mobilename> phone. It's smooth texture gives an awesome feel. It save your mobile from scratches even if scratched with keys in your pocket. All buttons and jacks are accessible through the cutouts with a precise fit seamless compatibility for perfect fitting. Moreover, Its advanced shock and shatter absorption saves it from drops and bumps. Protection against scratches on backside of mobile and very comfortable and easy installation.");
		colorMap.put("Flip Cover Black", "Mozette Black Flip Cover fits perfectly and compatible to your <mobilename> phone. It's smooth texture gives an awesome feel. It save your mobile from scratches even if scratched with keys in your pocket. All buttons and jacks are accessible through the cutouts with a precise fit seamless compatibility for perfect fitting. Moreover, Its advanced shock and shatter absorption saves it from drops and bumps. Protection against scratches on backside of mobile and very comfortable and easy installation.");
		colorMap.put("Defender", "Mozette protective Back Cover is a foolproof solution to protect your mobile from any damage. It best fits and perfectly compatible to your <mobilename> mobile. Outer layer has rugged design to add excess protection. More over the outer surface covers the inner shell to absorb impact from extreme bumps and shocks. Comes with the built-in stand, offering comfortable angle for watching videos, Video chat and web-surfing on any surface. Complete access to all features of your device.");
		colorMap.put("Cherry", "Mozette ultra thin and shock resistant back cover protects your mobile from scratches,bumps and shocks. It's made of rubber and polycarbonate which makes you feel smooth. Its sleek and ultra thin design is flexible and reliable. Gives comfortable access to ports, buttons, camera, sensors and all other features. Polished and laser cut texture makes it scratch proof. Overall it is best in material and durability.All buttons and jacks are accessible through the cutouts, making it comfortable.");
		colorMap.put("Chrome", "Mozette Crystal Clear and High Quality Transparent cover best fits and compatible to your <mobilename>. Its golden edges gives your phone a royal look. Its sleek and ultra thin design is flexible and reliable. Gives comfortable access to ports, buttons, camera, sensors and all other features. It's sleek body minimizes the bulk and gives the original feel of the phone. It's rounded corners and raised edges protects your phone completely from damaging your phone. Polished and laser cut texture makes it scratch proof. Designed to fit <mobilename> perfectly. Overall it is best in material and durability.All buttons and jacks are accessible through the cutouts, making it comfortable.");
		colorMap.put("Tempered Glass", "Mozette Premium Tempered Glass for <mobilename> is designed to provide maximum protection from scratches, drops and crash landings.it is crafted to cover the entire screen from edge to edge with a unique flex bend assured not to peel or curl The Screen Protector for <mobilename> is built with laser cut tempered glass with rounded, polished edges. The Mozette <mobilename> Tempered Glass can offer High Definition clarity and precise touchscreen experience. Additionally, it comes with the Oleophobic Coating that defies oil smudges and fingerprints.");
		colorMap.put("Screen Guard", "Mozette Premium Tempered Glass for <mobilename> is designed to provide maximum protection from scratches, drops and crash landings.it is crafted to cover the entire screen from edge to edge with a unique flex bend assured not to peel or curl The Screen Protector for <mobilename> is built with laser cut tempered glass with rounded, polished edges. The Mozette <mobilename> Tempered Glass can offer High Definition clarity and precise touchscreen experience. Additionally, it comes with the Oleophobic Coating that defies oil smudges and fingerprints.");
		return colorMap;
	}

	private Properties loadProperties() throws IOException{

	    //to load application's properties, we use this class
	    Properties mainProperties = new Properties();

	    FileInputStream file;

	    //the base folder is ./, the root of the main.properties file  
	    String path = "./code/config.properties";//"D:/AAA_WORK/Important Batch Files/Flipkart/Non-LIVE to LIVE/code/config.properties

	    //load the file handle for main.properties
	    file = new FileInputStream(path);

	    //load all the properties from this file
	    mainProperties.load(file);

	    //we have loaded the properties, so close the file handle
	    file.close();


	    return mainProperties;
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

	public void setColorMap(Map<String, String> colorMap) {
		this.colorMap = colorMap;
	}

	public Map<String, String> getColorMap() {
		return colorMap;
	}
	
	public Map<String, String> getSkuStringMap() {
		return skuStringMap;
	}

	public void setSkuStringMap(Map<String, String> skuStringMap) {
		this.skuStringMap = skuStringMap;
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
}
