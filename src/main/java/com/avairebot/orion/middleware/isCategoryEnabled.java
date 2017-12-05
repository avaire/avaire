package com.avairebot.orion.middleware;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.middleware.Middleware;
import com.avairebot.orion.database.controllers.GuildController;
import com.avairebot.orion.database.transformers.ChannelTransformer;
import com.avairebot.orion.database.transformers.GuildTransformer;
import net.dv8tion.jda.core.entities.Message;

public class isCategoryEnabled extends Middleware {

    public isCategoryEnabled(Orion orion) {
        super(orion);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        if (!message.getChannelType().isGuild()) {
            return stack.next();
        }

        if (isCategoryCommands(stack)) {
            return stack.next();
        }

        GuildTransformer transformer = GuildController.fetchGuild(orion, message.getGuild());
        if (transformer == null) {
            return stack.next();
        }

        ChannelTransformer channel = transformer.getChannel(message.getChannel().getId());
        if (channel == null) {
            return stack.next();
        }

        if (!channel.isCategoryEnabled(stack.getCommandContainer().getCategory())) {
            return false;
        }

        return stack.next();
    }

    private boolean isCategoryCommands(MiddlewareStack stack) {
        return stack.getCommand().getClass().getTypeName().equals("com.avairebot.orion.commands.administration.ToggleCategoryCommand") ||
            stack.getCommand().getClass().getTypeName().equals("com.avairebot.orion.commands.administration.CategoriesCommand");
    }
}
