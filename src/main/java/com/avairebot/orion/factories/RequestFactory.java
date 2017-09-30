package com.avairebot.orion.factories;

import com.avairebot.orion.requests.Request;

public class RequestFactory {

    public static Request makeGET(String url) {
        return new Request(url);
    }
}
