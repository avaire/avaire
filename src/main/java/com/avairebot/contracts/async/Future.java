/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

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
    private static final ExecutorService service = Executors.newFixedThreadPool(3);

    /**
     * The default success consumer that should be used if no success consumer is given.
     */
    private Consumer<Response> defaultSuccess = (Response) -> {
        //
    };

    /**
     * The default failure consumer that should be used if no failure consumer is given.
     */
    private Consumer<Throwable> defaultFailure = (Exception) -> {
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
     * invoked with the content of the request, if the future request fails
     * the failure consumer will be invoked instead.
     *
     * @param success The consumer that should be invoked on success.
     * @param failure The consumer that should be invoked on failure.
     */
    public void send(final Consumer success, final Consumer<Throwable> failure) {
        service.submit(() -> handle(
            success == null ? defaultSuccess : success,
            failure == null ? defaultFailure : failure

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
