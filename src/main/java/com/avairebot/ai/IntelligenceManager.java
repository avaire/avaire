/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.ai;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.chat.ConsoleColor;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.ai.Intent;
import com.avairebot.factories.MessageFactory;
import com.avairebot.handlers.DatabaseEventHolder;
import com.avairebot.metrics.Metrics;
import io.prometheus.client.Histogram;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IntelligenceManager {

    private final static String actionOutput = ConsoleColor.format("%cyanExecuting Intelligence Action \"%reset%action%%cyan\" for:"
        + "\n\t\t%cyanUser:\t %author%"
        + "\n\t\t%cyanServer:\t %server%"
        + "\n\t\t%cyanChannel: %channel%"
        + "\n\t\t%cyanMessage: %reset%message%"
        + "\n\t\t%cyanResponse: %reset%response%");

    private final static String propertyOutput = ConsoleColor.format(
        "%reset%s %cyan[%reset%s%cyan]"
    );

    private final static Map<IntentAction, Intent> intents = new HashMap<>();

    private final ExecutorService executor;

    private boolean enabled = false;
    private AIDataService service;

    public IntelligenceManager(AvaIre avaire) {
        String dialogFlowClientToken = avaire.getConfig().getString("apiKeys.dialogflow", "invalid");
        if (dialogFlowClientToken.length() != 32) {
            executor = null;
            return;
        }


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

        Metrics.aiRequestsExecuted.labels(intent.getClass().getSimpleName()).inc(0D);

        intents.put(new IntentAction(intent.getAction()), intent);
        return true;
    }

    public void request(Message message, DatabaseEventHolder databaseEventHolder, String request) {
        if (!isEnabled()) {
            return;
        }

        Metrics.aiRequestsReceived.inc();

        String[] split = request.split(" ");

        executor.submit(() -> sendRequest(message, databaseEventHolder, String.join(" ",
            Arrays.copyOfRange(split, 1, split.length)
        ).trim()));
    }

    private void sendRequest(Message message, DatabaseEventHolder databaseEventHolder, String request) {
        try {
            AIResponse response = service.request(new AIRequest(request));

            String action = response.getResult().getAction();
            AvaIre.getLogger().info(actionOutput
                .replace("%action%", action)
                .replace("%author%", generateUsername(message))
                .replace("%server%", generateServer(message))
                .replace("%channel%", generateChannel(message))
                .replace("%message%", message.getContentRaw())
                .replace("%response%", response.getResult().getFulfillment().getSpeech())
            );

            if (response.getStatus().getCode() != 200) {
                MessageFactory.makeError(message, response.getStatus().getErrorDetails()).queue();
                return;
            }

            for (Map.Entry<IntentAction, Intent> entry : intents.entrySet()) {
                if (entry.getKey().isWildcard() && action.startsWith(entry.getKey().getAction())) {
                    invokeIntent(message, databaseEventHolder, response, entry.getValue());
                    return;
                }

                if (entry.getKey().getAction().equals(action)) {
                    invokeIntent(message, databaseEventHolder, response, entry.getValue());
                    return;
                }
            }
        } catch (AIServiceException e) {
            e.printStackTrace();
        }
    }

    private void invokeIntent(Message message, DatabaseEventHolder databaseEventHolder, AIResponse response, Intent intent) {
        Metrics.aiRequestsExecuted.labels(intent.getClass().getSimpleName()).inc();
        Histogram.Timer timer = Metrics.aiExecutionTime.labels(intent.getClass().getSimpleName()).startTimer();

        intent.onIntent(new CommandMessage(
            null, databaseEventHolder, message
        ), response);

        timer.observeDuration();
    }

    private String generateUsername(Message message) {
        return String.format(propertyOutput,
            message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator(),
            message.getAuthor().getId()
        );
    }

    private String generateServer(Message message) {
        if (!message.getChannelType().isGuild()) {
            return ConsoleColor.GREEN + "PRIVATE";
        }

        return String.format(propertyOutput,
            message.getGuild().getName(),
            message.getGuild().getId()
        );
    }

    private CharSequence generateChannel(Message message) {
        if (!message.getChannelType().isGuild()) {
            return ConsoleColor.GREEN + "PRIVATE";
        }

        return String.format(propertyOutput,
            message.getChannel().getName(),
            message.getChannel().getId()
        );
    }

    public Set<Map.Entry<IntentAction, Intent>> entrySet() {
        return intents.entrySet();
    }
}
