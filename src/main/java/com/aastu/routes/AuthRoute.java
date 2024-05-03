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
        sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "{\"message\": \"Bad Request\"}");
        break;
    }
  }

  private static void handleLogin(HttpExchange exchange) throws IOException {
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
      sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, errorJson);
      return;
    }
    // Database query
    try {
      String storedPassword = Database.getUserPassword(login.getIdNumber());
      var inputPassword = login.getPassword();

      if (!SecurePassword.authenticatePassword(inputPassword, storedPassword)) {
        var error = new Message();
        error.setMessage("Unauthenticated user: provided incorrect password");
        sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, ReqRes.makeJsonString(error));
        return;
      }
    } catch (SQLException e) {
      var error = new Message();
      error.setMessage("Server Error");
      sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, ReqRes.makeJsonString(error));
      return;
    }
    // The user is authenticated: Sign the user with jwt
    String payLoad = ReqRes.makeJsonString(login);
    String jwt = Util.signUser(payLoad);
    
    exchange.getResponseHeaders().set("Authorization", "Bearer " + jwt);
    sendResponse(exchange, HttpURLConnection.HTTP_OK, "{\"Authenticated\": \"true\"}");
  }

  private static void handleSignUp(HttpExchange exchange) throws IOException {
    // Request body
    String requestBody = ReqRes.readRequestBody(exchange.getRequestBody());
    Student student = (Student) ReqRes.makeModelFromJson(requestBody, Student.class);


    // Validating the user request inputs.
    try {
      Validate.section(student.getSection());
      Validate.email(student.getEmailAddress());
      Validate.idNumber(student.getIdNumber());
      Validate.name(student.getFirstName());
      Validate.name(student.getMiddleName());
      Validate.name(student.getLastName());
      Validate.classYear(Integer.toString(student.getClassYear()));
      Validate.password(student.getPassword());
    } catch (Error e) {
      var error = new Message();
      error.setMessage(e.getMessage());
      sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, ReqRes.makeJsonString(error));
      return;
    }

    // It has passed the validation: now save to database
    try {
      Database.saveStudent(student, student.getPassword());
    } catch (SQLException e) {
      var error = new Message();
      error.setMessage(e.getMessage());
      sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, ReqRes.makeJsonString(error));
      return;
    }

    // Send success response
    var message = new Message();
    message.setMessage("Operation Successful.");
    sendResponse(exchange, HttpURLConnection.HTTP_CREATED, ReqRes.makeJsonString(message));
  }

  private static void handLogout(HttpExchange exchange) {
  }

  private static void sendResponse(HttpExchange exchange, int statusCode, String jsonBody) throws IOException {
    exchange.sendResponseHeaders(statusCode, jsonBody.getBytes().length);
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.getResponseBody().write(jsonBody.getBytes());
    exchange.getResponseBody().close();
  }


  public static void main(String[] args) {
  }
}
