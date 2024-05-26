package com.aastu.routes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.ArrayList;

import com.aastu.database.Database;
import com.aastu.model.AdminMessage;
import com.aastu.model.ClearanceApp;
import com.aastu.model.IdNumberPayload;
import com.aastu.model.Message;
import com.aastu.model.Notification;
import com.aastu.utils.ReqRes;
import com.aastu.utils.Util;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AdminRoute implements HttpHandler {

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    // api/admin/message
    // api/admin/status/?applicantId=
    // api/admin/status/all/?newstatus=
    String path = exchange.getRequestURI().getPath();
    path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    switch (path) {
      case "/api/admin/applications":
        handleGetApplications(exchange);
        break;
      case "/api/admin/message":
        handleSendMessage(exchange);
        break;
      case "/api/admin/status":
        handleAppStatus(exchange);
        break;
      case "/api/admin/status/all":
        handleAllAppStatus(exchange);
      default:
        break;
    }
  }

  private void handleGetApplications(HttpExchange exchange) {
    Message responseMessage = new Message();

    // Authenticate the request using the auth token inside the header;
    String authToken = exchange.getRequestHeaders().getFirst("Authorization");
    String[] auth = authToken == null ? null : authToken.split(" "); // To separate the bearer key word
    if (auth == null || auth.length <= 1 || !Util.verifyUser(auth[1])) {
      responseMessage.setMessage("Unauthorized");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, ReqRes.makeJsonString(responseMessage));
      return;
    }

    // Authenticated. Then, get the payload from jwt token
    String payload = Util.getDecodedPayload(auth[1]);

    // Since the payload contains the login info meaning username and password
    IdNumberPayload idNumber = (IdNumberPayload) ReqRes.makeModelFromJson(payload, IdNumberPayload.class);
    String adminId = idNumber.getIdNumber();

    try {
      var applications = new ArrayList<ClearanceApp>();
      switch (adminId) {
        case "REGISTRAR001":
          applications = Database.getClearanceApplicationsByStatusColumn("registrarStatus");
          break;
        case "CAFETERIA001":
          applications = Database.getClearanceApplicationsByStatusColumn("diningOfficeStatus");
          break;
        case "DORMITORY001":
          applications = Database.getClearanceApplicationsByStatusColumn("dormitoryStatus");
          break;
        case "COLLEGEADMIN001":
          applications = Database.getClearanceApplicationsByStatusColumn("collegeAdminStatus");
          break;
        default:
          break;
      }

      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_ACCEPTED, ReqRes.makeJsonString(applications));
    } catch (Exception e) {
      responseMessage.setMessage("Unable to fetch data");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, ReqRes.makeJsonString(responseMessage));
    }
  }

  private void handleAllAppStatus(HttpExchange exchange) {
    Message responseMessage = new Message();

    // Authenticate the request using the auth token inside the header;
    String authToken = exchange.getRequestHeaders().getFirst("Authorization");
    String[] auth = authToken == null ? null : authToken.split(" "); // To separate the bearer key word
    if (auth == null || auth.length <= 1 || !Util.verifyUser(auth[1])) {
      responseMessage.setMessage("Unauthorized");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, ReqRes.makeJsonString(responseMessage));
      return;
    }

    // Authenticated. Then, get the payload from jwt token
    String payload = Util.getDecodedPayload(auth[1]);

    // Since the payload contains the login info meaning username and password
    IdNumberPayload idNumber = (IdNumberPayload) ReqRes.makeModelFromJson(payload, IdNumberPayload.class);
    String adminId = idNumber.getIdNumber();
    String newStatus = exchange.getRequestURI().getQuery().split("newstatus=")[1];

    try {
      switch (adminId) {
        case "REGISTRAR001":
          Database.updateStatusColumn("registrarStatus", newStatus);
          break;
        case "CAFETERIA001":
          Database.updateStatusColumn("diningOfficeStatus", newStatus);
          break;
        case "DORMITORY001":
          Database.updateStatusColumn("dormitoryStatus", newStatus);
          break;
        case "COLLEGEADMIN001":
          Database.updateStatusColumn("collegeAdminStatus", newStatus);
          break;
        default:
          break;
      }
      responseMessage.setMessage("Status updated successfully");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_ACCEPTED, ReqRes.makeJsonString(responseMessage));
    } catch (Exception e) {
      System.out.println(e.getMessage());
      responseMessage.setMessage("Internal Error! Please try again later");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, ReqRes.makeJsonString(responseMessage));
    }
  }

  private void handleAppStatus(HttpExchange exchange) {
    Message responseMessage = new Message();

    // Authenticate the request using the auth token inside the header;
    String authToken = exchange.getRequestHeaders().getFirst("Authorization");
    String[] auth = authToken == null ? null : authToken.split(" "); // To separate the bearer key word
    if (auth == null || auth.length <= 1 || !Util.verifyUser(auth[1])) {
      responseMessage.setMessage("Unauthorized");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, ReqRes.makeJsonString(responseMessage));
      return;
    }

    // Authenticated. Then, get the payload from jwt token
    String payload = Util.getDecodedPayload(auth[1]);

    // Since the payload contains the login info meaning username and password
    IdNumberPayload idNumber = (IdNumberPayload) ReqRes.makeModelFromJson(payload, IdNumberPayload.class);
    String adminId = idNumber.getIdNumber();
    String applicationId = exchange.getRequestURI().getQuery().split("applicationId=")[1];

    Message newStatus = (Message) ReqRes.makeModelFromJson(ReqRes.readRequestBody(exchange.getRequestBody()),
        Message.class);
    String newStatusMessage = newStatus.getMessage();

    try {
      switch (adminId) {
        case "REGISTRAR001":
          Database.updateStatusColumn(Integer.parseInt(applicationId), "registrarStatus", newStatusMessage);
          break;
        case "CAFETERIA001":
          Database.updateStatusColumn(Integer.parseInt(applicationId), "diningOfficeStatus", newStatusMessage);
          break;
        case "DORMITORY001":
          Database.updateStatusColumn(Integer.parseInt(applicationId), "dormitoryStatus", newStatusMessage);
          break;
        case "COLLEGEADMIN001":
          Database.updateStatusColumn(Integer.parseInt(applicationId), "collegeAdminStatus", newStatusMessage);
          break;
        default:
          break;
      }
      responseMessage.setMessage("Operation successful");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_ACCEPTED, ReqRes.makeJsonString(responseMessage));
    } catch (Exception e) {
      responseMessage.setMessage("Internal Error! Please try again later");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, ReqRes.makeJsonString(responseMessage));
    }
  }

  private void handleSendMessage(HttpExchange exchange) {
    String jsonBody = ReqRes.readRequestBody(exchange.getRequestBody());
    AdminMessage adminMessage = (AdminMessage) ReqRes.makeModelFromJson(jsonBody, AdminMessage.class);

    String receiver = adminMessage.getReceiverId();
    String message = adminMessage.getMessage();

    Message responseMessage = new Message();
    // Authenticate the request using the auth token inside the header;
    String authToken = exchange.getRequestHeaders().getFirst("Authorization");
    String[] auth = authToken == null ? null : authToken.split(" "); // To separate the bearer key word
    if (auth == null || auth.length <= 1 || !Util.verifyUser(auth[1])) {
      responseMessage.setMessage("Unauthorized");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, ReqRes.makeJsonString(responseMessage));
      return;
    }

    // Authenticated. Then, get the payload from jwt token
    String payload = Util.getDecodedPayload(auth[1]);

    // Since the payload contains the login info meaning username and password
    IdNumberPayload idNumber = (IdNumberPayload) ReqRes.makeModelFromJson(payload, IdNumberPayload.class);
    String adminId = idNumber.getIdNumber();

    try {
      String adminName = Database.getAdminName(adminId);
      if (receiver.equals("ALL")) {
        var ids = Database.getStudentIds();
        for (String id : ids) {
          Database.createNotification(id,
              new Notification("Broadcasted message from " + adminName, message, Util.getDateString()));
        }
      } else {
        Database.createNotification(receiver,
            new Notification("You have new message from " + adminName, message, Util.getDateString()));
      }
      responseMessage.setMessage("Message sent succesfully.");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_CREATED, ReqRes.makeJsonString(responseMessage));
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      responseMessage.setMessage("Internal error occured");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, ReqRes.makeJsonString(responseMessage));
    }

  }

}
