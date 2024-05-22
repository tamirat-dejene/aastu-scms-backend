package com.aastu.routes;

import java.io.IOException;
import java.net.HttpURLConnection;

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

    // Send notification if there is any new
    boolean thereIsNewNotification = true;
    Notification notification = null;
    if (thereIsNewNotification)
      notification = new Notification("Application status", "You application has been declined", Util.getDateString());

    if (notification != null) {
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_ACCEPTED, ReqRes.makeJsonString(notification));
    } else {
      Message message = new Message();
      message.setMessage("No new notification");
      ReqRes.sendResponse(exchange, HttpURLConnection.HTTP_NOT_MODIFIED, ReqRes.makeJsonString(message));
    }

  }

}
