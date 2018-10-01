package amr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

public class CreateCombosListings {
	

	private final static String  SKU_COUNT_PROPERTIES = "./code/skuCount.txt";
	private final static String  COMBO_COUNT_PROPERTIES = "./code/comboCount.txt";


	private Map<String, String> comboCaseTypes;
	private Map<String, String> comboSKUMap;
	private int comboCount;
	private Map<String, String> keywordsMap;
	private int comboSkuStart;
	private Map<String, String> colorMap;
	private int skuStart;
	private boolean plusImage;
	private int combosLimit=0;
	private int combosCreated = 0;
	
	public CreateCombosListings() throws FileNotFoundException, IOException{
		comboSKUMap = loadComboSkuMap();
		comboCaseTypes = loaComboCaseTypes();
		this.comboCount = 0;
		keywordsMap = loadKeywords();
		colorMap = loadColorMap();
		this.skuStart= getLastSkuCount();
		this.plusImage = false;
		this.comboSkuStart = getLastComboSkuCount();
	}

	private int getLastComboSkuCount() throws FileNotFoundException, IOException {
		try(BufferedReader br = new BufferedReader(new FileReader(COMBO_COUNT_PROPERTIES))) {
		    String line = br.readLine();
		    if(line != null && line.contains("comboSkuCount=")){
		    	line = line.replace("comboSkuCount=", "");
		    	if(!line.isEmpty() && StringUtils.isNumeric(line)){
//		    		System.out.println("Last Combo SKU created was : "+line);
		    		return new Integer(line).intValue() + 1;
		    	}
		    }
		}
		return -1;
	}
	


