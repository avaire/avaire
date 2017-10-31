package com.avairebot.orion.contracts.chat;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class Restable {

    protected final TextChannel channel;

    public Restable(TextChannel channel) {
        this.channel = channel;
    }

    /**
     * Submits a Request for execution.
     * <br>Using the default callback functions:
     * {@link net.dv8tion.jda.core.requests.RestAction#DEFAULT_SUCCESS DEFAULT_SUCCESS} and
     * {@link net.dv8tion.jda.core.requests.RestAction#DEFAULT_FAILURE DEFAULT_FAILURE}
     * <p>
     * <p><b>This method is asynchronous</b>
     */
    public void queue() {
        sendMessage().queue();
    }

    /**
     * Submits a Request for execution.
     * <br>Using the default failure callback function.
     * <p>
     * <p><b>This method is asynchronous</b>
     *
     * @param success The success callback that will be called at a convenient time
     *                for the API. (can be null)
     */
    public void queue(Consumer<Message> success) {
        sendMessage().queue(success);
    }

    /**
     * Submits a Request for execution.
     * <p>
     * <p><b>This method is asynchronous</b>
     *
     * @param success The success callback that will be called at a convenient time
     *                for the API. (can be null)
     * @param failure The failure callback that will be called if the Request
     *                encounters an exception at its execution point.
     */
    public void queue(Consumer<Message> success, Consumer<Throwable> failure) {
        sendMessage().queue(success, failure);
    }

    /**
     * Schedules a call to {@link #queue()} to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     * <p>
     * <p>This operation gives no access to the response value.
     * <br>Use {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer)} to access
     * the success consumer for {@link #queue(java.util.function.Consumer)}!
     * <p>
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
    public ScheduledFuture<?> queueAfter(long delay, TimeUnit unit) {
        return sendMessage().queueAfter(delay, unit);
    }

    /**
     * Schedules a call to {@link #queue(java.util.function.Consumer)} to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     * <p>
     * <p>This operation gives no access to the failure callback.
     * <br>Use {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer, java.util.function.Consumer)} to access
     * the failure consumer for {@link #queue(java.util.function.Consumer, java.util.function.Consumer)}!
     * <p>
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
    public ScheduledFuture<?> queueAfter(long delay, TimeUnit unit, Consumer<Message> success) {
        return sendMessage().queueAfter(delay, unit, success);
    }

    /**
     * Schedules a call to {@link #queue(java.util.function.Consumer, java.util.function.Consumer)}
     * to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     * <p>
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
    public ScheduledFuture<?> queueAfter(long delay, TimeUnit unit, Consumer<Message> success, Consumer<Throwable> failure) {
        return sendMessage().queueAfter(delay, unit, success, failure);
    }

    /**
     * Schedules a call to {@link #queue()} to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     * <p>
     * <p>This operation gives no access to the response value.
     * <br>Use {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer)} to access
     * the success consumer for {@link #queue(java.util.function.Consumer)}!
     * <p>
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
    public ScheduledFuture<?> queueAfter(long delay, TimeUnit unit, ScheduledExecutorService executor) {
        return sendMessage().queueAfter(delay, unit, executor);
    }

    /**
     * Schedules a call to {@link #queue(java.util.function.Consumer)} to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     * <p>
     * <p>This operation gives no access to the failure callback.
     * <br>Use {@link #queueAfter(long, java.util.concurrent.TimeUnit, java.util.function.Consumer, java.util.function.Consumer)} to access
     * the failure consumer for {@link #queue(java.util.function.Consumer, java.util.function.Consumer)}!
     * <p>
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
    public ScheduledFuture<?> queueAfter(long delay, TimeUnit unit, Consumer<Message> success, ScheduledExecutorService executor) {
        return sendMessage().queueAfter(delay, unit, success, executor);
    }

    /**
     * Schedules a call to {@link #queue(java.util.function.Consumer, java.util.function.Consumer)}
     * to be executed after the specified {@code delay}.
     * <br>This is an <b>asynchronous</b> operation that will return a
     * {@link java.util.concurrent.ScheduledFuture ScheduledFuture} representing the task.
     * <p>
     * <p>The specified {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} is used for this operation.
     *
     * @param delay    The delay after which this computation should be executed, negative to execute immediately
     * @param unit     The {@link java.util.concurrent.TimeUnit TimeUnit} to convert the specified {@code delay}
     * @param success  The success {@link java.util.function.Consumer Consumer} that should be called
     *                 once the {@link #queue(java.util.function.Consumer, java.util.function.Consumer)} operation completes successfully.
     * @param failure  The failure {@link java.util.function.Consumer Consumer} that should be called
     *                 in case of an error of the {@link #queue(java.util.function.Consumer, java.util.function.Consumer)} operation.
     * @param executor The Non-null {@link java.util.concurrent.ScheduledExecutorService ScheduledExecutorService} that should be used
     *                 to schedule this operation
     * @return {@link java.util.concurrent.ScheduledFuture ScheduledFuture}
     * representing the delayed operation
     * @throws java.lang.IllegalArgumentException If the provided TimeUnit or ScheduledExecutorService is {@code null}
     */
    public ScheduledFuture<?> queueAfter(long delay, TimeUnit unit, Consumer<Message> success, Consumer<Throwable> failure, ScheduledExecutorService executor) {
        return sendMessage().queueAfter(delay, unit, success, failure, executor);
    }

    private RestAction<Message> sendMessage() {
        if (channel == null) {
            throw new RuntimeException("Invalid channel given, the channel can not be null!");
        }
        return channel.sendMessage(buildEmbed());
    }

    public abstract MessageEmbed buildEmbed();
}
