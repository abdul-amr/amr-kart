package amr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.sun.rowset.internal.Row;

import javafx.scene.control.Cell;

public class ImageShack {

//	private static final String IMAGESTACK_API_KEY = "238GKORTa6c41db5b47a6a8c2718df729b2e1ff9";
//	private static final String IMAGESTACK_USERNAME = "rma.retailers6";
//	private static final String IMAGESTACK_PASSWORD = "9700525711";
	
	private static final String IMAGESTACK_API_KEY = "47CJOPUW2a77d9042024c025bd3e9e2842bfb07a";
	private static final String IMAGESTACK_USERNAME = "shezan.listings1";
	private static final String IMAGESTACK_PASSWORD = "9618208283";
	
	
	private static final String IMAGESTACK_ALBUM_NAME = "Personal Collection";
	
	private Map<String, String> skuVsImageUrlMap = new HashMap<String, String>();
	private String errorMessage;
	private String authToken;
	private String photoshoppedImagesParentFolder;
	private boolean isLoggedIn = false;
	
	public ImageShack(String imagesParentFolder){
		super();
		this.photoshoppedImagesParentFolder = imagesParentFolder;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

	public Map<String, String> getSkuVsImageUrlMap() {
		return skuVsImageUrlMap;
	}

	public void setSkuVsImageUrlMap(Map<String, String> skuVsImageUrlMap) {
		this.skuVsImageUrlMap = skuVsImageUrlMap;
	}

	public String getPhotoshoppedImagesParentFolder() {
		return photoshoppedImagesParentFolder;
	}

	public void setPhotoshoppedImagesParentFolder(
			String photoshoppedImagesParentFolder) {
		this.photoshoppedImagesParentFolder = photoshoppedImagesParentFolder;
	}

	private List<String> imageShackUpload(String authToken, List<File> files, String albumName, String folderPath) {
		List<String> imageLinks = new ArrayList<>();
		try {
			String url = "https://api.imageshack.com/v2/images";
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost httppost = new HttpPost(url);
			httppost.addHeader("Authorization", "Bearer "+ImageShack.IMAGESTACK_API_KEY);
			
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			for(File file : files){
				builder.addBinaryBody("file[]", file);
			}
			builder.addTextBody("auth_token", authToken, ContentType.TEXT_PLAIN);//"1ad50d1fd3f55c7bc3d769731f259624"
			builder.addTextBody("key", ImageShack.IMAGESTACK_API_KEY, ContentType.TEXT_PLAIN);//"39BEJRSTfaf161b45e9d90da6c4ee6915d285c70"
			builder.addTextBody("album", albumName, ContentType.TEXT_PLAIN);
			builder.addPart("public", new StringBody("public", ContentType.TEXT_PLAIN));
			HttpEntity entity = builder.build();
		    httppost.setEntity(entity);
			HttpResponse response = client.execute(httppost);
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer resultString = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				resultString.append(line);
//				System.out.println("ImageShack Logs : "+line);
			}
			Gson gson = new Gson();
			ImageShackUploadResponse iSUResponse = gson.fromJson(resultString.toString(), ImageShackUploadResponse.class);
			Result result = iSUResponse.getResult();
			if(result != null && result.getImages() != null && result.getImages().size() > 0){
				List<Map<String, Object>> images = result.getImages();
				for(Map<String, Object> image : images){
					String directLink = (String) image.get("direct_link");
					imageLinks.add(convertImage(directLink));
				}
			}
//			saveDirectImageLinksInExcel();
		} catch (Exception e) {
			System.out.println("Exception in imageShackUpload method in folder :: "+folderPath);
			e.printStackTrace();
			System.out.println("\n\n Trying to upload again");
			imageLinks = imageShackUpload(authToken,files,albumName,folderPath);
		}
		return imageLinks;
	}
	