	public void createComboForOnePhoneNew(String mF) throws IOException {
		// TODO Auto-generated method stub
		
		String[] mobileFolders =mF.split(",");
		for(String mobileFolder: mobileFolders){
			
			resizeImagesForOnePhone(mobileFolder);
			
//			String caseTypes[] = "Anti-radiation Case,Back Cover,Back Replacement Cover,Book Cover,Bumper Case,Cases with Holder,Dot View Case,Dual Protection Case,Flip Cover,Front & Back Case,Front Cover,Grip Back Cover,Shock Proof Case".split(",");
//			String caseTypes[] = "Back Cover".split(",");
//			for(String eachCase1 : caseTypes){
				File test = new File(mobileFolder+"/Combos/Covers");
				if(test!=null && test.exists() && test.isDirectory() && test.listFiles().length > 0){//Combos already created
					System.out.println("Combos Already created for Mobile : "+mobileFolder);
					return;
				}
				String[] caseFolers = getComboCaseTypes().get("All").split(",");//Transparent,Defender,Cherry,Chrome,Flip Cover Gold,Flip Cover Black,Tempered Glass
				for(int i =0; i<caseFolers.length; i++){	//Transparent,Defender,Cherry,Chrome
					File caseFolder1 = new File(mobileFolder+"/"+caseFolers[i]);
					if(caseFolder1.exists() && caseFolder1.isDirectory()){
//						createCombinationsOfOneType(mobileFolder,caseFolder1,eachCase);
						for(int j=i+1; j< caseFolers.length; j++){
							File caseFolder2 = new File(mobileFolder+"/"+caseFolers[j]);
							if(caseFolder2.exists() && caseFolder2.isDirectory()){
								createCombinationsOfMixedTypes(mobileFolder,caseFolder1,caseFolder2);
							}
						}
					}
				}
//			}
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
									String sku = skuToken[0]+"-"+skuToken[1]+"-"+skuToken[2]+"-a"+this.comboSkuStart++;
									String type1 = skuToken[1].equals("fcb") || skuToken[1].equals("fcg") ? "Flip Cover" : skuToken[1].equals("sg") ? "Screen Guard" : "Back Cover";
									String type2 = skuToken[2].equals("fcb") || skuToken[2].equals("fcg") ? "Flip Cover" : skuToken[2].equals("sg") ? "Screen Guard" : "Back Cover";
									String type3 = skuToken[2].equals("sg") ? "Screen Protector" : "Cover";
									line = sku+"\t"+
											tokens[1]+"\t"+
											tokens[2].replace("mzt", "M")+"\t"+
											type1+"::"+type2+"\t"+//change
											tokens[4]+"\t"+
											"Cover\t"+
											tokens[5]+"\t"+
											"Cover::"+type3+"\t"+//Cover or Screen Protector
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
			ImageShack iS = new ImageShack("");
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
							
							if(getCombosLimit() == 0 || getCombosLimit() > imageList.length){
								setCombosLimit(imageList.length);//If there is no limit on combos
							}
							
							List<File> fileList = new ArrayList<>();
							if(imageList != null && imageList.length > 0){
								for(int i = 0; i <= getCombosLimit(); i++){
									if(fileList.size() <= 9){
//										System.out.println(imageList[randomIntegers.get(i)].getName());
										if(imageList[randomIntegers.get(i)].isDirectory()){
											continue;
										}
										fileList.add(imageList[randomIntegers.get(i)]);
										tempFiles.remove(randomIntegers.get(i));
									}else{
										List<String> imageUrls = iS.processImageUploadForCombo(fileList, "combos",combosFoldr+"/Combos");
										if(imageUrls.size() == fileList.size()){
											List<Map<Integer, String>> completedListings = popuateEachRowForCombo(fileList,imageUrls,mobileName);
											writeCombosToFile(completedListings,combosFoldr+"/Combos");
											combosCreated += 10;
											System.out.println("Combos Created : "+combosCreated);
										}
										fileList.removeAll(fileList);
										fileList = new ArrayList<>();
										if(imageList[randomIntegers.get(i)].isDirectory()){
											continue;
										}
										fileList.add(imageList[randomIntegers.get(i)]);
										tempFiles.remove(randomIntegers.get(i));
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
			if(imageName1.contains("Flip")){
				comboSetName[0] = "FLIP COVER";
			}else if(imageName1.contains("Glass")){
				comboSetName[0] = "SCREEN PROTECTOR(TEMPERED GLASS)";
			}else{
				comboSetName[0] = "BACK COVER";
			}
			if(imageName2.contains("Flip")){
				comboSetName[1] = "FLIP COVER";
			}else if(imageName2.contains("Glass")){
				comboSetName[1] = "SCREEN PROTECTOR(TEMPERED GLASS)";
			}else{
				comboSetName[1] = "BACK COVER";
			}
			
			Map<Integer, String> eachRow = new HashMap<>();
			this.skuStart++;
			String color1 = this.getColorMap().get(imageName1);
			String color2 = this.getColorMap().get(imageName2);
			eachRow.put(6, "mzt"+getComboSKUMap().get(imageName1)+""+getComboSKUMap().get(imageName2)+"-a"+this.comboSkuStart);
			eachRow.put(7, "Mozette");
			eachRow.put(8, "M"+getComboSKUMap().get(imageName1)+""+getComboSKUMap().get(imageName2)+"-a"+this.comboSkuStart);
			eachRow.put(9, "Back Cover");
			eachRow.put(10, color1+"::"+color2);
			eachRow.put(11, mobileName);
			eachRow.put(12, "Rubber::Plastic");
			eachRow.put(13, "Mobile");
			eachRow.put(14, comboSetName[0]+"::"+comboSetName[1]);//use ::*************
			eachRow.put(15, "No Theme");
			eachRow.put(16, color1+"::"+color2);
			eachRow.put(17, imageUrls.get(i));
			eachRow.put(25, "2");
			String desc = "";
			
			if(comboSetName[0].equals(comboSetName[1]) && color1.equals(color2)){
				desc = "BEST QUALITY COMBO SET OF 2 "+color1+" "+comboSetName[0]+". MOZETTE CASES AND COVERS ARE DESIGNED TO FULLY PROTECT YOUR MOBILE FROM GETTING DAMAGED. THEY ARE IMPACT RESISTANT AND HIGHLY DURABLE. MOREOVER, ITS EXTREME SLIM PROFLE AND LIGHT WEIGHT ADDS NO ADDITIONAL BULK TO YOUR PHONE. GIVES COMPLETE ACCESS TO ALL THE BUTTONS, PORTS AND SENSORS. THE MOZETTE CASE COVERS 100% OF THE OUTER SURFACE OF THE PHONE AND PRECISION MOLDED WITH NO SEAMS OR SHARP EDGES.";
			}else{
				desc = "BEST QUALITY COMBO SET OF 1 "+color1+" "+comboSetName[0]+" AND 1 "+color2+" "+comboSetName[1]+". MOZETTE CASES AND COVERS ARE DESIGNED TO FULLY PROTECT YOUR MOBILE FROM GETTING DAMAGED. THEY ARE IMPACT RESISTANT AND HIGHLY DURABLE. MOREOVER, ITS EXTREME SLIM PROFLE AND LIGHT WEIGHT ADDS NO ADDITIONAL BULK TO YOUR PHONE. GIVES COMPLETE ACCESS TO ALL THE BUTTONS, PORTS AND SENSORS. THE MOZETTE CASE COVERS 100% OF THE OUTER SURFACE OF THE PHONE AND PRECISION MOLDED WITH NO SEAMS OR SHARP EDGES.";
			}
			
			eachRow.put(26, desc.toUpperCase());
			String keywords = getKeywordsMap().get(imageName1);
			String keywords1 = keywords.replace("mobilename", mobileName.toLowerCase());
			if(keywords1.length() >= 1000){
				keywords1 = keywords1.substring(0,999);
				keywords1 = keywords1.substring(0,keywords1.lastIndexOf(":"));
			}
//			eachRow.put(27,keywords1);
			eachRow.put(27, "FULLY PROTECTIVE::MORE RELIABLE ");
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
		System.out.println("Image 1 "+image1);
		System.out.println("Image 2 "+image2);
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
			e.printStackTrace();
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

	private void updateComboSkuCount() throws IOException {
		// TODO Auto-generated method stub
//		System.out.println("Updating the Combo SKU count"+comboSkuStart);
		FileUtils.writeStringToFile(new File(COMBO_COUNT_PROPERTIES), "comboSkuCount="+this.comboSkuStart);
	}
	
	private Map<String, String> loaComboCaseTypes() {
		// TODO Auto-generated method stub
		Map<String, String> comboCaseTypes = new HashMap<>();
		comboCaseTypes.put("All","Transparent,Defender,Cherry,Chrome,Flip Cover Gold,Flip Cover Black,Tempered Glass");
		comboCaseTypes.put("Anti-radiation Case","Flip Cover Gold,Flip Cover Black,Defender");
		comboCaseTypes.put("Back Cover","Transparent,Defender,Cherry,Chrome");
		comboCaseTypes.put("Back Replacement Cover","Transparent,Defender,Cherry,Chrome");
		comboCaseTypes.put("Book Cover","Flip Cover Gold,Flip Cover Black");
		comboCaseTypes.put("Bumper Case","Defender");
		comboCaseTypes.put("Cases with Holder","Defender");
		comboCaseTypes.put("Dot View Case","Transparent,Cherry,Chrome");
		comboCaseTypes.put("Dual Protection Case","Flip Cover Gold,Flip Cover Black");
		comboCaseTypes.put("Flip Cover","Flip Cover Gold,Flip Cover Black");
		comboCaseTypes.put("Front & Back Case","Flip Cover Black,Flip Cover Gold");
		comboCaseTypes.put("Front Cover","Flip Cover Gold,Flip Cover Black");
		comboCaseTypes.put("Grip Back Cover","Chrome,Cherry,Defender,Transparent");
		comboCaseTypes.put("Shock Proof Case","Transparent,Defender,Cherry,Chrome,Flip Cover Gold,Flip Cover Black");
		comboCaseTypes.put("Mixed Back and Flip","Flip Cover Black,Flip Cover Gold,Chrome,Cherry,Defender,Transparent");
		return comboCaseTypes;
	}
	
	private Map<String, String> loadComboSkuMap() {
		Map<String, String> caseTypes = new HashMap<>();
		caseTypes.put("Transparent", "-tp");
		caseTypes.put("Flip Cover Gold", "-fcg");
		caseTypes.put("Flip Cover Black", "-fcb");
		caseTypes.put("Defender", "-def");
		caseTypes.put("Cherry", "-chr");
		caseTypes.put("Chrome", "-crm");
		caseTypes.put("Tempered Glass", "-sg");
		return caseTypes;
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

	private Map<String, String> loadColorMap() {
		Map<String, String> colorMap = new HashMap<>();
		colorMap.put("Transparent", "Transparent");
		colorMap.put("Flip Cover Gold", "Gold");
		colorMap.put("Flip Cover Black", "Black");
		colorMap.put("Defender", "Black");
		colorMap.put("Cherry", "Black");
		colorMap.put("Chrome", "Gold");
		colorMap.put("Tempered Glass", "Transparent");
		colorMap.put("Screen Guard", "Transparent");
		return colorMap;
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
}
