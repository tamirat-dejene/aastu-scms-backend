package com.aastu.routes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;

import com.aastu.database.Database;
import com.aastu.model.Login;
import com.aastu.model.Message;
import com.aastu.model.SecurePassword;
import com.aastu.model.Student;
import com.aastu.utils.ReqRes;
import com.aastu.utils.Util;
import com.aastu.utils.Validate;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class AuthRoute implements HttpHandler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String url = exchange.getRequestURI().getPath();
    System.out.println(url);
    switch (url) {
      case "/api/auth/login":
        handleLogin(exchange);
        break;
      case "/api/auth/signup":
        handleSignUp(exchange);
        break;
      case "/api/auth/logout":
        handLogout(exchange);
        break;
      default:
        ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "{\"message\": \"Bad Request\"}");
        break;
    }
  }

  private static void handleLogin(HttpExchange exchange) throws IOException {
    String method = exchange.getRequestMethod();
    switch (method) {
      case "GET":
        handleLoginGetRequest(exchange);
        break;
      case "POST":
        handleLoginPostRequest(exchange);
    }
  }

  private static void handleLoginGetRequest(HttpExchange exchange) throws IOException {
    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
    if (authHeader == null) {
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "{ \"Authenticated\": \"false\"}");
      return;
    }
    // Separate the Bearer from the token i.e. Authorization = Bearer token
    String token = authHeader.split(" ")[1];

    // We will validate using the token sent inside the header
    if (Util.verifyUser(token))
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_OK, "{\"Authenticated\": \"true\"}");
    else
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, "{ \"Authenticated\": \"false\"}");
  }
  
  private static void handleLoginPostRequest(HttpExchange exchange) throws IOException {
    // Reading Json data from the request body
    String requestBody = ReqRes.readRequestBody(exchange.getRequestBody());

    // Convert JSON data to Login object
    Login login = (Login) ReqRes.makeModelFromJson(requestBody, Login.class);
    // Rechecking validity of the login info
    try {
      Validate.idNumber(login.getIdNumber());
      Validate.password(login.getPassword());
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
      String storedPassword = Database.getUserPassword(login.getIdNumber());
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
    String payLoad = ReqRes.makeJsonString(login);
    String jwt = Util.signUser(payLoad);

    exchange.getResponseHeaders().set("Authorization", "Bearer " + jwt);
    ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_OK, "{\"Authenticated\": \"true\"}");
  }
  
  private static void handleSignUp(HttpExchange exchange) throws IOException {
    // Request body
    String requestBody = ReqRes.readRequestBody(exchange.getRequestBody());
    Student student = (Student) ReqRes.makeModelFromJson(requestBody, Student.class);

    // Read hashed password from the request header
    String passwordJson = exchange.getRequestHeaders().getFirst("Password");
    SecurePassword password = (SecurePassword) ReqRes.makeModelFromJson(passwordJson, SecurePassword.class);
    String hashedPassword = password.getHashedPassword();

    System.out.println(requestBody);

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
      System.out.println(hashedPassword);
    } catch (SQLException e) {
      var error = new Message();
      error.setMessage(e.getMessage());
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, ReqRes.makeJsonString(error));
      return;
    }

    // Send success response
    var message = new Message();
    message.setMessage("Operation Successful.");
    ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_CREATED, ReqRes.makeJsonString(message));
  }

  private static void handLogout(HttpExchange exchange) {
  }

  public static void main(String[] args) {
  }
}
