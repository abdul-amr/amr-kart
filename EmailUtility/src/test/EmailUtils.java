package test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailUtils {
	
	private Session session;
	
	public EmailUtils(){
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");

		this.session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("abdul.mudassir5086@gmail.com","Mudassir88");
				}
			});
		
	}
	
	public static void main(String[] args) {
		EmailUtils e = new EmailUtils();
		List<String> filePaths = new ArrayList<>();
		filePaths.add("D:/AAA_WORK/Important Batch Files/Flipkart/ExtractAndUpdateSKUs/skuList.txt");
		filePaths.add("D:/AAA_WORK/Important Batch Files/Flipkart/ExtractAndUpdateSKUs/mobileSearchResult.txt");
		filePaths.add("D:/AAA_WORK/Important Batch Files/Flipkart/ExtractAndUpdateSKUs/code/config.properties");
		System.out.println(filePaths.toString());
//		e.sendEmail("No results", "No Results Found", "ExtractAndUpdateSKUs", filePaths);
	}
	
	public boolean sendEmail(String sellerName, String batchName, String subBatchName, String body, List<String> filePaths, String to){
			if(to == null || "".equals(to)){
				to = "abdul.mudassir5086@gmail.com";
			}
		    try {
		        Message message = new MimeMessage(session);
		        message.setFrom(new InternetAddress("abdul.mudassir5086@gmail.com"));
		        message.setRecipients(Message.RecipientType.TO,
		                InternetAddress.parse(to));
		        Date date = new Date();  
			    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");  
			    String strDate = formatter.format(date);  
		        message.setSubject(sellerName.trim().toUpperCase()+" Batch : "+batchName+" - "+subBatchName+" - "+strDate);
		        
		        if(filePaths != null && filePaths.size() > 0){
		        	Multipart multipart = new MimeMultipart();
		        	MimeBodyPart textBodyPart = new MimeBodyPart();
		        	textBodyPart.setText(body);
		        	multipart.addBodyPart(textBodyPart);
		        	
		        	for(String filePath : filePaths){
		        		MimeBodyPart messageBodyPart = new MimeBodyPart();
		        		messageBodyPart = new MimeBodyPart();
		        		String file = filePath;
		        		DataSource source = new FileDataSource(file);
		        		messageBodyPart.setDataHandler(new DataHandler(source));
		        		if(filePath.indexOf("/") != -1){
		        			filePath = filePath.substring(filePath.lastIndexOf("/")+1, filePath.length());
		        		}else if(filePath.indexOf("\\") != -1){
		        			filePath = filePath.substring(filePath.lastIndexOf("\\")+1, filePath.length());
		        		}
		        		messageBodyPart.setFileName(filePath.replace(".properties", ".txt"));
		        		multipart.addBodyPart(messageBodyPart);
		        		message.setContent(multipart);
		        	}
		        }else{
		        	message.setText(body);
		        }


//		        System.out.println("Sending");
		        Transport.send(message);
//		        System.out.println("Done");
		        return true;

		    } catch (MessagingException e) {
		        e.printStackTrace();
		    }
		    return false;
		
	}
}
