package com.aastu.utils;

import com.aastu.model.Notification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ReqRes {

  public static String makeJsonString(Object obj) {
    Gson gson = new GsonBuilder().setDateFormat("h:mm a, E, MMM dd, yyyy").create();
    return gson.toJson(obj);
  }

  public static Object makeModelFromJson(String jsonString, java.lang.reflect.Type type) {
    try {
      Gson gson = new GsonBuilder().setDateFormat("h:mm a, E, MMM dd, yyyy").create();
      return gson.fromJson(jsonString, type);
    } catch (Exception e) {
      System.out.println("Error");
      System.out.println(e.getMessage());
      return null;
    }
  }

  public static String readRequestBody(InputStream requestBodyStream) {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(requestBodyStream, StandardCharsets.UTF_8));
    StringBuilder requestBody = new StringBuilder();
    String line;
    try {
      while ((line = reader.readLine()) != null)
        requestBody.append(line);
      reader.close();
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    return requestBody.toString();
  }

  public static void sendResponse(HttpExchange exchange, int statusCode, String jsonBody) {
    try {
      exchange.sendResponseHeaders(statusCode, jsonBody.getBytes().length);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.getResponseBody().write(jsonBody.getBytes());
      exchange.getResponseBody().close();
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }


  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    ArrayList<Integer> nums = new ArrayList<>();
    nums.add(1);
    nums.add(2);
    nums.add(3);
    nums.add(4);
    nums.add(5);

    ArrayList<Notification> notifications = new ArrayList<>();
    notifications.add(new Notification("Abbbb", "Bccccc", Util.getDateString()));
    notifications.add(new Notification("Abbbb", "Bccccc", Util.getDateString()));
    notifications.add(new Notification("Abbbb", "Bccccc", Util.getDateString()));
    notifications.add(new Notification("Abbbb", "Bccccc", Util.getDateString()));
    notifications.add(new Notification("Abbbb", "Bccccc", Util.getDateString()));
    notifications.add(new Notification("Abbbb", "Bccccc", Util.getDateString()));
    String jsonNotficatioString = makeJsonString(notifications);
    System.out.println(jsonNotficatioString);

    notifications = (ArrayList<Notification>) makeModelFromJson(jsonNotficatioString, notifications.getClass());
    System.out.println(notifications.size());

    Notification[] notifications2 = new Notification[6];
    notifications2 = (Notification[]) makeModelFromJson(jsonNotficatioString, notifications2.getClass());

    for (Notification notification : notifications2) {
      System.out.println(notification.getTitle());
    }
    
    
  }
}
