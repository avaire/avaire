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

package com.avairebot.chat;

import com.avairebot.contracts.chat.ProgressClosure;
import com.avairebot.contracts.chat.ProgressStep;
import com.avairebot.contracts.chat.ProgressStepStatus;
import com.avairebot.contracts.chat.Restable;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

public class ProgressMessage extends Restable {

    private final List<ProgressStep> steps = new ArrayList<>();
    private final EnumMap<ProgressStepStatus, String> emtoes = new EnumMap<>(ProgressStepStatus.class);

    private final String message;
    private final EmbedBuilder builder;

    private String finishMessage;

    private Color successColor;
    private Color failureColor;

    public ProgressMessage(MessageChannel channel, String message) {
        super(channel);

        this.message = message;
        this.builder = new EmbedBuilder();

        this.successColor = MessageType.SUCCESS.getColor();
        this.failureColor = MessageType.ERROR.getColor();

        for (ProgressStepStatus status : ProgressStepStatus.values()) {
            emtoes.put(status, status.getDefaultEmote());
        }
    }

    public ProgressMessage(MessageChannel channel) {
        this(channel, null);
    }

    public ProgressMessage setFinishMessage(String finishMessage) {
        this.finishMessage = finishMessage;
        return this;
    }

    public ProgressMessage setTitle(String title, String url) {
        builder.setTitle(trimString(title, MessageEmbed.TITLE_MAX_LENGTH), url);
        return this;
    }

    public ProgressMessage setTitle(String title) {
        return setTitle(title, null);
    }

    public ProgressMessage setFooter(String text, String iconUrl) {
        builder.setFooter(trimString(text, MessageEmbed.TITLE_MAX_LENGTH), iconUrl);
        return this;
    }

    public ProgressMessage setFooter(String text) {
        return setFooter(text, null);
    }

    public ProgressMessage setBuildStepEmote(@Nonnull ProgressStepStatus status, String emote) {
        emtoes.put(status, emote);
        return this;
    }

    public void setSuccessColor(Color successColor) {
        this.successColor = successColor;
    }

    public void setFailureColor(Color failureColor) {
        this.failureColor = failureColor;
    }

    public ProgressMessage addStep(String message, ProgressClosure closure) {
        return addStep(message, closure, null);
    }

    public ProgressMessage addStep(String message, ProgressClosure closure, String failureMessage) {
        steps.add(new ProgressStep(message, closure, failureMessage));
        return this;
    }

    @Override
    public MessageEmbed buildEmbed() {
        Color embedColor = successColor;
        for (ProgressStep step : steps) {
            if (step.isCompleted() && !step.getStatus().getValue()) {
                embedColor = failureColor;
                break;
            }

            if (!step.isCompleted()) {
                embedColor = null;
                break;
            }
        }

        builder.setColor(embedColor);
        builder.setDescription(toString());

        return builder.build();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (message != null) {
            builder.append(message)
                .append("\n");
        }

        boolean isCurrentTask = true;
        boolean isCompleted = true;
        String failureMessage = null;

        for (ProgressStep step : steps) {
            if (step.isCompleted() && !step.getStatus().getValue()) {
                failureMessage = step.getFailureMessage();
                if (failureMessage == null && step.getException() != null) {
                    failureMessage = step.getException().getMessage();
                }

                failureMessage = new PlaceholderMessage(null, failureMessage)
                    .set("error", step.getException() == null
                        ? "Unknown error"
                        : step.getException().getMessage()
                    ).toString();
            }

            if (!step.isCompleted()) {
                isCompleted = false;
            }

            String emote = emtoes.get(step.getStatus());
            if (isCurrentTask && failureMessage == null && !step.isCompleted()) {
                isCurrentTask = false;
                emote = emtoes.get(ProgressStepStatus.RUNNING);
            }

            builder.append(emote)
                .append(" ")
                .append(step.getMessage())
                .append("\n");
        }

        if (failureMessage != null) {
            builder.append("\n")
                .append("\uD83D\uDCE2 ")
                .append(failureMessage);
        } else if (isCompleted && finishMessage != null) {
            builder.append("\n")
                .append("\uD83D\uDCE3 ")
                .append(finishMessage);
        }

        return builder.toString().trim();
    }

    @Override
    protected Consumer<Message> handleSuccessConsumer(Message message, Consumer<Message> success) {
        for (ProgressStep step : steps) {
            if (step.isCompleted()) {
                continue;
            }

            boolean result;

            try {
                result = step.run();
            } catch (FriendlyException e) {
                result = false;
                step.setException(e);
            }

            MessageAction messageAction = getChannelPermissionType().canSendEmbed()
                ? message.editMessage(buildEmbed())
                : message.editMessage(toString());

            boolean finalResult = result;
            messageAction.queue(editMessage -> {
                if (finalResult) {
                    handleSuccessConsumer(editMessage, success);
                }
            });

            break;
        }
        return super.handleSuccessConsumer(message, success);
    }

    private String trimString(String string, int length) {
        if (string == null) {
            return null;
        }

        if (string.length() < length) {
            return string;
        }

        return string.substring(0, length);
    }
}