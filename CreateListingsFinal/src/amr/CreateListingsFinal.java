package amr;

import java.io.File;
import java.util.Properties;

public class CreateListingsFinal {
	
	private Properties configProp;
	private ConfigurationLoader configurationLoader;

	public CreateListingsFinal() throws Exception{
		configurationLoader = new ConfigurationLoader();
		configProp = configurationLoader.loadProperties();
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		CreateListingsFinal createListings = new CreateListingsFinal();
		String mobileForlderNames = (String) createListings.configProp.get("images-folder-path");
		if(mobileForlderNames == null || "".equals(mobileForlderNames)){
			System.out.println("images-folder-path is missing in config.properties");
			return;
		}
		File dateFolder = new File(mobileForlderNames);
		System.out.println(mobileForlderNames);
		if(dateFolder == null || !dateFolder.isDirectory()){
			System.out.println("images-folder-path in config.properties is not a directory or missing");
			return;
		}
		mobileForlderNames = "";
		for(File eachMobileFolder : dateFolder.listFiles()){
			mobileForlderNames+= eachMobileFolder+",";
		}
		mobileForlderNames = mobileForlderNames.substring(0, mobileForlderNames.length()-1);
		System.out.println("Creating Listings for "+mobileForlderNames);
		if(args[0].equals("SingleListing")){
			CreateSingleListings createSingleListings = new CreateSingleListings();
			//Creating Single Listings
			createSingleListings.createFlipkartListingsForManyPhone(mobileForlderNames.split(","));
			//Creating screen guards
			System.out.println("Creating Listings for Screen Guards");
			createSingleListings.createTemperedGlassListings(mobileForlderNames);
			//Extract Listings of All Sellers and write to File
			createSingleListings.extractListingsForAllSellers(mobileForlderNames);
		}else if(args[0].equals("SingleSeedsListing")) {
			
		}else if(args[0].equals("CreateComboImages")){
			String plusImage = (String) createListings.configProp.get("plusimage");
			String combosLimit = (String) createListings.configProp.get("cases-combos-limit");
			String maCombosLimit = (String) createListings.configProp.get("mobile-accessories-combos-limit");
			CreateCombosListings CreateCombosListings = new CreateCombosListings();
			CreateCombosListings.setPlusImage("Y".equals(plusImage) ? true : false);
			if(combosLimit != null && !"".equals(combosLimit)){
				CreateCombosListings.setCombosLimit(new Integer(combosLimit));
			}
			if(maCombosLimit != null && !"".equals(maCombosLimit)){
				CreateCombosListings.setmACombosLimit(new Integer(maCombosLimit));
			}
			if(args[1].equals("Covers")){
				CreateCombosListings.setCoversCombo(true);
			}else if(args[1].equals("MobileAccessories")){
				CreateCombosListings.setMACombo(true);
			}
			CreateCombosListings.createComboForOnePhoneNew(mobileForlderNames);
			System.out.println("Completed creating Combo Images.");
			System.out.println("Please delete improper images from Combos folder of each Mobile.");
		}else if(args[0].equals("CreateCombosListing")){
			CreateCombosListings CreateCombosListings = new CreateCombosListings();
			if(args[1].equals("Covers")){
				CreateCombosListings.setCoversCombo(true);
			}else if(args[1].equals("MobileAccessories")){
				CreateCombosListings.setMACombo(true);
			}
			CreateCombosListings.createCombosCatalog(mobileForlderNames);
			CreateCombosListings.extractListingsForAllSellers(mobileForlderNames);
		}
//		else if(args[0].equals("CreateComboImages")){
//			String plusImage = (String) createListings.configProp.get("plusimage");
//			CreateCombosListings CreateCombosListings = new CreateCombosListings();
//			CreateCombosListings.setPlusImage("Y".equals(plusImage) ? true : false);
//			CreateCombosListings.createComboForOnePhoneNew(mobileForlderNames);
//		}else if(args[0].equals("CreateCombosListing")){
//			String plusImage = (String) createListings.configProp.get("plusimage");
//			String combosLimit = (String) createListings.configProp.get("comboslimit");
//			CreateCombosListings CreateCombosListings = new CreateCombosListings();
//			CreateCombosListings.setPlusImage("Y".equals(plusImage) ? true : false);
//			if(!"".equals(combosLimit)){
//				CreateCombosListings.setCombosLimit(new Integer(combosLimit));
//			}
//			CreateCombosListings.createCombosCatalog(mobileForlderNames);
//		}
//		else if(args[0].equals("ExtractCombos")){
//			CreateCombosListings CreateCombosListings = new CreateCombosListings();
//			CreateCombosListings.extractListingsForAllSellers(mobileForlderNames);
//		}
	}

}
