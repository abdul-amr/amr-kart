package com.retailers.rma;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.google.gson.Gson;
import com.retailers.rma.imageshack.pojo.ImageShackUploadResponse;
import com.retailers.rma.imageshack.pojo.Result;
import com.retailers.rma.pojo.FlipkartListing;
import com.retailers.rma.pojo.UpdateFlipkartListingResponse;

class MobileCasesAndCovers {
	
	private final static String  SKU_COUNT_PROPERTIES = "src/com/retailers/rma/skuCount.txt";
	private final static String  COMBO_COUNT_PROPERTIES = "src/com/retailers/rma/comboCount.txt";
	private final static String  TEMPERED_COUNT_PROPERTIES = "src/com/retailers/rma/temperedSkuCount.txt";
	private final static String  PAYTM_VALID_MOBILES = "src/com/retailers/rma/paytm_mobile_list.txt";
	
	private Map<String, String> caseTypesMap;
	private Map<String, String> skuStringMap;
	private Map<String, String> modelMap;
	private Map<String, String> keywordsMap;
	private Map<String, String> colorMap;
	private Map<String, String> descriptionMap;
	private Map<String, String> pricingMap;
	private Map<String, String> pricingLevelMap;
	private Map<String, String> amazonBuletPOintsMap;
	private Map<String, String> accountVsBrandNames;
	private Map<String, String> accountVsSkus;
	private String[] brandColors;
	private int skuStart;
	private int temperedSkuStart;
	private int comboCount;
	private Map<String, String> comboCaseTypes;
	private Map<String, String> comboSKUMap;
	private int comboSkuStart;
	private String token = "db6f2e02-5cb6-4ef3-86fa-38f5d31cbc96";
	
	public MobileCasesAndCovers() throws Exception {	
		caseTypesMap = loadCaseTypes();
		keywordsMap = loadKeywords();
		skuStringMap = loadSkuMap();
		modelMap = loadModelMap();
		colorMap = loadColorMap();
		descriptionMap = loadDescriptionMap();
		pricingMap = loadPricingMap();
		pricingLevelMap = loadPricingLevelMap();
		amazonBuletPOintsMap = loadAmazonBulletPoints();
		comboSKUMap = loadComboSkuMap();
		comboCaseTypes = loaComboCaseTypes();
		accountVsBrandNames = loadBrandNames();
		accountVsSkus = loadSkus();
		brandColors = "Shady,Arc,Zync,Shiny,Smooth,Soft,Pure,Crystal,Brash,Ablaze,Dusty,Flamboyant,Gaily,Iridescent,Mellow,Pastel,Sepia,Splashy,Vivid".split(",");
		
		this.skuStart= getLastSkuCount();
		this.comboCount = 0;
		this.comboSkuStart = getLastComboSkuCount();
		this.temperedSkuStart = getLastTemperedSkuCount();
		
		
		if(skuStart == -1 || comboSkuStart == -1){
			throw new Exception("Last updated SKU count is incorrect");
		}
		
		this.skuStart += 2;
		this.comboSkuStart += 2;
	}

