package com.aastu.routes;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StudentRoute implements HttpHandler {

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String path = exchange.getRequestURI().getPath();

    switch (path) {
      case "/api/student/":
        
        break;
    
      default:
        break;
    }
  }
  
}
