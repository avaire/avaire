package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.middleware.Middleware;
import net.dv8tion.jda.core.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class MiddlewareHandler {

    private static final Map<String, Middleware> middlewares = new HashMap<>();

    /**
     * Gets a middleware by name, the name will ignore letter casing.
     *
     * @param name The name of the middleware that should be fetched.
     * @return Possibly-null, the instance of the middleware with the given name.
     */
    @Nullable
    public static Middleware getMiddleware(@Nonnull String name) {
        return middlewares.getOrDefault(name.toLowerCase(), null);
    }

    /**
     * Returns the name of a registered middleware by class.
     *
     * @param clazz The type of class that should be returned.
     * @return The name of the registered middleware by class.
     */
    @Nullable
    public static String getName(@Nonnull Class<? extends Middleware> clazz) {
        for (Map.Entry<String, Middleware> middleware : middlewares.entrySet()) {
            if (middleware.getValue().getClass().getSimpleName().equalsIgnoreCase(clazz.getSimpleName())) {
                return middleware.getKey();
            }
        }
        return null;
    }

    /**
     * Registers a middleware with the given name, middlewares can be used through
     * the {@link Command#getMiddleware() getMiddleware()} method, middleware
     * names will ignore letter casing and there can't be two middlewares
     * registered with the same name at the same time.
     *
     * @param name       The name of the middleware.
     * @param middleware The middleware instance that should be linked to the given name.
     * @throws IllegalArgumentException This is thrown if a middleware is already registered with the given name.
     */
    public static void register(@Nonnull String name, @Nonnull Middleware middleware) {
        Checks.notNull(name, "Middleware name");
        Checks.notNull(middleware, "Middleware");

        if (middlewares.containsKey(name.toLowerCase())) {
            throw new IllegalArgumentException(name + " has already been registered as a middleware");
        }
        middlewares.put(name.toLowerCase(), middleware);
    }

    /**
     * Initializes and prepares the middleware containers and
     * the middleware stack builder to handle commands.
     *
     * @param avaire The AvaIre application instance.
     */
    public static void initialize(AvaIre avaire) {
        MiddlewareStack.buildGlobalMiddlewares(avaire);
    }
}
