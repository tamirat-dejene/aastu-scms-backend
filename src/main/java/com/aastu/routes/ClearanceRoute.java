package com.aastu.routes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;

import com.aastu.database.Database;
import com.aastu.model.ClearanceApplication;
import com.aastu.model.ClearanceReason;
import com.aastu.model.IdNumberPayload;
import com.aastu.model.Message;
import com.aastu.model.Status;
import com.aastu.utils.ReqRes;
import com.aastu.utils.Util;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ClearanceRoute implements HttpHandler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    // Get reuest method
    String method = exchange.getRequestMethod();

    switch (method) {
      case "POST":
        hanleClearancePost(exchange);
        break;
      case "GET":
        hanleClearanceGet(exchange);
    }
  }

  public static void hanleClearancePost(HttpExchange exchange) throws IOException {
    // Create response message
    Message message = new Message();

    // Authenticate the request using the auth token inside the header;
    String authToken = exchange.getRequestHeaders().getFirst("Authorization");
    String[] auth = authToken == null ? null : authToken.split(" "); // To separate the bearer key word
    if (auth == null || auth.length <= 1 || !Util.verifyUser(auth[1])) {
      message.setMessage("Unauthorized");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, ReqRes.makeJsonString(message));
      return;
    }

    // Authenticated. Then, get the payload from jwt token
    String payload = Util.getDecodedPayload(auth[1]);

    // Since the payload contains the login info meaning username and password
    IdNumberPayload idNumber = (IdNumberPayload) ReqRes.makeModelFromJson(payload, IdNumberPayload.class);
    String userId = idNumber.getIdNumber();

    // And lets read the clearance reason from the request body
    String clearanceString = ReqRes.readRequestBody(exchange.getRequestBody());
    ClearanceReason clearanceReason = (ClearanceReason) ReqRes.makeModelFromJson(clearanceString,
        ClearanceReason.class);

    // Build clearance application
    ClearanceApplication clearanceApplication = new ClearanceApplication(userId, clearanceReason.getClearanceReason(),
        Status.PENDING);

    // If current applicant has no other pending application -> save the applcation,
    // else -> dont
    try {
      // check if there is a pending application with the current applicant
      var pendingApp = Database.getPendingApplications(userId);
      boolean thereIsPendingApp = false;
      if (pendingApp != null) {
        for (var app : pendingApp) {
          if (app.getStatus().equals(Status.PENDING)) {
            thereIsPendingApp = true;
            break;
          }
        }
      }

      if (thereIsPendingApp) {
        message.setMessage("There is a pending application associated with the current applicant");
        ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_CONFLICT, ReqRes.makeJsonString(message));
        return;
      }

      // Save to the database
      int appNumber = Database.saveApplication(clearanceApplication);
      // create application id
      String applicationId = "AASTUSCMS-" + appNumber;
      message.setMessage(applicationId);
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_CREATED, ReqRes.makeJsonString(message));
    } catch (SQLException e) {
      message.setMessage(e.getMessage());
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, ReqRes.makeJsonString(message));
    }
  }

  public static void hanleClearanceGet(HttpExchange exchange) throws IOException {
    Message message = new Message();

    // Read the application id from the url
    // applicationNumber=AASTUSCMS-23
    String query = exchange.getRequestURI().getQuery().split("=")[1];
    String uniqueId = query.split("-")[1];

    // Authenticate the request using the auth token inside the header;
    String authToken = exchange.getRequestHeaders().getFirst("Authorization");
    String[] auth = authToken == null ? null : authToken.split(" "); // To separate the bearer key word
    if (auth == null || auth.length <= 1 || !Util.verifyUser(auth[1])) {
      message.setMessage("Unauthorized");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, ReqRes.makeJsonString(message));
      return;
    }

    // Authenticated. Then, get the payload from jwt token
    String payload = Util.getDecodedPayload(auth[1]);
    // Since the payload contains the id info meaning username
    IdNumberPayload idNumber = (IdNumberPayload) ReqRes.makeModelFromJson(payload, IdNumberPayload.class);

    try {
      String applicantId = Database.getApplicantId(Integer.parseInt(uniqueId));
      if (applicantId == null) {
        message.setMessage("Application doesn't exist");
        ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, ReqRes.makeJsonString(message));
        return;
      } else {
        if (!applicantId.equals(idNumber.getIdNumber())) {
          message.setMessage("You can only check your app status");
          ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, ReqRes.makeJsonString(message));
          return;
        }
      }

      String status = Database.getStatusString(Integer.parseInt(uniqueId));
      message.setMessage(status);
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_ACCEPTED, ReqRes.makeJsonString(message));
    } catch (Exception e) {
      System.out.println(e.getMessage());
      message.setMessage("Internal error occured");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, ReqRes.makeJsonString(message));
    }
  }
}
