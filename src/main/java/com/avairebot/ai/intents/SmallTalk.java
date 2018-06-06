package com.avairebot.ai.intents;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.ai.Intent;
import com.avairebot.utilities.StringReplacementUtil;

public class SmallTalk extends Intent {

    public SmallTalk(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public String getAction() {
        return "smalltalk.*";
    }

    @Override
    public void onIntent(CommandMessage context, AIResponse response) {
        String nickname = context.getAuthor().getName();
        if (context.getMessage().getChannelType().isGuild()) {
            nickname = context.getMember().getEffectiveName();
        }

        context.makeInfo(StringReplacementUtil.replaceAll(
            response.getResult().getFulfillment().getSpeech(),
            "%nick%", nickname
        )).queue();
    }
}