	public boolean imageShackLogin() throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		String url = "https://imageshack.com/rest_api/v2/user/login";
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httppost = new HttpPost(url);
		httppost.addHeader("Authorization", "Bearer "+ImageShack.IMAGESTACK_API_KEY);//"39BEJRSTfaf161b45e9d90da6c4ee6915d285c70"
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
	    postParameters.add(new BasicNameValuePair("username", ImageShack.IMAGESTACK_USERNAME));//"rma.retailers.images"
	    postParameters.add(new BasicNameValuePair("password", ImageShack.IMAGESTACK_PASSWORD));//"9700525711"
	    postParameters.add(new BasicNameValuePair("api_key", ImageShack.IMAGESTACK_API_KEY));
	    httppost.setEntity(new UrlEncodedFormEntity(postParameters));
	    
		HttpResponse response = client.execute(httppost);
		
//		System.out.println(response.toString());
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
//			System.out.println(line);
		}
		Gson gson = new Gson();
		Login login = gson.fromJson(result.toString(), Login.class);
		Map<String, Object> loginMap = login.getResult();
		if(login.getSuccess() && loginMap != null && loginMap.get("auth_token") != null){
			String authToken = loginMap.get("auth_token").toString();
			setAuthToken(authToken);
			setLoggedIn(true);
//			System.out.println("Successfully Logged in to IMAGESHACK : "+authToken);
			return true;
		}else{
			setErrorMessage(line);
			System.out.println("Failed to Login to IMAGESHACK");
			return false;
		}
	}
	
	public List<String> processImageUploadNew(String folderName,String mobileName) throws ClientProtocolException, IOException{
		
		List<String> directLinks =null;
		File folder = new File(folderName);
		if(!isLoggedIn()){
			imageShackLogin();
		}
		if(folder.exists()){
			File[] files = folder.listFiles();
			List<File> fileList = new ArrayList<>();
			for(File file : files){
				if(!file.isDirectory()){
					fileList.add(file);
				}
			}
			if(fileList.size() > 0){
				directLinks = imageShackUpload(getAuthToken(), fileList, mobileName,folder.getAbsolutePath());
			}
			if(directLinks == null || directLinks.size() != fileList.size()){
				System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n \t\t\t\t\tNOT ALL images are uploaded of folder : "+folderName+" ;  mobile name : "+mobileName+"\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
			}
		}
		return directLinks;
	}
	
	public List<String> processImageUploadForCombo(List<File> fileList,String mobileName,String folderPath) throws ClientProtocolException, IOException{
		
		List<String> directLinks =null;
		if(!isLoggedIn()){
			imageShackLogin();
		}
		if(fileList!=null && fileList.size() > 0){
			directLinks = imageShackUpload(getAuthToken(), fileList, mobileName,folderPath);
		}
		
		return directLinks;
	}

	public void processBulkUpload(String folderPath) {
		File parentDirectory = new File(folderPath);
		File[] files = parentDirectory.listFiles();
		List<File> fileList = new ArrayList<>();
		for(File file : files){
			if(file.isDirectory()){
				processBulkUpload(folderPath+"/"+file.getName());
			}else{
				fileList.add(file);
			}
		}
		if(fileList.size() > 0){
			List<String> directLinks = imageShackUpload(getAuthToken(), fileList, ImageShack.IMAGESTACK_ALBUM_NAME,folderPath);
			if(directLinks.size() > 0){
//				System.out.println(directLinks.toString());
				skuVsImageUrlMap.put(parentDirectory.getName(), StringUtils.join(directLinks, ','));
			}
		}
	}
	
	public void processUpload(String folderPath) throws ClientProtocolException, IOException {
		File parentDirectory = new File(folderPath);
		File[] files = parentDirectory.listFiles();
		for(File file : files){
			if(file.isDirectory()){
				processUpload(folderPath+"/"+file.getName());
			}else{
				List<File> fileList = new ArrayList<>();
				fileList.add(file);
				List<String> directLinks = imageShackUpload(getAuthToken(), fileList, ImageShack.IMAGESTACK_ALBUM_NAME,folderPath);
				if(directLinks.size() > 0){
					System.out.println("Success :: "+file.getName()+" :: "+directLinks.get(0));
					String existingDirectLinks = skuVsImageUrlMap.get(parentDirectory.getName());
					if(existingDirectLinks != null && !existingDirectLinks.isEmpty()){
						skuVsImageUrlMap.put(parentDirectory.getName(), existingDirectLinks+","+directLinks.get(0));
					}else{
						skuVsImageUrlMap.put(parentDirectory.getName(), directLinks.get(0));
					}
				}else{
					System.out.println("Failure :: "+file.getName()+" :: Failed to Upload");
				}
			}
		}
	}

