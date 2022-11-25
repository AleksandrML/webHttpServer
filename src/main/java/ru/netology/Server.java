package ru.netology;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class Server {

    private final Map<String, Handler> concurrentHashMapForRequest = new ConcurrentHashMap<>();

    public void run(int port, int poolNumber) throws IOException {

        final var validPaths = getValidPaths();
        final ExecutorService threadPool = Executors.newFixedThreadPool(poolNumber);

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    // we may need to close it afterwards but can not use try with sources to prevent closing if we don't want it, it will be closed in thread
                    final var socket = serverSocket.accept();
                    threadPool.submit(new ClientServingThread(validPaths, socket, this));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getValidPaths() throws IOException {
        return Stream.of(new File("public").listFiles()).filter(file -> !file.isDirectory())
                .map(File::getName).map(x -> "/" + x).toList();
    }

    public Boolean checkForProperlyHandleEndpoint(Request request, BufferedOutputStream out) throws IOException {
        if (!concurrentHashMapForRequest.containsKey(request.getRequestMethod() + request.getPath())) {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
            return false;
        }
        return true;
    }

    public void addHandler(String requestMethod, String path, Handler handler) {
        concurrentHashMapForRequest.put(requestMethod + path, handler);
    }

    public Handler getHandler(Request request) {
        return concurrentHashMapForRequest.get(request.getRequestMethod() + request.getPath());
    }

}
