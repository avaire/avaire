package com.avairebot.orion.factories;

import com.avairebot.orion.requests.Request;

public class RequestFactory {

    public static Request makeGET(String url) throws Exception {
        return new Request(url);
    }
}