//	public boolean saveDirectImageLinksInExcel() {
//		// TODO Auto-generated method stub
//		FileInputStream file = null;
//		Map<String, String> skuVsImageUrlMap = getSkuVsImageUrlMap();
//		FileOutputStream output_file = null;
//		XSSFWorkbook workbook = null;
//		try {
//			File directory = new File("D:\\AAA_WORK\\JARS\\POI Testing\\Template");
//			File[] files = directory.listFiles();
//			if(files!= null && files.length > 0){
//				file = new FileInputStream(files[0]);
//			    workbook = new XSSFWorkbook(file);
//			    XSSFSheet sheet = workbook.getSheet("Flipkart and SnapDeal Template");
//			    boolean flag = true;
//			    int rowPointer = 0;
//			    int imagIndex = -1;
//			    while(flag){
//			    	boolean isDone = (rowPointer-1)<skuVsImageUrlMap.size();
//			    	if(!isDone){
//			    		break;
//			    	}
//				    Row row = sheet.getRow(rowPointer);
//				    if(row != null){
//				    	Iterator<Cell> iterator = row.cellIterator();
//				    	int cellPointer = 0;
//				    	String skuId = null;
//				    	String directLinks = null;
//				    	while(cellPointer < 11){
//				    		Cell cell = row.getCell(cellPointer);
//				    		if(cell == null){
//				    			cell = row.createCell(cellPointer);
//				    		}
//				    		if(row.getRowNum() == 0 && cell.getStringCellValue().startsWith("Image")){
//				    			imagIndex = cellPointer;
//				    		}else if(cellPointer == 0){
//				    			skuId = cell.getStringCellValue();
//				    			if(skuId != null && !skuId.isEmpty()){
//				    				directLinks = skuVsImageUrlMap.get(skuId);
//				    			}
//				    		}else if(cellPointer == imagIndex && skuId != null && !skuId.isEmpty() && directLinks != null && !directLinks.isEmpty()){
//				    			cell.setCellValue(directLinks);
//				    		}
//				    		cellPointer++;
//				    	}
//				    }else{
//				    	break;
//				    }
//				    rowPointer++;
//			    }
//			    output_file =new FileOutputStream(files[0]);
//			    workbook.write(output_file);
//			    return true;
//			}
//		} catch (FileNotFoundException e) {
//		    e.printStackTrace();
//		} catch (IOException e) {
//		    e.printStackTrace();
//		}finally {
//			if(file != null){
//				try {
//					file.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			if(output_file != null){
//				try {
//					output_file.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			if(workbook != null){
//				try {
//					workbook.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		return false;
//	}

//	public boolean start() throws ClientProtocolException, IOException {
//		// TODO Auto-generated method stub
//		if(imageShackLogin()){
//			processBulkUpload(getPhotoshoppedImagesParentFolder());
////			processUpload(getPhotoshoppedImagesParentFolder());
//			if(getSkuVsImageUrlMap().size() > 0){
//				System.out.println("Successfully Uploaded the Image Files\nStarted saving direct Links in excel");
//				if(saveDirectImageLinksInExcel()){
//					System.out.println("Completed saving direct Links in excel");
//					return true;
//				}
//			}
//			return false;
//		}else{
//			System.out.println("IMAGESHACK Authentication FAILED ::  "+getErrorMessage());
//			return false;
//		}
//	}
	
	private String convertImage(String url){
		if(!StringUtils.isBlank(url)){
			url = url.replace("imagizer.", "http://");
			url = url.replace("http://imageshack.com", "http://imageshack.com/a");
		}
		return url;
	}
}
