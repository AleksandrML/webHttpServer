package ru.netology;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Request {

    private String requestMethod;
    private String path;
    private List<NameValuePair> pairs;

    public Request parse(String[] requestParts) {
        this.requestMethod = requestParts[0];
        var index = requestParts[1].indexOf('?');
        if (index >= 0) {
            this.path = requestParts[1].substring(0, index);
            this.pairs = URLEncodedUtils.parse(requestParts[1].substring(index+1), Charset.forName("UTF-8"));
        } else {
            this.path = requestParts[1];
        }
        return this;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getPath() {
        return path;
    }

    public List<NameValuePair> getQueryParams() {
        return this.pairs;
    }

    public List<String> getQueryParam(String name) {
        List<String> values = new ArrayList<>();  // there may be several equal names, so we need to parse them all
        if (this.pairs == null) {
                return values;  // empty list
        }
        for (NameValuePair parameter : this.pairs) {
            if (parameter.getName().equals(name)) {
                values.add(parameter.getValue());
            }
        }
        return values;
    }

}
