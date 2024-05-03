package com.aastu.model;

import java.util.Map;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;

public class Request {
  private String baseUrl;
  private String path;
  private Map<String, String> headerMap;
  private String jsonBody;

  public Request() {
    baseUrl = null;
    path = null;
    headerMap = null;
    jsonBody = null;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Map<String, String> getHeaderMap() {
    return headerMap;
  }

  public void setHeaderMap(Map<String, String> headerMap) {
    this.headerMap = headerMap;
  }

  public String getJsonBody() {
    return jsonBody;
  }

  public void setJsonBody(String json) {
    this.jsonBody = json;
  }

  // @SuppressWarnings("exports")
  public Builder makeBuilder() {
    var builder = HttpRequest.newBuilder();

    String baseUrl = this.getBaseUrl();
    String path = "";
    if (baseUrl != null) {
      if (this.getPath() != null)
        path = this.getPath();
      try {
        builder.uri(new URI(baseUrl + path));
      } catch (URISyntaxException e) {
        throw new Error(e.getMessage());
      }
    }

    if (this.getHeaderMap() == null)
      return builder;

    var headerMap = this.getHeaderMap();
    var it = headerMap.entrySet().iterator();
    while (it.hasNext()) {
      var current = it.next();
      builder.setHeader(current.getKey(), current.getValue());
    }

    return builder;
  }
}
