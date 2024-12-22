package com.jodo.portal.security.encryption;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodo.portal.model.Email;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	public HashMap<String, Object> pushEmail(Email mailData, MultipartFile[] files) {

		HashMap<String, Object> response = new HashMap<>();

		if (mailData == null) {
			log.warn("Email data is null");
			response.put("error", "Email data cannot be null");
			return response;
		}

		List<MultipartFile> attachments = new ArrayList<>();
		if (files != null && files.length > 0) {
			attachments.addAll(Arrays.asList(files));
			log.info("Number of attachments: {}", attachments.size());
		}

		if (mailData.getSubject() == null || mailData.getSubject().isEmpty()) {
			log.warn("Subject is blank");
			response.put("error", "Subject should not be blank");
			return response;
		}

		if (mailData.getToAddresses() == null || mailData.getToAddresses().isEmpty()) {
			log.warn("TO address is blank");
			response.put("error", "TO should not be blank");
			return response;
		}

		if (!validateEmails(mailData.getToAddresses(), "TO", response)) {
			return response;
		}

		if (mailData.getBccAddresses() != null) {
			if (!mailData.getBccAddresses().isBlank() && !validateEmails(mailData.getBccAddresses(), "BCC", response)) {
				return response;
			}
		}

		if (mailData.getCcAddresses() != null) {
			if (!mailData.getCcAddresses().isBlank() && !validateEmails(mailData.getCcAddresses(), "CC", response)) {
				return response;
			}
		}

		boolean isEmailSent = sendMail(mailData, attachments);
		if (isEmailSent) {
//			log.info("Email sent successfully ::  "+ mailData);
			response.put("success", true);
		} else {
			response.put("error", false);
		}
		return response;
	}

	private boolean validateEmails(String addresses, String type, HashMap<String, Object> response) {
		String[] recipientList = addresses.split(";");
		for (String recipient : recipientList) {
			if (!emailValidate(recipient.trim())) {
				log.warn("Invalid {} recipient email: {}", type, recipient);
				response.put("error", "Invalid " + type + " recipient email: " + recipient);
				return false;
			}
		}
		return true;
	}

	public boolean sendMail(Email mailData, List<MultipartFile> attachments) {
		String host = "smtp.gmail.com";
		String port = "587";
		String username = "no.replay.bosa@gmail.com";
		String password = "nvermdlskpeuyfkv";
		String displayEmail = "no.replay.bosa@gmail.com";

		try {
			Properties properties = new Properties();
			properties.put("mail.smtp.host", host);
			properties.put("mail.smtp.port", port);
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.starttls.enable", "true");

			Session session = Session.getInstance(properties, new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});

			if (mailData.getOrganisationName() == null || mailData.getOrganisationName().isBlank()) {
				mailData.setOrganisationName("no-reply");
			}

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(displayEmail, mailData.getOrganisationName()));

			if (mailData.getFromAddress() == null) {
				mailData.setFromAddress(username);
			}
			message.setReplyTo(InternetAddress.parse(mailData.getFromAddress()));

			addRecipients(message, Message.RecipientType.TO, mailData.getToAddresses());
			addRecipients(message, Message.RecipientType.CC, mailData.getCcAddresses());
			addRecipients(message, Message.RecipientType.BCC, mailData.getBccAddresses());

			message.setSubject(mailData.getSubject());

			Multipart multipart = new MimeMultipart();

			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(mailData.getBody(), "text/html");
			multipart.addBodyPart(messageBodyPart);

			if (attachments != null && !attachments.isEmpty()) {
				mailData.setFileAttached(true);
				log.info("Attachments present. Number of attachments: {}", attachments.size());

				for (MultipartFile multipartFile : attachments) {
					MimeBodyPart attachPart = new MimeBodyPart();
					try {
						File file = convertToFile(multipartFile);
						attachPart.attachFile(file);
						log.info("Attached file: {}", file.getName());
					} catch (Exception ex) {
						log.error("Error attaching file: {}", multipartFile.getOriginalFilename(), ex);
					}
					multipart.addBodyPart(attachPart);
				}
			}

			message.setContent(multipart);
			Transport.send(message);
			log.info("Email sent to SMTP server");

			return true;
		} catch (Exception e) {
			log.error("Error sending email", e);
			return false;
		}
	}

	private void addRecipients(Message message, Message.RecipientType type, String recipients)
			throws MessagingException {
		if (recipients != null && !recipients.trim().isEmpty()) {
			String[] recipientList = recipients.split(";");
			for (String recipient : recipientList) {
				message.addRecipient(type, new InternetAddress(recipient.trim()));
			}
			log.info("Added {} recipients to {}", recipientList.length, type);
		}
	}

	private File convertToFile(MultipartFile multipartFile) throws IOException {
		File file = new File(multipartFile.getOriginalFilename());
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(multipartFile.getBytes());
			log.info("Converted MultipartFile to File: {}", file.getName());
		}
		return file;
	}

	public static boolean emailValidate(String emailId) {
		try {
			String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(emailId);
			return matcher.matches();
		} catch (Exception e) {
			log.error("Exception in emailValidate", e);
			return false;
		}
	}
	
//	public static void main(String[] args) {
//	    EmailService emailService = new EmailService();
//
//	    // Create a mock Email object
//	    Email mailData = new Email();
//	    mailData.setSubject("Test Email");
//	    mailData.setToAddresses("abdubey42@gmail.com");
//	    mailData.setCcAddresses("abdubey42@gmail.com");
//	    mailData.setBccAddresses("abdubey42@gmail.com");
//	    mailData.setFromAddress("no.replay.bosa@gmail.com");
//	    mailData.setBody("<h1>This is a test email</h1>");
//	    mailData.setOrganisationName("SBIL Health Insurance");
//
//	    // Send email without attachments
//	    HashMap<String, Object> response = emailService.pushEmail(mailData, null);
//
//	    // Log the response
//	    ObjectMapper objectMapper = new ObjectMapper();
//	    try {
//	        String jsonResponse = objectMapper.writeValueAsString(response);
//	        System.out.println("Response: " + jsonResponse);
//	    } catch (JsonProcessingException e) {
//	        e.printStackTrace();
//	    }
//	}
}
