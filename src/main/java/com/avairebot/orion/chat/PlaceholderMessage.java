package com.avairebot.orion.chat;

import com.avairebot.orion.contracts.chat.Restable;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class PlaceholderMessage extends Restable {

    private EmbedBuilder builder;
    private String message;

    public PlaceholderMessage(MessageChannel channel, EmbedBuilder builder, String message) {
        super(channel);

        this.builder = builder;
        this.message = message;
    }

    public PlaceholderMessage(EmbedBuilder builder, String message) {
        super(null);

        this.builder = builder;
        this.message = message;
    }

    public PlaceholderMessage set(String placeholder, String value) {
        message = message.replaceAll(":" + placeholder, value);
        return this;
    }

    public PlaceholderMessage set(String placeholder, Object value) {
        return set(placeholder, value.toString());
    }

    public PlaceholderMessage setTitle(String title, String url) {
        builder.setTitle(title, url);
        return this;
    }

    public PlaceholderMessage setTitle(String title) {
        return setTitle(title, null);
    }

    public PlaceholderMessage setFooter(String text, String iconUrl) {
        builder.setFooter(text, iconUrl);
        return this;
    }

    public PlaceholderMessage setFooter(String text) {
        return setFooter(text, null);
    }

    public PlaceholderMessage addField(String name, String value, boolean inline) {
        builder.addField(name, value, inline);
        return this;
    }

    public PlaceholderMessage addField(MessageEmbed.Field field) {
        builder.addField(field);
        return this;
    }

    public EmbedBuilder build() {
        return builder.setDescription(message);
    }

    @Override
    public MessageEmbed buildEmbed() {
        return builder.setDescription(message).build();
    }

    @Override
    public String toString() {
        return message;
    }
}
