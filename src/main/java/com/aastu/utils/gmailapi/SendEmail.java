package com.aastu.utils.gmailapi;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

@SuppressWarnings("deprecation")
public class SendEmail {
  private static final String APPLICATION_NAME = "SCMS Mailer";
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final String TOKENS_DIRECTORY_PATH = "src/main/resources/storedcredentials";
  private static final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_SEND, GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_METADATA);
  private static final String CREDENTIALS_FILE_PATH = "/credentials/credentials.json";

  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
    InputStream in = SendEmail.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")
        .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    return credential;
  }

  public static MimeMessage createEmail(String toEmailAddress, String fromEmailAddress, String subject, String bodyText)
      throws MessagingException {

    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);
    MimeMessage email = new MimeMessage(session);

    email.setFrom(new InternetAddress(fromEmailAddress));
    email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(toEmailAddress));
    email.setSubject(subject);
    email.setText(bodyText);

    return email;
  }

  public static Message createMessageWithEmail(MimeMessage emailContent)
      throws MessagingException, IOException {
    /*
     * Encode the MimeMessage, instantiate a Message object, and set the base64url
     * encoded message string as the value of the raw property
     */
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    emailContent.writeTo(buffer);
    byte[] bytes = buffer.toByteArray();
    String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
    Message message = new Message();
    message.setRaw(encodedEmail);
    return message;
  }

  public static Message createEmailAsMessage(String toEmailAddress, String fromEmailAddress, String subject,
      String bodyText) throws MessagingException, IOException {
    var mime = SendEmail.createEmail(toEmailAddress, fromEmailAddress, subject, bodyText);
    return SendEmail.createMessageWithEmail(mime);
  }

  public static Message sendEmail(Message message)
      throws MessagingException, IOException, GeneralSecurityException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

    Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
        .setApplicationName(APPLICATION_NAME)
        .build();

    
    try {
      // Create send message
      message = service.users().messages().send("me", message).execute();
      var sentm = service.users().messages().list("me").setLabelIds(List.of("SENT")).execute().getMessages();

      String message_id = message.getId();
      var k = sentm.stream().filter(sent -> sent.getId().equals(message_id)).count();
      System.out.println(k);
      
      System.out.println("Message id: " + message.getId());
      System.out.println(message.toPrettyString());
      return message;
    } catch (GoogleJsonResponseException e) {
      GoogleJsonError error = e.getDetails();
      System.out.println("Error code: " + error.getCode());
      System.err.println(error.toPrettyString());
      throw e;
    }
  }

  static void test1() {
    try {
      MimeMessage mimeMessage;
      mimeMessage = SendEmail.createEmail("tamirat.dejene@aastustudent.edu.et", "me",
          "Hello buddy!",
          "How are you doing buddy?");
      Message message;
      message = SendEmail.createMessageWithEmail(mimeMessage);
      SendEmail.sendEmail(message);
    } catch (MessagingException | IOException | GeneralSecurityException e) {
      System.out.println(e.getMessage() + " kmnnnnnnnnnnn");
    }
  }

  public static void main(String... args) throws GeneralSecurityException, IOException {
    test1();
  }
}
