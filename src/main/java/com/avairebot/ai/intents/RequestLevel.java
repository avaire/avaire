package com.avairebot.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.commands.CommandContainer;
import com.avairebot.commands.CommandHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.utility.RankCommand;
import com.avairebot.contracts.ai.Intent;
import com.avairebot.database.transformers.GuildTransformer;
import com.avairebot.factories.MessageFactory;

public class RequestLevel extends Intent {

    public RequestLevel(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getAction() {
        return "request.level";
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onIntent(CommandMessage context, AIResponse response) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null || !guildTransformer.isLevels()) {
            MessageFactory.makeWarning(context.getMessage(),
                "This server doesn't have the `Levels & Experience` feature enabled so I can't tell you what level you are :("
            ).queue();
            return;
        }

        CommandContainer container = CommandHandler.getCommand(RankCommand.class);
        container.getCommand().onCommand(
            new CommandMessage(container, context.getDatabaseEventHolder(), context.getMessage()), new String[]{"---skip-mentions"}
        );
    }
}
