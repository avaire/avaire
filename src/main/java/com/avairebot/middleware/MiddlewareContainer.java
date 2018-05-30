package com.avairebot.middleware;

import com.avairebot.contracts.middleware.Middleware;

class MiddlewareContainer {

    private final Middleware middleware;
    private final String[] arguments;

    MiddlewareContainer(Middleware middleware) {
        this.middleware = middleware;
        this.arguments = new String[0];
    }

    MiddlewareContainer(Middleware middleware, String[] arguments) {
        this.middleware = middleware;
        this.arguments = arguments;
    }

    Middleware getMiddleware() {
        return middleware;
    }

    String[] getArguments() {
        return arguments;
    }
}
