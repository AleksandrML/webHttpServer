package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public void run(int port, int poolNumber) throws IOException {

        final var validPaths = getValidPaths();
        final ExecutorService threadPool = Executors.newFixedThreadPool(poolNumber);

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    // we may need to close it afterwards but can not use try with sources to prevent closing if we don't want it, it will be closed in thread
                    final var socket = serverSocket.accept();
                    threadPool.submit(new ClientServingThread(validPaths, socket));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getValidPaths() throws IOException {
        return Files.walk(Path.of(".", "public")).map(Path::toString).filter(x -> !x.equals("./public"))
                .map(x -> x.replace("./public", "")).toList();
    }

}
