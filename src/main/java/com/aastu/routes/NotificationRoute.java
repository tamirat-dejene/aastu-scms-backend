package com.aastu.routes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.ArrayList;

import com.aastu.database.Database;
import com.aastu.model.IdNumberPayload;
import com.aastu.model.Message;
import com.aastu.model.Notification;
import com.aastu.utils.ReqRes;
import com.aastu.utils.Util;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class NotificationRoute implements HttpHandler {

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    switch (exchange.getRequestMethod()) {
      case "GET":
        hanleGetNotification(exchange);
        break;

      default:
        break;
    }
  }

  private void hanleGetNotification(HttpExchange exchange) {
    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
    if (authHeader == null) {
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, "{ \"Authenticated\": \"false\"}");
      return;
    }
    // Separate the Bearer from the token i.e. Authorization = Bearer token
    String[] token = authHeader.split(" ");

    // We will validate using the token sent inside the header
    if (token.length <= 1 || !Util.verifyUser(token[1])) {
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, "{ \"Authenticated\": \"false\"}");
      return;
    }

    // Get user id from the payload
    String payload = Util.getDecodedPayload(token[1]);
    IdNumberPayload id = (IdNumberPayload) ReqRes.makeModelFromJson(payload, IdNumberPayload.class);

    try {
      ArrayList<Notification> notifications = Database.getNotificationsByStudentId(id.getIdNumber());
      String jsonArray = ReqRes.makeJsonString(notifications);
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_ACCEPTED, jsonArray);
    } catch (SQLException e) {
      Message message = new Message();
      message.setMessage("Unable to fetch notifications");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, ReqRes.makeJsonString(message));
    }
  }

}