	public static void main(String[] args) throws Exception {
		
		MobileCasesAndCovers thisObject = new MobileCasesAndCovers();
		
//		thisObject.activateListings("");
//		thisObject.updateFlipkartListings("mzt-tp-chr-a16091");//
		
		String[] multiplePhones = "D:/Mobile Cases/Xiaomi/Redmi Note 5 Pro,D:/Mobile Cases/Xiaomi/Mi Redmi Note 5 Pro,D:/Mobile Cases/Xiaomi/Xiaomi Redmi Note 5 Pro".split(",");//".split(",");
		thisObject.createFlipkartListingsForManyPhone(multiplePhones);
		
		thisObject.extractSpecificCatalog("D:/Mobile Cases/Xiaomi/Redmi Note 5 Pro,D:/Mobile Cases/Xiaomi/Mi Redmi Note 5 Pro,D:/Mobile Cases/Xiaomi/Xiaomi Redmi Note 5 Pro",
//				"Tempered Glass,Screen Guard,Privacy Screen Guard");
				"Back Cover,Back Replacement Cover_Differnet Brand Color,Bumper Case_Differnet Brand Color,Flip Cover,Front & Back Case_Differnet Brand Color,Book Cover_Differnet Brand Color");
		
		
		thisObject.extractSpecificCatalogForOtherSellers("TRAM",
				"D:/Mobile Cases/Xiaomi/Redmi Note 5 Pro,D:/Mobile Cases/Xiaomi/Mi Redmi Note 5 Pro,D:/Mobile Cases/Xiaomi/Xiaomi Redmi Note 5 Pro",
//				"Tempered Glass,Screen Guard,Privacy Screen Guard");
				"Back Cover,Back Replacement Cover_Differnet Brand Color,Bumper Case_Differnet Brand Color,Flip Cover,Front & Back Case_Differnet Brand Color,Book Cover_Differnet Brand Color");

//		8886417634
		String folders = "D:/Mobile Cases/Xiaomi/Redmi Note 5 Pro,D:/Mobile Cases/Xiaomi/Mi Redmi Note 5 Pro,D:/Mobile Cases/Xiaomi/Xiaomi Redmi Note 5 Pro";
		thisObject.createComboForOnePhoneNew(folders);
		thisObject.createCombosCatalog(folders);
		thisObject.createFinalComboListing(folders);
		thisObject.extractCombosCatalogForOtherSellers("TRAM","D:/Mobile Cases/Xiaomi/Mi Redmi Note 5,D:/Mobile Cases/Xiaomi/Xiaomi Redmi Note 5");
//		
		thisObject.createMobileAccessoriesCombo("MAR","D:/Mobile Cases/Xiaomi/Mi Redmi Note 5,D:/Mobile Cases/Xiaomi/Xiaomi Redmi Note 5");
//
		thisObject.createTemperedGlassListings("D:/Mobile Cases/Xiaomi/Redmi Note 5 Pro,D:/Mobile Cases/Xiaomi/Mi Redmi Note 5 Pro,D:/Mobile Cases/Xiaomi/Xiaomi Redmi Note 5 Pro");//
//		
//		thisObject.getPriceList("ZOTIKOS Flip Cover for Lenovo K8 Note (Gold, Rubber, Plastic):zot-fcg-a65791;ZOTIKOS Front & Back Case for Lenovo K6 Power (Gold, Rubber, Plastic):zot-fcg-c32302;ZOTIKOS Back Cover for Samsung C9 Pro (Transparent, Rubber, Plastic):zot-tp-c39102;ZOTIKOS Back Cover for Samsung Z2 (Transparent, Rubber, Plastic):zot-tp-c30869;ZOTIKOS Back Replacement Cover for OPPO F3 (Black, Rubber, Plastic):zot-chr-c55347;ZOTIKOS Back Cover for Samsung Galaxy J1 ace (Black, Rubber, Plastic):zot-chr-c2005;ZOTIKOS Back Replacement Cover for VIVO V5S (Mellow Black, Rubber, Plastic):zot-chr-c55804;ZOTIKOS Bumper Case for SAMSUNG Galaxy S8 Plus (Black, Rubber, Plastic):zot-def-c56565;ZOTIKOS Back Replacement Cover for SAMSUNG GALAXY J7 MAX (Black, Rubber, Plastic):zot-def-c61665;ZOTIKOS Back Cover for SAMSUNG GALAXY J7 Pro (Transparent, Rubber, Plastic):zot-tp-c64678;ZOTIKOS Back Cover for SAMSUNG Galaxy S8 Plus (Transparent, Rubber, Plastic):zot-tp-c56668;ZOTIKOS Flip Cover for SAMSUNG Galaxy J3 Pro (Gold, Rubber, Plastic):zot-fcg-c59116;ZOTIKOS Back Cover for Motorola Moto G5 Plus (Black, Rubber, Plastic):zot-chr-c39290;ZOTIKOS Flip Cover for Lenovo K6 Power (Black, Rubber, Plastic):zot-fcb-c32112;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 (Transparent, Rubber, Plastic):zot-tp-c56473;ZOTIKOS Back Cover for Motorola Moto M (Transparent, Rubber, Plastic):zot-tp-c12812;ZOTIKOS Back Cover for VIVO V5S (Black, Rubber, Plastic):zot-chr-c55688;ZOTIKOS Back Cover for Infinix Hot 4 Pro (Arc Black, Rubber, Plastic):zot-def-a65137;ZOTIKOS Back Cover for Samsung Galaxy J5 Prime (Black, Rubber, Plastic):zot-chr-c25441;ZOTIKOS Back Cover for Nokia 3 (Black, Rubber, Plastic):zot-def-c59588;ZOTIKOS Flip Cover for VIVO Y53 (Gold, Rubber, Plastic):zot-fcg-c49737;ZOTIKOS Back Replacement Cover for OPPO F3 (Black, Rubber, Plastic):zot-chr-c55354;ZOTIKOS Back Cover for SAMSUNG Galaxy S8 (Transparent, Rubber, Plastic):zot-tp-c56341;ZOTIKOS Back Cover for OPPO A37 (Gold, Rubber, Plastic):zot-crm-c51941;ZOTIKOS Back Cover for LG Q6 (Gaily Black, Rubber, Plastic):zot-chr-a66023;ZOTIKOS Back Cover for VIVO V5 Plus (Black, Rubber, Plastic):zot-chr-c48169;ZOTIKOS Back Cover for Samsung Galaxy J2 Ace (Black, Rubber, Plastic):zot-def-c43632;ZOTIKOS Back Cover for Samsung Galaxy J2 (Black, Rubber, Plastic):zot-def-c2800;ZOTIKOS Back Cover for Lenovo K8 Note (Gold, Rubber, Plastic):zot-crm-a65520;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Shady Black, Rubber, Plastic):zot-fcb-a65169;ZOTIKOS Front & Back Case for Samsung Galaxy J1 (4G) (Dusty Gold, Rubber, Plastic):zot-fcg-c43068;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 (Black, Rubber, Plastic):zot-def-c61348;ZOTIKOS Flip Cover for Xiaomi Redmi 2 Prime (Black Flip Cover, Rubber, Plastic):zot-fcb-c15947;ZOTIKOS Back Cover for VIVO V5S (Gold, Rubber, Plastic):zot-crm-c55809;ZOTIKOS Back Cover for VIVO Y66 (Black, Rubber, Plastic):zot-chr-c50905;ZOTIKOS Bumper Case for MOTOROLA MOTO E4 PLUS (Black, Rubber, Plastic):zot-def-c61045;ZOTIKOS Back Cover for Redmi Note 4 (Transparent Back Cover, Rubber, Plastic):zot-tp-c14281;ZOTIKOS Back Replacement Cover for Huawei Honor 8 Lite (Transparent, Rubber, Plastic):zot-tp-c60311;ZOTIKOS Back Cover for VIVO V5 Plus (Gold, Rubber, Plastic):zot-crm-c48400;ZOTIKOS Front & Back Case for Samsung Galaxy J2 Ace (Gold, Rubber, Plastic):zot-fcg-c43974;ZOTIKOS Back Cover for SAMSUNG Galaxy J3 Pro (Transparent, Rubber, Plastic):zot-tp-c59179;ZOTIKOS Flip Cover for MOTOROLA MOTO C (Gold, Rubber, Plastic):zot-fcg-c58271;ZOTIKOS Book Cover for SAMSUNG Galaxy S8 (Shiny Gold, Rubber, Plastic):zot-fcg-c56314;ZOTIKOS Front & Back Case for Samsung Galaxy J1 ace (Black, Rubber, Plastic):zot-fcb-c2218;ZOTIKOS Back Cover for Samsung C9 Pro (Gold, Rubber, Plastic):zot-crm-c38704;ZOTIKOS Back Cover for LG Q6 (Black, Rubber, Plastic):zot-def-a66046;ZOTIKOS Back Cover for Google Pixel XL (Black, Rubber, Plastic):zot-def-c35209;ZOTIKOS Back Cover for OnePlus 5 (Transparent, Rubber, Plastic):zot-tp-c60859;ZOTIKOS Back Replacement Cover for OPPO F3 (Transparent, Rubber, Plastic):zot-tp-c55646;ZOTIKOS Flip Cover for Oppo F3 Plus (Gold, Rubber, Plastic):zot-fcg-c54217;ZOTIKOS Flip Cover for Lenovo K8 Note (Shady Gold, Rubber, Plastic):zot-fcg-a65847;ZOTIKOS Back Cover for VIVO V5S (Black, Rubber, Plastic):zot-chr-c55690;ZOTIKOS Front & Back Case for SAMSUNG Galaxy J3 Pro (Black, Rubber, Plastic):zot-fcb-c59072;ZOTIKOS Book Cover for Infinix Hot 4 Pro (Arc Black, Rubber, Plastic):zot-fcb-a65188;ZOTIKOS Back Replacement Cover for MOTOROLA Z2 PLAY (Black, Rubber, Plastic):zot-def-c59329;ZOTIKOS Front & Back Case for VIVO Y53 (Black, Rubber, Plastic):zot-fcb-c49629;ZOTIKOS Book Cover for OnePlus 5 (Arc Gold, Rubber, Plastic):zot-fcg-c60841;ZOTIKOS Back Replacement Cover for Lenovo K8 Note (Shiny Black, Rubber, Plastic):zot-chr-a65501;ZOTIKOS Flip Cover for Lenovo K8 Note (Crystal Gold, Rubber, Plastic):zot-fcg-a65854;ZOTIKOS Back Cover for LG Q6 (Black, Rubber, Plastic):zot-chr-a65955;ZOTIKOS Back Cover for VIVO Y53 (Gold, Rubber, Plastic):zot-crm-c49299;ZOTIKOS Back Cover for Samsung Galaxy J1 (4G) (Transparent, Rubber, Plastic):zot-tp-c43196;ZOTIKOS Bumper Case for SAMSUNG GALAXY ON MAX (Black, Rubber, Plastic):zot-def-c62017;ZOTIKOS Back Cover for VIVO V5 Plus (Transparent, Rubber, Plastic):zot-tp-c48874;ZOTIKOS Bumper Case for MOTOROLA MOTO E4 (Black, Rubber, Plastic):zot-def-c61379;ZOTIKOS Flip Cover for SAMSUNG GALAXY J7 Pro (Black, Rubber, Plastic):zot-fcb-c64583;ZOTIKOS Back Cover for SAMSUNG Galaxy J3 Pro (Black, Rubber, Plastic):zot-chr-c58884;ZOTIKOS Back Cover for Samsung Galaxy J7 (Transparent Back Cover, Rubber, Plastic):zot-tp-c4007;ZOTIKOS Back Replacement Cover for Yu Yunique 2 (Soft Black, Rubber, Plastic):zot-def-c64981;ZOTIKOS Flip Cover for Honor 6x (Black, Rubber, Plastic):zot-fcb-c35919;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 PLUS (Transparent, Rubber, Plastic):zot-tp-c61213;ZOTIKOS Back Cover for MOTOROLA MOTO C (Transparent, Rubber, Plastic):zot-tp-c58328;ZOTIKOS Back Cover for Lenovo P2 (Transparent, Rubber, Plastic):zot-tp-c35662;ZOTIKOS Front & Back Case for VIVO Y66 (Black, Rubber, Plastic):zot-fcb-c51460;ZOTIKOS Back Cover for LG Q6 (Black, Rubber, Plastic):zot-chr-a65962;ZOTIKOS Front & Back Case for Oppo F3 Plus (Shiny Gold, Rubber, Plastic):zot-fcg-c54236;ZOTIKOS Front & Back Case for Samsung Galaxy A5 (2016) (Black, Rubber, Plastic):zot-fcb-c29873;ZOTIKOS Back Cover for OnePlus 3 (Transparent, Rubber, Plastic):zot-tp-c33919;ZOTIKOS Back Cover for Oppo F1s (Black, Rubber, Plastic):zot-chr-c52874;ZOTIKOS Back Replacement Cover for Nokia 3 (Black, Rubber, Plastic):zot-def-c59603;ZOTIKOS Back Replacement Cover for SAMSUNG GALAXY J7 Pro (Transparent, Rubber, Plastic):zot-tp-c64712;ZOTIKOS Back Cover for Google Pixel XL (Transparent, Rubber, Plastic):zot-tp-c35345;ZOTIKOS Back Cover for Lenovo K6 Power (Transparent, Rubber, Plastic):zot-tp-c32411;ZOTIKOS Back Replacement Cover for SAMSUNG GALAXY ON MAX (Pastel Transparent, Rubber, Plastic):zot-tp-c62228;ZOTIKOS Back Cover for Yu Yunique 2 (Transparent, Rubber, Plastic):zot-tp-c65094;ZOTIKOS Flip Cover for OnePlus 3 (Gold, Rubber, Plastic):zot-fcg-c33829;ZOTIKOS Back Cover for Oppo F1s (Transparent, Rubber, Plastic):zot-tp-c53525;ZOTIKOS Back Cover for Samsung Galaxy J7 (Black, Rubber, Plastic):zot-def-cc3577;ZOTIKOS Back Cover for Samsung Galaxy J2 (Transparent Back Cover, Rubber, Plastic):zot-tp-c3270;ZOTIKOS Back Cover for SAMSUNG GALAXY J7 Pro (Black, Rubber, Plastic):zot-def-c64537;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 (Transparent, Rubber, Plastic):zot-tp-c61554;ZOTIKOS Flip Cover for Lenovo K8 Note (Zync Black, Rubber, Plastic):zot-fcb-a65760;ZOTIKOS Back Cover for Lenovo A6600 Plus (Transparent, Rubber, Plastic):zot-tp-c36254;ZOTIKOS Front & Back Case for Lenovo K6 Note (Black, Rubber, Plastic):zot-fcb-c31296;ZOTIKOS Flip Cover for Lenovo K8 Note (Gold, Rubber, Plastic):zot-fcg-a65784;ZOTIKOS Book Cover for MOTOROLA MOTO E4 PLUS (Shady Black, Rubber, Plastic):zot-fcb-c61096;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Shiny Gold, Rubber, Plastic):zot-fcg-a65234;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 (Transparent, Rubber, Plastic):zot-tp-c56454;ZOTIKOS Bumper Case for VIVO V5S (Soft Black, Rubber, Plastic):zot-def-c55954;ZOTIKOS Back Cover for MOTOROLA MOTO C PLUS (Transparent, Rubber, Plastic):zot-tp-c58652;ZOTIKOS Flip Cover for Samsung Galaxy On Nxt (Gold, Rubber, Plastic):zot-fcg-c44691;ZOTIKOS Front & Back Case for Lenovo K6 Power (Black, Rubber, Plastic):zot-fcb-c32107;ZOTIKOS Flip Cover for MOTOROLA MOTO C PLUS (Black, Rubber, Plastic):zot-fcb-c58552;ZOTIKOS Flip Cover for VIVO Y53 (Black, Rubber, Plastic):zot-fcb-c49587;ZOTIKOS Flip Cover for Infinix Note 4 (Black, Rubber, Plastic):zot-fcb-a65337;ZOTIKOS Back Replacement Cover for Infinix Note 4 (Arc Black, Rubber, Plastic):zot-chr-a65293;ZOTIKOS Back Cover for SAMSUNG GALAXY ON MAX (Transparent, Rubber, Plastic):zot-tp-c62149;ZOTIKOS Book Cover for Lenovo K8 Note (Shady Gold, Rubber, Plastic):zot-fcg-a65908;ZOTIKOS Flip Cover for VIVO Y55L (Black, Rubber, Plastic):zot-fcb-c50508;ZOTIKOS Back Cover for Oppo F3 Plus (Black, Rubber, Plastic):zot-chr-c53641;ZOTIKOS Flip Cover for Redmi Note 4 (Black, Rubber, Plastic):zot-fcb-c13986;ZOTIKOS Front & Back Case for Samsung Galaxy J1 (4G) (Gold, Rubber, Plastic):zot-fcg-c43061;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Gold, Rubber, Plastic):zot-fcg-a65201;ZOTIKOS Book Cover for SAMSUNG Galaxy S8 Plus (Smooth Black, Rubber, Plastic):zot-fcb-c56614;ZOTIKOS Back Cover for VIVO V5 (Black, Rubber, Plastic):zot-chr-c46343;ZOTIKOS Flip Cover for VIVO V5 Plus (Black, Rubber, Plastic):zot-fcb-c48630;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 (Transparent, Rubber, Plastic):zot-tp-c56464;ZOTIKOS Back Cover for Lenovo Vibe K5 Note (Black, Rubber, Plastic):zot-chr-c54416;ZOTIKOS Bumper Case for MOTOROLA MOTO E4 (Brash Black, Rubber, Plastic):zot-def-c61377;ZOTIKOS Back Cover for VIVO Y66 (Black, Rubber, Plastic):zot-chr-c50914;ZOTIKOS Front & Back Case for Samsung Galaxy J7 (Zync Black Front & Back Case, Rubber, Plastic):zot-fcb-c3751;ZOTIKOS Back Cover for Samsung Galaxy J1 (4G) (Black, Rubber, Plastic):zot-def-c42726;ZOTIKOS Back Cover for VIVO Y66 (Gold, Rubber, Plastic):zot-crm-c51133;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy J3 Pro (Smooth Transparent, Rubber, Plastic):zot-tp-c59211;ZOTIKOS Back Cover for VIVO Y55L (Transparent, Rubber, Plastic):zot-tp-c50733;ZOTIKOS Front & Back Case for Infinix Note 4 (Soft Gold, Rubber, Plastic):zot-fcg-a65408;ZOTIKOS Back Replacement Cover for Yu Yunique 2 (Pure Transparent, Rubber, Plastic):zot-tp-c65117;ZOTIKOS Back Cover for VIVO Y53 (Transparent, Rubber, Plastic):zot-tp-c49818;ZOTIKOS Flip Cover for Samsung Galaxy J7 Prime (Black Flip Cover, Rubber, Plastic):zot-fcb-c5240;ZOTIKOS Back Cover for Infinix Hot 4 Pro (Shady Black, Rubber, Plastic):zot-chr-a65128;ZOTIKOS Front & Back Case for SAMSUNG GALAXY ON MAX (Black, Rubber, Plastic):zot-fcb-c62060;ZOTIKOS Front & Back Case for MOTOROLA MOTO E4 PLUS (Black, Rubber, Plastic):zot-fcb-c61086;ZOTIKOS Front & Back Case for Samsung Galaxy J2 Ace (Black, Rubber, Plastic):zot-fcb-c43801;ZOTIKOS Back Replacement Cover for MOTOROLA Z2 PLAY (Flamboyant Transparent, Rubber, Plastic):zot-tp-c59521;ZOTIKOS Front & Back Case for SAMSUNG Z4 (Gold, Rubber, Plastic):zot-fcg-c60414;ZOTIKOS Back Cover for OnePlus 3 (Black, Rubber, Plastic):zot-def-c33667;ZOTIKOS Flip Cover for Samsung Galaxy J1 (4G) (Gold, Rubber, Plastic):zot-fcg-c43023;ZOTIKOS Back Cover for LG Q6 (Smooth Transparent, Rubber, Plastic):zot-tp-a66262;ZOTIKOS Front & Back Case for Samsung Galaxy J2 (Black, Rubber, Plastic):zot-fcb-c2962;ZOTIKOS Flip Cover for Lenovo K8 Note (Dusty Gold, Rubber, Plastic):zot-fcg-a65857;ZOTIKOS Book Cover for MOTOROLA MOTO C PLUS (Brash Black, Rubber, Plastic):zot-fcb-c58603;ZOTIKOS Back Cover for Motorola Moto G5 Plus (Gold, Rubber, Plastic):zot-crm-c39374;ZOTIKOS Back Cover for Honor 6x (Transparent, Rubber, Plastic):zot-tp-c36048;ZOTIKOS Front & Back Case for SAMSUNG GALAXY ON MAX (Brash Black, Rubber, Plastic):zot-fcb-c62065;ZOTIKOS Back Replacement Cover for OPPO F3 (Black, Rubber, Plastic):zot-chr-c55349;ZOTIKOS Back Cover for VIVO V5 Plus (Black, Rubber, Plastic):zot-def-c48510;ZOTIKOS Back Cover for Huawei Honor 8 Lite (Transparent, Rubber, Plastic):zot-tp-c60263;ZOTIKOS Flip Cover for Lenovo K6 Note (Gold, Rubber, Plastic):zot-fcg-c31446;ZOTIKOS Book Cover for MOTOROLA MOTO E4 PLUS (Zync Black, Rubber, Plastic):zot-fcb-c61098;ZOTIKOS Back Cover for Oppo F3 Plus (Black, Rubber, Plastic):zot-chr-c53653;ZOTIKOS Back Cover for Honor 6x (Black, Rubber, Plastic):zot-def-c35839;ZOTIKOS Back Cover for LG Q6 (Zync Black, Rubber, Plastic):zot-chr-a66013;ZOTIKOS Back Cover for Samsung Galaxy J1 (4G) (Gold, Rubber, Plastic):zot-crm-c42593;ZOTIKOS Flip Cover for Lenovo K8 Note (Black, Rubber, Plastic):zot-fcb-a65635;ZOTIKOS Front & Back Case for Redmi Note 4 (Black, Rubber, Plastic):zot-fcb-c14028;ZOTIKOS Flip Cover for VIVO V5 (Black, Rubber, Plastic):zot-fcb-c46848;ZOTIKOS Back Cover for Infinix Note 4 (Transparent, Rubber, Plastic):zot-tp-a65430;ZOTIKOS Back Cover for Samsung Galaxy J7 (Black, Rubber, Plastic):zot-def-cc3575;ZOTIKOS Flip Cover for Lenovo P2 (Black, Rubber, Plastic):zot-fcb-c35505;ZOTIKOS Back Cover for OnePlus 3 (Black, Rubber, Plastic):zot-chr-c33523;ZOTIKOS Front & Back Case for Samsung Galaxy J7 Prime (Crystal Black Front & Back Case, Rubber, Plastic):zot-fcb-c5276;ZOTIKOS Bumper Case for Yu Yunique 2 (Black, Rubber, Plastic):zot-def-c65002;ZOTIKOS Back Cover for Infinix Note 4 (Black, Rubber, Plastic):zot-chr-a65279;ZOTIKOS Back Replacement Cover for Yu Yunique 2 (Arc Black, Rubber, Plastic):zot-def-c64977;ZOTIKOS Front & Back Case for OnePlus 5 (Black, Rubber, Plastic):zot-fcb-c60785;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 PLUS (Black, Rubber, Plastic):zot-chr-c60937;ZOTIKOS Back Cover for LG Q6 (Black, Rubber, Plastic):zot-def-a66037;ZOTIKOS Back Cover for OnePlus 2 (Gold, Rubber, Plastic):zot-crm-c33198;ZOTIKOS Flip Cover for Lenovo K8 Note (Smooth Black, Rubber, Plastic):zot-fcb-a65762;ZOTIKOS Back Cover for SAMSUNG Galaxy J3 Pro (Black, Rubber, Plastic):zot-def-c58918;ZOTIKOS Back Replacement Cover for SAMSUNG GALAXY J7 MAX (Gold, Rubber, Plastic):zot-crm-c61625;ZOTIKOS Back Cover for VIVO V5 (Black, Rubber, Plastic):zot-chr-c46355;ZOTIKOS Back Cover for VIVO Y53 (Black, Rubber, Plastic):zot-chr-c49099;ZOTIKOS Back Replacement Cover for MOTOROLA Z2 PLAY (Black, Rubber, Plastic):zot-def-c59327;ZOTIKOS Back Replacement Cover for Infinix Note 4 (Pure Transparent, Rubber, Plastic):zot-tp-a65454;ZOTIKOS Front & Back Case for Samsung Galaxy J1 (4G) (Arc Gold, Rubber, Plastic):zot-fcg-c43059;ZOTIKOS Back Cover for VIVO Y55L (Transparent, Rubber, Plastic):zot-tp-c50726;ZOTIKOS Back Cover for OnePlus 5 (Transparent, Rubber, Plastic):zot-tp-c60852;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Pure Gold, Rubber, Plastic):zot-fcg-a65237;ZOTIKOS Back Cover for Oppo F3 Plus (Black, Rubber, Plastic):zot-def-c53968;ZOTIKOS Back Cover for Samsung Galaxy On Nxt (Black, Rubber, Plastic):zot-chr-c44246;ZOTIKOS Flip Cover for SAMSUNG Galaxy S8 Plus (Black, Rubber, Plastic):zot-fcb-c56570;ZOTIKOS Book Cover for SAMSUNG Galaxy S8 (Black, Rubber, Plastic):zot-fcb-c56272;ZOTIKOS Flip Cover for Lenovo K8 Note (Black, Rubber, Plastic):zot-fcb-a65630;ZOTIKOS Back Replacement Cover for VIVO V5S (Transparent, Rubber, Plastic):zot-tp-c56123;ZOTIKOS Back Cover for MICROMAX EVOK NOTE E453 (Transparent, Rubber, Plastic):zot-tp-c60706;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Smooth Black, Rubber, Plastic):zot-fcb-a65173;ZOTIKOS Back Cover for Lenovo K6 Note (Transparent, Rubber, Plastic):zot-tp-c31593;ZOTIKOS Front & Back Case for Samsung Z2 (Gold, Rubber, Plastic):zot-fcg-c30847;ZOTIKOS Flip Cover for Infinix Note 4 (Gold, Rubber, Plastic):zot-fcg-a65388;ZOTIKOS Back Cover for MOTOROLA MOTO E4 (Black, Rubber, Plastic):zot-def-c61290;ZOTIKOS Back Cover for Samsung Galaxy On Nxt (Black, Rubber, Plastic):zot-def-c44493;ZOTIKOS Back Cover for Lenovo K6 Power (Transparent, Rubber, Plastic):zot-tp-c32407;ZOTIKOS Back Cover for Redmi 3S Prime (Black, Rubber, Plastic):zot-chr-c10019;ZOTIKOS Front & Back Case for Redmi 3S Prime (Flamboyant Black, Rubber, Plastic):zot-fcb-c10413;ZOTIKOS Front & Back Case for SAMSUNG GALAXY J7 Pro (Smooth Gold, Rubber, Plastic):zot-fcg-c64651;ZOTIKOS Front & Back Case for Samsung Galaxy J1 ace (Zync Gold Front & Back Case, Rubber, Plastic):zot-fcg-c2369;ZOTIKOS Back Cover for LG Q6 (Transparent, Rubber, Plastic):zot-tp-a66236;ZOTIKOS Back Cover for Oppo F3 Plus (Black, Rubber, Plastic):zot-chr-c53660;ZOTIKOS Back Cover for Lenovo K8 Note (Brash Gold, Rubber, Plastic):zot-crm-a65553;ZOTIKOS Back Cover for VIVO Y55L (Transparent, Rubber, Plastic):zot-tp-c50741;ZOTIKOS Back Replacement Cover for VIVO V5S (Transparent, Rubber, Plastic):zot-tp-c56129;ZOTIKOS Front & Back Case for Samsung Galaxy J3 (2016) (Black, Rubber, Plastic):zot-fcb-c25050;ZOTIKOS Back Cover for iPhone 7 (Gold, Rubber, Plastic):zot-crm-c16775;ZOTIKOS Back Cover for SAMSUNG GALAXY J7 Pro (Black, Rubber, Plastic):zot-def-c64536;ZOTIKOS Back Replacement Cover for SAMSUNG GALAXY J7 MAX (Transparent, Rubber, Plastic):zot-tp-c61887;ZOTIKOS Back Replacement Cover for OPPO F3 (Black, Rubber, Plastic):zot-chr-c55336;ZOTIKOS Flip Cover for Samsung Galaxy J7 Prime (Gold Flip Cover, Rubber, Plastic):zot-fcg-c5364;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 Plus (Transparent, Rubber, Plastic):zot-tp-c56807;ZOTIKOS Flip Cover for Samsung Z2 (Gold, Rubber, Plastic):zot-fcg-c30836;ZOTIKOS Back Cover for Infinix Hot 4 Pro (Transparent, Rubber, Plastic):zot-tp-a65241;ZOTIKOS Flip Cover for Redmi 3S Prime (Black, Rubber, Plastic):zot-fcb-c10373;ZOTIKOS Back Cover for Samsung Galaxy J2 Ace (Gold, Rubber, Plastic):zot-crm-c43502;ZOTIKOS Flip Cover for Infinix Note 4 (Soft Black, Rubber, Plastic):zot-fcb-a65379;ZOTIKOS Flip Cover for Redmi Note 4 (Black, Rubber, Plastic):zot-fcb-c13985;ZOTIKOS Front & Back Case for Redmi Note 4 (Crystal Gold, Rubber, Plastic):zot-fcg-c14188;ZOTIKOS Flip Cover for Lenovo K8 Note (Gold, Rubber, Plastic):zot-fcg-a65803;ZOTIKOS Back Cover for Samsung Galaxy J7 (Black Back Cover, Rubber, Plastic):zot-chr-c3415;ZOTIKOS Back Cover for MOTOROLA MOTO E4 PLUS (Transparent, Rubber, Plastic):zot-tp-c61147;ZOTIKOS Book Cover for LG Q6 (Smooth Black, Rubber, Plastic):zot-fcb-a66182;ZOTIKOS Front & Back Case for Lenovo K8 Note (Vivid Gold, Rubber, Plastic):zot-fcg-a65865;ZOTIKOS Back Cover for Samsung Galaxy J5 Prime (Black, Rubber, Plastic):zot-def-c25521;ZOTIKOS Flip Cover for Infinix Note 4 (Arc Gold, Rubber, Plastic):zot-fcg-a65404;ZOTIKOS Front & Back Case for Infinix Note 4 (Smooth Black, Rubber, Plastic):zot-fcb-a65362;ZOTIKOS Front & Back Case for Lenovo K8 Note (Soft Black, Rubber, Plastic):zot-fcb-a65711;ZOTIKOS Front & Back Case for Samsung Galaxy J7 Prime (Gold, Rubber, Plastic):zot-fcg-c5388;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO C PLUS (Arc Black, Rubber, Plastic):zot-chr-c58436;ZOTIKOS Flip Cover for VIVO V5 Plus (Black, Rubber, Plastic):zot-fcb-c48634;ZOTIKOS Back Cover for Samsung Galaxy J2 (Transparent, Rubber, Plastic):zot-tp-c3283;ZOTIKOS Back Cover for Infinix Note 4 (Black, Rubber, Plastic):zot-def-a65302;ZOTIKOS Back Replacement Cover for MOTOROLA Z2 PLAY (Transparent, Rubber, Plastic):zot-tp-c59510;ZOTIKOS Flip Cover for Infinix Note 4 (Pure Gold, Rubber, Plastic):zot-fcg-a65409;ZOTIKOS Back Replacement Cover for VIVO V5S (Pastel Black, Rubber, Plastic):zot-chr-c55805;ZOTIKOS Back Cover for Samsung C9 Pro (Transparent, Rubber, Plastic):zot-tp-c39086;ZOTIKOS Front & Back Case for VIVO Y55L (Black, Rubber, Plastic):zot-fcb-c50538;ZOTIKOS Back Cover for VIVO Y66 (Black, Rubber, Plastic):zot-chr-c50913;ZOTIKOS Flip Cover for Samsung Galaxy J1 (4G) (Black, Rubber, Plastic):zot-fcb-c42840;ZOTIKOS Flip Cover for VIVO Y55L (Black, Rubber, Plastic):zot-fcb-c50502;ZOTIKOS Front & Back Case for OnePlus 3 (Gold, Rubber, Plastic):zot-fcg-c33859;ZOTIKOS Book Cover for MOTOROLA MOTO C PLUS (Smooth Black, Rubber, Plastic):zot-fcb-c58599;ZOTIKOS Flip Cover for Lenovo K6 Power (Black, Rubber, Plastic):zot-fcb-c32043;ZOTIKOS Front & Back Case for Samsung Galaxy J7 (Gold, Rubber, Plastic):zot-fcg-cc3919;ZOTIKOS Flip Cover for Redmi Note 4 (Gold, Rubber, Plastic):zot-fcg-c14152;ZOTIKOS Book Cover for SAMSUNG GALAXY J7 MAX (Black, Rubber, Plastic):zot-fcb-c61749;ZOTIKOS Back Replacement Cover for Huawei Honor 8 Lite (Ablaze Transparent, Rubber, Plastic):zot-tp-c60310;ZOTIKOS Front & Back Case for Lenovo K6 Note (Black, Rubber, Plastic):zot-fcb-c31300;ZOTIKOS Back Cover for Infinix Note 4 (Shady Black, Rubber, Plastic):zot-chr-a65292;ZOTIKOS Book Cover for SAMSUNG GALAXY J7 MAX (Shady Black, Rubber, Plastic):zot-fcb-c61744;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 PLUS (Crystal Black, Rubber, Plastic):zot-chr-c60943;ZOTIKOS Back Cover for Lenovo K8 Note (Shady Transparent, Rubber, Plastic):zot-tp-a65943;ZOTIKOS Front & Back Case for LG Q6 (Arc Black, Rubber, Plastic):zot-fcb-a66163;ZOTIKOS Flip Cover for Samsung Galaxy J5 Prime (Black, Rubber, Plastic):zot-fcb-c25579;ZOTIKOS Back Cover for Lenovo Vibe K5 Note (Gold, Rubber, Plastic):zot-crm-c54554;ZOTIKOS Book Cover for Infinix Hot 4 Pro (Zync Black, Rubber, Plastic):zot-fcb-a65189;ZOTIKOS Back Cover for SAMSUNG Galaxy S8 (Black, Rubber, Plastic):zot-def-c56184;ZOTIKOS Front & Back Case for SAMSUNG Z4 (Gold, Rubber, Plastic):zot-fcg-c60415;ZOTIKOS Bumper Case for Yu Yunique 2 (Black, Rubber, Plastic):zot-def-c64997;ZOTIKOS Flip Cover for Lenovo K8 Note (Dusty Black, Rubber, Plastic):zot-fcb-a65716;ZOTIKOS Back Cover for Infinix Hot 4 Pro (Transparent, Rubber, Plastic):zot-tp-a65239;ZOTIKOS Back Replacement Cover for Infinix Note 4 (Shiny Black, Rubber, Plastic):zot-def-a65319;ZOTIKOS Front & Back Case for Lenovo K8 Note (Smooth Black, Rubber, Plastic):zot-fcb-a65730;ZOTIKOS Flip Cover for OPPO F3 (Gold, Rubber, Plastic):zot-fcg-c55562;ZOTIKOS Back Replacement Cover for SAMSUNG GALAXY J7 MAX (Zync Black, Rubber, Plastic):zot-def-c61664;ZOTIKOS Flip Cover for Motorola Moto M (Gold, Rubber, Plastic):zot-fcg-c12719;ZOTIKOS Back Cover for LG Q6 (Arc Black, Rubber, Plastic):zot-chr-a66012;ZOTIKOS Back Cover for VIVO V5 (Transparent, Rubber, Plastic):zot-tp-c47077;ZOTIKOS Flip Cover for OnePlus 2 (Gold, Rubber, Plastic):zot-fcg-c33359;ZOTIKOS Front & Back Case for Motorola Moto M (Soft Black, Rubber, Plastic):zot-fcb-c12641;ZOTIKOS Flip Cover for Lenovo Vibe K5 Note (Black, Rubber, Plastic):zot-fcb-c54876;ZOTIKOS Back Cover for Huawei Honor 8 Lite (Black, Rubber, Plastic):zot-chr-c60239;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 PLUS (Transparent, Rubber, Plastic):zot-tp-c61236;ZOTIKOS Back Cover for Redmi Note 4 (Transparent, Rubber, Plastic):zot-tp-c14273;ZOTIKOS Back Cover for Lenovo P2 (Transparent, Rubber, Plastic):zot-tp-c35659;ZOTIKOS Back Replacement Cover for Lenovo K8 Note (Soft Transparent, Rubber, Plastic):zot-tp-a65948;ZOTIKOS Flip Cover for VIVO V5 Plus (Black, Rubber, Plastic):zot-fcb-c48627;ZOTIKOS Back Cover for Oppo F1s (Black, Rubber, Plastic):zot-chr-c52882;ZOTIKOS Book Cover for Infinix Hot 4 Pro (Smooth Black, Rubber, Plastic):zot-fcb-a65191;ZOTIKOS Back Cover for VIVO V5 (Black, Rubber, Plastic):zot-def-c46692;ZOTIKOS Back Cover for OnePlus 3 (Gold, Rubber, Plastic):zot-crm-c33596;ZOTIKOS Front & Back Case for Lenovo K8 Note (Flamboyant Gold, Rubber, Plastic):zot-fcg-a65858;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Gold, Rubber, Plastic):zot-fcg-a65202;ZOTIKOS Bumper Case for MOTOROLA MOTO C PLUS (Soft Black, Rubber, Plastic):zot-def-c58539;ZOTIKOS Flip Cover for Samsung Galaxy A5 (2017) (Black, Rubber, Plastic):zot-fcb-c24449;ZOTIKOS Book Cover for SAMSUNG Galaxy J3 Pro (Gold, Rubber, Plastic):zot-fcg-c59158;ZOTIKOS Back Replacement Cover for Huawei Honor 8 Lite (Zync Transparent, Rubber, Plastic):zot-tp-c60303;ZOTIKOS Back Cover for MICROMAX EVOK NOTE E453 (Transparent, Rubber, Plastic):zot-tp-c60707;ZOTIKOS Back Cover for Samsung Galaxy J2 Ace (Transparent, Rubber, Plastic):zot-tp-c44112;ZOTIKOS Back Cover for Samsung Galaxy A5 (2017) (Black, Rubber, Plastic):zot-chr-c24317;ZOTIKOS Back Replacement Cover for LG Q6 (Soft Transparent, Rubber, Plastic):zot-tp-a66263;ZOTIKOS Back Cover for VIVO Y55L (Black, Rubber, Plastic):zot-chr-c50007;ZOTIKOS Back Cover for Samsung Galaxy J2 (Gold Back Cover, Rubber, Plastic):zot-crm-c2658;ZOTIKOS Front & Back Case for SAMSUNG GALAXY J7 MAX (Black, Rubber, Plastic):zot-fcb-c61732;ZOTIKOS Back Cover for OPPO A37 (Transparent, Rubber, Plastic):zot-tp-c52216;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 Plus (Transparent, Rubber, Plastic):zot-tp-c56799;ZOTIKOS Front & Back Case for Samsung Galaxy J2 Ace (Smooth Black, Rubber, Plastic):zot-fcb-c43792;ZOTIKOS Book Cover for Lenovo K8 Note (Shady Gold, Rubber, Plastic):zot-fcg-a65889;ZOTIKOS Flip Cover for VIVO V5S (Black, Rubber, Plastic):zot-fcb-c55968;ZOTIKOS Back Cover for Xiaomi Redmi 2 Prime (Black Back Cover, Rubber, Plastic):zot-def-c15921;ZOTIKOS Flip Cover for VIVO V5S (Black, Rubber, Plastic):zot-fcb-c55970;ZOTIKOS Front & Back Case for Lenovo K6 Note (Black, Rubber, Plastic):zot-fcb-c31305;ZOTIKOS Back Cover for Lenovo K8 Note (Dusty Black, Rubber, Plastic):zot-def-a65626;ZOTIKOS Back Cover for OPPO A37 (Transparent, Rubber, Plastic):zot-tp-c52218;ZOTIKOS Front & Back Case for Samsung Galaxy On Nxt (Gold, Rubber, Plastic):zot-fcg-c44726;ZOTIKOS Front & Back Case for Samsung Galaxy A5 (2017) (Black, Rubber, Plastic):zot-fcb-c24480;ZOTIKOS Back Cover for Samsung Galaxy J1 (4G) (Black, Rubber, Plastic):zot-chr-c42437;ZOTIKOS Front & Back Case for SAMSUNG Galaxy S8 (Gold, Rubber, Plastic):zot-fcg-c56303;ZOTIKOS Flip Cover for Lenovo K6 Power (Gold, Rubber, Plastic):zot-fcg-c32264;ZOTIKOS Back Cover for Samsung Galaxy J2 Ace (Black, Rubber, Plastic):zot-chr-c43339;ZOTIKOS Front & Back Case for OPPO F3 (Shiny Black, Rubber, Plastic):zot-fcb-c55536;ZOTIKOS Front & Back Case for Lenovo K6 Power (Black, Rubber, Plastic):zot-fcb-c32116;ZOTIKOS Back Cover for Samsung C9 Pro (Transparent, Rubber, Plastic):zot-tp-c39083;ZOTIKOS Back Cover for VIVO V5 (Black, Rubber, Plastic):zot-chr-c46364;ZOTIKOS Bumper Case for SAMSUNG Galaxy S8 (Black, Rubber, Plastic):zot-def-c56220;ZOTIKOS Back Replacement Cover for Yu Yunique 2 (Gold, Rubber, Plastic):zot-crm-c64948;ZOTIKOS Book Cover for LG Q6 (Soft Gold, Rubber, Plastic):zot-fcg-a66226;ZOTIKOS Back Cover for Nokia 3 (Gold, Rubber, Plastic):zot-crm-c59576;ZOTIKOS Bumper Case for MOTOROLA Z2 PLAY (Black, Rubber, Plastic):zot-def-c59368;ZOTIKOS Back Cover for VIVO Y55L (Black, Rubber, Plastic):zot-def-c50335;ZOTIKOS Back Cover for Lenovo K8 Note (Black, Rubber, Plastic):zot-chr-a65461;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 (Transparent, Rubber, Plastic):zot-tp-c56481;ZOTIKOS Back Cover for SAMSUNG Galaxy S8 (Transparent, Rubber, Plastic):zot-tp-c56323;ZOTIKOS Back Cover for OnePlus 2 (Black, Rubber, Plastic):zot-def-c33230;ZOTIKOS Back Cover for Samsung Galaxy J7 (Transparent Back Cover, Rubber, Plastic):zot-tp-c4013;ZOTIKOS Flip Cover for Lenovo K8 Note (Shiny Gold, Rubber, Plastic):zot-fcg-a65850;ZOTIKOS Back Cover for Samsung Galaxy J1 ace (Black Back Cover, Rubber, Plastic):zot-def-c2121;ZOTIKOS Back Cover for Samsung Z2 (Black, Rubber, Plastic):zot-chr-c30709;ZOTIKOS Front & Back Case for Samsung Galaxy J7 (Gold, Rubber, Plastic):zot-fcg-c3916;ZOTIKOS Book Cover for Nokia 3 (Arc Black, Rubber, Plastic):zot-fcb-c59653;ZOTIKOS Back Replacement Cover for MOTOROLA Z2 PLAY (Black, Rubber, Plastic):zot-def-c59318;ZOTIKOS Bumper Case for LG Q6 (Sepia Black, Rubber, Plastic):zot-def-a66136;ZOTIKOS Book Cover for OnePlus 5 (Gold, Rubber, Plastic):zot-fcg-c60842;ZOTIKOS Front & Back Case for Samsung Galaxy J7 Prime (Soft Gold Front & Back Case, Rubber, Plastic):zot-fcg-c5392;ZOTIKOS Bumper Case for Infinix Note 4 (Shady Black, Rubber, Plastic):zot-def-a65328;ZOTIKOS Back Replacement Cover for SAMSUNG GALAXY J7 Pro (Transparent, Rubber, Plastic):zot-tp-c64713;ZOTIKOS Flip Cover for Samsung Galaxy On Nxt (Gold, Rubber, Plastic):zot-fcg-c44699;ZOTIKOS Back Cover for Redmi 3S Prime (Transparent, Rubber, Plastic):zot-tp-c10658;ZOTIKOS Back Cover for Lenovo K8 Note (Gold, Rubber, Plastic):zot-crm-a65517;ZOTIKOS Bumper Case for Yu Yunique 2 (Black, Rubber, Plastic):zot-def-c64994;ZOTIKOS Bumper Case for LG Q6 (Flamboyant Black, Rubber, Plastic):zot-def-a66131;ZOTIKOS Flip Cover for LG Q6 (Black, Rubber, Plastic):zot-fcb-a66142;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 (Black, Rubber, Plastic):zot-chr-c56178;ZOTIKOS Flip Cover for MICROMAX EVOK NOTE E453 (Black, Rubber, Plastic):zot-fcb-c60617;ZOTIKOS Back Replacement Cover for VIVO V5S (Black, Rubber, Plastic):zot-def-c55917;ZOTIKOS Back Cover for Samsung Galaxy J1 (4G) (Transparent, Rubber, Plastic):zot-tp-c43200;ZOTIKOS Back Cover for Samsung Galaxy J2 (Black Back Cover, Rubber, Plastic):zot-def-c2789;ZOTIKOS Book Cover for MICROMAX EVOK NOTE E453 (Smooth Gold, Rubber, Plastic):zot-fcg-c60703;ZOTIKOS Front & Back Case for VIVO V5S (Gold, Rubber, Plastic):zot-fcg-c56036;ZOTIKOS Flip Cover for SAMSUNG Galaxy J3 Pro (Black, Rubber, Plastic):zot-fcb-c59014;ZOTIKOS Flip Cover for Lenovo K8 Note (Pastel Gold, Rubber, Plastic):zot-fcg-a65862;ZOTIKOS Back Cover for Samsung Galaxy J2 (Black, Rubber, Plastic):zot-chr-c2511;ZOTIKOS Back Cover for SAMSUNG Galaxy S8 (Black, Rubber, Plastic):zot-chr-c56154;ZOTIKOS Book Cover for SAMSUNG GALAXY J7 Pro (Shiny Black, Rubber, Plastic):zot-fcb-c64621;ZOTIKOS Flip Cover for LG Q6 (Smooth Black, Rubber, Plastic):zot-fcb-a66166;ZOTIKOS Front & Back Case for Nokia 3 (Black, Rubber, Plastic):zot-fcb-c59641;ZOTIKOS Front & Back Case for VIVO Y66 (Black, Rubber, Plastic):zot-fcb-c51464;ZOTIKOS Front & Back Case for VIVO V5 (Black, Rubber, Plastic):zot-fcb-c46895;ZOTIKOS Back Cover for Lenovo Vibe K5 Note (Black, Rubber, Plastic):zot-chr-c54420;ZOTIKOS Flip Cover for Motorola Moto E3 Power (Black, Rubber, Plastic):zot-fcb-c41460;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 PLUS (Black, Rubber, Plastic):zot-def-c61015;ZOTIKOS Flip Cover for Honor 6x (Gold, Rubber, Plastic):zot-fcg-c35996;ZOTIKOS Book Cover for VIVO V5S (Black, Rubber, Plastic):zot-fcb-c56008;ZOTIKOS Flip Cover for Infinix Note 4 (Shiny Gold, Rubber, Plastic):zot-fcg-a65406;ZOTIKOS Book Cover for OPPO F3 (Arc Black, Rubber, Plastic):zot-fcb-c55550;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 (Transparent, Rubber, Plastic):zot-tp-c61562;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 (Pastel Transparent, Rubber, Plastic):zot-tp-c56462;ZOTIKOS Book Cover for SAMSUNG GALAXY J7 Pro (Gold, Rubber, Plastic):zot-fcg-c64666;ZOTIKOS Back Cover for OnePlus 3 (Black, Rubber, Plastic):zot-chr-c33518;ZOTIKOS Flip Cover for Samsung Galaxy J1 ace (Black, Rubber, Plastic):zot-fcb-c2178;ZOTIKOS Back Cover for Samsung Galaxy J7 Prime (Black Back Cover, Rubber, Plastic):zot-def-c5156;ZOTIKOS Back Cover for Motorola Moto G5 Plus (Black, Rubber, Plastic):zot-chr-c39294;ZOTIKOS Front & Back Case for Samsung Galaxy A5 (2017) (Shiny Gold, Rubber, Plastic):zot-fcg-c24552;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 PLUS (Shiny Black, Rubber, Plastic):zot-chr-c60939;ZOTIKOS Front & Back Case for Oppo F1s (Gold, Rubber, Plastic):zot-fcg-c53467;ZOTIKOS Back Cover for Oppo F3 Plus (Black, Rubber, Plastic):zot-chr-c53643;ZOTIKOS Back Cover for Infinix Note 4 (Shiny Black, Rubber, Plastic):zot-chr-a65295;ZOTIKOS Flip Cover for Lenovo K8 Note (Pastel Black, Rubber, Plastic):zot-fcb-a65721;ZOTIKOS Bumper Case for SAMSUNG Galaxy J3 Pro (Arc Black, Rubber, Plastic):zot-def-c58992;ZOTIKOS Front & Back Case for VIVO V5 Plus (Black, Rubber, Plastic):zot-fcb-c48668;ZOTIKOS Bumper Case for Infinix Note 4 (Zync Black, Rubber, Plastic):zot-def-a65330;ZOTIKOS Flip Cover for OPPO A37 (Black, Rubber, Plastic):zot-fcb-c52056;ZOTIKOS Front & Back Case for Honor 6x (Black, Rubber, Plastic):zot-fcb-c35937;ZOTIKOS Back Cover for iPhone 7 (Black Back Cover, Rubber, Plastic):zot-chr-c16723;ZOTIKOS Back Cover for Samsung Galaxy J2 Ace (Black, Rubber, Plastic):zot-def-c43626;ZOTIKOS Book Cover for SAMSUNG Galaxy J3 Pro (Ablaze Gold, Rubber, Plastic):zot-fcg-c59167;ZOTIKOS Back Cover for Samsung C9 Pro (Transparent, Rubber, Plastic):zot-tp-c39088;ZOTIKOS Flip Cover for Redmi Note 4 (Black Flip Cover, Rubber, Plastic):zot-fcb-c13988;ZOTIKOS Back Replacement Cover for VIVO V5S (Black, Rubber, Plastic):zot-chr-c55771;ZOTIKOS Back Cover for Samsung Galaxy J2 (Black, Rubber, Plastic):zot-def-c2796;ZOTIKOS Flip Cover for Yu Yunique 2 (Gold, Rubber, Plastic):zot-fcg-c65051;ZOTIKOS Back Cover for iPhone 7 Plus (Transparent, Rubber, Plastic):zot-tp-c24231;ZOTIKOS Flip Cover for VIVO Y55L (Black, Rubber, Plastic):zot-fcb-c50500;ZOTIKOS Back Cover for Samsung Galaxy J3 (2016) (Black, Rubber, Plastic):zot-def-c25009;ZOTIKOS Back Cover for Samsung C9 Pro (Transparent, Rubber, Plastic):zot-tp-c39095;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 Plus (Transparent, Rubber, Plastic):zot-tp-c56796;ZOTIKOS Back Cover for MICROMAX EVOK NOTE E453 (Transparent, Rubber, Plastic):zot-tp-c60711;ZOTIKOS Back Cover for Oppo F3 Plus (Black, Rubber, Plastic):zot-chr-c53650;ZOTIKOS Front & Back Case for Xiaomi Mi Max (Black, Rubber, Plastic):zot-fcb-c14614;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy J3 Pro (Transparent, Rubber, Plastic):zot-tp-c59219;ZOTIKOS Book Cover for Lenovo K8 Note (Pure Black, Rubber, Plastic):zot-fcb-a65764;ZOTIKOS Back Replacement Cover for OPPO F3 (Black, Rubber, Plastic):zot-chr-c55358;ZOTIKOS Back Cover for Samsung Galaxy J7 Prime (Transparent, Rubber, Plastic):zot-tp-c5476;ZOTIKOS Back Cover for Redmi Note 4 (Black Back Cover, Rubber, Plastic):zot-chr-c13766;ZOTIKOS Flip Cover for MOTOROLA Z2 PLAY (Black, Rubber, Plastic):zot-fcb-c59378;ZOTIKOS Back Cover for VIVO Y55L (Black, Rubber, Plastic):zot-chr-c49987;ZOTIKOS Back Replacement Cover for VIVO V5S (Smooth Black, Rubber, Plastic):zot-chr-c55794;ZOTIKOS Back Cover for Motorola Moto M (Black, Rubber, Plastic):zot-def-c12540;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 PLUS (Pure Black, Rubber, Plastic):zot-def-c61009;ZOTIKOS Back Cover for OnePlus 3T (Black, Rubber, Plastic):zot-chr-c34022;ZOTIKOS Back Cover for OPPO F3 (Gold, Rubber, Plastic):zot-crm-c55369;ZOTIKOS Back Cover for Oppo F3 Plus (Gold, Rubber, Plastic):zot-crm-c53870;ZOTIKOS Flip Cover for Infinix Note 4 (Black, Rubber, Plastic):zot-fcb-a65338;ZOTIKOS Front & Back Case for Oppo F1s (Gold, Rubber, Plastic):zot-fcg-c53468;ZOTIKOS Flip Cover for Samsung Galaxy J2 (Gold, Rubber, Plastic):zot-fcg-c3082;ZOTIKOS Back Cover for Samsung Galaxy J7 (Transparent, Rubber, Plastic):zot-tp-cc4000;ZOTIKOS Flip Cover for Lenovo Vibe K5 Note (Black, Rubber, Plastic):zot-fcb-c54880;ZOTIKOS Back Cover for VIVO Y66 (Gold, Rubber, Plastic):zot-crm-c51134;ZOTIKOS Front & Back Case for Infinix Note 4 (Pure Black, Rubber, Plastic):zot-fcb-a65364;ZOTIKOS Back Cover for MICROMAX EVOK NOTE E453 (Black, Rubber, Plastic):zot-chr-c60558;ZOTIKOS Front & Back Case for Samsung Galaxy J1 ace (Black, Rubber, Plastic):zot-fcb-c2216;ZOTIKOS Back Cover for Samsung Galaxy J7 (Black, Rubber, Plastic):zot-def-c3572;ZOTIKOS Back Replacement Cover for Nokia 3 (Soft Transparent, Rubber, Plastic):zot-tp-c59746;ZOTIKOS Back Cover for LG Q6 (Black, Rubber, Plastic):zot-chr-a65958;ZOTIKOS Back Cover for SAMSUNG Z4 (Black, Rubber, Plastic):zot-chr-c60318;ZOTIKOS Back Cover for Samsung Galaxy J1 (4G) (Transparent, Rubber, Plastic):zot-tp-c43201;ZOTIKOS Flip Cover for Lenovo K6 Note (Black, Rubber, Plastic):zot-fcb-c31244;ZOTIKOS Back Cover for Lenovo Vibe K5 Note (Black, Rubber, Plastic):zot-def-c54662;ZOTIKOS Back Cover for Motorola Moto E3 Power (Black, Rubber, Plastic):zot-chr-c41147;ZOTIKOS Bumper Case for Lenovo K8 Note (Pure Black, Rubber, Plastic):zot-def-a65622;ZOTIKOS Back Cover for Motorola Moto M (Transparent, Rubber, Plastic):zot-tp-c12819;ZOTIKOS Back Cover for VIVO V5 Plus (Black, Rubber, Plastic):zot-chr-c48193;ZOTIKOS Flip Cover for iPhone 7 Plus (Gold, Rubber, Plastic):zot-fcg-c24161;ZOTIKOS Flip Cover for Samsung Galaxy J2 (Black, Rubber, Plastic):zot-fcb-c2906;ZOTIKOS Back Cover for MOTOROLA MOTO C PLUS (Black, Rubber, Plastic):zot-chr-c58402;ZOTIKOS Back Cover for Lenovo K8 Note (Black, Rubber, Plastic):zot-chr-a65465;ZOTIKOS Back Cover for Infinix Note 4 (Transparent, Rubber, Plastic):zot-tp-a65427;ZOTIKOS Back Cover for Samsung C9 Pro (Black, Rubber, Plastic):zot-chr-c38656;ZOTIKOS Flip Cover for Lenovo K8 Note (Soft Gold, Rubber, Plastic):zot-fcg-a65894;ZOTIKOS Flip Cover for Oppo F1s (Gold, Rubber, Plastic):zot-fcg-c53449;ZOTIKOS Book Cover for SAMSUNG Galaxy S8 Plus (Shady Black, Rubber, Plastic):zot-fcb-c56610;ZOTIKOS Bumper Case for MOTOROLA MOTO C (Black, Rubber, Plastic):zot-def-c58208;ZOTIKOS Back Cover for OPPO A37 (Black, Rubber, Plastic):zot-chr-c51824;ZOTIKOS Front & Back Case for Lenovo K8 Note (Gaily Black, Rubber, Plastic):zot-fcb-a65718;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 (Black, Rubber, Plastic):zot-chr-c56172;ZOTIKOS Flip Cover for Lenovo K8 Note (Black, Rubber, Plastic):zot-fcb-a65636;ZOTIKOS Back Cover for Samsung Galaxy J2 (Black Back Cover, Rubber, Plastic):zot-chr-c2503;ZOTIKOS Front & Back Case for Lenovo K6 Power (Dusty Black, Rubber, Plastic):zot-fcb-c32110;ZOTIKOS Back Cover for iPhone 7 (Black Back Cover, Rubber, Plastic):zot-chr-c16722;ZOTIKOS Back Cover for VIVO V5 (Gold, Rubber, Plastic):zot-crm-c46565;ZOTIKOS Back Cover for Lenovo K8 Note (Transparent, Rubber, Plastic):zot-tp-a65910;ZOTIKOS Book Cover for LG Q6 (Pure Gold, Rubber, Plastic):zot-fcg-a66227;ZOTIKOS Back Cover for MOTOROLA MOTO E4 (Transparent, Rubber, Plastic):zot-tp-c61498;ZOTIKOS Front & Back Case for OPPO A37 (Black, Rubber, Plastic):zot-fcb-c52079;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 PLUS (Transparent, Rubber, Plastic):zot-tp-c61234;ZOTIKOS Back Cover for Samsung Galaxy On Nxt (Black, Rubber, Plastic):zot-chr-c44247;ZOTIKOS Back Cover for OnePlus 2 (Transparent, Rubber, Plastic):zot-tp-c33442;ZOTIKOS Back Cover for Samsung Galaxy J1 ace (Black Back Cover, Rubber, Plastic):zot-chr-c2003;ZOTIKOS Back Cover for Samsung Galaxy J7 (Black Back Cover, Rubber, Plastic):zot-chr-c3405;ZOTIKOS Front & Back Case for Lenovo K6 Note (Gold, Rubber, Plastic):zot-fcg-c31490;ZOTIKOS Front & Back Case for Lenovo K8 Note (Zync Gold, Rubber, Plastic):zot-fcg-a65849;ZOTIKOS Back Replacement Cover for Yu Yunique 2 (Black, Rubber, Plastic):zot-chr-c64935;ZOTIKOS Back Cover for Lenovo Vibe K5 Note (Gold, Rubber, Plastic):zot-crm-c54551;ZOTIKOS Flip Cover for SAMSUNG GALAXY ON MAX (Black, Rubber, Plastic):zot-fcb-c62028;ZOTIKOS Front & Back Case for Honor 6x (Gold, Rubber, Plastic):zot-fcg-c36010;ZOTIKOS Flip Cover for Samsung Galaxy A5 (2016) (Gold, Rubber, Plastic):zot-fcg-c29933;ZOTIKOS Back Cover for Oppo F3 Plus (Black, Rubber, Plastic):zot-chr-c53642;ZOTIKOS Back Cover for VIVO Y66 (Transparent, Rubber, Plastic):zot-tp-c51658;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Black, Rubber, Plastic):zot-fcb-a65143;ZOTIKOS Back Cover for SAMSUNG Galaxy S8 Plus (Black, Rubber, Plastic):zot-def-c56524;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Black, Rubber, Plastic):zot-fcb-a65148;ZOTIKOS Back Cover for Redmi Note 4 (Black, Rubber, Plastic):zot-def-c13947;ZOTIKOS Back Cover for VIVO Y55L (Black, Rubber, Plastic):zot-def-c50336;ZOTIKOS Back Cover for Redmi 3S Prime (Transparent, Rubber, Plastic):zot-tp-c10659;ZOTIKOS Flip Cover for Infinix Note 4 (Black, Rubber, Plastic):zot-fcb-a65340;ZOTIKOS Flip Cover for Motorola Moto E3 Power (Gold, Rubber, Plastic):zot-fcg-c41569;ZOTIKOS Flip Cover for Samsung Galaxy J1 (4G) (Black, Rubber, Plastic):zot-fcb-c42842;ZOTIKOS Flip Cover for Oppo F1s (Black, Rubber, Plastic):zot-fcb-c53356;ZOTIKOS Back Replacement Cover for OPPO F3 (Iridescent Black, Rubber, Plastic):zot-def-c55468;ZOTIKOS Flip Cover for SAMSUNG Galaxy S8 Plus (Black, Rubber, Plastic):zot-fcb-c56571;ZOTIKOS Front & Back Case for Samsung Z2 (Gold, Rubber, Plastic):zot-fcg-c30846;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 (Black, Rubber, Plastic):zot-def-c61345;ZOTIKOS Front & Back Case for VIVO V5S (Arc Gold, Rubber, Plastic):zot-fcg-c56037;ZOTIKOS Back Cover for SAMSUNG GALAXY ON MAX (Transparent, Rubber, Plastic):zot-tp-c62162;ZOTIKOS Back Cover for VIVO V5S (Transparent, Rubber, Plastic):zot-tp-c56064;ZOTIKOS Book Cover for Infinix Hot 4 Pro (Shady Gold, Rubber, Plastic):zot-fcg-a65231;ZOTIKOS Flip Cover for MICROMAX EVOK NOTE E453 (Gold, Rubber, Plastic):zot-fcg-c60665;ZOTIKOS Back Cover for LG Q6 (Shiny Black, Rubber, Plastic):zot-chr-a66014;ZOTIKOS Back Replacement Cover for SAMSUNG GALAXY ON MAX (Black, Rubber, Plastic):zot-chr-c61929;ZOTIKOS Flip Cover for VIVO Y66 (Gold, Rubber, Plastic):zot-fcg-c51570;ZOTIKOS Back Cover for Oppo F3 Plus (Black, Rubber, Plastic):zot-chr-c53647;ZOTIKOS Back Replacement Cover for VIVO V5S (Shady Black, Rubber, Plastic):zot-def-c55913;ZOTIKOS Flip Cover for MOTOROLA MOTO E4 PLUS (Black, Rubber, Plastic):zot-fcb-c61051;ZOTIKOS Back Cover for Infinix Hot 4 Pro (Shiny Transparent, Rubber, Plastic):zot-tp-a65268;ZOTIKOS Flip Cover for SAMSUNG Galaxy S8 Plus (Gold, Rubber, Plastic):zot-fcg-c56619;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 PLUS (Ablaze Black, Rubber, Plastic):zot-chr-c60945;ZOTIKOS Flip Cover for SAMSUNG Galaxy S8 Plus (Gold, Rubber, Plastic):zot-fcg-c56621;ZOTIKOS Back Cover for LG Q6 (Splashy Black, Rubber, Plastic):zot-def-a66101;ZOTIKOS Flip Cover for Lenovo K8 Note (Black, Rubber, Plastic):zot-fcb-a65631;ZOTIKOS Flip Cover for Samsung Galaxy J1 (4G) (Black, Rubber, Plastic):zot-fcb-c42835;ZOTIKOS Back Cover for VIVO Y66 (Transparent, Rubber, Plastic):zot-tp-c51646;ZOTIKOS Back Cover for Google Pixel XL (Black, Rubber, Plastic):zot-def-c35213;ZOTIKOS Front & Back Case for Samsung Galaxy J7 (Black, Rubber, Plastic):zot-fcb-c3761;ZOTIKOS Flip Cover for LG Q6 (Soft Black, Rubber, Plastic):zot-fcb-a66167;ZOTIKOS Back Cover for VIVO V5 (Gold, Rubber, Plastic):zot-crm-c46567;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO C (Soft Black, Rubber, Plastic):zot-chr-c58106;ZOTIKOS Back Cover for OPPO F3 (Gold, Rubber, Plastic):zot-crm-c55368;ZOTIKOS Back Cover for VIVO V5 (Transparent, Rubber, Plastic):zot-tp-c47076;ZOTIKOS Book Cover for SAMSUNG GALAXY J7 MAX (Shiny Gold, Rubber, Plastic):zot-fcg-c61798;ZOTIKOS Bumper Case for SAMSUNG Galaxy S8 (Black, Rubber, Plastic):zot-def-c56224;ZOTIKOS Flip Cover for Lenovo K8 Note (Flamboyant Gold, Rubber, Plastic):zot-fcg-a65900;ZOTIKOS Back Replacement Cover for OnePlus 5 (Transparent, Rubber, Plastic):zot-tp-c60900;ZOTIKOS Flip Cover for VIVO Y66 (Gold, Rubber, Plastic):zot-fcg-c51564;ZOTIKOS Flip Cover for Nokia 3 (Gold, Rubber, Plastic):zot-fcg-c59683;ZOTIKOS Back Cover for Redmi Note 4 (Transparent, Rubber, Plastic):zot-tp-c14277;ZOTIKOS Back Cover for SAMSUNG Galaxy S8 Plus (Transparent, Rubber, Plastic):zot-tp-c56694;ZOTIKOS Back Replacement Cover for LG Q6 (Gaily Black, Rubber, Plastic):zot-def-a66096;ZOTIKOS Book Cover for Yu Yunique 2 (Gold, Rubber, Plastic):zot-fcg-c65081;ZOTIKOS Back Cover for VIVO Y66 (Black, Rubber, Plastic):zot-def-c51262;ZOTIKOS Flip Cover for Samsung Galaxy J2 Ace (Black, Rubber, Plastic):zot-fcb-c43741;ZOTIKOS Back Replacement Cover for SAMSUNG GALAXY J7 MAX (Shady Black, Rubber, Plastic):zot-chr-c61589;ZOTIKOS Back Cover for VIVO V5 (Black, Rubber, Plastic):zot-chr-c46358;ZOTIKOS Back Cover for Redmi 3S Prime (Black, Rubber, Plastic):zot-chr-c10005;ZOTIKOS Back Cover for OnePlus 5 (Transparent, Rubber, Plastic):zot-tp-c60854;ZOTIKOS Book Cover for MOTOROLA MOTO C PLUS (Black, Rubber, Plastic):zot-fcb-c58595;ZOTIKOS Front & Back Case for Samsung Galaxy J2 Ace (Gold, Rubber, Plastic):zot-fcg-c43981;ZOTIKOS Back Cover for Samsung Galaxy J2 (Black, Rubber, Plastic):zot-def-c2797;ZOTIKOS Back Cover for VIVO V5S (Black, Rubber, Plastic):zot-chr-c55676;ZOTIKOS Flip Cover for VIVO V5 Plus (Black, Rubber, Plastic):zot-fcb-c48638;ZOTIKOS Back Cover for VIVO V5 (Black, Rubber, Plastic):zot-def-c46693;ZOTIKOS Front & Back Case for Motorola Moto E3 Power (Gold, Rubber, Plastic):zot-fcg-c41593;ZOTIKOS Back Cover for VIVO V5 Plus (Black, Rubber, Plastic):zot-def-c48517;ZOTIKOS Back Cover for Lenovo Vibe K5 Note (Black, Rubber, Plastic):zot-def-c54663;ZOTIKOS Back Cover for Infinix Note 4 (Smooth Black, Rubber, Plastic):zot-chr-a65296;ZOTIKOS Book Cover for OPPO F3 (Zync Gold, Rubber, Plastic):zot-fcg-c55594;ZOTIKOS Book Cover for Lenovo K8 Note (Sepia Gold, Rubber, Plastic):zot-fcg-a65905;ZOTIKOS Back Replacement Cover for Lenovo K8 Note (Arc Transparent, Rubber, Plastic):zot-tp-a65944;ZOTIKOS Back Cover for Lenovo Vibe K5 Note (Transparent, Rubber, Plastic):zot-tp-c55062;ZOTIKOS Back Replacement Cover for SAMSUNG GALAXY ON MAX (Gold, Rubber, Plastic):zot-crm-c61969;ZOTIKOS Back Cover for LG Q6 (Black, Rubber, Plastic):zot-chr-a65967;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Black, Rubber, Plastic):zot-fcb-a65145;ZOTIKOS Back Cover for Redmi 3S Prime (Gold, Rubber, Plastic):zot-crm-c10151;ZOTIKOS Back Cover for Infinix Note 4 (Black, Rubber, Plastic):zot-chr-a65277;ZOTIKOS Back Replacement Cover for Lenovo K8 Note (Gaily Black, Rubber, Plastic):zot-chr-a65510;ZOTIKOS Front & Back Case for Samsung Galaxy J5 Prime (Black, Rubber, Plastic):zot-fcb-c25599;ZOTIKOS Back Cover for Samsung Galaxy On Nxt (Black, Rubber, Plastic):zot-chr-c44251;ZOTIKOS Front & Back Case for MOTOROLA MOTO E4 PLUS (Gold, Rubber, Plastic):zot-fcg-c61123;ZOTIKOS Front & Back Case for Infinix Note 4 (Shiny Black, Rubber, Plastic):zot-fcb-a65361;ZOTIKOS Back Cover for Oppo F3 Plus (Black, Rubber, Plastic):zot-def-c53965;ZOTIKOS Back Replacement Cover for Infinix Note 4 (Shiny Transparent, Rubber, Plastic):zot-tp-a65451;ZOTIKOS Flip Cover for MICROMAX EVOK NOTE E453 (Gold, Rubber, Plastic):zot-fcg-c60668;ZOTIKOS Back Cover for VIVO Y53 (Transparent, Rubber, Plastic):zot-tp-c49822;ZOTIKOS Book Cover for MOTOROLA MOTO E4 (Gold, Rubber, Plastic):zot-fcg-c61471;ZOTIKOS Back Cover for Lenovo K6 Power (Gold, Rubber, Plastic):zot-crm-c31791;ZOTIKOS Back Cover for VIVO Y55L (Transparent, Rubber, Plastic):zot-tp-c50742;ZOTIKOS Book Cover for Lenovo K8 Note (Shady Black, Rubber, Plastic):zot-fcb-a65758;ZOTIKOS Bumper Case for MOTOROLA MOTO C (Black, Rubber, Plastic):zot-def-c58213;ZOTIKOS Book Cover for Nokia 3 (Zync Black, Rubber, Plastic):zot-fcb-c59654;ZOTIKOS Book Cover for SAMSUNG Galaxy S8 Plus (Black, Rubber, Plastic):zot-fcb-c56612;ZOTIKOS Book Cover for LG Q6 (Smooth Gold, Rubber, Plastic):zot-fcg-a66225;ZOTIKOS Back Cover for OPPO F3 (Black, Rubber, Plastic):zot-chr-c55258;ZOTIKOS Front & Back Case for Samsung Galaxy A5 (2017) (Black, Rubber, Plastic):zot-fcb-c24479;ZOTIKOS Back Cover for SAMSUNG GALAXY ON MAX (Transparent, Rubber, Plastic):zot-tp-c62139;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO C (Shady Transparent, Rubber, Plastic):zot-tp-c58397;ZOTIKOS Flip Cover for Xiaomi Mi 5s Plus (Black, Rubber, Plastic):zot-fcb-c17522;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 (Black, Rubber, Plastic):zot-chr-c56176;ZOTIKOS Back Replacement Cover for OPPO F3 (Black, Rubber, Plastic):zot-chr-c55335;ZOTIKOS Back Cover for VIVO Y66 (Transparent, Rubber, Plastic):zot-tp-c51656;ZOTIKOS Back Cover for OnePlus 3 (Transparent, Rubber, Plastic):zot-tp-c33926;ZOTIKOS Front & Back Case for MOTOROLA MOTO C (Zync Black, Rubber, Plastic):zot-fcb-c58245;ZOTIKOS Book Cover for SAMSUNG GALAXY ON MAX (Gold, Rubber, Plastic):zot-fcg-c62136;ZOTIKOS Flip Cover for VIVO V5 (Gold, Rubber, Plastic):zot-fcg-c47003;ZOTIKOS Flip Cover for SAMSUNG Galaxy J3 Pro (Black, Rubber, Plastic):zot-fcb-c59010;ZOTIKOS Back Cover for Nokia 3 (Transparent, Rubber, Plastic):zot-tp-c59706;ZOTIKOS Book Cover for MOTOROLA MOTO E4 (Gold, Rubber, Plastic):zot-fcg-c61469;ZOTIKOS Back Cover for Redmi Note 4 (Transparent Back Cover, Rubber, Plastic):zot-tp-c14279;ZOTIKOS Back Replacement Cover for VIVO V5S (Black, Rubber, Plastic):zot-chr-c55784;ZOTIKOS Back Cover for Lenovo K8 Note (Transparent, Rubber, Plastic):zot-tp-a65920;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Zync Black, Rubber, Plastic):zot-fcb-a65171;ZOTIKOS Book Cover for MOTOROLA MOTO C (Brash Black, Rubber, Plastic):zot-fcb-c58269;ZOTIKOS Back Replacement Cover for SAMSUNG GALAXY J7 Pro (Black, Rubber, Plastic):zot-chr-c64521;ZOTIKOS Back Cover for MOTOROLA Z2 PLAY (Transparent, Rubber, Plastic):zot-tp-c59472;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Gold, Rubber, Plastic):zot-fcg-a65198;ZOTIKOS Back Cover for Infinix Hot 4 Pro (Arc Transparent, Rubber, Plastic):zot-tp-a65266;ZOTIKOS Back Cover for VIVO V5S (Black, Rubber, Plastic):zot-chr-c55664;ZOTIKOS Back Cover for Lenovo K8 Note (Black, Rubber, Plastic):zot-def-a65567;ZOTIKOS Front & Back Case for MOTOROLA Z2 PLAY (Gold, Rubber, Plastic):zot-fcg-c59445;ZOTIKOS Front & Back Case for OnePlus 2 (Black, Rubber, Plastic):zot-fcb-c33300;ZOTIKOS Back Replacement Cover for MOTOROLA Z2 PLAY (Black, Rubber, Plastic):zot-chr-c59243;ZOTIKOS Flip Cover for Lenovo K8 Note (Gold, Rubber, Plastic):zot-fcg-a65804;ZOTIKOS Front & Back Case for Infinix Note 4 (Zync Gold, Rubber, Plastic):zot-fcg-a65405;ZOTIKOS Back Cover for OPPO F3 (Gold, Rubber, Plastic):zot-crm-c55366;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO C PLUS (Dusty Transparent, Rubber, Plastic):zot-tp-c58722;ZOTIKOS Flip Cover for MOTOROLA Z2 PLAY (Gold, Rubber, Plastic):zot-fcg-c59426;ZOTIKOS Back Cover for Samsung Galaxy J2 Ace (Transparent, Rubber, Plastic):zot-tp-c44109;ZOTIKOS Back Cover for Lenovo K8 Note (Transparent, Rubber, Plastic):zot-tp-a65911;ZOTIKOS Flip Cover for Infinix Note 4 (Arc Black, Rubber, Plastic):zot-fcb-a65359;ZOTIKOS Front & Back Case for Samsung Galaxy J5 Prime (Black, Rubber, Plastic):zot-fcb-c25600;ZOTIKOS Front & Back Case for OnePlus 3T (Gold, Rubber, Plastic):zot-fcg-c34365;ZOTIKOS Back Cover for MOTOROLA MOTO E4 (Transparent, Rubber, Plastic):zot-tp-c61491;ZOTIKOS Front & Back Case for Samsung Galaxy J1 (4G) (Black, Rubber, Plastic):zot-fcb-c42887;ZOTIKOS Flip Cover for LG Q6 (Gold, Rubber, Plastic):zot-fcg-a66186;ZOTIKOS Front & Back Case for VIVO Y66 (Shiny Black, Rubber, Plastic):zot-fcb-c51455;ZOTIKOS Back Cover for Samsung Galaxy J7 Prime (Gold Back Cover, Rubber, Plastic):zot-crm-c5032;ZOTIKOS Back Cover for VIVO V5 Plus (Transparent, Rubber, Plastic):zot-tp-c48880;ZOTIKOS Flip Cover for VIVO Y55L (Gold, Rubber, Plastic):zot-fcg-c50649;ZOTIKOS Book Cover for Infinix Note 4 (Soft Gold, Rubber, Plastic):zot-fcg-a65422;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Soft Black, Rubber, Plastic):zot-fcb-a65192;ZOTIKOS Back Cover for MOTOROLA MOTO E4 PLUS (Transparent, Rubber, Plastic):zot-tp-c61153;ZOTIKOS Flip Cover for VIVO Y53 (Black, Rubber, Plastic):zot-fcb-c49586;ZOTIKOS Back Cover for Redmi 3S Prime (Black, Rubber, Plastic):zot-def-c10259;ZOTIKOS Back Cover for VIVO Y66 (Black, Rubber, Plastic):zot-chr-c50902;ZOTIKOS Back Cover for Samsung Galaxy J2 Ace (Gold, Rubber, Plastic):zot-crm-c43495;ZOTIKOS Flip Cover for Samsung Galaxy J1 (4G) (Gold, Rubber, Plastic):zot-fcg-c43016;ZOTIKOS Front & Back Case for VIVO Y66 (Black, Rubber, Plastic):zot-fcb-c51462;ZOTIKOS Flip Cover for Nokia 3 (Black, Rubber, Plastic):zot-fcb-c59613;ZOTIKOS Back Cover for SAMSUNG Z4 (Black, Rubber, Plastic):zot-chr-c60317;ZOTIKOS Flip Cover for SAMSUNG GALAXY J7 MAX (Black, Rubber, Plastic):zot-fcb-c61726;ZOTIKOS Back Replacement Cover for SAMSUNG GALAXY J7 MAX (Black, Rubber, Plastic):zot-def-c61663;ZOTIKOS Back Cover for SAMSUNG GALAXY J7 Pro (Black, Rubber, Plastic):zot-def-c64530;ZOTIKOS Flip Cover for Lenovo K8 Note (Soft Black, Rubber, Plastic):zot-fcb-a65731;ZOTIKOS Back Replacement Cover for VIVO V5S (Transparent, Rubber, Plastic):zot-tp-c56142;ZOTIKOS Back Cover for Lenovo A6600 Plus (Transparent, Rubber, Plastic):zot-tp-c36252;ZOTIKOS Bumper Case for SAMSUNG GALAXY ON MAX (Black, Rubber, Plastic):zot-def-c62016;ZOTIKOS Book Cover for Nokia 3 (Gold, Rubber, Plastic):zot-fcg-c59695;ZOTIKOS Flip Cover for Samsung Galaxy J1 (4G) (Gold, Rubber, Plastic):zot-fcg-c43009;ZOTIKOS Back Cover for VIVO V5 (Gold, Rubber, Plastic):zot-crm-c46570;ZOTIKOS Back Cover for VIVO Y66 (Black, Rubber, Plastic):zot-chr-c50920;ZOTIKOS Back Replacement Cover for OPPO F3 (Zync Black, Rubber, Plastic):zot-def-c55457;ZOTIKOS Back Cover for Lenovo P2 (Transparent, Rubber, Plastic):zot-tp-c35658;ZOTIKOS Back Cover for Lenovo K6 Power (Gold, Rubber, Plastic):zot-crm-c31798;ZOTIKOS Flip Cover for Samsung Galaxy J1 ace (Black Flip Cover, Rubber, Plastic):zot-fcb-c2169;ZOTIKOS Front & Back Case for Motorola Moto E3 Power (Gold, Rubber, Plastic):zot-fcg-c41591;ZOTIKOS Back Replacement Cover for OPPO F3 (Transparent, Rubber, Plastic):zot-tp-c55649;ZOTIKOS Back Cover for MOTOROLA Z2 PLAY (Black, Rubber, Plastic):zot-def-c59260;ZOTIKOS Front & Back Case for Lenovo P2 (Black, Rubber, Plastic):zot-fcb-c35529;ZOTIKOS Back Cover for SAMSUNG Galaxy J3 Pro (Transparent, Rubber, Plastic):zot-tp-c59172;ZOTIKOS Bumper Case for SAMSUNG GALAXY ON MAX (Black, Rubber, Plastic):zot-def-c62022;ZOTIKOS Back Cover for Lenovo K6 Note (Black, Rubber, Plastic):zot-chr-c30930;ZOTIKOS Flip Cover for Samsung Galaxy J7 (Black, Rubber, Plastic):zot-fcb-c3713;ZOTIKOS Back Cover for Redmi Note 4 (Transparent, Rubber, Plastic):zot-tp-c14272;ZOTIKOS Front & Back Case for MICROMAX EVOK POWER Q4260 (Smooth Black, Rubber, Plastic):zot-fcb-c60468;ZOTIKOS Back Cover for MICROMAX EVOK POWER Q4260 (Transparent, Rubber, Plastic):zot-tp-c60535;ZOTIKOS Flip Cover for iPhone 7 (Gold, Rubber, Plastic):zot-fcg-c17028;ZOTIKOS Front & Back Case for Lenovo K8 Note (Pure Black, Rubber, Plastic):zot-fcb-a65712;ZOTIKOS Book Cover for SAMSUNG GALAXY ON MAX (Dusty Black, Rubber, Plastic):zot-fcb-c62089;ZOTIKOS Book Cover for SAMSUNG Galaxy J3 Pro (Black, Rubber, Plastic):zot-fcb-c59098;ZOTIKOS Back Cover for VIVO Y55L (Transparent, Rubber, Plastic):zot-tp-c50745;ZOTIKOS Bumper Case for SAMSUNG GALAXY J7 MAX (Zync Black, Rubber, Plastic):zot-def-c61682;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO C PLUS (Black, Rubber, Plastic):zot-chr-c58437;ZOTIKOS Back Cover for Samsung Galaxy J2 (Transparent, Rubber, Plastic):zot-tp-c3277;ZOTIKOS Front & Back Case for SAMSUNG Galaxy J3 Pro (Black, Rubber, Plastic):zot-fcb-c59062;ZOTIKOS Bumper Case for MOTOROLA MOTO E4 PLUS (Black, Rubber, Plastic):zot-def-c61036;ZOTIKOS Back Cover for Samsung Galaxy J1 ace (Transparent, Rubber, Plastic):zot-tp-c2445;ZOTIKOS Front & Back Case for MOTOROLA MOTO C (Black, Rubber, Plastic):zot-fcb-c58247;ZOTIKOS Back Cover for Samsung Galaxy A5 (2016) (Transparent, Rubber, Plastic):zot-tp-c29980;ZOTIKOS Flip Cover for Lenovo K6 Power (Black, Rubber, Plastic):zot-fcb-c32044;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 (Soft Black, Rubber, Plastic):zot-def-c61342;ZOTIKOS Flip Cover for Samsung Galaxy J7 Prime (Black, Rubber, Plastic):zot-fcb-c5238;ZOTIKOS Back Cover for MOTOROLA MOTO E4 PLUS (Black, Rubber, Plastic):zot-def-c60970;ZOTIKOS Flip Cover for MICROMAX EVOK NOTE E453 (Black, Rubber, Plastic):zot-fcb-c60622;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO C (Transparent, Rubber, Plastic):zot-tp-c58388;ZOTIKOS Front & Back Case for Samsung C9 Pro (Black, Rubber, Plastic):zot-fcb-c38910;ZOTIKOS Flip Cover for Samsung Galaxy A5 (2016) (Black, Rubber, Plastic):zot-fcb-c29851;ZOTIKOS Back Cover for Samsung Galaxy J2 Ace (Black, Rubber, Plastic):zot-def-c43630;ZOTIKOS Front & Back Case for VIVO V5S (Gold, Rubber, Plastic):zot-fcg-c56040;ZOTIKOS Front & Back Case for Samsung Galaxy J7 (Gold, Rubber, Plastic):zot-fcg-cc3920;ZOTIKOS Back Cover for Samsung Galaxy J2 Ace (Transparent, Rubber, Plastic):zot-tp-c44104;ZOTIKOS Book Cover for OPPO F3 (Pure Gold, Rubber, Plastic):zot-fcg-c55598;ZOTIKOS Flip Cover for Redmi 3S Prime (Black Flip Cover, Rubber, Plastic):zot-fcb-c10367;ZOTIKOS Back Cover for OnePlus 2 (Transparent, Rubber, Plastic):zot-tp-c33436;ZOTIKOS Flip Cover for MOTOROLA MOTO E4 (Gold, Rubber, Plastic):zot-fcg-c61443;ZOTIKOS Back Cover for Samsung Galaxy On Nxt (Transparent, Rubber, Plastic):zot-tp-c44806;ZOTIKOS Back Cover for Motorola Moto E3 Power (Black, Rubber, Plastic):zot-def-c41285;ZOTIKOS Flip Cover for Lenovo K8 Note (Black, Rubber, Plastic):zot-fcb-a65628;ZOTIKOS Flip Cover for Samsung Galaxy J7 (Black, Rubber, Plastic):zot-fcb-c3706;ZOTIKOS Back Cover for VIVO V5 Plus (Black, Rubber, Plastic):zot-def-c48509;ZOTIKOS Back Replacement Cover for OPPO F3 (Crystal Transparent, Rubber, Plastic):zot-tp-c55651;ZOTIKOS Back Cover for Motorola Moto M (Transparent, Rubber, Plastic):zot-tp-c12822;ZOTIKOS Back Cover for Yu Yunique 2 (Black, Rubber, Plastic):zot-chr-c64902;ZOTIKOS Front & Back Case for Samsung Galaxy J7 (Crystal Black Front & Back Case, Rubber, Plastic):zot-fcb-c3756;ZOTIKOS Back Cover for Yu Yunique 2 (Transparent, Rubber, Plastic):zot-tp-c65092;ZOTIKOS Back Cover for Infinix Hot 4 Pro (Black, Rubber, Plastic):zot-chr-a65122;ZOTIKOS Back Cover for Lenovo K6 Note (Gold, Rubber, Plastic):zot-crm-c31040;ZOTIKOS Flip Cover for SAMSUNG Galaxy J3 Pro (Gold, Rubber, Plastic):zot-fcg-c59110;ZOTIKOS Book Cover for SAMSUNG GALAXY J7 Pro (Black, Rubber, Plastic):zot-fcb-c64624;ZOTIKOS Back Cover for OnePlus 3T (Transparent, Rubber, Plastic):zot-tp-c34422;ZOTIKOS Flip Cover for Oppo F1s (Black, Rubber, Plastic):zot-fcb-c53355;ZOTIKOS Back Cover for Samsung Galaxy J7 (Black, Rubber, Plastic):zot-def-c3569;ZOTIKOS Back Cover for Motorola Moto E3 Power (Gold, Rubber, Plastic):zot-crm-c41228;ZOTIKOS Back Cover for Redmi Note 4 (Transparent Back Cover, Rubber, Plastic):zot-tp-c14284;ZOTIKOS Back Cover for VIVO V5 (Transparent, Rubber, Plastic):zot-tp-c47083;ZOTIKOS Bumper Case for Lenovo K8 Note (Crystal Black, Rubber, Plastic):zot-def-a65623;ZOTIKOS Back Cover for SAMSUNG GALAXY J7 Pro (Transparent, Rubber, Plastic):zot-tp-c64671;ZOTIKOS Flip Cover for MOTOROLA MOTO E4 PLUS (Black, Rubber, Plastic):zot-fcb-c61055;ZOTIKOS Book Cover for MOTOROLA Z2 PLAY (Black, Rubber, Plastic):zot-fcb-c59415;ZOTIKOS Flip Cover for Samsung Z2 (Black, Rubber, Plastic):zot-fcb-c30803;ZOTIKOS Front & Back Case for Redmi 3S Prime (Pure Gold, Rubber, Plastic):zot-fcg-c10546;ZOTIKOS Back Cover for OPPO A37 (Black, Rubber, Plastic):zot-def-c52009;ZOTIKOS Back Cover for Samsung Galaxy J7 (Black, Rubber, Plastic):zot-chr-c3411;ZOTIKOS Flip Cover for Nokia 3 (Black, Rubber, Plastic):zot-fcb-c59618;ZOTIKOS Front & Back Case for Lenovo Vibe K5 Note (Gold, Rubber, Plastic):zot-fcg-c54986;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Arc Black, Rubber, Plastic):zot-fcb-a65170;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 Plus (Transparent, Rubber, Plastic):zot-tp-c56830;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 (Transparent, Rubber, Plastic):zot-tp-c61560;ZOTIKOS Flip Cover for Xiaomi Mi Max (Gold, Rubber, Plastic):zot-fcg-c14638;ZOTIKOS Flip Cover for VIVO V5 (Gold, Rubber, Plastic):zot-fcg-c47002;ZOTIKOS Back Cover for Lenovo Vibe K5 Note (Black, Rubber, Plastic):zot-chr-c54422;ZOTIKOS Back Cover for Samsung Galaxy J7 (Black, Rubber, Plastic):zot-chr-cc3409;ZOTIKOS Back Cover for VIVO Y55L (Transparent, Rubber, Plastic):zot-tp-c50739;ZOTIKOS Front & Back Case for OnePlus 5 (Black, Rubber, Plastic):zot-fcb-c60787;ZOTIKOS Flip Cover for Lenovo K8 Note (Black, Rubber, Plastic):zot-fcb-a65639;ZOTIKOS Back Cover for Samsung Z2 (Transparent, Rubber, Plastic):zot-tp-c30870;ZOTIKOS Back Replacement Cover for Infinix Note 4 (Zync Transparent, Rubber, Plastic):zot-tp-a65450;ZOTIKOS Flip Cover for OnePlus 2 (Black, Rubber, Plastic):zot-fcb-c33272;ZOTIKOS Back Cover for MOTOROLA MOTO C (Black, Rubber, Plastic):zot-chr-c58070;ZOTIKOS Front & Back Case for OnePlus 3 (Black, Rubber, Plastic):zot-fcb-c33760;ZOTIKOS Back Cover for VIVO V5 Plus (Transparent, Rubber, Plastic):zot-tp-c48878;ZOTIKOS Back Cover for Motorola Moto E3 Power (Transparent, Rubber, Plastic):zot-tp-c41663;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 (Shady Black, Rubber, Plastic):zot-def-c56204;ZOTIKOS Book Cover for MOTOROLA MOTO E4 PLUS (Black, Rubber, Plastic):zot-fcb-c61097;ZOTIKOS Back Cover for Oppo F1s (Transparent, Rubber, Plastic):zot-tp-c53529;ZOTIKOS Front & Back Case for MICROMAX EVOK POWER Q4260 (Black, Rubber, Plastic):zot-fcb-c60467;ZOTIKOS Back Replacement Cover for MICROMAX EVOK POWER Q4260 (Smooth Transparent, Rubber, Plastic):zot-tp-c60552;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 (Transparent, Rubber, Plastic):zot-tp-c56458;ZOTIKOS Flip Cover for VIVO V5 Plus (Gold, Rubber, Plastic):zot-fcg-c48780;ZOTIKOS Flip Cover for OnePlus 3T (Black, Rubber, Plastic):zot-fcb-c34237;ZOTIKOS Flip Cover for LG Q6 (Shady Gold, Rubber, Plastic):zot-fcg-a66207;ZOTIKOS Back Replacement Cover for MICROMAX EVOK NOTE E453 (Transparent, Rubber, Plastic):zot-tp-c60738;ZOTIKOS Flip Cover for VIVO Y53 (Black, Rubber, Plastic):zot-fcb-c49581;ZOTIKOS Flip Cover for Oppo F1s (Black, Rubber, Plastic):zot-fcb-c53359;ZOTIKOS Back Cover for OnePlus 2 (Transparent, Rubber, Plastic):zot-tp-c33439;ZOTIKOS Back Replacement Cover for SAMSUNG Z4 (Arc Black, Rubber, Plastic):zot-chr-c60330;ZOTIKOS Flip Cover for OnePlus 3 (Black, Rubber, Plastic):zot-fcb-c33736;ZOTIKOS Back Cover for Lenovo Vibe K5 Note (Black, Rubber, Plastic):zot-chr-c54413;ZOTIKOS Back Replacement Cover for Infinix Note 4 (Smooth Transparent, Rubber, Plastic):zot-tp-a65452;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 PLUS (Transparent, Rubber, Plastic):zot-tp-c61231;ZOTIKOS Back Cover for MOTOROLA MOTO C PLUS (Gold, Rubber, Plastic):zot-crm-c58446;ZOTIKOS Back Cover for Samsung Galaxy J2 Ace (Gold, Rubber, Plastic):zot-crm-c43497;ZOTIKOS Flip Cover for iPhone 7 Plus (Gold, Rubber, Plastic):zot-fcg-c24165;ZOTIKOS Book Cover for LG Q6 (Shady Gold, Rubber, Plastic):zot-fcg-a66221;ZOTIKOS Front & Back Case for Samsung Galaxy J1 ace (Black, Rubber, Plastic):zot-fcb-c2221;ZOTIKOS Book Cover for Infinix Note 4 (Pure Gold, Rubber, Plastic):zot-fcg-a65423;ZOTIKOS Back Cover for MOTOROLA MOTO C (Transparent, Rubber, Plastic):zot-tp-c58317;ZOTIKOS Flip Cover for LG Q6 (Zync Black, Rubber, Plastic):zot-fcb-a66180;ZOTIKOS Book Cover for SAMSUNG GALAXY J7 MAX (Gold, Rubber, Plastic):zot-fcg-c61797;ZOTIKOS Flip Cover for Samsung Galaxy J7 Prime (Black, Rubber, Plastic):zot-fcb-c5242;ZOTIKOS Flip Cover for SAMSUNG GALAXY J7 MAX (Gold, Rubber, Plastic):zot-fcg-c61759;ZOTIKOS Back Cover for VIVO V5 (Black, Rubber, Plastic):zot-def-c46697;ZOTIKOS Back Replacement Cover for VIVO V5S (Gold, Rubber, Plastic):zot-crm-c55853;ZOTIKOS Back Cover for Samsung Galaxy J2 (Transparent, Rubber, Plastic):zot-tp-c3279;ZOTIKOS Front & Back Case for Samsung C9 Pro (Black, Rubber, Plastic):zot-fcb-c38907;ZOTIKOS Front & Back Case for Redmi Note 4 (Black, Rubber, Plastic):zot-fcb-c14034;ZOTIKOS Back Cover for iPhone 7 (Gold, Rubber, Plastic):zot-crm-c16780;ZOTIKOS Back Cover for Lenovo Vibe K5 Note (Black, Rubber, Plastic):zot-chr-c54418;ZOTIKOS Front & Back Case for Lenovo K6 Power (Iridescent Black, Rubber, Plastic):zot-fcb-c32113;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy J3 Pro (Black, Rubber, Plastic):zot-chr-c58910;ZOTIKOS Front & Back Case for MOTOROLA MOTO C PLUS (Shiny Black, Rubber, Plastic):zot-fcb-c58580;ZOTIKOS Flip Cover for OnePlus 5 (Black, Rubber, Plastic):zot-fcb-c60763;ZOTIKOS Flip Cover for Lenovo K8 Note (Gold, Rubber, Plastic):zot-fcg-a65788;ZOTIKOS Back Cover for Samsung Z2 (Black, Rubber, Plastic):zot-def-c30777;ZOTIKOS Flip Cover for iPhone 7 (Black Flip Cover, Rubber, Plastic):zot-fcb-c16975;ZOTIKOS Flip Cover for Lenovo Vibe K5 Note (Gold, Rubber, Plastic):zot-fcg-c54982;ZOTIKOS Flip Cover for MOTOROLA MOTO E4 (Black, Rubber, Plastic):zot-fcb-c61388;ZOTIKOS Back Cover for VIVO V5 Plus (Black, Rubber, Plastic):zot-chr-c48191;ZOTIKOS Back Replacement Cover for OnePlus 5 (Transparent, Rubber, Plastic):zot-tp-c60891;ZOTIKOS Back Cover for VIVO Y55L (Black, Rubber, Plastic):zot-def-c50340;ZOTIKOS Front & Back Case for MOTOROLA MOTO C PLUS (Gold, Rubber, Plastic):zot-fcg-c58624;ZOTIKOS Front & Back Case for Samsung Galaxy J2 Ace (Shady Black, Rubber, Plastic):zot-fcb-c43788;ZOTIKOS Flip Cover for Redmi 3S Prime (Black, Rubber, Plastic):zot-fcb-c10371;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 (Black, Rubber, Plastic):zot-chr-c61278;ZOTIKOS Bumper Case for MOTOROLA MOTO E4 (Black, Rubber, Plastic):zot-def-c61373;ZOTIKOS Front & Back Case for Motorola Moto G5 Plus (Black, Rubber, Plastic):zot-fcb-c39580;ZOTIKOS Back Cover for VIVO Y53 (Black, Rubber, Plastic):zot-def-c49418;ZOTIKOS Front & Back Case for Redmi 3S Prime (Zync Gold, Rubber, Plastic):zot-fcg-c10542;ZOTIKOS Front & Back Case for Redmi 3S Prime (Black, Rubber, Plastic):zot-fcb-c10405;ZOTIKOS Back Replacement Cover for OPPO F3 (Transparent, Rubber, Plastic):zot-tp-c55657;ZOTIKOS Book Cover for SAMSUNG GALAXY J7 Pro (Gold, Rubber, Plastic):zot-fcg-c64664;ZOTIKOS Book Cover for Infinix Hot 4 Pro (Zync Gold, Rubber, Plastic):zot-fcg-a65233;ZOTIKOS Back Cover for MOTOROLA Z2 PLAY (Black, Rubber, Plastic):zot-def-c59265;ZOTIKOS Back Cover for Nokia 3 (Black, Rubber, Plastic):zot-chr-c59535;ZOTIKOS Flip Cover for Lenovo K8 Note (Black, Rubber, Plastic):zot-fcb-a65634;ZOTIKOS Flip Cover for MICROMAX EVOK POWER Q4260 (Gold, Rubber, Plastic):zot-fcg-c60489;ZOTIKOS Back Replacement Cover for MOTOROLA Z2 PLAY (Transparent, Rubber, Plastic):zot-tp-c59515;ZOTIKOS Back Cover for Redmi 3S Prime (Gold, Rubber, Plastic):zot-crm-c10145;ZOTIKOS Back Cover for OPPO A37 (Black, Rubber, Plastic):zot-chr-c51818;ZOTIKOS Back Cover for VIVO Y55L (Transparent, Rubber, Plastic):zot-tp-c50746;ZOTIKOS Back Replacement Cover for VIVO V5S (Black, Rubber, Plastic):zot-def-c55922;ZOTIKOS Back Cover for Yu Yunique 2 (Gold, Rubber, Plastic):zot-crm-c64938;ZOTIKOS Back Cover for MOTOROLA Z2 PLAY (Transparent, Rubber, Plastic):zot-tp-c59471;ZOTIKOS Book Cover for SAMSUNG Z4 (Gold, Rubber, Plastic):zot-fcg-c60431;ZOTIKOS Back Cover for iPhone 7 (Transparent, Rubber, Plastic):zot-tp-c17107;ZOTIKOS Front & Back Case for Redmi Note 4 (Smooth Gold, Rubber, Plastic):zot-fcg-c14185;ZOTIKOS Flip Cover for MOTOROLA MOTO C (Gold, Rubber, Plastic):zot-fcg-c58275;ZOTIKOS Book Cover for VIVO V5S (Black, Rubber, Plastic):zot-fcb-c56012;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Shiny Black, Rubber, Plastic):zot-fcb-a65172;ZOTIKOS Front & Back Case for OnePlus 3 (Brash Black, Rubber, Plastic):zot-fcb-c33765;ZOTIKOS Back Cover for VIVO V5S (Transparent, Rubber, Plastic):zot-tp-c56059;ZOTIKOS Back Cover for Lenovo K6 Power (Black, Rubber, Plastic):zot-def-c31908;ZOTIKOS Front & Back Case for MOTOROLA MOTO E4 (Gold, Rubber, Plastic):zot-fcg-c61460;ZOTIKOS Back Cover for Lenovo K6 Note (Gold, Rubber, Plastic):zot-crm-c31038;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO C (Smooth Black, Rubber, Plastic):zot-chr-c58105;ZOTIKOS Flip Cover for Lenovo K8 Note (Flamboyant Black, Rubber, Plastic):zot-fcb-a65769;ZOTIKOS Flip Cover for Infinix Note 4 (Zync Black, Rubber, Plastic):zot-fcb-a65360;ZOTIKOS Flip Cover for Redmi 3S Prime (Black, Rubber, Plastic):zot-fcb-c10376;ZOTIKOS Back Cover for VIVO V5S (Transparent, Rubber, Plastic):zot-tp-c56069;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Black, Rubber, Plastic):zot-fcb-a65144;ZOTIKOS Back Cover for Infinix Note 4 (Black, Rubber, Plastic):zot-chr-a65276;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO C (Black, Rubber, Plastic):zot-chr-c58111;ZOTIKOS Bumper Case for Yu Yunique 2 (Black, Rubber, Plastic):zot-def-c65000;ZOTIKOS Back Cover for VIVO V5S (Black, Rubber, Plastic):zot-chr-c55687;ZOTIKOS Flip Cover for Samsung Galaxy J7 (Black Flip Cover, Rubber, Plastic):zot-fcb-c3699;ZOTIKOS Back Cover for Lenovo P2 (Transparent, Rubber, Plastic):zot-tp-c35669;ZOTIKOS Bumper Case for MOTOROLA MOTO E4 (Black, Rubber, Plastic):zot-def-c61383;ZOTIKOS Back Cover for Samsung Galaxy A5 (2017) (Transparent, Rubber, Plastic):zot-tp-c24584;ZOTIKOS Book Cover for Yu Yunique 2 (Black, Rubber, Plastic):zot-fcb-c65042;ZOTIKOS Back Cover for OPPO A37 (Black, Rubber, Plastic):zot-def-c52008;ZOTIKOS Front & Back Case for Redmi Note 4 (Black, Rubber, Plastic):zot-fcb-c14037;ZOTIKOS Flip Cover for SAMSUNG Galaxy J3 Pro (Black, Rubber, Plastic):zot-fcb-c59006;ZOTIKOS Back Cover for VIVO Y66 (Black, Rubber, Plastic):zot-chr-c50911;ZOTIKOS Back Cover for Honor 6x (Black, Rubber, Plastic):zot-def-c35837;ZOTIKOS Bumper Case for SAMSUNG GALAXY J7 Pro (Black, Rubber, Plastic):zot-def-c64572;ZOTIKOS Back Replacement Cover for OPPO F3 (Crystal Black, Rubber, Plastic):zot-def-c55462;ZOTIKOS Back Replacement Cover for Infinix Note 4 (Arc Black, Rubber, Plastic):zot-def-a65317;ZOTIKOS Back Cover for OPPO F3 (Black, Rubber, Plastic):zot-chr-c55257;ZOTIKOS Back Cover for Oppo F1s (Gold, Rubber, Plastic):zot-crm-c53100;ZOTIKOS Back Cover for Samsung C9 Pro (Transparent, Rubber, Plastic):zot-tp-c39084;ZOTIKOS Back Cover for Samsung Galaxy J5 Prime (Black, Rubber, Plastic):zot-chr-c25444;ZOTIKOS Front & Back Case for Infinix Hot 4 Pro (Pure Black, Rubber, Plastic):zot-fcb-a65175;ZOTIKOS Back Cover for iPhone 7 Plus (Gold, Rubber, Plastic):zot-crm-c24031;ZOTIKOS Back Replacement Cover for SAMSUNG GALAXY J7 Pro (Transparent, Rubber, Plastic):zot-tp-c64706;ZOTIKOS Back Cover for Samsung Galaxy J1 (4G) (Black, Rubber, Plastic):zot-def-c42718;ZOTIKOS Back Cover for SAMSUNG Galaxy S8 Plus (Black, Rubber, Plastic):zot-def-c56529;ZOTIKOS Back Cover for Yu Yunique 2 (Black, Rubber, Plastic):zot-chr-c64900;ZOTIKOS Front & Back Case for iPhone 7 Plus (Black, Rubber, Plastic):zot-fcb-c24120;ZOTIKOS Back Cover for Lenovo K6 Power (Black, Rubber, Plastic):zot-def-c31905;ZOTIKOS Flip Cover for VIVO Y55L (Black, Rubber, Plastic):zot-fcb-c50504;ZOTIKOS Back Cover for SAMSUNG GALAXY J7 MAX (Transparent, Rubber, Plastic):zot-tp-c61808;ZOTIKOS Back Cover for Motorola Moto E3 Power (Transparent, Rubber, Plastic):zot-tp-c41672;ZOTIKOS Flip Cover for iPhone 7 Plus (Black, Rubber, Plastic):zot-fcb-c24106;ZOTIKOS Flip Cover for Lenovo K8 Note (Splashy Gold, Rubber, Plastic):zot-fcg-a65906;ZOTIKOS Back Cover for VIVO V5S (Transparent, Rubber, Plastic):zot-tp-c56076;ZOTIKOS Flip Cover for OPPO F3 (Black, Rubber, Plastic):zot-fcb-c55509;ZOTIKOS Front & Back Case for Infinix Hot 4 Pro (Soft Black, Rubber, Plastic):zot-fcb-a65174;ZOTIKOS Back Cover for MOTOROLA MOTO E4 (Black, Rubber, Plastic):zot-def-c61294;ZOTIKOS Flip Cover for Lenovo K8 Note (Black, Rubber, Plastic):zot-fcb-a65649;ZOTIKOS Flip Cover for Infinix Note 4 (Gold, Rubber, Plastic):zot-fcg-a65386;ZOTIKOS Back Replacement Cover for SAMSUNG GALAXY J7 Pro (Black, Rubber, Plastic):zot-chr-c64518;ZOTIKOS Front & Back Case for iPhone 7 Plus (Smooth Black, Rubber, Plastic):zot-fcb-c24124;ZOTIKOS Back Cover for SAMSUNG GALAXY J7 MAX (Transparent, Rubber, Plastic):zot-tp-c61804;ZOTIKOS Back Cover for MOTOROLA MOTO C (Black, Rubber, Plastic):zot-def-c58121;ZOTIKOS Flip Cover for Infinix Hot 4 Pro (Crystal Black, Rubber, Plastic):zot-fcb-a65194;ZOTIKOS Back Cover for Motorola Moto E3 Power (Black, Rubber, Plastic):zot-def-c41283;ZOTIKOS Back Cover for VIVO V5 (Black, Rubber, Plastic):zot-chr-c46338;ZOTIKOS Flip Cover for Lenovo K6 Power (Black, Rubber, Plastic):zot-fcb-c32046;ZOTIKOS Bumper Case for LG Q6 (Gaily Black, Rubber, Plastic):zot-def-a66132;ZOTIKOS Front & Back Case for Lenovo K8 Note (Iridescent Black, Rubber, Plastic):zot-fcb-a65719;ZOTIKOS Back Cover for Motorola Moto G5 Plus (Black, Rubber, Plastic):zot-def-c39416;ZOTIKOS Back Cover for VIVO V5S (Black, Rubber, Plastic):zot-def-c55866;ZOTIKOS Back Cover for OPPO A37 (Gold, Rubber, Plastic):zot-crm-c51940;ZOTIKOS Back Cover for Redmi 3S Prime (Black, Rubber, Plastic):zot-def-c10252;ZOTIKOS Flip Cover for Samsung Galaxy On Nxt (Gold, Rubber, Plastic):zot-fcg-c44694;ZOTIKOS Back Cover for VIVO Y66 (Black, Rubber, Plastic):zot-chr-c50927;ZOTIKOS Back Replacement Cover for Lenovo K8 Note (Zync Gold, Rubber, Plastic):zot-crm-a65547;ZOTIKOS Front & Back Case for Samsung Galaxy J7 (Black, Rubber, Plastic):zot-fcb-c3760;ZOTIKOS Front & Back Case for Google Pixel XL (Black, Rubber, Plastic):zot-fcb-c35278;ZOTIKOS Flip Cover for MOTOROLA MOTO C PLUS (Black, Rubber, Plastic):zot-fcb-c58553;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 Plus (Transparent, Rubber, Plastic):zot-tp-c56809;ZOTIKOS Back Replacement Cover for MICROMAX EVOK NOTE E453 (Gold, Rubber, Plastic):zot-crm-c60597;ZOTIKOS Back Cover for Samsung Galaxy J2 (Black, Rubber, Plastic):zot-chr-c2504;ZOTIKOS Back Replacement Cover for Yu Yunique 2 (Black, Rubber, Plastic):zot-chr-c64936;ZOTIKOS Flip Cover for MICROMAX EVOK POWER Q4260 (Black, Rubber, Plastic):zot-fcb-c60445;ZOTIKOS Back Cover for Motorola Moto E3 Power (Black, Rubber, Plastic):zot-def-c41296;ZOTIKOS Flip Cover for MOTOROLA MOTO C (Black, Rubber, Plastic):zot-fcb-c58263;ZOTIKOS Front & Back Case for Lenovo K6 Power (Gold, Rubber, Plastic):zot-fcg-c32296;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO C PLUS (Black, Rubber, Plastic):zot-chr-c58439;ZOTIKOS Back Cover for Samsung Galaxy J1 ace (Transparent Back Cover, Rubber, Plastic):zot-tp-c2442;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO C PLUS (Black, Rubber, Plastic):zot-chr-c58445;ZOTIKOS Back Cover for MOTOROLA Z2 PLAY (Transparent, Rubber, Plastic):zot-tp-c59466;ZOTIKOS Back Cover for MOTOROLA MOTO E4 (Transparent, Rubber, Plastic):zot-tp-c61480;ZOTIKOS Front & Back Case for MOTOROLA MOTO E4 (Black, Rubber, Plastic):zot-fcb-c61417;ZOTIKOS Back Replacement Cover for LG Q6 (Shady Transparent, Rubber, Plastic):zot-tp-a66258;ZOTIKOS Back Cover for Lenovo Vibe K5 Note (Black, Rubber, Plastic):zot-chr-c54412;ZOTIKOS Flip Cover for Oppo F3 Plus (Black, Rubber, Plastic):zot-fcb-c54127;ZOTIKOS Front & Back Case for Yu Yunique 2 (Shiny Black, Rubber, Plastic):zot-fcb-c65027;ZOTIKOS Back Cover for Redmi 3S Prime (Gold Back Cover, Rubber, Plastic):zot-crm-c10143;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 (Black, Rubber, Plastic):zot-def-c56207;ZOTIKOS Front & Back Case for MOTOROLA MOTO E4 PLUS (Soft Black, Rubber, Plastic):zot-fcb-c61083;ZOTIKOS Back Cover for VIVO Y66 (Black, Rubber, Plastic):zot-chr-c50909;ZOTIKOS Flip Cover for Samsung Galaxy J5 Prime (Gold, Rubber, Plastic):zot-fcg-c25663;ZOTIKOS Book Cover for SAMSUNG GALAXY J7 MAX (Gold, Rubber, Plastic):zot-fcg-c61795;ZOTIKOS Back Cover for Oppo F1s (Gold, Rubber, Plastic):zot-crm-c53097;ZOTIKOS Front & Back Case for VIVO V5 (Arc Black, Rubber, Plastic):zot-fcb-c46888;ZOTIKOS Back Replacement Cover for MOTOROLA Z2 PLAY (Black, Rubber, Plastic):zot-def-c59325;ZOTIKOS Back Cover for VIVO Y53 (Gold, Rubber, Plastic):zot-crm-c49305;ZOTIKOS Front & Back Case for Samsung Galaxy J7 (Brash Black Front & Back Case, Rubber, Plastic):zot-fcb-c3757;ZOTIKOS Flip Cover for Lenovo K6 Note (Black, Rubber, Plastic):zot-fcb-c31252;ZOTIKOS Back Cover for VIVO V5 (Transparent, Rubber, Plastic):zot-tp-c47093;ZOTIKOS Back Cover for Samsung Galaxy J2 Ace (Black, Rubber, Plastic):zot-def-c43634;ZOTIKOS Back Cover for VIVO V5S (Black, Rubber, Plastic):zot-chr-c55682;ZOTIKOS Back Cover for iPhone 7 Plus (Transparent, Rubber, Plastic):zot-tp-c24236;ZOTIKOS Flip Cover for SAMSUNG GALAXY J7 Pro (Gold, Rubber, Plastic):zot-fcg-c64647;ZOTIKOS Front & Back Case for Samsung Galaxy J5 Prime (Gold, Rubber, Plastic):zot-fcg-c25676;ZOTIKOS Back Replacement Cover for SAMSUNG Z4 (Black, Rubber, Plastic):zot-chr-c60332;ZOTIKOS Back Cover for VIVO Y66 (Black, Rubber, Plastic):zot-chr-c50907;ZOTIKOS Bumper Case for OPPO F3 (Black, Rubber, Plastic):zot-def-c55494;ZOTIKOS Back Cover for Yu Yunique 2 (Black, Rubber, Plastic):zot-chr-c64895;ZOTIKOS Front & Back Case for VIVO V5 (Black, Rubber, Plastic):zot-fcb-c46893;ZOTIKOS Back Cover for OPPO F3 (Black, Rubber, Plastic):zot-def-c55406;ZOTIKOS Flip Cover for MOTOROLA MOTO C (Gold, Rubber, Plastic):zot-fcg-c58274;ZOTIKOS Back Cover for Infinix Hot 4 Pro (Black, Rubber, Plastic):zot-def-a65130;ZOTIKOS Front & Back Case for Lenovo K6 Power (Gold, Rubber, Plastic):zot-fcg-c32300;ZOTIKOS Book Cover for SAMSUNG GALAXY ON MAX (Black, Rubber, Plastic):zot-fcb-c62080;ZOTIKOS Book Cover for MICROMAX EVOK NOTE E453 (Gold, Rubber, Plastic):zot-fcg-c60704;ZOTIKOS Back Cover for Samsung Galaxy J1 (4G) (Gold, Rubber, Plastic):zot-crm-c42587;ZOTIKOS Flip Cover for OnePlus 3 (Black, Rubber, Plastic):zot-fcb-c33738;ZOTIKOS Flip Cover for Samsung Galaxy J7 (Gold, Rubber, Plastic):zot-fcg-cc3885;ZOTIKOS Back Cover for SAMSUNG Galaxy S8 (Transparent, Rubber, Plastic):zot-tp-c56350;ZOTIKOS Back Cover for MOTOROLA Z2 PLAY (Black, Rubber, Plastic):zot-def-c59270;ZOTIKOS Flip Cover for Samsung Galaxy On Nxt (Gold, Rubber, Plastic):zot-fcg-c44690;ZOTIKOS Back Cover for VIVO V5 (Black, Rubber, Plastic):zot-chr-c46349;ZOTIKOS Front & Back Case for Lenovo K8 Note (Shady Black, Rubber, Plastic):zot-fcb-a65726;ZOTIKOS Front & Back Case for Samsung Galaxy J2 Ace (Gold, Rubber, Plastic):zot-fcg-c43982;ZOTIKOS Flip Cover for Samsung Galaxy A5 (2016) (Black, Rubber, Plastic):zot-fcb-c29845;ZOTIKOS Flip Cover for Honor 6x (Black, Rubber, Plastic):zot-fcb-c35920;ZOTIKOS Flip Cover for Samsung Galaxy J1 (4G) (Black, Rubber, Plastic):zot-fcb-c42834;ZOTIKOS Front & Back Case for Samsung Galaxy A5 (2016) (Shady Black, Rubber, Plastic):zot-fcb-c29868;ZOTIKOS Front & Back Case for SAMSUNG Galaxy J3 Pro (Black, Rubber, Plastic):zot-fcb-c59070;ZOTIKOS Back Cover for SAMSUNG GALAXY J7 MAX (Transparent, Rubber, Plastic):zot-tp-c61811;ZOTIKOS Book Cover for Lenovo K8 Note (Gaily Black, Rubber, Plastic):zot-fcb-a65770;ZOTIKOS Flip Cover for Lenovo K8 Note (Black, Rubber, Plastic):zot-fcb-a65647;ZOTIKOS Back Cover for Oppo F3 Plus (Black, Rubber, Plastic):zot-chr-c53658;ZOTIKOS Bumper Case for MOTOROLA MOTO E4 (Black, Rubber, Plastic):zot-def-c61371;ZOTIKOS Book Cover for Infinix Hot 4 Pro (Soft Gold, Rubber, Plastic):zot-fcg-a65236;ZOTIKOS Flip Cover for Samsung C9 Pro (Gold, Rubber, Plastic):zot-fcg-c38996;ZOTIKOS Back Replacement Cover for MOTOROLA MOTO E4 PLUS (Black, Rubber, Plastic):zot-def-c61005;ZOTIKOS Back Cover for VIVO V5 Plus (Black, Rubber, Plastic):zot-def-c48518;ZOTIKOS Back Cover for VIVO V5 (Black, Rubber, Plastic):zot-chr-c46348;ZOTIKOS Back Cover for Lenovo Vibe K5 Note (Transparent, Rubber, Plastic):zot-tp-c55059;ZOTIKOS Flip Cover for MOTOROLA MOTO E4 (Black, Rubber, Plastic):zot-fcb-c61385;ZOTIKOS Flip Cover for MOTOROLA MOTO C PLUS (Gold, Rubber, Plastic):zot-fcg-c58608;ZOTIKOS Flip Cover for SAMSUNG Galaxy J3 Pro (Gold, Rubber, Plastic):zot-fcg-c59144;ZOTIKOS Flip Cover for Infinix Note 4 (Black, Rubber, Plastic):zot-fcb-a65341;ZOTIKOS Back Replacement Cover for Infinix Note 4 (Zync Black, Rubber, Plastic):zot-def-a65318;ZOTIKOS Book Cover for SAMSUNG Galaxy S8 (Gold, Rubber, Plastic):zot-fcg-c56311;ZOTIKOS Book Cover for MICROMAX EVOK NOTE E453 (Gold, Rubber, Plastic):zot-fcg-c60705;ZOTIKOS Back Cover for Honor 6x (Black, Rubber, Plastic):zot-def-c35838;ZOTIKOS Back Cover for Samsung Galaxy A5 (2017) (Gold, Rubber, Plastic):zot-crm-c24352;ZOTIKOS Back Cover for MOTOROLA MOTO E4 (Black, Rubber, Plastic):zot-chr-c61246;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 Plus (Transparent, Rubber, Plastic):zot-tp-c56792;ZOTIKOS Book Cover for MOTOROLA Z2 PLAY (Gold, Rubber, Plastic):zot-fcg-c59462;ZOTIKOS Back Replacement Cover for OPPO F3 (Black, Rubber, Plastic):zot-def-c55470;ZOTIKOS Flip Cover for VIVO V5S (Gold, Rubber, Plastic):zot-fcg-c56018;ZOTIKOS Front & Back Case for Samsung Galaxy On Nxt (Gold, Rubber, Plastic):zot-fcg-c44727;ZOTIKOS Bumper Case for OPPO F3 (Black, Rubber, Plastic):zot-def-c55507;ZOTIKOS Back Cover for MOTOROLA MOTO C PLUS (Black, Rubber, Plastic):zot-chr-c58406;ZOTIKOS Front & Back Case for Samsung Galaxy J2 (Gold, Rubber, Plastic):zot-fcg-c3134;ZOTIKOS Book Cover for LG Q6 (Arc Gold, Rubber, Plastic):zot-fcg-a66222;ZOTIKOS Back Cover for SAMSUNG GALAXY J7 Pro (Gold, Rubber, Plastic):zot-crm-c64523;ZOTIKOS Back Cover for Samsung Galaxy J2 (Gold, Rubber, Plastic):zot-crm-c2663;ZOTIKOS Flip Cover for Motorola Moto M (Gold, Rubber, Plastic):zot-fcg-c12717;ZOTIKOS Back Cover for SAMSUNG Galaxy S8 (Transparent, Rubber, Plastic):zot-tp-c56349;ZOTIKOS Back Cover for Samsung Galaxy J7 (Black, Rubber, Plastic):zot-chr-cc3413;ZOTIKOS Back Cover for Samsung Galaxy J2 (Gold, Rubber, Plastic):zot-crm-c2668;ZOTIKOS Front & Back Case for Samsung C9 Pro (Black, Rubber, Plastic):zot-fcb-c38911;ZOTIKOS Front & Back Case for Infinix Note 4 (Smooth Gold, Rubber, Plastic):zot-fcg-a65407;ZOTIKOS Flip Cover for MICROMAX EVOK POWER Q4260 (Black, Rubber, Plastic):zot-fcb-c60444;ZOTIKOS Back Cover for SAMSUNG GALAXY ON MAX (Gold, Rubber, Plastic):zot-crm-c61930;ZOTIKOS Flip Cover for Oppo F1s (Gold, Rubber, Plastic):zot-fcg-c53444;ZOTIKOS Book Cover for MOTOROLA MOTO E4 PLUS (Black, Rubber, Plastic):zot-fcb-c61099;ZOTIKOS Flip Cover for Lenovo K8 Note (Black, Rubber, Plastic):zot-fcb-a65648;ZOTIKOS Back Cover for Lenovo Vibe K5 Note (Transparent, Rubber, Plastic):zot-tp-c55054;ZOTIKOS Bumper Case for MOTOROLA MOTO E4 (Black, Rubber, Plastic):zot-def-c61369;ZOTIKOS Back Cover for Redmi Note 4 (Black, Rubber, Plastic):zot-def-c13948;ZOTIKOS Front & Back Case for Lenovo P2 (Gold, Rubber, Plastic):zot-fcg-c35601;ZOTIKOS Front & Back Case for Lenovo K6 Power (Gold, Rubber, Plastic):zot-fcg-c32304;ZOTIKOS Back Cover for VIVO V5 (Transparent, Rubber, Plastic):zot-tp-c47082;ZOTIKOS Front & Back Case for Motorola Moto E3 Power (Black, Rubber, Plastic):zot-fcb-c41488;ZOTIKOS Back Replacement Cover for OnePlus 5 (Black, Rubber, Plastic):zot-chr-c60755;ZOTIKOS Back Cover for Samsung Galaxy A5 (2016) (Black, Rubber, Plastic):zot-def-c29792;ZOTIKOS Book Cover for OnePlus 5 (Black, Rubber, Plastic):zot-fcb-c60801;ZOTIKOS Back Cover for Samsung Galaxy J1 (4G) (Black, Rubber, Plastic):zot-chr-c42430;ZOTIKOS Front & Back Case for Lenovo K8 Note (Shady Black, Rubber, Plastic):zot-fcb-a65706;ZOTIKOS Back Cover for SAMSUNG GALAXY J7 MAX (Gold, Rubber, Plastic):zot-crm-c61599;ZOTIKOS Back Cover for VIVO V5 Plus (Transparent, Rubber, Plastic):zot-tp-c48869;ZOTIKOS Back Cover for MOTOROLA MOTO C (Black, Rubber, Plastic):zot-chr-c58069;ZOTIKOS Back Replacement Cover for SAMSUNG Galaxy S8 Plus (Transparent, Rubber, Plastic):zot-tp-c56797");
//		
//		thisObject.getShippingList("Mozette Back Cover for Motorola Moto G5 Plus (Mellow Transparent):mzt-tp-a39747;Mozette Back Cover for SAMSUNG Galaxy J5 Prime (Transparent, Gold):mzt-tp-crm-a19562");
//		
//		thisObject.getDifferentColors("Black::Transparent,Black,Black::Gold,Black");
//
//		//ACTIVE/INACTIVE List
//		thisObject.getUniquePhones("Mozette Flip Cover for Samsung Galaxy J3 (2016) (Gold);");
//		thisObject.generateFlipkartActiveInactiveList("Mozette Flip Cover for Samsung Galaxy J3 (2016) (Gold):mzt-fcg-a25081:ACTIVE;");
//		
//		//****************** Flipkart Extract QC Failed Items ********************
//		thisObject.extractQCFailed(new File("C:/Users/Alveena/Downloads/test"),"C:/Users/Alveena/Downloads/test");
//		
//		
//		//******************  Amazon *******************
//		//Should contain ###backup folder and it should directly contain the types folders
//		
//		thisObject.createAmazonListing("Grip Back Cover_Differnet Brand Color,Shock Proof Case,Back Replacement Cover,Shock Proof Case_Differnet Brand Color,Bumper Case,Flip Cover_Differnet Brand Color,Dual Protection Case,Back Cover,Back Cover_Differnet Brand Color,Grip Back Cover,Flip Cover,Flip Cover_Differnet Brand Color,Front & Back Case,Front & Back Case_Differnet Brand Color","D:/RMA Drive/Mobile Cases/Samsung/Samsung Galaxy J7 Prime/Combos,D:/RMA Drive/Mobile Cases/Samsung/Samsung Galaxy S7/Combos,D:/RMA Drive/Mobile Cases/Samsung/Samsung Galaxy A7 (2017)/Combos,D:/RMA Drive/Mobile Cases/Samsung/Samsung Galaxy J5 Prime/Combos,D:/RMA Drive/Mobile Cases/Samsung/Samsung Galaxy J2 Prime/Combos,D:/RMA Drive/Mobile Cases/Samsung/Samsung C9 Pro/Combos");//D:/RMA Drive/Mobile Cases/Lenovo/Lenovo K6 Power/###backup/Catalog
//		
//		//***************** PayTM **********************
//		thisObject.createPayTmListings("AMR","Flip Cover,Back Cover","D:/RMA Drive/Mobile Cases/Google/Google Pixel,D:/RMA Drive/Mobile Cases/Google/Google Pixel XL");
//		
//		//******************  Shop Clues *******************
//		//Run for each brand at a time as we need the Brand name in one argument
//		thisObject.createShopCluesListing("Xiaomi","Back Cover,Flip Cover","D:/RMA Drive/Mobile Cases/###Xiaomi_backup/Redmi Note 4");//D:/RMA Drive/Mobile Cases/Lenovo/Lenovo K6 Power/###backup/Catalog
//		
//		thisObject.pyTMUpload(new File("D:/RMA Drive/Mobile Cases/Samsung/SAMSUNG A9 Pro/Cherry/1.jpeg"));
	}

