package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.administration.ToggleCategoryCommand;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.ChannelTransformer;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.Message;

import java.util.concurrent.TimeUnit;

public class isCategoryEnabled extends Middleware {

    public isCategoryEnabled(AvaIre avaire) {
        super(avaire);
    }

    @SuppressWarnings("ConstantConditions")
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
            if (isHelpCommand(stack) && stack.isMentionableCommand()) {
                MessageFactory.makeError(message, "The help command is disable in this channel, you can enable it by using the `:category` command.")
                    .set("category", CommandHandler.getCommand(ToggleCategoryCommand.class).getCommand().generateCommandTrigger(message))
                    .queue(success -> success.delete().queueAfter(15, TimeUnit.SECONDS, null, RestActionUtil.IGNORE));
            }

            return false;
        }

        return stack.next();
    }

    private boolean isCategoryCommands(MiddlewareStack stack) {
        return stack.getCommand().getClass().getTypeName().equals("com.avairebot.commands.administration.ToggleCategoryCommand") ||
            stack.getCommand().getClass().getTypeName().equals("com.avairebot.commands.administration.CategoriesCommand");
    }

    private boolean isHelpCommand(MiddlewareStack stack) {
        return stack.getCommand().getClass().getTypeName().equals("com.avairebot.commands.help.HelpCommand");
    }
}
