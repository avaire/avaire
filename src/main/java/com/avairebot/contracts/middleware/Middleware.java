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

package com.avairebot.contracts.middleware;

import com.avairebot.AvaIre;
import com.avairebot.metrics.Metrics;
import com.avairebot.middleware.MiddlewareStack;
import com.avairebot.plugin.JavaPlugin;
import com.avairebot.utilities.CacheUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public abstract class Middleware {

    /**
     * The Guava cache instance, used for caching the sent messages, and
     * helps determine if the response message should be sent or not.
     *
     * @see Metrics#setup(AvaIre) Metrics setup.
     */
    public static final Cache<Long, Boolean> messageCache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(2500, TimeUnit.MILLISECONDS)
        .build();

    /**
     * The AvaIre class instance, this is used to access
     * and interact with the rest of the application.
     */
    protected final AvaIre avaire;

    /**
     * Instantiates the middleware and sets the avaire class instance.
     *
     * @param avaire The AvaIre application class instance.
     */
    public Middleware(AvaIre avaire) {
        this.avaire = avaire;
    }

    /**
     * Instantiates the middleware and sets the avaire class instance through the plugin instance.
     *
     * @param plugin The AvaIre application class instance.
     */
    public Middleware(JavaPlugin plugin) {
        this.avaire = plugin.getAvaire();
    }

    /**
     * Builds the help description that should be displayed when the help command is used
     * for a command that uses the middleware, if null is returned the middleware will
     * be omitted from the help command.
     *
     * @param arguments The arguments that was given to the middleware for the current command.
     * @return Possibly-null, the description of the middleware, or null if no description should be displayed.
     */
    @Nullable
    public String buildHelpDescription(@Nonnull String[] arguments) {
        return null;
    }

    /**
     * Invoked by the middleware stack, handles the middleware request message
     * event, on success the {@link MiddlewareStack#next()} method should be
     * called to call the next middleware in the chain, on failure the
     * method should return false.
     *
     * @param message The JDA message object.
     * @param stack   The middleware stack for the current command.
     * @param args    The arguments given the current middleware.
     * @return Invoke {@link MiddlewareStack#next()} on success, false on failure.
     */
    public abstract boolean handle(@Nonnull Message message, @Nonnull MiddlewareStack stack, String... args);

    /**
     * Checks the message cache to see if the user has received an error message in
     * the last 2½ seconds, if they did the callback will be ignored and the
     * previous value will be returned instead, if they haven't received
     * an error message in the last 2½ seconds, the callback will be
     * invoked and the returned value will be cached.
     *
     * @param message  The JDA message that invoked the middleware stack.
     * @param callback The callback that should be invoked to send the message to the user.
     * @return The value returned by the callback, or the previous value returned by the callback if
     * the user has received an error message in the last 2½ seconds.
     */
    protected boolean runMessageCheck(@Nonnull Message message, @Nonnull Callable<Boolean> callback) {
        return (boolean) CacheUtil.getUncheckedUnwrapped(messageCache, message.getAuthor().getIdLong(), callback);
    }
}
