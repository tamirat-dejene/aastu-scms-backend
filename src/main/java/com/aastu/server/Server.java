package com.aastu.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.aastu.routes.AccountRoute;
import com.aastu.routes.AuthRoute;
import com.aastu.routes.ClearanceRoute;
import com.aastu.routes.NotificationRoute;
import com.aastu.routes.StudentRoute;
import com.aastu.utils.Util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Server {
    public static void main(String[] args) {
        try {
            Server.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    static void start() throws IOException {
        int PORT = Integer.parseInt(Util.getEnv().getProperty("HTTP_PORT"));
        String HOSTNAME = Util.getEnv().getProperty("HTTP_HOST_NAME");
        HttpServer server = HttpServer.create(new InetSocketAddress(HOSTNAME, PORT), 0);

        server.createContext("/", new HelloHandler());
        
        server.createContext("/api/clearance", new ClearanceRoute());
        server.createContext("/api/auth", new AuthRoute());
        server.createContext("/api/student", new StudentRoute());
        server.createContext("/api/account", new AccountRoute());
        server.createContext("/api/notification", new NotificationRoute());

        server.start();
        System.out.println("Server is listening on port " + PORT);
        System.out.println("[http://" + HOSTNAME + ":" + PORT + "]");
    }

    // Landing page handler
    static class HelloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Hello, There! You have landed AASTU-SCMS developed by AASTU Students!";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
