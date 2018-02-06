package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.DJGuildLevel;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.administration.IAmCommand;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.database.controllers.GuildController;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

public class RequireDJLevel extends Middleware {

    public RequireDJLevel(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public boolean handle(Message message, MiddlewareStack stack, String... args) {
        if (!message.getChannelType().isGuild()) {
            return stack.next();
        }

        if (args.length > 0) {
            DJGuildLevel level = DJGuildLevel.fromName(args[0]);

            if (level != null && AudioHandler.canRunDJAction(avaire, message, level)) {
                return stack.next();
            }

            return sendErrorMessage(message);
        }

        if (AudioHandler.canRunDJAction(avaire, message, DJGuildLevel.NORMAL)) {
            return stack.next();
        }

        return sendErrorMessage(message);
    }

    @SuppressWarnings("ConstantConditions")
    private boolean sendErrorMessage(Message message) {
        GuildTransformer guildTransformer = GuildController.fetchGuild(avaire, message);
        if (guildTransformer != null && guildTransformer.getSelfAssignableRoles().containsValue("dj")) {
            CommandContainer command = CommandHandler.getCommand(IAmCommand.class);
            MessageFactory.makeError(message, "The `DJ` Discord role is required to run this command.\nYou can use the `:iam DJ` command to get the role!")
                .set("iam", command.getCommand().generateCommandTrigger(message))
                .queue();
            return false;
        }

        MessageFactory.makeError(message, "The `DJ` Discord role is required to run this command!").queue();

        return false;
    }
}
