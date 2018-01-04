package com.avairebot.contracts.ai;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.contracts.reflection.Reflectionable;
import net.dv8tion.jda.core.entities.Message;

public abstract class Intent extends Reflectionable {

    public Intent(AvaIre avaire) {
        super(avaire);
    }

    public abstract String getAction();

    public abstract void onIntent(Message message, AIResponse response);
}
