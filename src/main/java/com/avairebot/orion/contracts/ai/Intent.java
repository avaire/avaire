package com.avairebot.orion.contracts.ai;

import ai.api.model.AIResponse;
import com.avairebot.orion.Orion;
import net.dv8tion.jda.core.entities.Message;

public abstract class Intent {

    protected final Orion orion;

    public Intent(Orion orion) {
        this.orion = orion;
    }

    public abstract String getAction();

    public abstract void onIntent(Message message, AIResponse response);
}
