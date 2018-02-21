package com.avairebot.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.logger.EventLogger;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookClientBuilder;
import net.dv8tion.jda.webhook.WebhookMessageBuilder;

public class SendWebhookMessagesJob extends Job {

    public SendWebhookMessagesJob(AvaIre avaire) {
        super(avaire);
    }

    @Override
    public void run() {
        if (!avaire.areWeReadyYet()) {
            return;
        }

        if (EventLogger.getGuildMessages().isEmpty()) {
            return;
        }

        WebhookClient client = new WebhookClientBuilder(
            Long.parseLong(avaire.getConfig().getString("webhook.id", "0")),
            avaire.getConfig().getString("webhook.token", "")
        ).build();

        client.send(new WebhookMessageBuilder()
            .setAvatarUrl(avaire.getSelfUser().getEffectiveAvatarUrl())
            .setUsername(avaire.getSelfUser().getName())
            .addEmbeds(EventLogger.pullGuildMessages())
            .build()
        );

        client.close();
    }
}
