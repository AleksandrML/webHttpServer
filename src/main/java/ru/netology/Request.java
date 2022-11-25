package ru.netology;

import java.util.List;

public class Request {

    private String requestMethod;
    private String path;
    private List<String> headers;
    private String body;

    public Request parse(String[] requestParts) {
        this.requestMethod = requestParts[0];
        this.path = requestParts[1];
        return this;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getPath() {
        return path;
    }

}
