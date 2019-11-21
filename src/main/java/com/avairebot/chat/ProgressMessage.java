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
import net.dv8tion.jda.core.utils.Checks;

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

    /**
     * Creates a new progress message instance for the given channel with the given
     * message, the message will be displayed at the top of the embed progress
     * message, and will be visible the whole time the message is processed.
     *
     * @param channel The channel the message should be sent in.
     * @param message The message that should be included in the progress message.
     */
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

    /**
     * Creates a new progress message instance for the
     * given channel with no primary message.
     *
     * @param channel The channel the message should be sent in.
     */
    public ProgressMessage(MessageChannel channel) {
        this(channel, null);
    }

    /**
     * Sets a finished message that should be displayed after all
     * the tasks have been completed successfully.
     *
     * @param finishMessage The finish message that should be displayed after
     *                      all the tasks have been processed successfully.
     * @return The current {@code ProgressMessage} instance.
     */
    public ProgressMessage setFinishMessage(String finishMessage) {
        this.finishMessage = finishMessage;
        return this;
    }

    /**
     * Sets the title of the embed message that is generated for the process message,
     * with the URL redirect that should be used by the title for redirects.
     *
     * @param title The title that should be set to the embed message.
     * @param url   The URL that the title should redirect to when clicked.
     * @return The current {@code ProgressMessage} instance.
     */
    public ProgressMessage setTitle(String title, String url) {
        builder.setTitle(trimString(title, MessageEmbed.TITLE_MAX_LENGTH), url);
        return this;
    }

    /**
     * Sets the title of the embed message that is generated for the process message.
     *
     * @param title The title that should be set to the embed message.
     * @return The current {@code ProgressMessage} instance.
     */
    public ProgressMessage setTitle(String title) {
        return setTitle(title, null);
    }

    /**
     * Sets the footer of the embed message that is generated for
     * the process message, with the given footer icon.
     *
     * @param text    The footer that should be set to the embed message.
     * @param iconUrl The footer icon that should be set in to the embed message.
     * @return The current {@code ProgressMessage} instance.
     */
    public ProgressMessage setFooter(String text, String iconUrl) {
        builder.setFooter(trimString(text, MessageEmbed.TITLE_MAX_LENGTH), iconUrl);
        return this;
    }

    /**
     * Sets the footer of the embed message that is generated for the process message.
     *
     * @param text The footer that should be set to the embed message.
     * @return The current {@code ProgressMessage} instance.
     */
    public ProgressMessage setFooter(String text) {
        return setFooter(text, null);
    }

    /**
     * Sets the build emote that will be used for the given progress build step.
     *
     * @param status The build step status that should have its emote replaced.
     * @param emote  The emote that should be used for the
     * @return The current {@code ProgressMessage} instance.
     */
    public ProgressMessage setBuildStepEmote(@Nonnull ProgressStepStatus status, @Nonnull String emote) {
        Checks.notNull(status, "Progress step status");
        Checks.notNull(emote, "Emote");

        emtoes.put(status, emote);
        return this;
    }

    /**
     * Sets the color of the embed message if the
     * progress message finishes successfully.
     *
     * @param color The color that should be set if the progress
     *              message finishes successfully.
     * @return The current {@code ProgressMessage} instance.
     */
    public ProgressMessage setSuccessColor(Color color) {
        this.successColor = color;
        return this;
    }

    /**
     * Sets the color of the embed message if the
     * progress message finishes with a failure.
     *
     * @param color The color that should be set if the progress
     *              message finishes with a failure.
     * @return The current {@code ProgressMessage} instance.
     */
    public ProgressMessage setFailureColor(Color color) {
        this.failureColor = color;
        return this;
    }

    /**
     * Adds a build step that should be processed when the message is queued,
     * the progress message steps are processed in sequential order. In the
     * event that the step fails with an exception, or returns false,
     * the exception error message will be sent and the step chain
     * will be stopped.
     *
     * @param message The message that describes the build step.
     * @param closure The closure that should be invoked to handle the progress build step.
     * @return The current {@code ProgressMessage} instance.
     */
    public ProgressMessage addStep(String message, ProgressClosure closure) {
        return addStep(message, closure, null);
    }

    /**
     * Adds a build step that should be processed when the message is queued,
     * the progress message steps are processed in sequential order. In the
     * event that the step fails with an exception, or returns false,
     * the given failure message will be sent and the step chain
     * will be stopped.
     * <p>
     * The failure message can include ":error" somewhere to include
     * the exception message that made the build step fail.
     *
     * @param message        The message that describes the build step.
     * @param closure        The closure that should be invoked to handle the progress build step.
     * @param failureMessage The message that should be displayed if the build step fails.
     * @return The current {@code ProgressMessage} instance.
     */
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
        if (steps.isEmpty()) {
            throw new IllegalStateException("Can't generate progress message without any build steps!");
        }

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
    protected void handleSuccessConsumer(Message message, Consumer<Message> success) {
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
        super.handleSuccessConsumer(message, success);
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
