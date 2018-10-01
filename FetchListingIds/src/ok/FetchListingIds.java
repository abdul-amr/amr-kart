package ok;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class FetchListingIds {
	public static void main(String[] args) {
		
//		FetchListingIds fetchListingIds = new FetchListingIds();
//
//		fetchListingIds.skuVsPhoneMap = fetchListingIds.loadSkuVsPhoneMapFromCentralLocation();
//		String inventoryFileName = fetchListingIds.getConfigProp().getProperty("inventoryfile");
//		String mobileNamesCommaSeparated = fetchListingIds.getConfigProp().getProperty("mobiles");
		
//		Mailer.send("abdul.mudassir5086@gmail.com","Mudassir88","amr.retailers@gmail.com","hello javatpoint","How r u?"); 
		Date date = new Date();  
	    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");  
	    String strDate = formatter.format(date);  
	    System.out.println("Date Format with dd-M-yyyy hh:mm:ss : "+strDate); 
	}
}