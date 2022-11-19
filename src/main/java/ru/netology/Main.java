package ru.netology;

import java.io.IOException;


public class Main {

    public static final int serverPort = 9999;
    public static final int poolNumber = 64;

    public static void main(String[] args) throws IOException {
        var server = new Server();
        server.run(serverPort, poolNumber);
    }

}
