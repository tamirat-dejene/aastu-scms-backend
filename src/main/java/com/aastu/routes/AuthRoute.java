package com.aastu.routes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.sql.SQLException;

import javax.mail.MessagingException;

import com.aastu.database.Database;
import com.aastu.model.IdNumberPayload;
import com.aastu.model.Login;
import com.aastu.model.Message;
import com.aastu.model.SecurePassword;
import com.aastu.model.Student;
import com.aastu.utils.ReqRes;
import com.aastu.utils.Util;
import com.aastu.utils.Validate;
import com.aastu.utils.gmailapi.SendEmail;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AuthRoute implements HttpHandler {
  private static String OTP = null;

  @Override
  public void handle(HttpExchange exchange) {
    String url = exchange.getRequestURI().getPath();
    if (url.endsWith("/"))
      url = url.substring(0, url.length() - 1);
    switch (url) {
      case "/api/auth/login":
      case "/api/auth/login/admin":
        handleLogin(exchange);
        break;
      case "/api/auth/signup":
        switch (exchange.getRequestMethod()) {
          case "POST":
            handleSignUp(exchange);
            break;
          case "GET":
            if (exchange.getRequestURI().getQuery().contains("otp="))
              handleOTP(exchange);
            else if (exchange.getRequestURI().getQuery().contains("emailid="))
              handleEmail(exchange);
            break;
          default:
            break;
        }
        break;
      case "/api/auth/logout":
        handLogout(exchange);
        break;
      default:
        ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "{\"message\": \"Bad Request\"}");
        System.out.println("Bad request.............");
        break;
    }
  }


  private static void handleEmail(HttpExchange exchange) {
    // Try to send otp
    OTP = Util.generateOTP();
    Message message = new Message();
    try {
      String emailId = exchange.getRequestURI().getQuery().split("=")[1];
      System.out.println(emailId);
      var emailMessage = SendEmail.createEmailAsMessage(emailId, Util.getEnv().getProperty("email.id"),
          "One Time Password", "Your one time password: " + OTP);
      SendEmail.sendEmail(emailMessage);

      message.setMessage("OTP Sent successfully");
      ReqRes.sendResponse(exchange, 200, ReqRes.makeJsonString(message));
    } catch (MessagingException | IOException | GeneralSecurityException e) {
      message.setMessage("Error sending OTP. Try again later");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, ReqRes.makeJsonString(message));
    }
  }

  private static void handleOTP(HttpExchange exchange) {
    String otp = exchange.getRequestURI().getQuery().split("=")[1];
    Message message = new Message();
    if (otp.equals(OTP)) {
      message.setMessage("Request Accepted");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_ACCEPTED, ReqRes.makeJsonString(message));
    } else {
      message.setMessage("Incorrect otp. Try again later.");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_CONFLICT, ReqRes.makeJsonString(message));
    }
  }

  private static void handleLogin(HttpExchange exchange) {
    String method = exchange.getRequestMethod();
    switch (method) {
      case "GET":
        handleLoginGetRequest(exchange);
        break;
      case "POST":
        handleLoginPostRequest(exchange);
    }
  }

  private static void handleLoginGetRequest(HttpExchange exchange) {
    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
    if (authHeader == null) {
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "{ \"Authenticated\": \"false\"}");
      return;
    }
    // Separate the Bearer from the token i.e. Authorization = Bearer token
    String[] token = authHeader.split(" ");

    // We will validate using the token sent inside the header
    if (token.length <= 1 || !Util.verifyUser(token[1]))
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, "{ \"Authenticated\": \"false\"}");
    else
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_OK, "{\"Authenticated\": \"true\"}");
  }

  private static void handleLoginPostRequest(HttpExchange exchange) {
    // Reading Json data from the request body
    String requestBody = ReqRes.readRequestBody(exchange.getRequestBody());

    // Convert JSON data to Login object
    Login login = (Login) ReqRes.makeModelFromJson(requestBody, Login.class);
    // Rechecking validity of the login info
    try {
      if (!exchange.getRequestURI().getPath().contains("admin")) { 
        Validate.idNumber(login.getIdNumber());
        Validate.password(login.getPassword());
      } else {
        Validate.departmentId(login.getIdNumber());
        Validate.departmentPwd(login.getPassword());
      }
    } catch (Error e) {
      String errorJson = null;
      var error = new Message();
      error.setMessage(e.getMessage());
      errorJson = ReqRes.makeJsonString(error);
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, errorJson);
      return;
    }

    // Database query
    try {
      String storedPassword = exchange.getRequestURI().getPath().contains("admin")
          ? Database.getAdminPassword(login.getIdNumber())
          : Database.getUserPassword(login.getIdNumber());
          
      var inputPassword = login.getPassword();

      if (!SecurePassword.authenticatePassword(inputPassword, storedPassword)) {
        var error = new Message();
        error.setMessage("Incorrect username/password");
        ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, ReqRes.makeJsonString(error));
        return;
      }
    } catch (SQLException e) {
      var error = new Message();
      error.setMessage("Server Error! We appriciate your patience!");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, ReqRes.makeJsonString(error));
      return;
    }
    // The user is authenticated: Sign the user with jwt
    IdNumberPayload idNumber = new IdNumberPayload();
    idNumber.setIdNumber(login.getIdNumber());
    String payLoad = ReqRes.makeJsonString(idNumber);
    String jwt = Util.signUser(payLoad);

    exchange.getResponseHeaders().set("Authorization", "Bearer " + jwt);
    ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_OK, "{\"Authenticated\": \"true\"}");
  }

  private static void handleSignUp(HttpExchange exchange) {
    // Request body
    String requestBody = ReqRes.readRequestBody(exchange.getRequestBody());
    Student student = (Student) ReqRes.makeModelFromJson(requestBody, Student.class);

    // Read hashed password from the request header
    String passwordJson = exchange.getRequestHeaders().getFirst("Password");
    SecurePassword password = (SecurePassword) ReqRes.makeModelFromJson(passwordJson, SecurePassword.class);
    String hashedPassword = password.getHashedPassword();

    // Validating the user request inputs.
    try {
      Validate.section(student.getSection());
      Validate.email(student.getEmailAddress());
      Validate.idNumber(student.getIdNumber());
      Validate.name(student.getFirstName());
      Validate.name(student.getMiddleName());
      Validate.name(student.getLastName());
      Validate.classYear(Integer.toString(student.getClassYear()));
    } catch (Error e) {
      var error = new Message();
      error.setMessage(e.getMessage());
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, ReqRes.makeJsonString(error));
      return;
    }

    // It has passed the validation: now save to database
    try {
      Database.saveStudent(student, hashedPassword);
    } catch (SQLException e) {
      var error = new Message();
      error.setMessage("Account with this email is already in the db");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, ReqRes.makeJsonString(error));
      return;
    }

    // Send success response
    var message = new Message();
    message.setMessage("Registeration successful");
    ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_CREATED, ReqRes.makeJsonString(message));
  }

  private static void handLogout(HttpExchange exchange) {
  }

  public static void main(String[] args) {
    System.out.println("abcd".substring(0, 4));
  }
}
