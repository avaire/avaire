package com.avairebot.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Job;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookClientBuilder;
import net.dv8tion.jda.webhook.WebhookMessageBuilder;

import java.util.ArrayList;
import java.util.List;

public class SendWebhookMessagesJob extends Job {

    private static final List<MessageEmbed> MESSAGE_EMBEDS = new ArrayList<>();

    public SendWebhookMessagesJob(AvaIre avaire) {
        super(avaire);
    }

    public static void addMessageEmbed(AvaIre avaire, MessageEmbed embed) {
        if (isWebhookEnabled(avaire)) {
            MESSAGE_EMBEDS.add(embed);
        }
    }

    private static boolean isWebhookEnabled(AvaIre avaire) {
        return avaire.getConfig().getString("webhook.id", "").trim().length() > 0 &&
            avaire.getConfig().getString("webhook.token", "").trim().length() > 0;
    }

    @Override
    public void run() {
        if (!avaire.areWeReadyYet()) {
            return;
        }

        if (!isWebhookEnabled(avaire)) {
            MESSAGE_EMBEDS.clear();
            return;
        }

        if (MESSAGE_EMBEDS.isEmpty()) {
            return;
        }

        WebhookMessageBuilder builder = new WebhookMessageBuilder()
            .setAvatarUrl(avaire.getSelfUser().getAvatarUrl())
            .setUsername(avaire.getSelfUser().getName());

        for (MessageEmbed embed : MESSAGE_EMBEDS) {
            builder.addEmbeds(embed);
        }

        WebhookClient client = new WebhookClientBuilder(
            Long.parseLong(avaire.getConfig().getString("webhook.id", "0")),
            avaire.getConfig().getString("webhook.token", "")
        ).build();

        client.send(builder.build());
    }
}
