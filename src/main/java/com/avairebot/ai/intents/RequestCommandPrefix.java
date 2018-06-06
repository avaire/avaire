package com.avairebot.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.commands.Category;
import com.avairebot.commands.CategoryHandler;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.ai.Intent;

import java.util.ArrayList;
import java.util.List;

public class RequestCommandPrefix extends Intent {

    public RequestCommandPrefix(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getAction() {
        return "command.prefix";
    }

    @Override
    public void onIntent(CommandMessage context, AIResponse response) {
        List<String> prefixes = new ArrayList<>();
        for (Category category : CategoryHandler.getValues()) {
            if (category.isGlobal()) continue;

            prefixes.add(
                String.format("`%s` %s", category.getPrefix(context.getMessage()), category.getName())
            );
        }

        context.makeSuccess(
            "Here is all my prefixes for this server.\n\n" +
                String.join("\n", prefixes)
        ).queue();
    }
}