	private void createTemperedGlassListings(String mobileFolders) throws IOException {
		// TODO Auto-generated method stub
		String folders[] = mobileFolders.split(",");
		for(String eachFolder : folders){
			File mobile = new File(eachFolder);
			String mobileName = mobile.getName();
			File temperedGlasses = new File(eachFolder+"/Tempered Glass");
			if(temperedGlasses != null && temperedGlasses.exists()){
				resizeImagesForOnePhone(eachFolder+"/Tempered Glass");
				createTemperedGlassListngs(mobileName,temperedGlasses,eachFolder);
				File abc = new File(eachFolder+"/###backup/Tempered Glass");
				abc.getParentFile().mkdirs();
				FileUtils.copyDirectory(new File(eachFolder+"/Catalog/Tempered Glass"), abc);
			}
			
		}
		
		updateTemperedSkuCount();
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

	private void createComboImagesForTemperedGlasses(File eachType) {
		File box = new File(eachType.getAbsolutePath()+"/box");
		File[] boxImages = box.listFiles();
		File[] images = eachType.listFiles();
		if(images.length > 0 && boxImages.length > 0){
			for(File boxImage : boxImages){
				for(File image: images){
					if(image == null || image.isDirectory() || boxImage == null || boxImage.isDirectory()){
						continue;
					}
					createCombo(boxImage.getAbsolutePath(), image.getAbsolutePath(), eachType.getAbsolutePath()+"/"+boxImage.getName()+"_"+image.getName()+".jpeg",false);
				}
			}
		}
	}

	private void activateListings(String inactiveListings) {
		// TODO Auto-generated method stub
		
		String skus = "xol-chr-a52873:NONE;xol-chr-a24002:NONE;xol-fcb-a37428:NONE";
		for(String tokens : skus.split(";")){
			String[] arr = tokens.split(":");
			if(inactiveListings.contains(arr[0]+",")){
				System.out.println("NONE");
			}else{
				System.out.println(arr[1]);
			}
		}
		
	}

	private void createPayTmListings(String accountName,String types, String flipkartFolder) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		List<String> paytmValidobiles = getPayTMValidMobiles();
		System.out.println(paytmValidobiles.size());
		String[] eachPhone = flipkartFolder.split(",");
		for(String mobile : eachPhone){
			String mobileName = mobile.substring(mobile.lastIndexOf("/")+1);
			if(!paytmValidobiles.contains(mobileName)){
				continue;
			}
			File eachMobile = null;
			if(mobile.contains("Combos")){
//				eachMobile = new File(mobile+"/Catalog");
//				if(eachMobile.exists()){
//					File[] typrList = eachMobile.listFiles();
//					for(File cat : typrList){
//						if(cat.getName().startsWith("Final")){
//								List<Map<Integer, String>> listing = createEachAmazonComboListing(cat);
//								writeToAmazonFile(listing,mobile,cat.getName().replace(".txt", ""));
//						}
//					}
//				}else{
//					System.out.println("\n\n\n\n\n\n");
//					System.out.println("########################################################## Issue with : "+mobile);
//					System.out.println("\n\n\n\n\n\n");
//				}
			}else{
				eachMobile = new File(mobile+"/###backup");
				if(eachMobile.exists()){
					File[] typrList = eachMobile.listFiles();
					for(File type : typrList){
						for(String extractType : types.split(",")){
							File cat = new File(type.getAbsolutePath()+"/"+extractType+".txt");
							File copy = new File(mobile+"/###"+accountName+"_PayTM_completed/"+type.getName()+"/"+cat.getName());
							if(copy.exists()){
								continue;
							}
							copy.getParentFile().mkdirs();
							if(cat.exists()){
								List<Map<Integer, String>> listing = createEachPayTmListing(accountName,cat,type.getName());
								writeToPayTMFile(listing,mobile,extractType);
								FileUtils.copyFile(cat, copy);
							}
						}
					}
				}else{
					System.out.println("\n\n\n\n\n\n");
					System.out.println("########################################################## Issue with : "+mobile);
					System.out.println("\n\n\n\n\n\n");
				}
			}
		}
	}

