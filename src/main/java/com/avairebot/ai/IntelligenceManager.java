package com.avairebot.ai;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.contracts.ai.Intent;
import com.avairebot.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IntelligenceManager {

    private final static String ACTION_OUTPUT = "Executing Intelligence Action \"%action%\" for:"
        + "\n\t\tUser:\t %author%"
        + "\n\t\tServer:\t %server%"
        + "\n\t\tChannel: %channel%"
        + "\n\t\tMessage: %message%"
        + "\n\t\tResponse: %response%";

    private final static Map<IntentAction, Intent> INTENTS = new HashMap<>();

    private final AvaIre avaire;
    private final ExecutorService executor;

    private boolean enabled = false;
    private AIDataService service;

    public IntelligenceManager(AvaIre avaire) {
        String dialogFlowClientToken = avaire.getConfig().getString("apiKeys.dialogflow", "invalid");
        if (dialogFlowClientToken.length() != 32) {
            executor = null;
            this.avaire = null;
            return;
        }

        this.avaire = avaire;

        executor = Executors.newFixedThreadPool(2);
        service = new AIDataService(new AIConfiguration(dialogFlowClientToken));
        enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean registerIntent(Intent intent) {
        if (!isEnabled()) {
            return false;
        }

        INTENTS.put(new IntentAction(intent.getAction()), intent);
        return true;
    }

    public void request(Message message, String request) {
        if (!isEnabled()) {
            return;
        }

        String[] split = request.split(" ");

        executor.submit(() -> sendRequest(message, String.join(" ",
            Arrays.copyOfRange(split, 1, split.length)
        ).trim()));
    }

    private void sendRequest(Message message, String request) {
        try {
            AIResponse response = service.request(new AIRequest(request));

            String action = response.getResult().getAction();
            AvaIre.getLogger().info(ACTION_OUTPUT
                .replace("%action%", action)
                .replace("%author%", generateUsername(message))
                .replace("%server%", generateServer(message))
                .replace("%channel%", generateChannel(message))
                .replace("%message%", message.getRawContent())
                .replace("%response%", response.getResult().getFulfillment().getSpeech())
            );

            if (response.getStatus().getCode() != 200) {
                MessageFactory.makeError(message, response.getStatus().getErrorDetails()).queue();
                return;
            }

            for (Map.Entry<IntentAction, Intent> entry : INTENTS.entrySet()) {
                if (entry.getKey().isWildcard() && action.startsWith(entry.getKey().getAction())) {
                    entry.getValue().onIntent(message, response);
                    return;
                }

                if (entry.getKey().getAction().equals(action)) {
                    entry.getValue().onIntent(message, response);
                }
            }
        } catch (AIServiceException e) {
            e.printStackTrace();
        }
    }

    private String generateUsername(Message message) {
        return String.format("%s#%s [%s]",
            message.getAuthor().getName(),
            message.getAuthor().getDiscriminator(),
            message.getAuthor().getId()
        );
    }

    private String generateServer(Message message) {
        if (!message.getChannelType().isGuild()) {
            return "PRIVATE";
        }

        return String.format("%s [%s]",
            message.getGuild().getName(),
            message.getGuild().getId()
        );
    }

    private CharSequence generateChannel(Message message) {
        if (!message.getChannelType().isGuild()) {
            return "PRIVATE";
        }

        return String.format("%s [%s]",
            message.getChannel().getName(),
            message.getChannel().getId()
        );
    }

    public Set<Map.Entry<IntentAction, Intent>> entrySet() {
        return INTENTS.entrySet();
    }
}
