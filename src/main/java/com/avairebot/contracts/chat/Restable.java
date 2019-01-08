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

package com.avairebot.contracts.chat;

import com.avairebot.utilities.CheckPermissionUtil;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class Restable {

    /**
     * The message channel that the restable instance should be used for.
     */
    protected final MessageChannel channel;

    /**
     * Creates a new restable instance for the given message channel, allowing
     * for easy access to JDAs queue system for Rest messages, if no channel
     * is given the message won't be able to be sent, and will throw a
     * {@link RuntimeException runtime exception} instead.
     *
     * @param channel The channel that the message should be sent to, or {@code NULL}.
     */
    public Restable(@Nullable MessageChannel channel) {
        this.channel = channel;
    }

    /**
     * Builds the embedded message that should
     * be sent to the set message channel.
     *
     * @return The JDA {@link MessageEmbed embed} instance.
     */
    public abstract MessageEmbed buildEmbed();

    /**
     * Submits a Request for execution.
     * <br>Using the default callback functions:
     * {@link net.dv8tion.jda.core.requests.RestAction#DEFAULT_SUCCESS DEFAULT_SUCCESS} and
     * {@link net.dv8tion.jda.core.requests.RestAction#DEFAULT_FAILURE DEFAULT_FAILURE}
     * <br>
     * <p><b>This method is asynchronous</b>
     */
    public void queue() {
        sendMessage().ifPresent(action -> action.queue(null, RestActionUtil.handleMessageCreate));
    }

    /**
     * Submits a Request for execution.
     * <br>Using the default failure callback function.
     * <br>
     * <p><b>This method is asynchronous</b>
     *
     * @param success The success callback that will be called at a convenient time
     *                for the API. (can be null)
     */
    public void queue(Consumer<Message> success) {
        sendMessage().ifPresent(action -> action.queue(success, RestActionUtil.handleMessageCreate));
    }

    /**
     * Submits a Request for execution.
     * <br>
     * <p><b>This method is asynchronous</b>
     *
     * @param success The success callback that will be called at a convenient time
     *                for the API. (can be null)
     * @param failure The failure callback that will be called if the Request
     *                encounters an exception at its execution point.
     */
    public void queue(Consumer<Message> success, Consumer<Throwable> failure) {
        sendMessage().ifPresent(action -> action.queue(success, failure == null ? RestActionUtil.handleMessageCreate : failure));
    }

    /**
     * Schedules a call to {@link #queue()} to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     * <br>
     * <p>This operation gives no access to the response value.
     * <br>Use {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer)} to access
     * the success consumer for {@link #queue(java.util.function.Consumer)}!
     * <br>
     * <p>The global JDA {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} is used for this operation.
     * <br>You can change the core pool size for this Executor through {@link net.dv8tion.jda.core.JDABuilder#setCorePoolSize(int) JDABuilder.setCorePoolSize(int)}
     * or provide your own Executor with {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.concurrent.ScheduledExecutorService)}
     *
     * @param delay The delay after which this computation should be executed, negative to execute immediately
     * @param unit  The {@link java.util.concurrent.TimeUnit TimeUnit} to convert the specified {@code delay}
     * @return {@link java.util.concurrent.ScheduledFuture ScheduledFuture}
     * representing the delayed operation
     * @throws java.lang.IllegalArgumentException If the provided TimeUnit is {@code null}
     */
    public Future<?> queueAfter(long delay, TimeUnit unit) {
        Optional<MessageAction> messageAction = sendMessage();
        if (messageAction.isPresent()) {
            return messageAction.get().queueAfter(delay, unit);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Schedules a call to {@link #queue(java.util.function.Consumer)} to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     * <br>
     * <p>This operation gives no access to the failure callback.
     * <br>Use {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer, java.util.function.Consumer)} to access
     * the failure consumer for {@link #queue(java.util.function.Consumer, java.util.function.Consumer)}!
     * <br>
     * <p>The global JDA {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} is used for this operation.
     * <br>You can change the core pool size for this Executor through {@link net.dv8tion.jda.core.JDABuilder#setCorePoolSize(int) JDABuilder.setCorePoolSize(int)}
     * or provide your own Executor with {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer, java.util.concurrent.ScheduledExecutorService)}
     *
     * @param delay   The delay after which this computation should be executed, negative to execute immediately
     * @param unit    The {@link java.util.concurrent.TimeUnit TimeUnit} to convert the specified {@code delay}
     * @param success The success {@link java.util.function.Consumer Consumer} that should be called
     *                once the {@link #queue(java.util.function.Consumer)} operation completes successfully.
     * @return {@link java.util.concurrent.ScheduledFuture ScheduledFuture}
     * representing the delayed operation
     * @throws java.lang.IllegalArgumentException If the provided TimeUnit is {@code null}
     */
    public Future<?> queueAfter(long delay, TimeUnit unit, Consumer<Message> success) {
        Optional<MessageAction> messageAction = sendMessage();
        if (messageAction.isPresent()) {
            return messageAction.get().queueAfter(delay, unit, success, RestActionUtil.handleMessageCreate);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Schedules a call to {@link #queue(java.util.function.Consumer, java.util.function.Consumer)}
     * to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     * <br>
     * <p>The global JDA {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} is used for this operation.
     * <br>You can change the core pool size for this Executor through {@link net.dv8tion.jda.core.JDABuilder#setCorePoolSize(int) JDABuilder.setCorePoolSize(int)}
     * or provide your own Executor with
     * {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer, java.util.function.Consumer, java.util.concurrent.ScheduledExecutorService)}
     *
     * @param delay   The delay after which this computation should be executed, negative to execute immediately
     * @param unit    The {@link java.util.concurrent.TimeUnit TimeUnit} to convert the specified {@code delay}
     * @param success The success {@link java.util.function.Consumer Consumer} that should be called
     *                once the {@link #queue(java.util.function.Consumer, java.util.function.Consumer)} operation completes successfully.
     * @param failure The failure {@link java.util.function.Consumer Consumer} that should be called
     *                in case of an error of the {@link #queue(java.util.function.Consumer, java.util.function.Consumer)} operation.
     * @return {@link java.util.concurrent.ScheduledFuture ScheduledFuture}
     * representing the delayed operation
     * @throws java.lang.IllegalArgumentException If the provided TimeUnit is {@code null}
     */
    public Future<?> queueAfter(long delay, TimeUnit unit, Consumer<Message> success, Consumer<Throwable> failure) {
        Optional<MessageAction> messageAction = sendMessage();
        if (messageAction.isPresent()) {
            return messageAction.get().queueAfter(delay, unit, success, failure == null ? RestActionUtil.handleMessageCreate : failure);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Schedules a call to {@link #queue()} to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     * <br>
     * <p>This operation gives no access to the response value.
     * <br>Use {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer)} to access
     * the success consumer for {@link #queue(java.util.function.Consumer)}!
     * <br>
     * <p>The specified {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} is used for this operation.
     *
     * @param delay    The delay after which this computation should be executed, negative to execute immediately
     * @param unit     The {@link java.util.concurrent.TimeUnit TimeUnit} to convert the specified {@code delay}
     * @param executor The Non-null {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} that should be used
     *                 to schedule this operation
     * @return {@link java.util.concurrent.ScheduledFuture ScheduledFuture}
     * representing the delayed operation
     * @throws java.lang.IllegalArgumentException If the provided TimeUnit or ScheduledExecutorService is {@code null}
     */
    public Future<?> queueAfter(long delay, TimeUnit unit, ScheduledExecutorService executor) {
        Optional<MessageAction> messageAction = sendMessage();
        if (messageAction.isPresent()) {
            return messageAction.get().queueAfter(delay, unit, null, RestActionUtil.handleMessageCreate, executor);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Schedules a call to {@link #queue(java.util.function.Consumer)} to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     * <br>
     * <p>This operation gives no access to the failure callback.
     * <br>Use {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer, java.util.function.Consumer)} to access
     * the failure consumer for {@link #queue(java.util.function.Consumer, java.util.function.Consumer)}!
     * <br>
     * <p>The specified {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} is used for this operation.
     *
     * @param delay    The delay after which this computation should be executed, negative to execute immediately
     * @param unit     The {@link java.util.concurrent.TimeUnit TimeUnit} to convert the specified {@code delay}
     * @param success  The success {@link java.util.function.Consumer Consumer} that should be called
     *                 once the {@link #queue(java.util.function.Consumer)} operation completes successfully.
     * @param executor The Non-null {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} that should be used
     *                 to schedule this operation
     * @return {@link java.util.concurrent.ScheduledFuture ScheduledFuture}
     * representing the delayed operation
     * @throws java.lang.IllegalArgumentException If the provided TimeUnit or ScheduledExecutorService is {@code null}
     */
    public Future<?> queueAfter(long delay, TimeUnit unit, Consumer<Message> success, ScheduledExecutorService executor) {
        Optional<MessageAction> messageAction = sendMessage();
        if (messageAction.isPresent()) {
            return messageAction.get().queueAfter(delay, unit, success, RestActionUtil.handleMessageCreate, executor);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Schedules a call to {@link #queue(java.util.function.Consumer, java.util.function.Consumer)}
     * to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     * <br>
     * <p>The specified {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} is used for this operation.
     *
     * @param delay    The delay after which this computation should be executed, negative to execute immediately
     * @param unit     The {@link TimeUnit TimeUnit} to convert the specified {@code delay}
     * @param success  The success {@link Consumer Consumer} that should be called
     *                 once the {@link #queue(Consumer, Consumer)} operation completes successfully.
     * @param failure  The failure {@link Consumer Consumer} that should be called
     *                 in case of an error of the {@link #queue(Consumer, Consumer)} operation.
     * @param executor The Non-null {@link ScheduledExecutorService ScheduledExecutorService} that should be used
     *                 to schedule this operation
     * @return {@link java.util.concurrent.ScheduledFuture ScheduledFuture}
     * representing the delayed operation
     * @throws java.lang.IllegalArgumentException If the provided TimeUnit or ScheduledExecutorService is {@code null}
     */
    public Future<?> queueAfter(long delay, TimeUnit unit, Consumer<Message> success, Consumer<Throwable> failure, ScheduledExecutorService executor) {
        Optional<MessageAction> messageAction = sendMessage();
        if (messageAction.isPresent()) {
            if (failure == null) {
                failure = RestActionUtil.handleMessageCreate;
            }
            return messageAction.get().queueAfter(delay, unit, success, failure, executor);
        }
        return CompletableFuture.completedFuture(null);
    }

    private Optional<MessageAction> sendMessage() {
        if (channel == null) {
            throw new RuntimeException("Message channel is NULL, can't queue message if the channel is not set!");
        }

        CheckPermissionUtil.PermissionCheckType type = CheckPermissionUtil.canSendMessages(channel);
        if (type.canSendEmbed()) {
            return Optional.of(channel.sendMessage(buildEmbed()));
        }

        if (type.canSendMessage()) {
            String message = toString();
            if (message == null || message.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(channel.sendMessage(toString()));
        }

        return Optional.empty();
    }
}
