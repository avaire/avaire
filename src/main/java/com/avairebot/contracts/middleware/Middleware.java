package com.avairebot.contracts.middleware;

import com.avairebot.AvaIre;
import com.avairebot.middleware.MiddlewareStack;
import com.avairebot.plugin.JavaPlugin;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Middleware {

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
}
