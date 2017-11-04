package com.avairebot.orion.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.ai.Intent;
import com.avairebot.orion.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

public class SmallTalk extends Intent {

    public SmallTalk(Orion orion) {
        super(orion);
    }

    @Override
    public String getAction() {
        return "smalltalk.*";
    }

    @Override
    public void onIntent(Message message, AIResponse response) {
        String nickname = message.getAuthor().getName();
        if (message.getChannelType().isGuild()) {
            nickname = message.getMember().getEffectiveName();
        }

        MessageFactory.makeInfo(message,
            response.getResult().getFulfillment().getSpeech().replaceAll("%nick%", nickname)
        ).queue();
    }
}