	private void writeToPayTMFile(List<Map<Integer, String>> completeListings, String mobile, String extractType) throws IOException {
		// TODO Auto-generated method stub
		File amazonCat = new File(mobile+"/###Amazon_completed/Catalog/"+extractType+".txt");
		
		if(!amazonCat.exists()){
			amazonCat.getParentFile().mkdirs();
			amazonCat.createNewFile();
		}
	    
		if(completeListings.size() > 0){
			List<String> lines = new ArrayList<>();
			for(Map<Integer, String> row : completeListings){
				String writer = "";
				for(int cellPointer=0; cellPointer < 68; cellPointer++) {
			    	String cell = row.get(cellPointer);
			    	if(cell != null && !cell.isEmpty()){
			    		writer += cell+"\t";
			    		System.out.print(cell+"\t");
			    	}else{
			    		writer += "\t";
			    		System.out.print("\t");
			    	}
			    }
				System.out.print("\n");
				lines.add(writer);
			}
			FileUtils.writeLines(amazonCat, lines, true);
		}
	}

	private List<Map<Integer, String>> createEachPayTmListing(String accountName, File cat, String type) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		List<Map<Integer, String>> listing = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(cat))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	if(StringUtils.isNotBlank(line)){
		    		String[] flipkartLine = line.split("\t");
		    		Map<Integer, String> map = new HashMap<>();
		    		String brandName = accountVsBrandNames.get(accountName);
		    		String skuName = accountVsSkus.get(accountName);
		    		String title = brandName+" "+flipkartLine[4]+" "+flipkartLine[3]+" for "+flipkartLine[5];
		    		map.put(0, title);
		    		map.put(1, flipkartLine[0].replace("mzt-", skuName+"-"));
		    		map.put(2, "999");
		    		map.put(3, getPayTMPrice(flipkartLine[0]));
		    		map.put(4, "0.1");
		    		map.put(5, flipkartLine[11]);
		    		map.put(6, "3");
		    		map.put(7, flipkartLine[4]);
		    		map.put(8, getPayTMType(flipkartLine[3]));
		    		map.put(9, flipkartLine[5]);
		    		map.put(10, getPayTMMaterial(flipkartLine[0]));
		    		map.put(23, brandName);
		    		
		    		String images = "";
		    		if(StringUtils.isNotBlank(flipkartLine[12])){
		    			images += flipkartLine[12]+";";
		    		}
		    		if(StringUtils.isNotBlank(flipkartLine[13])){
		    			images += flipkartLine[13]+";";
		    		}
		    		if(StringUtils.isNotBlank(flipkartLine[14])){
		    			images += flipkartLine[14]+";";
		    		}
		    		
		    		if(images.indexOf(";") != -1){
		    			map.put(24, images.substring(0,images.length()-1));
		    		}else{
		    			map.put(24, images);
		    		}
		    		
		    		String description = getDescriptionMap().get(type);
					if(StringUtils.isNotBlank(description)){
						map.put(25, description.replaceAll("Mozette", brandName).replaceAll("<mobilename>", flipkartLine[5]));
					}
					
		    		map.put(26, "9");
		    		map.put(27, "6");
		    		map.put(28, "1");
		    		map.put(29, "1 Pc");
		    		map.put(30, "Yes");
		    		map.put(32, "Yes");
		    		
		    		if(flipkartLine[0].contains("-def")){
		    			map.put(33, "Yes");
		    		}
		    		listing.add(map);
		    	}
		    }
		}
		return listing;
	}

	private String getPayTMMaterial(String skus) {
		// TODO Auto-generated method stub
		Map<String, String> payTmTypes = new HashMap<>();
		payTmTypes.put("tp","Rubber;Silicone;Soft Case");
		payTmTypes.put("chr","Rubber;Silicone;Matte");
		payTmTypes.put("crm","Rubber;Silicone;Soft Case");
		payTmTypes.put("def","Rubber;Polycarbonate");
		payTmTypes.put("fcb","Leather;Plastic");
		payTmTypes.put("fcg","Leather;Plastic");

		Set<String> types= new TreeSet<>();
		
		String[] skuArr = skus.split("-");
		if(skuArr.length == 4){
			Collections.addAll(types, payTmTypes.get(skuArr[1]).split(";"));
			Collections.addAll(types, payTmTypes.get(skuArr[2]).split(";"));
		}else if(skuArr.length == 3){
			Collections.addAll(types, payTmTypes.get(skuArr[1]).split(";"));
		}
		
		String finalS = "";
		
		for(String tpe : types){
			finalS += tpe+";";
		}
		
		return finalS.substring(0, finalS.length()-1);
	}

	private String getPayTMPrice(String sku) {
		Map<String, String> rateMap = new HashMap<>();
		String[] rates = "tp:199,chr:219,crm:219,def:299,fcb:259,fcg:259,tp+chr:269,tp+def:319,tp+fcb:275,tp+fcg:275,chr+def:355,chr+fcb:299,chr+fcg:299,def+fcb:399,def+fcg:399,fcb+fcg:349,tp+tp:249,chr+chr:275,def+def:399,fcb+fcb:349,fcg+fcg:349,tp+crm:269,crm+crm:275,chr+crm:275,def+crm:355,fcb+crm:299,fcg+crm:299".split(",");
		for(String rate : rates){
			rateMap.put(rate.split(":")[0], rate.split(":")[1]);
		}
		String[] tokens = sku.split("-");
		
		if(tokens.length > 3){
			if(rateMap.containsKey(tokens[1]+"+"+tokens[2])){
				return rateMap.get(tokens[1]+"+"+tokens[2]);
			}else if(rateMap.containsKey(tokens[2]+"+"+tokens[1])){
				return rateMap.get(tokens[2]+"+"+tokens[1]);
			}else{
				System.out.println("missed");
				return null;
			}
		}else{
			if(rateMap.containsKey(tokens[1])){
				return rateMap.get(tokens[1]);
			}else{
				System.out.println("missed");
				return null;
			}
		}
	}

	private Map<String, String> loadPayTMType() {
		// TODO Auto-generated method stub
		Map<String, String> payTmTypes = new HashMap<>();
		payTmTypes.put("Anti-radiation Case","Full Protection Case");
		payTmTypes.put("Back Cover","Back Cover");
		payTmTypes.put("Back Replacement Cover","Back Cover");
		payTmTypes.put("Book Cover","Book Cover");
		payTmTypes.put("Bumper Case","Bumper");
		payTmTypes.put("Cases with Holder","Kickstand");
		payTmTypes.put("Dot View Case","Back Cover");
		payTmTypes.put("Dual Protection Case","Full Protection Case");
		payTmTypes.put("Flip Cover","Flip Cover");
		payTmTypes.put("Front & Back Case","Front Cover");
		payTmTypes.put("Front Cover","Front Cover");
		payTmTypes.put("Grip Back Cover","Back Cover");
		payTmTypes.put("Shock Proof Case","Armor Case");
		return payTmTypes;
	}
	
	

	private String getPayTMType(String flipkartType) {
		// TODO Auto-generated method stub
		return loadPayTMType().get(flipkartType);
	}

	private void getUniquePhones(String productDetails) {
		// TODO Auto-generated method stub
		TreeMap<String, String> map = new TreeMap<>();
		for(String eachProduct : productDetails.split(";")){
			String[] productArr = eachProduct.split(":");
			String productDesc = productArr[0].substring(productArr[0].indexOf("for ")+4, productArr[0].lastIndexOf(" (")).trim();
			map.put(productDesc, "");
		}
		for(String key : map.keySet()){
			System.out.println(key);
		}
		
	}

	private void generateFlipkartActiveInactiveList(String productDetails) {
		// TODO Auto-generated method stub
		Map<String, Map<String, String>> activeList = new TreeMap<>();
		Map<String, Map<String, String>> inactiveList = new TreeMap<>();
		Map<String, Map<String, String>> partialList = new TreeMap<>();
		

		Map<String, String> legacyList = new TreeMap<>();
		for(String eachProduct : productDetails.split(";")){
			String[] productArr = eachProduct.split(":");
			String productDesc = productArr[0].substring(productArr[0].indexOf("for ")+4, productArr[0].lastIndexOf(" (")).trim();
//			tempList.put(productDesc.trim(), "");
			String activeOrInactive = productArr[2].equals("ACTIVE") ? "A" : "I";
			if(productArr[1].indexOf("-") == -1){
				//Add to legacy list if not exists else append with comma
				if(legacyList.get(productDesc) != null){
					String value = legacyList.get(productDesc);
					if(!value.equals(activeOrInactive)){
						legacyList.put(productDesc, value+","+activeOrInactive);
					}
				}else{
					legacyList.put(productDesc, activeOrInactive);
				}
				continue;
			}
			List<String> types = new ArrayList<>();
			String[] caseTypes = productArr[1].split("-");
			if(caseTypes.length == 4){
				types.add(caseTypes[1]);
				types.add(caseTypes[2]);
			}else if(caseTypes.length == 3){
				types.add(caseTypes[1]);
			}
			Map<String, String> temp = null;
			if(activeList.keySet().contains(productDesc)){
				temp = activeList.get(productDesc);
				for(String eachType : types){
					if(temp.get(eachType) != null){
						if(!temp.get(eachType).contains(activeOrInactive)){
							temp.put(eachType, temp.get(eachType)+","+activeOrInactive);
							partialList.put(productDesc, temp);
							activeList.remove(productDesc);
						}
					}else{
						temp.put(eachType, activeOrInactive);
						activeList.put(productDesc, temp);
					}
				}
			}else if(inactiveList.keySet().contains(productDesc)){
				temp = inactiveList.get(productDesc);
				for(String eachType : types){
					if(temp.get(eachType) != null){
						if(!temp.get(eachType).contains(activeOrInactive)){
							temp.put(eachType, temp.get(eachType)+","+activeOrInactive);
							partialList.put(productDesc, temp);
							inactiveList.remove(productDesc);
						}
					}else{
						temp.put(eachType, activeOrInactive);
						inactiveList.put(productDesc, temp);
					}
				}
			}else if(partialList.keySet().contains(productDesc)){
				temp = partialList.get(productDesc);
				for(String eachType : types){
					if(temp.get(eachType) != null){
						if(!temp.get(eachType).contains(activeOrInactive)){
							temp.put(eachType, temp.get(eachType)+","+activeOrInactive);
							partialList.put(productDesc, temp);
						}
					}else{
						temp.put(eachType, activeOrInactive);
						partialList.put(productDesc, temp);
					}
				}
			}
			
			if(temp == null){
				temp = new HashMap<>();
				if(productArr[2].equals("ACTIVE")){
					//Add in active List
					for(String eachType : types){
						temp.put(eachType, "A");
					}
					activeList.put(productDesc, temp);
				}else if(productArr[2].equals("INACTIVE")){
					//Add in Inactive List
					for(String eachType : types){
						temp.put(eachType, "I");
					}
					inactiveList.put(productDesc, temp);
				}
			}
			
			
		}
		System.out.println("Product\ttp\tchr\tcrm\tdef\tfcg\tfcb");
		for(String key : activeList.keySet()){
			Map<String, String> temps = activeList.get(key);
			String print = key+"\t";
			
			print = temps.get("tp") != null ? print+""+temps.get("tp")+"\t" : print;
			print = temps.get("chr") != null ? print+""+temps.get("chr")+"\t" : print;
			print = temps.get("crm") != null ? print+""+temps.get("crm")+"\t" : print;
			print = temps.get("def") != null ? print+""+temps.get("def")+"\t" : print;
			print = temps.get("fcg") != null ? print+""+temps.get("fcg")+"\t" : print;
			print = temps.get("fcb") != null ? print+""+temps.get("fcb") : print;
			
			System.out.println(print);
		}
		
		for(String key : inactiveList.keySet()){
			Map<String, String> temps = inactiveList.get(key);
			String print = key+"\t";
			
			print = temps.get("tp") != null ? print+""+temps.get("tp")+"\t" : print;
			print = temps.get("chr") != null ? print+""+temps.get("chr")+"\t" : print;
			print = temps.get("crm") != null ? print+""+temps.get("crm")+"\t" : print;
			print = temps.get("def") != null ? print+""+temps.get("def")+"\t" : print;
			print = temps.get("fcg") != null ? print+""+temps.get("fcg")+"\t" : print;
			print = temps.get("fcb") != null ? print+""+temps.get("fcb") : print;
			
			System.out.println(print);
		}
		
		for(String key : partialList.keySet()){
			Map<String, String> temps = partialList.get(key);
			String print = key+"\t";
			
			print = temps.get("tp") != null ? print+""+temps.get("tp")+"\t" : print;
			print = temps.get("chr") != null ? print+""+temps.get("chr")+"\t" : print;
			print = temps.get("crm") != null ? print+""+temps.get("crm")+"\t" : print;
			print = temps.get("def") != null ? print+""+temps.get("def")+"\t" : print;
			print = temps.get("fcg") != null ? print+""+temps.get("fcg")+"\t" : print;
			print = temps.get("fcb") != null ? print+""+temps.get("fcb") : print;
			
			System.out.println(print);
		}
		
		for(String key : legacyList.keySet()){
			String temps = legacyList.get(key);
			String print = key+"\t"+temps;
			
			System.out.println(print);
		}
	}

	private void createMobileAccessoriesCombo(String sellerName, String filePath) throws IOException {
		// TODO Auto-generated method stub
		String[] paths = filePath.split(",");
		for(String mobileFolder : paths){
			File catalog = new File(mobileFolder+"/Combos/Catalog");
			if(catalog != null && catalog.exists() && catalog.isDirectory()){
				File[] files = catalog.listFiles();
				for(File file : files){
					if(file.getName().startsWith("Final")){
						List<String> lines = new ArrayList<>();
						File writeFile = new File(mobileFolder+"/###MobileAccessoriesCombo_"+sellerName+"/"+file.getName());
						writeFile.getParentFile().mkdirs();
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
									if(sellerName.equals("MAR")){
							    		line = line.replaceAll("mzt-", "mot-");
							    		line = line.replaceAll("Mozette\tM-", "Motaz\tMt-");
							    		line = line.replaceAll("mozette", "motaz");
							    		line = line.replaceAll("Mozette", "Motaz");
							    		line = line.replaceAll("MOZETTE", "MOTAZ");
							    	}else if(sellerName.equals("AMR")){
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
							    	}
									lines.add(line);
									System.out.println(line);
								}
							}
						}
						FileUtils.writeLines(writeFile, lines);
					}
				}
			}
		}
		//update combo count in comboCount.txt file
		updateComboSkuCount();
	}

	private void getDifferentColors(String colors) {
		// TODO Auto-generated method stub
		String[] adj = "ablaze,bleached,bold,brash,bright,chintzy,clean,cold,color-coded,cool,dappled,dark,darkly,deep,delicate,dusty,faded,fast,festive,fiery,flamboyant,flaming,fluorescent,fresh,gaily,glowing,harmonious,harsh,iridescent,jazzy,light,loud,matching,medium,mellow,monochrome,monotone,multicolored,muted,neutral,opalescent,pale,pastel,psychedelic,pure,restrained,rich,sepia,showy,sickly,sober,soft,somber,splashy,tinged,tinted,tonal,translucent,two-tone,vibrant,violent,vivid,warm,washed-out,watery,charcoal,amber,amethyst,avocado,azure,bistre,bright,brilliant,brindle,buff,canary,carmine,carnelian,cerise,charcoal,chartreuse,chestnut,chocolate,chrome,citrine,claret,clear,cobalt,copper,coral,cordovan,crimson,crystalline,dark,drab,dun,emerald,flesh,flushed,fuchsia,garnet,gay,grizzly,iridescent,jade,jet,lake,lavender,lilac,mahogany,maize,mauve,mint,navy,obsidian,ocher,onyx,opaque,orchid,pale,peach,pearl,plum,poppy,primrose,puce,ruby,ruddy,rust,sable,salmon,sapphire,scarlet,sepia,shimmering,sienna,silver,slate,smoky,snowy,sooty,spruce,tan,topaz,turquoise,twinkling,ultramarine,umber,vermilion,walnut,wine".split(",");
		int counter = 0;
		String[] color = colors.split(",");
		for(String c : color){
			if(counter == adj.length){
				counter = 0;
			}
			String output = adj[counter].substring(0, 1).toUpperCase() + adj[counter].substring(1);
			counter++;
			if(counter == adj.length){
				counter = 0;
			}
			
			if(c.contains("::")){
				String output1 = adj[counter].substring(0, 1).toUpperCase() + adj[counter].substring(1);
				System.out.println(output+" "+c.split("::")[0]+"::"+output1+" "+c.split("::")[1]);
			}else{
				System.out.println(output+" "+c);
			}
		}
	}

	private void updateFlipkartListings(String skus) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		if(!"".equals(skus)){
			for(String sku : skus.split(",")){
				FlipkartListing flipkartListing = getFlipkartListings(sku);
				Gson gson = new Gson();
				if(flipkartListing!= null){
//					System.out.println(flipkartListing.getListingId());
//					System.out.println(flipkartListing.getAttributeValues().get("selling_price"));
					Map<String, String> attributeMap = new HashMap<>();
//					attributeMap.put("zonal_shipping_charge", "40");
//					attributeMap.put("national_shipping_charge", "58");
//					attributeMap.put("local_shipping_charge", "30");
//					attributeMap.put("listing_status", "ACTIVE");
					attributeMap.put("procurement_type", "REGULAR");//REGULAR,DOMESTIC,EXPRESS
					attributeMap.put("procurement_sla", "1");
//					attributeMap.put("package_length", "25");
//					attributeMap.put("package_breadth", "14");
//					attributeMap.put("package_height", "3");
//					attributeMap.put("package_weight", "0.1");
//					attributeMap.put("selling_price", "449");
//					attributeMap.put("actual_stock_count", "250");
//					attributeMap.put("stock_count", "250");
					flipkartListing.setAttributeValues(attributeMap);
					String json = gson.toJson(flipkartListing);
					System.out.println(json);
//					UpdateFlipkartListingResponse uflrFlipkartListingResponse = updateFlipkartListings(json,flipkartListing.getSkuId());
//					if(uflrFlipkartListingResponse.getStatus() != null && uflrFlipkartListingResponse.getStatus().equals("success")){
//						System.out.println("Successfully Updated the Inventory");
//					}else{
//						System.out.println(sku);
//					}
				}
			}
		}
	}

	public List<String> readRandomFlipkartFile(File catFile){
		List<String> flipkartTemplateRows = new ArrayList<>();
		FileInputStream file = null;
		HSSFWorkbook workbook = null;
		try {
		    file = new FileInputStream(catFile);//"D:\\AAA_WORK\\JARS\\POI Testing\\Sunglasses Template Information.xlsx"
		    workbook = new HSSFWorkbook(file);
		    HSSFSheet sheet = workbook.getSheet("cases_covers");
		    
		    boolean flag = true;
		    int rowPOinter = 4;
		    while(flag){
		    	Row row = sheet.getRow(rowPOinter);
			    if(row != null){
			    	String rowMap = "";
				    for(int index = 1; index < 56;index++){
				    	Cell cell = row.getCell(index);
				    	if(index == 6 && cell == null){
				    		flag = false;
				    		break;
				    	}
				    	
				    	if(cell != null){
				    		cell.setCellType(Cell.CELL_TYPE_STRING);
					    	String cellValue = cell.getStringCellValue();
					    	if(index == 1 && "".equals(cellValue)){
					    		flag = false;
					    		break;
					    	}else{
					    		if(index == 2 && cellValue.contains("\n")){
					    			cellValue = cellValue.replaceAll("\n", "");
					    		}
					    		rowMap += cellValue+"\t";
					    	}
				    	}
				    }
				    if(rowMap.length() > 0){
				    	flipkartTemplateRows.add(rowMap);
				    }
			    }else{
			    	break;
			    }
			    rowPOinter++;
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
			if(file != null){
				try {
					file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(workbook != null){
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return (flipkartTemplateRows != null && flipkartTemplateRows.size() > 0) ? flipkartTemplateRows : null;
	}
	
	private void extractQCFailed(File file, String outputS) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		//Extract the data from catalog files and dump into qc.txt
		File output = new File(outputS+"/qc.txt");
		if(output.exists()){
			FileUtils.write(output, "");
		} else {
			output.getParentFile().mkdirs();
			output.createNewFile();
		}
		if(file != null && file.exists() && file.isDirectory()){
			for(File eachCat : file.listFiles()){
				if(eachCat.isDirectory()){
					continue;
				}
				List<String> lines = readRandomFlipkartFile(eachCat);
				FileUtils.writeLines(output, lines, true);
			}
		}
		
		//Filter catalog files
		List<String> skuList = new ArrayList<>();
		Map<String, List<String>> failedSkus = new TreeMap<>();
		boolean av = false;
		boolean avFailed = false;
		List<String> passesList = new ArrayList<>();
		List<String> temp = new ArrayList<>();
		if(file != null && file.exists()){
			try (BufferedReader br = new BufferedReader(new FileReader(output))) {
			    String line;
			    while ((line = br.readLine()) != null) {
			    	if(line != null && !line.isEmpty() && line.contains("\t")){
			    		String[] tokens = line.trim().split("\t\t\t\tmzt");
			    		if(tokens[0].trim().startsWith("Failed") && !tokens[0].contains("This SKU ID is already listed against another listing")){
			    			if(line.length() < 700){
				    			System.out.println("SKIPPED - "+line);
				    			continue;
				    		}
			    			if(tokens.length > 1 && !tokens[1].isEmpty()){
			    				tokens[0] = tokens[0].replaceAll("\t", "");
			    				String error = tokens[0].replaceFirst("Failed", "").trim();
			    				line = "zot"+tokens[1];
			    				line = line.replaceFirst("Mozette", error).trim();
			    				String sku = line.split("\t")[0];
			    				
			    				List<String> similarSkus = failedSkus.get(error);
			    				if(similarSkus != null && similarSkus.size() > 0){
			    					similarSkus.add(line);
			    					failedSkus.put(error, similarSkus);
			    				}else{
			    					similarSkus = new ArrayList<>();
			    					similarSkus.add(line);
			    					failedSkus.put(error, similarSkus);
			    				}
			    			}
			    		} else if(tokens[0].trim().startsWith("Passed")){
			    			line = line.trim().split("Approved\t\t")[1].trim().split("\t")[0];
			    			skuList.add(line);
			    		} else if(tokens[0].startsWith("AVFailed") && !tokens[0].contains("This SKU ID is already listed against another listing")){
			    			avFailed = true;
			    			if(line.length() < 700){
				    			System.out.println("SKIPPED - "+line);
				    			continue;
				    		}
			    			if(tokens.length > 1 && !tokens[1].isEmpty()){
			    				tokens[0] = tokens[0].replaceAll("\t", "");
			    				String error = tokens[0].replaceFirst("AVFailed", "").trim();
			    				line = "mzt"+tokens[1];
			    				line = line.replaceFirst("Mozette", error);
			    				
			    				List<String> similarSkus = failedSkus.get(error);
			    				if(similarSkus != null && similarSkus.size() > 0){
			    					similarSkus.add(line);
			    					failedSkus.put(error, similarSkus);
			    				}else{
			    					similarSkus = new ArrayList<>();
			    					similarSkus.add(line);
			    					failedSkus.put(error, similarSkus);
			    				}
			    			}
			    		} else if(tokens[0].startsWith("AVPassed")){
			    			av = true;
			    			String data = line.trim().split("\t\t\t\tmzt")[1];
			    			passesList.add("mzt"+data);
			    		}
			    	}
			    }
			}
		}
		if(failedSkus.size() > 0 || passesList.size() > 0){
			File qcFile = new File(outputS+"/"+"Filtered"+"/qc.txt");
			qcFile.getParentFile().mkdirs();
			qcFile.createNewFile();
			for(String key : failedSkus.keySet()){
				List<String> similarSkus = failedSkus.get(key);
				for(String each : similarSkus){
					String sku = each.split("\t")[0];
					if(!skuList.contains(sku)){// && !isExistingSku(sku)
						temp.add(each);
						System.out.println(each);
					}
				}
			}
			if(!avFailed){
				temp.addAll(passesList);
			}
			FileUtils.writeLines(qcFile, temp);
		}
	}

	private boolean isExistingSku(String sku) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		FlipkartListing flipkartListing = getFlipkartListings(sku);
		return flipkartListing != null;
	}

	private void extractSpecificCatalog(String mobiles, String types) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
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
										    	System.out.println(line);
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
	}

	private void extractSpecificCatalogForOtherSellers(String sellerName, String mobiles, String types) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
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
										    	System.out.println(line);
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
	}
	
	private void extractCombosCatalogForOtherSellers(String sellerName, String mobiles) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		String[] mobilesF = mobiles.split(",");
		for(String each : mobilesF){
			File eachMobile = new File(each);
			File catalog = new File(eachMobile.getAbsolutePath()+"/Combos/Catalog");
			if(catalog != null && catalog.exists() && catalog.isDirectory()){
				File[] finals = catalog.listFiles();
				for(File eachFinal : finals){
					if(!eachFinal.getName().contains("Final")){
						continue;
					}
					File newFinal = new File(eachMobile.getAbsolutePath()+"/###Combos_Completed_"+sellerName+"/"+eachFinal.getName());
					newFinal.getParentFile().mkdirs();
					PrintWriter writer = new PrintWriter(newFinal);
					try (BufferedReader br = new BufferedReader(new FileReader(eachFinal))) {
					    String line;
					    while ((line = br.readLine()) != null) {
					    	if(sellerName.equals("MAR")){
					    		line = line.replaceAll("mzt-", "mot-");
					    		line = line.replaceAll("Mozette\tM-", "Motaz\tMt-");
					    		line = line.replaceAll("mozette", "motaz");
					    		line = line.replaceAll("Mozette", "Motaz");
					    		line = line.replaceAll("MOZETTE", "MOTAZ");
					    	}else if(sellerName.equals("AMR")){
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
					    	}
					    	System.out.println(line);
					    	writer.print(line+"\n");
					    }
					}
					writer.close();
				}
			}else{
				System.out.println("\n\n\n\n\n\n###backup does not exists for : "+each);
			}
		}
	}
	
	private static void writeToAmazonFile(List<Map<Integer, String>> completeListings, String mobile,String extractType) throws IOException {
		
		File amazonCat = new File(mobile+"/###Amazon_completed/Catalog/"+extractType+".txt");
		
		if(!amazonCat.exists()){
			amazonCat.getParentFile().mkdirs();
			amazonCat.createNewFile();
		}
	    
		if(completeListings.size() > 0){
			List<String> lines = new ArrayList<>();
			for(Map<Integer, String> row : completeListings){
				String writer = "";
				for(int cellPointer=0; cellPointer < 68; cellPointer++) {
			    	String cell = row.get(cellPointer);
			    	if(cell != null && !cell.isEmpty()){
			    		writer += cell+"\t";
			    		System.out.print(cell+"\t");
			    	}else{
			    		writer += "\t";
			    		System.out.print("\t");
			    	}
			    }
				System.out.print("\n");
				lines.add(writer);
			}
			FileUtils.writeLines(amazonCat, lines, true);
		}
	}

	private void createAmazonListing(String types, String flipkartFolder) throws FileNotFoundException, IOException {
		String[] eachPhone = flipkartFolder.split(",");
		for(String mobile : eachPhone){
			File eachMobile = null;
			if(mobile.contains("Combos")){
				eachMobile = new File(mobile+"/Catalog");
				if(eachMobile.exists()){
					File[] typrList = eachMobile.listFiles();
					for(File cat : typrList){
						if(cat.getName().startsWith("Final")){
								List<Map<Integer, String>> listing = createEachAmazonComboListing(cat);
								writeToAmazonFile(listing,mobile,cat.getName().replace(".txt", ""));
						}
					}
				}else{
					System.out.println("\n\n\n\n\n\n");
					System.out.println("########################################################## Issue with : "+mobile);
					System.out.println("\n\n\n\n\n\n");
				}
			}else{
				eachMobile = new File(mobile+"/###backup");
				if(eachMobile.exists()){
					File[] typrList = eachMobile.listFiles();
					for(File type : typrList){
						for(String extractType : types.split(",")){
							File cat = new File(type.getAbsolutePath()+"/"+extractType+".txt");
							File copy = new File(mobile+"/###Amazon_completed/"+type.getName()+"/"+cat.getName());
							if(copy.exists()){
								continue;
							}
							copy.getParentFile().mkdirs();
							if(cat.exists()){
								List<Map<Integer, String>> listing = createEachAmazonListing(cat,type.getName().replace(".txt", ""));
								writeToAmazonFile(listing,mobile,extractType);
								FileUtils.copyFile(cat, copy);
							}
						}
					}
				}else{
					System.out.println("\n\n\n\n\n\n");
					System.out.println("########################################################## Issue with : "+mobile);
					System.out.println("\n\n\n\n\n\n");
				}
			}
		}
	}

	private List<Map<Integer, String>> createEachAmazonComboListing(File cat) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		List<Map<Integer, String>> listing = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(cat))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	if(StringUtils.isNotBlank(line)){
		    		
		    		String[] flipkartLine = line.split("\t");
		    		String[] tkns = flipkartLine[0].split("-");
		    		if(tkns.length < 4){
		    			continue;
		    		}
		    		Map<Integer, String> map = new HashMap<>();
		    		map.put(0, flipkartLine[0]);
		    		map.put(3, "Reflect-Ray "+flipkartLine[4].split("::")[0]+" "+flipkartLine[3]+" and "+flipkartLine[4].split("::")[1]+" "+flipkartLine[3]+" Combo for "+flipkartLine[5]);
		    		map.put(4, "Reflect-Ray");
		    		map.put(5, "RMA");
		    		map.put(6, "Reflect-Ray mobile Covers are designed to fully protect your mobile from getting damaged. They are impact resistant and highly durable. Moreover, its extreme slim profle and light weight adds no additional bulk to your phone. Gives complete access to all the buttons, ports and sensors. The crystal clear case covers 100% of the outer surface of the phone and precision molded with no seams or sharp edges.");
		    		map.put(7, "PhoneAccessory");
		    		map.put(8, flipkartLine[0].replace("mzt", "M"));
		    		map.put(9, flipkartLine[5]);
		    		map.put(11, "999");
		    		map.put(12, "250");
		    		map.put(13, "New");
		    		map.put(16, getAmazonPriceList(flipkartLine[0]));
		    		
		    		map.put(19, "2");
		    		map.put(20, "1");
		    		
		    		map.put(28, "A_MOBILE_GEN");
		    		map.put(36, "50");
		    		map.put(37, "GR");
		    		
		    		
		    		map.put(38, "QUALITY COMBO SET OF 1 "+flipkartLine[4].split("::")[0]+" "+flipkartLine[3]+" AND 1 "+flipkartLine[4].split("::")[1]+" "+flipkartLine[3]);
		    		
		    		String type1 = tkns[1].equals("tp") ? "Transparent" : tkns[1].equals("chr") ? "Cherry" : tkns[1].equals("crm") ? "Chrome" : tkns[1].equals("def") ? "Defender" : tkns[1].equals("fcb") ? "Flip Cover Black" : tkns[1].equals("fcg") ? "Flip Cover Gold" : "";
		    		String type2 = tkns[2].equals("tp") ? "Transparent" : tkns[2].equals("chr") ? "Cherry" : tkns[2].equals("crm") ? "Chrome" : tkns[2].equals("def") ? "Defender" : tkns[2].equals("fcb") ? "Flip Cover Black" : tkns[2].equals("fcg") ? "Flip Cover Gold" : "";
		    		
		    		if(!type1.isEmpty()){
		    			String[] bulletPoints1 = amazonBuletPOintsMap.get(type1).split("::");
		    			map.put(39, bulletPoints1[0].replaceAll("<mobilename>", flipkartLine[5]));
		    			map.put(40, bulletPoints1[1].replaceAll("<mobilename>", flipkartLine[5]));
		    		}
		    		
		    		if(!type2.isEmpty()){
		    			String[] bulletPoints2 = amazonBuletPOintsMap.get(type1).split("::");
		    			map.put(41, bulletPoints2[0].replaceAll("<mobilename>", flipkartLine[5]));
		    			map.put(42, bulletPoints2[1].replaceAll("<mobilename>", flipkartLine[5]));
		    		}
		    		
		    		map.put(43, "1389409031");
		    		
		    		map.put(45, flipkartLine[11]);
		    		map.put(46, flipkartLine[12]);
		    		map.put(47, flipkartLine[13]);
		    		map.put(48, flipkartLine[14]);
		    		
		    		map.put(50, "5");
		    		map.put(51, "17");
		    		map.put(52, "25");
		    		map.put(53, "CM");
		    		map.put(54, "0.1");
		    		map.put(55, "KG");
		    		map.put(56, "India");
		    		map.put(65, flipkartLine[4].replaceAll("::", " and "));
		    		map.put(66, flipkartLine[4].split("::")[0]);
		    		map.put(67, flipkartLine[6].replaceAll("::", " and "));
		    		listing.add(map);
		    	}
		    }
		}
		return listing;
	}

	private List<Map<Integer, String>> createEachAmazonListing(File cat, String type) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		List<Map<Integer, String>> listing = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(cat))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	if(StringUtils.isNotBlank(line)){
		    		String[] flipkartLine = line.split("\t");
		    		Map<Integer, String> map = new HashMap<>();
		    		map.put(0, flipkartLine[0]);
		    		map.put(3, "Reflect-Ray "+flipkartLine[4]+" "+flipkartLine[3]+" for "+flipkartLine[5]);
		    		map.put(4, "Reflect-Ray");
		    		map.put(5, "RMA");
		    		map.put(6, "Reflect-Ray mobile Covers are designed to fully protect your mobile from getting damaged. They are impact resistant and highly durable. Moreover, its extreme slim profle and light weight adds no additional bulk to your phone. Gives complete access to all the buttons, ports and sensors. The crystal clear case covers 100% of the outer surface of the phone and precision molded with no seams or sharp edges.");
		    		map.put(7, "PhoneAccessory");
		    		map.put(8, flipkartLine[0].replace("mzt", "M"));
		    		map.put(9, flipkartLine[5]);
		    		map.put(11, "999");
		    		map.put(12, "250");
		    		map.put(13, "New");
		    		map.put(16, getAmazonPriceList(flipkartLine[0]));
		    		
		    		map.put(19, "1");
		    		map.put(20, "1");
		    		
		    		map.put(28, "A_MOBILE_GEN");
		    		map.put(36, "50");
		    		map.put(37, "GR");
		    		
		    		String[] bulletPoints = amazonBuletPOintsMap.get(type).split("::");
		    		
		    		map.put(38, bulletPoints[0].replaceAll("<mobilename>", flipkartLine[5]));
		    		map.put(39, bulletPoints[1].replaceAll("<mobilename>", flipkartLine[5]));
		    		map.put(40, bulletPoints[2].replaceAll("<mobilename>", flipkartLine[5]));
		    		map.put(41, bulletPoints[3].replaceAll("<mobilename>", flipkartLine[5]));
		    		map.put(42, bulletPoints[4].replaceAll("<mobilename>", flipkartLine[5]));
		    		
		    		map.put(43, "1389409031");
		    		
		    		map.put(45, flipkartLine[11]);
		    		map.put(46, flipkartLine[12]);
		    		map.put(47, flipkartLine[13]);
		    		map.put(48, flipkartLine[14]);
		    		
		    		map.put(50, "5");
		    		map.put(51, "17");
		    		map.put(52, "25");
		    		map.put(53, "CM");
		    		map.put(54, "0.1");
		    		map.put(55, "KG");
		    		map.put(56, "India");
		    		map.put(65, flipkartLine[4]);
		    		map.put(66, flipkartLine[4]);
		    		map.put(67, flipkartLine[6].split("::")[0]);
		    		listing.add(map);
		    	}
		    }
		}
		return listing;
	}

	private void createShopCluesListing(String brand, String types, String flipkartFolder) throws IOException {
		String[] eachPhone = flipkartFolder.split(",");
		for(String mobile : eachPhone){
			File eachMobile = null;
			if(mobile.contains("Combos")){
			}else{
				eachMobile = new File(mobile+"/###backup");
				if(eachMobile.exists()){
					File[] typrList = eachMobile.listFiles();
					for(File type : typrList){
						for(String extractType : types.split(",")){
							File cat = new File(type.getAbsolutePath()+"/"+extractType+".txt");
							File copy = new File(mobile+"/###ShopClues_completed/"+type.getName()+"/"+cat.getName());
							if(copy.exists()){
								continue;
							}
							copy.getParentFile().mkdirs();
							if(cat.exists()){
								List<Map<Integer, String>> listing = createEachShopCluesListing(brand,cat,type.getName().replace(".txt", ""));
								writeToShopCluesFile(listing,mobile,extractType);
								FileUtils.copyFile(cat, copy);
							}
						}
					}
				}else{
					System.out.println("\n\n\n\n\n\n");
					System.out.println("########################################################## Issue with : "+mobile);
					System.out.println("\n\n\n\n\n\n");
				}
			}
		}
	}

	private void writeToShopCluesFile(List<Map<Integer, String>> completeListings, String mobile, String extractType) throws IOException {
		
		File shopCluesCat = new File(mobile+"/###ShopClues_completed/Catalog/"+extractType+".txt");
		
		if(!shopCluesCat.exists()){
			shopCluesCat.getParentFile().mkdirs();
			shopCluesCat.createNewFile();
		}
	    
		if(completeListings.size() > 0){
			List<String> lines = new ArrayList<>();
			for(Map<Integer, String> row : completeListings){
				String writer = "";
				for(int cellPointer=0; cellPointer < 68; cellPointer++) {
			    	String cell = row.get(cellPointer);
			    	if(cell != null && !cell.isEmpty()){
			    		writer += cell+"\t";
			    		System.out.print(cell+"\t");
			    	}else{
			    		writer += "\t";
			    		System.out.print("\t");
			    	}
			    }
				System.out.print("\n");
				lines.add(writer);
			}
			FileUtils.writeLines(shopCluesCat, lines, true);
		}
	}

	private List<Map<Integer, String>> createEachShopCluesListing(String brand,File cat, String type) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		List<Map<Integer, String>> listing = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(cat))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	if(StringUtils.isNotBlank(line)){
		    		String[] flipkartLine = line.split("\t");
		    		Map<Integer, String> map = new HashMap<>();
		    		map.put(1, flipkartLine[0]);
		    		map.put(2, flipkartLine[0]);
		    		map.put(3, "Mozette "+flipkartLine[4]+" "+flipkartLine[3]+" for "+flipkartLine[5]);
		    		
		    		map.put(4, "Selling Price");//?
		    		
		    		map.put(5, "Mozette mobile Covers are designed to fully protect your mobile from getting damaged. They are impact resistant and highly durable. Moreover, its extreme slim profle and light weight adds no additional bulk to your phone. Gives complete access to all the buttons, ports and sensors. The crystal clear case covers 100% of the outer surface of the phone and precision molded with no seams or sharp edges.");
		    		map.put(6, "Active");
		    		map.put(7, flipkartLine[11]);
		    		map.put(8, "999");
		    		map.put(11, "250");
		    		
		    		map.put(12, flipkartLine[12]);
		    		map.put(13, flipkartLine[13]);
		    		map.put(14, flipkartLine[14]);
		    		map.put(15, "Y");
		    		map.put(16, "65");
		    		map.put(17, "Others");
		    		
		    		String temp = flipkartLine[0].split("-")[1];
		    		String materialType = temp.equals("crm") || temp.equals("tp") ? "Silicon" : temp.equals("fcg") || temp.equals("fcb") ? "Artificial Leather" : temp.equals("def") || temp.equals("chr") ? "Rubber" : "";
		    		
		    		if(!materialType.isEmpty()){
		    			map.put(18, materialType);
		    		}
		    		map.put(19, brand);
		    		
		    		String caseType = temp.equals("def") || temp.equals("crm") || temp.equals("chr") || temp.equals("tp") ? "Back Cover" : temp.equals("fcg") || temp.equals("fcb") ? "Flip Cover" : "";
		    		
		    		if(!caseType.isEmpty()){
		    			map.put(20, caseType);
		    		}
		    		if(temp.equals("def")){
		    			map.put(21, "Yes");
		    		}else if(temp.equals("fcb") || temp.equals("fcg")){
		    			map.put(21, "Fold stand");
		    		}
		    		
		    		map.put(22, "HIGH QUALITY, RELIABLE and FULLY PROTECTED");
		    		map.put(25, flipkartLine[5]);
		    		map.put(27, "25*17*5");
		    		map.put(28, flipkartLine[0].replace("mzt", "M"));
		    		map.put(29, flipkartLine[0].replace("mzt", "M"));
		    		map.put(32, "Mozette");
		    		map.put(34, flipkartLine[4]);
		    		listing.add(map);
		    	}
		    }
		}
		return listing;
	}

	private void createFinalComboListing(String mobileFolders) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		String[] mF = mobileFolders.split(",");
		for(String comboCatalogFolder : mF){
			comboCatalogFolder += "/Combos/Catalog";
			File mainFolder = new File(comboCatalogFolder);
			int lineCount = 0;
			int fileCount = 1;
			if(mainFolder != null && mainFolder.exists()){
				File[] fileList = mainFolder.listFiles();
				FileWriter fw = new FileWriter(comboCatalogFolder+"/Final"+fileCount+".txt");
				for(File eachFile : fileList){
					try (BufferedReader br = new BufferedReader(new FileReader(eachFile))) {
						String line;
						while ((line = br.readLine()) != null) {
							if(lineCount == 300){
								fw.close();
								lineCount = 0;
								fileCount += 1;
								fw = new FileWriter(comboCatalogFolder+"/Final"+fileCount+".txt");
							}
							System.out.println(line);
							fw.write(line);
							fw.write("\n");
							lineCount++;
						}
					}
				}
				fw.close();
			}
		}
	}

	public String getPriceList(String nameVsSkus) {
		
		String priceList = "";
		for(String sku : nameVsSkus.split(";")){
			if(sku.indexOf(":") == -1){
				System.out.println("ERROR IN INPUT MISSING :");
				return null;
			}
			
			String name = sku.split(":")[0];
			if(name.indexOf("Mozette") != -1){
				name = name.substring(name.indexOf("Mozette ")+8, name.indexOf(" for"));
			}else if(name.indexOf("XOLDA") != -1){
				name = name.substring(name.indexOf("XOLDA ")+6, name.indexOf(" for"));
			}else if(name.indexOf("Motaz") != -1){
				name = name.substring(name.indexOf("Motaz ")+6, name.indexOf(" for"));
			}else if(name.indexOf("ZOTIKOS") != -1){
				name = name.substring(name.indexOf("ZOTIKOS ")+8, name.indexOf(" for"));
			}
			String skus = sku.split(":")[1];
			
			if(StringUtils.isBlank(name)){
				System.out.println("ERROR IN INPUT MISSING CASE TYPE");
				return null;
			}
			
//			String[] rates = getPricingMap().get(getPricingLevelMap().get(name)).split(",");
			String[] rates = getPricingMap().get("compete").split(",");
			
			Map<String, String> rateMap  = new HashMap<>();
			
			for(String rate : rates){
				rateMap.put(rate.split(":")[0], rate.split(":")[1]);
			}
			for(String sku1 :skus.split(",")){
				String[] tokens = sku1.split("-");
				
				if(tokens.length > 3){
					if(rateMap.containsKey(tokens[1]+"+"+tokens[2])){
						System.out.println(rateMap.get(tokens[1]+"+"+tokens[2]));
						priceList += rateMap.get(tokens[1]+"+"+tokens[2])+",";
					}else if(rateMap.containsKey(tokens[2]+"+"+tokens[1])){
						System.out.println(rateMap.get(tokens[2]+"+"+tokens[1]));
						priceList += rateMap.get(tokens[2]+"+"+tokens[1])+",";
					}else{
						System.out.println("missed");
					}
				}else{
					if(rateMap.containsKey(tokens[1])){
						System.out.println(rateMap.get(tokens[1]));
						priceList += rateMap.get(tokens[1])+",";
					}else{
						System.out.println("missed");
						return null;
					}
				}
			}
		}
			
		return priceList.length() > 0 ? priceList.substring(0, priceList.length()-1): "";
	}
	
	private void getShippingList(String nameVsSkus) {
		
		for(String sku : nameVsSkus.split(";")){
			if(sku.indexOf(":") == -1){
				System.out.println("ERROR MISSING INPUT");
				return;
			}
			
			String name = sku.split(":")[0];
			name = name.substring(name.indexOf("Mozette ")+8, name.indexOf(" for"));
			String skus = sku.split(":")[1];
			
			if(StringUtils.isBlank(name)){
				System.out.println("ERROR MISSING NAME");
				continue;
			}
			
			String level = getPricingLevelMap().get(name);
			if(level.equalsIgnoreCase("level-2") || level.equalsIgnoreCase("level-4")){
				System.out.println("0\t0\t0");
			}else{
				System.out.println("35\t45\t65");
			}
		}
	}
	
	private String getAmazonPriceList(String skus) {
		// TODO Auto-generated method stub
		Map<String, String> rateMap = new HashMap<>();
		String[] rates = "tp+chr:210,tp+def:275,tp+fcb:245,tp+fcg:245,chr+def:295,chr+fcb:255,chr+fcg:255,def+fcb:325,def+fcg:325,fcb+fcg:299,tp+tp:195,chr+chr:235,def+def:355,fcb+fcb:299,fcg+fcg:299,tp+crm:210,crm+crm:235,chr+crm:235,def+crm:295,fcb+crm:255,fcg+crm:255,tp:155,chr:175,crm:165,def:245,fcg:204,fcb:204".split(",");
		for(String rate : rates){
			rateMap.put(rate.split(":")[0], rate.split(":")[1]);
		}
		
		for(String sku :skus.split(",")){
			String[] tokens = sku.split("-");
			
			if(tokens.length > 3){
				if(rateMap.containsKey(tokens[1]+"+"+tokens[2])){
					return rateMap.get(tokens[1]+"+"+tokens[2]);
				}else if(rateMap.containsKey(tokens[2]+"+"+tokens[1])){
					return rateMap.get(tokens[2]+"+"+tokens[1]);
				}else{
					System.out.println("missed");
				}
			}else{
				if(rateMap.containsKey(tokens[1])){
					return rateMap.get(tokens[1]);
				}else{
					System.out.println("missed");
				}
			}
		}
		return "";
	}

	private void createCombosCatalog(String combosFolders) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		String[] cF = combosFolders.split(",");
		
		for(String combosFoldr : cF){
			
			File test = new File(combosFoldr+"/Combos/Catalog");
			if(test!=null && test.isDirectory() && test.exists() && test.listFiles().length > 0){//Combos already created
				continue;
			}
			
			String mobileName = combosFoldr.substring(combosFoldr.lastIndexOf("/")+1);
			File combosFolder = new File(combosFoldr+"/Combos");
			ImageShack iS = new ImageShack("");
			if(combosFolder != null && combosFolder.exists() && combosFolder.isDirectory()){
				File[] caseTypes = combosFolder.listFiles();
				if(caseTypes != null && caseTypes.length > 0){
					for(File caseType : caseTypes){
						if(caseType.isDirectory() && !caseType.getName().contains("#") && !caseType.getName().contains("Catalog")){
//							String caseName = caseType.getName();
							File[] imageList = caseType.listFiles();
							List<File> fileList = new ArrayList<>();
							if(imageList != null && imageList.length > 0){
								for(int i = 0; i < imageList.length; i++){
									if(fileList.size() <= 9){
										System.out.println(imageList[i].getName());
										fileList.add(imageList[i]);
									}else{
										List<String> imageUrls = iS.processImageUploadForCombo(fileList, "combos",combosFoldr+"/Combos");
										if(imageUrls.size() == fileList.size()){
											List<Map<Integer, String>> completedListings = popuateEachRowForCombo(fileList,imageUrls,mobileName);
											writeCombosToFile(completedListings,combosFoldr+"/Combos");
										}
										fileList.removeAll(fileList);
										fileList = new ArrayList<>();
									}
								}
							}
						}
					}
				}
			}
			
			//update combo count in comboCount.txt file
			updateComboSkuCount();
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

	private void createComboForOnePhoneNew(String mF) throws IOException {
		// TODO Auto-generated method stub
		
		String[] mobileFolders =mF.split(",");
		for(String mobileFolder: mobileFolders){
			
			resizeImagesForOnePhone(mobileFolder);
			
//			String caseTypes[] = "Anti-radiation Case,Back Cover,Back Replacement Cover,Book Cover,Bumper Case,Cases with Holder,Dot View Case,Dual Protection Case,Flip Cover,Front & Back Case,Front Cover,Grip Back Cover,Shock Proof Case".split(",");
//			String caseTypes[] = "Back Cover".split(",");
//			for(String eachCase1 : caseTypes){
				File test = new File(mobileFolder+"/Combos/Covers");
				if(test!=null && test.exists() && test.isDirectory() && test.listFiles().length > 0){//Combos already created
					return;
				}
				String[] caseFolers = getComboCaseTypes().get("All").split(",");
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
					createCombo(image1.getAbsolutePath(), image2.getAbsolutePath(), mobileFolder+"/Combos/Covers/"+caseFolder1.getName()+"_"+caseFolder2.getName()+"_"+this.comboCount+".jpeg",false);
					this.comboCount++;
				}
			}
		}
	}

	private void createCombinationsOfOneType(String mobileFolder, File caseType, String eachCase) throws IOException {
		// TODO Auto-generated method stub
		String caseName = caseType.getName();
		new File(mobileFolder+"/Combos/"+eachCase).mkdirs();
		File[] images = caseType.listFiles();
		if(images != null && images.length > 0){
			for(int i = 0; i < images.length; i++){
				String image1 = images[i].getAbsolutePath();
				for(int j = i+1; j < images.length; j++){
					String image2 = images[j].getAbsolutePath();
					createCombo(image1, image2, mobileFolder+"/Combos/"+eachCase+"/"+caseName+"_"+caseName+"_"+this.comboCount+".jpeg",true);
					this.comboCount++;
				}
			}
		}
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
			System.out.println("First Image Height : "+firstImageHeight);
			
			bufferredImage2 = ImageIO.read(new File(image2));
			int secondImageWidth = bufferredImage2.getWidth();
			int[] minMaxY2 = this.getMinMaxY(bufferredImage2);
			int[] minMaxX2 = this.getMinMaxX(bufferredImage2);
			bufferredImage2 = bufferredImage2.getSubimage(minMaxX2[0],minMaxY2[0],minMaxX2[1]-minMaxX2[0],minMaxY2[1]-minMaxY2[0]);
			int secondImageHeight = minMaxY2[1]-minMaxY2[0];
			System.out.println("Second Image Height : "+secondImageHeight);
			
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
	
	private void createFlipkartListingsForManyPhone(String[] multiplePhones) throws ClientProtocolException, IOException {
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
		
		File parentFolder = new File(mobileFolder);
		String mobileName = parentFolder.getName();
		System.out.println("Mobile Name : "+mobileName);
		File[] varieties = parentFolder.listFiles();
		if(varieties != null && varieties.length > 0){
			for(File eachType : varieties){
				System.out.println("Starting to create listings of type : "+eachType.getName());
				if(eachType.getName().contains("Combo") || eachType.getName().contains("Glass") 
						|| eachType.getName().contains("Tempered") || eachType.getName().contains("Catalog") 
						|| eachType.getName().contains("#")){
					continue;
				}
				createListngs(mobileName,eachType,mobileFolder);
			}
		}
		
		FileUtils.copyDirectory(new File(parentFolder.getAbsolutePath()+"/Catalog"), new File(parentFolder.getAbsolutePath()+"/###backup"));
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
	
	private List<String> pyTMUpload(File file) {
		List<String> imageLinks = new ArrayList<>();
		try {
			
			
			
			String url = "https://catalogadmin.paytm.com/v1/merchant/482781/resource/put";
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost httppost = new HttpPost(url);
			httppost.addHeader("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryPUy2mxZbyEfrBORg");
			httppost.addHeader("Host", "catalogadmin.paytm.com");
			httppost.addHeader("Origin", "https://seller.paytm.com");
			httppost.addHeader("Referer", "https://seller.paytm.com/new/catalog_new");
			FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
			
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

			builder.addBinaryBody("upfile", file, ContentType.DEFAULT_BINARY, "1.jpeg");
//			for(File file : files){
//				builder.addPart("upFile", fileBody);
//			}
			HttpEntity entity = builder.build();
		    httppost.setEntity(entity);
			HttpResponse response = client.execute(httppost);
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer resultString = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				resultString.append(line);
				System.out.println(line);
			}
//			Gson gson = new Gson();
//			ImageShackUploadResponse iSUResponse = gson.fromJson(resultString.toString(), ImageShackUploadResponse.class);
//			Result result = iSUResponse.getResult();
//			if(result != null && result.getImages() != null && result.getImages().size() > 0){
//				List<Map<String, Object>> images = result.getImages();
//				for(Map<String, Object> image : images){
//					String directLink = (String) image.get("direct_link");
//					imageLinks.add(convertImage(directLink));
//				}
//			}
//			saveDirectImageLinksInExcel();
		} catch (Exception e) {
//			System.out.println("Exception in imageShackUpload method in folder :: "+folderPath);
			e.printStackTrace();
			System.out.println("\n\n Trying to upload again");
//			imageLinks = imageShackUpload(authToken,files,albumName,folderPath);
		}
		return imageLinks;
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
			completeListings.add(eachRow);
		}
		return completeListings;
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
	
	private static void writeToFile(List<Map<Integer, String>> completeListings, String caseType,String mobileFolder, String folderName, String mobileName, boolean diffBrandColors) {
		
		String filePath = mobileFolder+"/Catalog/"+folderName;
		String fileName = "";
		if(diffBrandColors){
			fileName = filePath+"/"+caseType+"_"+"Differnet"+" Brand Color.txt";
		}else{
			fileName = filePath+"/"+caseType+".txt";
		}
		
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		try{
		    PrintWriter writer = new PrintWriter(file);
		    
			if(completeListings.size() > 0){
				for(Map<Integer, String> row : completeListings){
					for(int cellPointer=6; cellPointer < 31; cellPointer++) {
				    	String cell = row.get(cellPointer);
				    	if(cell != null && !cell.isEmpty()){
				    		writer.print(cell+"\t");
				    		System.out.print(cell+"\t");
				    	}else{
				    		writer.print("\t");
				    		System.out.print("\t");
				    	}
				    }
					writer.print("\n");
					System.out.print("\n");
				}
				
			}
		    writer.close();
		} catch (IOException e) {
		   // do something
			System.out.println(e);
		}
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
				    		System.out.print(cell+"\t");
				    	}else{
				    		writer.print("\t");
				    		System.out.print("\t");
				    	}
				    }
					writer.print("\n");
					System.out.print("\n");
				}
				
			}
		    writer.close();
		} catch (IOException e) {
		   // do something
			System.out.println(e);
		}
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
		caseTypes.put("Transparent", "Back Cover:Y,Back Replacement Cover:Y");
		caseTypes.put("Flip Cover Gold", "Flip Cover:Y,Front & Back Case:Y,Book Cover:Y");
		caseTypes.put("Flip Cover Black", "Flip Cover:Y,Front & Back Case:Y,Book Cover:Y");
		caseTypes.put("Defender", "Back Cover:Y,Back Replacement Cover:Y,Bumper Case:Y");
		caseTypes.put("Cherry", "Back Cover:Y,Back Replacement Cover:Y");
		caseTypes.put("Chrome", "Back Cover:Y,Back Replacement Cover:Y");
		caseTypes.put("Tempered Glass", "Tempered Glass:Y,Screen Guard:Y,Privacy Screen Guard:Y");
		caseTypes.put("Screen Guard", "Tempered Glass:Y,Screen Guard:Y");
		return caseTypes;
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
		return caseTypes;
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
	
	private Map<String, String> loadModelMap() {
		Map<String, String> caseTypes = new HashMap<>();
		caseTypes.put("Transparent", "M-tp-a");
		caseTypes.put("Flip Cover Gold", "M-fcg-a");
		caseTypes.put("Flip Cover Black", "M-fcb-a");
		caseTypes.put("Defender", "M-def-a");
		caseTypes.put("Cherry", "M-chr-a");
		caseTypes.put("Chrome", "M-crm-a");
		caseTypes.put("Tempered Glass", "M-sg-a");
		caseTypes.put("Screen Guard", "M-sg-a");
		return caseTypes;
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

	private Map<String, String> loadAmazonBulletPoints() {
		Map<String, String> amazonBulletPOints = new HashMap<>();
		amazonBulletPOints.put("Transparent", "REFLECT-RAY Transparent Case best fits and compatible with <mobilename>. Its sleek and ultra thin design is flexible and reliable. Gives comfortable access to ports, buttons, camera, sensors and all other features.::CLEAR,SLIM AND TRANSPARENT - Its crystal clear and transparent sleek body minimizes the bulk and gives the original feel of the phone.::FULL FLEDGE PROTECTION - Its rounded corners and raised edges protects the phone completely from damaging the back and front part of your phone respectively.::BEST QUALITY - Polished and laser cut texture makes it scratch proof. Designed to fit <mobilename> perfectly. Overall it is best in material and durability.::All buttons and jacks are accessible through the cutouts, making it comfortable.");
		amazonBulletPOints.put("Flip Cover Gold", "REFLECT-RAY Scratch Proof: Save your mobile from scratches even if scratched with keys in your pocket, magnetic clasp not available.::REFLECT-RAY Shatter Proof: Advance shock and shatter absorption from drops and bumps.::Ultra-thin and lightweight: Protection against scratches on backside of mobile and very comfortable installation.::All buttons and jacks are accessible through the cutouts with a precise fit seamless compatibility for perfect fitting. Easy installation and removal.::Hard but flexible material makes fitting and removing the case much easier.");
		amazonBulletPOints.put("Flip Cover Black", "REFLECT-RAY Scratch Proof: Save your mobile from scratches even if scratched with keys in your pocket, magnetic clasp not available.::REFLECT-RAY Shatter Proof: Advance shock and shatter absorption from drops and bumps.::Ultra-thin and lightweight: Protection against scratches on backside of mobile and very comfortable installation.::All buttons and jacks are accessible through the cutouts with a precise fit seamless compatibility for perfect fitting. Easy installation and removal.::Hard but flexible material makes fitting and removing the case much easier.");
		amazonBulletPOints.put("Defender", "REFLECT-RAY AUTHENTIC HYBRID CASE - Military Grade Royal Protective Case with Back Fitted Media Stand, Heavy Duty Design. All buttons and jacks are accessible through the cutouts with a precise fit seamless compatibility for perfect fitting. Easy installation and removal.::REFLECT-RAY Shatter Proof: - Outer layer fix perfectly around the inner shell to absorb impact form drops bumps and shocks. Hard but flexible material makes fitting and removing the case much easier.::SIGNAL PROTECION - Armor cover case. Detachable dual layer. Beautiful and Tough.::BUILT-IN MEDIA STAND - Comes with the built-in stand, offering comfortable angle for watching videos, Video chat and web-surfing on any surface - study table, office desk, dining table and anywhere else.::DUAL LAYER COVER - Complete access to all features of the device. Tough impact resistant shell. A clear protector is built in to the inner shell to prevent scratches and smudges including speaker, camera, microphone and all buttons. Enhances the appearance of phone.");
		amazonBulletPOints.put("Cherry", "REFLECT-RAY Rugged Armor Series with Anti Shock Corners is thin as well as impact and shock resistant. Made up of high quality eco friendly materials, inside web pattern, proper holes and cut-outs for sensors. Raised lips protect the screen and camera bump.::Unique Look featuring Smooth Brushed texture, glossy accents and carbon fibre texture. Easy to remove & install, anti scratch, washable case. Heat Dissipation Design, flexible and tear resistant.::Tactile Buttons for natural feedback and easy press. Trusted Quality from REFLECT-RAY, Quality difference is felt with the use of product::Package Content: 1 REFLECT-RAY Case in Retail Packing. Note - Images are only for illustrative purposes, actual product and colour may slightly differ.::All buttons and jacks are accessible through the cutouts with a precise fit seamless compatibility for perfect fitting. Easy installation and removal. Hard but flexible material makes fitting and removing the case much easier.");
		amazonBulletPOints.put("Chrome", "REFLECT-RAY Transparent and Gold edge case best fits and compatible with <mobilename>. Its sleek and ultra thin design is flexible and reliable. Gives comfortable access to ports, buttons, camera, sensors and all other features.::CLEAR,GOLD AND TRANSPARENT - Its crystal clear and transparent sleek body minimizes the bulk and gives the original feel of the phone.::FULL FLEDGE PROTECTION - Its rounded corners and raised edges protects the phone completely from damaging the back and front part of your phone respectively.::BEST QUALITY - Polished and laser cut texture makes it scratch proof. Designed to fit <mobilename> perfectly. Overall it is best in material and durability.::All buttons and jacks are accessible through the cutouts, making it comfortable.");
		return amazonBulletPOints;
	}

	private Map<String, String> loadPricingLevelMap() {
		Map<String, String> comboCaseTypes = new HashMap<>();
//		comboCaseTypes.put("Anti-radiation Case","level-4");
//		comboCaseTypes.put("Back Cover","level-1");
//		comboCaseTypes.put("Back Replacement Cover","level-2");
//		comboCaseTypes.put("Book Cover","level-3");
//		comboCaseTypes.put("Bumper Case","level-2");
//		comboCaseTypes.put("Cases with Holder","level-3");
//		comboCaseTypes.put("Dot View Case","level-2");
//		comboCaseTypes.put("Dual Protection Case","level-4");
//		comboCaseTypes.put("Flip Cover","level-1");
//		comboCaseTypes.put("Front & Back Case","level-1");
//		comboCaseTypes.put("Front Cover","level-3");
//		comboCaseTypes.put("Grip Back Cover","level-1");
//		comboCaseTypes.put("Shock Proof Case","level-4");
		comboCaseTypes.put("Anti-radiation Case","level-1");
		comboCaseTypes.put("Back Cover","level-1");
		comboCaseTypes.put("Back Replacement Cover","level-1");
		comboCaseTypes.put("Book Cover","level-1");
		comboCaseTypes.put("Bumper Case","level-1");
		comboCaseTypes.put("Cases with Holder","level-1");
		comboCaseTypes.put("Dot View Case","level-1");
		comboCaseTypes.put("Dual Protection Case","level-1");
		comboCaseTypes.put("Flip Cover","level-1");
		comboCaseTypes.put("Front & Back Case","level-1");
		comboCaseTypes.put("Front Cover","level-1");
		comboCaseTypes.put("Grip Back Cover","level-1");
		comboCaseTypes.put("Shock Proof Case","level-1");
		comboCaseTypes.put("Wallet Case Cover","level-1");
		comboCaseTypes.put("Tempered Glass Guard","level-1");
		comboCaseTypes.put("Screen Guard","level-1");
		comboCaseTypes.put("Privacy Screen Guard","level-1");
		return comboCaseTypes;
	}

	private Map<String, String> loadPricingMap() {
		Map<String, String> comboCaseTypes = new HashMap<>();
//		comboCaseTypes.put("compete","tp+chr:319,tp+def:379,tp+fcb:359,tp+fcg:359,chr+def:389,chr+fcb:369,chr+fcg:369,def+fcb:429,def+fcg:429,fcb+fcg:399,tp+tp:289,chr+chr:339,def+def:459,fcb+fcb:399,fcg+fcg:399,tp+crm:319,crm+crm:339,chr+crm:339,def+crm:389,fcb+crm:369,fcg+crm:369,tp+sg:289,chr+sg:319,def+sg:379,crm+sg:319,fcb+sg:359,fcg+sg:359,tp:189,crm:199,chr:209,def:269,fcb:239,fcg:239,sg:189");
		comboCaseTypes.put("compete","tp+chr:340,tp+def:399,tp+fcb:359,tp+fcg:379,chr+def:410,chr+fcb:389,chr+fcg:389,def+fcb:449,def+fcg:449,fcb+fcg:419,tp+tp:310,chr+chr:359,def+def:479,fcb+fcb:419,fcg+fcg:419,tp+crm:339,crm+crm:359,chr+crm:359,def+crm:410,fcb+crm:389,fcg+crm:389,tp+sg:310,chr+sg:339,def+sg:399,crm+sg:339,fcb+sg:379,fcg+sg:379,tp:210,crm:210,chr:239,def:289,fcb:259,fcg:259,sg:210");
		comboCaseTypes.put("level-1","tp+chr:319,tp+def:379,tp+fcb:359,tp+fcg:359,chr+def:389,chr+fcb:369,chr+fcg:369,def+fcb:429,def+fcg:429,fcb+fcg:399,tp+tp:289,chr+chr:339,def+def:459,fcb+fcb:399,fcg+fcg:399,tp+crm:319,crm+crm:339,chr+crm:339,def+crm:389,fcb+crm:369,fcg+crm:369,tp:199,crm:229,chr:219,def:299,fcb:259,fcg:259,sg:199");
		comboCaseTypes.put("level-2","tp+chr:389,tp+def:449,tp+fcb:429,tp+fcg:429,chr+def:459,chr+fcb:439,chr+fcg:439,def+fcb:499,def+fcg:499,fcb+fcg:469,tp+tp:359,chr+chr:409,def+def:529,fcb+fcb:469,fcg+fcg:469,tp+crm:389,crm+crm:409,chr+crm:409,def+crm:459,fcb+crm:439,fcg+crm:439,tp:289,crm:329,chr:319,def:389,fcb:349,fcg:349");
		comboCaseTypes.put("level-3","tp+chr:319,tp+def:379,tp+fcb:359,tp+fcg:359,chr+def:389,chr+fcb:369,chr+fcg:369,def+fcb:429,def+fcg:429,fcb+fcg:399,tp+tp:289,chr+chr:339,def+def:459,fcb+fcb:399,fcg+fcg:399,tp+crm:319,crm+crm:339,chr+crm:339,def+crm:389,fcb+crm:369,fcg+crm:369,tp:219,crm:259,chr:249,def:319,fcb:279,fcg:279");
		comboCaseTypes.put("level-4","tp+chr:499,tp+def:599,tp+fcb:499,tp+fcg:499,chr+def:599,chr+fcb:599,chr+fcg:599,def+fcb:699,def+fcg:699,fcb+fcg:599,tp+tp:450,chr+chr:599,def+def:459,fcb+fcb:599,fcg+fcg:599,tp+crm:499,crm+crm:599,chr+crm:599,def+crm:599,fcb+crm:599,fcg+crm:599,tp:399,crm:399,chr:399,def:499,fcb:499,fcg:499");
		return comboCaseTypes;
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

	private Map<String, String> loadSkus() {
		// TODO Auto-generated method stub
		Map<String, String> comboCaseTypes = new HashMap<>();
		comboCaseTypes.put("RMA","rR");
		comboCaseTypes.put("AMR","mzt");
		return comboCaseTypes;
	}

	private Map<String, String> loadBrandNames() {
		// TODO Auto-generated method stub
		Map<String, String> comboCaseTypes = new HashMap<>();
		comboCaseTypes.put("RMA","Reflect-Ray");
		comboCaseTypes.put("AMR","Mozette");
		return comboCaseTypes;
	}
	
	private void resizeImagesForMultiplePhones(String[] folderNames) throws IOException{
		
		if(folderNames != null && folderNames.length > 0){
			for(String folderName : folderNames){
				resizeImagesForOnePhone(folderName);
			}
		}
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
			System.out.println("skipping : "+image1);
			return;
		}
		
		System.out.println("Resizing : "+image1);
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
		
		System.out.println(bufferredImage1.getWidth()+"-"+bufferredImage1.getHeight()+"-"+x1+"-"+y1+"-"+width+"-"+height);
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

	public Map<String, String> getCaseTypesMap() {
		return caseTypesMap;
	}

	public void setCaseTypesMap(Map<String, String> caseTypesMap) {
		this.caseTypesMap = caseTypesMap;
	}

	public Map<String, String> getComboSKUMap() {
		return comboSKUMap;
	}

	public void setComboSKUMap(Map<String, String> comboSKUMap) {
		this.comboSKUMap = comboSKUMap;
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
	
	public Map<String, String> getSkuStringMap() {
		return skuStringMap;
	}

	public Map<String, String> getComboCaseTypes() {
		return comboCaseTypes;
	}

	public void setComboCaseTypes(Map<String, String> comboCaseTypes) {
		this.comboCaseTypes = comboCaseTypes;
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

	public Map<String, String> getPricingMap() {
		return pricingMap;
	}

	public void setPricingMap(Map<String, String> pricingMap) {
		this.pricingMap = pricingMap;
	}

	public Map<String, String> getPricingLevelMap() {
		return pricingLevelMap;
	}

	public void setPricingLevelMap(Map<String, String> pricingLevelMap) {
		this.pricingLevelMap = pricingLevelMap;
	}

	public Map<String, String> getDescriptionMap() {
		return descriptionMap;
	}

	public void setDescriptionMap(Map<String, String> descriptionMap) {
		this.descriptionMap = descriptionMap;
	}

	public void setColorMap(Map<String, String> colorMap) {
		this.colorMap = colorMap;
	}

	public Map<String, String> getColorMap() {
		return colorMap;
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
	
	private List<String> getPayTMValidMobiles() throws FileNotFoundException, IOException {
		List<String> mobiles = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new FileReader(PAYTM_VALID_MOBILES))) {
		    String line = br.readLine();
		    while ((line = br.readLine()) != null) {
				mobiles.add(line);
			}
		}
		return mobiles;
	}

	private void updateSkuCount() throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Updating the SKU count"+skuStart);
		FileUtils.writeStringToFile(new File(SKU_COUNT_PROPERTIES), "skuCount="+this.skuStart);
	}
	private void updateTemperedSkuCount() throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Updating the Tempered SKU count"+temperedSkuStart);
		FileUtils.writeStringToFile(new File(TEMPERED_COUNT_PROPERTIES), "temperedSkuCount="+this.temperedSkuStart);
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

	private void updateComboSkuCount() throws IOException {
		// TODO Auto-generated method stub
//		System.out.println("Updating the Combo SKU count"+comboSkuStart);
		FileUtils.writeStringToFile(new File(COMBO_COUNT_PROPERTIES), "comboSkuCount="+this.comboSkuStart);
	}
	
	private FlipkartListing getFlipkartListings(String sku) throws ClientProtocolException, IOException {
		FlipkartListing flipkartListing = null;
		String url = "https://api.flipkart.net/sellers/skus/"+sku+"/listings";
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		request.addHeader("Authorization", "Bearer "+token);
		request.addHeader("Content-Type", "application/json");
		HttpResponse response = client.execute(request);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
			if(line.contains("Content not found")){
				System.out.println(sku);
			}
			System.out.println(line);
		}
		if(result.indexOf("listingId") != -1){
			Gson gson = new Gson();
			flipkartListing = gson.fromJson(result.toString(), FlipkartListing.class);
//			System.out.println(flipkartListing.getSkuId());
		}else{
			return null;
		}
		return flipkartListing;
	}
	
	//curl -u 13518b3a60b44522b2546a72b4a71b68218bb:33a224e762e3d8c6a63c3d433d963cb48  https://api.flipkart.net/oauth-service/oauth/token?grant_type=client_credentials&scope=Seller_Api
	private void getFlipkartToken() throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		//curl -u 13518b3a60b44522b2546a72b4a71b68218bb:33a224e762e3d8c6a63c3d433d963cb48  https://api.flipkart.net/oauth-service/oauth/token?grant_type=client_credentials&scope=Seller_Api
		String url = "https://api.flipkart.net/oauth-service/oauth/token?grant_type=client_credentials&scope=Seller_Api";

		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, 
		    new UsernamePasswordCredentials("<13518b3a60b44522b2546a72b4a71b68218bb>", "<33a224e762e3d8c6a63c3d433d963cb48>"));
		HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);

		System.out.println("Response Code : " 
	                + response.toString());
	}
	
	public UpdateFlipkartListingResponse updateFlipkartListings(String flipkartListingJson, String sku) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		String url = "https://api.flipkart.net/sellers/skus/"+sku+"/listings";
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(url);
		request.addHeader("Authorization", "Bearer "+token);
		request.addHeader("Content-Type", "application/json");
		StringEntity params =new StringEntity(flipkartListingJson);
		request.setEntity(params);
		HttpResponse response = client.execute(request);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
			System.out.println(line);
		}
		Gson gson = new Gson();
		return gson.fromJson(result.toString(), UpdateFlipkartListingResponse.class);
	}

	public Map<String, String> getAccountVsBrandNames() {
		return accountVsBrandNames;
	}

	public void setAccountVsBrandNames(Map<String, String> accountVsBrandNames) {
		this.accountVsBrandNames = accountVsBrandNames;
	}

	public Map<String, String> getAccountVsSkus() {
		return accountVsSkus;
	}

	public void setAccountVsSkus(Map<String, String> accountVsSkus) {
		this.accountVsSkus = accountVsSkus;
	}
}