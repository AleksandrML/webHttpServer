package ru.netology;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

class ClientServingThread extends Thread {

    private final Socket socket;

    private final Server server;
    private final List<String> validPaths;

    public ClientServingThread(List<String> validPaths, Socket socket, Server server) throws IOException {
        this.validPaths = validPaths;
        this.socket = socket;
        this.server = server;
        start();
    }

    @Override
    public void run() {
        BufferedReader in = null;
        BufferedOutputStream out = null;
        while (true) {
            try {
                if (socket.isClosed()) {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                    return;
                }
                // it was final vars init, but we need to close it and yet can not use with sources to prevent closing if we don't want it
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedOutputStream(socket.getOutputStream());
                // read only request line for simplicity
                // must be in form GET /path HTTP/1.1
                final var requestLine = in.readLine();
                if (requestLine != null) {
                    final var parts = requestLine.split(" ");

                    if (parts.length != 3) {
                        // close socket and sources
                        socket.close();
                        in.close();
                        out.close();
                        return;
                    }

                    final var path = parts[1];  // priority to static paths as it was before, then to handlers if there are not any static paths for requested
                    if (!validPaths.contains(path)) {
                        Request request = new Request().parse(parts);
                        if (!server.checkForProperlyHandleEndpoint(request, out)) {
                            continue;
                        }
                        server.getHandler(request).handle(request, out);
                    } else {
                        final var filePath = Path.of(".", "public", path);
                        final var mimeType = Files.probeContentType(filePath);

                        // special case for classic
                        if (path.equals("/classic.html")) {
                            final var template = Files.readString(filePath);
                            final var content = template.replace(
                                    "{time}",
                                    LocalDateTime.now().toString()
                            ).getBytes();
                            out.write((
                                    "HTTP/1.1 200 OK\r\n" +
                                            "Content-Type: " + mimeType + "\r\n" +
                                            "Content-Length: " + content.length + "\r\n" +
                                            "Connection: close\r\n" +
                                            "\r\n"
                            ).getBytes());
                            out.write(content);
                            out.flush();
                            continue;
                        }

                        final var length = Files.size(filePath);
                        out.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        Files.copy(filePath, out);
                        out.flush();
                    }

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
