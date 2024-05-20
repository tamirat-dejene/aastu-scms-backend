package com.aastu.utils;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ReqRes {

  public static String makeJsonString(Object obj) {
    Gson gson = new Gson();
    return gson.toJson(obj);
  }

  public static Object makeModelFromJson(String jsonString, java.lang.reflect.Type type) {
    try {
      Gson gson = new Gson();
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

}
