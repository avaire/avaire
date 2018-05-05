package com.avairebot.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.contracts.ai.Intent;
import com.avairebot.factories.MessageFactory;
import com.avairebot.utilities.StringReplacementUtil;
import net.dv8tion.jda.core.entities.Message;

public class SmallTalk extends Intent {

    public SmallTalk(AvaIre avaire) {
        super(avaire);
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

        MessageFactory.makeInfo(message, StringReplacementUtil.replaceAll(
            response.getResult().getFulfillment().getSpeech(),
            "%nick%", nickname
        )).queue();
    }
}
