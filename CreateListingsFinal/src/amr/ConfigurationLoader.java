package amr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;


public class ConfigurationLoader {
	

	private final static String  SKU_COUNT_PROPERTIES = "./code/zcore/skuCount.txt";
	private final static String  TEMPERED_COUNT_PROPERTIES = "./code/zcore/temperedSkuCount.txt";
	private final static String  COMBO_COUNT_PROPERTIES = "./code/zcore/comboCount.txt";
	
	public Properties loadProperties(String path) throws IOException{

	    //to load application's properties, we use this class
	    Properties mainProperties = new Properties();

	    FileInputStream file;

	    //load the file handle for main.properties
	    file = new FileInputStream(path);

	    //load all the properties from this file
	    mainProperties.load(file);

	    //we have loaded the properties, so close the file handle
	    file.close();


	    return mainProperties;
	}
	
	public Map<String, String> loadKeywords() throws IOException {
		Map<String, String> keywordsMap = new HashMap<>();
		Properties properties = loadProperties("./code/zcore/keywords-map.properties");
		if(properties.size() > 0){
			for (String key : properties.stringPropertyNames()) {
			    String value = properties.getProperty(key);
			    keywordsMap.put(key, value);
//			    System.out.println(key+"::"+value);
			}
		}
		return keywordsMap;
	}
	
	public Map<String, String> loadCaseTypes() throws IOException {
		Map<String, String> keywordsMap = new HashMap<>();
		Properties properties = loadProperties("./code/zcore/casetypes-map.properties");
		if(properties.size() > 0){
			for (String key : properties.stringPropertyNames()) {
			    String value = properties.getProperty(key);
			    keywordsMap.put(key, value);
//			    System.out.println(key+"::"+value);
			}
		}
		return keywordsMap;
	}
	
	public Map<String, String> loadColorMap() throws IOException {
		Map<String, String> keywordsMap = new HashMap<>();
		Properties properties = loadProperties("./code/zcore/color-map.properties");
		if(properties.size() > 0){
			for (String key : properties.stringPropertyNames()) {
			    String value = properties.getProperty(key);
			    keywordsMap.put(key, value);
//			    System.out.println(key+"::"+value);
			}
		}
		return keywordsMap;
	}
	
	public Map<String, String> loadModelMap() throws IOException {
		Map<String, String> keywordsMap = new HashMap<>();
		Properties properties = loadProperties("./code/zcore/model-map.properties");
		if(properties.size() > 0){
			for (String key : properties.stringPropertyNames()) {
			    String value = properties.getProperty(key);
			    keywordsMap.put(key, value);
//			    System.out.println(key+"::"+value);
			}
		}
		return keywordsMap;
	}
	
	public Map<String, String> loadSkuMap() throws IOException {
		Map<String, String> keywordsMap = new HashMap<>();
		Properties properties = loadProperties("./code/zcore/sku-map.properties");
		if(properties.size() > 0){
			for (String key : properties.stringPropertyNames()) {
			    String value = properties.getProperty(key);
			    keywordsMap.put(key, value);
//			    System.out.println(key+"::"+value);
			}
		}
		return keywordsMap;
	}
	
	public Map<String, String> loadComboSkuMap() throws IOException {
		Map<String, String> keywordsMap = new HashMap<>();
		Properties properties = loadProperties("./code/zcore/combo-sku-map.properties");
		if(properties.size() > 0){
			for (String key : properties.stringPropertyNames()) {
			    String value = properties.getProperty(key);
			    keywordsMap.put(key, value);
//			    System.out.println(key+"::"+value);
			}
		}
		return keywordsMap;
	}
	
	public Map<String, String> loaComboCaseTypes() throws IOException {
		Map<String, String> keywordsMap = new HashMap<>();
		Properties properties = loadProperties("./code/zcore/combo-casetypes-map.properties");
		if(properties.size() > 0){
			for (String key : properties.stringPropertyNames()) {
			    String value = properties.getProperty(key);
			    keywordsMap.put(key, value);
//			    System.out.println(key+"::"+value);
			}
		}
		return keywordsMap;
	}
	
	public Map<String, String> loadDescriptionMap() throws IOException {
		Map<String, String> keywordsMap = new HashMap<>();
		Properties properties = loadProperties("./code/zcore/description-map.properties");
		if(properties.size() > 0){
			for (String key : properties.stringPropertyNames()) {
			    String value = properties.getProperty(key);
			    keywordsMap.put(key, value);
//			    System.out.println(key+"::"+value);
			}
		}
		return keywordsMap;
	}
	
	public String[] loadbrandColors() throws IOException {
		Properties properties = loadProperties("./code/zcore/brand-colors.properties");
		if(properties.size() > 0){
			return properties.getProperty("brandcolors").split(",");
		}
		System.out.println("Brand Colors are missing");
		return null;
	}

	public int getLastSkuCount() throws FileNotFoundException, IOException {
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

	public int getLastComboSkuCount() throws FileNotFoundException, IOException {
		try(BufferedReader br = new BufferedReader(new FileReader(COMBO_COUNT_PROPERTIES))) {
		    String line = br.readLine();
		    if(line != null && line.contains("comboSkuCount=")){
		    	line = line.replace("comboSkuCount=", "");
		    	if(!line.isEmpty() && StringUtils.isNumeric(line)){
//		    		System.out.println("Last SKU created was : "+line);
		    		return new Integer(line).intValue() + 1;
		    	}
		    }
		}
		return -1;
	}

	public int getLastTemperedSkuCount() throws FileNotFoundException, IOException {
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

	public Properties loadProperties() throws IOException{

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

	public Properties loadImageShackProperties() throws IOException{

	    //to load application's properties, we use this class
	    Properties mainProperties = new Properties();

	    FileInputStream file;

	    //the base folder is ./, the root of the main.properties file  
	    String path = "./code/zcore/image-shack.properties";//"D:/AAA_WORK/Important Batch Files/Flipkart/Non-LIVE to LIVE/code/config.properties

	    //load the file handle for main.properties
	    file = new FileInputStream(path);

	    //load all the properties from this file
	    mainProperties.load(file);

	    //we have loaded the properties, so close the file handle
	    file.close();


	    return mainProperties;
	}

	public void updateSkuCount(int skuCount) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Updating the SKU count"+skuCount);
		FileUtils.writeStringToFile(new File(SKU_COUNT_PROPERTIES), "skuCount="+skuCount);
	}
	
	public void updateTemperedSkuCount(int temperedSkuStart) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Updating the Tempered SKU count"+temperedSkuStart);
		FileUtils.writeStringToFile(new File(TEMPERED_COUNT_PROPERTIES), "temperedSkuCount="+temperedSkuStart);
	}
}
