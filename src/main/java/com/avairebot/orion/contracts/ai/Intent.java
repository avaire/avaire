package com.avairebot.orion.contracts.ai;

import ai.api.model.AIResponse;
import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.reflection.Reflectionable;
import net.dv8tion.jda.core.entities.Message;

public abstract class Intent extends Reflectionable {

    public Intent(Orion orion) {
        super(orion);
    }

    public abstract String getAction();

    public abstract void onIntent(Message message, AIResponse response);
}
