package com.avairebot.contracts.ai;

import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.reflection.Reflectionable;

public abstract class Intent extends Reflectionable {

    public Intent(AvaIre avaire) {
        super(avaire);
    }

    /**
     * Gets the AI action key, the action key is returned by the AI to tell what
     * sort of action or intent that was behind the message the user sent, the
     * actions can use wildcards by defining the action using a star(*).
     * <p>
     * An example of a valid wildcard action would be <code>example.intent.*</code>,
     * the intent would be invoked whenever a response from the AI is returned
     * that starts with <code>example.intent.</code>, but not if it only
     * starts with <code>example.intent</code>.
     *
     * @return The AI action that is used to determine what should invoke the intent.
     */
    public abstract String getAction();

    /**
     * Handles the AI intent with the command message and AI responses returned from the AI.
     *
     * @param message  The command message that was used to invoke the AI intent.
     * @param response The response returned from the AI.
     */
    public abstract void onIntent(CommandMessage message, AIResponse response);
}
