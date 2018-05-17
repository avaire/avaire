package com.avairebot.contracts.ai;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.reflection.Reflectionable;

public abstract class Intent extends Reflectionable {

    public Intent(AvaIre avaire) {
        super(avaire);
    }

    public abstract String getAction();

    public abstract void onIntent(CommandMessage message, AIResponse response);
}
