package com.aastu.routes;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ClearanceRoute implements HttpHandler {

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    exchange.getRequestURI().getQuery();
    
  }
  
}
