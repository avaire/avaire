package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import net.dv8tion.jda.core.entities.Message;

public class isCategoryEnabled extends Middleware {

    public isCategoryEnabled(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        if (!message.getChannelType().isGuild()) {
            return stack.next();
        }

        if (isCategoryCommands(stack)) {
            return stack.next();
        }

        GuildTransformer transformer = GuildController.fetchGuild(avaire, message.getGuild());
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
        return stack.getCommand().getClass().getTypeName().equals("com.avairebot.avaire.commands.administration.ToggleCategoryCommand") ||
            stack.getCommand().getClass().getTypeName().equals("com.avairebot.avaire.commands.administration.CategoriesCommand");
    }
}
