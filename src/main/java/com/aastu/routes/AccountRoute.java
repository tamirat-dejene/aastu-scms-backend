package com.aastu.routes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;

import com.aastu.database.Database;
import com.aastu.model.IdNumberPayload;
import com.aastu.model.Message;
import com.aastu.model.SecurePassword;
import com.aastu.model.UpdatePassword;
import com.aastu.utils.ReqRes;
import com.aastu.utils.Util;
import com.aastu.utils.Validate;
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
          default:
            break;
        }
        break;

      default:
        break;
    }
  }

  public static void handleChangePwd(HttpExchange exchange) throws IOException {
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
