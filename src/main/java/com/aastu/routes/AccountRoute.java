package com.aastu.routes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.sql.SQLException;

import javax.mail.MessagingException;

import com.aastu.database.Database;
import com.aastu.model.IdNumberPayload;
import com.aastu.model.Message;
import com.aastu.model.SecurePassword;
import com.aastu.model.UpdatePassword;
import com.aastu.utils.ReqRes;
import com.aastu.utils.Util;
import com.aastu.utils.Validate;
import com.aastu.utils.gmailapi.SendEmail;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AccountRoute implements HttpHandler {

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    switch (exchange.getRequestMethod()) {
      case "PUT":
        String url = exchange.getRequestURI().getPath();
        switch (url) {
          case "/api/account/updatepw":
            handleChangePwd(exchange);
            break;
          case "/api/account/resetpw":
            handleResetPwd(exchange);
            break;
          default:
            break;
        }
        break;

      default:
        break;
    }
  }

  private static void handleResetPwd(HttpExchange exchange) throws IOException {
    Message message = new Message();

    String emailjson = ReqRes.readRequestBody(exchange.getRequestBody());
    message = (Message) ReqRes.makeModelFromJson(emailjson, Message.class);
    
    String emailId = message.getMessage();
    String idNumber = exchange.getRequestHeaders().getFirst("Authorization");

    if (idNumber == null || emailId == null) {
      message.setMessage("Not valid idnumber/email");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, ReqRes.makeJsonString(message));
      return;
    }

    // Check if the email and id provided matches in the database
    try {
      String storedEmail = Database.getUserEmail(idNumber);
      if (storedEmail == null || !storedEmail.equals(emailId)) {
        message.setMessage("Email and id number doesn't match");
        ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, ReqRes.makeJsonString(message));
        return;
      }
    } catch (SQLException e) {
      message.setMessage("Internal/Server error! Retry later.");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, ReqRes.makeJsonString(message));
      return;
    }

    // Send Otp to the user's email
    String OTP = Util.generateOTP();
    try {
      var emailMessage = SendEmail.createEmailAsMessage(emailId, Util.getEnv().getProperty("email.id"),
          "One Time Password", "Your one time password " + OTP);
      SendEmail.sendEmail(emailMessage);

      message.setMessage("Success");

    } catch (MessagingException | IOException | GeneralSecurityException e) {
      System.out.println(e.getMessage());
    }

    ReqRes.sendResponse(exchange, 202, ReqRes.makeJsonString(message));
  }

  private static void handleChangePwd(HttpExchange exchange) throws IOException {
    // Authenticate the request
    String authToken = exchange.getRequestHeaders().getFirst("Authorization");
    String token = (authToken == null || authToken.split(" ").length <= 1) ? null : authToken.split(" ")[1];

    Message message = new Message();
    if (token == null || !Util.verifyUser(token)) {
      message.setMessage("Unauthorized user request!");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, ReqRes.makeJsonString(message));
      return;
    }

    // Validate the update request data
    String jsonBody = ReqRes.readRequestBody(exchange.getRequestBody());
    UpdatePassword update = (UpdatePassword) ReqRes.makeModelFromJson(jsonBody, UpdatePassword.class);

    try {
      Validate.password(update.getOldPassword());
      Validate.password(update.getNewPassword());
      Validate.password(update.getConfirm());
    } catch (Error e) {
      message.setMessage(e.getMessage());
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, ReqRes.makeJsonString(message));
      return;
    }

    // Check the old password against the stored password

    String loginJson = Util.getDecodedPayload(token);
    IdNumberPayload idNumber = (IdNumberPayload) ReqRes.makeModelFromJson(loginJson, IdNumberPayload.class);

    try {
      if (!SecurePassword.authenticatePassword(update.getOldPassword(), Database.getUserPassword(idNumber.getIdNumber()))) {
        message.setMessage("Wrong old password");
        ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, ReqRes.makeJsonString(message));
        return;
      }
    } catch (SQLException e) {
      message.setMessage("Internal error!");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, ReqRes.makeJsonString(message));
      return;
    }

    // Update the stored password
    // First we need to hash the new password
    SecurePassword passwd = SecurePassword.saltAndHashPassword(update.getNewPassword());
    try {
      int r = Database.updatePassword(idNumber.getIdNumber(), passwd.getHashedPassword());
      if (r > 0) {
        message.setMessage("Operaton Sccessfull!");
        ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_ACCEPTED, ReqRes.makeJsonString(message));
        return;
      } else {
        message.setMessage("Something is wrong with the request! Try again later");
        ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, ReqRes.makeJsonString(message));
      }
    } catch (Exception e) {
      message.setMessage("Internal Error");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, ReqRes.makeJsonString(message));
    }
  }
}
