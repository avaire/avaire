package com.avairebot.contracts.async;

import com.avairebot.requests.Response;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public abstract class Future {

    /**
     * The thread executor service provider, all future requests will be added to the pool of threads.
     */
    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(3);

    /**
     * The default success consumer that should be used if no success consumer is given.
     */
    private Consumer<Response> DEFAULT_SUCCESS = (Response) -> {
    };

    /**
     * The default failure consumer that should be used if no failure consumer is given.
     */
    private Consumer<Throwable> DEFAULT_FAILURE = (Exception) -> {
        LoggerFactory.getLogger(Future.class).error(String.format(
            "Future Consumer returned failure: [%s] %s", Exception.getClass().getSimpleName(), Exception.getMessage()
        ), Exception);
    };

    /**
     * Sends the future request with no consumers.
     */
    public void send() {
        this.send(null, null);
    }

    /**
     * Sends the future request, on success the given success consumer will be
     * invoked with the content of the request, if the future request fails
     * the default failure consumer will be invoked.
     *
     * @param success The consumer that should be invoked on success.
     */
    public void send(Consumer success) {
        this.send(success, null);
    }

    /**
     * Sends the future request, on success the given success consumer will be
     * invoked with the content of the reuqest, if the future request fails
     * the failure consumer will be invoked instead.
     *
     * @param success The consumer that should be invoked on success.
     * @param failure The consumer that should be invoked on failure.
     */
    public void send(final Consumer success, final Consumer<Throwable> failure) {
        SERVICE.submit(() -> handle(
            success == null ? DEFAULT_SUCCESS : success,
            failure == null ? DEFAULT_FAILURE : failure

        ));
    }

    /**
     * Handles the future request.
     *
     * @param success Never-null success consumer.
     * @param failure Never-null failure consumer.
     */
    protected abstract void handle(Consumer success, Consumer<Throwable> failure);
}
