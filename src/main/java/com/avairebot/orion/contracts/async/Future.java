package com.avairebot.orion.contracts.async;

import com.avairebot.orion.requests.Response;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.function.Consumer;

public abstract class Future {

    private Consumer<Response> DEFAULT_SUCCESS = (Response) -> {
    };
    private Consumer<Throwable> DEFAULT_FAILURE = (Exception) -> {
        SimpleLog.getLog(Future.class).fatal(String.format(
                "Future Consumer returned failure: [%s] %s", Exception.getClass().getSimpleName(), Exception.getMessage()
        ));
    };

    public void send() {
        this.send(null, null);
    }

    public void send(Consumer success) {
        this.send(success, null);
    }

    public void send(final Consumer success, final Consumer<Throwable> failure) {
        new Thread(() -> {
            handle(
                    success == null ? DEFAULT_SUCCESS : success,
                    failure == null ? DEFAULT_FAILURE : failure

            );
        }).start();
    }

    public abstract void handle(Consumer success, Consumer<Throwable> failure);
}
