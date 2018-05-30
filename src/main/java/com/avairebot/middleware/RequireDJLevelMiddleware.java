package com.avairebot.middleware;

import com.avairebot.AvaIre;
import com.avairebot.audio.AudioHandler;
import com.avairebot.audio.DJGuildLevel;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.administration.IAmCommand;
import com.avairebot.contracts.middleware.DJCheckMessage;
import com.avairebot.contracts.middleware.Middleware;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;

public class RequireDJLevelMiddleware extends Middleware {

    public RequireDJLevelMiddleware(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public boolean handle(@Nonnull Message message, @Nonnull MiddlewareStack stack, String... args) {
        if (!message.getChannelType().isGuild()) {
            return stack.next();
        }

        if (args.length > 0) {
            DJGuildLevel level = DJGuildLevel.fromName(args[0]);

            if (level != null && AudioHandler.getDefaultAudioHandler().canRunDJAction(avaire, message, level)) {
                return stack.next();
            }

            return sendErrorMessage(message, stack);
        }

        if (AudioHandler.getDefaultAudioHandler().canRunDJAction(avaire, message, DJGuildLevel.NORMAL)) {
            return stack.next();
        }

        return sendErrorMessage(message, stack);
    }

    @SuppressWarnings("ConstantConditions")
    private boolean sendErrorMessage(Message message, MiddlewareStack stack) {
        String djcheckMessage = "The `DJ` Discord role is required to run this command!";

        DJCheckMessage annotation = stack.getCommand().getClass().getAnnotation(DJCheckMessage.class);
        if (annotation != null && annotation.message().trim().length() > 0) {
            if (annotation.overwrite()) {
                djcheckMessage = annotation.message();
            } else {
                djcheckMessage += annotation.message();
            }
        }

        GuildTransformer guildTransformer = stack.getDatabaseEventHolder().getGuild();
        if (guildTransformer != null && guildTransformer.getSelfAssignableRoles().containsValue("dj")) {
            djcheckMessage += "\nYou can use the `:iam DJ` command to get the role!";
        }

        CommandContainer command = CommandHandler.getCommand(IAmCommand.class);
        MessageFactory.makeError(message, djcheckMessage)
            .set("iam", command.getCommand().generateCommandTrigger(message))
            .set("prefix", stack.getCommand().generateCommandPrefix(message))
            .queue();

        return false;
    }
}
