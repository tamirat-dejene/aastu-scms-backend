package com.aastu.utils;

import com.google.gson.Gson;

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

  public static String readRequestBody(InputStream requestBodyStream) throws IOException {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(requestBodyStream, StandardCharsets.UTF_8));
    StringBuilder requestBody = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null)
      requestBody.append(line);
    reader.close();

    return requestBody.toString();
  }
}