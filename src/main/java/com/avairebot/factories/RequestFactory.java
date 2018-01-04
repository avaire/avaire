package com.avairebot.factories;

import com.avairebot.requests.Request;

public class RequestFactory {

    public static Request makeGET(String url) {
        return new Request(url);
    }
}
