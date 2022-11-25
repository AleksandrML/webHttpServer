package ru.netology;

import java.io.IOException;


public class Main {

    public static final int serverPort = 9999;
    public static final int poolNumber = 64;

    public static void main(String[] args) throws IOException {
        var server = new Server();

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            var message = "I'm a response to get request";
            try {
                responseStream.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + "text/plain" + "\r\n" +
                                "Content-Length: " + message.length() + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n" + message
                ).getBytes());
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.run(serverPort, poolNumber);
    }

}
