/*
 * Copyright (c) 2019.
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

package com.avairebot.ai.dialogflow;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.chat.ConsoleColor;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.ai.IntelligenceService;
import com.avairebot.contracts.ai.Intent;
import com.avairebot.factories.MessageFactory;
import com.avairebot.handlers.DatabaseEventHolder;
import com.avairebot.metrics.Metrics;
import com.avairebot.utilities.AutoloaderUtil;
import io.prometheus.client.Histogram;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DialogFlowService implements IntelligenceService {

    private static final Logger log = LoggerFactory.getLogger(DialogFlowService.class);

    private final static String actionOutput = ConsoleColor.format(
        "%cyanExecuting Intelligence Action \"%reset%action%%cyan\" for:"
            + "\n\t\t%cyanUser:\t %author%"
            + "\n\t\t%cyanServer:\t %server%"
            + "\n\t\t%cyanChannel: %channel%"
            + "\n\t\t%cyanMessage: %reset%message%"
            + "\n\t\t%cyanResponse: %reset%response%"
    );

    private final static String propertyOutput = ConsoleColor.format(
        "%reset%s %cyan[%reset%s%cyan]"
    );

    private final static Map<IntentAction, Intent> intents = new HashMap<>();

    private ExecutorService executor;
    private AIDataService service;

    @Override
    public boolean isEnabled() {
        return service != null && !executor.isShutdown();
    }

    @Override
    public void registerService(AvaIre avaire) {
        String dialogFlowClientToken = avaire.getConfig().getString("apiKeys.dialogflow", "invalid");
        if (dialogFlowClientToken.length() != 32) {
            executor = null;
            return;
        }

        executor = Executors.newFixedThreadPool(2);
        service = new AIDataService(new AIConfiguration(dialogFlowClientToken));

        log.info("Registering DialogFlow intents...");
        AutoloaderUtil.load(Constants.PACKAGE_INTENTS_PATH, intent -> {
            Metrics.aiRequestsExecuted.labels(intent.getClass().getSimpleName()).inc(0D);

            intents.put(new IntentAction(((Intent) intent).getAction()), (Intent) intent);
        });
        log.info(String.format("\tRegistered %s DialogFlow intelligence intents successfully!", intents.size()));
    }

    @Override
    public void unregisterService(AvaIre avaire) {
        if (executor != null) {
            executor.shutdownNow();
        }
        service = null;
    }

    @Override
    public void onMessage(Message message, DatabaseEventHolder databaseEventHolder) {
        if (intents.isEmpty()) {
            return;
        }

        String[] split = message.getContentStripped().split(" ");
        executor.submit(() -> processRequest(message, databaseEventHolder, String.join(" ",
            Arrays.copyOfRange(split, 1, split.length)
        ).trim()));
    }

    private void processRequest(Message message, DatabaseEventHolder databaseEventHolder, String request) {
        try {
            AIResponse response = service.request(new AIRequest(request));

            String action = response.getResult().getAction();
            log.info(actionOutput
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
}
